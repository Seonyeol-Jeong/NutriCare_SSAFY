# NutriCare

피부 상태 분석과 개인 맞춤 식단 추천을 연결한 풀스택 프로젝트입니다.  
얼굴 사진을 업로드하면 ResNet18이 피부 상태를 분류하고, 결과와 건강 프로필을 바탕으로 QLoRA 기반 VLM이 식단을 생성합니다.

![Intro](front_vue/nutricare_front/src/assets/IntroImg1.png)

## 핵심 기능
- 피부 사진 업로드 및 AI 분석 결과 저장 (진단 라벨 + 클래스별 확률)
- 건강 정보 기반 맞춤 식단 추천 (VLM + QLoRA)
- 게시판/댓글/이미지 업로드
- JWT 로그인, Google OAuth, 권한 분리 (USER/ADMIN)

## 기술 스택
- Frontend: Vue 3, Vite, Pinia, Axios, Bootstrap
- Backend: Spring Boot 3.5.8, MyBatis, Spring Security, JWT, GCS, Swagger
- AI: FastAPI, PyTorch(ResNet18), MLflow, Transformers, PEFT(QLoRA)
- DB: MySQL 8

## 시스템 구성
- Frontend: `front_vue/nutricare_front` (Vite)
- Backend: `back/NutriCare_SSAFY` (Spring Boot)
- AI 분석: `AI/src/resnet_api.py` (ResNet18 + MLflow, port 8001)
- AI 식단: `AI/src/diet_api.py` (Qwen3-VL QLoRA, port 8000)
- DB: MySQL (`nutricare_db`)

## 디렉터리 구조
- `front_vue/nutricare_front`: Vue 프론트엔드
- `back/NutriCare_SSAFY`: Spring Boot 백엔드 + MyBatis
- `AI/src`: FastAPI 추론 API (ResNet, Diet)
- `AI/notebooks`: 모델 학습/실험 노트북
- `back/res/sql.sql`: DB 스키마

## 실행 방법

### 1) DB 준비
```bash
# MySQL에서 실행
source back/res/sql.sql
```

### 2) 백엔드 설정
`back/NutriCare_SSAFY/.env`에 아래 항목을 설정하세요.
- `JWT_SECRET`
- `spring.datasource.*`
- `gcs.*` (bucket, credentials)
- `spring.security.oauth2.*`
- `STT_API_KEY`, `STT_URL`

`back/NutriCare_SSAFY/src/main/resources/application.properties`에서 AI 서버 주소를 확인하세요.
```
ai.fastapi.url=http://<AI_HOST>:8001/analyze
ai.diet.url=http://<AI_HOST>:8000/diet/generate
```

### 3) 백엔드 실행
```bash
cd back/NutriCare_SSAFY
mvn spring-boot:run
```

### 4) AI 서버 실행
```bash
pip install -r requirements.txt
cd AI/src
uvicorn resnet_api:app --host 0.0.0.0 --port 8001
uvicorn diet_api:app --host 0.0.0.0 --port 8000
```

`diet_api.py`는 아래 환경변수를 참고합니다.
- `DIET_BASE_MODEL` (기본값: `Qwen/Qwen3-VL-8B-Instruct`)
- `DIET_ADAPTER_PATH` (기본값: `../notebooks/qlora-adapter`)

### 5) 프론트엔드 실행
```bash
cd front_vue/nutricare_front
npm install
npm run dev
```

## 주요 API (요약)

### Backend (Spring Boot)
- 사용자: `POST /api/users`, `POST /api/users/login`, `GET /api/users/me`
- 건강정보: `GET /api/health-profiles/me`, `POST /api/health-profiles/me`
- 사진: `POST /api/user-photos` (multipart), `GET /api/user-photos/users/{userId}`
- 분석결과: `POST /api/analysis-results`, `GET /api/analysis-results/photos/{photoId}`
- 식단: `POST /api/diet-recommendations/create`, `POST /api/diet-recommendations/{recId}`, `GET /api/diet-recommendations/{recId}`
- 게시판: `GET /api/boards`, `POST /api/boards`, `PUT /api/boards/{boardId}`, `DELETE /api/boards/{boardId}`
- 댓글: `GET /api/boards/{boardId}/comments`, `POST /api/boards/{boardId}/comments`, `PUT /api/comments/{commentId}`, `DELETE /api/comments/{commentId}`
- 이미지: `POST /api/board-images`
- 기타: `GET /api/admin/users`, `POST /api/voice/command`

### AI (FastAPI)
- 피부 분석: `POST /analyze`
- 식단 생성: `POST /diet/generate`

## 분석 결과 저장 컬럼
`analysis_result` 테이블은 클래스별 확률을 컬럼으로 저장합니다.
- `prob_gunsun`, `prob_atopy`, `prob_acne`, `prob_normal`, `prob_rosacea`, `prob_seborr`

## 참고
- Swagger: `http://localhost:8082/swagger-ui/index.html`
- DB 스키마: `back/res/sql.sql`
