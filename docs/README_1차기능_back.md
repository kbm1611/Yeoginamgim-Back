# YEOGINAMGIM-BACKEND

`YEOGINAMGIM-BACKEND`는 **여기남김** 프로젝트의 Spring Boot 백엔드입니다.

여기남김은 사용자가 실제 장소를 기반으로 디지털 공간에 **흔적(trace)** 을 남기고, 다른 사용자의 흔적을 함께 둘러볼 수 있는 공간 기반 감성 아카이빙 서비스입니다. 카카오 장소 검색으로 공간을 찾고, 해당 공간의 보드에 텍스트, 이미지, 스티커, 위치 정보를 포함한 흔적을 남기는 흐름을 제공합니다.

이 문서는 Google Sheet의 `2.기획서`, `5.API 명세서`와 현재 백엔드 코드 구조를 기준으로 정리했습니다.

---

## 프로젝트 개요

기존 리뷰 서비스가 별점과 긴 후기 중심이라면, 여기남김은 특정 공간에 방문했을 때의 감정, 짧은 메모, 사진, 낙서, 스티커를 포스트잇처럼 남기는 경험에 초점을 둡니다.

핵심 흐름은 다음과 같습니다.

```text
회원가입 또는 로그인
→ 현재 위치 기반 주변 장소 탐색
→ 특정 장소 선택
→ 카카오 장소 ID 기준으로 보드 조회 또는 생성
→ 공간 보드 입장
→ 흔적 작성
→ 이미지 / 텍스트 / 스타일 / 위치 정보 저장
→ 다른 사용자가 남긴 흔적 조회
→ 좋아요 / 신고
→ 내가 남긴 기록을 보관함에서 다시 보기
```

---

## 1차 기능 범위

### 1. 회원가입 / 로그인

- 일반 회원가입
- JWT 기반 일반 로그인
- 카카오 OAuth 로그인
- 구글 OAuth 로그인
- 로그인 사용자 정보 조회
- 회원 정보 수정

### 2. 지도 기반 공간 탐색

- 현재 위치 기반 주변 장소 조회
- 카테고리/검색어 기반 장소 조회
- 카카오 Local API와 CSV 캐시 기반 장소 정보 관리
- 장소별 흔적 개수 표시
- 흔적 수 기준 인기 장소 조회

### 3. 공간 보드

- 카카오 장소 ID 기반 보드 조회 또는 생성
- 장소 스냅샷 기반 보드 생성
- 보드 상세 조회
- 보드별 흔적 목록 조회
- 좌표 범위 기반 흔적 조회

### 4. 흔적 작성

- 보드 위치에 흔적 작성
- 텍스트/이미지/스타일 정보를 `TraceElement`로 저장
- 흔적 상세 조회
- 작성자 본인의 흔적 수정
- 작성자 본인의 흔적 숨김 처리
- 흔적 이미지 업로드
- 흔적 좋아요 등록/취소
- 흔적 신고

### 5. 보관함

- 내가 남긴 흔적 전체 조회
- 내가 남긴 흔적 개별 조회
- 월별 달력 아카이브 조회
- 보드/장소별 아카이브 조회
- 내가 받은 좋아요가 있는 흔적 조회

---

## 기술 스택

### Backend

- Java 17
- Spring Boot 4.0.3
- Spring Web MVC
- Spring Data JPA
- Bean Validation
- Spring Security Crypto
- JWT
- Lombok

### Database

- MySQL
- SQL 초기화 파일: `src/main/resources/sql/schema.sql`, `src/main/resources/sql/data.sql`

### External API

- Kakao Local API
- Kakao OAuth API
- Google OAuth API

### Build Tool

- Gradle Wrapper

---

## 프로젝트 구조

```text
Yeoginamgim-Back
├─ src
│  ├─ main
│  │  ├─ java
│  │  │  └─ com
│  │  │     └─ yeginamgim
│  │  │        ├─ AppStart.java
│  │  │        ├─ archive
│  │  │        ├─ auth
│  │  │        ├─ board
│  │  │        ├─ global
│  │  │        ├─ place
│  │  │        ├─ report
│  │  │        ├─ trace
│  │  │        └─ user
│  │  └─ resources
│  │     ├─ application.properties
│  │     └─ sql
│  │        ├─ schema.sql
│  │        └─ data.sql
│  └─ test
│     └─ java
├─ docs
│  └─ README_1차기능_back.md
├─ build.gradle
├─ gradlew.bat
└─ settings.gradle
```

---

## 패키지 역할

### `global`

공통 예외 처리, 공통 엔티티 시간 처리, 웹/CORS 설정, 파일 업로드 경로 설정을 관리합니다.

### `auth`

일반 로그인, JWT 토큰 발급, 카카오/구글 OAuth 로그인과 콜백 처리를 담당합니다.

### `user`

일반 회원가입, 로그인 사용자 정보 조회, 닉네임과 프로필 이미지 수정을 담당합니다.

### `place`

현재 위치 또는 검색 조건 기반 장소 조회, 인기 장소 조회, 카카오 Local API 연동, CSV 장소 캐시 조회를 담당합니다.

### `board`

카카오 장소 ID에 연결된 보드 조회/생성과 보드 상세 조회를 담당합니다. 장소 상세 정보는 보드 응답의 `place` 스냅샷으로 제공됩니다.

### `trace`

보드에 남기는 흔적과 흔적 요소를 관리합니다. 흔적 목록/영역 조회, 작성, 상세 조회, 수정, 숨김 삭제, 이미지 업로드, 좋아요 등록/취소를 담당합니다.

### `report`

특정 흔적에 대한 신고 등록을 담당합니다.

### `archive`

로그인 사용자가 남긴 흔적을 전체, 개별, 달력, 보드/장소별, 좋아요 받은 흔적 기준으로 조회합니다.

---

## 주요 API

### User

```text
POST  /api/user/signup
GET   /api/user/myinfo
PATCH /api/user/update
```

### Auth

```text
POST /api/auth/login
GET  /api/auth/oauth/kakao
GET  /api/auth/oauth/kakao/callback
GET  /api/auth/oauth/google
GET  /api/auth/oauth/google/callback
GET  /api/auth/logout
```

### Place

```text
GET /api/places/nearby
GET /api/places/popular
```

### Board

```text
GET  /api/places/{kakaoPlaceId}/board
GET  /api/boards/{boardId}
POST /api/boards
```

### Trace

```text
GET    /api/boards/{boardId}/traces
GET    /api/boards/{boardId}/traces/area
POST   /api/boards/{boardId}/traces
GET    /api/traces/{traceId}
POST   /api/traces/images
PATCH  /api/traces/{traceId}
DELETE /api/traces/{traceId}
POST   /api/traces/{traceId}/likes
DELETE /api/traces/{traceId}/likes
```

### Report

```text
POST /api/traces/{traceId}/reports
```

### Archive

```text
GET /api/me/traces
GET /api/me/traces/{traceId}
GET /api/me/archive/calendar
GET /api/me/archive/boards
GET /api/me/received-likes
```

---

## 설정 파일 관리

`application.properties`는 GitHub에 올리는 기본 설정 파일입니다. 현재 주요 기본값은 다음과 같습니다.

```properties
spring.application.name=YEOGINAMGIM-BACKEND
spring.config.import=optional:application-secret.properties
server.port=8080
spring.datasource.url=jdbc:mysql://localhost:3306/yeoginamgim_db?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Seoul
spring.jpa.hibernate.ddl-auto=update
spring.sql.init.mode=always
place.cache-file-path=../data/places-cache.csv
```

`application-secret.properties`는 개인별로 생성하는 비밀 설정 파일입니다. DB 계정, JWT Secret, Kakao/Google OAuth Secret, Kakao REST API Key처럼 외부에 공개되면 안 되는 값을 작성합니다.

```properties
spring.datasource.username=your_db_username
spring.datasource.password=your_db_password

jwt.secret=your_jwt_secret_key

kakao.client-id=your_kakao_client_id
kakao.client-secret=your_kakao_client_secret
kakao.redirect-uri=http://localhost:8080/api/auth/oauth/kakao/callback
kakao.rest-api-key=your_kakao_rest_api_key

google.client-id=your_google_client_id
google.client-secret=your_google_client_secret
google.redirect-uri=http://localhost:8080/api/auth/oauth/google/callback
```

---

## 개발 시 주의사항

- Controller에는 복잡한 비즈니스 로직을 작성하지 않습니다.
- 핵심 로직은 Service에서 처리합니다.
- DB 접근은 Repository에서 처리합니다.
- Entity를 API 응답으로 직접 반환하지 않습니다.
- 요청과 응답은 DTO를 사용합니다.
- API 키, 비밀번호, JWT Secret은 코드나 README에 직접 작성하지 않습니다.
- `application-secret.properties`는 GitHub에 올리지 않습니다.
- 업로드/캐시 데이터는 소스 폴더가 아닌 `uploads/` 또는 `data/` 같은 런타임 폴더에 둡니다.

---

## 추천 개발 흐름

### 1. 백엔드 폴더 이동

```powershell
cd Yeoginamgim-Back
```

### 2. 설정 파일 작성

`src/main/resources/application-secret.properties` 파일을 생성하고 개인별 비밀 설정을 작성합니다.

### 3. 테스트

```powershell
.\gradlew.bat test
```

### 4. 빌드

```powershell
.\gradlew.bat build
```

### 5. 서버 실행

```powershell
.\gradlew.bat bootRun
```

---

## Git 관리 주의사항

GitHub에 올리면 안 되는 파일:

```text
application-secret.properties
.env
build/
out/
.idea/
*.log
uploads/
data/
```

`.gitignore`에 반드시 포함해야 합니다.
