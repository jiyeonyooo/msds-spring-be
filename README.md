# MSDS Backend

> Meditate. Slow Down. Stay.

MSDS 게스트하우스의 객실·예약·명상 프로그램·웰니스 서비스를 제공하는 Spring Boot REST API입니다. JWT 기반 회원 인증과 운영자 권한, 이미지 업로드, Swagger API 문서를 포함합니다.

## 주요 기능

- 회원가입·로그인·프로필 관리와 관리자 회원 관리
- 객실·실객실·부대시설 조회 및 운영자 관리
- 숙박 가능 여부 조회, 예약 생성·취소·상태 관리
- 명상 프로그램 신청·취소와 후기 관리
- 웰니스 체크·이력·통계와 조용한 공간 추천
- 1:1 문의 작성·조회와 관리자 답변
- 객실·시설·프로그램 이미지 업로드 및 로컬 파일 제공

## 기술 스택

| 구분 | 기술 |
| --- | --- |
| 언어 | Java 21 |
| 프레임워크 | Spring Boot 4.1, Spring MVC |
| 데이터 | Spring Data JPA, MySQL, H2(Test) |
| 인증·인가 | Spring Security, JWT(JJWT) |
| 문서 | springdoc OpenAPI, Swagger UI |
| 빌드 | Gradle 9 Wrapper, WAR |
| 기타 | Bean Validation, Lombok, Spring DevTools |

## 시작하기

### 사전 준비

- JDK 21
- MySQL
- Git

별도 Gradle 설치는 필요하지 않습니다. 저장소에 포함된 Gradle Wrapper를 사용합니다.

### 1. 데이터베이스 준비

애플리케이션 실행 전에 MySQL 서버와 데이터베이스가 존재해야 합니다. 개발용 예시는 다음과 같습니다.

```sql
CREATE DATABASE msds_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE USER 'msds'@'localhost' IDENTIFIED BY 'your-password';
GRANT ALL PRIVILEGES ON msds_db.* TO 'msds'@'localhost';
FLUSH PRIVILEGES;
```

JPA의 `ddl-auto`는 현재 `update`이므로 테이블은 실행 시 갱신되지만, 데이터베이스 자체는 자동으로 생성되지 않습니다.

### 2. 환경변수 설정

프로젝트 루트에서 예제 파일을 복사합니다.

```powershell
Copy-Item .env.example .env
```

macOS/Linux에서는 다음 명령을 사용합니다.

```bash
cp .env.example .env
```

`.env`의 필수 항목을 채웁니다.

```dotenv
DB_URL=jdbc:mysql://127.0.0.1:3306/msds_db
DB_USERNAME=msds
DB_PASSWORD=your-password
JWT_SECRET=replace-with-a-random-secret-at-least-32-bytes
JWT_ACCESS_TOKEN_VALIDITY_MS=7200000
```

`JWT_SECRET`은 최소 32바이트 이상의 충분히 긴 임의 문자열로 교체하세요. `.env`에는 비밀번호와 서명 키가 포함되므로 Git에 커밋하면 안 됩니다. 운영 환경에서는 `.env` 대신 배포 환경의 Secret 관리 기능을 사용합니다.

### 3. 애플리케이션 실행

Windows PowerShell:

```powershell
.\gradlew.bat bootRun
```

macOS/Linux:

```bash
./gradlew bootRun
```

서버는 기본적으로 `http://localhost:8080`에서 실행됩니다.

## API 문서

서버 실행 후 다음 주소에서 실제 컨트롤러 기준 API 명세를 확인할 수 있습니다.

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

주요 API 그룹은 다음과 같습니다.

| 경로 | 설명 | 기본 접근 권한 |
| --- | --- | --- |
| `/api/auth` | 회원가입·로그인·로그아웃 | 공개/회원 |
| `/api/users` | 내 정보 조회·수정·탈퇴 | 회원 |
| `/api/rooms`, `/api/facilities` | 객실·시설 조회 | 공개 |
| `/api/resv` | 예약 가능 여부와 회원 예약 | 조회 공개, 예약 회원 |
| `/api/meditation` | 프로그램·신청·후기 | 일부 조회 공개, 변경 회원 |
| `/api/wellness` | 웰니스 문항·체크·이력 | 문항·게스트 체크 공개, 이력 회원 |
| `/api/quietness` | 조용함 현황·공간 추천 | 공개 조회 |
| `/api/inquiries` | 내 문의 작성·조회 | 회원 |
| `/api/admin` | 운영자용 통합 API | 관리자 |

공통 JSON 응답 형식은 다음과 같습니다.

```json
{
  "code": "OK",
  "message": "요청을 처리했습니다.",
  "data": {}
}
```

인증이 필요한 요청은 로그인 응답의 access token을 전달해야 합니다.

```http
Authorization: Bearer <access-token>
```

서버는 세션과 refresh token을 사용하지 않는 stateless 방식입니다. 기본 access token 유효시간은 2시간입니다.

## 환경변수

| 변수 | 필수 | 기본값 | 설명 |
| --- | --- | --- | --- |
| `DB_URL` | 예 | 없음 | MySQL JDBC URL |
| `DB_USERNAME` | 예 | 없음 | MySQL 사용자명 |
| `DB_PASSWORD` | 예 | 없음 | MySQL 비밀번호 |
| `JWT_SECRET` | 예 | 없음 | JWT 서명 키, 최소 32바이트 권장 |
| `JWT_ACCESS_TOKEN_VALIDITY_MS` | 아니요 | `7200000` | access token 유효시간(ms) |
| `UPLOAD_DIR` | 아니요 | `./uploads` | 업로드 파일을 보존할 디렉터리 |
| `QUIETNESS_DEMO_ENABLED` | 아니요 | `false` | 조용함 데모 데이터 생성 |
| `QUIETNESS_DEMO_GUESTHOUSE_ID` | 아니요 | `1` | 조용함 데모 게스트하우스 ID |
| `PROGRAM_DEMO_ENABLED` | 아니요 | `false` | 프로그램 데모 데이터 생성 |
| `ROOM_DEMO_ENABLED` | 아니요 | `false` | 객실·시설 데모 데이터 생성 |
| `WELLNESS_DEMO_ENABLED` | 아니요 | `false` | 웰니스 데모 이력 생성 |
| `WELLNESS_STATS_MINIMUM_MEMBERS` | 아니요 | `5` | 통계를 공개할 최소 고유 회원 수 |
| `ADMIN_INQUIRY_DEMO_ENABLED` | 아니요 | `false` | 문의·관리자 데모 데이터 생성 |
| `ADMIN_INQUIRY_DEMO_EMAIL` | 아니요 | `admin@msds.com` | 데모 관리자 이메일 |
| `ADMIN_INQUIRY_DEMO_PASSWORD` | 아니요 | `Admin2026!` | 데모 관리자 비밀번호 |

데모 플래그는 로컬 시연 환경에서만 활성화하세요. 운영 환경에서는 모두 `false`를 유지하고, 데모 관리자 비밀번호도 사용하지 않습니다.

## 이미지 업로드

- 기본 업로드 위치는 프로젝트 루트의 `uploads`입니다.
- 객실·시설 이미지는 `/uploads/**`, 프로그램 이미지는 `/images/**`로 제공됩니다.
- 파일 하나의 최대 크기는 10MB, 요청 전체 최대 크기는 50MB입니다.
- 운영 환경에서는 `UPLOAD_DIR`을 배포 파일 외부의 영구 저장 경로로 지정해야 합니다.

## 프로젝트 구조

```text
src/main/java/
├── com/example/meditation/  # 애플리케이션, 웰니스, 조용함
├── global/                  # 공통 응답, 예외, 파일·OpenAPI 설정
├── member/                  # 인증, 회원, 문의
├── meditation_program/      # 명상 프로그램, 신청, 후기
├── resv/                    # 숙박 예약
└── room/                    # 객실과 시설

src/main/resources/
├── application.properties  # 공통 애플리케이션 설정
└── db/mock/                 # 수동 실행용 개발 데이터 SQL
```

각 도메인은 일반적으로 `controller`, `service`, `repository`, `entity`, `dto` 계층으로 구성됩니다.

## 테스트와 빌드

Windows PowerShell:

```powershell
.\gradlew.bat test
.\gradlew.bat build
```

macOS/Linux:

```bash
./gradlew test
./gradlew build
```

테스트는 MySQL 호환 모드의 인메모리 H2를 사용하므로 로컬 MySQL 접속 정보에 의존하지 않습니다.

## 프론트엔드 연동

[MSDS Frontend](https://github.com/jiyeonyooo/msds-react-fe)는 개발 환경에서 `/api`, `/meditation`, `/uploads` 요청을 이 서버로 프록시합니다. 기본 조합은 다음과 같습니다.

- Backend: `http://localhost:8080`
- Frontend: `http://localhost:5173`

백엔드 CORS는 로컬 프론트엔드 주소 `http://localhost:5173`과 `http://127.0.0.1:5173`을 허용합니다.

## 추가 문서

- [DB 환경변수 설정 가이드](docs/DB%20환경변수%20설정%20가이드.md)
- [공통 API 응답 및 예외 처리 가이드](docs/공통%20API%20응답%20및%20예외%20처리%20가이드.md)

## 문제 해결

- `Could not resolve placeholder 'DB_URL'`: 프로젝트 루트에 `.env`가 있는지와 실행 작업 디렉터리를 확인합니다.
- `Unknown database`: JDBC URL에 지정한 데이터베이스를 먼저 생성합니다.
- `Access denied`: MySQL 계정·비밀번호와 데이터베이스 권한을 확인합니다.
- `Communications link failure`: MySQL 실행 여부, 호스트와 포트를 확인합니다.
- `.env` 변경이 반영되지 않음: 애플리케이션을 완전히 종료한 뒤 다시 실행합니다.
