import io
import json
import os
import re
from pathlib import Path
from typing import List, Optional

import requests
import torch
from fastapi import FastAPI, HTTPException
from peft import PeftModel
from pydantic import BaseModel, Field
from transformers import AutoModelForVision2Seq, AutoProcessor, BitsAndBytesConfig
from PIL import Image

# QLoRA adapter 경로/베이스 모델은 env로 덮어쓸 수 있습니다.
_ROOT_DIR = Path(__file__).resolve().parents[1]  # AI 디렉터리
BASE_MODEL = os.environ.get("DIET_BASE_MODEL", "Qwen/Qwen3-VL-8B-Instruct")
ADAPTER_PATH = Path(os.environ.get("DIET_ADAPTER_PATH", _ROOT_DIR / "notebooks" / "qlora-adapter"))


class DietContextModel(BaseModel):
    user_id: Optional[int] = Field(None, alias="userId")
    user_name: Optional[str] = Field(None, alias="userName")
    birth_year: Optional[int] = Field(None, alias="birthYear")
    gender: Optional[str]
    height_cm: Optional[float] = Field(None, alias="heightCm")
    weight_kg: Optional[float] = Field(None, alias="weightKg")
    activity_level: Optional[str] = Field(None, alias="activityLevel")
    health_goal_type: Optional[str] = Field(None, alias="healthGoalType")
    health_updated_at: Optional[str] = Field(None, alias="healthUpdatedAt")
    diagnosis_name: Optional[str] = Field(None, alias="diagnosisName")
    analysis_created_at: Optional[str] = Field(None, alias="analysisCreatedAt")
    photo_id: Optional[int] = Field(None, alias="photoId")
    photo_url: Optional[str] = Field(None, alias="photoUrl")
    rec_id: Optional[int] = Field(None, alias="recId")
    rec_memo: Optional[str] = Field(None, alias="recMemo")
    rec_created_at: Optional[str] = Field(None, alias="recCreatedAt")


class CaloriePlanModel(BaseModel):
    bmr: Optional[int] = None
    tdee: Optional[int] = None
    target_calories: Optional[int] = Field(None, alias="targetCalories")
    activity_factor: Optional[float] = Field(None, alias="activityFactor")
    age_used: Optional[int] = Field(None, alias="ageUsed")
    gender_used: Optional[str] = Field(None, alias="genderUsed")
    goal_type: Optional[str] = Field(None, alias="goalType")


class DietItem(BaseModel):
    menuName: str
    description: str
    calories: int
    notes: Optional[str] = None
    recipeUrl: Optional[str] = None
    skincareUrl: Optional[str] = None


class DietLlmRequest(BaseModel):
    context: DietContextModel
    caloriePlan: Optional[CaloriePlanModel] = None
    rulesText: Optional[str] = None


class DietResult(BaseModel):
    rec_id: int
    menu_name: str
    description: Optional[str] = None
    calories: int
    notes: Optional[str] = None
    recipe_url: Optional[str] = None


app = FastAPI(title="Diet Generation (Qwen3-VL QLoRA)", version="1.1")

_MODEL = None
_PROCESSOR = None


def _load_image_for_vlm(url: str) -> Image.Image:
    if not url:
        raise HTTPException(status_code=400, detail="photo_url is required for diet generation")
    if url.startswith("http://") or url.startswith("https://"):
        resp = requests.get(url, timeout=10)
        resp.raise_for_status()
        return Image.open(io.BytesIO(resp.content)).convert("RGB")
    if not Path(url).exists():
        raise HTTPException(status_code=400, detail=f"cannot find image: {url}")
    return Image.open(url).convert("RGB")


def _load_model_once():
    """베이스 모델 + QLoRA 어댑터를 1회만 로드."""
    global _MODEL, _PROCESSOR
    if _MODEL and _PROCESSOR:
        return _MODEL, _PROCESSOR

    if not ADAPTER_PATH.exists():
        raise RuntimeError(f"adapter path not found: {ADAPTER_PATH}")

    bnb_config = BitsAndBytesConfig(
        load_in_4bit=True,
        bnb_4bit_use_double_quant=True,
        bnb_4bit_quant_type="nf4",
        bnb_4bit_compute_dtype=torch.bfloat16,
    )

    processor = AutoProcessor.from_pretrained(
        BASE_MODEL,
        trust_remote_code=True,
    )
    base = AutoModelForVision2Seq.from_pretrained(
        BASE_MODEL,
        quantization_config=bnb_config,
        device_map="auto",
        trust_remote_code=True,
    )
    model = PeftModel.from_pretrained(base, ADAPTER_PATH)
    model.eval()

    _MODEL, _PROCESSOR = model, processor
    return _MODEL, _PROCESSOR


def _build_messages(ctx: DietContextModel, plan: Optional[CaloriePlanModel], rules: Optional[str]) -> List[dict]:
    goal = plan.target_calories if plan and plan.target_calories else 550
    sys_prompt = "너는 얼굴 이미지를 참고하여 아침/점심/저녁 식단 JSON 배열만 생성하는 영양사 모델이다. JSON 외 텍스트는 금지한다."
    user_text = (
        f"진단: {ctx.diagnosis_name or '일반'}\n"
        f"키/몸무게/활동/목표: {ctx.height_cm or '-'}cm, {ctx.weight_kg or '-'}kg, {ctx.activity_level or '-'}, {ctx.health_goal_type or (plan.goal_type if plan and plan.goal_type else '-')}\n"
        f"1식 칼로리 목표: {goal} kcal\n"
        f"식이 룰: {rules or '가공식품 최소화, 단백질 우선'}\n"
        "반드시 JSON 배열만 출력하라. 키: menuName, description, calories, notes"
    )
    return [
        {"role": "system", "content": sys_prompt},
        {"role": "user", "content": [{"type": "image"}, {"type": "text", "text": user_text}]},
    ]


def _extract_json(text: str):
    """모델 출력에서 JSON 배열 블록을 추출."""
    match = re.search(r"(\\[[\\s\\S]*\\])", text)
    if not match:
        raise ValueError("no JSON array found in generation")
    block = match.group(1)
    return json.loads(block)


@app.post("/diet/generate", summary="이미지+컨텍스트로 식단 JSON 생성")
def diet_generate(body: DietLlmRequest) -> List[DietItem]:
    """
    Qwen3-VL QLoRA 어댑터로 식단을 생성하고 DietItem 리스트를 반환.
    """
    if not body.context.rec_id:
        raise HTTPException(status_code=400, detail="rec_id (diet_recommendation.rec_id) is required")

    try:
        image = _load_image_for_vlm(body.context.photo_url)
    except HTTPException:
        raise
    except Exception as exc:
        raise HTTPException(status_code=400, detail=f"이미지 로드 실패: {exc}")

    try:
        model, processor = _load_model_once()
    except Exception as exc:
        raise HTTPException(status_code=500, detail=f"모델 로드 실패: {exc}")

    messages = _build_messages(body.context, body.caloriePlan, body.rulesText)
    chat_text = processor.apply_chat_template(
        messages,
        add_generation_prompt=True,
        tokenize=False,
    )
    inputs = processor(
        images=[image],
        text=chat_text,
        return_tensors="pt",
    )
    inputs = {k: v.to(model.device) for k, v in inputs.items()}

    try:
        with torch.no_grad():
            outputs = model.generate(
                **inputs,
                max_new_tokens=512,
                do_sample=False,
            )
        decoded = processor.tokenizer.decode(outputs[0], skip_special_tokens=False)
        parsed = _extract_json(decoded)
        items = [DietItem(**item) for item in parsed]
    except Exception as exc:
        raise HTTPException(status_code=500, detail=f"생성/파싱 실패: {exc}")

    # DB 스키마(diet_result)와 동일한 필드명으로 매핑하여 반환
    return [
        DietResult(
            rec_id=body.context.rec_id,
            menu_name=item.menuName,
            description=item.description,
            calories=item.calories,
            notes=item.notes,
            recipe_url=item.recipeUrl,
        )
        for item in items
    ]
