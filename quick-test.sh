#!/bin/bash

# 빠른 테스트 스크립트
BASE_URL="http://localhost:8081"

echo "=========================================="
echo "관리자 승인 기능 빠른 테스트"
echo "=========================================="
echo ""

# 1. 테스트 사용자 생성
echo "1. 테스트 사용자 생성 중..."
RESPONSE=$(curl -s -X POST "${BASE_URL}/api/test/create-test-user")
echo "$RESPONSE"
echo ""

# licenseId 추출 (간단한 방법)
LICENSE_ID=$(curl -s -X GET "${BASE_URL}/api/admin/licenses/pending" | grep -o '"licenseId":[0-9]*' | head -1 | grep -o '[0-9]*')

if [ -z "$LICENSE_ID" ]; then
    echo "면허증 ID를 찾을 수 없습니다. 다시 시도해주세요."
    exit 1
fi

echo "생성된 License ID: $LICENSE_ID"
echo ""

# 2. 승인 대기 목록 조회
echo "2. 승인 대기 목록 조회:"
curl -X GET "${BASE_URL}/api/admin/licenses/pending" \
  -H "Content-Type: application/json" | python3 -m json.tool
echo ""
echo ""

# 3. 면허증 승인
echo "3. 면허증 승인 (License ID: $LICENSE_ID):"
curl -X POST "${BASE_URL}/api/admin/licenses/${LICENSE_ID}/approve?adminId=1" \
  -H "Content-Type: application/json" | python3 -m json.tool
echo ""
echo ""

# 4. 승인 후 상태 확인
echo "4. 승인 후 목록 확인:"
curl -X GET "${BASE_URL}/api/admin/licenses/pending" \
  -H "Content-Type: application/json" | python3 -m json.tool
echo ""

echo "=========================================="
echo "테스트 완료!"
echo "=========================================="

