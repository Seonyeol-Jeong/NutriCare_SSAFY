# NutriCare Front-End 프로젝트 기술 명세

이 문서는 NutriCare 프론트엔드 프로젝트에 적용된 핵심 기술 요소를 설명합니다.

---

### 1. SPA, 라우팅, 상태 관리

본 프로젝트는 Vue.js를 기반으로 한 **SPA(Single Page Application)**로 구축되었습니다. SPA 아키텍처를 통해 사용자에게 부드러운 페이지 전환 경험을 제공하며, 서버와의 통신을 최소화하여 성능을 향상시켰습니다.

-   **라우팅 (Routing)**
    -   `vue-router` 라이브러리를 사용하여 페이지 간의 이동을 관리합니다.
    -   `src/router/index.js` 파일에 모든 경로(Route) 정보가 중앙에서 정의되어 있습니다. `createRouter`와 `createWebHistory`를 사용하여 History API 기반의 라우팅을 구현하였으며, 이를 통해 브라우저의 주소창과 실제 뷰를 동기화합니다.
    -   각 경로는 특정 Vue 컴포넌트와 매핑되어 있으며, 중첩 라우트(Nested Routes)를 활용하여 `HomeView`나 `BoardView` 같은 레이아웃 컴포넌트 내에서 하위 뷰를 렌더링하는 구조를 가집니다.

-   **상태 관리 (State Management)**
    -   `pinia`를 프로젝트의 공식 상태 관리 라이브러리로 사용합니다. Pinia는 직관적이고 타입스크립트 지원이 뛰어나며, 모듈화된 스토어 관리에 용이합니다.
    -   `src/stores` 디렉토리 내에 `user.js`, `board.js`, `analysis.js` 등 기능별로 모듈화된 스토어(Store)를 생성하여 관리합니다.
    -   각 스토어는 `defineStore`를 통해 정의되며, `state`, `getters`, `actions`를 포함합니다.
        -   `state`: 컴포넌트 전반에서 공유되는 데이터 (예: 로그인 토큰, 사용자 정보)
        -   `getters`: `state`를 기반으로 한 계산된 속성 (예: 사용자 이름, 관리자 여부)
        -   `actions`: 비동기 작업을 포함한 데이터 변경 로직 (예: 로그인 API 호출, 프로필 업데이트)

---

### 2. REST API 통신

서버와의 비동기 통신은 `axios` 라이브러리를 기반으로 구현되었습니다. 특히, **인터셉터(Interceptor)**를 활용하여 API 요청과 응답을 전역적으로 관리함으로써 코드 중복을 줄이고 유지보수성을 높였습니다.

-   **Axios 인스턴스 및 인터셉터 설정 (`src/api/axios.js`)**
    -   `axios.create`를 사용하여 baseURL, timeout 등 공통 설정을 포함하는 인스턴스를 생성했습니다.
    -   **요청 인터셉터 (Request Interceptor)**: API 요청이 서버로 전송되기 전에 실행됩니다. `publicPaths`에 지정된 경로(로그인, 회원가입 등)를 제외한 모든 요청에 대해 `localStorage`에서 `accessToken`을 가져와 `Authorization: Bearer ${token}` 헤더를 자동으로 추가합니다.
    -   **응답 인터셉터 (Response Interceptor)**: 서버로부터 응답을 받은 후 실행됩니다.
        -   `401 Unauthorized` 에러 발생 시 (토큰 만료 등), 사용자 세션을 초기화하는 `logout` 액션을 호출하고 세션 만료 경고를 출력합니다.
        -   `403 Forbidden` 에러 발생 시, 접근 권한이 없다는 `alert`을 표시합니다. 단, 세션 복구를 위한 `/users/me` 요청에서 발생하는 403 에러는 사용자가 불편을 겪지 않도록 예외 처리하여 경고창을 띄우지 않습니다.

-   **API 호출**
    -   실제 API 호출은 주로 Pinia 스토어의 `actions` 내에서 이루어집니다. 예를 들어, `user` 스토어의 `login` 액션은 `/users/login` 엔드포인트로 POST 요청을 보내고, 성공 시 토큰과 사용자 정보를 `state`에 저장합니다.

---

### 3. CSS 적용 및 스타일링

프로젝트의 디자인과 레이아웃은 다음과 같은 방식으로 구현되었습니다.

-   **전역 스타일링 프레임워크**
    -   `Bootstrap` CSS 프레임워크를 `main.js`에서 전역으로 가져와 프로젝트 전반의 기본 스타일과 그리드 시스템을 구축했습니다. 이를 통해 일관된 디자인을 유지하고 반응형 웹 디자인을 용이하게 구현합니다.

-   **컴포넌트 단위 스타일링 (Scoped CSS)**
    -   Vue의 핵심 기능인 **단일 파일 컴포넌트(SFC)** 내에서 `<style scoped>` 속성을 사용합니다.
    -   `scoped` 스타일은 해당 컴포넌트의 DOM 요소에만 적용되므로, 다른 컴포넌트와의 CSS 충돌을 방지하고 스타일의 캡슐화를 보장합니다. 프로젝트의 대부분 컴포넌트(`PageDescribe.vue`, `HomeView.vue` 등)가 이 방식을 따릅니다.

-   **동적 CSS 바인딩**
    -   Vue의 `v-bind()` CSS 함수를 사용하여 컴포넌트의 `<script setup>`에 정의된 JavaScript 변수를 CSS 속성에 동적으로 바인딩합니다. `HomeView.vue`에서 배경 이미지를 설정하는 데 이 기법이 사용되었습니다.

-   **에셋 관리**
    -   로고, 배경 이미지 등의 정적 에셋은 `src/assets` 디렉토리에서 관리하며, 컴포넌트에서 `import`하여 사용합니다. 이는 빌드 시 웹팩(Vite)이 자동으로 경로를 처리해주므로 배포 시 발생할 수 있는 경로 문제를 방지합니다.