package com.padellevel.services;

import com.padellevel.data.*;
import com.padellevel.repository.NotificacionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Servicio principal para gestionar notificaciones multicanal.
 *
 * Soporta múltiples canales de notificación:
 * - EMAIL: Notificaciones por correo electrónico (SendGrid)
 * - PUSH: Notificaciones push (Firebase Cloud Messaging)
 * - SMS: Mensajes de texto (Twilio)
 * - IN_APP: Notificaciones dentro de la aplicación
 *
 * Las notificaciones se persisten en la base de datos y se envían
 * a través de servicios especializados según el canal.
 */
@Service
public class NotificacionService {

    private static final Logger logger = LoggerFactory.getLogger(NotificacionService.class);

    private final NotificacionRepository notificacionRepository;
    private final EmailNotificationService emailService;

    public NotificacionService(NotificacionRepository notificacionRepository,
                              EmailNotificationService emailService) {
        this.notificacionRepository = notificacionRepository;
        this.emailService = emailService;
    }

    /**
     * Envía una notificación a un usuario por el canal especificado.
     *
     * @param destinatario Usuario que recibirá la notificación
     * @param tipo Tipo de notificación
     * @param canal Canal de envío
     * @param titulo Título de la notificación
     * @param mensaje Cuerpo del mensaje
     * @param variables Variables para plantillas (opcional)
     * @return La notificación creada y enviada
     */
    @Transactional
    public Notificacion enviarNotificacion(User destinatario, TipoNotificacion tipo,
                                          CanalNotificacion canal, String titulo,
                                          String mensaje, Map<String, String> variables) {

        logger.info("Enviando notificación {} a {} por canal {}",
                   tipo, destinatario.getUsername(), canal);

        // Crear registro de notificación
        Notificacion notificacion = new Notificacion();
        notificacion.setDestinatario(destinatario);
        notificacion.setTipo(tipo);
        notificacion.setCanal(canal);
        notificacion.setTitulo(titulo);
        notificacion.setMensaje(mensaje);
        notificacion.setLeida(false);
        notificacion.setFechaEnvio(LocalDateTime.now());

        // Guardar notificación
        Notificacion notificacionGuardada = notificacionRepository.save(notificacion);

        // Enviar por el canal correspondiente
        try {
            switch (canal) {
                case EMAIL:
                    emailService.enviarEmail(destinatario.getEmail(), titulo, mensaje, variables);
                    notificacion.setEnviada(true);
                    logger.info("Email enviado exitosamente a {}", destinatario.getEmail());
                    break;

                case PUSH:
                    // TODO: Implementar con Firebase en futuras iteraciones
                    logger.info("Push notification (no implementado aún)");
                    notificacion.setEnviada(false);
                    break;

                case SMS:
                    // TODO: Implementar con Twilio en futuras iteraciones
                    logger.info("SMS notification (no implementado aún)");
                    notificacion.setEnviada(false);
                    break;

                case IN_APP:
                    // Para notificaciones in-app, solo se persiste en DB
                    notificacion.setEnviada(true);
                    logger.info("Notificación in-app creada");
                    break;

                default:
                    logger.warn("Canal de notificación no soportado: {}", canal);
                    notificacion.setEnviada(false);
            }

        } catch (Exception e) {
            logger.error("Error al enviar notificación por canal {}", canal, e);
            notificacion.setEnviada(false);
        }

        return notificacionRepository.save(notificacion);
    }

    /**
     * Notifica a un jugador sobre la confirmación de inscripción.
     */
    @Transactional
    public void notificarInscripcionConfirmada(Inscripcion inscripcion) {
        logger.info("Notificando inscripción confirmada: {}", inscripcion.getId());

        User jugador = inscripcion.getJugador();
        Torneo torneo = inscripcion.getTorneo();

        String titulo = "Inscripción confirmada - " + torneo.getNombre();
        String mensaje = String.format(
            "Hola %s,\n\n" +
            "Tu inscripción al torneo '%s' ha sido confirmada exitosamente.\n\n" +
            "Fecha inicio: %s\n" +
            "Fecha fin: %s\n" +
            "Ubicación: %s\n\n" +
            "¡Nos vemos en la cancha!\n\n" +
            "Equipo PadelClub Pro",
            jugador.getName(),
            torneo.getNombre(),
            torneo.getFechaInicio(),
            torneo.getFechaFin(),
            torneo.getClub() != null ? torneo.getClub().getNombre() : "Por definir"
        );

        Map<String, String> variables = Map.of(
            "jugador_nombre", jugador.getName(),
            "torneo_nombre", torneo.getNombre(),
            "torneo_fecha_inicio", torneo.getFechaInicio().toString(),
            "torneo_fecha_fin", torneo.getFechaFin().toString()
        );

        enviarNotificacion(jugador, TipoNotificacion.INSCRIPCION_CONFIRMADA,
                         CanalNotificacion.EMAIL, titulo, mensaje, variables);

        // También crear notificación in-app
        enviarNotificacion(jugador, TipoNotificacion.INSCRIPCION_CONFIRMADA,
                         CanalNotificacion.IN_APP, titulo, mensaje, variables);
    }

    /**
     * Notifica a un jugador sobre el pago confirmado.
     */
    @Transactional
    public void notificarPagoConfirmado(Pago pago) {
        logger.info("Notificando pago confirmado: {}", pago.getId());

        User jugador = pago.getJugador();
        Torneo torneo = pago.getTorneo();

        String titulo = "Pago confirmado - " + torneo.getNombre();
        String mensaje = String.format(
            "Hola %s,\n\n" +
            "Hemos recibido tu pago de %.2f EUR para el torneo '%s'.\n\n" +
            "Detalles del pago:\n" +
            "- Monto: %.2f EUR\n" +
            "- Fecha: %s\n" +
            "- Método: %s\n" +
            "- Comprobante: %s\n\n" +
            "Tu inscripción está ahora confirmada.\n\n" +
            "Equipo PadelClub Pro",
            jugador.getName(),
            pago.getMonto(),
            torneo.getNombre(),
            pago.getMonto(),
            pago.getFechaPago(),
            pago.getMetodo(),
            pago.getComprobante()
        );

        Map<String, String> variables = Map.of(
            "jugador_nombre", jugador.getName(),
            "torneo_nombre", torneo.getNombre(),
            "monto", String.format("%.2f", pago.getMonto()),
            "comprobante", pago.getComprobante() != null ? pago.getComprobante() : "N/A"
        );

        enviarNotificacion(jugador, TipoNotificacion.PAGO_CONFIRMADO,
                         CanalNotificacion.EMAIL, titulo, mensaje, variables);

        enviarNotificacion(jugador, TipoNotificacion.PAGO_CONFIRMADO,
                         CanalNotificacion.IN_APP, titulo, mensaje, variables);
    }

    /**
     * Notifica a los jugadores de un partido sobre su programación.
     */
    @Transactional
    public void notificarPartidoProgramado(Enfrentamiento enfrentamiento) {
        logger.info("Notificando partido programado: {}", enfrentamiento.getId());

        if (enfrentamiento.getFechaProgramada() == null) {
            logger.warn("El enfrentamiento no tiene fecha programada");
            return;
        }

        Equipo equipo1 = enfrentamiento.getEquipo1();
        Equipo equipo2 = enfrentamiento.getEquipo2();
        Pozo pozo = enfrentamiento.getPozo();

        String titulo = "Partido programado - " + pozo.getNombre();
        String mensajeBase = String.format(
            "Se ha programado tu partido:\n\n" +
            "%s vs %s\n\n" +
            "Fecha: %s\n" +
            "Pista: %s\n" +
            "Ronda: %s\n\n" +
            "¡Buena suerte!\n\n" +
            "Equipo PadelClub Pro",
            equipo1.getNombreEquipo(),
            equipo2.getNombreEquipo(),
            enfrentamiento.getFechaProgramada(),
            enfrentamiento.getPista() != null ? "Pista " + enfrentamiento.getPista() : "Por asignar",
            enfrentamiento.getRonda() != null ? enfrentamiento.getRonda() : "N/A"
        );

        // Notificar a jugadores del equipo 1
        notificarJugadoresEquipo(equipo1, titulo, mensajeBase);

        // Notificar a jugadores del equipo 2
        notificarJugadoresEquipo(equipo2, titulo, mensajeBase);
    }

    /**
     * Notifica resultado de partido a los jugadores.
     */
    @Transactional
    public void notificarResultadoPartido(Enfrentamiento enfrentamiento) {
        logger.info("Notificando resultado de partido: {}", enfrentamiento.getId());

        Equipo equipo1 = enfrentamiento.getEquipo1();
        Equipo equipo2 = enfrentamiento.getEquipo2();

        Integer ganador = determinarGanador(enfrentamiento);
        String resultado = String.format("%d - %d",
                                        enfrentamiento.getSetsEquipo1(),
                                        enfrentamiento.getSetsEquipo2());

        String titulo = "Resultado del partido";
        String mensaje = String.format(
            "Resultado del partido:\n\n" +
            "%s vs %s\n" +
            "Resultado: %s\n" +
            "Ganador: %s\n\n" +
            "¡Gracias por participar!\n\n" +
            "Equipo PadelClub Pro",
            equipo1.getNombreEquipo(),
            equipo2.getNombreEquipo(),
            resultado,
            ganador == 1 ? equipo1.getNombreEquipo() :
            ganador == 2 ? equipo2.getNombreEquipo() : "Empate"
        );

        // Notificar a todos los jugadores
        notificarJugadoresEquipo(equipo1, titulo, mensaje);
        notificarJugadoresEquipo(equipo2, titulo, mensaje);
    }

    /**
     * Envía recordatorio de partido próximo (24h antes).
     */
    @Transactional
    public void enviarRecordatorioPartido(Enfrentamiento enfrentamiento) {
        logger.info("Enviando recordatorio de partido: {}", enfrentamiento.getId());

        if (enfrentamiento.getFechaProgramada() == null) {
            return;
        }

        Equipo equipo1 = enfrentamiento.getEquipo1();
        Equipo equipo2 = enfrentamiento.getEquipo2();

        String titulo = "Recordatorio: Partido mañana";
        String mensaje = String.format(
            "Te recordamos que mañana tienes un partido programado:\n\n" +
            "%s vs %s\n" +
            "Fecha: %s\n" +
            "Pista: %s\n\n" +
            "¡No olvides asistir!\n\n" +
            "Equipo PadelClub Pro",
            equipo1.getNombreEquipo(),
            equipo2.getNombreEquipo(),
            enfrentamiento.getFechaProgramada(),
            enfrentamiento.getPista() != null ? "Pista " + enfrentamiento.getPista() : "Por asignar"
        );

        notificarJugadoresEquipo(equipo1, titulo, mensaje);
        notificarJugadoresEquipo(equipo2, titulo, mensaje);
    }

    /**
     * Marca una notificación como leída.
     */
    @Transactional
    public void marcarComoLeida(Long notificacionId) {
        notificacionRepository.findById(notificacionId).ifPresent(notificacion -> {
            notificacion.setLeida(true);
            notificacionRepository.save(notificacion);
            logger.info("Notificación {} marcada como leída", notificacionId);
        });
    }

    /**
     * Obtiene notificaciones de un usuario.
     */
    public List<Notificacion> obtenerNotificacionesUsuario(User usuario) {
        return notificacionRepository.findByDestinatario(usuario);
    }

    /**
     * Obtiene notificaciones no leídas de un usuario.
     */
    public List<Notificacion> obtenerNotificacionesNoLeidas(User usuario) {
        return notificacionRepository.findByDestinatarioAndLeida(usuario, false);
    }

    /**
     * Cuenta notificaciones no leídas de un usuario.
     */
    public long contarNotificacionesNoLeidas(User usuario) {
        return notificacionRepository.countByDestinatarioAndLeida(usuario, false);
    }

    // ==================== MÉTODOS PRIVADOS ====================

    /**
     * Notifica a todos los jugadores de un equipo.
     */
    private void notificarJugadoresEquipo(Equipo equipo, String titulo, String mensaje) {
        if (equipo.getParticipante1() != null) {
            enviarNotificacion(equipo.getParticipante1(), TipoNotificacion.PARTIDO_PROGRAMADO,
                             CanalNotificacion.EMAIL, titulo, mensaje, null);
            enviarNotificacion(equipo.getParticipante1(), TipoNotificacion.PARTIDO_PROGRAMADO,
                             CanalNotificacion.IN_APP, titulo, mensaje, null);
        }

        if (equipo.getParticipante2() != null) {
            enviarNotificacion(equipo.getParticipante2(), TipoNotificacion.PARTIDO_PROGRAMADO,
                             CanalNotificacion.EMAIL, titulo, mensaje, null);
            enviarNotificacion(equipo.getParticipante2(), TipoNotificacion.PARTIDO_PROGRAMADO,
                             CanalNotificacion.IN_APP, titulo, mensaje, null);
        }
    }

    /**
     * Determina el equipo ganador.
     */
    private Integer determinarGanador(Enfrentamiento enfrentamiento) {
        Integer setsEquipo1 = enfrentamiento.getSetsEquipo1();
        Integer setsEquipo2 = enfrentamiento.getSetsEquipo2();

        if (setsEquipo1 == null || setsEquipo2 == null) {
            return null;
        }

        if (setsEquipo1 > setsEquipo2) {
            return 1;
        } else if (setsEquipo2 > setsEquipo1) {
            return 2;
        } else {
            return null; // Empate
        }
    }
}
