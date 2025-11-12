# 🌍 Configuración de Entornos - Padel Club Pro

## Estrategia Multi-Entorno

Este proyecto utiliza 4 entornos independientes:

1. **Local** - Desarrollo en portátil
2. **Integración** - Testing de integración en la nube
3. **UAT** - User Acceptance Testing
4. **Producción** - Entorno productivo

---

## 📁 Estructura de Archivos de Configuración

```
src/main/resources/
├── application.properties                    # Configuración base
├── application-local.properties             # Entorno local
├── application-integration.properties       # Entorno integración
├── application-uat.properties              # Entorno UAT
└── application-production.properties        # Entorno producción
```

---

## 🔧 Configuración por Entorno

### 1️⃣ LOCAL (Desarrollo)

**Base de datos:** HSQLDB (file-based) o PostgreSQL local
**Puerto:** 8080
**Características:**
- Hot reload habilitado
- Debug logging
- Seed data automático
- Sin SSL
- Email/SMS en modo mock

**Variables de entorno:**
```bash
SPRING_PROFILES_ACTIVE=local
```

### 2️⃣ INTEGRACIÓN (Cloud)

**Base de datos:** PostgreSQL en RDS/Cloud SQL
**Puerto:** 8080 (interno), 443 (externo)
**Características:**
- SSL habilitado
- Logs a CloudWatch/Stackdriver
- Email/SMS real (SendGrid test API)
- Cache con Redis
- Seed data de prueba

**Variables de entorno:**
```bash
SPRING_PROFILES_ACTIVE=integration
DB_HOST=integration-db.example.com
DB_NAME=padelclub_integration
DB_USER=admin
DB_PASSWORD=${DB_PASSWORD}
STRIPE_SECRET_KEY=${STRIPE_TEST_KEY}
```

### 3️⃣ UAT (User Acceptance Testing)

**Base de datos:** PostgreSQL dedicado
**Puerto:** 8080 (interno), 443 (externo)
**Características:**
- SSL habilitado
- Logs completos
- Email/SMS real
- Cache con Redis
- Datos de prueba realistas
- Backup diario

**Variables de entorno:**
```bash
SPRING_PROFILES_ACTIVE=uat
DB_HOST=uat-db.example.com
DB_NAME=padelclub_uat
DB_USER=admin
DB_PASSWORD=${DB_PASSWORD}
STRIPE_SECRET_KEY=${STRIPE_TEST_KEY}
```

### 4️⃣ PRODUCCIÓN

**Base de datos:** PostgreSQL con replicación
**Puerto:** 8080 (interno), 443 (externo)
**Características:**
- SSL obligatorio
- Logs optimizados
- Email/SMS real (producción)
- Cache con Redis Cluster
- Backup automático cada 6 horas
- Monitoreo 24/7
- CDN para assets
- Rate limiting

**Variables de entorno:**
```bash
SPRING_PROFILES_ACTIVE=production
DB_HOST=prod-db.example.com
DB_NAME=padelclub_prod
DB_USER=admin
DB_PASSWORD=${DB_PASSWORD}
STRIPE_SECRET_KEY=${STRIPE_LIVE_KEY}
```

---

## 🗄️ Base de Datos por Entorno

| Entorno | Motor | Host | Backup | Replicación |
|---------|-------|------|--------|-------------|
| Local | HSQLDB/PostgreSQL | localhost | Manual | No |
| Integración | PostgreSQL 15 | RDS/Cloud SQL | Diario | No |
| UAT | PostgreSQL 15 | RDS/Cloud SQL | Diario | No |
| Producción | PostgreSQL 15 | RDS/Cloud SQL | Cada 6h | Sí |

---

## 🚀 Configuración de Servicios Externos

### Stripe

| Entorno | API Key | Webhooks |
|---------|---------|----------|
| Local | Test Key | ngrok/localhost |
| Integración | Test Key | https://integration.padelclub.pro/webhook |
| UAT | Test Key | https://uat.padelclub.pro/webhook |
| Producción | **Live Key** | https://padelclub.pro/webhook |

### Google OAuth2

| Entorno | Client ID | Redirect URI |
|---------|-----------|--------------|
| Local | Dev Client ID | http://localhost:8080/login/oauth2/code/google |
| Integración | Integration Client ID | https://integration.padelclub.pro/login/oauth2/code/google |
| UAT | UAT Client ID | https://uat.padelclub.pro/login/oauth2/code/google |
| Producción | **Prod Client ID** | https://padelclub.pro/login/oauth2/code/google |

### SendGrid (Email)

| Entorno | API Key | Templates |
|---------|---------|-----------|
| Local | Mock/Test | Test templates |
| Integración | Test Key | Test templates |
| UAT | Test Key | Production templates |
| Producción | **Live Key** | Production templates |

### Firebase (Push Notifications)

| Entorno | Project | Server Key |
|---------|---------|------------|
| Local | padel-dev | Dev key |
| Integración | padel-integration | Integration key |
| UAT | padel-uat | UAT key |
| Producción | **padel-prod** | **Prod key** |

---

## 🐳 Docker Compose por Entorno

### Local
```yaml
version: '3.8'
services:
  app:
    build: .
    environment:
      - SPRING_PROFILES_ACTIVE=local
    ports:
      - "8080:8080"
```

### Integración/UAT/Producción
```yaml
version: '3.8'
services:
  app:
    image: padelclub-pro:${VERSION}
    environment:
      - SPRING_PROFILES_ACTIVE=${ENV}
    ports:
      - "8080:8080"

  postgres:
    image: postgres:15
    environment:
      - POSTGRES_DB=${DB_NAME}
      - POSTGRES_USER=${DB_USER}
      - POSTGRES_PASSWORD=${DB_PASSWORD}

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
```

---

## 🔄 CI/CD Pipeline

### GitHub Actions Workflow

```yaml
name: CI/CD Pipeline

on:
  push:
    branches:
      - main           # → Producción
      - uat            # → UAT
      - integration    # → Integración
      - develop        # → Build only

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - Checkout
      - Setup Java 17
      - Maven build
      - Run tests
      - SonarQube scan
      - Build Docker image

  deploy-integration:
    if: github.ref == 'refs/heads/integration'
    needs: build
    runs-on: ubuntu-latest
    steps:
      - Deploy to Integration
      - Run smoke tests

  deploy-uat:
    if: github.ref == 'refs/heads/uat'
    needs: build
    runs-on: ubuntu-latest
    steps:
      - Deploy to UAT
      - Run E2E tests

  deploy-production:
    if: github.ref == 'refs/heads/main'
    needs: build
    runs-on: ubuntu-latest
    environment: production
    steps:
      - Manual approval
      - Blue-Green deployment
      - Health check
      - Rollback capability
```

---

## 🌐 URLs de Entornos

| Entorno | URL Principal | Admin Panel | API |
|---------|---------------|-------------|-----|
| Local | http://localhost:8080 | http://localhost:8080/admin | http://localhost:8080/api |
| Integración | https://integration.padelclub.pro | https://integration.padelclub.pro/admin | https://integration.padelclub.pro/api |
| UAT | https://uat.padelclub.pro | https://uat.padelclub.pro/admin | https://uat.padelclub.pro/api |
| Producción | https://padelclub.pro | https://admin.padelclub.pro | https://api.padelclub.pro |

---

## 🔒 Seguridad por Entorno

### Local
- ❌ Sin SSL
- ⚠️ Credenciales hardcoded permitido
- ⚠️ CORS: *

### Integración/UAT
- ✅ SSL con certificado válido
- ✅ Variables de entorno
- ✅ CORS restringido
- ✅ Rate limiting básico

### Producción
- ✅ SSL A+ rating
- ✅ Secrets Manager (AWS/GCP)
- ✅ CORS estricto
- ✅ Rate limiting agresivo
- ✅ WAF habilitado
- ✅ DDoS protection
- ✅ 2FA para admins
- ✅ Audit logging

---

## 📊 Monitoreo y Logs

### Local
- Console logs
- Spring Boot Actuator

### Integración/UAT
- CloudWatch/Stackdriver
- Application logs
- Access logs
- Error tracking (Sentry)

### Producción
- CloudWatch/Stackdriver
- ELK Stack
- Prometheus + Grafana
- Sentry/Rollbar
- Uptime monitoring
- Performance monitoring (New Relic/Datadog)

---

## 🔧 Configuración de Red

### Local
```
127.0.0.1:8080 → App
127.0.0.1:5432 → PostgreSQL (opcional)
```

### Cloud (Integration/UAT/Prod)
```
Internet → CloudFlare CDN → Load Balancer → App Servers
                                          → Redis Cluster
                                          → PostgreSQL (RDS/Cloud SQL)
```

---

## 💾 Estrategia de Backup

| Entorno | Frecuencia | Retención | Ubicación |
|---------|-----------|-----------|-----------|
| Local | Manual | N/A | Local |
| Integración | Diario 2am | 7 días | S3/Cloud Storage |
| UAT | Diario 2am | 30 días | S3/Cloud Storage |
| Producción | Cada 6h | 90 días + archive | S3/Cloud Storage Multi-region |

---

## 🧪 Testing por Entorno

### Local
- Unit tests
- Integration tests
- Manual testing

### Integración
- Smoke tests automáticos
- Integration tests
- API tests

### UAT
- E2E tests
- Performance tests
- Security tests
- User acceptance testing

### Producción
- Smoke tests post-deploy
- Synthetic monitoring
- Real user monitoring

---

## 📦 Artefactos y Versionado

### Estrategia de Versiones
```
<major>.<minor>.<patch>-<env>.<build>

Ejemplos:
1.0.0-local.123
1.0.0-integration.456
1.0.0-uat.789
1.0.0-production.1000
```

### Docker Images
```
padelclub-pro:1.0.0-local
padelclub-pro:1.0.0-integration
padelclub-pro:1.0.0-uat
padelclub-pro:1.0.0-production
```

---

## 🚦 Estrategia de Promoción

```
Developer → Local Testing
             ↓
       Git push to develop
             ↓
       CI Build + Tests
             ↓
    Merge to integration branch
             ↓
   Auto-deploy to INTEGRATION
             ↓
    Smoke tests pass? → YES
             ↓
    Merge to uat branch
             ↓
   Auto-deploy to UAT
             ↓
    E2E tests + Manual QA → APPROVED
             ↓
    Create Release PR to main
             ↓
    Code review + Approval
             ↓
    Merge to main
             ↓
   **MANUAL APPROVAL REQUIRED**
             ↓
  Blue-Green Deploy to PRODUCTION
             ↓
    Health checks pass → GREEN
             ↓
    Switch traffic to new version
             ↓
    Monitor for 1 hour
             ↓
   Issues? → Rollback to BLUE
   Success? → Mark GREEN as stable
```

---

## 🛠️ Comandos Útiles

### Ejecutar localmente
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### Build para entorno específico
```bash
./mvnw clean package -Pintegration
./mvnw clean package -Puat
./mvnw clean package -Pproduction
```

### Docker build
```bash
docker build -t padelclub-pro:1.0.0-local .
docker build -t padelclub-pro:1.0.0-integration .
```

### Docker run
```bash
docker run -e SPRING_PROFILES_ACTIVE=local -p 8080:8080 padelclub-pro:1.0.0-local
```

### Ver logs
```bash
# Local
tail -f logs/application.log

# Cloud (AWS)
aws logs tail /aws/ecs/padelclub-integration --follow

# Cloud (GCP)
gcloud logging tail "resource.type=k8s_container"
```

---

## ✅ Checklist de Configuración

### Local
- [ ] Java 17 instalado
- [ ] Maven/Gradle configurado
- [ ] IDE configurado (IntelliJ/Eclipse)
- [ ] Git configurado
- [ ] Docker Desktop (opcional)
- [ ] PostgreSQL local (opcional)

### Integración
- [ ] Infraestructura cloud provisionada
- [ ] Base de datos creada
- [ ] Credenciales configuradas
- [ ] DNS configurado
- [ ] SSL certificado instalado
- [ ] CI/CD pipeline configurado

### UAT
- [ ] Infraestructura cloud provisionada
- [ ] Base de datos creada
- [ ] Datos de prueba cargados
- [ ] DNS configurado
- [ ] SSL certificado instalado
- [ ] Testers tienen acceso

### Producción
- [ ] Infraestructura cloud provisionada con redundancia
- [ ] Base de datos con replicación
- [ ] CDN configurado
- [ ] DNS con failover
- [ ] SSL A+ rating
- [ ] Monitoreo 24/7 configurado
- [ ] Backup automático activo
- [ ] DDoS protection activo
- [ ] Equipo de soporte capacitado
- [ ] Runbook de incidentes documentado

---

## 🎯 Siguiente Paso

Una vez configurados los entornos, procederemos con:
- **FASE 1.1:** Crear nuevas entidades del modelo de datos
- **FASE 1.2:** Implementar servicios core
- **FASE 1.3:** Desarrollar vistas de usuario

---

## 📞 Soporte

- **Local:** Logs en consola
- **Integración/UAT:** Slack channel #padel-dev
- **Producción:** PagerDuty + Slack #padel-alerts
