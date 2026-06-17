## 2. 팀 구성 및 역할

<table cellspacing="0" cellpadding="12">
  <tr>
    <td align="center" width="33%" bgcolor="#1F4E79">
      <strong>김용성</strong>
    </td>
    <td align="center" width="33%" bgcolor="#1F4E79">
      <strong>강병모</strong>
    </td>
    <td align="center" width="33%" bgcolor="#1F4E79">
      <strong>이태형</strong>
    </td>
  </tr>
  <tr>
    <td align="center">
      <strong>팀장 / 백엔드</strong>
    </td>
    <td align="center">
      <strong>Git 병합 / 파이썬</strong>
    </td>
    <td align="center">
      <strong>배포 / 백엔드</strong>
    </td>
  </tr>
  <tr>
    <td valign="top">
      <ul>
        <li>서비스 주요 화면 프론트엔드 구현</li>
        <li>커스텀 보드 DB 설계 및 CRUD 구현</li>
        <li>기획 및 일정 관리</li>
      </ul>
    </td>
    <td valign="top">
      <ul>
        <li>욕설 필터 기능 구현 및 배포</li>
        <li>사용자 정보 관리 기능 구현</li>
        <li>Redis 활용 이메일 인증</li>
      </ul>
    </td>
    <td valign="top">
      <ul>
        <li>AWS 배포 인프라</li>
        <li>UI 구성 및 사용자 흐름</li>
        <li>팔로우, 알림, 신고, 추천</li>
      </ul>
    </td>
  </tr>
  <tr>
    <td align="center">
      <a href="https://github.com/ys06o">GitHub</a>
    </td>
    <td align="center">
      <a href="https://github.com/kbm1611">GitHub</a>
    </td>
    <td align="center">
      <a href="https://github.com/lth0330">GitHub</a>
    </td>
  </tr>
</table>

## 3. 기술 스택

### 프론트엔드

![React](https://img.shields.io/badge/REACT-61DAFB?style=for-the-badge&logo=react&logoColor=black)
![Vite](https://img.shields.io/badge/VITE-646CFF?style=for-the-badge&logo=vite&logoColor=white)
![JavaScript](https://img.shields.io/badge/JAVASCRIPT-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black)
![React Router](https://img.shields.io/badge/REACT%20ROUTER-CA4245?style=for-the-badge&logo=reactrouter&logoColor=white)
![Tailwind CSS](https://img.shields.io/badge/TAILWIND%20CSS-06B6D4?style=for-the-badge&logo=tailwindcss&logoColor=white)

### 백엔드

![Java](https://img.shields.io/badge/JAVA-F89820?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/SPRING%20BOOT-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![BCrypt](https://img.shields.io/badge/BCRYPT%20PASSWORD%20ENCODER-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/SPRING%20DATA%20JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)
![Lombok](https://img.shields.io/badge/LOMBOK-BC4521?style=for-the-badge)

### 욕설 필터링

![Python](https://img.shields.io/badge/PYTHON-3776AB?style=for-the-badge&logo=python&logoColor=white)
![FastAPI](https://img.shields.io/badge/FASTAPI-009688?style=for-the-badge&logo=fastapi&logoColor=white)
![PyTorch](https://img.shields.io/badge/PYTORCH-EE4C2C?style=for-the-badge&logo=pytorch&logoColor=white)
![ELECTRA](https://img.shields.io/badge/ELECTRA-FF6F00?style=for-the-badge&logo=google&logoColor=white)

### 데이터베이스 / 저장소

![MySQL](https://img.shields.io/badge/MYSQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/REDIS-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![AWS RDS](https://img.shields.io/badge/AWS%20RDS-527FFF?style=for-the-badge&logo=amazonrds&logoColor=white)
![AWS S3](https://img.shields.io/badge/AWS%20S3-569A31?style=for-the-badge&logo=amazons3&logoColor=white)
![CSV Cache](https://img.shields.io/badge/CSV%20CACHE-217346?style=for-the-badge)

### 외부 API

![Kakao Map](https://img.shields.io/badge/KAKAO%20MAP-FFCD00?style=for-the-badge&logo=kakao&logoColor=black)
![Kakao Local API](https://img.shields.io/badge/KAKAO%20LOCAL%20API-FFCD00?style=for-the-badge&logo=kakao&logoColor=black)
![Kakao OAuth](https://img.shields.io/badge/KAKAO%20OAUTH-FFCD00?style=for-the-badge&logo=kakao&logoColor=black)
![Google OAuth](https://img.shields.io/badge/GOOGLE%20OAUTH-4285F4?style=for-the-badge&logo=google&logoColor=white)
![SMTP Mail](https://img.shields.io/badge/SMTP%20MAIL-EA4335?style=for-the-badge&logo=gmail&logoColor=white)

### 배포 / 인프라

![Docker](https://img.shields.io/badge/DOCKER-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![AWS Elastic Beanstalk](https://img.shields.io/badge/AWS%20ELASTIC%20BEANSTALK-FF9900?style=for-the-badge&logo=amazonaws&logoColor=white)
![Amazon CloudFront](https://img.shields.io/badge/AMAZON%20CLOUDFRONT-8C4FFF?style=for-the-badge&logo=amazonaws&logoColor=white)
![S3 Static Hosting](https://img.shields.io/badge/S3%20STATIC%20HOSTING-569A31?style=for-the-badge&logo=amazons3&logoColor=white)

## 4. 서비스 아키텍처

본 서비스는 사용자가 웹 또는 모바일 환경에서 React 기반 클라이언트에 접속하면, AWS CloudFront와 S3 정적 호스팅을 통해 프론트엔드 화면을 제공하는 구조입니다.  
클라이언트는 Spring Boot 백엔드 API와 통신하며, 필요한 경우 FastAPI 기반 Python 서버를 호출하여 욕설 필터링 기능을 수행합니다.

데이터는 MySQL, Redis, AWS S3를 통해 관리되며, 지도, 검색, 로그인, 메일 기능은 Kakao 및 Google OAuth, Kakao Local API, Kakao Maps SDK, SMTP 서버와 연동하여 처리합니다.  
Python FastAPI 서버는 Docker 이미지로 빌드된 후 AWS ECR에 저장되고, AWS Elastic Beanstalk를 통해 컨테이너 환경에서 실행됩니다.

![Yeoginamgim System Architecture](./images/system-architecture.png)

## 5. 시연영상 링크

- [시연영상 보러가기](https://www.youtube.com/watch?v=tKTGVVTD5zw)

## 6. 참고 링크

- [프로젝트 배포 링크](https://d3vvhygufn2oi5.cloudfront.net/splash)
- [프론트엔드 깃허브 링크](https://github.com/kbm1611/Yeoginamgim-Front)
- [파이썬 깃허브 링크](https://github.com/kbm1611/Yeoginamgim-Python)
- [API 문서 / 참고 자료](https://docs.google.com/spreadsheets/d/1JPG2olfW4FKcZR7B_yKi8_nwn-M5Zn7T_9ogwQE2qAI/edit?gid=678243450#gid=678243450)
- [PPT 링크](https://canva.link/p8bahrbzjqnzgro)
- [피그마 링크](https://www.figma.com/design/PyZlUiPCcvxLt8mgNn91FM/%EC%A0%9C%EB%AA%A9-%EC%97%86%EC%9D%8C?t=HEMUU0J3LAuI08KZ-1)
