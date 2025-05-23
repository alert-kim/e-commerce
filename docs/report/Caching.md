# 캐싱 전략 설계 및 적용 보고서

## 개요
캐싱 대상을 선정하고 적절한 캐싱 전략을 설계 및 적용하여 성능을 개선한다.

---

## 1. 캐시 저장소: Redis

- 외부 캐시 저장소인 **Redis**를 사용하여 캐싱을 진행한다.
- 외부 캐시이므로 In-memory Cache와는 다르게 **추가적인 네트워크 비용**이 발생하지만, **데이터 일관성 유지**가 가능하다.
  - In-memory Cache는 서버 별로 캐시가 존재하여 분산 환경에서 **데이터 불일치**가 발생할 수 있다.
  - Redis는 외부에 위치해 여러 서버에서 **동일한 캐시 데이터**를 조회할 수 있다.
- 향후 필요 시 **짧은 TTL을 가진 In-memory Cache**와의 혼합 전략도 고려할 수 있다.

---

## 2. 캐싱 대상

### 2.1 인기 상품 조회

- 인기 상품은 대다수 사용자가 자주 조회하지만, **변경 빈도는 낮다**.
  - 일간 판매 집계를 통해 **스케줄러가 하루 1회** 데이터를 업데이트한다.
- 조회 시 **3번의 DB 조회**가 발생한다.
  - 인기 상품 ID 목록 조회
  - 각 ID에 대한 상품 정보 조회
  - 상품의 재고 정보 조회

### 2.2 유저 조회

- 유저 정보는 **변경 기능이 없으며**, 주로 **유저 검증 용도**로 사용된다.
- 여러 로직에서 사용되며, **반복 조회 가능성이 높아 캐시 적중률이 높음**이 예상된다.

---

## 3. 캐싱 전략

### 3.1 공통 전략: Look-Aside Cache (Lazy Loading)

- **동작 방식**
  - 어플리케이션이 조회 시 캐시를 먼저 확인 → 존재하면 그대로 사용 (cache hit)
  - 존재하지 않으면 **어플리케이션이 DB를 조회**하고, **조회 결과를 캐시에 저장** (cache miss)
- **장점**
  - 요청된 데이터만 캐시되어 메모리 효율이 높음
  - 캐시 로직 구현이 단순함
- **단점**
  - 캐시 미스 시 DB 조회로 인해 지연 발생 가능
- **적용 대상**
  - 유저, 상품 정보 등 **키 수가 많은 데이터**에 적합

### 3.2 인기 상품 캐시

- **캐시 단위 분리**
  - 인기 상품 캐시 / 상품 캐시 / 재고 캐시를 **도메인 별로 분리**
  - 이유: 각 도메인의 변경 주기가 다르며, 함께 캐싱 시 **evict 누락 발생 가능**
- **Evict 전략**
  - 인기 상품 ID 목록은 **하루 1회 스케줄러로 갱신**
  - 갱신 시점에 **캐시 삭제(evict)** 수행
  - 예외 상황 대비 **TTL(Time-To-Live) 24시간 설정**
- **첫 조회 지연 대응**
  - Lazy Loading 특성상 캐시 삭제 직후 DB 조회 발생
  - 삭제 직후 조회 로직을 실행하여 **초기 캐시 적재**

```kotlin
@Scheduled(cron = "0 10 0 * * *")
fun aggregateDaily() {
productFacade.aggregate(...) // DB 집계 & 캐시 evict
productFacade.getPopularProducts() // evict 직후 조회 → 캐시 생성
}
```

### 3.3 유저 캐시

- 캐시 TTL을 **1시간**으로 설정
- 사용자 활동 시간 동안 **DB 부하를 줄이는 데 효과적**

---

## 4. 캐싱 적용 결과

### 4.1 유저 캐시

**테스트 환경**
- 동시 사용자 수: 50
- 사용자 당 요청 수: 20

| 항목 | 캐시 적용 전 | 캐시 적용 후 |
|------|---------------|----------------|
| 총 소요 시간 | 494 ms | 447 ms |
| 초당 요청 처리량 | 2024.29 req/s | 2237.14 req/s |
| 평균 응답 시간 | 23.74 ms | 21.58 ms |
| 중간값 응답 시간 | 21.00 ms | 20.00 ms |
| 95% 백분위 응답 시간 | 71.00 ms | 39.00 ms |
| 99% 백분위 응답 시간 | 80.00 ms | 48.00 ms |

**성능 개선 요약**
- 총 소요 시간 감소: **9.51%**
- 초당 요청 처리량 증가: **10.51%**
- 평균 응답 시간 감소: **9.11%**
- 중간값 응답 시간 감소: **4.76%**
- 95 백분위 응답 시간 감소: 45.07%
- 99 백분위 응답 시간 감소: 40.00%

### 4.2 인기 상품 캐시

| 항목 | 캐시 적용 전 | 캐시 적용 후 |
|------|---------------|----------------|
| 총 소요 시간 | 4201 ms | 3283 ms |
| 초당 요청 처리량 | 238.04 req/s | 304.60 req/s |
| 평균 응답 시간 | 200.90 ms | 154.83 ms |
| 중간값 응답 시간 | 187.00 ms | 161.00 ms |
| 95% 백분위 응답 시간 | 434.00 ms | 252.00 ms |
| 99% 백분위 응답 시간 | 562.00 ms | 333.00 ms |

**성능 개선 요약**
- 총 소요 시간 감소: **21.85%**
- 초당 요청 처리량 증가: **27.96%**
- 평균 응답 시간 감소: **22.93%**
- 중간값 응답 시간 감소: **13.90%**
- 95% 응답 시간 감소: **41.94%**
- 99% 응답 시간 감소: **40.75%**

---

## 5. 결과 분석
- **전반적인 성능 수치 향상**: 전반적으로 소요시간이 감소하고, 처리량이 증가함 
  - 단, 유저 캐시(잔고 조회)는 캐시를 요청당 1회만 사용하고, 유저마다 조회하는 데이터가 다르므로 캐시 적중률이 낮아 성능 개선 폭이 적음.
- **테스트 조건 한계**: 단순 로직 실행 측정 시간 테스트로, 실제 서비스 환경과는 차이가 있음. 하지만 캐시를 활용하여 성능이 개선된 것은 확인함.


