package com.padellevel.services;

import com.padellevel.data.*;
import com.padellevel.repository.LogroRepository;
import com.padellevel.repository.LogroUsuarioRepository;
import com.padellevel.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio de gamificación que gestiona experiencia, niveles y logros.
 */
@Service
public class GamificacionService {

    private static final Logger logger = LoggerFactory.getLogger(GamificacionService.class);

    private final UserRepository userRepository;
    private final LogroRepository logroRepository;
    private final LogroUsuarioRepository logroUsuarioRepository;
    private NotificacionService notificacionService; // Lazy injection

    // Constantes de XP
    private static final int XP_PARTIDO_JUGADO = 10;
    private static final int XP_PARTIDO_GANADO = 25;
    private static final int XP_TORNEO_PARTICIPADO = 50;
    private static final int XP_TORNEO_GANADO = 200;
    private static final int XP_PRIMER_PUESTO = 300;
    private static final int XP_SEGUNDO_PUESTO = 200;
    private static final int XP_TERCER_PUESTO = 150;
    private static final int XP_INSCRIPCION_TORNEO = 5;
    private static final int XP_PAGO_COMPLETADO = 10;

    public GamificacionService(UserRepository userRepository,
                              LogroRepository logroRepository,
                              LogroUsuarioRepository logroUsuarioRepository) {
        this.userRepository = userRepository;
        this.logroRepository = logroRepository;
        this.logroUsuarioRepository = logroUsuarioRepository;
    }

    /**
     * Inyección lazy de NotificacionService para evitar dependencias circulares.
     */
    public void setNotificacionService(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    /**
     * Otorga experiencia a un jugador y verifica si sube de nivel.
     *
     * @param jugador el jugador
     * @param cantidad cantidad de XP a otorgar
     * @return true si el jugador subió de nivel
     */
    @Transactional
    public boolean otorgarExperiencia(User jugador, int cantidad) {
        if (jugador == null || cantidad <= 0) {
            return false;
        }

        int xpAnterior = jugador.getExperienciaTotal();
        int nivelAnterior = jugador.getNivelGamificacion();

        jugador.setExperienciaTotal(xpAnterior + cantidad);

        // Calcular nuevo nivel basado en XP total
        int nuevoNivel = calcularNivelPorXP(jugador.getExperienciaTotal());

        boolean subioDeNivel = false;
        if (nuevoNivel > nivelAnterior) {
            jugador.setNivelGamificacion(nuevoNivel);
            subioDeNivel = true;

            logger.info("¡Jugador {} subió al nivel {}!", jugador.getUsername(), nuevoNivel);

            // Notificar subida de nivel
            if (notificacionService != null) {
                Map<String, String> variables = new HashMap<>();
                variables.put("nivel", String.valueOf(nuevoNivel));
                variables.put("jugador", jugador.getNombreCompleto());

                notificacionService.enviarNotificacion(
                    jugador,
                    TipoNotificacion.SUBIDA_NIVEL,
                    CanalNotificacion.IN_APP,
                    "¡Subiste de nivel!",
                    "¡Felicitaciones! Alcanzaste el nivel " + nuevoNivel,
                    variables
                );
            }

            // Verificar logros relacionados con nivel
            verificarLogrosNivel(jugador, nuevoNivel);
        }

        userRepository.save(jugador);
        logger.debug("Jugador {} ganó {} XP (Total: {}, Nivel: {})",
                    jugador.getUsername(), cantidad, jugador.getExperienciaTotal(), jugador.getNivelGamificacion());

        return subioDeNivel;
    }

    /**
     * Calcula el nivel correspondiente a una cantidad de XP.
     *
     * @param xp experiencia total
     * @return nivel calculado (1-100)
     */
    private int calcularNivelPorXP(int xp) {
        int nivel = 1;
        while (nivel < 100 && xp >= User.calcularXPParaNivel(nivel + 1)) {
            nivel++;
        }
        return nivel;
    }

    /**
     * Otorga XP por jugar un partido.
     */
    @Transactional
    public void otorgarXPPartidoJugado(User jugador) {
        otorgarExperiencia(jugador, XP_PARTIDO_JUGADO);
    }

    /**
     * Otorga XP por ganar un partido.
     */
    @Transactional
    public void otorgarXPPartidoGanado(User jugador) {
        otorgarExperiencia(jugador, XP_PARTIDO_GANADO);
    }

    /**
     * Otorga XP por participar en un torneo.
     */
    @Transactional
    public void otorgarXPTorneoParticipado(User jugador) {
        otorgarExperiencia(jugador, XP_TORNEO_PARTICIPADO);
    }

    /**
     * Otorga XP por ganar un torneo.
     */
    @Transactional
    public void otorgarXPTorneoGanado(User jugador, int posicion) {
        int xp;
        switch (posicion) {
            case 1:
                xp = XP_PRIMER_PUESTO;
                break;
            case 2:
                xp = XP_SEGUNDO_PUESTO;
                break;
            case 3:
                xp = XP_TERCER_PUESTO;
                break;
            default:
                xp = XP_TORNEO_PARTICIPADO;
        }
        otorgarExperiencia(jugador, xp);
    }

    /**
     * Otorga XP por inscribirse a un torneo.
     */
    @Transactional
    public void otorgarXPInscripcion(User jugador) {
        otorgarExperiencia(jugador, XP_INSCRIPCION_TORNEO);
    }

    /**
     * Otorga XP por completar un pago.
     */
    @Transactional
    public void otorgarXPPagoCompletado(User jugador) {
        otorgarExperiencia(jugador, XP_PAGO_COMPLETADO);
    }

    /**
     * Desbloquea un logro para un jugador.
     *
     * @param jugador el jugador
     * @param logro el logro a desbloquear
     * @return LogroUsuario creado o actualizado
     */
    @Transactional
    public LogroUsuario desbloquearLogro(User jugador, Logro logro) {
        // Verificar si ya tiene el logro
        Optional<LogroUsuario> existente = logroUsuarioRepository.findByJugadorAndLogro(jugador, logro);

        if (existente.isPresent()) {
            logger.debug("Jugador {} ya tiene el logro {}", jugador.getUsername(), logro.getNombre());
            return existente.get();
        }

        // Crear nuevo logro desbloqueado
        LogroUsuario logroUsuario = new LogroUsuario(jugador, logro);
        logroUsuario.setProgresoActual(logro.getMeta() != null ? logro.getMeta() : 1);
        logroUsuario.setVisto(false);
        logroUsuarioRepository.save(logroUsuario);

        logger.info("¡Jugador {} desbloqueó el logro '{}'!", jugador.getUsername(), logro.getNombre());

        // Notificar logro desbloqueado
        if (notificacionService != null) {
            Map<String, String> variables = new HashMap<>();
            variables.put("logro", logro.getNombre());
            variables.put("descripcion", logro.getDescripcion());
            variables.put("jugador", jugador.getNombreCompleto());

            notificacionService.enviarNotificacion(
                jugador,
                TipoNotificacion.LOGRO_DESBLOQUEADO,
                CanalNotificacion.IN_APP,
                "¡Nuevo Logro Desbloqueado!",
                "Has desbloqueado: " + logro.getNombre(),
                variables
            );
        }

        return logroUsuario;
    }

    /**
     * Actualiza el progreso de un logro para un jugador.
     *
     * @param jugador el jugador
     * @param tipoLogro tipo de logro
     * @param incremento cantidad a incrementar
     */
    @Transactional
    public void actualizarProgresoLogro(User jugador, TipoLogro tipoLogro, int incremento) {
        // Buscar logros de este tipo
        List<Logro> logros = logroRepository.findAll().stream()
            .filter(l -> l.getTipo() == tipoLogro && l.getMeta() != null)
            .collect(Collectors.toList());

        for (Logro logro : logros) {
            Optional<LogroUsuario> logroUsuarioOpt = logroUsuarioRepository.findByJugadorAndLogro(jugador, logro);

            LogroUsuario logroUsuario;
            if (logroUsuarioOpt.isPresent()) {
                logroUsuario = logroUsuarioOpt.get();
                // Si ya está completo, no hacer nada
                if (logroUsuario.isCompleto()) {
                    continue;
                }
            } else {
                // Crear nuevo registro de progreso
                logroUsuario = new LogroUsuario(jugador, logro);
                logroUsuario.setProgresoActual(0);
            }

            // Incrementar progreso
            logroUsuario.setProgresoActual(logroUsuario.getProgresoActual() + incremento);

            // Verificar si se completó el logro
            if (logroUsuario.getProgresoActual() >= logro.getMeta()) {
                logroUsuario.setProgresoActual(logro.getMeta());
                logroUsuario.setFechaDesbloqueo(LocalDateTime.now());
                logger.info("¡Jugador {} completó el logro '{}'!", jugador.getUsername(), logro.getNombre());

                // Notificar
                if (notificacionService != null) {
                    Map<String, String> variables = new HashMap<>();
                    variables.put("logro", logro.getNombre());
                    variables.put("descripcion", logro.getDescripcion());

                    notificacionService.enviarNotificacion(
                        jugador,
                        TipoNotificacion.LOGRO_DESBLOQUEADO,
                        CanalNotificacion.IN_APP,
                        "¡Nuevo Logro Desbloqueado!",
                        "Has desbloqueado: " + logro.getNombre(),
                        variables
                    );
                }
            }

            logroUsuarioRepository.save(logroUsuario);
        }
    }

    /**
     * Verifica y otorga logros relacionados con alcanzar un nivel.
     */
    private void verificarLogrosNivel(User jugador, int nivel) {
        // Aquí se pueden implementar logros específicos por nivel
        // Por ejemplo: Nivel 10, Nivel 25, Nivel 50, Nivel 100
        if (nivel == 10 || nivel == 25 || nivel == 50 || nivel == 100) {
            actualizarProgresoLogro(jugador, TipoLogro.VETERANO, 1);
        }
    }

    /**
     * Obtiene todos los logros desbloqueados por un jugador.
     */
    @Transactional(readOnly = true)
    public List<LogroUsuario> obtenerLogrosDesbloqueados(User jugador) {
        return logroUsuarioRepository.findByJugadorOrderByFechaDesbloqueoDesc(jugador);
    }

    /**
     * Obtiene logros no vistos por un jugador.
     */
    @Transactional(readOnly = true)
    public List<LogroUsuario> obtenerLogrosNoVistos(User jugador) {
        return logroUsuarioRepository.findByJugadorAndVistoOrderByFechaDesbloqueoDesc(jugador, false);
    }

    /**
     * Marca un logro como visto.
     */
    @Transactional
    public void marcarLogroComoVisto(Long logroUsuarioId) {
        logroUsuarioRepository.findById(logroUsuarioId).ifPresent(lu -> {
            lu.setVisto(true);
            logroUsuarioRepository.save(lu);
        });
    }

    /**
     * Obtiene el ranking de jugadores por XP.
     */
    @Transactional(readOnly = true)
    public List<User> obtenerRankingPorExperiencia(int limite) {
        return userRepository.findAll().stream()
            .filter(u -> u.getExperienciaTotal() != null && u.getExperienciaTotal() > 0)
            .sorted(Comparator.comparing(User::getExperienciaTotal).reversed())
            .limit(limite)
            .collect(Collectors.toList());
    }

    /**
     * Obtiene el ranking de jugadores por logros.
     */
    @Transactional(readOnly = true)
    public List<Map.Entry<User, Long>> obtenerRankingPorLogros(int limite) {
        Map<User, Long> logrosPorJugador = new HashMap<>();

        List<User> jugadores = userRepository.findAll();
        for (User jugador : jugadores) {
            long count = logroUsuarioRepository.countByJugador(jugador);
            if (count > 0) {
                logrosPorJugador.put(jugador, count);
            }
        }

        return logrosPorJugador.entrySet().stream()
            .sorted(Map.Entry.<User, Long>comparingByValue().reversed())
            .limit(limite)
            .collect(Collectors.toList());
    }

    /**
     * Obtiene estadísticas de gamificación para un jugador.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticasGamificacion(User jugador) {
        Map<String, Object> stats = new HashMap<>();

        stats.put("experienciaTotal", jugador.getExperienciaTotal());
        stats.put("nivel", jugador.getNivelGamificacion());
        stats.put("xpParaSiguienteNivel", jugador.getXPParaSiguienteNivel());
        stats.put("progresoNivel", jugador.getProgresoNivel());
        stats.put("logrosDesbloqueados", logroUsuarioRepository.countByJugador(jugador));
        stats.put("logrosNoVistos", logroUsuarioRepository.findByJugadorAndVistoOrderByFechaDesbloqueoDesc(jugador, false).size());

        return stats;
    }
}
