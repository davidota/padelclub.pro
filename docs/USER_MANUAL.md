# Manual de Usuario - Padel Club Pro

## Tabla de Contenidos

- [Introducción](#introducción)
- [Primeros Pasos](#primeros-pasos)
- [Roles de Usuario](#roles-de-usuario)
- [Manual para Jugadores](#manual-para-jugadores)
- [Manual para Administradores](#manual-para-administradores)
- [Manual para Organizadores](#manual-para-organizadores)
- [Características Avanzadas](#características-avanzadas)
- [Preguntas Frecuentes](#preguntas-frecuentes)
- [Solución de Problemas](#solución-de-problemas)

---

## Introducción

Bienvenido a **Padel Club Pro**, el sistema completo de gestión de torneos de pádel. Este manual le guiará a través de todas las funcionalidades de la aplicación.

### ¿Qué puedo hacer con Padel Club Pro?

- Gestionar torneos de pádel de forma profesional
- Inscribirse y participar en competiciones
- Hacer seguimiento de estadísticas personales
- Comunicarse con otros jugadores en tiempo real
- Ganar logros y subir en los rankings
- Gestionar múltiples clubes (administradores)
- Procesar pagos de forma segura

---

## Primeros Pasos

### 1. Acceso a la Aplicación

**URL**: `http://localhost:8080` (desarrollo) o `https://your-domain.com` (producción)

### 2. Crear una Cuenta

#### Opción A: Registro Manual

1. Click en "Registrarse"
2. Completar formulario:
   - **Usuario**: Nombre de usuario único
   - **Email**: Correo electrónico válido
   - **Contraseña**: Mínimo 8 caracteres
   - **Nombre y Apellido**: Información personal
3. Aceptar términos y condiciones
4. Click en "Crear Cuenta"
5. Verificar email (si aplica)

#### Opción B: Login con Google

1. Click en "Continuar con Google"
2. Seleccionar cuenta de Google
3. Autorizar permisos solicitados
4. Se crea automáticamente la cuenta

### 3. Completar Perfil

Después del primer login:

1. Ir a "Mi Perfil"
2. Completar información:
   - **Nivel de habilidad**: Principiante, Intermedio, Avanzado, Experto, Profesional
   - **Fecha de nacimiento**
   - **Teléfono** (opcional)
   - **Biografía** (opcional)
   - **Foto de perfil** (opcional)
3. Guardar cambios

---

## Roles de Usuario

### PLAYER (Jugador)

**Permisos**:
- ✅ Ver torneos
- ✅ Inscribirse en torneos
- ✅ Ver estadísticas propias
- ✅ Participar en chats
- ✅ Ganar logros
- ❌ Crear torneos
- ❌ Gestionar clubes

### OPERATOR (Organizador)

**Permisos**:
- ✅ Todos los permisos de PLAYER
- ✅ Crear y gestionar torneos
- ✅ Gestionar inscripciones
- ✅ Registrar resultados
- ✅ Gestionar pistas
- ❌ Administrar clubes

### ADMIN (Administrador)

**Permisos**:
- ✅ Todos los permisos de OPERATOR
- ✅ Crear y gestionar clubes
- ✅ Gestionar usuarios
- ✅ Configurar sistema
- ✅ Ver todas las estadísticas
- ✅ Acceso completo al sistema

---

## Manual para Jugadores

### Ver Torneos Disponibles

1. **Página Principal**: Ver torneos destacados
2. **Menú "Torneos"**: Lista completa de torneos
3. **Filtros disponibles**:
   - Por fecha
   - Por formato (Americano, Liga, Eliminación)
   - Por nivel
   - Por estado (Abierto, En curso, Finalizado)
   - Por club

### Inscribirse en un Torneo

#### Torneo Gratuito

1. Click en el torneo deseado
2. Revisar información:
   - Fechas
   - Formato
   - Nivel requerido
   - Número de plazas
3. Click en "Inscribirse"
4. Seleccionar pareja (si aplica)
5. Confirmar inscripción

#### Torneo de Pago

1. Seguir pasos de torneo gratuito
2. Click en "Inscribirse y Pagar"
3. Ingresar datos de tarjeta (Stripe)
4. Revisar monto
5. Confirmar pago
6. Recibir confirmación por email

### Ver Mi Calendario

1. Menú "Mi Calendario"
2. Vista mensual/semanal/diaria
3. Ver mis partidos programados
4. Añadir a Google Calendar (botón "Sincronizar")
5. Recibir notificaciones automáticas

### Ver Mis Estadísticas

1. Menú "Mi Perfil" > "Estadísticas"
2. Información disponible:
   - **Partidos**: Jugados, Ganados, Perdidos
   - **Win Rate**: Porcentaje de victorias
   - **Racha**: Victorias/derrotas consecutivas
   - **Saque**: Efectividad de saque
   - **Gráficos**: Evolución en el tiempo
3. Comparar con otros jugadores

### Sistema de Gamificación

#### Ver Logros

1. Menú "Mi Perfil" > "Logros"
2. Ver logros:
   - **Desbloqueados**: Logros conseguidos
   - **Bloqueados**: Logros pendientes
   - **Progreso**: Porcentaje de compleción
3. Ver recompensas (XP ganada)

#### Rankings

1. Menú "Rankings"
2. Tipos de ranking:
   - **Global**: Todos los clubes
   - **Por Club**: Específico de tu club
   - **Por Categoría**: Por nivel de habilidad
3. Ver tu posición y puntos
4. Ver top 10 jugadores

#### Niveles y Experiencia

- **XP (Experiencia)**: Se gana jugando partidos
- **Niveles**: Del 1 al 100
- **Fórmula XP**: `nivel * 100 + (nivel - 1) * 50`
- **Fuentes de XP**:
  - Ganar partido: 100 XP
  - Perder partido: 50 XP
  - Desbloquear logro: Variable
  - Completar torneo: Variable

### Chat en Tiempo Real

#### Chat Privado

1. Ir a perfil de otro jugador
2. Click en "Enviar Mensaje"
3. Escribir mensaje
4. Enter para enviar
5. Ver estado "leído/no leído"

#### Chat de Equipo

1. Acceder desde "Mi Equipo"
2. Chat grupal con tu pareja
3. Compartir estrategias

#### Chat de Torneo

1. Acceder desde página del torneo
2. Chat con todos los participantes
3. Recibir anuncios del organizador

#### Chat de Club

1. Menú "Mi Club" > "Chat"
2. Chat con todos los miembros del club
3. Anuncios y noticias

### Notificaciones

1. **Campana de notificaciones** (arriba derecha)
2. Tipos de notificaciones:
   - Confirmación de inscripción
   - Recordatorio de partido (24h antes)
   - Cambio de horario
   - Resultado de partido
   - Nuevo logro desbloqueado
   - Mensaje nuevo en chat
3. Configurar preferencias:
   - In-app: Siempre activas
   - Email: Configurables
   - SMS: Configurables (si disponible)

---

## Manual para Administradores

### Gestionar Clubes

#### Crear Nuevo Club

1. Menú "Administración" > "Clubes"
2. Click "Nuevo Club"
3. Completar formulario:
   - **Nombre**: Nombre del club
   - **Dirección**: Ubicación física
   - **Ciudad y País**
   - **Teléfono y Email**: Contacto
   - **Website**: URL (opcional)
   - **Logo**: Imagen del club (max 5MB)
4. Configurar opciones:
   - **Moneda**: USD, EUR, MXN, etc.
   - **Zona horaria**
   - **Idioma**: es, en, etc.
   - **Colores**: Primario y secundario (hex)
5. Configurar permisos:
   - Inscripciones públicas: Sí/No
   - Requiere aprobación: Sí/No
   - Miembros pueden crear torneos: Sí/No
6. Guardar

#### Gestionar Miembros del Club

1. Ir a "Mi Club" > "Miembros"
2. Ver lista de miembros
3. Acciones disponibles:
   - **Agregar Miembro**: Invitar por email
   - **Promover a Staff**: Dar permisos de gestión
   - **Eliminar Miembro**: Quitar del club
   - **Ver Estadísticas**: Del miembro en el club
4. Exportar lista de miembros (CSV/Excel)

#### Gestionar Pistas

1. Menú "Mi Club" > "Pistas"
2. Click "Nueva Pista"
3. Completar:
   - **Nombre**: Ej: "Pista 1"
   - **Tipo**: Cubierta/Descubierta
   - **Estado**: Activa/Mantenimiento
4. Guardar
5. **Asignación automática**: El sistema asigna pistas a partidos

### Gestionar Usuarios

1. Menú "Administración" > "Usuarios"
2. Ver todos los usuarios del sistema
3. Acciones:
   - **Buscar**: Por nombre, email, username
   - **Filtrar**: Por rol, estado, club
   - **Editar**: Cambiar información
   - **Cambiar Rol**: Asignar PLAYER/OPERATOR/ADMIN
   - **Desactivar**: Suspender cuenta
4. Exportar datos de usuarios

### Configuración del Sistema

1. Menú "Administración" > "Configuración"
2. Configuraciones globales:
   - **Email**: SMTP settings
   - **Pagos**: Stripe configuration
   - **OAuth2**: Google credentials
   - **Notificaciones**: Preferencias por defecto
   - **Gamificación**: XP por acciones
   - **Backup**: Frecuencia y retención
3. Guardar cambios

---

## Manual para Organizadores

### Crear Torneo

1. Menú "Torneos" > "Nuevo Torneo"
2. **Paso 1: Información Básica**
   - Nombre del torneo
   - Descripción
   - Club organizador
   - Imagen/banner (opcional)

3. **Paso 2: Fechas**
   - Fecha inicio inscripción
   - Fecha cierre inscripción
   - Fecha inicio torneo
   - Fecha fin torneo (estimada)

4. **Paso 3: Formato**
   - **Tipo**: Americano / Liga / Eliminación
   - **Número de pozos**: Grupos por nivel
   - **Partidos simultáneos**: Cuántos a la vez
   - **Juegos por partido**: Ej: mejor de 3

5. **Paso 4: Inscripción**
   - **Cupo máximo**: Número de equipos/jugadores
   - **Nivel requerido**: Min y Max
   - **Es gratuito**: Sí/No
   - **Precio**: Si es de pago
   - **Público/Privado**: Visible para todos o solo invitados

6. **Paso 5: Premios (opcional)**
   - Agregar premios por posición
   - Descripción de premio
   - Valor (informativo)

7. Revisar y "Crear Torneo"

### Gestionar Inscripciones

1. Ir al torneo creado
2. Pestaña "Inscripciones"
3. Ver lista de inscritos:
   - **Pendientes**: Requieren aprobación
   - **Confirmados**: Ya aprobados
   - **Rechazados**: No aceptados
4. Acciones:
   - **Aprobar**: Confirmar inscripción
   - **Rechazar**: Con motivo opcional
   - **Ver Perfil**: Del jugador
   - **Contactar**: Enviar mensaje
5. Ver pagos (si torneo es de pago)

### Generar Emparejamientos

1. Cuando inscripciones están cerradas
2. Click en "Generar Emparejamientos"
3. El sistema:
   - Organiza jugadores en pozos por nivel
   - Crea enfrentamientos justos
   - Asigna pistas automáticamente
   - Genera calendario
4. Revisar emparejamientos generados
5. Ajustar manualmente si necesario
6. "Confirmar y Publicar"

### Registrar Resultados

1. Durante el torneo
2. Ir a "Enfrentamientos"
3. Seleccionar partido finalizado
4. Click en "Registrar Resultado"
5. Ingresar:
   - **Resultado del set 1**: Ej: 6-4
   - **Resultado del set 2**: Ej: 6-2
   - **Resultado del set 3** (si aplica)
   - **Equipo ganador**: Automático
6. Confirmar resultado
7. Se actualizan:
   - Clasificación del torneo
   - Estadísticas de jugadores
   - XP y logros (si aplica)

### Finalizar Torneo

1. Cuando todos los partidos están jugados
2. Click en "Finalizar Torneo"
3. El sistema:
   - Calcula posiciones finales
   - Asigna premios
   - Genera reporte final
   - Notifica a participantes
   - Actualiza rankings
4. Descargar reporte (PDF)
5. Compartir resultados

---

## Características Avanzadas

### Predicciones ML

#### Predecir Resultado de Partido

1. Ir a "Próximos Enfrentamientos"
2. Click en partido
3. Ver "Predicción ML"
4. Información mostrada:
   - **Probabilidades**: % de victoria de cada equipo
   - **Favorito**: Equipo con mayor probabilidad
   - **Nivel de confianza**: Alto/Medio/Bajo
   - **Análisis**: Factores considerados
5. Factores en la predicción:
   - Win rate histórico (40%)
   - Racha reciente (25%)
   - Experiencia (15%)
   - Efectividad de saque (10%)
   - Nivel de habilidad (10%)

#### Predecir Favoritos de Torneo

1. Ir a página del torneo
2. Click en "Ver Predicciones"
3. Ver top 3 favoritos
4. Para cada favorito:
   - Probabilidad de ganar
   - Justificación
   - Estadísticas clave

### Integración con Google Calendar

#### Sincronizar Calendario

1. Ir a "Mi Calendario"
2. Click en "Conectar con Google"
3. Autorizar acceso a Google Calendar
4. Seleccionar calendario de destino
5. Activar sincronización automática
6. Todos los partidos se agregan automáticamente

#### Configurar Recordatorios

1. En "Mi Perfil" > "Configuración"
2. Sección "Recordatorios"
3. Activar/Desactivar:
   - 24 horas antes
   - 2 horas antes
   - 30 minutos antes
4. Elegir canal: In-app / Email / Calendar notification

### Sistema de Pagos (Stripe)

#### Para Jugadores

1. Al inscribirse en torneo de pago
2. Click en "Pagar Inscripción"
3. Ingresar datos de tarjeta (seguro con Stripe)
4. Revisar monto
5. Confirmar pago
6. Recibir comprobante por email
7. Ver historial en "Mi Perfil" > "Pagos"

#### Para Administradores

1. Configurar Stripe:
   - API Key en variables de entorno
   - Webhook secret configurado
2. Ver dashboard de pagos en Stripe
3. Gestionar reembolsos si necesario
4. Ver reportes financieros

### Exportar Datos

#### Exportar Estadísticas

1. Ir a sección de estadísticas
2. Click en "Exportar"
3. Elegir formato: CSV / Excel / PDF
4. Seleccionar rango de fechas
5. Descargar archivo

#### Exportar Lista de Jugadores

1. Ir a "Miembros" o "Participantes"
2. Click en "Exportar Lista"
3. Formato: CSV / Excel
4. Se descarga con: Nombre, Email, Nivel, Estadísticas

---

## Preguntas Frecuentes

### General

**P: ¿Es gratis usar Padel Club Pro?**
R: La plataforma es gratuita para jugadores. Los clubes pueden tener suscripción de pago para funciones premium.

**P: ¿Funciona en móviles?**
R: Sí, la interfaz es completamente responsive y funciona en cualquier dispositivo.

**P: ¿Puedo pertenecer a múltiples clubes?**
R: Sí, los usuarios pueden ser miembros de varios clubes simultáneamente.

### Inscripciones

**P: ¿Puedo cancelar una inscripción?**
R: Sí, antes del cierre de inscripciones. Contacta al organizador para reembolsos.

**P: ¿Puedo cambiar de pareja?**
R: Depende de las reglas del torneo. Consulta con el organizador.

**P: ¿Qué pasa si no me presento?**
R: Pierdes el partido por walkover y puede afectar tu reputación en la plataforma.

### Torneos

**P: ¿Cómo funcionan los torneos Americanos?**
R: Las parejas rotan cada ronda. Cada jugador juega con diferentes compañeros.

**P: ¿Qué es un pozo?**
R: Un grupo de jugadores del mismo nivel que compiten entre sí.

**P: ¿Cómo se calculan los rankings?**
R: Basados en victorias, nivel de oponentes y consistencia. Usa sistema ELO modificado.

### Estadísticas

**P: ¿Cuándo se actualizan mis estadísticas?**
R: Inmediatamente después de que se registra el resultado de un partido.

**P: ¿Puedo ver estadísticas de otros jugadores?**
R: Sí, los perfiles y estadísticas son públicos por defecto.

### Problemas Técnicos

**P: No recibo notificaciones por email**
R: Verifica tu carpeta de spam y configura `noreply@padelclub.pro` como contacto seguro.

**P: El chat no funciona**
R: Verifica tu conexión a internet. El chat usa WebSocket y requiere conexión estable.

**P: No puedo subir mi foto de perfil**
R: Verifica que la imagen sea menor a 5MB y en formato JPG/PNG.

---

## Solución de Problemas

### No Puedo Iniciar Sesión

**Síntomas**: Mensaje "Usuario o contraseña incorrectos"

**Soluciones**:
1. Verificar que el usuario/email sea correcto
2. Usar "¿Olvidaste tu contraseña?"
3. Intentar con "Login con Google" si usaste ese método
4. Verificar que la cuenta no esté desactivada
5. Contactar soporte si persiste

### No Veo Mi Torneo en el Listado

**Posibles causas**:
1. Torneo es privado (solo invitados)
2. Filtros activos ocultan el torneo
3. Torneo está en un club al que no perteneces
4. Torneo fue cancelado

**Soluciones**:
1. Quitar todos los filtros
2. Buscar por nombre directamente
3. Contactar al organizador

### El Pago No Se Procesó

**Síntomas**: Error al intentar pagar con tarjeta

**Soluciones**:
1. Verificar datos de tarjeta
2. Verificar fondos disponibles
3. Intentar con otra tarjeta
4. Contactar a tu banco (posible bloqueo)
5. Esperar 5 minutos y reintentar
6. Contactar soporte con código de error

### Estadísticas Incorrectas

**Síntomas**: Las estadísticas no reflejan los partidos jugados

**Soluciones**:
1. Esperar 1-2 minutos (actualización puede tardar)
2. Refrescar la página (F5)
3. Limpiar caché del navegador
4. Verificar que el resultado fue registrado correctamente
5. Contactar soporte con detalles específicos

### Chat No Conecta

**Síntomas**: Mensajes no se envían o no llegan

**Soluciones**:
1. Verificar conexión a internet
2. Refrescar la página
3. Verificar que WebSocket no esté bloqueado por firewall
4. Probar en otro navegador
5. Verificar en `/actuator/health` que WebSocket esté activo

---

## Glosario de Términos

- **Americano**: Formato de torneo donde las parejas rotan
- **Pozo**: Grupo de jugadores del mismo nivel
- **Enfrentamiento**: Partido entre dos equipos
- **Win Rate**: Porcentaje de victorias
- **XP**: Experiencia (puntos de gamificación)
- **Logro**: Achievement desbloqueable
- **Staff**: Personal del club con permisos de gestión
- **Webhook**: Notificación automática del sistema
- **ML**: Machine Learning (Inteligencia Artificial)

---

## Contacto y Soporte

**Email**: support@padelclub.pro
**Horario**: Lunes a Viernes 9:00 - 18:00 (GMT-5)
**Tiempo de respuesta**: 24-48 horas

**Para reportar bugs**:
1. Ir a GitHub Issues
2. Buscar si ya fue reportado
3. Crear nuevo issue con:
   - Descripción detallada
   - Pasos para reproducir
   - Capturas de pantalla
   - Navegador y versión

---

<div align="center">

**¿Necesitas ayuda adicional?**
Consulta nuestra [documentación técnica](../README.md) o contacta a soporte.

</div>
