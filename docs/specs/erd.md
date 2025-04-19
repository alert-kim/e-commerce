```mermaid
erDiagram
    USERS {
        int id PK
        varchar(50) name
        timestamp created_at
    }
    BALANCES {
        int id PK
        int user_id
        decimal amount
        timestamp created_at
        timestamp updated_at
    }
    BALANCE_RECORDS {
        int id PK
        int balance_id
        varchar(10) type
        decimal amount
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
    ORDER_PRODUCTS {
        int order_id FK
        int product_id
        int quantity
        decimal unit_price
        decimal total_price
        timestamp created_at
    }
    ORDER_EVENTS {
        int id PK
        int order_id
        varchar(25) type
        text snapshot
        timestamp created_at
    }

    PRODUCTS {
        int id PK
        varchar status(15)
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
    PRODUCT_DAILY_SALES {
        int product_id
        date date
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
    COUPON_SOURCES {
        int id PK
        varchar(15) name
        deciaml discount_amount
        int quantity
        int initial_quantity
        timestamp created_at
        timestamp updated_at
    }

    PRODUCT_DAILY_SALES {
        int product_id FK
        int quantity
        date date
        timestamp created_at
        timestamp updated_at
    }
    
    USERS one to one or zero BALANCES: has
    USERS one to many ORDERS: has
    USERS one to many PAYMENTS: has
    USERS one to many COUPONS: has
    
    BALANCES one to many BALANCE_RECORDS: has
    
    ORDERS one to many ORDER_PRODUCTS: includes
    ORDER_PRODUCTS many to one PRODUCTS: references
    ORDERS one to many ORDER_EVENTS: has

    PRODUCTS one to one PRODUCT_STOCKS: has_stock
    PRODUCTS one to many PRODUCT_DAILY_SALES: has

    ORDERS one to many PAYMENTS: has
    COUPONS many to one COUPON_SOURCES: is_associated_with

    PRODUCT_DAILY_SALES many to one PRODUCTS: is_associated_with
```
