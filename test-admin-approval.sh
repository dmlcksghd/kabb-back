#!/bin/bash

# 관리자 승인 테스트 스크립트
BASE_URL="http://localhost:8080"

echo "=========================================="
echo "관리자 승인 기능 테스트"
echo "=========================================="
echo ""

# 1. 승인 대기 면허증 목록 조회
echo "1. 승인 대기 면허증 목록 조회"
echo "   GET ${BASE_URL}/api/admin/licenses/pending"
echo ""
curl -X GET "${BASE_URL}/api/admin/licenses/pending" \
  -H "Content-Type: application/json" | jq '.'
echo ""
echo ""

# 2. 면허증 승인 (licenseId는 실제 ID로 변경 필요)
echo "2. 면허증 승인 예시"
echo "   POST ${BASE_URL}/api/admin/licenses/1/approve?adminId=1"
echo ""
echo "   실제 사용 시:"
echo "   curl -X POST \"${BASE_URL}/api/admin/licenses/1/approve?adminId=1\" \\"
echo "     -H \"Content-Type: application/json\""
echo ""

# 3. 면허증 거절 예시
echo "3. 면허증 거절 예시"
echo "   POST ${BASE_URL}/api/admin/licenses/1/reject?adminId=1"
echo ""
echo "   실제 사용 시:"
echo "   curl -X POST \"${BASE_URL}/api/admin/licenses/1/reject?adminId=1\" \\"
echo "     -H \"Content-Type: application/json\" \\"
echo "     -d '{\"reason\": \"면허증이 불명확합니다\"}'"
echo ""

echo "=========================================="
echo "테스트 순서:"
echo "1. 먼저 회원가입 API로 사용자 등록"
echo "2. 승인 대기 목록 조회로 licenseId 확인"
echo "3. 승인 또는 거절 처리"
echo "=========================================="

