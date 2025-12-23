from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import logging

from resnet_mlflow import predict_image, _load_image_from_url_or_path, CLASS_NAMES

class PredictRequest(BaseModel):
    photo_id: int
    user_id: int
    photo_url: str

app = FastAPI(title="ResNet18 Inference (MLflow)", version="1.0")

logger = logging.getLogger("uvicorn.error")
if not logger.handlers:
    logging.basicConfig(
        level=logging.INFO,
        format="%(asctime)s %(levelname)s %(name)s: %(message)s",
    )
logger.setLevel(logging.INFO)

@app.post("/analyze", summary="Return prediction")
def predict_endpoint(body: PredictRequest):
    logger.info("Predict request received photo_id=%s user_id=%s", body.photo_id, body.user_id)
    logger.info("Loading image from %s", body.photo_url)
    try:
        local_path = _load_image_from_url_or_path(body.photo_url)
        logger.info("Image loaded local_path=%s", local_path)
        logger.info("Running inference")
        pred_label, probs = predict_image(local_path, class_names=CLASS_NAMES)
        logger.info("Inference complete pred=%s", pred_label)
        probs_list = probs.tolist()
        class_names = CLASS_NAMES or [str(i) for i in range(len(probs_list))]
        if CLASS_NAMES is None:
            logger.info("CLASS_NAMES not set; fallback to index labels")
        logger.info(
            "Class probabilities=%s",
            {name: float(probs_list[i]) for i, name in enumerate(class_names)},
        )
        print("Class probabilities:", {name: float(probs_list[i]) for i, name in enumerate(class_names)})
    except FileNotFoundError as exc:
        logger.warning("Image not found: %s", exc)
        raise HTTPException(status_code=400, detail=str(exc))
    except Exception as exc:
        logger.exception("Unexpected inference error")
        raise HTTPException(status_code=500, detail=f"Unexpected error: {exc}")

    if "probs_list" not in locals():
        probs_list = probs.tolist()
        class_names = CLASS_NAMES or [str(i) for i in range(len(probs_list))]
        if CLASS_NAMES is None:
            logger.info("CLASS_NAMES not set; fallback to index labels")

    prob_map = {
        "prob_gunsun": float(probs_list[0]) if len(probs_list) > 0 else None,
        "prob_atopy": float(probs_list[1]) if len(probs_list) > 1 else None,
        "prob_acne": float(probs_list[2]) if len(probs_list) > 2 else None,
        "prob_normal": float(probs_list[3]) if len(probs_list) > 3 else None,
        "prob_rosacea": float(probs_list[4]) if len(probs_list) > 4 else None,
        "prob_seborr": float(probs_list[5]) if len(probs_list) > 5 else None,
    }
    response_payload = {
        "analysis_id": None,
        "photo_id": body.photo_id,
        "diagnosis_name": str(pred_label),
        **prob_map,
        "class_names": class_names,
    }
    logger.info("Returning 응답 payload=%s", response_payload)
    return response_payload
