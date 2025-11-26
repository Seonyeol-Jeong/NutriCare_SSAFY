# 작업 진행상황

## 프로젝트 개요
- Spring Boot 3.5.8 + MyBatis REST API 서버, 기본 DB는 MySQL(`nutricare_db`).
- JWT 인증/인가와 Swagger(OpenAPI 2.6.0) 설정 완료, Bearer 스키마 포함.
- 주요 도메인: 회원(User), 게시판(Board/Comment/BoardImage), 파일 업로드; DB 스키마에는 건강/식단 관련 테이블(health_profile, diet_*)도 정의됨.

## 코드 구성(핵심 경로)
- `src/main/java/com/nutricare/config`: MyBatis 스캔, Swagger, WebMvc(정적 리소스 매핑, 인터셉터) 설정, BCrypt PasswordEncoder 빈.
- `src/main/java/com/nutricare/interceptor`: JWT 검증, 관리자 권한 검증 인터셉터.
- `src/main/java/com/nutricare/controller`: 회원/관리자, 게시판, 댓글, 파일 업로드 REST 컨트롤러.
- `src/main/java/com/nutricare/model`: dto/dao/service 계층, MyBatis 매퍼(`src/main/resources/mappers/*.xml`) 연동.
- `src/main/java/com/nutricare/util/JwtUtil`: HS256 서명 JWT 생성·파싱, 기본 만료 1시간.
- 설정 파일: `src/main/resources/application.properties` (로컬 MySQL, 매퍼 위치, 멀티파트 용량 등).

## 구현된 기능
- 회원: 회원가입(BCrypt 해시 저장), 로그인(JWT 발급), 내 정보 조회/수정/탈퇴, 로그아웃 응답; 관리자용 전체 회원 조회.
- 인증/인가: `/user/me`, `/admin/**` 등에 JWT 인터셉터 적용, 관리자 인터셉터로 role=ADMIN만 접근 허용.
- 게시판: 게시글 CRUD, 조회수 증가, 게시글 이미지 다건 등록 지원(`board_image`), 작성자 이름 조인 조회.
- 댓글: 게시글별 댓글 조회/작성/수정/소프트삭제.
- 파일 업로드: `POST /board-api/upload`로 `C:/nutricare_images/` 저장 후 `/images/{파일}` URL 반환, 정적 리소스 핸들러로 서빙.
- DB 스키마: `res/sql.sql`에 user/health_profile/photo/analysis_result/diet_recommendation/diet_meal/board/comment/board_image 테이블 정의.

## 확인된 이슈·위험 요소
- API 매핑: `BoardController` 삭제 API 경로 변수 오타(`{baordId}` vs `boardId`), 수정 API PathVariable 미사용으로 ID 누락 가능성(현재 수정 완료 상태 확인됨).
- 기능 미완: 건강/식단 테이블에 대응하는 코드/엔드포인트 부재.
- 파일 업로드: 로컬 절대경로 고정(`C:/nutricare_images/`), 운영/로컬 프로필 분리 필요.
- 테스트 부족: 기본 `contextLoads` 외 통합/단위 테스트 없음.
- 보안/유효성: JWT 리프레시·블랙리스트 미구현, 회원가입 이메일/입력 검증은 DB 제약 외 추가 로직 없음.

## 다음에 하면 좋을 작업
- 건강/식단 도메인 DTO/서비스/컨트롤러 설계 및 구현.
- 파일 업로드 경로를 설정/프로필별로 분리하고 환경 변수화.
- JWT 갱신·로그아웃 처리, 작성자 검증(게시글·댓글 수정/삭제) 로직 보완.
- 통합/단위 테스트 추가 및 DB 초기화 스크립트 자동화.
- API 매핑/권한 검증 등 기존 엔드포인트 재점검.

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
