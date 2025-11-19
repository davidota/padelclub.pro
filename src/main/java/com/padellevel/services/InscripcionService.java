package com.padellevel.services;

import com.padellevel.data.*;
import com.padellevel.repository.InscripcionPozoRepository;
import com.padellevel.repository.InscripcionRepository;
import com.stripe.exception.StripeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio para gestionar inscripciones a torneos y pozos.
 *
 * Funcionalidades principales:
 * - Inscribir jugadores a torneos (con o sin pago)
 * - Inscribir jugadores a pozos específicos
 * - Validar cupos disponibles
 * - Gestionar fechas de inscripción
 * - Integrar con sistema de pagos
 */
@Service
public class InscripcionService {

    private static final Logger logger = LoggerFactory.getLogger(InscripcionService.class);

    private final InscripcionRepository inscripcionRepository;
    private final InscripcionPozoRepository inscripcionPozoRepository;
    private final PagoService pagoService;
    private NotificacionService notificacionService; // Lazy injection to avoid circular dependency
    private GamificacionService gamificacionService; // Lazy injection to avoid circular dependency

    public InscripcionService(InscripcionRepository inscripcionRepository,
                             InscripcionPozoRepository inscripcionPozoRepository,
                             PagoService pagoService) {
        this.inscripcionRepository = inscripcionRepository;
        this.inscripcionPozoRepository = inscripcionPozoRepository;
        this.pagoService = pagoService;
    }

    /**
     * Sets the NotificacionService (lazy injection to avoid circular dependency).
     */
    public void setNotificacionService(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    /**
     * Sets the GamificacionService (lazy injection to avoid circular dependency).
     */
    public void setGamificacionService(GamificacionService gamificacionService) {
        this.gamificacionService = gamificacionService;
    }

    /**
     * Inscribe un jugador a un torneo.
     *
     * Si el torneo requiere pago, crea el Payment Intent y retorna el Pago
     * con el client_secret para que el frontend complete el pago.
     *
     * @param jugador El jugador a inscribir
     * @param torneo El torneo
     * @param pareja Pareja preferida (opcional)
     * @param fechasDisponibles Fechas en las que el jugador está disponible
     * @param comentarios Comentarios adicionales
     * @return La inscripción creada
     * @throws StripeException Si hay error al crear el Payment Intent
     */
    @Transactional
    public Inscripcion inscribirJugadorATorneo(User jugador, Torneo torneo, User pareja,
                                              List<LocalDate> fechasDisponibles, String comentarios)
            throws StripeException {

        logger.info("Inscribiendo jugador {} a torneo {}", jugador.getUsername(), torneo.getNombre());

        // Validar que las inscripciones estén abiertas
        validarInscripcionesAbiertas(torneo);

        // Validar que hay cupo disponible
        validarCupoDisponible(torneo);

        // Validar que el jugador no esté ya inscrito
        if (inscripcionRepository.findByJugadorAndTorneo(jugador, torneo).isPresent()) {
            throw new IllegalStateException("El jugador ya está inscrito en este torneo");
        }

        // Crear la inscripción
        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setJugador(jugador);
        inscripcion.setTorneo(torneo);
        inscripcion.setPareja(pareja);
        inscripcion.setFechasDisponibles(fechasDisponibles);
        inscripcion.setComentarios(comentarios);
        inscripcion.setFechaInscripcion(LocalDateTime.now());

        // Determinar estado inicial
        if (torneo.getEsGratuito() != null && torneo.getEsGratuito()) {
            // Torneo gratuito - confirmar directamente
            inscripcion.setEstado(EstadoInscripcion.CONFIRMADA);
            logger.info("Inscripción confirmada automáticamente (torneo gratuito)");
        } else {
            // Torneo de pago - marcar como pendiente hasta que se complete el pago
            inscripcion.setEstado(EstadoInscripcion.PENDIENTE);
            logger.info("Inscripción pendiente de pago");
        }

        Inscripcion inscripcionGuardada = inscripcionRepository.save(inscripcion);

        // Si requiere pago, crear el Payment Intent
        if (torneo.getEsGratuito() != null && !torneo.getEsGratuito()) {
            BigDecimal monto = torneo.getPrecioInscripcion();
            if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalStateException("El torneo requiere pago pero no tiene precio definido");
            }

            Pago pago = pagoService.crearPagoInscripcionTorneo(inscripcionGuardada, torneo, monto);
            inscripcionGuardada.setPago(pago);
            logger.info("Payment Intent creado - Client Secret: {}",
                       pago.getStripeClientSecret() != null ? "generado" : "N/A");
        } else {
            // Si es torneo gratuito, enviar notificación de confirmación inmediata
            if (notificacionService != null) {
                try {
                    notificacionService.notificarInscripcionConfirmada(inscripcionGuardada);
                } catch (Exception e) {
                    logger.error("Error al enviar notificación de inscripción confirmada", e);
                    // No fallar la inscripción por error de notificación
                }
            }

            // Otorgar XP por inscripción
            if (gamificacionService != null) {
                try {
                    gamificacionService.otorgarXPInscripcion(jugador);
                    gamificacionService.actualizarProgresoLogro(jugador, TipoLogro.DEBUT, 1);
                } catch (Exception e) {
                    logger.error("Error al otorgar XP por inscripción", e);
                    // No fallar la inscripción por error de gamificación
                }
            }
        }

        logger.info("Inscripción creada exitosamente - ID: {}", inscripcionGuardada.getId());
        return inscripcionGuardada;
    }

    /**
     * Inscribe un jugador a un pozo específico.
     *
     * El jugador debe estar previamente inscrito al torneo.
     *
     * @param jugador El jugador a inscribir
     * @param pozo El pozo
     * @param inscripcionTorneo La inscripción previa al torneo
     * @return La inscripción al pozo creada
     * @throws StripeException Si hay error al crear el Payment Intent
     */
    @Transactional
    public InscripcionPozo inscribirJugadorAPozo(User jugador, Pozo pozo, Inscripcion inscripcionTorneo)
            throws StripeException {

        logger.info("Inscribiendo jugador {} a pozo {}", jugador.getUsername(), pozo.getNombre());

        // Validar que el jugador esté inscrito al torneo
        if (inscripcionTorneo == null || !inscripcionTorneo.getTorneo().equals(pozo.getTorneo())) {
            throw new IllegalArgumentException("El jugador debe estar inscrito al torneo primero");
        }

        // Validar que las inscripciones al pozo estén abiertas
        validarInscripcionesPozoAbiertas(pozo);

        // Validar que hay cupo disponible en el pozo
        validarCupoPozoDisponible(pozo);

        // Validar que el jugador no esté ya inscrito en este pozo
        if (inscripcionPozoRepository.findByJugadorAndPozo(jugador, pozo).isPresent()) {
            throw new IllegalStateException("El jugador ya está inscrito en este pozo");
        }

        // Crear la inscripción al pozo
        InscripcionPozo inscripcionPozo = new InscripcionPozo();
        inscripcionPozo.setJugador(jugador);
        inscripcionPozo.setPozo(pozo);
        inscripcionPozo.setInscripcionTorneo(inscripcionTorneo);
        inscripcionPozo.setFechaInscripcion(LocalDateTime.now());

        // Determinar estado inicial
        if (pozo.getEsGratuito() != null && pozo.getEsGratuito()) {
            // Pozo gratuito - confirmar directamente
            inscripcionPozo.setEstado(EstadoInscripcion.CONFIRMADA);
            logger.info("Inscripción a pozo confirmada automáticamente (pozo gratuito)");
        } else {
            // Pozo de pago - marcar como pendiente
            inscripcionPozo.setEstado(EstadoInscripcion.PENDIENTE);
            logger.info("Inscripción a pozo pendiente de pago");
        }

        InscripcionPozo inscripcionPozoGuardada = inscripcionPozoRepository.save(inscripcionPozo);

        // Si requiere pago, crear el Payment Intent
        if (pozo.getEsGratuito() != null && !pozo.getEsGratuito()) {
            BigDecimal monto = pozo.getPrecioInscripcion();
            if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalStateException("El pozo requiere pago pero no tiene precio definido");
            }

            Pago pago = pagoService.crearPagoInscripcionPozo(inscripcionPozoGuardada, pozo, monto);
            inscripcionPozoGuardada.setPago(pago);
            logger.info("Payment Intent para pozo creado - Client Secret: {}",
                       pago.getStripeClientSecret() != null ? "generado" : "N/A");
        }

        logger.info("Inscripción a pozo creada exitosamente - ID: {}", inscripcionPozoGuardada.getId());
        return inscripcionPozoGuardada;
    }

    /**
     * Cancela una inscripción a un torneo.
     *
     * Si hay pago asociado y ya fue completado, procesa el reembolso.
     *
     * @param inscripcionId ID de la inscripción
     * @param motivo Motivo de la cancelación
     * @throws StripeException Si hay error al procesar el reembolso
     */
    @Transactional
    public void cancelarInscripcionTorneo(Long inscripcionId, String motivo) throws StripeException {
        logger.info("Cancelando inscripción {} - Motivo: {}", inscripcionId, motivo);

        Inscripcion inscripcion = inscripcionRepository.findById(inscripcionId)
            .orElseThrow(() -> new IllegalArgumentException("Inscripción no encontrada"));

        if (inscripcion.getEstado() == EstadoInscripcion.CANCELADA) {
            logger.warn("La inscripción ya estaba cancelada");
            return;
        }

        // Si hay pago completado, procesar reembolso
        if (inscripcion.getPago() != null && inscripcion.getPago().getEstado() == EstadoPago.COMPLETADO) {
            logger.info("Procesando reembolso para pago {}", inscripcion.getPago().getId());
            pagoService.procesarReembolsoCompleto(inscripcion.getPago().getId(), motivo);
        }

        // Marcar inscripción como cancelada
        inscripcion.setEstado(EstadoInscripcion.CANCELADA);
        inscripcionRepository.save(inscripcion);

        logger.info("Inscripción cancelada exitosamente");
    }

    /**
     * Obtiene todas las inscripciones de un torneo.
     */
    public List<Inscripcion> obtenerInscripcionesTorneo(Torneo torneo) {
        return inscripcionRepository.findByTorneo(torneo);
    }

    /**
     * Obtiene las inscripciones confirmadas de un torneo.
     */
    public List<Inscripcion> obtenerInscripcionesConfirmadasTorneo(Torneo torneo) {
        return inscripcionRepository.findByTorneoAndEstado(torneo, EstadoInscripcion.CONFIRMADA);
    }

    /**
     * Obtiene las inscripciones de un jugador.
     */
    public List<Inscripcion> obtenerInscripcionesJugador(User jugador) {
        return inscripcionRepository.findByJugador(jugador);
    }

    /**
     * Obtiene todas las inscripciones a un pozo.
     */
    public List<InscripcionPozo> obtenerInscripcionesPozo(Pozo pozo) {
        return inscripcionPozoRepository.findByPozo(pozo);
    }

    /**
     * Cuenta las inscripciones confirmadas de un torneo.
     */
    public long contarInscripcionesConfirmadas(Torneo torneo) {
        return inscripcionRepository.countByTorneoAndEstado(torneo, EstadoInscripcion.CONFIRMADA);
    }

    /**
     * Cuenta las inscripciones confirmadas de un pozo.
     */
    public long contarInscripcionesConfirmadasPozo(Pozo pozo) {
        return inscripcionPozoRepository.countByPozoAndEstado(pozo, EstadoInscripcion.CONFIRMADA);
    }

    // ==================== VALIDACIONES ====================

    /**
     * Valida que las inscripciones al torneo estén abiertas.
     */
    private void validarInscripcionesAbiertas(Torneo torneo) {
        LocalDateTime ahora = LocalDateTime.now();

        if (torneo.getFechaInicioInscripcion() != null &&
            ahora.isBefore(torneo.getFechaInicioInscripcion())) {
            throw new IllegalStateException("Las inscripciones aún no han abierto");
        }

        if (torneo.getFechaCierreInscripcion() != null &&
            ahora.isAfter(torneo.getFechaCierreInscripcion())) {
            throw new IllegalStateException("Las inscripciones ya han cerrado");
        }
    }

    /**
     * Valida que hay cupo disponible en el torneo.
     */
    private void validarCupoDisponible(Torneo torneo) {
        if (torneo.getCupoMaximo() == null) {
            return; // Sin límite de cupos
        }

        long inscritosConfirmados = contarInscripcionesConfirmadas(torneo);

        if (inscritosConfirmados >= torneo.getCupoMaximo()) {
            throw new IllegalStateException("El torneo ha alcanzado el cupo máximo");
        }
    }

    /**
     * Valida que las inscripciones al pozo estén abiertas.
     */
    private void validarInscripcionesPozoAbiertas(Pozo pozo) {
        if (pozo.getEstado() == EstadoPozo.INSCRIPCION_CERRADA ||
            pozo.getEstado() == EstadoPozo.EN_CURSO ||
            pozo.getEstado() == EstadoPozo.FINALIZADO) {
            throw new IllegalStateException("Las inscripciones a este pozo no están abiertas");
        }

        LocalDateTime ahora = LocalDateTime.now();

        if (pozo.getFechaInicioInscripcion() != null &&
            ahora.isBefore(pozo.getFechaInicioInscripcion())) {
            throw new IllegalStateException("Las inscripciones al pozo aún no han abierto");
        }

        if (pozo.getFechaCierreInscripcion() != null &&
            ahora.isAfter(pozo.getFechaCierreInscripcion())) {
            throw new IllegalStateException("Las inscripciones al pozo ya han cerrado");
        }
    }

    /**
     * Valida que hay cupo disponible en el pozo.
     */
    private void validarCupoPozoDisponible(Pozo pozo) {
        if (pozo.getCupoMaximo() == null) {
            return; // Sin límite de cupos
        }

        long inscritosConfirmados = contarInscripcionesConfirmadasPozo(pozo);

        if (inscritosConfirmados >= pozo.getCupoMaximo()) {
            throw new IllegalStateException("El pozo ha alcanzado el cupo máximo");
        }
    }
}
