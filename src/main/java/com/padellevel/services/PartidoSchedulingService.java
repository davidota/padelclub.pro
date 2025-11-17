package com.padellevel.services;

import com.padellevel.data.*;
import com.padellevel.repository.EnfrentamientoRepository;
import com.padellevel.repository.HorarioDisponibleRepository;
import com.padellevel.repository.PistaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio integral para la programación y gestión de horarios de partidos (enfrentamientos).
 *
 * Este servicio maneja:
 * - Asignación de pistas (canchas) a partidos
 * - Asignación de fechas y horarios a partidos
 * - Prevención de conflictos de programación
 * - Optimización del uso de pistas
 * - Reprogramación de partidos
 * - Verificación de disponibilidad
 *
 * @author PadelClub Team
 * @version 1.0
 */
@Service
public class PartidoSchedulingService {

    private static final Logger logger = LoggerFactory.getLogger(PartidoSchedulingService.class);

    private final EnfrentamientoRepository enfrentamientoRepository;
    private final HorarioDisponibleRepository horarioDisponibleRepository;
    private final PistaRepository pistaRepository;

    /**
     * Constructor con inyección de dependencias.
     *
     * @param enfrentamientoRepository Repositorio de enfrentamientos
     * @param horarioDisponibleRepository Repositorio de horarios disponibles
     * @param pistaRepository Repositorio de pistas
     */
    public PartidoSchedulingService(
            EnfrentamientoRepository enfrentamientoRepository,
            HorarioDisponibleRepository horarioDisponibleRepository,
            PistaRepository pistaRepository) {
        this.enfrentamientoRepository = enfrentamientoRepository;
        this.horarioDisponibleRepository = horarioDisponibleRepository;
        this.pistaRepository = pistaRepository;
    }

    /**
     * Programa un partido individual asignando pista, fecha y hora.
     *
     * El método realiza las siguientes validaciones:
     * - Verifica que el enfrentamiento no esté nulo
     * - Verifica que el partido no esté ya programado o finalizado
     * - Valida que la pista esté disponible en el horario especificado
     * - Previene conflictos de programación
     *
     * @param enfrentamiento El enfrentamiento a programar (no puede ser null)
     * @param pistaNúmero Número de la pista a asignar (debe ser válido)
     * @param fechaHora Fecha y hora programada (no puede ser en el pasado)
     * @return El enfrentamiento programado actualizado
     * @throws IllegalArgumentException si los parámetros son inválidos
     * @throws IllegalStateException si hay conflictos de programación
     */
    @Transactional
    public Enfrentamiento programarPartido(Enfrentamiento enfrentamiento, Integer pistaNúmero, LocalDateTime fechaHora) {
        logger.info("Iniciando programación de partido ID: {} en pista {} para fecha {}",
                   enfrentamiento != null ? enfrentamiento.getId() : "null", pistaNúmero, fechaHora);

        // Validación de parámetros
        if (enfrentamiento == null) {
            logger.error("Error: El enfrentamiento no puede ser null");
            throw new IllegalArgumentException("El enfrentamiento no puede ser null");
        }

        if (pistaNúmero == null) {
            logger.error("Error: El número de pista no puede ser null");
            throw new IllegalArgumentException("El número de pista no puede ser null");
        }

        if (fechaHora == null) {
            logger.error("Error: La fecha y hora no pueden ser null");
            throw new IllegalArgumentException("La fecha y hora no pueden ser null");
        }

        if (fechaHora.isBefore(LocalDateTime.now())) {
            logger.error("Error: No se puede programar un partido en el pasado: {}", fechaHora);
            throw new IllegalArgumentException("No se puede programar un partido en el pasado");
        }

        // Verificar estado del enfrentamiento
        if (enfrentamiento.getEstado() == EstadoEnfrentamiento.FINALIZADO) {
            logger.error("Error: No se puede programar un partido ya finalizado ID: {}", enfrentamiento.getId());
            throw new IllegalStateException("No se puede programar un partido ya finalizado");
        }

        if (enfrentamiento.getEstado() == EstadoEnfrentamiento.CANCELADO) {
            logger.error("Error: No se puede programar un partido cancelado ID: {}", enfrentamiento.getId());
            throw new IllegalStateException("No se puede programar un partido cancelado");
        }

        // Verificar disponibilidad de la pista
        if (!verificarDisponibilidadPista(pistaNúmero, fechaHora)) {
            logger.warn("La pista {} no está disponible en {}. Buscando conflictos...", pistaNúmero, fechaHora);
            List<Enfrentamiento> conflictos = buscarConflictosPista(pistaNúmero, fechaHora);
            if (!conflictos.isEmpty()) {
                logger.error("Conflicto de programación: La pista {} ya tiene {} partido(s) programado(s) en {}",
                           pistaNúmero, conflictos.size(), fechaHora);
                throw new IllegalStateException(
                    String.format("La pista %d ya está ocupada en %s. Hay %d partido(s) en conflicto.",
                               pistaNúmero, fechaHora, conflictos.size()));
            }
        }

        // Asignar valores al enfrentamiento
        enfrentamiento.setPista(pistaNúmero);
        enfrentamiento.setFechaProgramada(fechaHora);
        enfrentamiento.setEstado(EstadoEnfrentamiento.PROGRAMADO);

        // Guardar el enfrentamiento
        Enfrentamiento partidoProgramado = enfrentamientoRepository.save(enfrentamiento);

        // Intentar asociar con un horario disponible si existe
        actualizarHorarioDisponible(pistaNúmero, fechaHora, enfrentamiento);

        logger.info("Partido ID: {} programado exitosamente en pista {} para {}",
                   partidoProgramado.getId(), pistaNúmero, fechaHora);
        logger.debug("Estado del partido actualizado a: {}", partidoProgramado.getEstado());

        return partidoProgramado;
    }

    /**
     * Programa todos los partidos de un pozo utilizando los horarios disponibles proporcionados.
     *
     * Este método intenta optimizar la asignación de pistas y horarios para minimizar
     * los espacios vacíos y distribuir equitativamente los partidos.
     *
     * @param pozo El pozo cuyos partidos se van a programar (no puede ser null)
     * @param horariosDisponibles Lista de horarios disponibles para asignar (no puede ser null o vacía)
     * @return Lista de enfrentamientos programados exitosamente
     * @throws IllegalArgumentException si los parámetros son inválidos
     */
    @Transactional
    public List<Enfrentamiento> programarPozoCompleto(Pozo pozo, List<HorarioDisponible> horariosDisponibles) {
        logger.info("Iniciando programación completa del pozo: {}", pozo != null ? pozo.getNombre() : "null");

        // Validación de parámetros
        if (pozo == null) {
            logger.error("Error: El pozo no puede ser null");
            throw new IllegalArgumentException("El pozo no puede ser null");
        }

        if (horariosDisponibles == null || horariosDisponibles.isEmpty()) {
            logger.error("Error: Debe proporcionar al menos un horario disponible");
            throw new IllegalArgumentException("Debe proporcionar al menos un horario disponible");
        }

        // Obtener partidos pendientes de programación
        List<Enfrentamiento> partidosPendientes = obtenerPartidosPendientesProgramacion(pozo);

        if (partidosPendientes.isEmpty()) {
            logger.info("No hay partidos pendientes de programación en el pozo: {}", pozo.getNombre());
            return new ArrayList<>();
        }

        logger.info("Encontrados {} partidos pendientes de programación", partidosPendientes.size());
        logger.info("Horarios disponibles: {}", horariosDisponibles.size());

        // Verificar que hay suficientes horarios
        if (horariosDisponibles.size() < partidosPendientes.size()) {
            logger.warn("ADVERTENCIA: Hay {} partidos pero solo {} horarios disponibles. " +
                       "No se podrán programar todos los partidos.",
                       partidosPendientes.size(), horariosDisponibles.size());
        }

        // Ordenar horarios por fecha y hora
        List<HorarioDisponible> horariosOrdenados = horariosDisponibles.stream()
            .filter(h -> h.getDisponible() && h.getEnfrentamiento() == null)
            .sorted(Comparator.comparing(HorarioDisponible::getFecha)
                             .thenComparing(HorarioDisponible::getHoraInicio))
            .collect(Collectors.toList());

        logger.debug("Horarios ordenados y disponibles: {}", horariosOrdenados.size());

        // Programar partidos uno por uno
        List<Enfrentamiento> partidosProgramados = new ArrayList<>();
        int horarioIndex = 0;

        for (Enfrentamiento partido : partidosPendientes) {
            if (horarioIndex >= horariosOrdenados.size()) {
                logger.warn("Se agotaron los horarios disponibles. Partidos programados: {}/{}",
                           partidosProgramados.size(), partidosPendientes.size());
                break;
            }

            HorarioDisponible horario = horariosOrdenados.get(horarioIndex);

            try {
                // Construir LocalDateTime a partir del horario
                LocalDateTime fechaHora = LocalDateTime.of(horario.getFecha(), horario.getHoraInicio());
                Integer pistaNúmero = horario.getPista().getNumero();

                logger.debug("Programando partido ID: {} en pista {} para {}",
                           partido.getId(), pistaNúmero, fechaHora);

                // Programar el partido
                Enfrentamiento programado = programarPartido(partido, pistaNúmero, fechaHora);
                partidosProgramados.add(programado);

                // Marcar el horario como ocupado
                horario.setDisponible(false);
                horario.setEnfrentamiento(programado);
                horarioDisponibleRepository.save(horario);

                horarioIndex++;

            } catch (Exception e) {
                logger.error("Error al programar partido ID: {}: {}", partido.getId(), e.getMessage());
                // Continuar con el siguiente partido
                horarioIndex++;
            }
        }

        logger.info("Programación completa finalizada. Partidos programados: {}/{}",
                   partidosProgramados.size(), partidosPendientes.size());

        return partidosProgramados;
    }

    /**
     * Reprograma un partido a una nueva fecha y hora.
     *
     * Libera el horario anterior (si existe) y asigna el nuevo horario,
     * verificando la disponibilidad.
     *
     * @param enfrentamiento El enfrentamiento a reprogramar (no puede ser null)
     * @param nuevaFecha Nueva fecha y hora para el partido (no puede ser null)
     * @return El enfrentamiento reprogramado
     * @throws IllegalArgumentException si los parámetros son inválidos
     * @throws IllegalStateException si el partido no puede ser reprogramado
     */
    @Transactional
    public Enfrentamiento reprogramarPartido(Enfrentamiento enfrentamiento, LocalDateTime nuevaFecha) {
        logger.info("Iniciando reprogramación del partido ID: {} a nueva fecha: {}",
                   enfrentamiento != null ? enfrentamiento.getId() : "null", nuevaFecha);

        // Validación de parámetros
        if (enfrentamiento == null) {
            logger.error("Error: El enfrentamiento no puede ser null");
            throw new IllegalArgumentException("El enfrentamiento no puede ser null");
        }

        if (nuevaFecha == null) {
            logger.error("Error: La nueva fecha no puede ser null");
            throw new IllegalArgumentException("La nueva fecha no puede ser null");
        }

        if (nuevaFecha.isBefore(LocalDateTime.now())) {
            logger.error("Error: No se puede reprogramar un partido en el pasado: {}", nuevaFecha);
            throw new IllegalArgumentException("No se puede reprogramar un partido en el pasado");
        }

        // Verificar que el partido puede ser reprogramado
        if (enfrentamiento.getEstado() == EstadoEnfrentamiento.FINALIZADO) {
            logger.error("Error: No se puede reprogramar un partido finalizado ID: {}", enfrentamiento.getId());
            throw new IllegalStateException("No se puede reprogramar un partido ya finalizado");
        }

        if (enfrentamiento.getEstado() == EstadoEnfrentamiento.CANCELADO) {
            logger.error("Error: No se puede reprogramar un partido cancelado ID: {}", enfrentamiento.getId());
            throw new IllegalStateException("No se puede reprogramar un partido cancelado");
        }

        if (enfrentamiento.getEstado() == EstadoEnfrentamiento.EN_CURSO) {
            logger.error("Error: No se puede reprogramar un partido en curso ID: {}", enfrentamiento.getId());
            throw new IllegalStateException("No se puede reprogramar un partido en curso");
        }

        // Guardar información previa
        LocalDateTime fechaAnterior = enfrentamiento.getFechaProgramada();
        Integer pistaAnterior = enfrentamiento.getPista();

        logger.debug("Fecha anterior: {}, Pista anterior: {}", fechaAnterior, pistaAnterior);

        // Liberar horario anterior si existe
        if (fechaAnterior != null && pistaAnterior != null) {
            liberarHorarioDisponible(pistaAnterior, fechaAnterior, enfrentamiento);
        }

        // Usar la misma pista si está disponible, o buscar una nueva
        Integer pistaNueva = pistaAnterior;
        if (pistaNueva == null || !verificarDisponibilidadPista(pistaNueva, nuevaFecha)) {
            logger.debug("Pista {} no disponible, buscando pista alternativa...", pistaNueva);
            pistaNueva = buscarPistaDisponible(nuevaFecha);

            if (pistaNueva == null) {
                logger.error("No hay pistas disponibles para la fecha: {}", nuevaFecha);
                throw new IllegalStateException("No hay pistas disponibles para la fecha especificada");
            }
            logger.info("Pista alternativa encontrada: {}", pistaNueva);
        }

        // Programar en la nueva fecha
        enfrentamiento.setPista(pistaNueva);
        enfrentamiento.setFechaProgramada(nuevaFecha);

        // Actualizar estado si estaba pospuesto
        if (enfrentamiento.getEstado() == EstadoEnfrentamiento.POSPUESTO) {
            enfrentamiento.setEstado(EstadoEnfrentamiento.PROGRAMADO);
        }

        // Guardar cambios
        Enfrentamiento partidoReprogramado = enfrentamientoRepository.save(enfrentamiento);

        // Actualizar horario disponible
        actualizarHorarioDisponible(pistaNueva, nuevaFecha, enfrentamiento);

        logger.info("Partido ID: {} reprogramado exitosamente de {} a {}",
                   partidoReprogramado.getId(), fechaAnterior, nuevaFecha);

        return partidoReprogramado;
    }

    /**
     * Verifica si una pista está disponible en una fecha y hora específicas.
     *
     * Una pista está disponible si:
     * - No hay otros partidos programados en ese horario
     * - Existe un horario disponible marcado como libre
     *
     * @param pistaNúmero Número de la pista a verificar
     * @param fecha Fecha y hora a verificar
     * @return true si la pista está disponible, false en caso contrario
     */
    public boolean verificarDisponibilidadPista(Integer pistaNúmero, LocalDateTime fecha) {
        logger.debug("Verificando disponibilidad de pista {} para fecha {}", pistaNúmero, fecha);

        if (pistaNúmero == null || fecha == null) {
            logger.warn("Parámetros inválidos para verificar disponibilidad: pista={}, fecha={}",
                       pistaNúmero, fecha);
            return false;
        }

        // Verificar conflictos con partidos ya programados
        List<Enfrentamiento> conflictos = buscarConflictosPista(pistaNúmero, fecha);

        if (!conflictos.isEmpty()) {
            logger.debug("Pista {} no disponible: {} conflicto(s) encontrado(s)",
                        pistaNúmero, conflictos.size());
            return false;
        }

        logger.debug("Pista {} disponible para {}", pistaNúmero, fecha);
        return true;
    }

    /**
     * Obtiene los próximos partidos programados de un pozo, ordenados por fecha.
     *
     * @param pozo El pozo del cual obtener los partidos (no puede ser null)
     * @param limite Número máximo de partidos a retornar (debe ser mayor a 0)
     * @return Lista de los próximos partidos ordenados por fecha programada
     * @throws IllegalArgumentException si los parámetros son inválidos
     */
    public List<Enfrentamiento> obtenerProximosPartidos(Pozo pozo, int limite) {
        logger.debug("Obteniendo próximos {} partidos del pozo: {}",
                    limite, pozo != null ? pozo.getNombre() : "null");

        if (pozo == null) {
            logger.error("Error: El pozo no puede ser null");
            throw new IllegalArgumentException("El pozo no puede ser null");
        }

        if (limite <= 0) {
            logger.error("Error: El límite debe ser mayor a 0, recibido: {}", limite);
            throw new IllegalArgumentException("El límite debe ser mayor a 0");
        }

        LocalDateTime ahora = LocalDateTime.now();

        // Obtener partidos programados del pozo
        List<Enfrentamiento> todosPartidos = enfrentamientoRepository.findByPozo(pozo);

        // Filtrar y ordenar
        List<Enfrentamiento> proximosPartidos = todosPartidos.stream()
            .filter(e -> e.getFechaProgramada() != null)
            .filter(e -> e.getFechaProgramada().isAfter(ahora))
            .filter(e -> e.getEstado() == EstadoEnfrentamiento.PROGRAMADO)
            .sorted(Comparator.comparing(Enfrentamiento::getFechaProgramada))
            .limit(limite)
            .collect(Collectors.toList());

        logger.info("Encontrados {} próximos partidos del pozo {}",
                   proximosPartidos.size(), pozo.getNombre());

        return proximosPartidos;
    }

    /**
     * Obtiene todos los partidos pendientes de programación en un pozo.
     *
     * Un partido está pendiente de programación si:
     * - Su estado es PENDIENTE_PROGRAMACION
     * - No tiene fecha programada asignada
     *
     * @param pozo El pozo del cual obtener los partidos pendientes (no puede ser null)
     * @return Lista de partidos pendientes de programación
     * @throws IllegalArgumentException si el pozo es null
     */
    public List<Enfrentamiento> obtenerPartidosPendientesProgramacion(Pozo pozo) {
        logger.debug("Obteniendo partidos pendientes de programación del pozo: {}",
                    pozo != null ? pozo.getNombre() : "null");

        if (pozo == null) {
            logger.error("Error: El pozo no puede ser null");
            throw new IllegalArgumentException("El pozo no puede ser null");
        }

        List<Enfrentamiento> pendientes = enfrentamientoRepository
            .findByPozoIdAndEstado(pozo.getId(), EstadoEnfrentamiento.PENDIENTE_PROGRAMACION);

        logger.info("Encontrados {} partidos pendientes de programación en el pozo {}",
                   pendientes.size(), pozo.getNombre());

        return pendientes;
    }

    // ==================== MÉTODOS AUXILIARES PRIVADOS ====================

    /**
     * Busca conflictos de programación en una pista específica para un horario dado.
     * Considera un margen de 2 horas para detectar solapamientos.
     */
    private List<Enfrentamiento> buscarConflictosPista(Integer pistaNúmero, LocalDateTime fecha) {
        // Margen de 2 horas para detectar conflictos
        LocalDateTime inicio = fecha.minusHours(2);
        LocalDateTime fin = fecha.plusHours(2);

        List<Enfrentamiento> partidosEnRango = enfrentamientoRepository
            .findByFechaProgramadaBetween(inicio, fin);

        return partidosEnRango.stream()
            .filter(e -> pistaNúmero.equals(e.getPista()))
            .filter(e -> e.getEstado() != EstadoEnfrentamiento.CANCELADO)
            .filter(e -> e.getEstado() != EstadoEnfrentamiento.FINALIZADO)
            .collect(Collectors.toList());
    }

    /**
     * Busca una pista disponible para una fecha y hora específicas.
     *
     * @param fecha Fecha y hora para buscar disponibilidad
     * @return Número de pista disponible, o null si no hay ninguna disponible
     */
    private Integer buscarPistaDisponible(LocalDateTime fecha) {
        logger.debug("Buscando pista disponible para fecha: {}", fecha);

        LocalDate fechaSolo = fecha.toLocalDate();
        LocalTime horaSolo = fecha.toLocalTime();

        // Buscar en horarios disponibles
        List<HorarioDisponible> horarios = horarioDisponibleRepository.findAll();

        Optional<HorarioDisponible> horarioLibre = horarios.stream()
            .filter(h -> h.getDisponible())
            .filter(h -> h.getEnfrentamiento() == null)
            .filter(h -> h.getFecha().equals(fechaSolo))
            .filter(h -> !h.getHoraInicio().isAfter(horaSolo))
            .filter(h -> !h.getHoraFin().isBefore(horaSolo))
            .findFirst();

        if (horarioLibre.isPresent()) {
            Integer pistaNúmero = horarioLibre.get().getPista().getNumero();
            logger.debug("Pista disponible encontrada: {}", pistaNúmero);
            return pistaNúmero;
        }

        // Si no hay horarios disponibles, intentar con las pistas existentes
        List<Pista> todasPistas = pistaRepository.findAll();

        for (Pista pista : todasPistas) {
            if (pista.getDisponible() && verificarDisponibilidadPista(pista.getNumero(), fecha)) {
                logger.debug("Pista disponible encontrada (verificación directa): {}", pista.getNumero());
                return pista.getNumero();
            }
        }

        logger.warn("No se encontró ninguna pista disponible para {}", fecha);
        return null;
    }

    /**
     * Actualiza o crea un registro de horario disponible asociado al enfrentamiento.
     */
    private void actualizarHorarioDisponible(Integer pistaNúmero, LocalDateTime fechaHora,
                                            Enfrentamiento enfrentamiento) {
        try {
            LocalDate fecha = fechaHora.toLocalDate();
            LocalTime hora = fechaHora.toLocalTime();

            // Buscar pista
            Optional<Pista> pistaOpt = pistaRepository.findByNumero(pistaNúmero);

            if (pistaOpt.isPresent()) {
                Pista pista = pistaOpt.get();

                // Buscar horario disponible existente
                Optional<HorarioDisponible> horarioOpt = horarioDisponibleRepository
                    .findByPistaAndFechaAndHoraInicio(pista, fecha, hora);

                if (horarioOpt.isPresent()) {
                    HorarioDisponible horario = horarioOpt.get();
                    horario.setDisponible(false);
                    horario.setEnfrentamiento(enfrentamiento);
                    horarioDisponibleRepository.save(horario);
                    logger.debug("Horario disponible actualizado para pista {} en {}", pistaNúmero, fechaHora);
                } else {
                    logger.debug("No se encontró horario disponible exacto para pista {} en {}",
                               pistaNúmero, fechaHora);
                }
            }
        } catch (Exception e) {
            logger.warn("No se pudo actualizar horario disponible: {}", e.getMessage());
            // No lanzar excepción, esto es una operación secundaria
        }
    }

    /**
     * Libera un horario disponible previamente ocupado por un enfrentamiento.
     */
    private void liberarHorarioDisponible(Integer pistaNúmero, LocalDateTime fechaHora,
                                         Enfrentamiento enfrentamiento) {
        try {
            LocalDate fecha = fechaHora.toLocalDate();
            LocalTime hora = fechaHora.toLocalTime();

            // Buscar pista
            Optional<Pista> pistaOpt = pistaRepository.findByNumero(pistaNúmero);

            if (pistaOpt.isPresent()) {
                Pista pista = pistaOpt.get();

                // Buscar y liberar horario
                Optional<HorarioDisponible> horarioOpt = horarioDisponibleRepository
                    .findByPistaAndFechaAndHoraInicio(pista, fecha, hora);

                if (horarioOpt.isPresent()) {
                    HorarioDisponible horario = horarioOpt.get();
                    if (enfrentamiento.equals(horario.getEnfrentamiento())) {
                        horario.setDisponible(true);
                        horario.setEnfrentamiento(null);
                        horarioDisponibleRepository.save(horario);
                        logger.debug("Horario disponible liberado para pista {} en {}", pistaNúmero, fechaHora);
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("No se pudo liberar horario disponible: {}", e.getMessage());
            // No lanzar excepción, esto es una operación secundaria
        }
    }
}
