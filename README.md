# NutriCare_SSAFY

장준호, 정선열 프로젝트입니다!

Git remote repo: https://github.com/Seonyeol-Jeong/NutriCare_SSAFY/tree/main/back/NutriCare_SSAFY/src/main/java/com/nutricare

- ResNet 구축에 사용한 이미지는 [AI Hub 피부질환 이미지 데이터셋](https://www.aihub.or.kr/aihubdata/data/view.do?currMenu=115&topMenu=100&&srchDataRealmCode=REALM006&aihubDataSe=data&dataSetSn=71863)을 사용했습니다.


> 11.21 수행내용

## ✨ User 기능 구현 완료

이번 PR에서는 **User 도메인 전체 기능을 구축**했습니다.  
회원가입, 로그인(JWT 발급), 개인정보 수정/조회, 탈퇴 등 핵심 사용자 기능을 중심으로 구현했습니다.

---

### 📌 주요 수행 사항

#### 1. User 엔티티 확장
- `role` 필드 추가 (기본값: USER)
- 향후 관리자 기능 연동을 위해 역할 기반 권한 시스템 준비

#### 2. 회원가입 API 구현
- `POST /user`
- 사용자 정보 저장
- 입력받은 password는 BCrypt 기반 해시 후 저장

#### 3. 로그인 + JWT 발급
- `POST /user/login`
- Email/Password 검증 후 JWT 발급  
- JWT Payload에 포함되는 Claims:
  - `userId`
  - `role`

#### 4. JWT 인터셉터 구현
- 보호된 API 접근 시 JWT 검증
- 성공 시:
  - `request.setAttribute("userId")`
  - `request.setAttribute("role")`
- 실패 시: `401 Unauthorized`

#### 5. 관리자 전용 API 구현
- `/admin/all`
- 전체 회원 조회 기능
- Admin 권한이 아닌 경우 접근 불가하게 구성됨

#### 6. Swagger 설정 개선
- JWT Bearer 인증 UI 추가
- Authorization 헤더 자동 세팅 기능 지원

---

### 🔧 기타 코드 개선 사항
- 패키지 구조 정리 (`com.nutricare` 기준 루트 통일)
- JwtUtil 최신화 (0.9.x에서 발생하는 오류 해결)
- MyBatis ResultMap + Mapper에 `role` 매핑 추가

---

### ✔️ 테스트 결과
- 회원가입 정상 동작
- 로그인 & JWT 발급 정상
- `/admin/all` 권한 체크 정상 (401 / 200 분기 확인)

---
