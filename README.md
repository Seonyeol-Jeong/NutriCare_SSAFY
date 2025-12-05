# NutriCare_SSAFY

영양 상태 분석과 맞춤 식단 추천을 제공하는 SSAFY 팀 프로젝트입니다.  
- 백엔드: Spring Boot 3.5 + MyBatis 기반 REST API (JWT 인증, GCS 업로드, 식단/RAG 연동)  
- AI: FastAPI + MLflow로 로드한 ResNet18 추론 서비스
Git remote repo: https://github.com/Seonyeol-Jeong/NutriCare_SSAFY/tree/main/back/NutriCare_SSAFY/src/main/java/com/nutricare

## 레포지토리 구성
- `back/NutriCare_SSAFY`: Spring Boot 애플리케이션, MyBatis 매퍼, JWT 인터셉터, GCS 업로더
- `back/res/sql.sql`: MySQL 스키마 및 기본 데이터 스크립트
- `AI/src/resnet_api.py`, `AI/src/resnet_mlflow.py`: FastAPI 추론 엔드포인트와 MLflow 모델 로더
- `AI/notebooks/*`: MLflow 실행 결과(모델 아티팩트)
- `requirements.txt`: FastAPI/MLflow/PyTorch 등 AI 서비스 의존성

## 백엔드 주요 기능
- 인증/인가: 회원가입·로그인(JWT 발급), 내 정보 조회·수정·탈퇴, ROLE(USER/ADMIN) 관리, 관리자 전용 전체 사용자 조회(`/admin/all`)
- 게시판: 게시글 CRUD, 조회수 증가, 댓글 CRUD, 게시글 이미지(GCS 업로드) 매핑
- 사진 관리: JWT 기반 사진 업로드(`/file-api/upload-with-meta`) 후 DB 기록, 사진 단건/사용자별 조회, 삭제
- 건강·식단: diet_recommendation/diet_result CRUD, `/api/rag/diet/{recId}`에서 컨텍스트를 모아 FastAPI RAG 서비스 호출 후 diet_result 저장
- 문서화/보안: Swagger UI(`/swagger-ui/index.html`)에서 Bearer Token 입력 후 테스트 가능, `/admin/**`는 role=ADMIN 필요

## 백엔드 실행 가이드
1) 준비물: JDK 17+, Maven 3.9+, MySQL 8+, (선택) GCS 서비스 계정  
2) DB: `back/res/sql.sql`로 `nutricare_db` 생성/초기화  
3) 환경변수: `back/NutriCare_SSAFY/.env`의 `JWT_SECRET`, `GOOGLE_APPLICATION_CREDENTIALS` 등을 채우고 `src/main/resources/application.properties`에서 DB 계정 및 `gcs.*`(bucket/base-url/prefix/credentials-path)을 환경에 맞게 수정  
4) 실행: `cd back/NutriCare_SSAFY && mvn spring-boot:run`  
5) 테스트: `http://localhost:8080/swagger-ui/index.html` 접속 → Authorize 버튼에서 Bearer 입력  
6) GCS: `gcs.bucket-name`, `gcs.prefix-board`, `gcs.prefix-photo`, `gcs.credentials-path`를 운영 경로로 교체

## AI/추론 서비스
- 설치: `pip install -r requirements.txt` (PyTorch CUDA 버전은 환경에 맞게 조정)
- 기본 모델: `AI/notebooks/mlruns/.../artifacts/model`에 저장된 ResNet18, 클래스 이름은 `AI/data/train` 하위 폴더에서 자동 로드
- 실행: `cd AI/src && uvicorn resnet_api:app --host 0.0.0.0 --port 8000`
- 예시 요청:
```bash
curl -X POST http://localhost:8000/analyze \
  -H "Content-Type: application/json" \
  -d '{"photo_id":1,"user_id":1,"photo_url":"file:///path/to/image.jpg"}'
```
- 응답: `{"analysis_id": null, "photo_id": 1, "diagnosis_name": "<예측값>"}`
- Spring 측 RAG 연동은 `http://fastapi-server:8000/rag/diet` 엔드포인트가 존재한다고 가정하며 `/api/rag/diet/{recId}`에서 호출

## 주요 API
- 사용자: `POST /user`, `POST /user/login`, `GET|PATCH|DELETE /user/me`, `POST /user/logout`
- 관리자: `GET /admin/all`
- 게시판: `GET/POST /board-api/board`, `GET/PUT/DELETE /board-api/board/{boardId}`
- 댓글: `GET/POST /board-api/board/{boardId}/comment`, `PUT/DELETE /board-api/comment/{commentId}`
- 파일/GCS: `POST /file-api/upload-board-image`, `POST /file-api/upload-with-meta`(JWT)
- 사진: `GET /photo-api/photo/detail/{photoId}`, `GET /photo-api/photo/{userId}`, `POST /photo-api/photo`, `DELETE /photo-api/photo/{photoId}`
- 식단: `GET /diet-api/result/list/{recId}`, `GET /diet-api/result/{mealId}`, `POST /diet-api/result`, `PUT /diet-api/result`, `DELETE /diet-api/result/{mealId}`
- RAG: `POST /api/rag/diet/{recId}` – 컨텍스트 조회 → FastAPI 호출 → diet_result 저장

## 참고 및 제약
- `AnalysisResult` 관련 API/매퍼는 미완성 상태이며, FastAPI 분석 결과를 DB에 적재하는 로직을 추가해야 합니다.
- FastAPI RAG 엔드포인트 구현/배포는 레포에 포함되어 있지 않으므로 별도 구성 후 백엔드 URL을 맞춰야 합니다.
