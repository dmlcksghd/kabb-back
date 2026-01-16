#!/bin/bash

# API 테스트 스크립트
BASE_URL="http://localhost:8080"

echo "=== 회원가입 API 테스트 ==="
echo ""

# 테스트 이미지 파일이 없으므로, 실제 파일을 사용해야 합니다
# 예시: curl -X POST "${BASE_URL}/api/auth/signup" \
#   -F "email=test@example.com" \
#   -F "password=password123" \
#   -F "name=홍길동" \
#   -F "phone=010-1234-5678" \
#   -F "hospitalName=테스트병원" \
#   -F "hospitalAddress=서울시 강남구" \
#   -F "hospitalPhone=02-1234-5678" \
#   -F "licenseFile=@/path/to/license.pdf" \
#   -F "privacyPolicyAgreed=true" \
#   -F "termsOfServiceAgreed=true" \
#   -F "sensitiveInfoAgreed=true"

echo "회원가입 API: POST ${BASE_URL}/api/auth/signup"
echo ""
echo "필수 파라미터:"
echo "  - email: 이메일"
echo "  - password: 비밀번호 (8자 이상)"
echo "  - name: 이름"
echo "  - phone: 휴대폰 번호"
echo "  - hospitalName: 병원명"
echo "  - hospitalAddress: 병원 주소"
echo "  - hospitalPhone: 병원 전화번호"
echo "  - licenseFile: 면허증 파일 (이미지 또는 PDF)"
echo "  - privacyPolicyAgreed: 개인정보처리방침 동의 (true/false)"
echo "  - termsOfServiceAgreed: 서비스 이용약관 동의 (true/false)"
echo "  - sensitiveInfoAgreed: 민감정보 처리 동의 (true/false)"
echo ""

echo "=== 관리자 API 테스트 ==="
echo ""
echo "승인 대기 목록: GET ${BASE_URL}/api/admin/licenses/pending"
echo "면허증 승인: POST ${BASE_URL}/api/admin/licenses/{licenseId}/approve?adminId=1"
echo "면허증 거절: POST ${BASE_URL}/api/admin/licenses/{licenseId}/reject?adminId=1"
echo "  (Body: {\"reason\": \"거절 사유\"})"
echo ""

echo "=== H2 콘솔 ==="
echo "http://localhost:8080/h2-console"
echo "JDBC URL: jdbc:h2:mem:testdb"
echo "Username: sa"
echo "Password: 1234"

