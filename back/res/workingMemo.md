# 작업 진행상황

## 프로젝트 개요
- Spring Boot 3.5.8 + MyBatis REST API 서버, 기본 DB는 MySQL(`nutricare_db`).
- JWT 인증/인가와 Swagger(OpenAPI 2.6.0) 설정 완료, Bearer 스키마 포함.
- 주요 도메인: 회원(User), 게시판(Board/Comment/BoardImage), 파일 업로드/Photo; DB 스키마에는 건강/식단 관련 테이블(health_profile, diet_*)과 분석결과(analysis_result)도 정의됨.
- 파일 업로드는 Google Cloud Storage 사용(gcs.* 프로퍼티, `google-cloud-storage` 의존성, prefix/bucket 기반 업로드).

## 코드 구성(핵심 경로)
- `src/main/java/com/nutricare/config`: WebMvc/Swagger/인터셉터 설정, BCrypt PasswordEncoder, GCS 프로퍼티 바인딩(`GcsProperties`), GCS `Storage` 빈(`StorageConfig`), MyBatis 스캔. `NutriCareSsafyApplication`에서 `@EnableConfigurationProperties`로 등록.
- `src/main/java/com/nutricare/interceptor`: JWT 검증, 관리자 권한 검증 인터셉터(request attribute에 userId/role 세팅).
- `src/main/java/com/nutricare/controller`: 회원/관리자, 게시판, 댓글, 파일 업로드(`FileController`), 사진 메타데이터(`PhotoController`) REST 컨트롤러.
- `src/main/java/com/nutricare/model`: dto/dao/service 계층, MyBatis 매퍼(`src/main/resources/mappers/*.xml`) 연동.
- `src/main/java/com/nutricare/util/JwtUtil`: HS256 서명 JWT 생성·파싱, 기본 만료 1시간.
- 설정 파일: `src/main/resources/application.properties` (로컬 MySQL, 매퍼 위치, 멀티파트 용량, gcs.* 설정 등).

## 구현된 기능
- 회원: 회원가입(BCrypt 해시 저장), 로그인(JWT 발급), 내 정보 조회/수정/탈퇴, 로그아웃 응답; 관리자용 전체 회원 조회.
- 인증/인가: `/user/me`, `/admin/**` 등에 JWT 인터셉터 적용, 관리자 인터셉터로 role=ADMIN만 접근 허용.
- 게시판: 게시글 CRUD, 조회수 증가, 게시글 이미지 다건 등록 지원(`board_image`), 작성자 이름 조인 조회.
- 댓글: 게시글별 댓글 조회/작성/수정/소프트삭제.
- 파일 업로드(GCS): `/file-api/upload-board-image`로 게시글 이미지 다건 업로드 후 GCS URL을 `board_image`에 저장, `/file-api/upload-with-meta`로 JWT에서 userId를 읽어 업로드+`photo` 메타데이터 INSERT를 동시에 처리. `gcs.bucket-name/base-url/prefix-board/prefix-photo` 조합으로 경로 생성.
- DB 스키마: `res/sql.sql`에 user/health_profile/photo/analysis_result/diet_recommendation/diet_meal/board/comment/board_image 테이블 정의.

## 확인된 이슈·위험 요소
- `AnalysisResult` DTO/DAO/매퍼가 비어 있고 서비스/컨트롤러도 없음 → DB의 분석·식단 연계 기능이 전혀 동작하지 않음.
- `PhotoController` 사용자별 조회는 `@GetMapping("/photo")`에 `@PathVariable("userId")`를 요구해 경로가 맞지 않아 호출 불가 상태.
- GCS 자격/경로가 로컬 절대경로(`gcs.credentials-path=C:/Users/jangs/.../concrete-fabric-...json`)와 저장된 키 파일에 의존 → 환경/보안 위험, 프로필 분리 필요.
- Swagger 한글 설명과 주석이 다수 인코딩 깨짐(`AdminUserRestController`, `FileController`, `BoardController`, 인터셉터 등) → 문서/가독성 저하.
- 테스트 부족: 기본 `contextLoads` 외 통합/단위 테스트 없음. 업로드 실패/권한 검증 등에 대한 테스트 부재.
- 보안/유효성: JWT 리프레시·블랙리스트 미구현, 회원가입 입력 검증 로직은 DB 제약 외 별도 체크 없음.

## 다음에 하면 좋을 작업
- `analysis_result` 흐름 구현: DTO 필드/매퍼(SQL) 정의 → DAO/Service/Controller 작성, `diet_recommendation` 연계 포함.
- `PhotoController` 사용자별 조회 엔드포인트 경로 수정 및 통합 테스트 추가.
- GCS 설정 외부화: 프로필별 `application-{profile}.properties`/환경변수/시크릿 매니저로 버킷·자격 키 관리, 로컬 절대경로 제거.
- Swagger/주석 인코딩 정리 및 한글 설명 복구.
- JWT 갱신·로그아웃 처리, 작성자 검증(게시글·댓글 수정/삭제) 로직 보완.
- 통합/단위 테스트 보강 및 DB 초기화/샘플 데이터 자동화.

## 진행 메모
- [2025-11-25 17:08] BoardController 수정: 게시글 수정/삭제 API PathVariable 이름을 `boardId`로 통일, 수정 시 경로 ID를 바디에 주입하도록 수정(검수 완료).
- [2025-11-25 17:08] 요청에 따라 `@Operation` 관련 빌드 오류 항목은 진행상황 기록에서 제외하고 관여하지 않음.
- [2025-11-26 15:33] 파일 업로드 경로를 프로필별 설정값/환경변수로 분리하자는 의미 설명: 경로 하드코딩(`C:/nutricare_images/`) 대신 `application-{profile}.properties`나 환경변수로 관리하여 로컬/운영마다 다른 경로를 쓰고 설정만 바꿔 배포.
- [2025-11-26 15:45] 파일 업로드 저장소 향후 E3 서버 이전 계획 질문에 대한 메모: 현재는 로컬 경로 사용, 바로 변경은 필요 없으며 E3 환경/보안/접근 제어 준비 후 프로필/환경변수 기반으로 경로만 교체하는 방식 권장.
- [2025-11-26 15:49] FastAPI 피부질환 분류 연동 시 필요한 DTO 제안 정리: 이미지 업로드/분석 요청·결과 DTO, 모델 응답 DTO(코드/이름/확률), DB 저장용 AnalysisResult DTO(analysis_id, photo_id, diagnosis_code/name, created_at 등), 필요시 DietRecommendation 연결용 DTO.
- [2025-11-26 15:51] Photo DTO 추가: `com.nutricare.model.dto.Photo` 생성(필드: photoId, userId, photoUrl, createdAt; 기본/전체/부분 생성자, getter/setter, toString 포함).
- [2025-11-26 15:52] Photo DAO 필요 여부 메모: 사진 메타데이터를 DB에 남길 예정이면 DAO 필수(MyBatis 매퍼와 연동). 최소 메서드 insert/findById/findByUserId/list, 필요 시 삭제/페이지네이션 추가.
- [2025-11-26 15:54] Photo 업로드 방안: 기존 FileController로 파일 저장/URL 반환은 그대로 사용 가능. DB에 photo 메타데이터를 남기려면 FileController에서 PhotoService.insert 호출해 `photo_url`, `user_id`를 저장하거나 별도 PhotoController를 만들어 업로드+등록을 한 번에 처리하는 방식이 필요.
- [2025-11-26 15:58] 업로드 설계 권장: 별도 PhotoController/PhotoService 흐름으로 업로드+DB등록을 한 번에 처리하고, FileController는 일반 파일 업로드 전용으로 분리하는 방식을 추천(확장성/책임 분리). JWT userId를 컨트롤러에서 받아 photo 테이블에 저장.
- [2025-11-26 16:01] Photo 구조 제안: 업로드 시 PhotoController->PhotoService가 파일을 저장하고 photo 테이블에 userId+photoUrl을 기록. FastAPI 모델은 photo 테이블에서 URL(or 저장 경로)을 조회해 이미지를 로드/분석 후 결과를 반환하도록 분리. 이렇게 하면 백엔드 저장/목록 관리와 ML 서빙을 느슨히 결합.
- [2025-11-26 16:02] 메타데이터 설명: 실제 이미지 파일이 아니라 관련 정보(예: photoId, userId, photoUrl, 생성 시각 등)만 DB에 저장하는 것. 파일은 스토리지에 두고 DB에는 경로/식별자 같은 정보를 남겨 참조.
- [2025-11-26 16:15] Photo 서비스 범위 논의: 사용자가 직접 조회/수정하지 않더라도 백엔드/ML에서 참조·정리할 수 있게 최소 조회(findById 또는 findByUserId)와 필요시 삭제(정리/오류 롤백용) 정도는 권장. 업데이트는 주 URL 변경 등 특별한 경우만.
- [2025-11-26 16:19] board_image vs photo 차이 정리: board_image는 게시글과 1:N로 얹히는 부속 이미지라 DTO/매퍼만 있으면 충분(게시글 CRUD 안에 포함). photo는 사용자 업로드 이력/ML 분석 등 독립 도메인이라 업로드·저장·조회 흐름이 필요해 DAO/Service/Mapper/Controller까지 분리하는 구조가 적합.
- [2025-11-26 16:29] photoMapper 리뷰: 1) `selectListByUserId`의 `parameterType="userId"`는 잘못된 타입 지정으로 `long`(또는 `java.lang.Long`)으로 수정 필요. 2) insert 시 `useGeneratedKeys="true" keyProperty="photoId"`로 자동증가 키 주입 권장. 3) `SELECT *` 대신 공통 컬럼(`photoColumns`) 사용 권장. 나머지 컬럼/맵핑 구조는 테이블 정의와 일치 확인.
- [2025-11-26 16:33] photoMapper 재확인: parameterType 수정, useGeneratedKeys 추가, `photoColumns` include 적용 확인. 추가 권장: `selectOne`도 `SELECT <include refid="photoColumns"/> FROM photo WHERE photo_id = #{photoId}`로 통일하면 컬럼 명시 일관성 확보.
- [2025-11-26 16:59] 요청: 앞으로 진행 메모는 `res/workingMemo.md`에 기록.
- [2025-11-26 17:02] PhotoController에 GET 미구현 이유 공유: 초기 요구가 업로드/삭제 중심이라 POST/DELETE만 추가. 내부/모델 사용을 위해서는 selectOne/listByUserId GET 엔드포인트를 추가할 수 있음.
- [2025-11-26 17:06] PhotoController에 POST/DELETE 구현: `/photo-api/photo` POST로 Photo insert, `/photo-api/photo/{photoId}` DELETE로 삭제. 결과코드: 성공 시 201/200, 실패 시 400/500 반환.
- [2025-11-26 17:08] PhotoController에 GET 추가: `/photo-api/photo/user/{userId}`로 사용자별 사진 메타데이터 목록 조회 (비어 있으면 204, 성공 200). @Operation summary/description 포함.
- [2025-11-26 17:09] PhotoController 모든 메서드에 @Operation 추가: 단건 조회/사용자별 목록/등록/삭제 요약·설명 명시.
- [2025-11-25 17:25] "다음에 하면 좋을 작업" 섹션에 analysis_result는 외부 FastAPI에서 분석·저장을 맡고, 백엔드는 결과 수신/연동 준비를 추가로 수행하도록 메모.
- [2025-11-26 17:35] 분석 흐름: FastAPI는 분석 결과 JSON만 전달하고, Spring이 이를 받아 analysis_result DAO로 DB에 INSERT하는 구조로 변경.
- [2025-11-28 12:59] 업로드 흐름 정리: 게시글 이미지는 기존처럼 파일 저장/URL 반환만 사용, 사용자 얼굴 이미지는 `/file-api/upload-with-meta`로 파일 저장과 Photo 메타데이터(DB) 기록을 함께 처리하는 이원화 방식 사용(업로드+메타 저장 동시 처리 확인).
- [2025-12-04 16:23] 파일 업로드를 GCS 기반으로 전환: `google-cloud-storage` 의존성 추가, `GcsProperties`/`StorageConfig` 작성 및 `@EnableConfigurationProperties` 등록. `FileController`에서 게시글 이미지(`upload-board-image`)와 사진 업로드+메타 저장(`upload-with-meta`) 시 GCS 버킷/프리픽스(`board-images/`, `photo-images/`)에 업로드하고 URL을 DB에 저장. `application.properties`의 GCS 자격 경로를 로컬 사용자(`C:/Users/jangs/.../concrete-fabric-...json`)로 변경됨.
- [2025-12-04 16:23] `AnalysisResultDao`/DTO/매퍼 파일만 추가되었으나 내용이 비어 있어 실제 분석 결과 저장 기능은 미구현 상태.
- [2025-12-04 16:23] `PhotoController` 사용자별 조회 엔드포인트 경로 누락(`@GetMapping("/photo")` + `@PathVariable userId`)을 확인, 호출 불가 상태 기록.
