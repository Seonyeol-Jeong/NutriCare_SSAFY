from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

from resnet_mlflow import predict_image, _load_image_from_url_or_path, CLASS_NAMES, DEFAULT_MODEL_URI


class PredictRequest(BaseModel):
    photo_id: int
    user_id: int
    photo_url: str  # 로컬 경로 또는 http/https URL


app = FastAPI(title="ResNet18 Inference (MLflow)", version="1.0")


@app.post("/analyze", summary="예측 결과 반환")
def predict_endpoint(body: PredictRequest):
    """입력 JSON({photo_id,user_id,photo_url})을 받아 추론 후 결과(JSON)를 반환한다."""
    try:
        local_path = _load_image_from_url_or_path(body.photo_url)
        # CLASS_NAMES는 AI/data/train 하위 폴더명을 자동 로드한 값
        pred, probs = predict_image(local_path, class_names=CLASS_NAMES)
    except FileNotFoundError as exc:
        raise HTTPException(status_code=400, detail=str(exc))
    except Exception as exc:
        raise HTTPException(status_code=500, detail=f"예상치 못한 오류: {exc}")

    # analysis_id는 DB AUTO_INCREMENT로 생성되므로 여기서는 None 반환
    return {
        "analysis_id": None,
        "photo_id": body.photo_id,
        "diagnosis_name": str(pred),
    }
