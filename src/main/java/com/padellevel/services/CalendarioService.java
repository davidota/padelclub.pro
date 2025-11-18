package com.padellevel.services;

import com.padellevel.data.*;
import com.padellevel.repository.EnfrentamientoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Servicio para gestionar calendarios de partidos y eventos.
 *
 * Funcionalidades:
 * - Generar archivos .ics (iCalendar) para partidos
 * - Exportar calendario completo de un jugador
 * - Exportar calendario de un torneo
 * - Integración futura con Google Calendar API
 */
@Service
public class CalendarioService {

    private static final Logger logger = LoggerFactory.getLogger(CalendarioService.class);

    // Formato de fecha para iCalendar (ISO 8601 sin separadores)
    private static final DateTimeFormatter ICS_DATE_FORMAT =
        DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    private final EnfrentamientoRepository enfrentamientoRepository;

    public CalendarioService(EnfrentamientoRepository enfrentamientoRepository) {
        this.enfrentamientoRepository = enfrentamientoRepository;
    }

    /**
     * Genera un archivo .ics para un partido específico.
     *
     * @param enfrentamiento El partido a exportar
     * @return Contenido del archivo .ics
     */
    public String generarIcsParaPartido(Enfrentamiento enfrentamiento) {
        logger.info("Generando archivo .ics para enfrentamiento {}", enfrentamiento.getId());

        if (enfrentamiento.getFechaProgramada() == null) {
            throw new IllegalArgumentException("El partido no tiene fecha programada");
        }

        StringBuilder ics = new StringBuilder();

        // Cabecera del archivo iCalendar
        ics.append("BEGIN:VCALENDAR\r\n");
        ics.append("VERSION:2.0\r\n");
        ics.append("PRODID:-//PadelClub Pro//Tournament Manager//ES\r\n");
        ics.append("CALSCALE:GREGORIAN\r\n");
        ics.append("METHOD:PUBLISH\r\n");

        // Evento del partido
        ics.append("BEGIN:VEVENT\r\n");
        ics.append("UID:").append(generarUID(enfrentamiento)).append("\r\n");
        ics.append("DTSTAMP:").append(formatearFechaIcs(LocalDateTime.now())).append("\r\n");

        // Fecha y hora de inicio
        ics.append("DTSTART:").append(formatearFechaIcs(enfrentamiento.getFechaProgramada()))
           .append("\r\n");

        // Fecha y hora de fin (estimado 2 horas después)
        LocalDateTime fechaFin = enfrentamiento.getFechaProgramada().plusHours(2);
        ics.append("DTEND:").append(formatearFechaIcs(fechaFin)).append("\r\n");

        // Título del evento
        String titulo = generarTituloPartido(enfrentamiento);
        ics.append("SUMMARY:").append(escaparTextoIcs(titulo)).append("\r\n");

        // Descripción
        String descripcion = generarDescripcionPartido(enfrentamiento);
        ics.append("DESCRIPTION:").append(escaparTextoIcs(descripcion)).append("\r\n");

        // Ubicación
        String ubicacion = generarUbicacionPartido(enfrentamiento);
        if (ubicacion != null && !ubicacion.isEmpty()) {
            ics.append("LOCATION:").append(escaparTextoIcs(ubicacion)).append("\r\n");
        }

        // Estado del evento
        ics.append("STATUS:CONFIRMED\r\n");

        // Alarma de recordatorio (24 horas antes)
        ics.append("BEGIN:VALARM\r\n");
        ics.append("TRIGGER:-PT24H\r\n");
        ics.append("ACTION:DISPLAY\r\n");
        ics.append("DESCRIPTION:Recordatorio: Partido de pádel mañana\r\n");
        ics.append("END:VALARM\r\n");

        // Alarma de recordatorio (1 hora antes)
        ics.append("BEGIN:VALARM\r\n");
        ics.append("TRIGGER:-PT1H\r\n");
        ics.append("ACTION:DISPLAY\r\n");
        ics.append("DESCRIPTION:Recordatorio: Partido de pádel en 1 hora\r\n");
        ics.append("END:VALARM\r\n");

        ics.append("END:VEVENT\r\n");
        ics.append("END:VCALENDAR\r\n");

        logger.info("Archivo .ics generado exitosamente");
        return ics.toString();
    }

    /**
     * Genera un archivo .ics con todos los partidos de un jugador.
     *
     * @param jugador El jugador
     * @return Contenido del archivo .ics con todos sus partidos
     */
    public String generarIcsParaJugador(User jugador) {
        logger.info("Generando calendario completo para jugador {}", jugador.getUsername());

        // Obtener todos los partidos programados del jugador
        List<Enfrentamiento> partidos = obtenerPartidosProgramadosJugador(jugador);

        StringBuilder ics = new StringBuilder();

        // Cabecera del calendario
        ics.append("BEGIN:VCALENDAR\r\n");
        ics.append("VERSION:2.0\r\n");
        ics.append("PRODID:-//PadelClub Pro//Tournament Manager//ES\r\n");
        ics.append("CALSCALE:GREGORIAN\r\n");
        ics.append("METHOD:PUBLISH\r\n");
        ics.append("X-WR-CALNAME:Partidos de Pádel - ").append(jugador.getName()).append("\r\n");
        ics.append("X-WR-TIMEZONE:Europe/Madrid\r\n");

        // Agregar cada partido como evento
        for (Enfrentamiento partido : partidos) {
            if (partido.getFechaProgramada() != null) {
                agregarEventoPartido(ics, partido);
            }
        }

        ics.append("END:VCALENDAR\r\n");

        logger.info("Calendario generado con {} partidos", partidos.size());
        return ics.toString();
    }

    /**
     * Genera un archivo .ics con todos los partidos de un torneo.
     *
     * @param torneo El torneo
     * @return Contenido del archivo .ics con todos los partidos
     */
    public String generarIcsParaTorneo(Torneo torneo) {
        logger.info("Generando calendario para torneo {}", torneo.getNombre());

        // Obtener todos los partidos programados del torneo
        List<Enfrentamiento> partidos = obtenerPartidosProgramadosTorneo(torneo);

        StringBuilder ics = new StringBuilder();

        // Cabecera del calendario
        ics.append("BEGIN:VCALENDAR\r\n");
        ics.append("VERSION:2.0\r\n");
        ics.append("PRODID:-//PadelClub Pro//Tournament Manager//ES\r\n");
        ics.append("CALSCALE:GREGORIAN\r\n");
        ics.append("METHOD:PUBLISH\r\n");
        ics.append("X-WR-CALNAME:").append(escaparTextoIcs(torneo.getNombre())).append("\r\n");
        ics.append("X-WR-TIMEZONE:Europe/Madrid\r\n");

        // Agregar cada partido como evento
        for (Enfrentamiento partido : partidos) {
            if (partido.getFechaProgramada() != null) {
                agregarEventoPartido(ics, partido);
            }
        }

        ics.append("END:VCALENDAR\r\n");

        logger.info("Calendario generado con {} partidos", partidos.size());
        return ics.toString();
    }

    /**
     * Genera un archivo .ics con todos los partidos de un pozo.
     *
     * @param pozo El pozo
     * @return Contenido del archivo .ics con todos los partidos
     */
    public String generarIcsParaPozo(Pozo pozo) {
        logger.info("Generando calendario para pozo {}", pozo.getNombre());

        List<Enfrentamiento> partidos = enfrentamientoRepository.findByPozo(pozo);

        StringBuilder ics = new StringBuilder();

        ics.append("BEGIN:VCALENDAR\r\n");
        ics.append("VERSION:2.0\r\n");
        ics.append("PRODID:-//PadelClub Pro//Tournament Manager//ES\r\n");
        ics.append("CALSCALE:GREGORIAN\r\n");
        ics.append("METHOD:PUBLISH\r\n");
        ics.append("X-WR-CALNAME:").append(escaparTextoIcs(pozo.getNombre())).append("\r\n");
        ics.append("X-WR-TIMEZONE:Europe/Madrid\r\n");

        for (Enfrentamiento partido : partidos) {
            if (partido.getFechaProgramada() != null) {
                agregarEventoPartido(ics, partido);
            }
        }

        ics.append("END:VCALENDAR\r\n");

        logger.info("Calendario generado con {} partidos", partidos.size());
        return ics.toString();
    }

    // ==================== MÉTODOS PRIVADOS ====================

    /**
     * Agrega un evento de partido al calendario .ics.
     */
    private void agregarEventoPartido(StringBuilder ics, Enfrentamiento partido) {
        ics.append("BEGIN:VEVENT\r\n");
        ics.append("UID:").append(generarUID(partido)).append("\r\n");
        ics.append("DTSTAMP:").append(formatearFechaIcs(LocalDateTime.now())).append("\r\n");

        ics.append("DTSTART:").append(formatearFechaIcs(partido.getFechaProgramada()))
           .append("\r\n");

        LocalDateTime fechaFin = partido.getFechaProgramada().plusHours(2);
        ics.append("DTEND:").append(formatearFechaIcs(fechaFin)).append("\r\n");

        String titulo = generarTituloPartido(partido);
        ics.append("SUMMARY:").append(escaparTextoIcs(titulo)).append("\r\n");

        String descripcion = generarDescripcionPartido(partido);
        ics.append("DESCRIPTION:").append(escaparTextoIcs(descripcion)).append("\r\n");

        String ubicacion = generarUbicacionPartido(partido);
        if (ubicacion != null && !ubicacion.isEmpty()) {
            ics.append("LOCATION:").append(escaparTextoIcs(ubicacion)).append("\r\n");
        }

        ics.append("STATUS:CONFIRMED\r\n");

        // Recordatorios
        ics.append("BEGIN:VALARM\r\n");
        ics.append("TRIGGER:-PT24H\r\n");
        ics.append("ACTION:DISPLAY\r\n");
        ics.append("DESCRIPTION:Partido mañana\r\n");
        ics.append("END:VALARM\r\n");

        ics.append("END:VEVENT\r\n");
    }

    /**
     * Genera un UID único para el evento.
     */
    private String generarUID(Enfrentamiento partido) {
        return "enfrentamiento-" + partido.getId() + "@padelclub.pro";
    }

    /**
     * Formatea una fecha al formato requerido por iCalendar.
     */
    private String formatearFechaIcs(LocalDateTime fecha) {
        return fecha.format(ICS_DATE_FORMAT);
    }

    /**
     * Escapa texto para formato iCalendar (reemplaza saltos de línea, etc).
     */
    private String escaparTextoIcs(String texto) {
        return texto
            .replace("\\", "\\\\")
            .replace(",", "\\,")
            .replace(";", "\\;")
            .replace("\n", "\\n")
            .replace("\r", "");
    }

    /**
     * Genera el título del partido para el calendario.
     */
    private String generarTituloPartido(Enfrentamiento partido) {
        Equipo equipo1 = partido.getEquipo1();
        Equipo equipo2 = partido.getEquipo2();

        return String.format("Pádel: %s vs %s",
                           equipo1 != null ? equipo1.getNombreEquipo() : "TBD",
                           equipo2 != null ? equipo2.getNombreEquipo() : "TBD");
    }

    /**
     * Genera la descripción del partido.
     */
    private String generarDescripcionPartido(Enfrentamiento partido) {
        StringBuilder desc = new StringBuilder();

        Pozo pozo = partido.getPozo();
        if (pozo != null) {
            desc.append("Torneo: ").append(pozo.getTorneo().getNombre()).append("\\n");
            desc.append("Pozo: ").append(pozo.getNombre()).append("\\n");
        }

        if (partido.getRonda() != null) {
            desc.append("Ronda: ").append(partido.getRonda()).append("\\n");
        }

        if (partido.getPista() != null) {
            desc.append("Pista: ").append(partido.getPista()).append("\\n");
        }

        desc.append("\\nRecuerda llegar 15 minutos antes del partido.");

        return desc.toString();
    }

    /**
     * Genera la ubicación del partido.
     */
    private String generarUbicacionPartido(Enfrentamiento partido) {
        Pozo pozo = partido.getPozo();
        if (pozo != null && pozo.getTorneo() != null) {
            Club club = pozo.getTorneo().getClub();
            if (club != null) {
                StringBuilder ubicacion = new StringBuilder();
                ubicacion.append(club.getNombre());

                if (club.getDireccion() != null && !club.getDireccion().isEmpty()) {
                    ubicacion.append(", ").append(club.getDireccion());
                }

                if (partido.getPista() != null) {
                    ubicacion.append(" - Pista ").append(partido.getPista());
                }

                return ubicacion.toString();
            }
        }

        return null;
    }

    /**
     * Obtiene todos los partidos programados de un jugador.
     */
    private List<Enfrentamiento> obtenerPartidosProgramadosJugador(User jugador) {
        // TODO: Implementar query para obtener partidos del jugador
        // Por ahora retorna lista vacía
        return List.of();
    }

    /**
     * Obtiene todos los partidos programados de un torneo.
     */
    private List<Enfrentamiento> obtenerPartidosProgramadosTorneo(Torneo torneo) {
        // TODO: Implementar query para obtener partidos del torneo
        // Por ahora retorna lista vacía
        return List.of();
    }

    /**
     * TODO: Integración futura con Google Calendar API.
     *
     * Métodos a implementar:
     * - sincronizarConGoogleCalendar(User jugador)
     * - crearEventoEnGoogleCalendar(Enfrentamiento partido)
     * - actualizarEventoEnGoogleCalendar(Enfrentamiento partido)
     * - eliminarEventoDeGoogleCalendar(Enfrentamiento partido)
     */
}
