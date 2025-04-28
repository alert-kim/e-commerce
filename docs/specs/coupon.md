# 선착순 쿠폰

## 1. 시퀀스 다이어그램

### 1.1 쿠폰 발급

```mermaid
sequenceDiagram
    participant Server
    participant Coupon
    participant CouponSource

    Server ->> CouponSource: 쿠폰 발급
    break 쿠폰 발급 실패
        Coupon -->> Server: 찾을 수 없는 쿠폰, 쿠폰 재고 부족
    end

    Server ->> Coupon: 쿠폰 생성
```

### 1.2 보유 쿠폰 조회
```mermaid
sequenceDiagram
participant server
participant User
participant Coupon
    server->>User: 유저 검증
    
    server->>Coupon: 유저가 보유한 쿠폰 조회
    Coupon-->>server: 보유 쿠폰 반환
```
- 쿠폰 발급시, 해당 유저 아이디가 실제 있는 유저인지 검증합니다.
- 아직 사용 되지 않은 쿠폰을 반환합니다. 

## 2. ERD
```mermaid 
erDiagram
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
    COUPON_SOURCES {
      int id PK
      varchar(15) name
      deciaml discount_amount
      int quantity
      int initial_quantity
      timestamp created_at
      timestamp updated_at
    }
    COUPONS many to one or zero COUPON_SOURCES: is_associated_with
```
- COUPON_SOURCES
  - 쿠폰 소스에서 재고를 관리합니다. 
- COUPONS 
  - COUPONS 생성시 할인 규칙은 COUPONS에서 중복으로 저장합니다.
