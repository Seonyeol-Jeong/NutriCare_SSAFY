# NutriCare

영양 상태 분석과 맞춤 식단 추천을 하나로 묶은 프로젝트입니다.

![Intro](front_vue/nutricare_front/src/assets/IntroImg1.png)

## 핵심 경험
- 직관적 UI: 촬영 → 분석 → 추천, 3단계로 끝나는 흐름
- 한눈에 보는 대시보드: 섭취 기록, 영양 요약, 추천 식단
- 커뮤니티 기능: 게시판/댓글

## 기술 스택 (간단 요약)
- Frontend: Vue.js, Axios
- Backend: Spring Boot 3.5, MyBatis, JWT, GCS
- AI: FastAPI, MLflow, PyTorch(ResNet18), RAG
- DB: MySQL 8

## 주요 기능
- 회원/권한: JWT 기반 로그인, ROLE(USER/ADMIN)
- 게시판/댓글 + 이미지 업로드(GCS)
- 사진 분석 결과 저장/조회
- 식단 추천 + RAG 결과 저장

## 프로젝트 구조
- `front_vue/nutricare_front`: Vue 프론트엔드
- `back/NutriCare_SSAFY`: Spring Boot 백엔드 + MyBatis 매퍼
- `AI/src`: FastAPI 추론 API + MLflow 로딩
- `AI/notebooks`: 모델 학습/실험 기록
- `back/res/sql.sql`: MySQL 스키마/기본 데이터

## 실행 가이드 (요약)
1) DB 준비: `back/res/sql.sql`로 `nutricare_db` 생성
2) 백엔드 설정: `back/NutriCare_SSAFY/.env`에 `JWT_SECRET`, `GOOGLE_APPLICATION_CREDENTIALS` 입력 후
   `src/main/resources/application.properties`에 DB/GCS 설정 반영
3) 백엔드 실행: `cd back/NutriCare_SSAFY && mvn spring-boot:run`
4) AI 실행: `pip install -r requirements.txt` 후
   `cd AI/src && uvicorn resnet_api:app --host 0.0.0.0 --port 8000`

## 주요 API (요약)
- 사용자: `POST /user`, `POST /user/login`, `GET|PATCH|DELETE /user/me`
- 게시판: `GET/POST /board-api/board`, `PUT/DELETE /board-api/board/{boardId}`
- 댓글: `POST /board-api/board/{boardId}/comment`, `PUT/DELETE /board-api/comment/{commentId}`
- 사진: `POST /photo-api/photo`, `GET /photo-api/photo/{userId}`
- 식단: `POST /diet-api/result`, `GET /diet-api/result/list/{recId}`
- RAG: `POST /api/rag/diet/{recId}`

## 참고
- Swagger: `http://localhost:8080/swagger-ui/index.html`
- GCS 사용 시 `gcs.bucket-name`, `gcs.prefix-board`, `gcs.prefix-photo`, `gcs.credentials-path` 설정 필요
