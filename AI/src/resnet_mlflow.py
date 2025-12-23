"""
MLflow에 저장된 ResNet18을 불러와 추론만 수행하는 모듈.
- 기본 모델 URI: AI/notebooks/mlruns/... 경로로 설정
- API는 resnet_api.py에서 제공.
- 동시성 대비: 프로세스 내에서 모델을 1회 로드 후 캐시하여 재사용.
- 클래스 이름: 기본적으로 AI/data/train 하위 폴더명을 자동으로 로드.
"""

import argparse
import os
import tempfile
from pathlib import Path
from typing import Dict, List, Optional, Tuple

import mlflow.pytorch
import numpy as np
import requests
import torch
from PIL import Image
from dotenv import load_dotenv
from torchvision import transforms
from google.auth.transport.requests import AuthorizedSession
from google.oauth2 import service_account

IMAGENET_MEAN = [0.485, 0.456, 0.406]
IMAGENET_STD = [0.229, 0.224, 0.225]

_ROOT_DIR = Path(__file__).resolve().parents[2]
load_dotenv(_ROOT_DIR / "back" / "NutriCare_SSAFY" / ".env")
if "GOOGLE_APPLICATION_CREDENTIALS" in os.environ:
    cred_path = (_ROOT_DIR / os.environ["GOOGLE_APPLICATION_CREDENTIALS"]).resolve()
    os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = str(cred_path)

# 프로젝트 루트(AI) 기준 경로 설정
_BASE_DIR = Path(__file__).resolve().parents[1]  # AI 디렉터리

# 기본 모델 경로: AI/notebooks/mlruns/358423444567344628/d8b7f62c6dd64ce5bc0b3855fcff0991/artifacts/model
DEFAULT_MODEL_URI = str(
    _BASE_DIR/"notebooks/mlruns/358423444567344628/d8b7f62c6dd64ce5bc0b3855fcff0991/artifacts/model"
)

# 클래스 이름을 자동 로드할 기본 경로: AI/mixture_data
DEFAULT_CLASS_DIR = "../mixture_data"

# (model_uri, device.type) -> (model, device)
_MODEL_CACHE: Dict[Tuple[str, str], Tuple[torch.nn.Module, torch.device]] = {}
_AUTH_SESSION: Optional[AuthorizedSession] = None


def _get_authed_session() -> Optional[AuthorizedSession]:
    """GCS 인증 세션 생성 (키 없으면 None)."""
    global _AUTH_SESSION
    if _AUTH_SESSION:
        return _AUTH_SESSION

    cred_path = os.environ.get("GOOGLE_APPLICATION_CREDENTIALS")
    if not cred_path:
        return None

    creds = service_account.Credentials.from_service_account_file(
        cred_path,
        scopes=["https://www.googleapis.com/auth/devstorage.read_only"],
    )
    _AUTH_SESSION = AuthorizedSession(creds)
    return _AUTH_SESSION


def load_class_names_from_dir(class_dir: Path) -> Optional[List[str]]:
    """ImageFolder 구조의 하위 디렉터리명을 클래스 이름 리스트로 반환."""
    class_dir = Path(class_dir)
    if not class_dir.exists():
        print(f"[WARN] 클래스 경로가 없습니다: {class_dir}")
        return None
    names = [p.name for p in class_dir.iterdir() if p.is_dir()]
    if not names:
        print(f"[WARN] 클래스 디렉터리가 비어 있습니다: {class_dir}")
        return None
    names.sort()
    print(f"[INFO] 클래스 로드: {names}")
    return names


CLASS_NAMES = load_class_names_from_dir(DEFAULT_CLASS_DIR)


def build_transform(img_size: int) -> transforms.Compose:
    """ResNet18 학습 설정에 맞춰 리사이즈/정규화."""
    return transforms.Compose([
        transforms.Resize((img_size, img_size)),
        transforms.ToTensor(),
        transforms.Normalize(mean=IMAGENET_MEAN, std=IMAGENET_STD),
    ])


def load_model(model_uri: str = DEFAULT_MODEL_URI, device: Optional[torch.device] = None, use_cache: bool = True) -> Tuple[torch.nn.Module, torch.device]:
    """MLflow에서 내보낸 PyTorch 모델을 로드. 캐시를 사용해 중복 로드를 방지."""
    device = device or torch.device("cuda" if torch.cuda.is_available() else "cpu")
    cache_key = (model_uri, device.type)

    if use_cache and cache_key in _MODEL_CACHE:
        model, cached_device = _MODEL_CACHE[cache_key]
        return model, cached_device

    print(f"[INFO] 모델 로드 시작: {model_uri} (device={device})")
    model = mlflow.pytorch.load_model(model_uri, map_location=device)
    model = model.to(device)
    model.eval()
    print("[INFO] 모델 로드 완료, eval 모드 전환")

    if use_cache:
        _MODEL_CACHE[cache_key] = (model, device)
    return model, device


def _load_image_from_url_or_path(photo_url: str) -> str:
    """http/https면 임시 파일에 다운로드, 아니면 로컬 경로 확인 후 반환."""
    if photo_url.startswith("http://") or photo_url.startswith("https://"):
        print(f"[INFO] 원격 이미지 다운로드: {photo_url}")
        session = _get_authed_session() or requests
        resp = session.get(photo_url, timeout=10)
        resp.raise_for_status()
        tmp_file = tempfile.NamedTemporaryFile(delete=False, suffix=Path(photo_url).suffix or ".img")
        tmp_file.write(resp.content)
        tmp_file.flush()
        print(f"[INFO] 임시 파일 저장: {tmp_file.name}")
        return tmp_file.name
    if not Path(photo_url).exists():
        raise FileNotFoundError(f"파일을 찾을 수 없습니다: {photo_url}")
    print(f"[INFO] 로컬 이미지 사용: {photo_url}")
    return photo_url


def predict_image(
    image_path: str,
    model_uri: str = DEFAULT_MODEL_URI,
    img_size: int = 224,
    class_names: Optional[List[str]] = None,
    use_cache: bool = True,
) -> Tuple[object, np.ndarray]:
    """단일 이미지 추론 후 (라벨/클래스명, 확률분포) 반환. 모델 캐시를 재사용."""
    print(f"[INFO] 추론 시작 - 이미지: {image_path}, img_size: {img_size}")
    model, device = load_model(model_uri=model_uri, use_cache=use_cache)
    tfm = build_transform(img_size)

    img = Image.open(image_path).convert("RGB")
    tensor = tfm(img).unsqueeze(0).to(device)
    print(f"[INFO] 입력 텐서 shape: {tuple(tensor.shape)}")

    with torch.no_grad():
        logits = model(tensor)
        probs = torch.softmax(logits, dim=1).squeeze(0).cpu().numpy()

    pred_idx = int(np.argmax(probs))
    active_classes = class_names or CLASS_NAMES
    pred_label = active_classes[pred_idx] if (active_classes and pred_idx < len(active_classes)) else pred_idx
    print(f"[INFO] 추론 완료 - pred_idx: {pred_idx}, pred_label: {pred_label}, max_prob: {float(probs[pred_idx]):.4f}")
    return pred_label, probs


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="MLflow 모델을 사용하는 ResNet18 추론 전용 스크립트")
    parser.add_argument("--image-path", required=True, help="추론할 이미지 경로 또는 URL")
    parser.add_argument("--model-uri", default=DEFAULT_MODEL_URI, help="MLflow 모델 URI 또는 로컬 경로")
    parser.add_argument("--img-size", type=int, default=224, help="입력 리사이즈 크기")
    parser.add_argument("--class-dir", type=str, default=str(DEFAULT_CLASS_DIR), help="클래스 이름을 로드할 ImageFolder train 경로")
    parser.add_argument("--class-names", type=str, default=None, help="쉼표로 구분된 클래스 이름 목록 (선택)")
    parser.add_argument("--no-cache", action="store_true", help="모델 캐시 사용 안 함")
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    # 우선순위: --class-names > --class-dir > DEFAULT_CLASS_DIR
    if args.class_names:
        class_names = args.class_names.split(",")
    else:
        class_names = load_class_names_from_dir(Path(args.class_dir)) or CLASS_NAMES

    image_path = _load_image_from_url_or_path(args.image_path)
    pred, probs = predict_image(
        image_path=image_path,
        model_uri=args.model_uri,
        img_size=args.img_size,
        class_names=class_names,
        use_cache=not args.no_cache,
    )
    print(f"Prediction: {pred}")
    print(f"Probabilities: {probs.tolist()}")


if __name__ == "__main__":
    main()
