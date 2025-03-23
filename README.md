# 📍 위치 기반 네트워킹 플랫폼 for IT 행사

## 📋 프로젝트 소개  

대규모 IT 행사에서 참가자들이 효과적으로 네트워킹할 수 있도록 도와주는 **위치 기반 네트워킹 플랫폼**입니다.  
**주변 반경 내 참가자 탐색 및 실시간 채팅 기능**을 제공하여, 보다 자연스럽고 효율적으로 네트워킹을 시작할 수 있도록 지원합니다.

---

## 💡 주제 선정 이유

- 대규모 IT 행사에서 참가자들은 **네트워킹**을 주요 목적으로 참가하지만, 내향적인 참가자에게는 쉽지 않습니다.
- 오프라인 명함 교환 방식은 **비효율적**이며, 현대에는 **온라인 탐색 → 오프라인 네트워킹**으로 패턴이 전환 중입니다.
- 이를 반영하여, **행사 현장에서 실시간으로 주변 참가자를 탐색하고 대화를 시작**할 수 있는 솔루션이 필요합니다.

---

## 🎯 프로젝트 목적

- 주변 참가자 탐색 및 실시간 대화를 통해 **즉시 네트워킹**이 가능하도록 함
- **관심사 및 직무 기반** 매칭으로 적절한 네트워킹 기회 제공
- 온라인과 오프라인을 혼합한 **하이브리드 네트워킹 경험** 제공

---

## 🚀 주요 기능

### 1. 로그인

- **OAuth 2.0** 활용 소셜 로그인
- **GitHub, Google, Kakao** 활용

### 2. 참가자 탐색

- **Geolocation API 활용** 반경 10m 내 참가자 표시
- 관심 분야, 직무 필터링

### 3. 참가자 프로필

- 닉네임, 관심사, 직무, 경력 등 표시
- 프로필 공개 여부 선택 가능

### 4. 실시간 채팅

- **Web Socket / Stomp 활용** 1:1 실시간 채팅
- 온/오프라인 시 **알람** 제공

---

## 🗂️ ERD

![image (1)](https://github.com/user-attachments/assets/8fbfde51-a266-4325-bbaf-212a7e9bdc5e)

![image (2)](https://github.com/user-attachments/assets/64c7c215-abd5-4971-8cde-af278dc2797b)

---

## 🌐 배포 아키텍쳐

<img width="890" alt="스크린샷 2025-03-05 오후 3 40 53" src="https://github.com/user-attachments/assets/e21e490b-c427-43f5-93b0-66fd3be8c14f" />

---

## 👤 사용자 시나리오

1. 사용자는 **소셜로그인**을 황용하여 서비스를 이용할 수 있다.
2. 사용자는 최초 로그인 시 **마이페이지를** 작성할 수 있다.
3. 사용자는 반경 10m 이내의 **참가자를 탐색**하여 채팅을 요청 및 수락할 수 있다.
4. 사용자는 **실시간 채팅**을 이용하여 네트워킹을 진행할 수 있다.

---

## 🖼️ 플로우 차트

- [플로우 차트 - Figma](https://www.figma.com/design/BqAjNeXjE2eDQFY8RESlLP/Design-Folder?node-id=945-24457&t=fpCwRSAcu1XeF8L9-4)
- [화면 플로우 - Figma Board](https://www.figma.com/board/ax96ke2aqaqznxijTxKrQy/%ED%99%94%EB%A9%B4-%ED%94%8C%EB%A1%9C%EC%9A%B0?node-id=37-1203)

---

## 🛠️ 기술 스택

### 🎨 디자인

- Figma, Protopie, Phase, AfterEffect

### 🖥️ 프론트엔드

- React, TypeScript, Tailwind CSS, Zustand
- ESLint, Prettier

### 🌐 백엔드

- Java, Gradle, Spring Boot, Spring Data JPA, Spring Security
- OAuth, Web Socket, Stomp
- Redis, MySQL, MongoDB
- AWS EC2, RDS, GitHub Actions, Docker
- Swagger API, IntelliJ IDEA

---

## 👥 팀원 소개

| 이름 | 역할 |
| --- | --- |
| 🎨 디자이너 | 이준, 황소희 |
| 🖥️ 프론트엔드 | 안주현, 윤가은, 유지수 |
| 🌐 백엔드 | 이지은, 이서원, 성현아, 이정훈 |

---

## 🔗 링크
📌 [I-Contact-Web](https://www.i-contacts.link)             
📌 [GitHub](https://github.com/DeepDive-Final-Project)  
📌 [Notion](https://www.notion.so/I-Contact-Team-Project-1a47fd02709c8034a6f0f43be421f718)  
📌 [Jira](https://qweqwerty12321-1740141206278.atlassian.net/jira/software/projects/ICT/pages)

---
