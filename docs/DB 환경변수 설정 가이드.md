# DB 환경변수 설정 가이드

## 1. 목적

이 프로젝트는 DB 접속 정보를 소스 코드에 직접 저장하지 않는다. 공통 설정은 `application.properties`로 관리하고, 개발자마다 다른 DB URL·사용자명·비밀번호는 프로젝트 루트의 `.env` 파일로 관리한다.

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

`.env`의 값은 Spring Boot 4용 `spring-dotenv` 라이브러리가 Spring 환경변수로 등록한다.

```gradle
developmentOnly 'me.paulschwarz:springboot4-dotenv:5.1.0'
```

## 2. 최초 설정 방법

### 2.1 `.env.example` 복사

프로젝트 루트의 `.env.example`을 복사하여 같은 위치에 `.env` 파일을 만든다.

PowerShell:

```powershell
Copy-Item .env.example .env
```

프로젝트 구조는 다음과 같아야 한다.

```text
swproject/
├── .env
├── .env.example
├── build.gradle
├── settings.gradle
└── src/
```

`.env`를 `src/main/resources` 아래에 만들지 않는다.

### 2.2 로컬 DB 접속 정보 입력

생성한 `.env`에 자신의 MySQL 접속 정보를 입력한다.

```dotenv
DB_URL=jdbc:mysql://127.0.0.1:포트번호/DB_이름
DB_USERNAME=DB_사용자명
DB_PASSWORD=DB_비밀번호
```

각 값의 의미는 다음과 같다.

| 환경변수 | 설명 | 예시                                  |
| --- | --- |---------------------------------------|
| `DB_URL` | JDBC 접속 주소와 데이터베이스명 | `jdbc:mysql://127.0.0.1:1234/db` |
| `DB_USERNAME` | MySQL 사용자명 | `user`                                |
| `DB_PASSWORD` | MySQL 사용자 비밀번호 | 실제 로컬 비밀번호                    |



## 3. MySQL 사전 준비

`.env`는 접속 정보만 제공한다. MySQL 서버, 데이터베이스, 사용자는 별도로 준비되어 있어야 한다.

개발용 예시:

```sql
CREATE DATABASE msds_db;

CREATE USER 'msds'@'localhost' IDENTIFIED BY '본인의_DB_비밀번호';

GRANT ALL PRIVILEGES ON msds_db.* TO 'msds'@'localhost';

FLUSH PRIVILEGES;
```

이미 동일한 데이터베이스와 사용자가 있다면 다시 생성하지 않는다.

현재 개발 설정에는 다음 옵션이 적용되어 있다.

```properties
spring.jpa.hibernate.ddl-auto=update
```

따라서 애플리케이션이 DB에 정상 접속하고 엔티티를 찾으면 필요한 테이블을 갱신한다. 단, `msds_db` 데이터베이스 자체는 미리 존재해야 한다.

## 4. 애플리케이션 실행

`.env`를 저장한 후 프로젝트 루트를 작업 디렉터리로 하여 실행한다.

PowerShell:

```powershell
./gradlew.bat bootRun
```

IntelliJ에서는 `MeditationApplication`을 실행한다. `.env`를 찾지 못한다면 Run Configuration의 `Working directory`가 프로젝트 루트인지 확인한다.

```text
C:\workspace\swproject
```

`.env`를 변경한 경우 DevTools 재시작만 기다리지 말고 애플리케이션을 완전히 종료한 뒤 다시 실행하는 것이 안전하다.

## 5. Git 관리 규칙

실제 접속 정보가 들어 있는 `.env`는 절대 커밋하지 않는다.

현재 `.gitignore`에는 다음 규칙이 있다.

```gitignore
.env
.env.*
!.env.example
```

각 파일의 Git 관리 원칙은 다음과 같다.

| 파일 | 커밋 여부 | 이유 |
| --- | --- | --- |
| `.env` | 커밋 금지 | 실제 비밀번호 등 로컬 민감정보 포함 |
| `.env.example` | 커밋 | 팀원이 필요한 환경변수 이름을 확인하는 템플릿 |
| `application.properties` | 커밋 | 공통 Spring/JPA 설정과 환경변수 참조 포함 |
| `build.gradle` | 커밋 | dotenv 의존성과 전체 빌드 설정 포함 |

`.env`가 정말 무시되는지는 다음 명령으로 확인할 수 있다.

```powershell
git check-ignore -v .env
```

Git 상태에 `.env`가 나타나지 않는 것이 정상이다.

```powershell
git status --short
```

실수로 `.env`를 커밋했다면 단순히 파일을 삭제하는 것으로 끝내지 말고 즉시 비밀번호를 변경하고 Git 추적에서도 제거한다.

```powershell
git rm --cached .env
```

## 6. `.env.example` 관리 규칙

새로운 환경변수가 추가되면 `.env.example`에도 변수 이름을 추가한다. 실제 비밀번호, 토큰, 운영 서버 주소는 넣지 않는다.

```dotenv
DB_URL=
DB_USERNAME=
DB_PASSWORD=
```

코드에서 새로운 변수를 사용하면서 `.env.example` 갱신을 누락하지 않는다.

## 7. 환경변수 우선순위

OS 또는 실행 환경에 같은 이름의 실제 환경변수가 설정되어 있으면 해당 값이 `.env`보다 우선한다.

예를 들어 PowerShell에서 다음 값을 설정하고 실행하면 `.env`의 `DB_PASSWORD` 대신 PowerShell 값이 사용된다.

```powershell
$env:DB_PASSWORD="temporary-password"
./gradlew.bat bootRun
```

현재 PowerShell 세션에 설정된 값을 제거하려면 다음과 같이 실행한다.

```powershell
Remove-Item Env:DB_PASSWORD
```

개발 환경에서는 `.env`를 사용할 수 있지만, 운영 환경에서는 서버·컨테이너·배포 플랫폼의 Secret 또는 환경변수 기능을 사용한다.

## 8. 자주 발생하는 오류

### 8.1 환경변수 placeholder를 찾지 못함

```text
Could not resolve placeholder 'DB_URL'
```

확인할 내용:

1. 프로젝트 루트에 `.env`가 있는지 확인한다.
2. 파일명이 `.env.txt`가 아닌지 확인한다.
3. `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` 철자가 정확한지 확인한다.
4. Gradle 프로젝트를 다시 불러온다.
5. 애플리케이션을 완전히 종료한 뒤 다시 실행한다.

### 8.2 Access denied

```text
Access denied for user 'msds'@'localhost'
```

`DB_USERNAME` 또는 `DB_PASSWORD`가 실제 MySQL 계정과 다르거나 해당 사용자에게 `msds_db` 접근 권한이 없는 상태이다.

### 8.3 Unknown database

```text
Unknown database 'msds_db'
```

JPA는 테이블을 생성할 수 있지만 MySQL 데이터베이스 자체가 없으면 연결할 수 없다. 먼저 데이터베이스를 생성한다.

```sql
CREATE DATABASE msds_db;
```

### 8.4 Communications link failure

```text
Communications link failure
```

MySQL 서버가 실행 중인지, 포트가 `3306`인지, `DB_URL`의 호스트와 포트가 올바른지 확인한다.

### 8.5 변경한 `.env` 값이 반영되지 않음

1. 실행 중인 애플리케이션을 완전히 종료한다.
2. IntelliJ Run Configuration에 같은 이름의 환경변수가 있는지 확인한다.
3. OS 환경변수가 `.env`보다 우선한다는 점을 확인한다.
4. 애플리케이션을 다시 실행한다.

## 9. 팀원 설정 체크리스트

- [ ] MySQL 서버가 실행 중인가?
- [ ] `msds_db` 데이터베이스가 존재하는가?
- [ ] MySQL 사용자에게 `msds_db` 권한이 있는가?
- [ ] 프로젝트 루트에 `.env`를 만들었는가?
- [ ] `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`를 모두 입력했는가?
- [ ] `.env`가 Git에서 무시되는가?
- [ ] `.env.example`에는 실제 비밀번호가 없는가?
- [ ] Gradle 프로젝트를 다시 불러왔는가?
- [ ] 애플리케이션을 완전히 재시작했는가?
