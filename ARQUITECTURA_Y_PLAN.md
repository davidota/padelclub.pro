# 🎾 Aplicación de Torneos de Pádel - Arquitectura y Plan de Implementación

## 📋 Índice
1. [Resumen Ejecutivo](#resumen-ejecutivo)
2. [Arquitectura del Sistema](#arquitectura-del-sistema)
3. [Modelo de Datos Mejorado](#modelo-de-datos-mejorado)
4. [Plan de Implementación por Fases](#plan-de-implementación-por-fases)
5. [Tecnologías y Stack](#tecnologías-y-stack)
6. [Mejoras y Recomendaciones](#mejoras-y-recomendaciones)

---

## 🎯 Resumen Ejecutivo

### Estado Actual
- ✅ Spring Boot 3.4.1 + Vaadin 24.6.0
- ✅ Sistema básico de torneos y pozos
- ✅ Gestión de usuarios con roles
- ✅ Generación de enfrentamientos
- ✅ HSQLDB como base de datos

### Objetivo Final
Transformar la aplicación en una plataforma completa de gestión de torneos de pádel con:
- Inscripciones flexibles y control de cupos
- Múltiples modalidades de juego (Americano, Todos vs Todos, Eliminación)
- Sistema de pagos opcional (Stripe/PayPal)
- Estadísticas detalladas por jugador y pareja
- Notificaciones y calendario integrado
- Gamificación y modo social
- API pública y escalabilidad

---

## 🏗️ Arquitectura del Sistema

### Diagrama de Capas

```
┌─────────────────────────────────────────────────────────────┐
│                    CAPA DE PRESENTACIÓN                      │
│  ┌────────────┐  ┌────────────┐  ┌────────────────────┐    │
│  │ Vaadin UI  │  │ REST API   │  │ WebSocket/Push     │    │
│  │ (Web App)  │  │ (Mobile)   │  │ (Real-time)        │    │
│  └────────────┘  └────────────┘  └────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                    CAPA DE SEGURIDAD                         │
│  ┌──────────────────┐  ┌────────────────────────────┐       │
│  │ Spring Security  │  │ OAuth2 (Google)            │       │
│  │ JWT Tokens       │  │ Role-Based Access Control  │       │
│  └──────────────────┘  └────────────────────────────┘       │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                    CAPA DE SERVICIOS                         │
│  ┌────────────┐  ┌────────────┐  ┌────────────────────┐    │
│  │ Torneo     │  │ Inscripción│  │ Estadísticas       │    │
│  │ Service    │  │ Service    │  │ Service            │    │
│  ├────────────┤  ├────────────┤  ├────────────────────┤    │
│  │ Pozo       │  │ Pago       │  │ Notificación       │    │
│  │ Service    │  │ Service    │  │ Service            │    │
│  ├────────────┤  ├────────────┤  ├────────────────────┤    │
│  │ Match      │  │ Calendario │  │ Ranking            │    │
│  │ Service    │  │ Service    │  │ Service            │    │
│  └────────────┘  └────────────┘  └────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                    CAPA DE DATOS                             │
│  ┌────────────┐  ┌────────────┐  ┌────────────────────┐    │
│  │ Spring     │  │ Redis      │  │ File Storage       │    │
│  │ Data JPA   │  │ (Cache)    │  │ (S3/Local)         │    │
│  └────────────┘  └────────────┘  └────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                  CAPA DE PERSISTENCIA                        │
│  ┌────────────┐  ┌────────────┐  ┌────────────────────┐    │
│  │ PostgreSQL │  │ MongoDB    │  │ Elasticsearch      │    │
│  │ (Principal)│  │ (Logs/Chat)│  │ (Búsqueda)         │    │
│  └────────────┘  └────────────┘  └────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
```

### Servicios Externos

```
┌─────────────────────────────────────────────────────────────┐
│                    SERVICIOS EXTERNOS                        │
│  ┌────────────┐  ┌────────────┐  ┌────────────────────┐    │
│  │ Stripe/    │  │ SendGrid/  │  │ Google Calendar    │    │
│  │ PayPal     │  │ Twilio     │  │ API                │    │
│  │ (Pagos)    │  │ (Notif.)   │  │                    │    │
│  ├────────────┤  ├────────────┤  ├────────────────────┤    │
│  │ Firebase   │  │ AWS S3     │  │ Cloudinary         │    │
│  │ (Push)     │  │ (Storage)  │  │ (Imágenes)         │    │
│  └────────────┘  └────────────┘  └────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
```

---

## 🗄️ Modelo de Datos Mejorado

### Entidades Principales

#### 1. **User** (Usuario) - MEJORADO
```java
@Entity
public class User extends AbstractEntity {
    private String username;
    private String email; // ⭐ NUEVO
    private String googleId; // ⭐ NUEVO para OAuth
    private String name;
    private String apellido;
    private String hashedPassword;
    private String telefono; // ⭐ NUEVO
    private LocalDate fechaNacimiento; // ⭐ NUEVO
    private String bio; // ⭐ NUEVO para perfil social
    private byte[] profilePicture;
    private String nivel; // ⭐ NUEVO (Principiante, Intermedio, Avanzado, Pro)

    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Role> roles;

    @OneToMany(mappedBy = "jugador")
    private List<Inscripcion> inscripciones; // ⭐ NUEVO

    @OneToMany(mappedBy = "jugador")
    private List<EstadisticaJugador> estadisticas; // ⭐ NUEVO

    private Boolean activo = true;
    private LocalDateTime ultimoAcceso; // ⭐ NUEVO
}
```

#### 2. **Torneo** (Tournament) - MEJORADO
```java
@Entity
public class Torneo extends AbstractEntity {
    private String nombre;
    private String descripcion; // ⭐ NUEVO

    @Enumerated(EnumType.STRING)
    private TipoTorneo tipo;

    @Enumerated(EnumType.STRING)
    private EstadoTorneo estado;

    private LocalDate fechaInicio; // ⭐ NUEVO
    private LocalDate fechaFin; // ⭐ NUEVO
    private LocalDateTime fechaInicioInscripcion; // ⭐ NUEVO
    private LocalDateTime fechaCierreInscripcion; // ⭐ NUEVO

    private Integer cupoMaximo; // ⭐ NUEVO - máximo de parejas
    private Boolean esGratuito = true; // ⭐ NUEVO
    private BigDecimal precioInscripcion; // ⭐ NUEVO

    private String ubicacion; // ⭐ NUEVO
    private String imagenUrl; // ⭐ NUEVO

    @ManyToOne
    private Club club; // ⭐ NUEVO - soporte multiclub

    @ManyToOne
    private User organizador; // ⭐ NUEVO

    @OneToMany(mappedBy = "torneo")
    private List<Pozo> pozos;

    @OneToMany(mappedBy = "torneo")
    private List<Inscripcion> inscripciones; // ⭐ NUEVO

    @OneToMany(mappedBy = "torneo")
    private List<Premio> premios; // ⭐ NUEVO

    private Integer numeroEnfrentamientos;
    private Integer juegosPorEnfrentamiento;
    private Integer numeroDeVueltas;
    private Integer numeroEnfrentamientosSimultaneos;

    // Configuración de privacidad
    private Boolean esPublico = true; // ⭐ NUEVO
    private Boolean permitirEspectadores = true; // ⭐ NUEVO
}
```

#### 3. **Pozo** (Pool/Group) - MEJORADO
```java
@Entity
public class Pozo extends AbstractEntity {
    private String nombre;

    @Enumerated(EnumType.STRING)
    private ModalidadPozo modalidad; // ⭐ NUEVO enum
    // AMERICANO, TODOS_CONTRA_TODOS, ELIMINACION_DIRECTA

    @ManyToOne
    private Torneo torneo;

    private Integer cupoMaximo; // ⭐ NUEVO
    private Boolean esGratuito = true; // ⭐ NUEVO
    private BigDecimal precioInscripcion; // ⭐ NUEVO

    private LocalDateTime fechaInicioInscripcion; // ⭐ NUEVO
    private LocalDateTime fechaCierreInscripcion; // ⭐ NUEVO

    private LocalDate fechaInicio; // ⭐ NUEVO
    private LocalDate fechaFin; // ⭐ NUEVO

    @Enumerated(EnumType.STRING)
    private EstadoPozo estado; // ⭐ NUEVO
    // INSCRIPCION_ABIERTA, INSCRIPCION_CERRADA, EN_CURSO, FINALIZADO

    @OneToMany(mappedBy = "pozo")
    private List<Equipo> equipos;

    @OneToMany(mappedBy = "pozo")
    private List<Enfrentamiento> enfrentamientos;

    @OneToMany(mappedBy = "pozo")
    private List<InscripcionPozo> inscripciones; // ⭐ NUEVO

    private Integer numeroDeRondas; // ⭐ NUEVO para americano
    private Boolean permiteCambiosParejas; // ⭐ NUEVO para americano
}
```

#### 4. **Inscripcion** (Registration) - ⭐ NUEVO
```java
@Entity
public class Inscripcion extends AbstractEntity {
    @ManyToOne
    private User jugador;

    @ManyToOne
    private Torneo torneo;

    private LocalDateTime fechaInscripcion;

    @Enumerated(EnumType.STRING)
    private EstadoInscripcion estado;
    // PENDIENTE, CONFIRMADA, PAGADA, CANCELADA

    @ManyToOne
    private User pareja; // Pareja preferida (puede ser null)

    // Disponibilidad por fechas
    @ElementCollection
    private List<LocalDate> fechasDisponibles; // ⭐ NUEVO

    @OneToOne(mappedBy = "inscripcion")
    private Pago pago; // ⭐ NUEVO

    private String comentarios;
}
```

#### 5. **InscripcionPozo** - ⭐ NUEVO
```java
@Entity
public class InscripcionPozo extends AbstractEntity {
    @ManyToOne
    private User jugador;

    @ManyToOne
    private Pozo pozo;

    @ManyToOne
    private Inscripcion inscripcionTorneo; // Referencia

    private LocalDateTime fechaInscripcion;

    @Enumerated(EnumType.STRING)
    private EstadoInscripcion estado;

    @ManyToOne
    private Equipo equipo; // Asignado después

    @OneToOne
    private Pago pago; // Si el pozo tiene costo adicional
}
```

#### 6. **Equipo** (Team) - MEJORADO
```java
@Entity
public class Equipo extends AbstractEntity {
    private String nombreEquipo;

    @ManyToOne
    private User participante1;

    @ManyToOne
    private User participante2;

    @ManyToOne
    private Pozo pozo;

    @ManyToOne
    private Torneo torneo; // ⭐ NUEVO

    // Para modalidad americano
    private Boolean esTemporalAmericano = false; // ⭐ NUEVO
    private Integer rondaAmericano; // ⭐ NUEVO

    @OneToMany(mappedBy = "equipo")
    private List<EstadisticaEquipo> estadisticas; // ⭐ NUEVO
}
```

#### 7. **Enfrentamiento** (Match) - MEJORADO
```java
@Entity
public class Enfrentamiento extends AbstractEntity {
    @ManyToOne
    private Torneo torneo;

    @ManyToOne
    private Pozo pozo;

    @ManyToOne
    private Equipo equipo1;

    @ManyToOne
    private Equipo equipo2;

    private Integer ronda; // ⭐ NUEVO
    private Integer pista; // ⭐ NUEVO (court number)

    private LocalDateTime fechaProgramada; // ⭐ NUEVO
    private LocalDateTime fechaJugado; // ⭐ NUEVO

    @Enumerated(EnumType.STRING)
    private EstadoEnfrentamiento estado; // ⭐ NUEVO
    // PROGRAMADO, EN_CURSO, FINALIZADO, CANCELADO, POSPUESTO

    // Resultado mejorado
    private Integer setsEquipo1; // ⭐ NUEVO
    private Integer setsEquipo2; // ⭐ NUEVO

    @ElementCollection
    private List<Set> sets; // ⭐ NUEVO - Lista de sets

    @ManyToOne
    private User arbitro; // ⭐ NUEVO

    private String observaciones;

    // Para eliminación directa
    private Integer numeroFase; // ⭐ NUEVO (32avos, 16avos, 8vos, 4tos, semis, final)

    @Deprecated
    private String resultado; // Mantener por compatibilidad

    @Deprecated
    @ElementCollection
    private List<String> juegos; // Mantener por compatibilidad
}

@Embeddable
class Set {
    private Integer juegosEquipo1;
    private Integer juegosEquipo2;
    private Integer tiebreakEquipo1; // Si aplica
    private Integer tiebreakEquipo2;
}
```

#### 8. **EstadisticaJugador** - ⭐ NUEVO
```java
@Entity
public class EstadisticaJugador extends AbstractEntity {
    @ManyToOne
    private User jugador;

    @ManyToOne
    private Torneo torneo; // Null = estadística global

    @ManyToOne
    private Pozo pozo; // Null = estadística de torneo completo

    // Estadísticas generales
    private Integer partidosJugados = 0;
    private Integer partidosGanados = 0;
    private Integer partidosPerdidos = 0;
    private Integer partidosEmpatados = 0;

    private Integer setsGanados = 0;
    private Integer setsPerdidos = 0;

    private Integer juegosGanados = 0;
    private Integer juegosPerdidos = 0;

    private Integer puntosAFavor = 0;
    private Integer puntosEnContra = 0;

    // Métricas avanzadas
    private Double porcentajeVictorias;
    private Double rendimiento; // Calculado con algoritmo
    private Integer racha; // Victorias consecutivas

    private Integer posicionFinal; // En el torneo/pozo

    @Enumerated(EnumType.STRING)
    private TipoPremio premio; // ORO, PLATA, BRONCE, null

    private Integer puntos = 0; // Puntos acumulados

    private LocalDateTime ultimaActualizacion;
}
```

#### 9. **EstadisticaEquipo** (Team Stats) - ⭐ NUEVO
```java
@Entity
public class EstadisticaEquipo extends AbstractEntity {
    @ManyToOne
    private Equipo equipo;

    @ManyToOne
    private Torneo torneo;

    @ManyToOne
    private Pozo pozo;

    // Similar a EstadisticaJugador pero para equipos
    private Integer partidosJugados = 0;
    private Integer partidosGanados = 0;
    private Integer partidosPerdidos = 0;

    private Integer setsGanados = 0;
    private Integer setsPerdidos = 0;

    private Integer juegosGanados = 0;
    private Integer juegosPerdidos = 0;

    private Integer puntos = 0;
    private Integer posicion;

    private Double rendimiento;
}
```

#### 10. **Pago** (Payment) - ⭐ NUEVO
```java
@Entity
public class Pago extends AbstractEntity {
    @OneToOne
    private Inscripcion inscripcion;

    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    private MetodoPago metodoPago;
    // STRIPE, PAYPAL, EFECTIVO, TRANSFERENCIA

    @Enumerated(EnumType.STRING)
    private EstadoPago estado;
    // PENDIENTE, COMPLETADO, FALLIDO, REEMBOLSADO

    private String transactionId; // ID de Stripe/PayPal
    private String paymentIntentId;

    private LocalDateTime fechaPago;
    private LocalDateTime fechaReembolso;

    private String detalles;
}
```

#### 11. **Notificacion** - ⭐ NUEVO
```java
@Entity
public class Notificacion extends AbstractEntity {
    @ManyToOne
    private User destinatario;

    private String titulo;
    private String mensaje;

    @Enumerated(EnumType.STRING)
    private TipoNotificacion tipo;
    // PARTIDO_PROGRAMADO, RESULTADO, CAMBIO_CALENDARIO,
    // INSCRIPCION_CONFIRMADA, PAGO_EXITOSO, PREMIO, RECORDATORIO

    @Enumerated(EnumType.STRING)
    private CanalNotificacion canal;
    // EMAIL, PUSH, SMS, IN_APP

    private Boolean leida = false;
    private LocalDateTime fechaEnvio;
    private LocalDateTime fechaLeida;

    @ManyToOne
    private Enfrentamiento enfrentamiento; // Si aplica

    @ManyToOne
    private Torneo torneo; // Si aplica
}
```

#### 12. **Premio** - ⭐ NUEVO
```java
@Entity
public class Premio extends AbstractEntity {
    @ManyToOne
    private Torneo torneo;

    @ManyToOne
    private Pozo pozo;

    @Enumerated(EnumType.STRING)
    private TipoPremio tipo;
    // ORO, PLATA, BRONCE, FAIR_PLAY, MVP, GOLEADOR

    private String nombre;
    private String descripcion;

    @ManyToOne
    private Equipo equipoGanador;

    @ManyToOne
    private User jugadorGanador; // Para premios individuales

    private Integer posicion; // 1, 2, 3, etc.
}
```

#### 13. **Club** - ⭐ NUEVO (Soporte multiclub)
```java
@Entity
public class Club extends AbstractEntity {
    private String nombre;
    private String direccion;
    private String ciudad;
    private String telefono;
    private String email;
    private String website;

    private byte[] logo;

    @ManyToOne
    private User administrador;

    @OneToMany(mappedBy = "club")
    private List<Torneo> torneos;

    @OneToMany(mappedBy = "club")
    private List<Pista> pistas;

    private Boolean activo = true;
}
```

#### 14. **Pista** (Court) - ⭐ NUEVO
```java
@Entity
public class Pista extends AbstractEntity {
    private String nombre;
    private Integer numero;

    @ManyToOne
    private Club club;

    @Enumerated(EnumType.STRING)
    private TipoPista tipo;
    // INDOOR, OUTDOOR, CRISTAL, MURO

    private Boolean disponible = true;

    @OneToMany(mappedBy = "pista")
    private List<HorarioDisponible> horarios;
}
```

#### 15. **HorarioDisponible** - ⭐ NUEVO
```java
@Entity
public class HorarioDisponible extends AbstractEntity {
    @ManyToOne
    private Pista pista;

    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;

    @ManyToOne
    private Enfrentamiento enfrentamiento; // Si está ocupada

    private Boolean disponible = true;
}
```

#### 16. **Logro** (Achievement) - ⭐ NUEVO (Gamificación)
```java
@Entity
public class Logro extends AbstractEntity {
    private String nombre;
    private String descripcion;
    private String icono;

    @Enumerated(EnumType.STRING)
    private TipoLogro tipo;
    // PARTIDOS_JUGADOS, VICTORIAS, RACHA, TORNEOS_GANADOS, etc.

    private Integer meta; // Ej: 10 partidos, 5 victorias

    @ManyToMany
    private List<User> jugadores; // Jugadores que lo han desbloqueado
}
```

### Enumeraciones

```java
// Nuevas enumeraciones
public enum ModalidadPozo {
    AMERICANO,
    TODOS_CONTRA_TODOS,
    ELIMINACION_DIRECTA
}

public enum EstadoPozo {
    INSCRIPCION_ABIERTA,
    INSCRIPCION_CERRADA,
    EN_CURSO,
    FINALIZADO,
    CANCELADO
}

public enum EstadoInscripcion {
    PENDIENTE,
    CONFIRMADA,
    PAGADA,
    CANCELADA,
    RECHAZADA
}

public enum EstadoEnfrentamiento {
    PROGRAMADO,
    EN_CURSO,
    FINALIZADO,
    CANCELADO,
    POSPUESTO,
    WALKOVER
}

public enum MetodoPago {
    STRIPE,
    PAYPAL,
    EFECTIVO,
    TRANSFERENCIA,
    BIZUM
}

public enum EstadoPago {
    PENDIENTE,
    PROCESANDO,
    COMPLETADO,
    FALLIDO,
    REEMBOLSADO,
    CANCELADO
}

public enum TipoNotificacion {
    PARTIDO_PROGRAMADO,
    RESULTADO,
    CAMBIO_CALENDARIO,
    INSCRIPCION_CONFIRMADA,
    PAGO_EXITOSO,
    PREMIO,
    RECORDATORIO,
    MENSAJE,
    ACTUALIZACION_TORNEO
}

public enum CanalNotificacion {
    EMAIL,
    PUSH,
    SMS,
    IN_APP
}

public enum TipoPremio {
    ORO,
    PLATA,
    BRONCE,
    FAIR_PLAY,
    MVP,
    MEJOR_PAREJA,
    GOLEADOR
}

public enum TipoPista {
    INDOOR,
    OUTDOOR,
    CRISTAL,
    MURO,
    PANORAMICA
}

public enum TipoLogro {
    PARTIDOS_JUGADOS,
    VICTORIAS,
    RACHA_VICTORIAS,
    TORNEOS_GANADOS,
    PRIMERA_VICTORIA,
    PRIMERA_MEDALLA,
    VETERANO,
    INVICTO
}
```

---

## 📅 Plan de Implementación por Fases

### 🚀 FASE 1: Fundamentos y Mejoras Core (2 semanas)

#### 1.1 Mejora del Modelo de Datos
- [ ] Crear nuevas entidades (Inscripcion, Pago, Notificacion, etc.)
- [ ] Migrar a PostgreSQL (recomendado para producción)
- [ ] Actualizar entidades existentes con nuevos campos
- [ ] Crear scripts de migración de datos
- [ ] Agregar validaciones con Bean Validation

#### 1.2 Sistema de Inscripciones
- [ ] Crear `InscripcionService` y `InscripcionRepository`
- [ ] Implementar lógica de inscripción flexible
- [ ] Control de cupos y fechas de inscripción
- [ ] Inscripción en torneo + múltiples pozos
- [ ] Vista de inscripción para jugadores

#### 1.3 Mejora del Sistema de Partidos
- [ ] Actualizar `Enfrentamiento` con nuevo modelo de sets
- [ ] Crear vista para registro de resultados
- [ ] Implementar cálculo automático de ganador
- [ ] Sistema de validación de resultados

**Entregables Fase 1:**
- ✅ Modelo de datos completo
- ✅ Sistema de inscripciones funcional
- ✅ Registro de resultados mejorado
- ✅ Base de datos migrada a PostgreSQL

---

### 🎯 FASE 2: Modalidades de Torneo (2 semanas)

#### 2.1 Modalidad Americano
- [ ] Algoritmo de generación de enfrentamientos con rotación
- [ ] Sistema de rondas con cambio de parejas
- [ ] Cálculo de ranking individual (no por equipo)
- [ ] Vista específica para americano

#### 2.2 Modalidad Todos Contra Todos
- [ ] Generación de calendario completo
- [ ] Sistema de puntos (victoria=3, empate=1, derrota=0)
- [ ] Tabla de clasificación en tiempo real
- [ ] Vista de tabla de posiciones

#### 2.3 Modalidad Eliminación Directa
- [ ] Generación de bracket/cuadro
- [ ] Sistema de fases (32avos, 16avos, cuartos, semis, final)
- [ ] Vista de bracket con navegación
- [ ] Gestión de walkovers y byes

**Entregables Fase 2:**
- ✅ 3 modalidades implementadas
- ✅ Generación automática de enfrentamientos
- ✅ Vistas específicas para cada modalidad

---

### 📊 FASE 3: Estadísticas y Rankings (1.5 semanas)

#### 3.1 Sistema de Estadísticas
- [ ] Crear `EstadisticasService`
- [ ] Cálculo automático tras cada partido
- [ ] Estadísticas por jugador (global, torneo, pozo)
- [ ] Estadísticas por pareja
- [ ] Historial completo

#### 3.2 Sistema de Rankings
- [ ] Algoritmo de ranking dinámico
- [ ] Top 3 con badges (oro, plata, bronce)
- [ ] Ranking global y por torneo/pozo
- [ ] Vista de leaderboard

#### 3.3 Dashboard de Jugador
- [ ] Perfil de jugador con estadísticas
- [ ] Gráficos de rendimiento
- [ ] Historial de partidos
- [ ] Torneos participados

**Entregables Fase 3:**
- ✅ Sistema completo de estadísticas
- ✅ Rankings dinámicos
- ✅ Dashboard de jugador

---

### 💰 FASE 4: Sistema de Pagos (1.5 semanas)

#### 4.1 Integración con Stripe
- [ ] Configurar Stripe SDK
- [ ] Crear `PagoService`
- [ ] Implementar Payment Intent API
- [ ] Webhooks para confirmación de pagos
- [ ] Gestión de reembolsos

#### 4.2 Flujo de Pago en Inscripción
- [ ] Vista de checkout
- [ ] Integración con formulario de inscripción
- [ ] Confirmación de pago
- [ ] Envío de recibos por email

#### 4.3 Panel de Administración de Pagos
- [ ] Vista de pagos por torneo
- [ ] Reportes de ingresos
- [ ] Gestión de reembolsos
- [ ] Exportación de datos financieros

**Entregables Fase 4:**
- ✅ Integración con Stripe
- ✅ Flujo completo de pagos
- ✅ Panel de administración financiera

---

### 🔔 FASE 5: Notificaciones y Calendario (1 semana)

#### 5.1 Sistema de Notificaciones
- [ ] Crear `NotificacionService`
- [ ] Integración con SendGrid (email)
- [ ] Integración con Firebase (push)
- [ ] Notificaciones in-app
- [ ] Plantillas de notificaciones

#### 5.2 Tipos de Notificaciones
- [ ] Partido programado
- [ ] Cambios en calendario
- [ ] Resultados de partidos
- [ ] Confirmación de inscripción
- [ ] Recordatorios

#### 5.3 Calendario
- [ ] Vista de calendario de partidos
- [ ] Integración con Google Calendar API
- [ ] Exportación a .ics
- [ ] Sincronización bidireccional

**Entregables Fase 5:**
- ✅ Sistema completo de notificaciones
- ✅ Vista de calendario
- ✅ Integración con Google Calendar

---

### 🔐 FASE 6: Autenticación Google OAuth2 (1 semana)

#### 6.1 Configuración OAuth2
- [ ] Configurar Google Cloud Console
- [ ] Agregar dependencias Spring Security OAuth2
- [ ] Configurar `SecurityConfiguration` para OAuth2

#### 6.2 Flujo de Login
- [ ] Actualizar `LoginView` con botón Google
- [ ] Implementar `CustomOAuth2UserService`
- [ ] Mapeo de usuario Google → User entity
- [ ] Auto-registro de nuevos usuarios

#### 6.3 Compatibilidad
- [ ] Mantener login con username/password
- [ ] Vincular cuentas existentes
- [ ] Gestión de perfil unificada

**Entregables Fase 6:**
- ✅ Login con Google funcional
- ✅ Compatibilidad con sistema actual
- ✅ Auto-registro de usuarios

---

### 🎨 FASE 7: UI/UX y Funcionalidades Avanzadas (2 semanas)

#### 7.1 Mejoras de UI
- [ ] Diseño responsive para móviles
- [ ] Tema personalizado (colores corporativos)
- [ ] Animaciones y transiciones
- [ ] Iconografía consistente

#### 7.2 Perfiles Sociales
- [ ] Perfil público de jugador
- [ ] Sistema de fotos y bio
- [ ] Logros y medallas
- [ ] Seguir jugadores

#### 7.3 Modo Espectador
- [ ] Vista pública de torneos
- [ ] Resultados en tiempo real
- [ ] Rankings públicos
- [ ] Compartir en redes sociales

#### 7.4 Exportación y Reportes
- [ ] Generación de PDF con iText
- [ ] Reportes de torneo
- [ ] Rankings en PDF
- [ ] Certificados de participación

**Entregables Fase 7:**
- ✅ UI mejorada y responsive
- ✅ Perfiles sociales
- ✅ Modo espectador
- ✅ Sistema de exportación

---

### 🏆 FASE 8: Gamificación y Premios (1 semana)

#### 8.1 Sistema de Premios
- [ ] Configuración de premios por torneo
- [ ] Asignación automática (oro, plata, bronce)
- [ ] Premios especiales (MVP, Fair Play)
- [ ] Vista de podio

#### 8.2 Logros y Badges
- [ ] Sistema de logros desbloqueables
- [ ] Badges virtuales
- [ ] Notificaciones de logros
- [ ] Galería de logros en perfil

#### 8.3 Puntos y Niveles
- [ ] Sistema de puntos por participación
- [ ] Niveles de jugador
- [ ] Rewards por nivel
- [ ] Tabla de puntos global

**Entregables Fase 8:**
- ✅ Sistema de premios
- ✅ Gamificación completa
- ✅ Motivación para jugadores

---

### 🌐 FASE 9: API REST y Escalabilidad (1.5 semanas)

#### 9.1 API REST
- [ ] Crear controladores REST
- [ ] Documentación con Swagger/OpenAPI
- [ ] Versionado de API (v1, v2)
- [ ] Rate limiting

#### 9.2 Endpoints Principales
```
POST   /api/v1/torneos
GET    /api/v1/torneos
GET    /api/v1/torneos/{id}
PUT    /api/v1/torneos/{id}
DELETE /api/v1/torneos/{id}

POST   /api/v1/inscripciones
GET    /api/v1/jugadores/{id}/estadisticas
GET    /api/v1/torneos/{id}/ranking
POST   /api/v1/enfrentamientos/{id}/resultado
```

#### 9.3 Seguridad API
- [ ] JWT tokens
- [ ] API keys para terceros
- [ ] OAuth2 para apps externas
- [ ] CORS configurado

#### 9.4 Cache y Performance
- [ ] Redis para cache
- [ ] Cache de rankings
- [ ] Cache de estadísticas
- [ ] Optimización de queries

**Entregables Fase 9:**
- ✅ API REST completa y documentada
- ✅ Seguridad robusta
- ✅ Performance optimizada

---

### 🚀 FASE 10: Funcionalidades Premium (2 semanas)

#### 10.1 Chat Interno
- [ ] WebSocket con Spring
- [ ] Chat entre jugadores
- [ ] Chat de torneo
- [ ] Notificaciones de mensajes

#### 10.2 Streaming
- [ ] Integración con YouTube/Twitch API
- [ ] Enlace de partidos importantes
- [ ] Vista de streaming en app

#### 10.3 Machine Learning (opcional)
- [ ] Análisis de rendimiento
- [ ] Sugerencias de emparejamiento
- [ ] Predicción de resultados
- [ ] Recomendación de nivel

#### 10.4 Modo Offline
- [ ] Service Worker
- [ ] IndexedDB para datos locales
- [ ] Sincronización al reconectar
- [ ] PWA completo

**Entregables Fase 10:**
- ✅ Chat funcional
- ✅ Streaming integrado
- ✅ ML básico (si aplica)
- ✅ Modo offline

---

### 🏢 FASE 11: Soporte Multiclub (1 semana)

#### 11.1 Entidad Club
- [ ] Crear modelo de Club
- [ ] Gestión de clubes
- [ ] Administración por club
- [ ] Logo y branding

#### 11.2 Gestión de Pistas
- [ ] Modelo de Pista
- [ ] Horarios disponibles
- [ ] Asignación a partidos
- [ ] Vista de ocupación

#### 11.3 Panel de Administración de Club
- [ ] Dashboard de club
- [ ] Estadísticas del club
- [ ] Gestión de torneos propios
- [ ] Usuarios del club

**Entregables Fase 11:**
- ✅ Soporte para múltiples clubes
- ✅ Gestión de pistas
- ✅ Panel de administración

---

### 🧪 FASE 12: Testing, Documentación y Deploy (1.5 semanas)

#### 12.1 Testing
- [ ] Unit tests (JUnit 5)
- [ ] Integration tests (Spring Test)
- [ ] E2E tests (Playwright)
- [ ] Coverage > 80%

#### 12.2 Documentación
- [ ] Documentación técnica
- [ ] Manual de usuario
- [ ] Guías de administración
- [ ] API documentation

#### 12.3 Deploy
- [ ] Configuración de producción
- [ ] Docker y Docker Compose
- [ ] CI/CD con GitHub Actions
- [ ] Deploy en cloud (AWS/GCP/Azure)

#### 12.4 Monitoreo
- [ ] Logs con SLF4J
- [ ] Métricas con Actuator
- [ ] Alertas
- [ ] Dashboard de monitoreo

**Entregables Fase 12:**
- ✅ Tests completos
- ✅ Documentación completa
- ✅ App en producción
- ✅ Monitoreo activo

---

## 🛠️ Tecnologías y Stack

### Backend
- **Framework**: Spring Boot 3.4.1
- **Java**: 17 (LTS)
- **ORM**: Spring Data JPA + Hibernate
- **Database**: PostgreSQL 15+ (producción), HSQLDB (desarrollo)
- **Security**: Spring Security + OAuth2
- **Payments**: Stripe SDK
- **Email**: SendGrid
- **Push**: Firebase Cloud Messaging
- **PDF**: iText 7
- **Cache**: Redis
- **Testing**: JUnit 5, Mockito, TestContainers

### Frontend
- **UI Framework**: Vaadin 24.6.0
- **Components**: Vaadin Flow Components
- **Styling**: Custom CSS + Vaadin Themes
- **Charts**: Vaadin Charts / Apache ECharts
- **Calendar**: FullCalendar integration
- **Icons**: Line Awesome

### Infraestructura
- **Containerization**: Docker + Docker Compose
- **CI/CD**: GitHub Actions
- **Cloud**: AWS (EC2, RDS, S3, CloudFront)
- **CDN**: CloudFlare
- **Monitoring**: Prometheus + Grafana
- **Logs**: ELK Stack (Elasticsearch, Logstash, Kibana)

### APIs Externas
- **Google OAuth2**: Autenticación
- **Google Calendar API**: Sincronización de calendario
- **Stripe API**: Pagos
- **SendGrid API**: Emails transaccionales
- **Firebase**: Push notifications
- **Cloudinary**: Gestión de imágenes (opcional)

---

## 💡 Mejoras y Recomendaciones

### Mejoras Propuestas

#### 1. **Sistema de Niveles de Jugador**
Implementar un sistema ELO o similar para asignar niveles dinámicos a jugadores basados en su rendimiento histórico.

#### 2. **Predicción de Disponibilidad**
Machine Learning para predecir qué jugadores estarán disponibles en ciertas fechas basándose en patrones históricos.

#### 3. **Marketplace de Pistas**
Permitir que clubes ofrezcan sus pistas disponibles y jugadores las reserven para práctica.

#### 4. **Sistema de Patrocinios**
Permitir que empresas patrocinen torneos y aparecer en la app.

#### 5. **Transmisión en Vivo**
Integración con plataformas de streaming para transmitir partidos finales.

#### 6. **App Móvil Nativa**
Desarrollar app iOS/Android con Flutter o React Native usando la API REST.

#### 7. **Análisis de Video**
Subir videos de partidos y análisis con IA (detección de jugadas, estadísticas avanzadas).

#### 8. **Torneo Virtual**
Torneos online donde los jugadores reportan resultados de partidos jugados en diferentes ubicaciones.

### Recomendaciones Técnicas

#### Arquitectura
- **Migrar a PostgreSQL**: HSQLDB es bueno para desarrollo pero PostgreSQL es mejor para producción
- **Implementar CQRS**: Separar comandos (escritura) de queries (lectura) para mejor performance
- **Event Sourcing**: Para tener historial completo de cambios en torneos
- **Microservicios** (futuro): Separar en servicios: Torneos, Pagos, Notificaciones, Estadísticas

#### Seguridad
- **HTTPS obligatorio**: Certificado SSL/TLS
- **Rate Limiting**: Prevenir abuso de API
- **2FA**: Autenticación de dos factores para administradores
- **Auditoría**: Log de todas las acciones críticas

#### Performance
- **CDN para assets estáticos**: CloudFlare o CloudFront
- **Database indexing**: Índices en columnas frecuentemente consultadas
- **Query optimization**: Usar Hibernate Statistics para detectar N+1 queries
- **Lazy loading**: Para relaciones grandes

#### UX
- **Progressive Web App**: Instalar app en dispositivos móviles
- **Dark mode**: Tema oscuro opcional
- **Internacionalización**: Soporte multi-idioma (español, inglés, portugués)
- **Accesibilidad**: WCAG 2.1 AA compliance

---

## 📈 Estimación de Tiempo Total

| Fase | Duración | Complejidad |
|------|----------|-------------|
| FASE 1: Fundamentos | 2 semanas | Alta |
| FASE 2: Modalidades | 2 semanas | Alta |
| FASE 3: Estadísticas | 1.5 semanas | Media |
| FASE 4: Pagos | 1.5 semanas | Alta |
| FASE 5: Notificaciones | 1 semana | Media |
| FASE 6: OAuth2 | 1 semana | Media |
| FASE 7: UI/UX | 2 semanas | Alta |
| FASE 8: Gamificación | 1 semana | Baja |
| FASE 9: API REST | 1.5 semanas | Media |
| FASE 10: Premium | 2 semanas | Alta |
| FASE 11: Multiclub | 1 semana | Media |
| FASE 12: Testing | 1.5 semanas | Alta |
| **TOTAL** | **18 semanas** | **(~4.5 meses)** |

**Nota**: Tiempos estimados para 1 desarrollador full-time. Con un equipo de 2-3 personas, se puede reducir a 2-3 meses.

---

## 🎯 MVP (Minimum Viable Product)

Para lanzar una versión funcional rápida, priorizar:

### MVP Core (6 semanas)
- ✅ FASE 1: Fundamentos y mejoras core
- ✅ FASE 2: Modalidades de torneo (al menos 1: Todos contra todos)
- ✅ FASE 3: Estadísticas y rankings básicos
- ✅ FASE 7: UI mejorada y responsive
- ✅ Registro de resultados
- ✅ Inscripciones simples (sin pagos)

### Post-MVP (siguiente iteración)
- FASE 4: Pagos
- FASE 6: Google OAuth2
- FASE 5: Notificaciones
- Resto de fases

---

## 🚀 Próximos Pasos Inmediatos

### ¿Qué hacer ahora?

1. **Validar el plan**: Revisar y aprobar las fases propuestas
2. **Decidir alcance**: ¿MVP o desarrollo completo?
3. **Priorizar fases**: Reordenar según necesidades del negocio
4. **Configurar entorno**: PostgreSQL, Stripe, APIs externas
5. **Comenzar FASE 1**: Migración del modelo de datos

### Primera Implementación Sugerida

Si decides continuar, empezaré por:
1. ✅ Crear todas las nuevas entidades
2. ✅ Actualizar repositorios
3. ✅ Migrar datos existentes
4. ✅ Crear `InscripcionService`
5. ✅ Implementar primera vista de inscripción

---

## 📞 Preguntas a Resolver

Antes de comenzar, necesitamos definir:

1. **Alcance**: ¿MVP o desarrollo completo?
2. **Prioridad**: ¿Qué funcionalidades son críticas para el lanzamiento?
3. **Base de datos**: ¿Migramos a PostgreSQL ahora o después?
4. **Pagos**: ¿Stripe o también PayPal?
5. **Notificaciones**: ¿Email obligatorio o también push?
6. **Autenticación**: ¿Solo Google o mantener username/password?
7. **Hosting**: ¿Cloud específico? (AWS, GCP, Azure)
8. **Presupuesto**: ¿Hay limitaciones para servicios de pago?

---

## ✅ Conclusión

Este plan proporciona una hoja de ruta completa para transformar la aplicación actual en una plataforma robusta de gestión de torneos de pádel con todas las funcionalidades solicitadas y más.

**El código actual es una excelente base** con arquitectura limpia y separación de responsabilidades. Con las mejoras propuestas, la aplicación será:
- 🚀 Escalable
- 🔒 Segura
- 💰 Monetizable
- 📱 Accesible desde cualquier dispositivo
- 🎮 Gamificada y atractiva
- 📊 Rica en estadísticas
- 🌐 Lista para crecer internacionalmente

**¿Comenzamos con la implementación?** 🎾
