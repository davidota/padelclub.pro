# Padel Club Pro - Sistema de Gestión de Torneos de Pádel

<div align="center">

![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)
![Java](https://img.shields.io/badge/Java-17-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-brightgreen.svg)
![Vaadin](https://img.shields.io/badge/Vaadin-24.5.4-00B4F0.svg)

**Sistema completo de gestión de torneos de pádel con gamificación, chat en tiempo real y predicciones ML**

[Documentación](#-documentación) •
[Características](#-características-principales) •
[Instalación](#-instalación) •
[Despliegue](#-despliegue)

</div>

---

## 📋 Tabla de Contenidos

- [Descripción](#-descripción)
- [Características Principales](#-características-principales)
- [Tecnologías](#️-tecnologías)
- [Requisitos](#-requisitos)
- [Instalación Rápida](#-instalación-rápida)
- [Configuración](#️-configuración)
- [Uso](#-uso)
- [Documentación](#-documentación)
- [API REST](#-api-rest)
- [Testing](#-testing)
- [Despliegue](#-despliegue)

---

## 🎯 Descripción

**Padel Club Pro** es una aplicación web enterprise para la gestión integral de clubes de pádel, torneos y competiciones. Diseñada para clubes deportivos, organizadores de eventos y jugadores profesionales.

### ¿Para quién es?

- **Clubes Deportivos**: Gestión completa de instalaciones, miembros y torneos
- **Organizadores**: Herramientas profesionales para gestionar competiciones
- **Jugadores**: Inscripciones, estadísticas y rankings en tiempo real
- **Administradores**: Control multi-club con permisos granulares

---

## ✨ Características Principales

### 🏆 Gestión de Torneos
- Múltiples formatos: Americano, Liga, Eliminación
- Sistema de emparejamiento inteligente
- Gestión automatizada de pozos y enfrentamientos
- Calendario integrado con Google Calendar
- Asignación automática de pistas

### 👥 Gestión de Usuarios
- Sistema de roles: Admin, Player, Operator
- OAuth2 Google Login
- Perfiles con estadísticas detalladas
- Multi-club: usuarios en múltiples clubes
- Niveles de habilidad (Principiante a Profesional)

### 🎮 Gamificación Completa
- 50+ logros desbloqueables
- Rankings por club y categoría
- Sistema de experiencia (niveles 1-100)
- Premios y trofeos
- Estadísticas avanzadas de rendimiento

### 💬 Comunicación en Tiempo Real
- Chat WebSocket (privado, equipo, torneo, club)
- Notificaciones multi-canal (in-app, email, SMS)
- Integración Google Calendar
- Alertas en tiempo real

### 🤖 Inteligencia Artificial
- Predicciones ML de resultados de partidos
- Análisis estadístico de enfrentamientos
- Identificación de favoritos en torneos
- Scoring multi-factor con Apache Commons Math

### 💳 Pagos Integrados
- Integración completa con Stripe
- Inscripciones gratuitas y de pago
- Webhooks automáticos
- Historial de transacciones

### 🏢 Multi-Club Enterprise
- Soporte para múltiples clubes
- Configuración independiente por club
- Permisos granulares por club
- Branding personalizado (colores, logo)

### 🔌 API REST Completa
- 50+ endpoints documentados
- Swagger/OpenAPI UI interactivo
- Autenticación JWT
- Paginación y filtros avanzados

---

## 🛠️ Tecnologías

**Backend**: Java 17 • Spring Boot 3.4 • Spring Security • Spring Data JPA • Hibernate • OAuth2 • WebSocket

**Frontend**: Vaadin Flow 24.5 • Vaadin Components • Responsive Design • Material Design

**Base de Datos**: PostgreSQL (prod) • H2 (dev) • HikariCP

**Integraciones**: Stripe • Google OAuth2 • Google Calendar • Swagger/OpenAPI

**DevOps**: Docker • Docker Compose • GitHub Actions • Maven • JUnit 5 • Mockito

**Monitoreo**: Spring Actuator • Prometheus • Micrometer

---

## 📋 Requisitos

### Mínimos
- Java JDK 17+
- Maven 3.8+
- 2GB RAM
- 5GB disco
- PostgreSQL 14+ o H2

### Recomendados
- Java JDK 17 (LTS)
- Maven 3.9+
- 4GB+ RAM
- 4 CPU cores
- 10GB+ disco
- PostgreSQL 15+
- Docker 20.10+ (opcional)

### Cuentas Requeridas
1. **Google Cloud**: OAuth2 + Calendar API
2. **Stripe**: API Keys (test y producción)
3. **PostgreSQL**: Base de datos (local o cloud)

---

## 🚀 Instalación Rápida

### Opción 1: Docker (Recomendado)

```bash
# Clonar
git clone https://github.com/davidota/padelclub.pro.git
cd padelclub.pro

# Configurar
cp .env.example .env
# Editar .env con tus credenciales

# Iniciar
docker-compose -f docker-compose.production.yml up -d

# Acceder: http://localhost:8080
```

### Opción 2: Local (Desarrollo)

```bash
# Clonar
git clone https://github.com/davidota/padelclub.pro.git
cd padelclub.pro

# Configurar
cp .env.example .env

# Compilar e iniciar
mvn clean install
mvn spring-boot:run

# Acceder: http://localhost:8080
```

---

## ⚙️ Configuración

### Variables de Entorno Esenciales

```bash
# Base de datos
DATABASE_URL=jdbc:postgresql://localhost:5432/padelclub_prod
DATABASE_USERNAME=padel_user
DATABASE_PASSWORD=your_secure_password

# OAuth2 Google
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret

# Stripe
STRIPE_API_KEY=sk_test_your_stripe_key
STRIPE_WEBHOOK_SECRET=whsec_your_webhook_secret

# App
APP_URL=https://your-domain.com
PORT=8080
SPRING_PROFILES_ACTIVE=prod
```

**Ver configuración completa**: [CONFIGURATION.md](docs/CONFIGURATION.md)

---

## 💻 Uso

### Primer Acceso
1. Acceder a `http://localhost:8080`
2. Registrarse o usar "Login con Google"
3. Completar perfil (nivel, información personal)
4. Explorar torneos y rankings

### Como Administrador
1. Panel de administración > Crear Club
2. Configurar club (logo, colores, pistas)
3. Crear torneo y gestionar inscripciones
4. Iniciar torneo y registrar resultados

### Como Jugador
1. Buscar torneos disponibles
2. Inscribirse (pagar si aplica)
3. Ver calendario de partidos
4. Chatear con equipo
5. Consultar estadísticas y logros

**Manual completo**: [USER_MANUAL.md](docs/USER_MANUAL.md)

---

## 📚 Documentación

- [📖 Manual de Usuario](docs/USER_MANUAL.md)
- [🚀 Guía de Despliegue](DEPLOYMENT.md)
- [⚙️ Configuración Detallada](docs/CONFIGURATION.md)
- [🏗️ Arquitectura del Sistema](docs/ARCHITECTURE.md)
- [🔌 Documentación de API](docs/API_GUIDE.md)

### Swagger UI
- **Dev**: http://localhost:8080/swagger-ui.html
- **Prod**: https://your-domain.com/swagger-ui.html

---

## 🔌 API REST

### Endpoints Principales

**Torneos**
```
GET    /api/v1/torneos              - Listar torneos
GET    /api/v1/torneos/{id}         - Obtener torneo
POST   /api/v1/torneos              - Crear torneo
```

**Clubes**
```
GET    /api/v1/clubes               - Listar clubes
GET    /api/v1/clubes/mis-clubes    - Mis clubes
POST   /api/v1/clubes               - Crear club
```

**Gamificación**
```
GET    /api/v1/gamificacion/ranking - Ranking global
GET    /api/v1/gamificacion/logros  - Logros
```

**Predicciones ML**
```
GET    /api/v1/predicciones/enfrentamiento - Predecir partido
GET    /api/v1/predicciones/torneo/{id}    - Predecir torneo
```

**Ver API completa**: [API_GUIDE.md](docs/API_GUIDE.md)

---

## 🧪 Testing

```bash
# Todos los tests
mvn test

# Con cobertura
mvn test jacoco:report

# Solo unitarios
mvn test -Dgroups="unit"

# Solo integración
mvn test -Dgroups="integration"
```

**Cobertura**: 22+ tests unitarios, 6+ tests de integración

---

## 🚀 Despliegue

### Entornos Soportados
- **Cloud**: AWS, Google Cloud, Azure, Heroku, DigitalOcean
- **On-Premise**: Linux, Docker Swarm, Kubernetes

### Requisitos de Producción
```
OS: Linux (Ubuntu 20.04+)
RAM: 4GB mínimo, 8GB recomendado
CPU: 2 cores mínimo, 4 recomendado
Disco: 20GB SSD
DB: PostgreSQL 14+ (separado o RDS/Cloud SQL)
Red: Puertos 80, 443, 8080
SSL/TLS: Certificado válido
```

### Despliegue Rápido

```bash
# Con Docker
docker-compose -f docker-compose.production.yml up -d

# Con script
./scripts/deploy.sh full-docker

# Verificar
./scripts/health-check.sh full
```

**Guía completa**: [DEPLOYMENT.md](DEPLOYMENT.md)

---

## 📊 Arquitectura

```
┌─────────────────────────────────────┐
│       Load Balancer / Nginx         │
└─────────────────────────────────────┘
                 │
┌─────────────────────────────────────┐
│   Application (Spring Boot)         │
│  ┌─────────┐ ┌─────────┐ ┌────────┐│
│  │ Vaadin  │ │REST API │ │WebSocket││
│  └─────────┘ └─────────┘ └────────┘│
│  ┌─────────────────────────────────┐│
│  │   Security + OAuth2             ││
│  └─────────────────────────────────┘│
│  ┌─────────────────────────────────┐│
│  │   Business Layer (Services)     ││
│  └─────────────────────────────────┘│
│  ┌─────────────────────────────────┐│
│  │   Data Layer (JPA/Repos)        ││
│  └─────────────────────────────────┘│
└─────────────────────────────────────┘
                 │
┌─────────────────────────────────────┐
│      PostgreSQL Database            │
└─────────────────────────────────────┘
```

**Detalles**: [ARCHITECTURE.md](docs/ARCHITECTURE.md)

---

## 📈 Métricas del Proyecto

- **Entidades**: 20+ modelos de dominio
- **Repositorios**: 15+ con queries optimizadas
- **Servicios**: 12+ servicios de negocio
- **API REST**: 50+ endpoints
- **Vistas**: 10+ vistas Vaadin
- **Tests**: 28+ tests
- **LOC**: ~11,000+ líneas

---

## 📞 Soporte

- **Email**: support@padelclub.pro
- **Issues**: [GitHub Issues](https://github.com/davidota/padelclub.pro/issues)
- **Docs**: [docs.padelclub.pro](https://docs.padelclub.pro)

---

## 📄 Licencia

MIT License - Ver [LICENSE](LICENSE)

---

<div align="center">

**Made with ❤️ for the Padel Community**

[⬆ Volver arriba](#padel-club-pro---sistema-de-gestión-de-torneos-de-pádel)

</div>
