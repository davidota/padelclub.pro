# Guía de Deployment - Padel Club Pro

Esta guía describe los procesos de deployment para la aplicación Padel Club Pro.

## Tabla de Contenidos

- [Requisitos](#requisitos)
- [Configuración de Entorno](#configuración-de-entorno)
- [Deployment con Docker](#deployment-con-docker)
- [Deployment Manual](#deployment-manual)
- [CI/CD con GitHub Actions](#cicd-con-github-actions)
- [Monitoreo y Logs](#monitoreo-y-logs)
- [Rollback](#rollback)
- [Troubleshooting](#troubleshooting)

## Requisitos

### Requisitos de Producción

- **Java**: JDK 17 o superior
- **Base de Datos**: PostgreSQL 14+ (recomendado) o H2 (solo desarrollo)
- **Memoria**: Mínimo 2GB RAM, Recomendado 4GB+
- **CPU**: Mínimo 2 cores
- **Disco**: 10GB+ de espacio libre
- **Docker** (opcional): 20.10+ con Docker Compose

### Variables de Entorno Requeridas

```bash
# Base de datos
DATABASE_URL=jdbc:postgresql://localhost:5432/padelclub_prod
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=secure_password

# OAuth2 Google
GOOGLE_CLIENT_ID=your_client_id
GOOGLE_CLIENT_SECRET=your_client_secret

# Stripe
STRIPE_API_KEY=your_stripe_api_key
STRIPE_WEBHOOK_SECRET=your_webhook_secret

# Aplicación
APP_URL=https://padelclub.pro
PORT=8080
```

## Configuración de Entorno

### 1. Preparar Base de Datos

```sql
-- Crear base de datos
CREATE DATABASE padelclub_prod;

-- Crear usuario
CREATE USER padel_user WITH ENCRYPTED PASSWORD 'secure_password';

-- Otorgar permisos
GRANT ALL PRIVILEGES ON DATABASE padelclub_prod TO padel_user;
```

### 2. Configurar Variables de Entorno

Crear archivo `.env` en el directorio raíz:

```bash
cp .env.example .env
# Editar .env con los valores correctos
```

## Deployment con Docker

### Opción 1: Docker Compose (Recomendado)

```bash
# 1. Build de la imagen
docker-compose -f docker-compose.production.yml build

# 2. Iniciar servicios
docker-compose -f docker-compose.production.yml up -d

# 3. Verificar estado
docker-compose -f docker-compose.production.yml ps

# 4. Ver logs
docker-compose -f docker-compose.production.yml logs -f app
```

### Opción 2: Docker Manual

```bash
# 1. Build de la imagen
docker build -t padelclub-pro:latest .

# 2. Ejecutar contenedor
docker run -d \
  --name padelclub-app \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DATABASE_URL=$DATABASE_URL \
  -e DATABASE_USERNAME=$DATABASE_USERNAME \
  -e DATABASE_PASSWORD=$DATABASE_PASSWORD \
  -e GOOGLE_CLIENT_ID=$GOOGLE_CLIENT_ID \
  -e GOOGLE_CLIENT_SECRET=$GOOGLE_CLIENT_SECRET \
  -e STRIPE_API_KEY=$STRIPE_API_KEY \
  --restart unless-stopped \
  padelclub-pro:latest
```

## Deployment Manual

### 1. Build de la Aplicación

```bash
# Limpiar y compilar
mvn clean package -DskipTests -Pproduction

# El JAR se generará en: target/padel-1.0-SNAPSHOT.jar
```

### 2. Ejecutar Aplicación

```bash
# Opción 1: Directamente
java -jar \
  -Xms512m -Xmx2g \
  -Dspring.profiles.active=prod \
  target/padel-1.0-SNAPSHOT.jar

# Opción 2: Como servicio systemd (ver más abajo)
```

### 3. Configurar como Servicio Systemd

Crear archivo `/etc/systemd/system/padelclub.service`:

```ini
[Unit]
Description=Padel Club Pro Application
After=syslog.target network.target

[Service]
User=padel
Group=padel
SuccessExitStatus=143
WorkingDirectory=/opt/padelclub
ExecStart=/usr/bin/java -Xms512m -Xmx2g -Dspring.profiles.active=prod -jar /opt/padelclub/app.jar
ExecStop=/bin/kill -15 $MAINPID

Environment="DATABASE_URL=jdbc:postgresql://localhost:5432/padelclub_prod"
Environment="DATABASE_USERNAME=padel_user"
Environment="DATABASE_PASSWORD=secure_password"

StandardOutput=append:/var/log/padelclub/application.log
StandardError=append:/var/log/padelclub/error.log

Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Comandos:

```bash
# Recargar systemd
sudo systemctl daemon-reload

# Iniciar servicio
sudo systemctl start padelclub

# Habilitar inicio automático
sudo systemctl enable padelclub

# Ver estado
sudo systemctl status padelclub

# Ver logs
sudo journalctl -u padelclub -f
```

## CI/CD con GitHub Actions

El proyecto incluye configuración de CI/CD automática con GitHub Actions.

### Flujo de Trabajo

1. **Push a rama**: Se ejecutan tests automáticamente
2. **Merge a develop**: Deploy automático a Staging
3. **Merge a main**: Deploy automático a Producción

### Configurar Secrets en GitHub

Ir a: `Settings > Secrets and variables > Actions`

Agregar los siguientes secrets:

```
DOCKER_USERNAME
DOCKER_PASSWORD
DATABASE_URL
DATABASE_USERNAME
DATABASE_PASSWORD
GOOGLE_CLIENT_ID
GOOGLE_CLIENT_SECRET
STRIPE_API_KEY
STRIPE_WEBHOOK_SECRET
```

### Trigger Manual de Deployment

```bash
# Desde GitHub UI: Actions > CI/CD Pipeline > Run workflow
# O mediante gh CLI:
gh workflow run ci-cd.yml
```

## Monitoreo y Logs

### Health Checks

```bash
# Health check básico
curl http://localhost:8080/actuator/health

# Información detallada
curl http://localhost:8080/actuator/info

# Métricas (Prometheus)
curl http://localhost:8080/actuator/prometheus
```

### Logs

```bash
# Docker Compose
docker-compose -f docker-compose.production.yml logs -f app

# Docker standalone
docker logs -f padelclub-app

# Systemd
sudo journalctl -u padelclub -f

# Archivo (si está configurado)
tail -f /var/log/padelclub/application.log
```

### Monitoreo con Prometheus + Grafana (Opcional)

```bash
# Usar docker-compose.monitoring.yml (si existe)
docker-compose -f docker-compose.monitoring.yml up -d

# Acceder a:
# - Grafana: http://localhost:3000
# - Prometheus: http://localhost:9090
```

## Rollback

### Con Docker

```bash
# 1. Detener contenedor actual
docker-compose -f docker-compose.production.yml down

# 2. Cambiar a versión anterior
docker pull padelclub-pro:previous-tag

# 3. Actualizar docker-compose.yml con el tag anterior

# 4. Reiniciar
docker-compose -f docker-compose.production.yml up -d
```

### Con Systemd

```bash
# 1. Detener servicio
sudo systemctl stop padelclub

# 2. Restaurar JAR anterior
sudo cp /opt/padelclub/backups/app.jar.backup /opt/padelclub/app.jar

# 3. Reiniciar servicio
sudo systemctl start padelclub
```

### Rollback de Base de Datos

```bash
# 1. Restaurar desde backup
psql -U padel_user -d padelclub_prod < backup_YYYYMMDD.sql

# 2. Si usas Flyway/Liquibase
mvn flyway:undo -Dflyway.target=<version>
```

## Troubleshooting

### Aplicación no inicia

1. **Verificar logs**:
   ```bash
   docker logs padelclub-app
   # O
   sudo journalctl -u padelclub -n 100
   ```

2. **Verificar conexión a base de datos**:
   ```bash
   psql -h localhost -U padel_user -d padelclub_prod
   ```

3. **Verificar puerto**:
   ```bash
   netstat -tulpn | grep 8080
   # O
   lsof -i :8080
   ```

### Performance Lento

1. **Aumentar memoria JVM**:
   ```bash
   JAVA_OPTS="-Xms1g -Xmx4g"
   ```

2. **Verificar métricas**:
   ```bash
   curl http://localhost:8080/actuator/metrics
   ```

3. **Verificar base de datos**:
   ```sql
   -- Consultas lentas
   SELECT * FROM pg_stat_statements ORDER BY total_time DESC LIMIT 10;
   ```

### Errores de OAuth2

1. **Verificar redirect URI** en Google Console
2. **Verificar variables de entorno**:
   ```bash
   echo $GOOGLE_CLIENT_ID
   echo $GOOGLE_CLIENT_SECRET
   ```

### Errores de Stripe

1. **Verificar webhook secret**
2. **Verificar endpoint de webhook** en Stripe Dashboard
3. **Verificar logs** de webhooks en Stripe

## Backup y Restore

### Backup de Base de Datos

```bash
# Backup completo
pg_dump -U padel_user -h localhost padelclub_prod > backup_$(date +%Y%m%d).sql

# Backup comprimido
pg_dump -U padel_user -h localhost padelclub_prod | gzip > backup_$(date +%Y%m%d).sql.gz

# Backup automático (cron)
0 2 * * * /usr/bin/pg_dump -U padel_user padelclub_prod > /backups/padelclub_$(date +\%Y\%m\%d).sql
```

### Restore

```bash
# Desde backup sin comprimir
psql -U padel_user -d padelclub_prod < backup_20240101.sql

# Desde backup comprimido
gunzip -c backup_20240101.sql.gz | psql -U padel_user -d padelclub_prod
```

## Seguridad

### Checklist de Seguridad

- [ ] Cambiar contraseñas por defecto
- [ ] Configurar SSL/TLS
- [ ] Configurar firewall
- [ ] Habilitar HTTPS
- [ ] Configurar rate limiting
- [ ] Revisar logs regularmente
- [ ] Mantener dependencias actualizadas
- [ ] Configurar backups automáticos
- [ ] Implementar monitoreo de seguridad

### Actualización de Seguridad

```bash
# Actualizar dependencias
mvn versions:display-dependency-updates

# Actualizar a versiones más recientes
mvn versions:use-latest-releases

# Verificar vulnerabilidades
mvn dependency:check
```

## Contacto y Soporte

Para problemas o preguntas:
- **Email**: support@padelclub.pro
- **GitHub Issues**: https://github.com/davidota/padelclub.pro/issues
- **Documentación**: https://docs.padelclub.pro
