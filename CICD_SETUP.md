# CI/CD 설정 가이드

## 📋 개요
이 프로젝트는 GitHub Actions를 사용하여 자동화된 CI/CD 파이프라인을 구성합니다.

## 🚀 빠른 시작

### 1. GitHub 저장소에 코드 푸시
```bash
git add .
git commit -m "Add CI/CD pipeline"
git push origin main
```

### 2. GitHub Actions 자동 실행
코드를 push하면 자동으로 다음이 실행됩니다:
- ✅ 코드 빌드
- ✅ 테스트 실행
- ✅ 빌드 아티팩트 생성

## 📁 워크플로우 파일 구조

```
.github/
└── workflows/
    ├── ci.yml              # CI 파이프라인 (빌드 및 테스트)
    ├── cd.yml              # CD 파이프라인 (배포)
    └── code-quality.yml    # 코드 품질 검사
```

## 🔧 워크플로우 설명

### CI (Continuous Integration)
**파일**: `.github/workflows/ci.yml`

**트리거**:
- `main`, `develop` 브랜치에 push
- `main`, `develop` 브랜치로 PR 생성

**작업**:
1. 코드 체크아웃
2. Java 17 설정
3. Gradle 빌드
4. 테스트 실행
5. 테스트 결과 및 JAR 파일 업로드

### CD (Continuous Deployment)
**파일**: `.github/workflows/cd.yml`

**트리거**:
- `main` 브랜치에 push
- `v*` 태그 생성 (예: `v1.0.0`)
- 수동 실행 (workflow_dispatch)

**작업**:
1. 코드 체크아웃
2. Java 17 설정
3. 빌드 (테스트 제외)
4. 릴리스 아티팩트 생성
5. Docker 이미지 빌드 및 푸시 (선택사항)

### Code Quality
**파일**: `.github/workflows/code-quality.yml`

**트리거**:
- `main`, `develop` 브랜치에 push
- `main`, `develop` 브랜치로 PR 생성

**작업**:
- 코드 포맷팅 검사
- 정적 분석 (선택사항)

## 🐳 Docker 배포 설정

### 1. Dockerfile 확인
프로젝트 루트에 `Dockerfile`이 있습니다.

### 2. Docker Hub 설정
1. GitHub Secrets에 추가:
   - `DOCKER_USERNAME`: Docker Hub 사용자명
   - `DOCKER_PASSWORD`: Docker Hub 비밀번호

2. `cd.yml` 파일 수정:
```yaml
- name: Set up Docker Buildx
  uses: docker/setup-buildx-action@v3
  if: ${{ true }}  # false를 true로 변경

- name: Login to Docker Hub
  uses: docker/login-action@v3
  with:
    username: ${{ secrets.DOCKER_USERNAME }}
    password: ${{ secrets.DOCKER_PASSWORD }}
  if: ${{ true }}  # false를 true로 변경

- name: Build and push Docker image
  uses: docker/build-push-action@v5
  with:
    context: .
    push: true
    tags: |
      your-dockerhub-username/kabb:latest
      your-dockerhub-username/kabb:${{ github.sha }}
  if: ${{ true }}  # false를 true로 변경
```

3. `your-dockerhub-username`을 실제 Docker Hub 사용자명으로 변경

### 3. Docker 이미지 빌드 및 실행 (로컬 테스트)
```bash
# 이미지 빌드
docker build -t kabb:latest .

# 컨테이너 실행
docker run -p 8080:8080 kabb:latest
```

## 🔐 GitHub Secrets 설정

GitHub 저장소의 Settings → Secrets and variables → Actions에서 다음을 추가할 수 있습니다:

### Docker 배포용
- `DOCKER_USERNAME`: Docker Hub 사용자명
- `DOCKER_PASSWORD`: Docker Hub 비밀번호

### 서버 배포용 (선택사항)
- `SSH_PRIVATE_KEY`: 서버 SSH 개인키
- `SERVER_HOST`: 서버 호스트 주소
- `SERVER_USER`: 서버 사용자명

## 📊 워크플로우 상태 확인

1. GitHub 저장소로 이동
2. "Actions" 탭 클릭
3. 실행 중인 워크플로우 확인
4. 각 단계의 로그 확인 가능

## 🛠️ 로컬에서 테스트

### CI 워크플로우 테스트
```bash
# 빌드 및 테스트
./gradlew build
./gradlew test
```

### Docker 빌드 테스트
```bash
docker build -t kabb:test .
docker run -p 8080:8080 kabb:test
```

## 📝 커스터마이징

### 다른 브랜치 사용
`ci.yml`과 `cd.yml`의 `branches` 부분을 수정:
```yaml
on:
  push:
    branches: [ main, develop, release/* ]
```

### 다른 Java 버전 사용
`setup-java` 액션의 `java-version` 수정:
```yaml
- name: Set up JDK 17
  uses: actions/setup-java@v4
  with:
    java-version: '21'  # 원하는 버전으로 변경
```

### 배포 환경 추가
`cd.yml`에 배포 스텝 추가:
```yaml
- name: Deploy to production
  run: |
    # 배포 스크립트 작성
    echo "Deploying to production..."
```

## ⚠️ 주의사항

1. **테스트 데이터 API**: 운영 환경에서는 `/api/test/**` 엔드포인트를 제거하세요.
2. **Security 설정**: 운영 환경에서는 적절한 인증/인가를 설정하세요.
3. **환경 변수**: 민감한 정보는 GitHub Secrets를 사용하세요.
4. **빌드 캐시**: Gradle 캐시를 사용하여 빌드 시간을 단축합니다.

## 🔗 참고 자료

- [GitHub Actions 문서](https://docs.github.com/en/actions)
- [Gradle 문서](https://docs.gradle.org/)
- [Docker 문서](https://docs.docker.com/)

