# YEOGINAMGIM-BACKEND

`YEOGINAMGIM-BACKEND`는 **여기남김** 프로젝트의 Spring Boot 백엔드입니다.

**여기남김**은 사용자가 실제 장소를 기반으로 디지털 공간에 포스트잇 형태의 흔적을 남길 수 있는 서비스입니다.  
사용자는 카카오 장소 검색을 통해 카페, 음식점, 도서관, 학교, 축제장 같은 공간을 찾고, 해당 공간의 보드에 텍스트, 사진, 스티커, 위치 정보를 포함한 포스트잇을 남길 수 있습니다.

백엔드는 다음 1차 기능을 담당합니다.

- 회원가입 / 로그인
- JWT 기반 인증
- 카카오 / 구글 OAuth 로그인
- 카카오 장소 API 연동
- 현재 위치 기반 주변 공간 조회
- 장소 정보 저장 및 조회
- 공간별 보드 생성 및 조회
- 보드 안 포스트잇 작성 / 조회 / 수정 / 삭제
- 포스트잇 이미지 업로드
- 포스트잇 꾸미기 요소 저장
- 좋아요 / 저장 기능
- 악성 글 / 욕설 신고 기능
- 사용자별 기록 보관함 조회
- 홈화면 인기 공간 / 최근 포스트잇 / 오늘 공감 기록 조회

---

## 프로젝트 개요

**여기남김**은 단순한 리뷰 서비스가 아니라, 사용자가 실제 공간에 자신의 감정과 추억을 남길 수 있는 **공간 기반 디지털 방명록 서비스**입니다.

기존 리뷰 서비스가 별점과 긴 후기 중심이었다면, 여기남김은 사용자가 공간에 방문했을 때 느낀 감정, 짧은 메모, 사진, 낙서, 스티커를 포스트잇처럼 남기는 경험을 제공합니다.

서비스의 핵심 흐름은 다음과 같습니다.

```text
사용자 로그인
→ 현재 위치 기반 주변 공간 탐색
→ 특정 공간 선택
→ 공간 보드 입장
→ 포스트잇 작성
→ 사진 / 텍스트 / 스티커 / 위치 정보 저장
→ 다른 사용자가 남긴 포스트잇 구경
→ 좋아요 / 저장 / 신고
→ 내가 남긴 기록 보관함에서 다시 보기
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
│  │  │        ├─ spot
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
│  │     └─ application-secret.properties
│  │
│  └─ test
│     └─ java
│        └─ com
│           └─ yeoginamgim
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

### `spot`

사용자가 기록을 남길 수 있는 실제 공간 정보를 관리하는 패키지입니다.

담당 역할:

- 카카오 장소 API 기반 장소 조회
- 현재 위치 기반 주변 공간 조회
- 장소 정보 DB 저장
- 장소 상세 조회
- 장소별 포스트잇 개수 조회
- 흔적 많은 순 인기 공간 조회
- 카카오 장소 ID 기반 중복 저장 방지

---

### `board`

특정 공간에 연결된 디지털 방명록 보드를 관리하는 패키지입니다.

담당 역할:

- 공간별 보드 생성
- 공간별 보드 조회
- 보드 상세 조회
- 보드에 배치된 포스트잇 목록 조회
- 보드 확대 / 축소 탐험에 필요한 기본 정보 제공

---

### `postit`

사용자가 보드에 남기는 포스트잇 기록을 관리하는 패키지입니다.

담당 역할:

- 포스트잇 작성
- 포스트잇 목록 조회
- 포스트잇 상세 조회
- 포스트잇 수정
- 포스트잇 삭제
- 포스트잇 위치 저장
- 포스트잇 색상 / 스타일 저장
- 포스트잇 이미지 연결
- 포스트잇 스티커 연결

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
별도의 핵심 엔티티를 만들기보다는 `User`, `Postit`, `Spot`, `Board`, `Reaction` 데이터를 조합하여 조회합니다.

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

Spot
 └─ Board
      └─ Postit
           ├─ PostitImage
           ├─ PostitSticker
           ├─ Reaction
           └─ Report

Sticker
 └─ PostitSticker
```

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

### Spot

카카오 장소 API에서 가져온 실제 공간 정보입니다.

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

특정 공간에 연결된 디지털 방명록 보드입니다.

주요 정보:

- 공간 ID
- 보드 이름
- 보드 설명
- 배경 이미지
- 보드 너비
- 보드 높이

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

---

### User

```text
GET /api/users/me
PATCH /api/users/me
```

---

### Spot

```text
GET /api/spots/nearby
GET /api/spots/popular
GET /api/spots/{spotId}
POST /api/spots/from-kakao
```

---

### Board

```text
GET /api/spots/{spotId}/boards
GET /api/boards/{boardId}
GET /api/boards/{boardId}/postits
```

---

### Postit

```text
POST /api/boards/{boardId}/postits
GET /api/boards/{boardId}/postits
GET /api/postits/{postitId}
PATCH /api/postits/{postitId}
PATCH /api/postits/{postitId}/position
DELETE /api/postits/{postitId}
```

---

### Reaction

```text
POST /api/postits/{postitId}/likes
DELETE /api/postits/{postitId}/likes

POST /api/postits/{postitId}/saves
DELETE /api/postits/{postitId}/saves
```

---

### Report

```text
POST /api/postits/{postitId}/reports
```

---

### Archive

```text
GET /api/users/me/postits
GET /api/users/me/postits/{postitId}
GET /api/users/me/archive/dates
GET /api/users/me/archive/spots
GET /api/users/me/liked-postits
GET /api/users/me/saved-postits
```

---

### Home

```text
GET /api/home
GET /api/home/popular-spots
GET /api/home/recent-postits
GET /api/home/today-liked-postits
```

---

### File

```text
POST /api/files/images
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
- 장소 정보는 `spot` 패키지에서 관리합니다.
- 공간 보드는 `board` 패키지에서 관리합니다.
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

---

### 2. 설정 파일 작성

`src/main/resources/application-secret.properties` 파일을 생성하고, 개인별 API 키와 DB 비밀번호를 작성합니다.

```properties
spring.datasource.username=your_db_username
spring.datasource.password=your_db_password

kakao.rest-api-key=your_kakao_rest_api_key

google.client-id=your_google_client_id
google.client-secret=your_google_client_secret
```

---

### 3. 의존성 설치 및 빌드

```bash
./gradlew build
```

Windows 환경에서는 다음 명령어를 사용할 수 있습니다.

```bash
gradlew.bat build
```

---

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
```

`.gitignore`에 반드시 포함해야 합니다.