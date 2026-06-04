# 여기남김 - 백엔드

> 실제 장소 기반 보드에 사용자의 흔적을 남기고 조회하는 공간 방명록 서비스 백엔드

---

## 개요

`YEOGINAMGIM-BACKEND`는 **여기남김** 프로젝트의 Spring Boot 백엔드입니다.

사용자는 카카오 장소 검색으로 공간을 찾고, 해당 장소의 보드에 텍스트/이미지/스타일/위치 정보를 포함한 **흔적(trace)** 을 남길 수 있습니다. 백엔드는 회원가입/로그인, 장소 조회, 보드 조회/생성, 흔적 작성/조회/수정/삭제, 좋아요, 신고, 보관함 조회를 담당합니다.

상세한 1차 기능 설명과 API 목록은 [docs/README_1차기능_back.md](docs/README_1차기능_back.md)를 참고하세요.

---

## 기술 스택

| 항목 | 내용 |
|---|---|
| 언어 | Java 17 |
| 프레임워크 | Spring Boot 4.0.3 |
| 웹 | Spring Web MVC |
| 데이터 | Spring Data JPA |
| 인증 | JWT, Kakao OAuth, Google OAuth |
| DB | MySQL |
| 빌드 도구 | Gradle Wrapper |

---

## 실행 방법

워크스페이스 루트 기준:

```powershell
cd Yeoginamgim-Back
.\gradlew.bat bootRun
```

테스트와 빌드는 다음 명령어로 확인합니다.

```powershell
.\gradlew.bat test
.\gradlew.bat build
```

---

## 설정 파일

기본 설정은 `src/main/resources/application.properties`에 있습니다.

개인별 비밀 설정은 아래 파일을 직접 생성해 작성합니다.

```text
src/main/resources/application-secret.properties
```

여기에는 DB 계정, JWT Secret, Kakao/Google OAuth Secret, Kakao REST API Key처럼 외부에 공개되면 안 되는 값을 넣습니다. 실제 secret 값은 README나 코드에 작성하지 않습니다.

---

## 폴더 구조

```text
Yeoginamgim-Back/
├─ src/main/java/com/yeginamgim/
│  ├─ archive/        # 내 흔적과 아카이브 조회
│  ├─ auth/           # 로그인, JWT, OAuth
│  ├─ board/          # 장소별 보드 조회/생성
│  ├─ global/         # 공통 설정, 예외, 파일 처리
│  ├─ place/          # 장소 조회, 카카오 Local API, CSV 캐시
│  ├─ report/         # 흔적 신고
│  ├─ trace/          # 흔적, 흔적 요소, 이미지, 좋아요
│  └─ user/           # 회원가입, 내 정보 조회/수정
├─ src/main/resources/
│  ├─ application.properties
│  └─ sql/
├─ docs/
│  └─ README_1차기능_back.md
├─ build.gradle
└─ gradlew.bat
```

---

## 주요 API

```text
POST  /api/user/signup
GET   /api/user/myinfo
PATCH /api/user/update

POST /api/auth/login
GET  /api/auth/oauth/kakao
GET  /api/auth/oauth/google
GET  /api/auth/logout

GET  /api/places/nearby
GET  /api/places/popular

GET  /api/places/{kakaoPlaceId}/board
GET  /api/boards/{boardId}
POST /api/boards

GET    /api/boards/{boardId}/traces
POST   /api/boards/{boardId}/traces
GET    /api/traces/{traceId}
PATCH  /api/traces/{traceId}
DELETE /api/traces/{traceId}
POST   /api/traces/{traceId}/likes
DELETE /api/traces/{traceId}/likes
POST   /api/traces/{traceId}/reports

GET /api/me/traces
GET /api/me/archive/calendar
GET /api/me/archive/boards
GET /api/me/received-likes
```

---

## 개발 기준

- Controller에는 HTTP 요청/응답과 검증 진입점만 둡니다.
- 비즈니스 로직은 Service에서 처리합니다.
- DB 접근은 Repository에서 처리합니다.
- Entity를 API 응답으로 직접 반환하지 않고 DTO를 사용합니다.
- 업로드/캐시 데이터는 소스 폴더가 아닌 `uploads/` 또는 `data/`에 둡니다.
