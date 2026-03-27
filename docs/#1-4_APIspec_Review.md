# API Specification - Review Module

> 도서 이커머스 플랫폼 API 명세서
> Review API (/review/*)

---

## 📌 Review API

| 기능 | Method | Endpoint | 설명 |
|------|--------|----------|------|
| 별점 등록 | POST | /review | 주문 상품에 별점(1~5) 등록 |
| 내 별점 수정 | PATCH | /review/{reviewId} | 별점 수정 |
| 내 별점 삭제 | DELETE | /review/{reviewId} | 별점 삭제 |
| 상품별 별점 조회 | GET | /review/product/{productId} | 상품의 별점 목록 조회 |

---
