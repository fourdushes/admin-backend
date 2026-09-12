# HearO Admin Backend

제작자: 양준형

HearO 관리자 전용 Spring Boot API입니다. 관리자 Frontend는 별도 저장소에서 배포하며, 이 저장소는 관리자 Backend 단일 Deployment만 관리합니다. 관리자 Backend에는 canary를 사용하지 않습니다.

## 현재 구조

- Spring Boot 4.0.6
- Java 21
- Gradle Wrapper
- Spring MVC, Spring Data JPA, QueryDSL, Redis
- Spring Security + 관리자 JWT
- MySQL: 기존 HearO RDS 재사용
- Redis: 기존 `redis` Service 재사용
- 내부 포트: `8083`
- Kubernetes namespace: `hearo`
- Service: `ClusterIP`
- CD: Argo CD 수동 Sync

기존 관리자 코드가 메인 Backend의 엔티티와 Repository를 직접 참조하고 있었기 때문에, 독립 빌드에 필요한 최소 도메인 매핑을 이 저장소에 포함했습니다. 테이블 이름과 컬럼은 메인 Backend의 최신 구조와 동일하게 유지해야 합니다. 운영에서는 `ddl-auto=validate`를 사용하므로 이 애플리케이션이 임의로 RDS 스키마를 변경하지 않습니다.

## API 경로

Controller의 기존 `/admin/**` 경로는 변경하지 않습니다. Traefik이 외부 `/api` prefix만 제거합니다.

```text
외부: https://admin.hearo-hearo.com/api/admin/find-ward-user
                         │
                         └─ StripPrefix(/api)
내부: http://hearo-admin-backend:8083/admin/find-ward-user
```

관리자 Frontend는 기존 `/admin/**` 호출을 `/api/admin/**`로 변경해야 합니다. `PathPrefix(/api)` route의 priority는 `200`으로 설정되어 Frontend의 `/` catch-all route보다 먼저 처리됩니다.

관리자 Frontend 저장소가 담당할 항목은 다음과 같습니다.

- `admin.hearo-hearo.com/` → OAuth → Admin Frontend
- Frontend catch-all route는 API route보다 낮은 priority 사용
- OAuth middleware는 Frontend route에만 적용

이 저장소가 담당하는 API route에는 OAuth middleware를 적용하지 않습니다. API 자체는 관리자 JWT로 보호합니다.

## 관리자 인증

로그인을 제외한 `/admin/**` 요청은 다음 헤더가 필요합니다.

```http
Authorization: Bearer <ADMIN_JWT>
```

Spring Security가 JWT 서명, 만료, `tokenType=ACCESS`, `role=ADMIN`을 검사한 뒤 `ROLE_ADMIN` 권한을 부여합니다. 일반 사용자나 기관 JWT는 유효한 토큰이어도 관리자 API를 호출할 수 없습니다.

- 로그인: `POST /api/admin/login`
- 공개 health: `GET /api/actuator/health`
- 그 외 `/api/admin/**`: `ROLE_ADMIN` 필수
- 그 외 내부 경로: 기본 거부

관리자 access token 기본 만료는 2시간, refresh token 기본 만료는 12시간입니다. 환경변수로 변경할 수 있습니다.

## RDS와 Redis

새 RDS를 만들지 않습니다. Deployment는 기존 namespace의 `hearo-backend-secret`에서 다음 key를 읽습니다.

```text
db-url
db-username
db-password
jwt-secret
```

먼저 기존 Secret을 확인합니다. 실제 값은 출력하거나 Git에 저장하지 마십시오.

```bash
sudo kubectl get secret hearo-backend-secret -n hearo
```

Redis는 기존 `redis.hearo.svc.cluster.local:6379`를 사용합니다. 관리자 refresh token은 기존 구현대로 Redis에 `refresh-token:admin:<admin-id>` 형식으로 저장됩니다.

## 설정과 profile

| 환경변수 | 용도 | 운영 설정 |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Spring profile | `prod` |
| `SERVER_PORT` | 내부 HTTP port | `8083` |
| `DB_URL` | 기존 RDS JDBC URL | Secret |
| `DB_USERNAME` | RDS 사용자 | Secret |
| `DB_PASSWORD` | RDS 비밀번호 | Secret |
| `JWT_SECRET` | 관리자 JWT 서명 key | Secret |
| `JWT_ADMIN_VALIDITY` | access token ms | 기본 7200000 |
| `JWT_ADMIN_REFRESH_VALIDITY` | refresh token ms | 기본 43200000 |
| `REDIS_HOST` | Redis Service | `redis` |
| `REDIS_PORT` | Redis port | `6379` |
| `CORS_ALLOWED_ORIGINS` | 필요할 때만 허용할 origin | `https://admin.hearo-hearo.com` |
| `JPA_DDL_AUTO` | Hibernate schema mode | `validate` |

Frontend와 API는 동일 origin이므로 운영 브라우저 요청에는 별도 CORS가 필요하지 않습니다. 로컬 개발이 필요할 때만 `CORS_ALLOWED_ORIGINS=http://localhost:8082`처럼 정확한 origin을 지정합니다. wildcard `*`는 사용하지 않습니다.

## 로컬 빌드와 실행

Java 21이 필요합니다.

```bash
./gradlew clean test bootJar
```

로컬 MySQL, Redis와 환경변수를 준비한 뒤 실행합니다.

```bash
export JWT_SECRET='로컬에서만 사용할 32바이트 이상의 키'
export DB_URL='jdbc:mysql://127.0.0.1:3306/hearO'
export DB_USERNAME='root'
export DB_PASSWORD='로컬 DB 비밀번호'
./gradlew bootRun
```

IDE에서는 `src` 폴더만 열지 말고 이 저장소의 `build.gradle`이 있는 `admin-backend` 최상위 폴더를 Gradle 프로젝트로 열어야 합니다. JDK 21을 선택한 뒤 Gradle Reload를 실행하십시오. QueryDSL `Q` 클래스는 `compileJava` 때 `build/generated/sources/annotationProcessor/java/main` 아래에 자동 생성되며 Git에는 올리지 않습니다.

## Docker

t4g.medium에 맞춰 ARM64 이미지를 빌드합니다.

```bash
docker buildx build --platform linux/arm64 -t hearo-admin-backend:local --load .
```

컨테이너는 non-root UID `1001`로 실행됩니다. Kubernetes에서는 JVM을 `-Xms128m -Xmx512m`로 제한합니다.

## ECR과 GitHub Actions 사전 설정

ECR repository가 없다면 한 번만 생성합니다.

```bash
aws ecr create-repository \
  --repository-name hearo-admin-backend \
  --image-tag-mutability IMMUTABLE \
  --image-scanning-configuration scanOnPush=true \
  --region ap-northeast-2
```

기존 GitHub Actions OIDC role을 재사용한다면 신뢰 정책의 기존 `sub` 조건을 지우지 말고 다음 값을 추가합니다.

```text
repo:fourdushes/admin-backend:ref:refs/heads/main
```

기존 서비스와 동일하게 IAM 정책은 AWS에서 관리합니다. 이 저장소에 IAM 정책 JSON을 복제해 관리하지 않습니다.

- GitHub Actions role에 연결된 기존 ECR push 고객 관리형 정책에 `hearo-admin-backend` repository ARN을 추가합니다.
- Web EC2의 `HearoWebEc2Role`에 연결된 기존 ECR pull 정책에 같은 repository ARN을 추가합니다.
- 기존 Backend, Frontend, Model repository ARN과 권한은 삭제하지 않습니다.

```text
arn:aws:ecr:ap-northeast-2:<AWS 계정 ID>:repository/hearo-admin-backend
```

GitHub repository 설정:

```text
Actions variable: AWS_REGION=ap-northeast-2
Actions variable: AWS_ACCOUNT_ID=<AWS 계정 ID>
Actions secret:   AWS_ROLE_ARN=<GitHub Actions OIDC role ARN>
```

CI/CD 흐름:

```text
main push
  → Gradle test + bootJar
  → linux/arm64 Docker build
  → ECR hearo-admin-backend:<git-sha> push
  → k8s/hearo-admin-backend.yaml image tag commit
  → Argo CD OutOfSync
  → 관리자가 Sync 승인
  → 단일 Admin Backend Deployment 갱신
```

## DNS와 TLS

Route 53에서 다음 A record를 생성합니다.

```text
admin.hearo-hearo.com → Web EC2의 Elastic IP
```

`k8s/hearo-admin-route.yaml`의 cert-manager Certificate가 `hearo-admin-tls` Secret을 생성합니다. 같은 Certificate를 Admin Frontend 저장소에서 중복 생성하지 말고, Frontend IngressRoute에서는 이 TLS Secret만 참조하십시오.

## Argo CD 등록과 배포

GitHub Actions가 첫 정상 이미지를 push하고 manifest의 `bootstrap` 태그를 실제 SHA로 바꾼 뒤 Web EC2에서 실행합니다.

```bash
git clone https://github.com/fourdushes/admin-backend.git
cd admin-backend
sudo ./deploy/argocd/install.sh
sudo kubectl get application hearo-admin-backend -n argocd
```

Argo CD 자동 Sync는 설정하지 않았습니다. Argo CD 화면에서 변경 내용을 확인하고 `hearo-admin-backend`의 `SYNC`를 직접 눌러야 배포됩니다.

배포 확인:

```bash
sudo kubectl get application hearo-admin-backend -n argocd
sudo kubectl get deployment,pod,service -n hearo -l app=hearo-admin-backend
sudo kubectl rollout status deployment/hearo-admin-backend -n hearo --timeout=180s
```

## Health와 테스트

Actuator는 health만 노출하며 상세 정보는 숨깁니다.

```bash
curl -fsS https://admin.hearo-hearo.com/api/actuator/health
```

인증 테스트:

```bash
# 401이어야 함
curl -i https://admin.hearo-hearo.com/api/admin/find-care

# 관리자 access token으로 호출
curl -i \
  -H 'Authorization: Bearer <ADMIN_JWT>' \
  'https://admin.hearo-hearo.com/api/admin/find-care?page=0&size=10'
```

로그 확인:

```bash
sudo kubectl logs -n hearo deployment/hearo-admin-backend --since=10m --timestamps
sudo kubectl describe pod -n hearo -l app=hearo-admin-backend
```

## 리소스와 기존 서비스 영향

Admin Backend는 단일 Pod이며 canary가 없습니다.

```text
request: 100m CPU / 256Mi
limit:   500m CPU / 768Mi
JVM:     Xms 128Mi / Xmx 512Mi
DB pool: 최대 5 connections
```

배포 중 Pod 두 개가 겹쳐 메모리를 압박하지 않도록 `maxSurge=0`, `maxUnavailable=1`을 사용합니다. 따라서 관리자 API 갱신 시 짧은 중단이 있을 수 있지만 기존 사용자 Frontend, 사용자 Backend stable/canary, 모델 EC2에는 routing이나 Deployment 변경이 없습니다.

Web EC2의 실제 여유 메모리는 배포 전후에 확인하십시오.

```bash
free -h
sudo kubectl top nodes
sudo kubectl top pods -n hearo
```

## Rollback

이전 정상 image SHA를 `k8s/hearo-admin-backend.yaml`에 다시 지정하고 commit/push한 뒤 Argo CD에서 Sync합니다.

```bash
git log --oneline -- k8s/hearo-admin-backend.yaml
```

Argo CD에서만 임시 rollback하면 Git의 최신 manifest와 다시 OutOfSync가 되므로, 최종 rollback은 반드시 manifest에도 반영합니다.
