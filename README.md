# 여긴남김 백엔드 README

Codex가 백엔드 작업의 뼈대를 빠르게 잡기 위한 구조 가이드입니다. 구현 판단은 항상 현재 소스 코드를 우선합니다.

## 한눈에 보기

- 런타임: Java 17, Spring Boot 4.0.3
- 빌드: Gradle Wrapper
- 주요 기술: Spring Web MVC, Spring Data JPA, Bean Validation, Lombok, JWT, MySQL
- 애플리케이션 진입점: `src/main/java/com/yeginamgim/AppStart.java`
- 기본 포트: `8080`
- 기본 DB URL: `jdbc:mysql://localhost:3306/yeoginamgim_db`
- 런타임 파일 위치: 실행 디렉터리 기준 `uploads/`
- 장소 CSV 캐시: `../data/places-cache.csv`

## 실행과 검증

```powershell
cd Yeoginamgim-Back
.\gradlew.bat bootRun
```

```powershell
cd Yeoginamgim-Back
.\gradlew.bat test
.\gradlew.bat build
```

## 설정 파일

- 공통 설정: `src/main/resources/application.properties`
- 로컬 비밀 설정: `src/main/resources/application-secret.properties`
- SQL 초기화: `src/main/resources/sql/schema.sql`, `src/main/resources/sql/data.sql`

`application-secret.properties`에는 DB 계정, JWT secret, OAuth secret, Kakao REST API key처럼 공개하면 안 되는 값을 둡니다. 실제 secret은 README나 소스에 적지 않습니다.

## 패키지 구조

```text
src/main/java/com/yeginamgim/
├─ archive/   # 내 흔적, 캘린더, 보드 아카이브, 즐겨찾기 장소
├─ auth/      # 로컬 로그인, Kakao/Google OAuth, JWT
├─ board/     # Kakao place id 기반 보드 조회와 생성
├─ global/    # 공통 설정, 예외 처리, 파일 업로드, BaseTime
├─ place/     # Kakao Local API, 주변/검색/인기 장소, CSV 캐시
├─ report/    # 흔적 신고
├─ trace/     # 흔적, 흔적 요소, 이미지 업로드, 좋아요
└─ user/      # 회원가입, 내 정보, 프로필 수정, 탈퇴
```

일반적인 계층은 `controller -> service -> repository -> entity`이며, API 입출력은 `dto`를 통해 다룹니다. 컨트롤러에는 HTTP 요청/응답과 검증 진입점만 두고, 권한 확인과 비즈니스 규칙은 서비스에 둡니다.

## 도메인별 주요 파일

- `auth/controller/AuthController.java`: 로그인, OAuth 시작/콜백, 로그아웃
- `auth/jwt/JWTService.java`: JWT 생성과 검증
- `user/controller/UserController.java`: 회원가입, 내 정보, 수정, 탈퇴
- `place/controller/PlaceController.java`: 주변/검색/인기 장소 API
- `place/repository/PlaceCsvStore.java`: 장소 캐시 CSV 읽기/쓰기
- `board/controller/BoardController.java`: 장소별 보드 조회, 보드 상세, 보드 생성
- `trace/controller/TraceController.java`: 보드 흔적 목록/영역 조회, 흔적 CRUD, 이미지, 좋아요
- `archive/controller/ArchiveController.java`: 내 흔적, 아카이브, 즐겨찾기 장소
- `report/controller/ReportController.java`: 흔적 신고
- `global/exception/GlobalExceptionHandler.java`: 공통 예외 응답
- `global/file/FileService.java`, `FileWebConfig.java`: 업로드 저장과 정적 제공

## 주요 API 표면

```text
POST   /api/auth/login
GET    /api/auth/oauth/kakao
GET    /api/auth/oauth/kakao/callback
GET    /api/auth/oauth/google
GET    /api/auth/oauth/google/callback
GET    /api/auth/logout

POST   /api/user/signup
GET    /api/user/myinfo
PATCH  /api/user/update
DELETE /api/user/me

GET    /api/places/nearby
GET    /api/places/search
GET    /api/places/popular

GET    /api/places/{kakaoPlaceId}/board
GET    /api/boards/{boardId}
POST   /api/boards

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
POST   /api/traces/{traceId}/reports

GET    /api/me/traces
GET    /api/me/traces/{traceId}
GET    /api/me/archive/calendar
GET    /api/me/archive/boards
GET    /api/me/received-likes
GET    /api/me/archive/favorite-places
POST   /api/me/archive/favorite-places/{kakaoPlaceId}
DELETE /api/me/archive/favorite-places/{kakaoPlaceId}
```

## 테스트 구조

테스트는 `src/test/java/com/yeginamgim/` 아래에 도메인별로 있습니다.

- controller 테스트: `auth`, `place`, `trace`, `user`
- service 테스트: `auth`, `board`, `place`, `user`
- util/repository 테스트: `place/util`, `place/repository`
- 공통 예외 테스트: `global/exception`

## 작업 원칙

- 엔티티를 API 응답으로 직접 반환하지 않습니다.
- DB 컬럼명이나 DTO shape는 프론트 사용처와 테스트를 확인한 뒤 바꿉니다.
- JWT/OAuth/security 변경 전에는 `auth` 코드와 테스트를 먼저 읽습니다.
- 업로드 파일은 소스 폴더가 아니라 실행 디렉터리의 `uploads/` 아래에 둡니다.
- 새 secret, token, password, API key를 커밋 대상 파일에 넣지 않습니다.
- `System.out.println`이나 임시 로그는 handoff 전에 제거합니다.

