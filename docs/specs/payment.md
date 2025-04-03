# 결제

결제를 진행하고, 유저는 주문 상품에 대한 소유권을 얻습니다.

## 1. 시퀀스 다이어그램

```mermaid
sequenceDiagram
    participant server
    participant Order
    participant Product
    participant Payment
    participant UserCoupon
    participant UserBalance
    participant UserItem
    participant DataPlatform
    server ->> Order: 결제 요청
    Order ->> Product: 상품 재고 차감
    break 재고 부족
        Product -->> Order: 상품 확보 실패
        Order -->> server: 주문 실패
    end

    Order ->> Payment: 결제
    Payment ->> UserCoupon: 쿠폰 사용
    Payment ->> UserBalance: 최종 결제 금액 차감
    break 잔고 부족
        UserBalance -->> Payment: 잔고부족
        alt 쿠폰 사용
            Payment -->> UserCoupon: 쿠폰 사용 취소
        end
        Payment -->> Order: 결제 실패
        Order ->> Product: 재고 복구
        Order -->> server: 주문 실패
    end
    Order ->> UserItem: 구매한 상품에 대한 소유권 부여
    Order -) DataPlatform: 성공한 결제 건에 대한 데이터 전달
```

- 재고 차감 전, 주문 검증을 진행합니다.
    - 요청으로 받은 유저 Id가 해당 주문의 소유주인지 확인합니다.
    - 주문의 결제 만료 시각을 지나지 않았는지, `READY` 상태인지 확인합니다.
- 재고를 먼저 차감한 후, 결제를 진행합니다.
    - 결제 성공 후 재고 부족으로 환불 해야하는 상황을 막기 위함입니다.
- 결제 실패시 대응
    - 결제 실패시, 쿠폰을 사용했다면 쿠폰 사용을 취소합니다.
    - 결제 실패시, 상품 재고를 복구합니다.
- 요구 사항에 따라, 외부 데이터 플랫폼으로 성공한 결제 건에 대한 데이터를 비동기로 전송합니다.

## 2. ERD

```mermaid
erDiagram
    ORDERS {
        int id PK
        int user_id FK
        varchar(20) status
        decimal total_price
        timestamp expires_at "(nullable)"
        timestamp created_at
        timestamp updated_at
    }
    PAYMENTS {
        int id PK
        int user_id FK
        int order_id FK
        int user_coupon_id FK "(nullable)"
        varchar(5) status
        decimal original_amount
        decimal discount_amount
        decimal final_amount
        timestamp created_at
        timestamp updated_at
    }
    USER_COUPONS {
        int id PK
        int user_id FK
        int coupon_id FK
        timestamp used_at "(nullable)"
        timestamp created_at
        timestamp updated_at
    }
    USER_ITEMS {
        int id PK
        int user_id FK
        int order_id FK
        int product_id FK
        timestamp created_at
        timestamp updated_at
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

    ORDERS one to many PAYMENTS: has
    PAYMENTS one to one USER_COUPONS: uses
    USER_ITEMS many to one ORDERS: is_associated_with
    USER_ITEMS many to one PRODUCTS: references
```

- PAYMENTS
    - 하나의 주문에 여러 번의 결제를 시도할 수 있으므로, ORDERS와 PAYMENTS는 1:N 관계로 설정했습니다.
- 쿠폰 사용
    - 결제 한 건에, 쿠폰 한 건만 사용할 수 있도록 1:1 관계로 설정했습니다.
- ORDERS.STATUS
    - 재고 확보시 `STOCK_ALLOCATE`가 되며, 실패시 `STOCK_ALLOCATION_FAIL`가 됩니다.
    - 결제 성공시 ORDERS.STATUS는 `PAID`가 되며, 실패시 `PAY_FAILED`가 됩니다.
    - 유저에게 전달 성공시 `DELIVERED`가 되며, 실패시 `DELIVERY_FAIL`가 됩니다.
    - 최종 결제가 완료되면 `COMPLETED`가 됩니다.
- USER_ITEMS
    - 구매 완료 후, 유저에게 상품을 주는 것을 간단하게 USER_ITEM을 생성하는 것으로 작성했습니다.
    - 소유권이고 낱개로 관리해야 한다 생각해 상품 한 개당 하나의 row를 가지도록 설계했습니다.
        - ex) A라는 상품을 3개 구매하면, USER_ITEMS에 3개의 row가 생성됩니다.
