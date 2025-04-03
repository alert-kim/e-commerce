# 잔액 조회/충전

잔액을 조회하고 충전할 수 있는 기능입니다.

## 1. 시퀀스 다이어그램

### 1.1 잔액 충전

```mermaid
sequenceDiagram
    participant server
    participant User
    participant UserBalance
    participant BalanceRecord
    server ->> User: 유저 검증
    server ->> UserBalance: 잔액 충전
    break 최대 잔액 초과
        UserBalance --> server: 에러 발생
    end
    UserBalance ->> server: 충전된 잔액
    server ->> BalanceRecord: 잔액 충전 내역 저장
```
- 유저 검증
  - 잔액 충전시, 해당 유저 아이디가 실제 있는 유저인지 검증합니다.
- 잔액이 생성되지 않은 유저
  - 충전 요청시 저장소가 없는 경우, 충전 요청 금액으로 초기화 하여 생성합니다.
- 충전 제약 
  - 최대 잔액을 초과해 저장할 수 없습니다.
- 내역 저장
  - 충전 내역을 저장합니다.
  
### 1.2 잔액 조회

```mermaid
sequenceDiagram
participant server
participant User
participant UserBalance
    server->>User: 유저 검증
    
    server->>UserBalance: 잔액 조회
    UserBalance-->>server: 잔액
```
- 해당 유저 아이디가 실제 있는 유저인지 검증합니다.
- 잔액 조회시, 해당 유저의 잔고가 생성되지 않은 경우, API 응답으로 잔고를 0으로 반환합니다.
    - 잔고를 실제 충전하기 시작할 때부터 의미가 있으므로, 따로 생성하지 않고 0으로 반환하도록 했습니다.

## 2. ERD
```mermaid
erDiagram
    USERS {
        int id PK
        varchar(50) name
        timestamp created_at
        timestamp updated_at
    }
    USER_BALANCES {
        int id PK
        int user_id FK
        decimal balance
        timestamp created_at
        timestamp updated_at
    }
    USER_BALANCE_RECORDS {
        int id PK
        int balance_id FK
        varchar(10) type
        decimal balance
        timestamp created_at
    }
    USERS one to one USER_BALANCES: has
    USER_BALANCES one to many USER_BALANCE_RECORDS: has
```
- 유저는 여러 개의 잔고를 가질 수 없으므로 1:1 관계로 설정했습니다.
- 유저 이름에 대한 조건은 없지만, name 필드를 추가했으며 이름에 대한 변경 가능성 때문에 updated_at을 추가했습니다.
