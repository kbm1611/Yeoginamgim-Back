# YEOGINAMGIM-BACKEND

`YEOGINAMGIM-BACKEND`는 **여기남김** 프로젝트의 Spring Boot 백엔드입니다.

**여기남김**은 사용자가 실제 장소를 기반으로 디지털 공간에 포스트잇 형태의 흔적을 남길 수 있는 서비스입니다.  
사용자는 카카오 장소 검색을 통해 카페, 음식점, 도서관, 학교, 축제장 같은 공간을 찾고, 해당 공간의 보드에 텍스트, 사진, 스티커, 위치 정보를 포함한 포스트잇을 남길 수 있습니다.

이 README는 **정규프로젝트2 - 여기남김 1차 기능**을 기준으로 작성되었습니다.

---

## 프로젝트 개요

**여기남김**은 단순한 리뷰 서비스가 아니라, 사용자가 실제 공간에 자신의 감정과 추억을 남길 수 있는 **공간 기반 디지털 방명록 서비스**입니다.

기존 리뷰 서비스가 별점과 긴 후기 중심이었다면, 여기남김은 사용자가 공간에 방문했을 때 느낀 감정, 짧은 메모, 사진, 낙서, 스티커를 포스트잇처럼 남기는 경험을 제공합니다.

서비스의 핵심 흐름은 다음과 같습니다.

```text
사용자 로그인
→ 현재 위치 기반 주변 공간 탐색
→ 특정 공간 선택
→ 카카오 장소 ID 기준으로 보드 조회 또는 생성
→ 공간 보드 입장
→ 포스트잇 작성
→ 사진 / 텍스트 / 스티커 / 위치 정보 저장
→ 다른 사용자가 남긴 포스트잇 구경
→ 좋아요 / 저장 / 신고
→ 내가 남긴 기록 보관함에서 다시 보기
```

---

## 1차 기능 범위

### 1. 회원가입 / 로그인

- 카카오 OAuth 로그인
- 구글 OAuth 로그인
- JWT 기반 회원가입 / 로그인
- 로그인한 사용자 기준으로 내가 남긴 포스트잇 조회
- 로그인한 사용자 기준으로 보관함 조회

---

### 2. 지도 기반 공간 탐색 시스템

- 현재 위치 기반 주변 공간 조회
- 카페, 음식점, 도서관 등 실제 장소 연동
- 카카오 장소 ID 기반 장소 식별
- 장소별 포스트잇 개수 표시
- 포스트잇이 많은 순서의 인기 공간 조회
- 카카오맵 API 기반 공간 조회

---

### 3. 공간 보드 시스템

- 카카오 장소 ID 기반 보드 생성
- 카카오 장소 ID 기반 보드 조회
- 하나의 카카오 장소당 하나의 보드 관리
- 실제 공간 벽처럼 포스트잇이 쌓이는 보드 제공
- 확대 / 축소 기반 보드 탐험 기능
- 포스트잇 위치 기반 보드 조회
- 오래된 포스트잇 유지 및 탐색
- 악성 글 / 욕설 신고 기능

---

### 4. 보관함 기능

- 내가 남긴 포스트잇 전체 조회
- 내가 남긴 포스트잇 개별 조회
- 날짜별 기록 조회
- 공간별 추억 아카이브 조회
- 좋아요 받은 포스트잇 조회
- 저장한 포스트잇 조회

---

### 5. 포스트잇 작성 시스템

- 포스트잇 메모 작성
- 폴라로이드 사진 업로드
- 손글씨 / 낙서 기능
- 스티커 꾸미기 기능
- 포스트잇 스타일 및 꾸미기 요소 선택
- 원하는 위치에 포스트잇 배치
- 작성한 포스트잇 수정 / 삭제

---

### 6. 홈화면

- 내 위치 기준 인기 공간 리스트 조회
- 방금 올라온 포스트잇 조회
- 오늘 많이 공감받은 포스트잇 조회

---

## 장소 / 보드 처리 방식

이 프로젝트에서는 장소 상세 정보를 별도 DB 테이블로 저장하지 않습니다.

장소명, 주소, 카테고리, 좌표와 같은 장소 상세 정보는 **카카오 장소 API 응답 또는 CSV 파일**을 통해 관리합니다.  
DB에는 장소 자체를 저장하지 않고, 사용자가 포스트잇을 남길 수 있는 보드만 저장합니다.

`board` 테이블은 카카오 장소 고유 ID인 `kakao_place_id`를 기준으로 생성됩니다.  
즉, 하나의 카카오 장소는 하나의 보드를 가집니다.

```text
카카오 장소 API / CSV
→ kakao_place_id 기준으로 장소 식별
→ board 테이블에서 kakao_place_id로 보드 조회
→ 보드가 없으면 새 보드 생성
→ board_id 기준으로 포스트잇 작성 / 조회
```

---

## 기술 스택

### Backend

- Java
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security
- JWT
- Lombok

### Database

- MySQL

### External API

- Kakao Local API
- Kakao Map API
- Kakao OAuth API
- Google OAuth API

### Build Tool

- Gradle

---

## 프로젝트 구조

```text
backend
├─ src
│  ├─ main
│  │  ├─ java
│  │  │  └─ com
│  │  │     └─ yeoginamgim
│  │  │        ├─ AppStart.java
│  │  │        │
│  │  │        ├─ global
│  │  │        │
│  │  │        ├─ auth
│  │  │        │
│  │  │        ├─ user
│  │  │        │
│  │  │        ├─ place
│  │  │        │
│  │  │        ├─ board
│  │  │        │
│  │  │        ├─ postit
│  │  │        │
│  │  │        ├─ reaction
│  │  │        │
│  │  │        ├─ report
│  │  │        │
│  │  │        ├─ archive
│  │  │        │
│  │  │        ├─ home
│  │  │        │
│  │  │        ├─ file
│  │  │        │
│  │  │        └─ external
│  │  │
│  │  └─ resources
│  │     ├─ application.properties
│  │     ├─ application-secret.properties
│  │     └─ data
│  │        └─ places.csv
│  │
│  └─ test
│     └─ java
│        └─ com
│           └─ yeoginamgim
│
├─ docs
│  ├─ API_SPEC.md
│  ├─ BRANCH_RULE.md
│  ├─ COMMIT_RULE.md
│  ├─ ENV_SETTING.md
│  └─ ERD.md
│
├─ build.gradle
├─ settings.gradle
├─ .gitignore
└─ README.md
```

---

## 패키지 역할

### `global`

프로젝트 전체에서 공통으로 사용하는 설정, 예외 처리, 응답 형식, 유틸 기능을 관리하는 패키지입니다.

담당 역할:

- Security 설정
- CORS 설정
- 공통 예외 처리
- 공통 응답 형식
- 공통 엔티티 시간 처리
- 파일 업로드 설정
- 공통 유틸 기능

---

### `auth`

로그인, JWT, OAuth 인증 기능을 담당하는 패키지입니다.

담당 역할:

- 일반 로그인
- JWT 토큰 발급
- JWT 토큰 검증
- 카카오 OAuth 로그인
- 구글 OAuth 로그인
- 로그인 사용자 인증 처리
- 토큰 재발급
- 로그아웃 처리

---

### `user`

회원 정보를 관리하는 패키지입니다.

담당 역할:

- 회원 정보 조회
- 로그인한 사용자 정보 조회
- 닉네임 수정
- 프로필 이미지 조회
- 사용자 권한 관리
- 사용자가 남긴 포스트잇과 연결

---

### `place`

카카오 장소 API와 CSV 파일을 기반으로 장소 정보를 조회하는 패키지입니다.

이 프로젝트에서는 장소 상세 정보를 별도 DB 테이블로 저장하지 않습니다.  
카카오 장소 API에서 제공하는 고유 ID를 기준으로 장소를 식별하고, 장소명, 주소, 카테고리, 좌표 등 필요한 장소 정보는 CSV 파일 또는 카카오 API 응답으로 관리합니다.

담당 역할:

- 카카오 장소 ID 기반 장소 식별
- CSV 파일 기반 장소 정보 조회
- 현재 위치 기반 주변 장소 조회
- 카카오 장소 API 연동
- 장소별 포스트잇 개수 조회
- 포스트잇 많은 순 인기 장소 조회
- 보드 조회에 필요한 장소 정보 제공

---

### `board`

카카오 장소 ID에 연결된 디지털 방명록 보드를 관리하는 패키지입니다.

장소 상세 정보는 DB에 저장하지 않지만, 사용자가 특정 장소에 포스트잇을 남기기 위해서는 우리 서비스 내부의 보드 식별자가 필요합니다.  
따라서 `board` 테이블은 `kakao_place_id`를 기준으로 생성되며, 하나의 카카오 장소는 하나의 보드를 가집니다.

담당 역할:

- 카카오 장소 ID 기반 보드 생성
- 카카오 장소 ID 기반 보드 조회
- 보드 상세 조회
- 보드에 배치된 포스트잇 목록 조회
- 보드 생성일 / 수정일 관리

---

### `postit`

사용자가 보드에 남기는 포스트잇 기록을 관리하는 패키지입니다.

포스트잇은 `board_id`를 기준으로 특정 장소의 보드에 연결됩니다.

담당 역할:

- 보드별 포스트잇 작성
- 보드별 포스트잇 목록 조회
- 포스트잇 상세 조회
- 포스트잇 수정
- 포스트잇 삭제
- 포스트잇 위치 저장
- 포스트잇 색상 / 스타일 저장
- 포스트잇 이미지 연결
- 포스트잇 스티커 연결
- 손글씨 / 낙서 데이터 연결

---

### `reaction`

포스트잇에 대한 좋아요, 저장 기능을 관리하는 패키지입니다.

담당 역할:

- 포스트잇 좋아요 등록
- 포스트잇 좋아요 취소
- 포스트잇 저장 등록
- 포스트잇 저장 취소
- 사용자별 중복 반응 방지
- 포스트잇 좋아요 수 / 저장 수 관리

---

### `report`

악성 글, 욕설, 부적절한 포스트잇 신고 기능을 관리하는 패키지입니다.

담당 역할:

- 포스트잇 신고 등록
- 신고 사유 저장
- 신고 상태 관리
- 사용자별 중복 신고 방지
- 신고된 포스트잇 상태 변경

---

### `archive`

사용자별 기록 보관함 기능을 관리하는 패키지입니다.

보관함은 사용자가 남긴 포스트잇을 모아 보여주는 기능입니다.  
별도의 핵심 엔티티를 만들기보다는 `User`, `Postit`, `Board`, `Reaction` 데이터를 조합하여 조회합니다.

담당 역할:

- 내가 남긴 포스트잇 전체 조회
- 내가 남긴 포스트잇 상세 조회
- 날짜별 기록 조회
- 공간별 추억 아카이브 조회
- 내가 받은 좋아요 수 조회
- 내가 저장한 포스트잇 조회

---

### `home`

홈화면에 필요한 사용자 친화적 데이터를 관리하는 패키지입니다.

담당 역할:

- 내 위치 기준 인기 공간 조회
- 방금 올라온 포스트잇 조회
- 오늘 많이 공감받은 포스트잇 조회
- 홈화면 통합 데이터 조회

---

### `file`

이미지 파일 업로드 기능을 관리하는 패키지입니다.

담당 역할:

- 포스트잇 이미지 업로드
- 프로필 이미지 업로드
- 파일명 변환
- 파일 저장 경로 관리
- 이미지 URL 반환

---

### `external`

외부 API 통신 기능을 관리하는 패키지입니다.

담당 역할:

- 카카오 Local API 통신
- 카카오 장소 검색 API 통신
- 카카오 OAuth API 통신
- 구글 OAuth API 통신
- 외부 API 응답 DTO 관리

---

## 주요 도메인 구조

```text
User
 ├─ Postit
 ├─ Reaction
 └─ Report

Board
 └─ Postit

Postit
 ├─ PostitImage
 ├─ PostitSticker
 ├─ PostitDrawing
 ├─ Reaction
 └─ Report

Sticker
 └─ PostitSticker
```

`Board`는 카카오 장소 ID인 `kakao_place_id`를 기준으로 생성됩니다.  
장소명, 주소, 좌표 같은 장소 상세 정보는 DB에 저장하지 않고 CSV 파일 또는 카카오 API 응답을 통해 조회합니다.

---

## 주요 도메인 설명

### User

서비스를 사용하는 회원입니다.

주요 정보:

- 이메일
- 닉네임
- 프로필 이미지
- OAuth 제공자
- OAuth 제공자 ID
- 사용자 권한

---

### Place

카카오 장소 API와 CSV 파일을 기반으로 관리되는 장소 정보입니다.

`Place`는 DB Entity가 아니라, 카카오 장소 ID를 기준으로 조회되는 장소 개념입니다.

주요 정보:

- 카카오 장소 ID
- 장소명
- 카테고리
- 주소
- 도로명 주소
- 위도
- 경도
- 지역 정보
- 포스트잇 개수

---

### Board

특정 카카오 장소에 연결된 디지털 방명록 보드입니다.

장소 상세 정보는 저장하지 않고, 카카오 장소 ID만 저장합니다.

주요 정보:

- 보드 ID
- 카카오 장소 ID
- 생성일
- 수정일

---

### Postit

사용자가 공간 보드에 남긴 포스트잇 기록입니다.

주요 정보:

- 보드 ID
- 작성자 ID
- 내용
- 배경 색상
- 글자 색상
- 글꼴 스타일
- 보드 위 x 좌표
- 보드 위 y 좌표
- 너비
- 높이
- 회전값
- z-index
- 좋아요 수
- 저장 수
- 상태값

---

### PostitImage

포스트잇에 첨부된 사진 정보입니다.

주요 정보:

- 포스트잇 ID
- 이미지 URL
- 원본 파일명
- 이미지 순서
- 이미지 위치
- 이미지 크기
- 회전값

---

### Sticker

포스트잇에 붙일 수 있는 스티커 원본 정보입니다.

주요 정보:

- 스티커 이름
- 스티커 이미지 URL
- 스티커 카테고리
- 사용 여부

---

### PostitSticker

사용자가 특정 포스트잇에 붙인 스티커 정보입니다.

주요 정보:

- 포스트잇 ID
- 스티커 ID
- 스티커 위치
- 스티커 크기
- 회전값
- z-index

---

### Reaction

사용자가 포스트잇에 남긴 좋아요 또는 저장 기록입니다.

주요 정보:

- 사용자 ID
- 포스트잇 ID
- 반응 타입

반응 타입 예시:

```text
LIKE
SAVE
```

---

### Report

사용자가 부적절한 포스트잇을 신고한 기록입니다.

주요 정보:

- 신고자 ID
- 포스트잇 ID
- 신고 사유
- 신고 상세 내용
- 신고 상태

신고 상태 예시:

```text
PENDING
ACCEPTED
REJECTED
```

---

## 주요 API 설계 예시

### Auth

```text
POST /api/auth/login
POST /api/auth/logout
POST /api/auth/reissue
GET /api/auth/oauth/kakao
GET /api/auth/oauth/google
```

### Place

```text
GET /api/places/nearby
GET /api/places/popular
```

### Board

```text
GET /api/places/{kakaoPlaceId}/board
GET /api/boards/{boardId}
GET /api/boards/{boardId}/postits
```

### Postit

```text
POST /api/boards/{boardId}/postits
GET /api/boards/{boardId}/postits
GET /api/postits/{postitId}
PATCH /api/postits/{postitId}
PATCH /api/postits/{postitId}/position
DELETE /api/postits/{postitId}
```

### Reaction

```text
POST /api/postits/{postitId}/likes
DELETE /api/postits/{postitId}/likes

POST /api/postits/{postitId}/saves
DELETE /api/postits/{postitId}/saves
```

### Report

```text
POST /api/postits/{postitId}/reports
```

### Archive

```text
GET /api/users/me/postits
GET /api/users/me/postits/{postitId}
GET /api/users/me/archive/dates
GET /api/users/me/archive/spots
GET /api/users/me/liked-postits
GET /api/users/me/saved-postits
```

### Home

```text
GET /api/home
GET /api/home/popular-spots
GET /api/home/recent-postits
GET /api/home/today-liked-postits
```

### File

```text
POST /api/files/images
```

---

## 추천 협업 문서 구조

README는 추후 발표 자료나 포트폴리오에서 요약해서 쓰기 쉽도록 **프로젝트 소개 중심**으로 유지합니다.  
개발하면서 자주 바뀌는 협업 문서는 `docs` 폴더에 따로 관리하는 것을 추천합니다.

```text
docs
├─ API_SPEC.md
├─ BRANCH_RULE.md
├─ COMMIT_RULE.md
├─ ENV_SETTING.md
└─ ERD.md
```

각 문서 역할:

```text
API_SPEC.md    : API 명세서
BRANCH_RULE.md : 브랜치 전략
COMMIT_RULE.md : 커밋 메시지 규칙
ENV_SETTING.md : 로컬 실행 및 설정 파일 작성 방법
ERD.md         : DB 테이블 구조와 ERD 정리
```

---

## 설정 파일 관리

`application.properties`는 GitHub에 올리는 기본 설정 파일입니다.

```properties
spring.application.name=YEOGINAMGIM-BACKEND

spring.config.import=optional:application-secret.properties

server.port=8080

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

`application-secret.properties`는 개인별로 생성하는 비밀 설정 파일입니다.  
DB 비밀번호, API Key, JWT Secret처럼 외부에 공개되면 안 되는 값을 작성합니다.

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/yeoginamgim_db
spring.datasource.username=your_db_username
spring.datasource.password=your_db_password

kakao.rest-api-key=your_kakao_rest_api_key

google.client-id=your_google_client_id
google.client-secret=your_google_client_secret

jwt.secret=your_jwt_secret_key
```

---

## 개발 시 주의사항

- Controller에는 복잡한 비즈니스 로직을 작성하지 않습니다.
- 핵심 로직은 Service에서 처리합니다.
- DB 접근은 Repository에서 처리합니다.
- Entity를 API 응답으로 직접 반환하지 않습니다.
- 요청과 응답은 DTO를 사용합니다.
- API 키와 비밀번호는 코드에 직접 작성하지 않습니다.
- `application-secret.properties`는 GitHub에 올리지 않습니다.
- 공통 설정은 `global` 패키지에서 관리합니다.
- 인증 관련 기능은 `auth` 패키지에서 관리합니다.
- 사용자 정보는 `user` 패키지에서 관리합니다.
- 장소 정보 조회는 `place` 패키지에서 관리합니다.
- 보드 생성과 조회는 `board` 패키지에서 관리합니다.
- 포스트잇 작성과 꾸미기는 `postit` 패키지에서 관리합니다.
- 좋아요와 저장은 `reaction` 패키지에서 관리합니다.
- 신고 기능은 `report` 패키지에서 관리합니다.
- 내 기록 보관함은 `archive` 패키지에서 관리합니다.
- 홈화면 조회 기능은 `home` 패키지에서 관리합니다.
- 이미지 업로드 기능은 `file` 패키지에서 관리합니다.
- 외부 API 통신은 `external` 패키지에서 관리합니다.

---

## 추천 개발 흐름

### 1. 프로젝트 클론

```bash
git clone [repository-url]
cd YEOGINAMGIM-BACKEND
```

### 2. 설정 파일 작성

`src/main/resources/application-secret.properties` 파일을 생성하고, 개인별 API 키와 DB 비밀번호를 작성합니다.

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/yeoginamgim_db
spring.datasource.username=your_db_username
spring.datasource.password=your_db_password

kakao.rest-api-key=your_kakao_rest_api_key

google.client-id=your_google_client_id
google.client-secret=your_google_client_secret

jwt.secret=your_jwt_secret_key
```

### 3. 의존성 설치 및 빌드

```bash
./gradlew build
```

Windows 환경에서는 다음 명령어를 사용할 수 있습니다.

```bash
gradlew.bat build
```

### 4. 서버 실행

```bash
./gradlew bootRun
```

Windows 환경에서는 다음 명령어를 사용할 수 있습니다.

```bash
gradlew.bat bootRun
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
```

`.gitignore`에 반드시 포함해야 합니다.
