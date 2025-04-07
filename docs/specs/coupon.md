# 선착순 쿠폰

## 1. 시퀀스 다이어그램

### 1.1 쿠폰 발급

```mermaid
sequenceDiagram
    participant Server
    participant UserCoupon
    participant Coupon
    Server ->> UserCoupon: 유저의 해당 쿠폰 발급 여부 확인
    break 중복 쿠폰
        UserCoupon -->> Server: 이미 발급된 쿠폰
    end

    Server ->> Coupon: 쿠폰 차감
    break 쿠폰 재고 없음
        Coupon -->> Server: 쿠폰 발급 실패
    end

    Server ->> UserCoupon: 유저에게 쿠폰 발급
```
- 유저는 쿠폰당 1개씩 밖에 갖지 못한다고 제한했습니다.
- 중복 쿠폰 확인부터 유저에게 쿠폰 발급까지 하나의 트랜잭션으로 묶인다고 가정했습니다.

### 1.2 보유 쿠폰 조회
```mermaid
sequenceDiagram
participant server
participant User
participant UserCoupon
    server->>User: 유저 검증
    
    server->>UserCoupon: 유저가 보유한 쿠폰 조회
    UserCoupon-->>server: 보유 쿠폰 반환
```
- 쿠폰 발급시, 해당 유저 아이디가 실제 있는 유저인지 검증합니다.
- 사용이 가능한 쿠폰을 반환합니다. 

## 2.5.1 ERD
```mermaid 
erDiagram
    USER_COUPONS {
        int id PK
        int user_id FK
        int coupon_id FK
        timestamp used_at "(nullable)"
        timestamp created_at
        timestamp updated_at
    }

    COUPONS {
        int id PK
        varchar(15) name
        varchar type
        int max_discount_amount
        int discount_amount "(nullable)"
        int discount_rate "(nullable)"
        int quantity
        int initial_quantity
        timestamp usable_from
        timestamp usable_to
        timestamp created_at
        timestamp updated_at
        timestamp deleted_at "(nullable)"
    }
    USER_COUPONS many to one COUPONS: is_associated_with
```
- COUPONS
  - 쿠폰은 정액 할인과, 정률 할인을 나타내는 TYPE(`FIXED`와 `PERCENTAGE`)로 구분됩니다.
  - 쿠폰에서 재고를 관리합니다. 
- USER_COUPONS 
  - USER_COUPONS 조회시 할인 규칙은 COUPONS에서 확인합니다
- 쿠폰 사용 제약
  - 쿠폰은 발급된 후, 사용기한(COUPONS.USABLE_FROM ~ USABLE_TO)이 지나면 사용할 수 없습니다.
  - 쿠폰은 최대 금액 범위 내에서 할인이 제공됩니다.
  - 사용 가능한 쿠폰이란, 사용 기한 내에 있으며, USER_COUPONS.USED_AT이 NULL인 쿠폰을 의미합니다.
