# NutriCare_SSAFY
영양 관리·식단 분석을 목표로 한 SSAFY 팀 프로젝트입니다. Spring Boot 기반 백엔드(커뮤니티, 인증, 파일 업로드)와 ResNet18 기반 이미지 분석 FastAPI 모듈을 함께 제공합니다.

- 백엔드: Spring Boot 3.5.8, MyBatis, MySQL 8, JWT(JJWT), BCrypt, Swagger(springdoc), Google Cloud Storage 업로드
- AI: FastAPI, PyTorch ResNet18(MLflow 모델 로드), Google Auth

## 서비스 흐름(요약)
1) 사용자가 회원가입/로그인(JWT 발급) 후 사진을 업로드하면 GCS에 저장되고 DB(photo)에 메타데이터가 기록됩니다.
2) FastAPI 추론 서버가 MLflow에 저장된 ResNet18 모델을 불러와 사진을 분류하고 진단명을 반환합니다.
3) 향후 분석 결과(analysis_result)와 식단 추천(diet_*)을 연동할 수 있도록 테이블이 미리 정의되어 있습니다.
4) 커뮤니티 게시글/댓글을 통해 정보 공유가 가능합니다.

## 주요 기능
- 인증/권한
  - 회원가입(BCrypt 해시), 로그인 시 JWT 발급, `/user/me` 자기 정보 조회·수정·탈퇴
  - 인터셉터로 JWT 검증, `/admin/**`는 role=ADMIN 사용자만 접근
- 커뮤니티
  - 게시글 CRUD 및 조회수 증가, 게시글 이미지 다중 업로드(board_image)
  - 댓글 CRUD
- 사진/분석 파이프라인
  - 사진 메타데이터 저장(photo), 분석/식단 추천용 테이블(analysis_result, diet_*) 사전 정의
  - GCS 업로드 API: 게시글 이미지 `/file-api/upload-board-image`, 사진 업로드+DB 저장 `/file-api/upload-with-meta`
- 문서/테스트
  - Swagger UI: `http://localhost:8080/swagger-ui/index.html`
  - DB 스키마 스크립트: `back/res/sql.sql`

## 폴더 구조
- `back/NutriCare_SSAFY`: Spring Boot 프로젝트
  - `src/main/java/com/nutricare/config`: MyBatis, Swagger, GCS, WebMvc/JWT 인터셉터 설정, `.env` 로더
  - `src/main/java/com/nutricare/controller`: User/Admin, Board, Comment, Photo, 파일 업로드 REST 컨트롤러
  - `src/main/java/com/nutricare/interceptor`: `JwtInterceptor`, `AdminInterceptor`
  - `src/main/java/com/nutricare/model`: DTO/DAO/Service, `src/main/resources/mappers/*.xml`
  - `src/main/resources/application.properties`: DB·GCS·JWT 설정 예시
- `back/res/sql.sql`: MySQL 스키마/테이블 생성 스크립트
- `AI/src`: ResNet18 추론(`resnet_mlflow.py`), FastAPI 엔드포인트(`resnet_api.py`)
- `AI/data/train`: 클래스 이름 로드용 ImageFolder 디렉터리(없으면 경고 로그)
- `AI/notebooks/mlruns/.../artifacts/model`: MLflow로 저장된 기본 모델 경로

## 사전 준비
- JDK 17+, Maven 3.9+, MySQL 8.x
- Python 3.10+ (가상환경 권장), CUDA 환경이 없으면 CPU로 동작
- GCS 서비스 계정 키(json)와 버킷(예: `nutricare-images`)
- `.env` 또는 시스템 환경변수로 아래 값을 설정

```
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/nutricare_db?serverTimezone=UTC
SPRING_DATASOURCE_USERNAME=ssafy
SPRING_DATASOURCE_PASSWORD=ssafy

JWT_SECRET=this_is_super_long_jwt_secret_key_1234   # 32바이트 이상

GCS_BUCKET_NAME=nutricare-images
GCS_BASE_URL=https://storage.googleapis.com
GCS_PREFIX_BOARD=board-images/
GCS_PREFIX_PHOTO=photo-images/
GOOGLE_APPLICATION_CREDENTIALS=back/NutriCare_SSAFY/concrete-fabric-479604-h3-cd9c0c483611.json
```

> `DotenvEnvironmentPostProcessor`가 루트 또는 클래스패스의 `.env`를 자동 로드합니다. 민감 정보는 Git에 커밋하지 마세요.

## 백엔드 실행
1) DB 초기화: `mysql -u <user> -p < back/res/sql.sql`
2) 설정 확인: `back/NutriCare_SSAFY/src/main/resources/application.properties` 또는 `.env`에서 DB/GCS/JWT 값을 실제 환경에 맞게 수정
3) 실행
   - Windows: `cd back/NutriCare_SSAFY && mvnw.cmd spring-boot:run`
   - macOS/Linux: `cd back/NutriCare_SSAFY && ./mvnw spring-boot:run`
4) API 문서: `http://localhost:8080/swagger-ui/index.html`

## AI 추론 서버 실행(FastAPI)
1) 의존성 설치
```
python -m venv .venv
.\\.venv\\Scripts\\activate  # macOS/Linux: source .venv/bin/activate
pip install -r requirements.txt
```
2) 필요 시 클래스/모델 경로 수정
   - 클래스 이름: `AI/data/train` 디렉터리 하위 폴더명 자동 로드
   - 모델 경로: `AI/src/resnet_mlflow.py`의 `DEFAULT_MODEL_URI`를 MLflow 모델 위치로 변경하거나 스크립트 실행 시 `--model-uri` 인자 사용
3) API 서버 실행
```
uvicorn src.resnet_api:app --host 0.0.0.0 --port 8000
```
   - 엔드포인트: `POST /analyze` (`photo_id`, `user_id`, `photo_url`) → `diagnosis_name` 반환

## 참고/주의
- `application.properties`는 Git에 한 번 추적된 파일입니다. 무시하려면 `git rm --cached back/NutriCare_SSAFY/src/main/resources/application.properties` 후 커밋하세요.
- 파일 업로드 기본 로컬 경로는 `C:/nutricare_images/`로 매핑되어 있습니다. 운영/개발 프로필별로 분리하려면 프로퍼티를 추가로 구성하세요.
- 일부 경로(@Operation 설명, PhotoController의 PathVariable 등)는 실제 API 경로와 불일치할 수 있어 점검이 필요합니다. Swagger에서 확인 후 보완하세요.
