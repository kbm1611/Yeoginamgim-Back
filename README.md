# YEOGINAMGIM-BACKEND
`YEOGINAMGIM-BACKEND`는 **여기남김** 프로젝트의 Spring Boot 백엔드입니다.
사용자 로그인, 장소 정보 저장, 공간 보드 관리, 흔적 작성/조회/수정/삭제, 이미지 업로드, 카카오 API 연동 등의 서버 기능을 담당합니다.

---
## 프로젝트 개요
여기남김은 사용자가 실제 장소를 기반으로 디지털 공간에 흔적을 남길 수 있는 서비스입니다.
사용자는 카카오 장소 검색을 통해 공간을 찾고, 해당 공간의 보드에 포스트잇, 사진, 텍스트, 스티커 등의 흔적을 남길 수 있습니다.
백엔드는 다음 기능을 담당합니다.
- 회원가입 / 로그인
- JWT 인증
- 카카오 / 구글 OAuth 로그인
- 카카오 장소 API 연동
- 장소 정보 저장 및 조회
- 공간 보드 생성 및 조회
- 흔적 작성 / 조회 / 수정 / 삭제
- 이미지 파일 업로드
- 사용자별 기록 관리
- 인기 장소 / 인기 흔적 조회

---
## 기술 스택
### Backend
- Java
- Spring Boot
- Spring Web
- Spring Data JPA
- JWT
- Lombok

### Database
- MySQL

### External API
- Kakao Local API
- Kakao Map API
- Google OAuth API
- Kakao OAuth API

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
│  │  │     └─ yeoginangim
│  │  │        ├─ YeoginangimApplication.java
│  │  │        ├─ config
│  │  │        ├─ controller
│  │  │        ├─ service
│  │  │        ├─ repository
│  │  │        ├─ entity
│  │  │        ├─ dto
│  │  │        ├─ exception
│  │  │        └─ util
│  │  │
│  │  └─ resources
│  │     └─ application.properties
│  │
│  └─ test
│
├─ build.gradle
├─ settings.gradle
├─ .gitignore
└─ README.md
```
---
### `config`
프로젝트 전역 설정 클래스를 관리하는 폴더입니다.
예를 들어 CORS 설정, WebClient 설정, 파일 업로드 설정, Swagger 설정 등이 들어갈 수 있습니다.
역할 예시:
- 프론트엔드와 통신하기 위한 CORS 설정
- 외부 API 호출을 위한 WebClient 설정
- 파일 업로드 경로 설정
- Swagger API 문서 설정
- 공통 Bean 등록

---
### `controller`
클라이언트의 요청을 받는 API 컨트롤러를 관리하는 폴더입니다.
프론트엔드에서 요청한 URL을 받아서 Service로 전달하고, 처리 결과를 응답합니다.
Controller는 복잡한 비즈니스 로직을 직접 처리하지 않고 Service에 위임하는 것이 좋습니다.
역할 예시:
- 회원가입 요청 받기
- 로그인 요청 받기
- 장소 검색 요청 받기
- 흔적 작성 요청 받기
- 이미지 업로드 요청 받기

---
### `service`
비즈니스 로직을 처리하는 폴더입니다.
Controller에서 받은 요청을 실제로 처리하며, 필요한 경우 Repository를 통해 DB와 통신합니다.
역할 예시:
- 회원가입 처리
- 로그인 처리
- 카카오 장소 검색 처리
- 장소 저장 처리
- 공간 보드 생성 처리
- 흔적 작성 처리
- 흔적 수정/삭제 처리
- 이미지 저장 처리
- 인기 장소 계산

---
### `repository`
DB에 접근하는 JPA Repository를 관리하는 폴더입니다.
Entity를 기준으로 데이터를 저장, 조회, 수정, 삭제합니다.
기본적인 CRUD는 `JpaRepository`를 상속받아 사용할 수 있습니다.
역할 예시:
- 회원 정보 조회
- 장소 정보 저장
- 특정 장소의 보드 조회
- 보드에 남겨진 흔적 목록 조회
- 사용자가 작성한 흔적 조회

---
### `entity`
DB 테이블과 매핑되는 JPA Entity 클래스를 관리하는 폴더입니다.
Entity는 실제 데이터베이스 테이블 구조와 연결됩니다.
역할 예시:
- 회원 테이블
- 장소 테이블
- 공간 보드 테이블
- 흔적 테이블
- 흔적 요소 테이블

---
### `dto`
데이터 전달 객체를 관리하는 폴더입니다.
프론트엔드와 백엔드가 데이터를 주고받을 때 Entity를 직접 사용하지 않고 DTO를 사용합니다.
DTO를 사용하면 필요한 데이터만 안전하게 요청/응답할 수 있습니다.
역할 예시:
- 회원가입 요청 데이터
- 로그인 요청 데이터
- 장소 검색 응답 데이터
- 흔적 작성 요청 데이터
- 흔적 상세 응답 데이터

---
### `exception`
예외 처리 관련 클래스를 관리하는 폴더입니다.
프로젝트에서 발생하는 에러를 일관된 형태로 응답하기 위해 사용합니다.
역할 예시:
- 존재하지 않는 회원 예외
- 존재하지 않는 장소 예외
- 로그인 실패 예외
- 권한 없음 예외
- 잘못된 요청값 예외
- 외부 API 호출 실패 예외
- 파일 업로드 실패 예외

---
### `util`
여러 곳에서 공통으로 사용하는 유틸리티 클래스를 관리하는 폴더입니다.
특정 도메인에 속하지 않는 공통 기능을 넣습니다.
역할 예시:
- 파일명 생성
- 날짜 변환
- 좌표 거리 계산
- 문자열 처리
- 랜덤 코드 생성

---
### `src/main/resources`
설정 파일과 리소스 파일을 관리하는 폴더입니다.
Spring Boot 설정 파일인 `application.properties`가 이곳에 위치합니다.

---
### `application.properties`
Spring Boot 기본 설정 파일입니다.
서버 포트, DB 연결, JPA 설정, 파일 업로드 설정 등을 작성합니다.

---
## 개발 시 주의사항
- Controller에는 복잡한 비즈니스 로직을 작성하지 않습니다.
- 핵심 로직은 Service에서 처리합니다.
- DB 접근은 Repository에서 처리합니다.
- Entity를 API 응답으로 직접 반환하지 않습니다.
- 요청과 응답은 DTO를 사용합니다.
- API 키와 비밀번호는 코드에 직접 작성하지 않습니다.
- `application-secret.properties`는 GitHub에 올리지 않습니다.
- 공통 설정은 `config` 폴더에서 관리합니다.
- 공통 예외 처리는 `exception` 폴더에서 관리합니다.
- 중복되는 기능은 `util` 폴더로 분리합니다.