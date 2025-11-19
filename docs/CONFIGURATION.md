# Guía de Configuración - Padel Club Pro

## Tabla de Contenidos

- [Variables de Entorno](#variables-de-entorno)
- [Configuración de Base de Datos](#configuración-de-base-de-datos)
- [Configuración de OAuth2 Google](#configuración-de-oauth2-google)
- [Configuración de Stripe](#configuración-de-stripe)
- [Configuración de Email](#configuración-de-email)
- [Configuración de Profiles](#configuración-de-profiles)
- [Configuración Avanzada](#configuración-avanzada)

---

## Variables de Entorno

### Archivo .env

Crear archivo `.env` en el directorio raíz del proyecto:

```bash
# ==================================================
# BASE DE DATOS
# ==================================================
DATABASE_URL=jdbc:postgresql://localhost:5432/padelclub_prod
DATABASE_USERNAME=padel_user
DATABASE_PASSWORD=tu_password_seguro
DATABASE_HOST=localhost
DATABASE_PORT=5432
DATABASE_NAME=padelclub_prod

# ==================================================
# GOOGLE OAUTH2
# ==================================================
GOOGLE_CLIENT_ID=123456789-abcdefghijklmnop.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=GOCSPX-abcdefghijklmnopqrstuvwx

# ==================================================
# STRIPE
# ==================================================
STRIPE_API_KEY=sk_live_tu_clave_stripe_produccion
STRIPE_API_KEY_TEST=sk_test_tu_clave_stripe_test
STRIPE_WEBHOOK_SECRET=whsec_tu_webhook_secret

# ==================================================
# APLICACIÓN
# ==================================================
APP_URL=https://padelclub.pro
PORT=8080
SPRING_PROFILES_ACTIVE=prod

# ==================================================
# SEGURIDAD
# ==================================================
JWT_SECRET=tu_secreto_jwt_muy_largo_y_seguro_minimo_64_caracteres
SESSION_TIMEOUT=30m

# ==================================================
# EMAIL (SMTP)
# ==================================================
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=noreply@padelclub.pro
MAIL_PASSWORD=tu_password_smtp

# ==================================================
# REDIS (OPCIONAL - Para caché)
# ==================================================
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# ==================================================
# LOGGING
# ==================================================
LOG_LEVEL=INFO
LOG_FILE=/var/log/padelclub/application.log

# ==================================================
# ACTUATOR
# ==================================================
MANAGEMENT_PORT=8081
MANAGEMENT_ENDPOINTS=health,info,metrics,prometheus
```

### Cargar Variables de Entorno

#### Linux/Mac

```bash
# Opción 1: Cargar desde archivo
export $(cat .env | grep -v '#' | xargs)

# Opción 2: Source
source .env

# Opción 3: En script
set -a
source .env
set +a
```

#### Windows

```powershell
# PowerShell
Get-Content .env | ForEach-Object {
    if ($_ -match '^([^#].+?)=(.+)$') {
        [Environment]::SetEnvironmentVariable($matches[1], $matches[2])
    }
}
```

#### Docker Compose

```yaml
# docker-compose.yml
services:
  app:
    env_file:
      - .env
```

---

## Configuración de Base de Datos

### PostgreSQL (Producción)

#### 1. Instalación de PostgreSQL

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
sudo systemctl enable postgresql
```

**Docker:**
```bash
docker run -d \
  --name postgres \
  -e POSTGRES_PASSWORD=your_password \
  -e POSTGRES_DB=padelclub_prod \
  -p 5432:5432 \
  -v pgdata:/var/lib/postgresql/data \
  postgres:15
```

#### 2. Crear Base de Datos y Usuario

```sql
-- Conectar como postgres
sudo -u postgres psql

-- Crear base de datos
CREATE DATABASE padelclub_prod;

-- Crear usuario
CREATE USER padel_user WITH ENCRYPTED PASSWORD 'tu_password_seguro';

-- Otorgar privilegios
GRANT ALL PRIVILEGES ON DATABASE padelclub_prod TO padel_user;

-- Configurar permisos en el schema
\c padelclub_prod
GRANT ALL ON SCHEMA public TO padel_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO padel_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO padel_user;

-- Salir
\q
```

#### 3. Configurar application-prod.properties

```properties
# PostgreSQL
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DATABASE_USERNAME}
spring.datasource.password=${DATABASE_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver

# HikariCP Connection Pool
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000

# JPA/Hibernate
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false
spring.jpa.properties.hibernate.jdbc.batch_size=20
```

#### 4. Optimización de PostgreSQL

Editar `/etc/postgresql/15/main/postgresql.conf`:

```conf
# Memory
shared_buffers = 256MB
effective_cache_size = 1GB
work_mem = 16MB
maintenance_work_mem = 128MB

# Checkpoint
checkpoint_completion_target = 0.9
wal_buffers = 16MB
default_statistics_target = 100

# Query tuning
random_page_cost = 1.1
effective_io_concurrency = 200

# Logging
log_min_duration_statement = 1000
log_line_prefix = '%t [%p]: [%l-1] user=%u,db=%d,app=%a,client=%h '
```

Reiniciar PostgreSQL:
```bash
sudo systemctl restart postgresql
```

### H2 (Desarrollo)

Para desarrollo local, la configuración por defecto ya incluye H2.

`application.properties` (desarrollo):
```properties
spring.datasource.url=jdbc:h2:file:./data/padelclub
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

spring.jpa.hibernate.ddl-auto=update
```

Acceso a consola H2: http://localhost:8080/h2-console

---

## Configuración de OAuth2 Google

### 1. Crear Proyecto en Google Cloud

1. Ir a [Google Cloud Console](https://console.cloud.google.com/)
2. Crear nuevo proyecto o seleccionar existente
3. Nombre: "Padel Club Pro"

### 2. Habilitar APIs

1. Ir a "APIs & Services" > "Library"
2. Buscar y habilitar:
   - **Google+ API** (para OAuth2)
   - **Google Calendar API** (para sincronización)
   - **People API** (para datos de perfil)

### 3. Configurar Pantalla de Consentimiento

1. Ir a "OAuth consent screen"
2. Tipo de usuario: **Externo**
3. Completar información:
   - Nombre de la aplicación: Padel Club Pro
   - Email de soporte: support@padelclub.pro
   - Logo (opcional)
   - Dominios autorizados: padelclub.pro
   - Email del desarrollador
4. Scopes necesarios:
   - `.../auth/userinfo.email`
   - `.../auth/userinfo.profile`
   - `.../auth/calendar.events`
5. Guardar

### 4. Crear Credenciales OAuth 2.0

1. Ir a "Credentials" > "Create Credentials" > "OAuth client ID"
2. Tipo de aplicación: **Web application**
3. Nombre: Padel Club Pro Web Client
4. **Authorized JavaScript origins**:
   ```
   http://localhost:8080
   https://padelclub.pro
   https://www.padelclub.pro
   ```
5. **Authorized redirect URIs**:
   ```
   http://localhost:8080/login/oauth2/code/google
   https://padelclub.pro/login/oauth2/code/google
   ```
6. Crear y copiar:
   - Client ID
   - Client Secret

### 5. Configurar en la Aplicación

Agregar a `.env`:
```bash
GOOGLE_CLIENT_ID=tu_client_id_aqui
GOOGLE_CLIENT_SECRET=tu_client_secret_aqui
```

Verificar en `application-prod.properties`:
```properties
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}
spring.security.oauth2.client.registration.google.scope=profile,email,https://www.googleapis.com/auth/calendar.events
spring.security.oauth2.client.registration.google.redirect-uri=${APP_URL}/login/oauth2/code/google
```

### 6. Verificar Configuración

```bash
# Test OAuth2 flow
curl -I https://padelclub.pro/oauth2/authorization/google

# Debería redirigir a Google
```

---

## Configuración de Stripe

### 1. Crear Cuenta Stripe

1. Ir a [Stripe](https://stripe.com)
2. Registrarse o iniciar sesión
3. Completar verificación de cuenta

### 2. Obtener API Keys

#### Modo Test (Desarrollo)

1. Ir a Developers > API keys
2. Copiar:
   - **Publishable key**: `pk_test_...`
   - **Secret key**: `sk_test_...`

#### Modo Producción

1. Activar cuenta (requiere verificación de negocio)
2. Ir a Developers > API keys
3. Copiar:
   - **Publishable key**: `pk_live_...`
   - **Secret key**: `sk_live_...`

### 3. Configurar Webhooks

1. Ir a Developers > Webhooks
2. "Add endpoint"
3. Endpoint URL: `https://padelclub.pro/api/stripe/webhook`
4. Seleccionar eventos:
   - `payment_intent.succeeded`
   - `payment_intent.payment_failed`
   - `payment_intent.canceled`
   - `charge.refunded`
5. Agregar endpoint
6. Copiar **Signing secret**: `whsec_...`

### 4. Configurar en la Aplicación

`.env`:
```bash
STRIPE_API_KEY=sk_live_tu_clave_produccion
STRIPE_API_KEY_TEST=sk_test_tu_clave_test
STRIPE_WEBHOOK_SECRET=whsec_tu_webhook_secret
```

`application-prod.properties`:
```properties
stripe.api.key=${STRIPE_API_KEY}
stripe.webhook.secret=${STRIPE_WEBHOOK_SECRET}
```

### 5. Testing

```bash
# Install Stripe CLI
brew install stripe/stripe-cli/stripe

# Login
stripe login

# Test webhook localmente
stripe listen --forward-to localhost:8080/api/stripe/webhook

# Trigger test event
stripe trigger payment_intent.succeeded
```

---

## Configuración de Email

### Opción 1: Gmail SMTP

#### 1. Habilitar "App Passwords"

1. Ir a Google Account > Security
2. Activar 2-Step Verification
3. Ir a "App passwords"
4. Generar contraseña para "Mail"
5. Copiar password de 16 caracteres

#### 2. Configurar en aplicación

`.env`:
```bash
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=noreply@padelclub.pro
MAIL_PASSWORD=xxxx xxxx xxxx xxxx
```

`application-prod.properties`:
```properties
spring.mail.host=${MAIL_HOST}
spring.mail.port=${MAIL_PORT}
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
```

### Opción 2: SendGrid

```properties
spring.mail.host=smtp.sendgrid.net
spring.mail.port=587
spring.mail.username=apikey
spring.mail.password=tu_sendgrid_api_key
```

### Opción 3: AWS SES

```properties
spring.mail.host=email-smtp.us-east-1.amazonaws.com
spring.mail.port=587
spring.mail.username=tu_smtp_username
spring.mail.password=tu_smtp_password
```

### Verificar Configuración

```java
// Test email
@Autowired
private JavaMailSender mailSender;

public void sendTestEmail() {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo("test@example.com");
    message.setSubject("Test Email");
    message.setText("Configuración correcta!");
    mailSender.send(message);
}
```

---

## Configuración de Profiles

### Profiles Disponibles

1. **default**: Desarrollo local (H2, sin OAuth2)
2. **test**: Para testing (H2 en memoria)
3. **prod**: Producción (PostgreSQL, todas las integraciones)

### Activar Profile

#### Por Variable de Entorno

```bash
export SPRING_PROFILES_ACTIVE=prod
```

#### Por Línea de Comandos

```bash
java -jar -Dspring.profiles.active=prod app.jar
```

#### En application.properties

```properties
spring.profiles.active=prod
```

#### En Docker

```yaml
environment:
  - SPRING_PROFILES_ACTIVE=prod
```

### Crear Profile Personalizado

Crear `application-custom.properties`:

```properties
# Heredar de prod
spring.profiles.include=prod

# Sobrescribir valores específicos
server.port=9090
logging.level.com.padellevel=DEBUG
```

Activar:
```bash
java -jar -Dspring.profiles.active=custom app.jar
```

---

## Configuración Avanzada

### SSL/TLS

#### Opción 1: Let's Encrypt con Nginx

```nginx
server {
    listen 80;
    server_name padelclub.pro www.padelclub.pro;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name padelclub.pro www.padelclub.pro;

    ssl_certificate /etc/letsencrypt/live/padelclub.pro/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/padelclub.pro/privkey.pem;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

Obtener certificado:
```bash
sudo certbot --nginx -d padelclub.pro -d www.padelclub.pro
```

#### Opción 2: SSL en Spring Boot

`application-prod.properties`:
```properties
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=${SSL_KEYSTORE_PASSWORD}
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=padelclub
```

Generar keystore:
```bash
keytool -genkeypair -alias padelclub -keyalg RSA -keysize 2048 \
  -storetype PKCS12 -keystore keystore.p12 -validity 3650
```

### Rate Limiting

Agregar dependencia:
```xml
<dependency>
    <groupId>com.github.vladimir-bukhtoyarov</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>7.6.0</version>
</dependency>
```

Configurar:
```properties
bucket4j.enabled=true
bucket4j.filters[0].cache-name=rate-limit-buckets
bucket4j.filters[0].url=.*
bucket4j.filters[0].rate-limits[0].bandwidths[0].capacity=100
bucket4j.filters[0].rate-limits[0].bandwidths[0].time=1
bucket4j.filters[0].rate-limits[0].bandwidths[0].unit=minutes
```

### CORS Personalizado

```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);
        config.addAllowedOrigin("https://padelclub.pro");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        source.registerCorsConfiguration("/api/**", config);
        return new CorsFilter(source);
    }
}
```

### Redis Cache (Opcional)

#### Instalación
```bash
# Docker
docker run -d --name redis -p 6379:6379 redis:7-alpine

# Ubuntu
sudo apt install redis-server
```

#### Configuración
```properties
spring.cache.type=redis
spring.redis.host=${REDIS_HOST:localhost}
spring.redis.port=${REDIS_PORT:6379}
spring.redis.password=${REDIS_PASSWORD:}
spring.redis.timeout=60000
```

#### Uso en código
```java
@Cacheable(value = "torneos", key = "#id")
public Torneo findById(Long id) {
    return torneoRepository.findById(id).orElse(null);
}

@CacheEvict(value = "torneos", key = "#torneo.id")
public void save(Torneo torneo) {
    torneoRepository.save(torneo);
}
```

### Monitoring con Prometheus

```properties
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.metrics.export.prometheus.enabled=true
management.endpoint.prometheus.enabled=true
```

Configurar Prometheus (`prometheus.yml`):
```yaml
scrape_configs:
  - job_name: 'spring-boot'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8080']
```

---

## Checklist de Configuración

### Pre-Producción

- [ ] Variables de entorno configuradas en `.env`
- [ ] PostgreSQL instalado y database creada
- [ ] Usuario de BD con permisos correctos
- [ ] Google OAuth2 credentials obtenidas y configuradas
- [ ] Stripe account verificada y webhooks configurados
- [ ] Email SMTP configurado y probado
- [ ] SSL/TLS certificado instalado
- [ ] Firewall configurado
- [ ] Backups automáticos configurados
- [ ] Monitoreo configurado (Actuator/Prometheus)
- [ ] Logs configurados y rotación activa

### Post-Despliegue

- [ ] Health check responde correctamente
- [ ] OAuth2 login funciona
- [ ] Pagos de prueba funcionan
- [ ] Emails se envían correctamente
- [ ] WebSocket conecta correctamente
- [ ] API REST responde
- [ ] Swagger UI accesible
- [ ] Backups funcionan
- [ ] Monitoreo recolecta métricas

---

## Troubleshooting de Configuración

### Base de Datos no conecta

```bash
# Verificar que PostgreSQL esté corriendo
sudo systemctl status postgresql

# Verificar conexión
psql -h localhost -U padel_user -d padelclub_prod

# Ver logs
sudo tail -f /var/log/postgresql/postgresql-15-main.log
```

### OAuth2 no funciona

1. Verificar redirect URI en Google Console
2. Verificar que variables de entorno están cargadas
3. Ver logs de aplicación para errores específicos
4. Verificar que APIs están habilitadas en Google Cloud

### Stripe webhooks fallan

1. Verificar que webhook secret es correcto
2. Verificar que endpoint es accesible públicamente
3. Probar con Stripe CLI
4. Ver logs de webhook en Stripe Dashboard

---

<div align="center">

**¿Problemas con la configuración?**
Consulta [DEPLOYMENT.md](../DEPLOYMENT.md) o contacta a soporte.

</div>
