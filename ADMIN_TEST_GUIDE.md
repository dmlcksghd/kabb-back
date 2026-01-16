# 관리자 승인 기능 테스트 가이드

## 📋 테스트 순서

### 1단계: 회원가입 (면허증 제출)

먼저 회원가입을 통해 사용자와 면허증을 생성해야 합니다.

**Postman 또는 curl 사용:**

```bash
curl -X POST "http://localhost:8080/api/auth/signup" \
  -F "email=doctor@example.com" \
  -F "password=password123" \
  -F "name=홍길동" \
  -F "phone=010-1234-5678" \
  -F "hospitalName=테스트병원" \
  -F "hospitalAddress=서울시 강남구 테스트로 123" \
  -F "hospitalPhone=02-1234-5678" \
  -F "licenseFile=@/path/to/license.pdf" \
  -F "privacyPolicyAgreed=true" \
  -F "termsOfServiceAgreed=true" \
  -F "sensitiveInfoAgreed=true"
```

**응답 예시:**
```json
{
  "success": true,
  "message": "회원가입이 완료되었습니다",
  "data": {
    "userId": 1,
    "email": "doctor@example.com",
    "name": "홍길동",
    "approvalStatus": "PENDING",
    "message": "회원가입이 완료되었습니다. 관리자 승인 후 서비스를 이용하실 수 있습니다.",
    "createdAt": "2025-11-30T20:11:37"
  },
  "timestamp": "2025-11-30T20:11:37"
}
```

### 2단계: 승인 대기 목록 조회

관리자가 승인 대기 중인 면허증 목록을 확인합니다.

```bash
curl -X GET "http://localhost:8080/api/admin/licenses/pending" \
  -H "Content-Type: application/json"
```

**응답 예시:**
```json
{
  "success": true,
  "data": [
    {
      "licenseId": 1,
      "userId": 1,
      "userName": "홍길동",
      "hospitalName": "테스트병원",
      "approvalStatus": "PENDING",
      "rejectionReason": null,
      "approvedAt": null
    }
  ],
  "timestamp": "2025-11-30T20:11:37"
}
```

### 3단계: 면허증 승인

관리자가 면허증을 승인합니다.

```bash
curl -X POST "http://localhost:8080/api/admin/licenses/1/approve?adminId=1" \
  -H "Content-Type: application/json"
```

**응답 예시:**
```json
{
  "success": true,
  "message": "면허증이 승인되었습니다",
  "data": {
    "licenseId": 1,
    "userId": 1,
    "userName": "홍길동",
    "hospitalName": "테스트병원",
    "approvalStatus": "APPROVED",
    "rejectionReason": null,
    "approvedAt": "2025-11-30T20:15:00"
  },
  "timestamp": "2025-11-30T20:15:00"
}
```

### 4단계: 면허증 거절 (선택사항)

면허증이 부적합한 경우 거절할 수 있습니다.

```bash
curl -X POST "http://localhost:8080/api/admin/licenses/1/reject?adminId=1" \
  -H "Content-Type: application/json" \
  -d '{"reason": "면허증이 불명확하거나 만료되었습니다"}'
```

**응답 예시:**
```json
{
  "success": true,
  "message": "면허증이 거절되었습니다",
  "data": {
    "licenseId": 1,
    "userId": 1,
    "userName": "홍길동",
    "hospitalName": "테스트병원",
    "approvalStatus": "REJECTED",
    "rejectionReason": "면허증이 불명확하거나 만료되었습니다",
    "approvedAt": "2025-11-30T20:15:00"
  },
  "timestamp": "2025-11-30T20:15:00"
}
```

## 🔍 H2 데이터베이스에서 확인

1. 브라우저에서 `http://localhost:8080/h2-console` 접속
2. 연결 정보 입력:
   - JDBC URL: `jdbc:h2:mem:testdb`
   - Username: `sa`
   - Password: `1234`
3. 다음 쿼리로 데이터 확인:

```sql
-- 승인 대기 사용자 확인
SELECT u.id, u.email, u.name, u.approval_status, l.id as license_id
FROM users u
LEFT JOIN licenses l ON u.id = l.user_id
WHERE u.approval_status = 'PENDING';

-- 면허증 정보 확인
SELECT * FROM licenses;

-- 동의 기록 확인
SELECT * FROM agreements;

-- 감사 로그 확인
SELECT * FROM audit_logs ORDER BY created_at DESC;
```

## 📝 Postman Collection 예시

Postman에서 사용할 수 있는 요청 예시:

### 1. 회원가입
- **Method**: POST
- **URL**: `http://localhost:8080/api/auth/signup`
- **Body**: form-data
  - email: `test@example.com`
  - password: `password123`
  - name: `홍길동`
  - phone: `010-1234-5678`
  - hospitalName: `테스트병원`
  - hospitalAddress: `서울시 강남구`
  - hospitalPhone: `02-1234-5678`
  - licenseFile: `[파일 선택]`
  - privacyPolicyAgreed: `true`
  - termsOfServiceAgreed: `true`
  - sensitiveInfoAgreed: `true`

### 2. 승인 대기 목록
- **Method**: GET
- **URL**: `http://localhost:8080/api/admin/licenses/pending`

### 3. 면허증 승인
- **Method**: POST
- **URL**: `http://localhost:8080/api/admin/licenses/{licenseId}/approve?adminId=1`
- **Path Variable**: `licenseId` (예: 1)

### 4. 면허증 거절
- **Method**: POST
- **URL**: `http://localhost:8080/api/admin/licenses/{licenseId}/reject?adminId=1`
- **Body**: raw JSON
  ```json
  {
    "reason": "면허증이 불명확합니다"
  }
  ```

## ⚠️ 주의사항

1. **포트 충돌**: 포트 8080이 이미 사용 중이면 다른 포트로 변경하거나 기존 프로세스를 종료하세요.
2. **파일 경로**: 면허증 파일은 실제 파일 경로를 사용해야 합니다.
3. **licenseId**: 승인/거절 시 실제 licenseId를 사용해야 합니다. 먼저 목록 조회로 확인하세요.
4. **Security 설정**: 현재는 테스트를 위해 관리자 API 접근이 허용되어 있습니다. 운영 환경에서는 JWT 인증을 추가해야 합니다.

