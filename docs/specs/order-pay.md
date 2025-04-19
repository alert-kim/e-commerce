# 주문 & 결제 & 완료된 주문 데이터 전송

- 주문과 결제를 진행합니다.
- 완료된 주문에 대해서는 일정 주기로 데이터를 전송합니다.

## 1. 주문& 결제
### 1.1 주문& 결제 시퀀스 다이어그램

```mermaid
sequenceDiagram
    autonumber
    participant server
    participant Order
    participant Product
    participant Coupon
    participant Balance
    participant Payment
    server->>User: 유저 검증

    server->>Order: 주문 생성

    server->>Product: 상품 재고 차감
    break 재고차감 실패 
        Product -->> server: 재고 부족
    end    
    Product -->> server: 상품 재고 반환
    server->>Order: 주문 상품 등록
    
    opt 쿠폰 사용 요청이 있는 경우
      server ->> Coupon: 쿠폰 사용
      break 쿠폰 사용 실패
        Coupon -->> server: 이미 사용된 쿠폰
      end
      Coupon -->> server: 쿠폰 정보 반환
    end

    server ->> Balance: 최종 결제 금액 차감
    break 결제 금액 차감 실패
      Balance -->> server: 잔고 부족
    end
    server ->> Payment: 결제 내역 생성
    server ->> Order: 결제 처리
    Order ->> Order: 결제 완료 처리, 이벤트 저장
```

- (1) 유저 검증을 진행합니다.
- (2) 주문을 생성합니다.
- (3) 상품 재고를 차감합니다.
  - 재고를 먼저 차감한 후, 결제를 진행합니다.
  - 결제 성공 후 재고 부족으로 환불 해야하는 상황을 막기 위함입니다.
- (6) 주문 상품을 등록합니다.
  - 확보한 재고를 주문 상품으로 등록합니다.
- (7) 쿠폰 사용 요청이 있는 경우, 쿠폰을 사용합니다.
- (8) 이미 사용된 쿠폰은 사용하지 못합니다.
- (10) 잔고에서 최종 결제 금액을 차감합니다.
- (11) 잔고가 부족한 경우 결제가 실패합니다.
- (12) 결제 내역을 생성합니다.
- (13) 주문을 결제 완료 처리합니다.
- (14) 완료된 주문에 대한 이벤트를 저장합니다
  - 저장된 이벤트는 외부 데이터 플랫폼에 주문 정보 전송, 상품 통계 데이터에 사용됩니다.

### 1.2 주문 & 결제 ERD

```mermaid
erDiagram
    ORDERS {
        int id PK
        int user_id
        varchar(25) status
        int coupon_id "(nullable)"
        decimal originalAmount
        decimal discountAmount
        decimal totalAmount
        timestamp created_at
        timestamp updated_at
    }
    ORDER_PRODUCTS {
        int order_id FK
        int product_id FK
        int quantity
        decimal unit_price
        decimal total_price
        timestamp created_at
    }
    PRODUCTS {
        int id PK
        varchar(15) status
        varchar(100) name
        text description
        decimal price
        timestamp created_at
        timestamp updated_at
    }
    PRODUCT_STOCKS {
        int product_id PK
        int quantity
        timestamp created_at
        timestamp updated_at
    } 
    PAYMENTS {
        int id PK
        int user_id
        int order_id
        decimal amount
        timestamp created_at
    }
    COUPONS {
        int id PK
        int user_id
        int coupon_source_id FK
        timestamp used_at "(nullable)"
        varchar(15) name
        deciaml discount_amount
        timestamp created_at
        timestamp updated_at
    }

    ORDERS one to many ORDER_PRODUCTS: includes
    ORDER_PRODUCTS many to one PRODUCTS: references
    ORDERS one to many PAYMENTS: has
```
- ORDER_ITEMS
  - 한 번에 여러 상품을 주문할 수 있기 떄문에 ORDER_ITEMS에 상품 별 주문을 관리합니다.
  - 가격 변동이 있을 수 있으므로, 주문 시점의 가격을 ORDER_ITEMS에 저장합니다.
- ORDER_ITEMS
  - 한 번에 여러 상품을 주문할 수 있기 떄문에 ORDER_ITEMS에 상품 별 주문을 관리합니다.
  - 가격 변동이 있을 수 있으므로, 주문 시점의 가격을 ORDER_ITEMS에 저장합니다.
- ORDER.STATUS
  - `READY(주문대기)`, `STOCK_ALLOCATED(재고 확보)`, `COMPLETED(주문완료)` 로 구분됩니다
- PAYMENTS
    - 하나의 주문에 여러 번의 결제를 시도할 수 있으므로, ORDERS와 PAYMENTS는 1:N 관계로 설정했습니다.


## 2. 완료된 주문 데이터 전송

### 2.1. 주문 데이터 전송 : 시퀀스 다이어그램

```mermaid
sequenceDiagram
    participant Scheduler
    participant OrderEvent

    Scheduler->>OrderEvent: 10초 간격으로 처리하지 않은 주문 완료 이벤트 조회
    OrderEvent-->>Scheduler: 해당 스케줄러가 미처리한 이벤트 반환
    Scheduler->>OrderDataPlatform: 각 이벤트에 대한 주문 정보 전송
    Scheduler->>OrderEvent: 마지막 처리한 주문 정보 업데이트
```

### 2.2 주문 데이터 전송 : ERD

```mermaid
erDiagram
    ORDER_EVENTS {
        int id PK
        int order_id
        varchar(25) type
        text snapshot
        timestamp created_at
    }
    ORDERS {
      int id PK
      int user_id
      varchar(25) status
      int coupon_id "(nullable)"
      decimal originalAmount
      decimal discountAmount
      decimal totalAmount
      timestamp created_at
      timestamp updated_at
    }

    ORDERS one to many ORDER_EVENTS: has
```
- ORDER_EVENTS
  - 주문의 현재 상태를 snapshot으로 저장합니다.
