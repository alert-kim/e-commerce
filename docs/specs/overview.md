# 이커머스 서비스 설계
- 이커머스 서비스 설계문서를 작성했습니다.
- 문서 특징
  - 시퀀스 다이어그램은 도메인 기반으로 큰 범위에서 작성했습니다.
  - ERD는 전체에 대한 부분은, [전체 erd](erd.md)에 작성했고, 각 기능별로 다시 작성했습니다.
  - 주요 에러에 대해서만 시퀀스 다이어그램에 작성했습니다.
  
## 1. 서비스 개요
- e-커머스 상품 주문 서비스입니다. 상품을 주문하고, 결제하는 것이 핵심 기능이며, 결제 수단은 미리 충전한 잔액을 사용합니다.
- 선착순 쿠폰을 발급받아 결제시 사용할 수 있다는 특징이 있습니다.

## 2. 기능별 
- [전체 erd](erd.md)
- [잔액 충전/조회](charge.md)
- [주문/결제](order-pay.md)
- [상품/인기상품 조회](product.md)
- [상품 판매 통계](popular-products.md)
- [선착순 쿠폰](coupon.md)
