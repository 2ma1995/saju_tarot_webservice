# 🔮 사주타로 서비스 API 테스트 가이드

> **서버 실행**: `./gradlew bootRun`  
> **Swagger UI**: http://localhost:8080/swagger-ui.html  
> **Base URL**: http://localhost:8080

---

## 📋 사전 준비: 계정 설정

### 테스트 계정 생성 (DB)
```sql
-- 현재 테스트 계정 확인
SELECT id, email, name, user_role FROM users;

-- 관리자 권한 부여
UPDATE users SET user_role = 'ADMIN' WHERE email = 'admin@test.com';

-- 상담사 권한 부여
UPDATE users SET user_role = 'COUNSELOR' WHERE email = 'counselor@test.com';
```

### Swagger 토큰 등록 방법
1. 아래 로그인 API 실행 → `accessToken` 복사
2. Swagger 우측 상단 **Authorize 🔒** 클릭
3. `accessToken` 값만 붙여넣기 (Bearer 제외)
4. **Authorize** → **Close**

---

## 🟢 케이스 1: 회원가입 & 인증 (토큰 불필요)

### 1-1. 회원가입
```
POST /api/users/signup
Content-Type: application/json

{
  "email": "newuser@test.com",
  "password": "test1234!",
  "name": "테스트유저",
  "nickname": "tester",
  "phone": "010-1234-5678"
}

예상 응답: 201 Created
```

### 1-2. 로그인 (USER)
```
POST /api/auth/login
Content-Type: application/json

{
  "email": "newuser@test.com",
  "password": "test1234!"
}

예상 응답:
{
  "accessToken": "eyJ...",
  "refreshToken": "eyJ...",
  "user": { "id": 1, "role": "USER" }
}
```

### 1-3. 로그인 (ADMIN)
```
POST /api/auth/login

{
  "email": "admin@test.com",
  "password": "test1234!"
}
```

### 1-4. Access Token 재발급
```
POST /api/auth/refresh

{
  "refreshToken": "eyJ..."
}

예상 응답: { "accessToken": "eyJ..." }
```

### 1-5. 로그아웃
```
POST /api/auth/logout
Authorization: Bearer {accessToken}

예상 응답: "로그아웃이 완료되었습니다."
```

### 1-6. 실패 케이스
| 상황 | 요청 | 예상 응답 |
|------|------|-----------|
| 잘못된 비밀번호 | password: "wrong!" | 401 INVALID_PASSWORD |
| 존재하지 않는 이메일 | email: "none@test.com" | 404 NOT_FOUND |
| 중복 이메일 회원가입 | 동일 email 재가입 | 400 DUPLICATE_EMAIL |

---

## 🟡 케이스 2: 상담사 조회 (토큰 불필요)

### 2-1. 상담사 목록 조회
```
GET /api/counselors?sort=rating&page=0&size=10

예상 응답: 활성화된 상담사 목록 (페이징)
```

### 2-2. 특정 상담사 프로필 조회
```
GET /api/counselors/profile/{counselorId}

예상 응답: 상담사 기본 정보 + 프로필
```

### 2-3. 상담사 검색
```
GET /api/counselors/profile/search?keyword=타로

예상 응답: 키워드 포함 상담사 목록
```

---

## 🔵 케이스 3: 예약 플로우 (USER 토큰 필요)

### 3-1. 상담사 스케줄 조회
```
GET /api/schedules/counselor/{counselorId}?date=2026-03-01
Authorization: Bearer {USER_accessToken}

예상 응답: 해당 날짜 가용 스케줄 목록
```

### 3-2. 예약 생성
```
POST /api/reservations
Authorization: Bearer {USER_accessToken}

{
  "counselorId": 5,
  "scheduleId": 1,
  "serviceItemId": 1,
  "note": "타로 상담 요청"
}

예상 응답: 201 Created (예약 정보)
```

### 3-3. 내 예약 목록 조회
```
GET /api/reservations/my
Authorization: Bearer {USER_accessToken}

예상 응답: 내 예약 전체 목록
```

### 3-4. 예약 취소
```
DELETE /api/reservations/{reservationId}
Authorization: Bearer {USER_accessToken}

예상 응답: "예약이 취소되었습니다."
```

### 3-5. 실패 케이스
| 상황 | 예상 응답 |
|------|-----------|
| 이미 예약된 스케줄 | 409 SCHEDULE_ALREADY_RESERVED |
| 존재하지 않는 스케줄 | 404 NOT_FOUND |
| 타인 예약 취소 시도 | 403 ACCESS_DENIED |

---

## 💳 케이스 4: 결제 플로우 (USER 토큰 필요)

### 4-1. 결제 요청
```
POST /api/payments/request
Authorization: Bearer {USER_accessToken}

{
  "reservationId": 1,
  "amount": 50000,
  "method": "CARD"
}

예상 응답: Toss Payments 결제 URL
```

### 4-2. 결제 성공 콜백 (Toss → 서버)
```
GET /api/payments/success?paymentKey=...&orderId=...&amount=50000
```

### 4-3. 내 결제 내역 조회
```
GET /api/payments/my
Authorization: Bearer {USER_accessToken}

예상 응답: 결제 목록 (PAID, REFUND, PENDING)
```

### 4-4. 환불 요청
```
POST /api/payments/{paymentId}/refund
Authorization: Bearer {USER_accessToken}

예상 응답: "환불이 완료되었습니다." + 스케줄 복구
```

### 4-5. 실패 케이스
| 상황 | 예상 응답 |
|------|-----------|
| 이미 환불된 결제 재환불 | 400 ALREADY_REFUNDED |
| 타인 결제 환불 시도 | 403 ACCESS_DENIED |

---

## ⭐ 케이스 5: 리뷰 (USER 토큰 필요)

### 5-1. 리뷰 작성
```
POST /api/reviews
Authorization: Bearer {USER_accessToken}

{
  "counselorId": 5,
  "reservationId": 1,
  "rating": 5,
  "comment": "정말 정확한 상담이었습니다!"
}

예상 응답: 201 Created
```

### 5-2. 상담사 리뷰 목록 조회
```
GET /api/reviews/counselor/{counselorId}

예상 응답: 리뷰 목록
```

### 5-3. 내 리뷰 삭제
```
DELETE /api/reviews/{reviewId}
Authorization: Bearer {USER_accessToken}

예상 응답: "리뷰가 삭제되었습니다."
```

---

## 🔴 케이스 6: 관리자 기능 (ADMIN 토큰 필요)

### 6-1. 전체 사용자 목록 조회
```
GET /api/admin/users?role=USER&active=true&page=0&size=10
Authorization: Bearer {ADMIN_accessToken}

예상 응답: 페이징된 사용자 목록
```

### 6-2. 사용자 역할 변경
```
PUT /api/admin/users/{userId}/role?newRole=COUNSELOR
Authorization: Bearer {ADMIN_accessToken}

예상 응답: "사용자 역할이 변경되었습니다."
```

### 6-3. 사용자 비활성화
```
PUT /api/admin/users/{userId}/deactivate
Authorization: Bearer {ADMIN_accessToken}

예상 응답: "사용자가 비활성화되었습니다."
```

### 6-4. 전체 결제 목록 조회 (필터)
```
GET /api/admin/payments?status=PAID
Authorization: Bearer {ADMIN_accessToken}

예상 응답: 결제 목록
```

### 6-5. 월별 수익 통계
```
GET /api/admin/payments/stats/monthly?year=2026
Authorization: Bearer {ADMIN_accessToken}

예상 응답: 월별 수익 데이터
```

### 6-6. 일별 대시보드 통계
```
GET /api/admin/payments/stats/daily?startDate=2026-01-01&endDate=2026-02-23
Authorization: Bearer {ADMIN_accessToken}

예상 응답: 기간별 통계
```

---

## ⚡ curl 스크립트 (전체 플로우 자동 테스트)

```bash
BASE="http://localhost:8080"

# 1. 로그인 후 토큰 저장
TOKEN=$(curl -s -X POST $BASE/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@test.com","password":"test1234!"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['accessToken'])")
echo "✅ 토큰 발급: $TOKEN"

# 2. 사용자 목록 조회
echo "\n📋 사용자 목록:"
curl -s "$BASE/api/admin/users" \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool

# 3. 상담사 목록 조회
echo "\n👥 상담사 목록:"
curl -s "$BASE/api/counselors" | python3 -m json.tool
```

---

## 🔁 자동 환불 스케줄러 테스트

결제 후 24시간이 지나도 상담이 완료되지 않으면 자동 환불됩니다.  
스케줄러는 매 정시(`0 0 * * * *`)에 실행됩니다.

수동으로 조건 만들기:
```sql
-- paidAt을 25시간 전으로 조작하여 스케줄러 동작 확인
UPDATE payments SET paid_at = DATE_SUB(NOW(), INTERVAL 25 HOUR)
WHERE payment_status = 'PAID';
```
다음 정시가 되면 자동 환불 처리됩니다.
