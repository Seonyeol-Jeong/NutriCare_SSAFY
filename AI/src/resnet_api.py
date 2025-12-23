from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import logging
import numpy as np

from resnet_mlflow import predict_image, _load_image_from_url_or_path, CLASS_NAMES

class PredictRequest(BaseModel):
    photo_id: int
    user_id: int
    photo_url: str
    top_k: int = 3

app = FastAPI(title="ResNet18 Inference (MLflow)", version="1.0")

logger = logging.getLogger(__name__)

@app.post("/analyze", summary="예측 결과 반환")
def predict_endpoint(body: PredictRequest):
    logger.info("예측 요청 수신 photo_id=%s user_id=%s", body.photo_id, body.user_id)
    logger.info("이미지 로드 시작: %s", body.photo_url)
    try:
        local_path = _load_image_from_url_or_path(body.photo_url)
        logger.info("이미지 로드 완료 local_path=%s", local_path)
        logger.info("추론 시작")
        pred_label, probs = predict_image(local_path, class_names=CLASS_NAMES)
        logger.info("추론 완료 pred=%s", pred_label)
        logger.info("확률 분포(probs)=%s", probs.tolist())
    except FileNotFoundError as exc:
        logger.warning("이미지 찾을 수 없음: %s", exc)
        raise HTTPException(status_code=400, detail=str(exc))
    except Exception as exc:
        logger.exception("예상치 못한 추론 오류")
        raise HTTPException(status_code=500, detail=f"예상치 못한 오류: {exc}")

    probs_list = probs.tolist()
    class_names = CLASS_NAMES or [str(i) for i in range(len(probs_list))]
    if CLASS_NAMES is None:
        logger.info("CLASS_NAMES 미설정: 인덱스 라벨로 대체")
    top_k = max(1, min(body.top_k, len(probs_list)))
    topk_indices = np.argsort(probs)[::-1][:top_k]

    topk_payload = [
        {"index": int(i), "name": class_names[i], "prob": float(probs[i])}
        for i in topk_indices
    ]

    logger.info("예측 응답 반환")
    return {
        "analysis_id": None,
        "photo_id": body.photo_id,
        "diagnosis_name": str(pred_label),
        "probabilities": probs_list,
        "top_k": topk_payload,
        "class_names": class_names,
    }
