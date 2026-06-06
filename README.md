# 여긴남김 백엔드 1차 기능 문서

`YEOGINAMGIM-BACKEND`는 **여긴남김** 서비스의 Spring Boot 백엔드입니다.

여긴남김은 사용자가 실제 장소를 찾고, 장소별 보드에 짧은 기억과 감정을 흔적으로 남기며, 나중에 보관함에서 다시 꺼내 볼 수 있는 공간 기반 기록 서비스입니다. 이 문서는 사람이 백엔드의 1차 기능 범위와 API 구조를 빠르게 이해할 수 있도록 현재 백엔드 코드 기준으로 정리했습니다.

---

## 서비스 한 줄 설명

장소, 보드, 흔적, 사용자 기록을 연결해 주는 API 서버입니다.

---

## 핵심 사용자 흐름

```text
회원가입 또는 로그인
→ 현재 위치나 검색어로 장소 탐색
→ Kakao place id 기준으로 보드 조회 또는 생성
→ 공간 보드 입장
→ 흔적 작성
→ 이미지, 텍스트, 스타일, 위치 정보 저장
→ 다른 사용자의 흔적 조회
→ 좋아요 또는 신고
→ 내가 남긴 기록을 보관함에서 다시 보기
```

---

## 1차 기능 범위

### 1. 회원과 인증

- 일반 회원가입
- JWT 기반 일반 로그인
- Kakao OAuth 로그인
- Google OAuth 로그인
- OAuth 콜백 처리
- 로그인 사용자 정보 조회
- 회원 정보 수정
- 회원 탈퇴
- 로그아웃

### 2. 장소 탐색

- 현재 위치 기반 주변 장소 조회
- 검색어 기반 장소 조회
- 카테고리 기반 장소 필터링
- 흔적 수 기준 인기 장소 조회
- Kakao Local API 연동
- CSV 기반 장소 캐시 관리
- 장소별 흔적 개수 제공

### 3. 공간 보드

- Kakao place id 기반 보드 조회
- 장소 스냅샷 기반 보드 생성
- 보드 상세 조회
- 장소와 보드의 연결 정보 제공

### 4. 흔적

- 최근 흔적 조회
- 보드별 흔적 목록 조회
- 보드 좌표 영역 기반 흔적 조회
- 보드 위 위치에 흔적 작성
- 텍스트, 이미지, 스타일 정보를 `TraceElement`로 저장
- 흔적 상세 조회
- 작성자 본인의 흔적 수정
- 작성자 본인의 흔적 숨김 처리
- 흔적 이미지 업로드
- 흔적 좋아요 등록/취소

### 5. 신고

- 특정 흔적 신고 등록

### 6. 보관함

- 내가 남긴 흔적 전체 조회
- 내가 남긴 흔적 개별 조회
- 월별 달력 아카이브 조회
- 보드/장소별 아카이브 조회
- 내가 받은 좋아요가 있는 흔적 조회
- 즐겨찾기 장소 목록 조회
- 즐겨찾기 장소 등록/취소

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

## 실행 방법

```powershell
cd Yeoginamgim-Back
.\gradlew.bat bootRun
```

테스트:

```powershell
cd Yeoginamgim-Back
.\gradlew.bat test
```

빌드:

```powershell
cd Yeoginamgim-Back
.\gradlew.bat build
```

---

## 프로젝트 구조

```text
Yeoginamgim-Back/
├─ docs/
│  └─ README_1차기능_back.md
├─ src/
│  ├─ main/
│  │  ├─ java/com/yeginamgim/
│  │  │  ├─ AppStart.java
│  │  │  ├─ archive/
│  │  │  ├─ auth/
│  │  │  ├─ board/
│  │  │  ├─ global/
│  │  │  ├─ place/
│  │  │  ├─ report/
│  │  │  ├─ trace/
│  │  │  └─ user/
│  │  └─ resources/
│  │     ├─ application.properties
│  │     └─ sql/
│  │        ├─ schema.sql
│  │        └─ data.sql
│  └─ test/java/com/yeginamgim/
├─ build.gradle
├─ gradlew.bat
└─ settings.gradle
```

---

## 패키지 역할

### `global`

공통 예외 처리, CORS와 웹 설정, 파일 업로드 경로 설정, 공통 시간 엔티티를 관리합니다.

### `auth`

일반 로그인, JWT 토큰 발급/검증, Kakao/Google OAuth 로그인 시작과 콜백 처리를 담당합니다.

### `user`

일반 회원가입, 로그인 사용자 정보 조회, 닉네임/프로필 수정, 회원 탈퇴를 담당합니다.

### `place`

현재 위치 또는 검색 조건 기반 장소 조회, 인기 장소 조회, Kakao Local API 연동, CSV 장소 캐시 조회와 저장을 담당합니다.

### `board`

Kakao place id에 연결된 보드 조회/생성과 보드 상세 조회를 담당합니다. 보드 응답에는 장소 스냅샷 정보가 함께 포함됩니다.

### `trace`

보드에 남기는 흔적과 흔적 요소를 관리합니다. 흔적 목록/영역 조회, 작성, 상세 조회, 수정, 숨김 삭제, 이미지 업로드, 좋아요 등록/취소를 담당합니다.

### `report`

특정 흔적에 대한 신고 등록을 담당합니다.

### `archive`

로그인 사용자가 남긴 흔적을 전체, 개별, 달력, 보드/장소별, 좋아요 받은 흔적 기준으로 조회합니다. 즐겨찾기 장소 조회/등록/취소도 담당합니다.

---

## 주요 파일

- `AppStart.java`: Spring Boot 애플리케이션 진입점
- `auth/controller/AuthController.java`: 로그인, OAuth 시작/콜백, 로그아웃
- `auth/jwt/JWTService.java`: JWT 생성과 검증
- `user/controller/UserController.java`: 회원가입, 내 정보, 수정, 탈퇴
- `place/controller/PlaceController.java`: 주변/검색/인기 장소 API
- `place/repository/PlaceCsvStore.java`: 장소 캐시 CSV 읽기/쓰기
- `board/controller/BoardController.java`: 장소별 보드 조회, 보드 상세, 보드 생성
- `trace/controller/TraceController.java`: 흔적 목록/작성/수정/숨김/이미지/좋아요
- `archive/controller/ArchiveController.java`: 내 흔적, 아카이브, 즐겨찾기 장소
- `report/controller/ReportController.java`: 흔적 신고
- `global/exception/GlobalExceptionHandler.java`: 공통 예외 응답
- `global/file/FileService.java`: 업로드 파일 저장

---

## 주요 API

### User

```text
POST   /api/user/signup
GET    /api/user/myinfo
PATCH  /api/user/update
DELETE /api/user/me
```

#### 회원가입

`POST /api/user/signup`은 JSON 요청을 지원하지 않고, `Content-Type: multipart/form-data` 요청만 받습니다.

요청 필드:

| 필드 | 필수 여부 | 설명 |
| --- | --- | --- |
| `email` | 필수 | 회원 이메일 |
| `password` | 필수 | 회원 비밀번호 |
| `nickname` | 필수 | 회원 닉네임 |
| `birthDate` | 선택 | 생년월일 문자열 |
| `profileUploadFile` | 선택 | 프로필 이미지 파일 |

요청 예시:

```powershell
curl.exe -X POST "http://localhost:8080/api/user/signup" `
  -F "email=new@example.com" `
  -F "password=password123" `
  -F "nickname=new-user" `
  -F "birthDate=060615" `
  -F "profileUploadFile=@C:\path\to\profile.png"
```

응답 예시:

```json
{
  "email": "new@example.com",
  "nickname": "new-user",
  "profileImageUrl": "/uploads/profile/profile.png",
  "birthDate": "060615",
  "createdAt": "2026-06-06T12:34:56"
}
```

응답 바디에는 `password`가 포함되지 않습니다. `application/json` 요청은 지원하지 않으며, JSON 바디로 요청하면 `415 Unsupported Media Type` 응답을 받을 수 있습니다.

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
GET /api/places/search
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
GET    /api/traces/recent
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
GET    /api/me/traces
GET    /api/me/traces/{traceId}
GET    /api/me/archive/calendar
GET    /api/me/archive/boards
GET    /api/me/received-likes
GET    /api/me/archive/favorite-places
POST   /api/me/archive/favorite-places/{kakaoPlaceId}
DELETE /api/me/archive/favorite-places/{kakaoPlaceId}
```

---

## 설정 파일 관리

`application.properties`는 GitHub에 올리는 기본 설정 파일입니다. 현재 주요 기본값은 다음과 같습니다.

```properties
spring.application.name=YEOGINAMGIM-BACKEND
spring.config.import=optional:application-secret.properties
server.port=8080
app.frontend-base-url=http://localhost:5173
server.servlet.encoding.charset=UTF-8
spring.datasource.url=jdbc:mysql://localhost:3306/yeoginamgim_db?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Seoul
spring.jpa.hibernate.ddl-auto=update
spring.sql.init.mode=always
place.cache-file-path=../data/places-cache.csv
```

`application-secret.properties`는 개인별로 생성하는 비밀 설정 파일입니다. DB 계정, JWT secret, Kakao/Google OAuth secret, Kakao REST API key처럼 외부에 공개되면 안 되는 값을 작성합니다.

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

실제 secret 값은 README, 코드, 이슈, 채팅에 적지 않습니다.

---

## 런타임 데이터

- 업로드 파일은 실행 디렉터리의 `uploads/` 아래에 저장합니다.
- 장소 CSV 캐시는 기본적으로 `../data/places-cache.csv`에 저장합니다.
- 업로드/캐시 데이터는 소스 폴더에 두지 않습니다.
- `uploads/`, `data/`는 GitHub에 올리지 않습니다.

---

## 테스트 구조

테스트는 `src/test/java/com/yeginamgim/` 아래에 도메인별로 있습니다.

- controller 테스트: `auth`, `place`, `trace`, `user`
- service 테스트: `auth`, `board`, `place`, `user`
- util/repository 테스트: `place/util`, `place/repository`
- 공통 예외 테스트: `global/exception`

---

## 개발 시 주의사항

- Controller에는 복잡한 비즈니스 로직을 작성하지 않습니다.
- 핵심 로직은 Service에서 처리합니다.
- DB 접근은 Repository에서 처리합니다.
- Entity를 API 응답으로 직접 반환하지 않습니다.
- 요청과 응답은 DTO를 사용합니다.
- DB 컬럼명이나 DTO shape는 프론트 사용처와 테스트를 확인한 뒤 바꿉니다.
- JWT/OAuth/security 변경 전에는 `auth` 코드와 테스트를 먼저 확인합니다.
- API key, password, JWT secret은 코드나 README에 직접 작성하지 않습니다.
- `application-secret.properties`는 GitHub에 올리지 않습니다.
- `System.out.println`이나 임시 로그는 handoff 전에 제거합니다.

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

