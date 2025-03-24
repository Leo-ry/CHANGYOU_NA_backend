# 💸 송금 시스템

계좌 등록/삭제, 입출금/이체 기능을 가진 간단 송금 시스템입니다.
과제의 요구사항에 따라 작성하였습니다.

---

## 🔧 기술 스탭

| 구별       | 기술                                       |
|------------|--------------------------------------------|
| Language   | Java 17 (Intellij ver)                     |
| Framework  | Spring Boot 3.4.4, Spring Data JPA         |
| DB         | MariaDB 10.7                               |
| Infra      | Docker, Docker Compose                     |
| Build Tool | Gradle                                     |
| Testing    | JUnit5, Mockito                            |
| Others     | Domain-Driven Design, Transactional Events |

---

## 🧹 주요 기능

### ✅ 계좌 관리

- 신규 계좌 등록 (중복 방지)
- 계좌 삭제 (논리적 비활성화 처리)

### ✅ 입금 / 출금

- 금액 입출금
- 출금 시 **일일 한도 1,000,000원**
- 출금액, 잔고, 비활성 계좌 예외 처리
- 입출금 내역은 `DEPOSIT`, `WITHDRAW` 로 구분

### ✅ 계좌 간 이체

- 출금 계좌 → 입금 계좌 간 송금
- 수수료 1% 적용 (소수점 첫번째 자리에서 버림처리)
- 일일 한도 **3,000,000원**
- 이체 내역은 `TRANSFER_OUT`, `TRANSFER_IN` 으로 구분

### ✅ 결제내역 저장

- 입출금/이체 시 결제내역 자동 생성
- 도메인 이벤트 기반으로 `Transit` 저장

### ✅ 결제내역 조회

- 특정 계좌의 결제내역 페이징처리된 리스트 조회
- 최신순 정렬
- `DEPOSIT`, `WITHDRAW`, `TRANSFER_OUT`, `TRANSFER_IN` 구분 포함

---

## 📦 프로젝트 시작 방법

### 🔹 사전 준비

- Docker, Docker Compose 설치 필요

### 🔹 시작

```bash
docker-compose up --build
```

- 서버: `http://localhost:8080`
- MariaDB: `localhost:3306`

| 항목     | 값            |
| -------- | ------------- |
| DB Name  | wirebarley    |
| Username | wirebarley    |
| Password | wirebarley123 |

---

## 🔗 API 예시

### POST /accounts

다음 정보로 계좌 등록

```json
{
  "bankCode": "001",
  "accountNumber": "1111222233334444",
  "ownerName": "홍길동"
}
```

### DELETE /accounts/{id}

다음 정보로 계좌 삭제

PathVariable 계좌의 ID 값을 기준으로 논리적인 삭제처리 -> 별도 물리적인 삭제처리를 요청하지않음

### POST /transit/deposit

입금 요청

```json
{
  "bankCode": "001",
  "accountNumber": "1111222233334444",
  "amount": 500000
}
```

### POST /transit/withdraw

출금 요청

```json
{
  "bankCode": "001",
  "accountNumber": "1111222233334444",
  "amount": 300000
}
```

### POST /transit/transfer

계좌간 송금

```json
{
  "fromBankCode": "001",
  "fromAccountNumber": "1111222233334444",
  "toBankCode": "002",
  "toAccountNumber": "2222333344556677",
  "amount": 100000
}
```

### GET /transit/{bankCode}/{accountNumber}/list

결제내역 조회

```http
GET /accounts/001/1111222233334444/transactions?page=0&size=10
```

---

## 🛡️ 예외 처리

모든 예외는 다음 구조로 발생합니다:

```json
{
  "timestamp": "2025-03-24T11:33:00",
  "code": "ACCOUNT_404",
  "message": "계좌를 찾을 수 없습니다.",
  "status": 404
}
```

| 코드                                 | 설명          |
|------------------------------------|-------------|
| ACCOUNT_NOT_FOUND| 계좌 없음       |
|ACCOUNT_ALREADY_EXISTS| 계좌 중복       |
|ACCOUNT_ALREADY_CLOSED| 이미 해지된 계좌   |
|ACCOUNT_NOT_ACTIVE| 비활성계좌 상태   |
|INSUFFICIENT_BALANCE| 잔액 부족       |
|DAILY_WITHDRAW_LIMIT_EXCEEDED | 일일 출금 한도 초과 |
|DAILY_TANSFER_LIMIT_EXCEEDED| 일일 이체 한도 초과 |
|VALIDATION_ERROR| 잘못된 요청 파라미터 |


---

## 🔬 테스트

- 단위 테스트: 서비스 로직 및 예외 처리
- 통합 테스트: 결제내역 자동 저장, 도메인 이벤트 수신
- Mockito 기반 Mock Test 작성

```bash
./gradlew test
```

---

## 📁 디렉토리 구조 요조

```bash
src/main/java
├── domain
│   ├── entity
│   ├── enums
│   ├── repository
│   └── event
├── service
├── controller
├── dto
├── handler
```

---

## 🧠 아키텍처  \uuc124계 특징

- 도메인 중심 설계 (DDD 패턴 일부 적용)
- 도메인 이벤트를 활용한 사이드 이퍼티 분리 (결제내역 생성)
- 테스트 원칙 가지고 가능성과 책임 분리
- RESTful API 구조 준수

---

## 🏁 기타

- Swagger UI: `/swagger-ui/index.html` (springdoc 적용 시)
- 로컬 DB 초기화:

```bash
docker-compose down -v
```

---

## 🤛 하고싶은 말

제가 알고있는 지식을 최대한 동원하여 최신 버전 기준으로 작성하였습니다.
잘 부탁드리겠습니다.
감사합니다.
