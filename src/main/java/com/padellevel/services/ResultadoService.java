package com.padellevel.services;

import com.padellevel.data.*;
import com.padellevel.repository.EnfrentamientoRepository;
import com.padellevel.services.modalidades.ModalidadCoordinatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio integral para el registro y gestión de resultados de partidos.
 *
 * Este servicio maneja:
 * - Registro de resultados de partidos con sets y puntajes
 * - Cálculo de ganadores basado en sets
 * - Actualización de estados de enfrentamientos
 * - Validación de resultados
 * - Registro de walkovers
 * - Cancelación de partidos
 * - Avance de ganadores en eliminatorias (mediante ModalidadCoordinatorService)
 *
 * @author PadelClub Team
 * @version 1.0
 */
@Service
public class ResultadoService {

    private static final Logger logger = LoggerFactory.getLogger(ResultadoService.class);

    private final EnfrentamientoRepository enfrentamientoRepository;
    private final ModalidadCoordinatorService modalidadCoordinatorService;

    /**
     * Constructor con inyección de dependencias.
     *
     * @param enfrentamientoRepository Repositorio de enfrentamientos
     * @param modalidadCoordinatorService Servicio coordinador de modalidades
     */
    public ResultadoService(
            EnfrentamientoRepository enfrentamientoRepository,
            ModalidadCoordinatorService modalidadCoordinatorService) {
        this.enfrentamientoRepository = enfrentamientoRepository;
        this.modalidadCoordinatorService = modalidadCoordinatorService;
    }

    /**
     * Registra el resultado completo de un partido incluyendo los sets jugados.
     *
     * Este método:
     * - Valida que el resultado sea coherente
     * - Asigna los sets al enfrentamiento
     * - Calcula el ganador automáticamente
     * - Actualiza el estado del partido a FINALIZADO
     * - Registra la fecha de finalización
     * - Avanza al ganador en eliminatorias (si aplica)
     *
     * @param enfrentamiento El enfrentamiento del cual registrar el resultado (no puede ser null)
     * @param sets Lista de sets jugados con sus puntajes (no puede ser null o vacía)
     * @return El enfrentamiento actualizado con el resultado
     * @throws IllegalArgumentException si los parámetros son inválidos o el resultado no es válido
     * @throws IllegalStateException si el partido no puede recibir un resultado
     */
    @Transactional
    public Enfrentamiento registrarResultado(Enfrentamiento enfrentamiento, List<SetEntity> sets) {
        logger.info("Iniciando registro de resultado para partido ID: {}",
                   enfrentamiento != null ? enfrentamiento.getId() : "null");

        // Validación de parámetros
        if (enfrentamiento == null) {
            logger.error("Error: El enfrentamiento no puede ser null");
            throw new IllegalArgumentException("El enfrentamiento no puede ser null");
        }

        if (sets == null || sets.isEmpty()) {
            logger.error("Error: Debe proporcionar al menos un set para el resultado");
            throw new IllegalArgumentException("Debe proporcionar al menos un set para el resultado");
        }

        // Verificar que el partido puede recibir resultado
        if (enfrentamiento.getEstado() == EstadoEnfrentamiento.FINALIZADO) {
            logger.error("Error: El partido ID: {} ya está finalizado", enfrentamiento.getId());
            throw new IllegalStateException("El partido ya tiene un resultado registrado");
        }

        if (enfrentamiento.getEstado() == EstadoEnfrentamiento.CANCELADO) {
            logger.error("Error: El partido ID: {} está cancelado", enfrentamiento.getId());
            throw new IllegalStateException("No se puede registrar resultado en un partido cancelado");
        }

        // Verificar que hay equipos asignados
        if (enfrentamiento.getEquipo1() == null || enfrentamiento.getEquipo2() == null) {
            logger.error("Error: El partido ID: {} no tiene equipos asignados", enfrentamiento.getId());
            throw new IllegalStateException("El partido debe tener dos equipos asignados");
        }

        // Validar el resultado
        validarResultado(sets);

        logger.debug("Resultado validado correctamente. Sets: {}", sets.size());

        // Asignar sets al enfrentamiento
        enfrentamiento.setSets(sets);

        // Calcular sets ganados por cada equipo
        enfrentamiento.calcularSetsGanados();

        logger.info("Sets calculados - Equipo 1: {}, Equipo 2: {}",
                   enfrentamiento.getSetsEquipo1(), enfrentamiento.getSetsEquipo2());

        // Actualizar estado del partido
        enfrentamiento.setEstado(EstadoEnfrentamiento.FINALIZADO);
        enfrentamiento.setFechaJugado(LocalDateTime.now());

        // Guardar el enfrentamiento
        Enfrentamiento partidoFinalizado = enfrentamientoRepository.save(enfrentamiento);

        logger.info("Resultado registrado exitosamente para partido ID: {}. Ganador: Equipo {}",
                   partidoFinalizado.getId(), partidoFinalizado.getGanador());

        // Calcular y avanzar ganador si es necesario (en eliminatorias)
        try {
            Equipo equipoGanador = calcularGanador(partidoFinalizado);
            if (equipoGanador != null && partidoFinalizado.getPozo() != null) {
                logger.debug("Intentando avanzar ganador en modalidad: {}",
                           partidoFinalizado.getPozo().getModalidad());
                modalidadCoordinatorService.avanzarGanador(partidoFinalizado, equipoGanador);
                logger.info("Ganador avanzado exitosamente en la eliminatoria");
            }
        } catch (Exception e) {
            logger.warn("No se pudo avanzar ganador (puede ser normal en algunas modalidades): {}",
                       e.getMessage());
            // No fallar el registro de resultado por esto
        }

        return partidoFinalizado;
    }

    /**
     * Método simplificado para registrar resultados proporcionando el conteo de sets
     * y la lista detallada de sets.
     *
     * @param enfrentamiento El enfrentamiento del cual registrar el resultado (no puede ser null)
     * @param setsEquipo1 Número de sets ganados por el equipo 1
     * @param setsEquipo2 Número de sets ganados por el equipo 2
     * @param sets Lista detallada de sets jugados (no puede ser null o vacía)
     * @return El enfrentamiento actualizado con el resultado
     * @throws IllegalArgumentException si los parámetros son inválidos
     */
    @Transactional
    public Enfrentamiento registrarResultadoSimple(Enfrentamiento enfrentamiento,
                                                   int setsEquipo1,
                                                   int setsEquipo2,
                                                   List<SetEntity> sets) {
        logger.info("Registrando resultado simple para partido ID: {} ({}-{})",
                   enfrentamiento != null ? enfrentamiento.getId() : "null", setsEquipo1, setsEquipo2);

        // Validaciones básicas
        if (setsEquipo1 < 0 || setsEquipo2 < 0) {
            logger.error("Error: Los sets no pueden ser negativos");
            throw new IllegalArgumentException("Los sets no pueden ser negativos");
        }

        if (setsEquipo1 == 0 && setsEquipo2 == 0) {
            logger.error("Error: Al menos un equipo debe ganar un set");
            throw new IllegalArgumentException("Al menos un equipo debe ganar un set");
        }

        // Verificar que los sets proporcionados coincidan con el conteo
        if (sets != null && !sets.isEmpty()) {
            int setsE1Calculados = 0;
            int setsE2Calculados = 0;

            for (SetEntity set : sets) {
                Integer ganador = set.getGanador();
                if (ganador != null) {
                    if (ganador == 1) setsE1Calculados++;
                    else if (ganador == 2) setsE2Calculados++;
                }
            }

            if (setsE1Calculados != setsEquipo1 || setsE2Calculados != setsEquipo2) {
                logger.warn("Advertencia: El conteo de sets ({}-{}) no coincide con los sets detallados ({}-{})",
                           setsEquipo1, setsEquipo2, setsE1Calculados, setsE2Calculados);
            }
        }

        // Usar el método principal de registro
        return registrarResultado(enfrentamiento, sets);
    }

    /**
     * Calcula el equipo ganador de un enfrentamiento basándose en los sets ganados.
     *
     * @param enfrentamiento El enfrentamiento del cual calcular el ganador (no puede ser null)
     * @return El equipo ganador, o null si no hay ganador determinado (empate)
     * @throws IllegalArgumentException si el enfrentamiento es null
     */
    public Equipo calcularGanador(Enfrentamiento enfrentamiento) {
        logger.debug("Calculando ganador del partido ID: {}",
                    enfrentamiento != null ? enfrentamiento.getId() : "null");

        if (enfrentamiento == null) {
            logger.error("Error: El enfrentamiento no puede ser null");
            throw new IllegalArgumentException("El enfrentamiento no puede ser null");
        }

        // Recalcular sets ganados por si acaso
        enfrentamiento.calcularSetsGanados();

        Integer numeroGanador = enfrentamiento.getGanador();

        if (numeroGanador == null) {
            logger.warn("No se pudo determinar ganador para partido ID: {} (posible empate)",
                       enfrentamiento.getId());
            return null;
        }

        Equipo ganador;
        if (numeroGanador == 1) {
            ganador = enfrentamiento.getEquipo1();
            logger.info("Ganador del partido ID: {} es Equipo 1 (ID: {})",
                       enfrentamiento.getId(), ganador != null ? ganador.getId() : "null");
        } else {
            ganador = enfrentamiento.getEquipo2();
            logger.info("Ganador del partido ID: {} es Equipo 2 (ID: {})",
                       enfrentamiento.getId(), ganador != null ? ganador.getId() : "null");
        }

        return ganador;
    }

    /**
     * Valida que un resultado (lista de sets) sea coherente y válido.
     *
     * Verifica:
     * - Que exista al menos un set
     * - Que no haya más de 5 sets (máximo estándar en padel)
     * - Que cada set tenga puntajes válidos
     * - Que los puntajes sean lógicos (ej: 6-4, 7-6, etc.)
     *
     * @param sets Lista de sets a validar (no puede ser null)
     * @throws IllegalArgumentException si el resultado no es válido
     */
    public void validarResultado(List<SetEntity> sets) {
        logger.debug("Validando resultado con {} sets", sets != null ? sets.size() : 0);

        if (sets == null || sets.isEmpty()) {
            logger.error("Error de validación: Debe haber al menos un set");
            throw new IllegalArgumentException("Debe haber al menos un set");
        }

        // Validar número máximo de sets (usualmente 3 o 5 en padel)
        if (sets.size() > 5) {
            logger.error("Error de validación: Demasiados sets ({}). Máximo permitido: 5", sets.size());
            throw new IllegalArgumentException("Un partido no puede tener más de 5 sets");
        }

        // Validar cada set
        for (int i = 0; i < sets.size(); i++) {
            SetEntity set = sets.get(i);

            if (set == null) {
                logger.error("Error de validación: El set {} es null", i + 1);
                throw new IllegalArgumentException("El set " + (i + 1) + " es null");
            }

            Integer j1 = set.getJuegosEquipo1();
            Integer j2 = set.getJuegosEquipo2();

            if (j1 == null || j2 == null) {
                logger.error("Error de validación: El set {} tiene puntajes null", i + 1);
                throw new IllegalArgumentException(
                    "El set " + (i + 1) + " debe tener puntajes para ambos equipos");
            }

            if (j1 < 0 || j2 < 0) {
                logger.error("Error de validación: El set {} tiene puntajes negativos ({}-{})",
                           i + 1, j1, j2);
                throw new IllegalArgumentException(
                    "Los puntajes no pueden ser negativos en el set " + (i + 1));
            }

            // Validar lógica de puntajes de padel
            validarPuntajeSet(set, i + 1);
        }

        logger.debug("Validación de resultado exitosa");
    }

    /**
     * Marca un partido como walkover (uno de los equipos no se presentó).
     *
     * @param enfrentamiento El enfrentamiento a marcar como walkover (no puede ser null)
     * @param equipoGanador El equipo que gana por walkover (no puede ser null)
     * @return El enfrentamiento actualizado
     * @throws IllegalArgumentException si los parámetros son inválidos
     * @throws IllegalStateException si el partido no puede marcarse como walkover
     */
    @Transactional
    public Enfrentamiento marcarComoWalkover(Enfrentamiento enfrentamiento, Equipo equipoGanador) {
        logger.info("Marcando partido ID: {} como walkover. Ganador: Equipo ID: {}",
                   enfrentamiento != null ? enfrentamiento.getId() : "null",
                   equipoGanador != null ? equipoGanador.getId() : "null");

        // Validación de parámetros
        if (enfrentamiento == null) {
            logger.error("Error: El enfrentamiento no puede ser null");
            throw new IllegalArgumentException("El enfrentamiento no puede ser null");
        }

        if (equipoGanador == null) {
            logger.error("Error: El equipo ganador no puede ser null");
            throw new IllegalArgumentException("El equipo ganador no puede ser null");
        }

        // Verificar estado del partido
        if (enfrentamiento.getEstado() == EstadoEnfrentamiento.FINALIZADO) {
            logger.error("Error: El partido ID: {} ya está finalizado", enfrentamiento.getId());
            throw new IllegalStateException("El partido ya está finalizado");
        }

        if (enfrentamiento.getEstado() == EstadoEnfrentamiento.CANCELADO) {
            logger.error("Error: El partido ID: {} está cancelado", enfrentamiento.getId());
            throw new IllegalStateException("El partido está cancelado");
        }

        // Verificar que el equipo ganador pertenece al partido
        if (!equipoGanador.equals(enfrentamiento.getEquipo1()) &&
            !equipoGanador.equals(enfrentamiento.getEquipo2())) {
            logger.error("Error: El equipo ganador no pertenece a este partido");
            throw new IllegalArgumentException("El equipo ganador no pertenece a este partido");
        }

        // Determinar qué equipo ganó y actualizar sets
        if (equipoGanador.equals(enfrentamiento.getEquipo1())) {
            enfrentamiento.setSetsEquipo1(1);
            enfrentamiento.setSetsEquipo2(0);
            logger.debug("Walkover a favor del Equipo 1");
        } else {
            enfrentamiento.setSetsEquipo1(0);
            enfrentamiento.setSetsEquipo2(1);
            logger.debug("Walkover a favor del Equipo 2");
        }

        // Actualizar estado y fecha
        enfrentamiento.setEstado(EstadoEnfrentamiento.WALKOVER);
        enfrentamiento.setFechaJugado(LocalDateTime.now());

        // Agregar observación
        String observacionAnterior = enfrentamiento.getObservaciones();
        String nuevaObservacion = "Partido terminado por walkover. Ganador: " +
            (equipoGanador.getNombreEquipo() != null ? equipoGanador.getNombreEquipo() : "Equipo " + equipoGanador.getId());

        if (observacionAnterior != null && !observacionAnterior.isEmpty()) {
            enfrentamiento.setObservaciones(observacionAnterior + "\n" + nuevaObservacion);
        } else {
            enfrentamiento.setObservaciones(nuevaObservacion);
        }

        // Guardar cambios
        Enfrentamiento partidoActualizado = enfrentamientoRepository.save(enfrentamiento);

        logger.info("Partido ID: {} marcado como walkover exitosamente", partidoActualizado.getId());

        // Intentar avanzar ganador en eliminatorias
        try {
            if (partidoActualizado.getPozo() != null) {
                logger.debug("Avanzando ganador por walkover en modalidad: {}",
                           partidoActualizado.getPozo().getModalidad());
                modalidadCoordinatorService.avanzarGanador(partidoActualizado, equipoGanador);
                logger.info("Ganador por walkover avanzado exitosamente");
            }
        } catch (Exception e) {
            logger.warn("No se pudo avanzar ganador por walkover: {}", e.getMessage());
        }

        return partidoActualizado;
    }

    /**
     * Cancela un partido registrando el motivo de cancelación.
     *
     * @param enfrentamiento El enfrentamiento a cancelar (no puede ser null)
     * @param motivo Motivo de la cancelación (puede ser null)
     * @return El enfrentamiento cancelado
     * @throws IllegalArgumentException si el enfrentamiento es null
     * @throws IllegalStateException si el partido no puede ser cancelado
     */
    @Transactional
    public Enfrentamiento cancelarPartido(Enfrentamiento enfrentamiento, String motivo) {
        logger.info("Cancelando partido ID: {}. Motivo: {}",
                   enfrentamiento != null ? enfrentamiento.getId() : "null",
                   motivo != null ? motivo : "No especificado");

        // Validación de parámetros
        if (enfrentamiento == null) {
            logger.error("Error: El enfrentamiento no puede ser null");
            throw new IllegalArgumentException("El enfrentamiento no puede ser null");
        }

        // Verificar que el partido puede ser cancelado
        if (enfrentamiento.getEstado() == EstadoEnfrentamiento.FINALIZADO) {
            logger.error("Error: No se puede cancelar un partido finalizado ID: {}", enfrentamiento.getId());
            throw new IllegalStateException("No se puede cancelar un partido ya finalizado");
        }

        if (enfrentamiento.getEstado() == EstadoEnfrentamiento.CANCELADO) {
            logger.warn("Advertencia: El partido ID: {} ya está cancelado", enfrentamiento.getId());
            return enfrentamiento; // Ya está cancelado, retornar sin cambios
        }

        if (enfrentamiento.getEstado() == EstadoEnfrentamiento.EN_CURSO) {
            logger.warn("Advertencia: Cancelando partido en curso ID: {}", enfrentamiento.getId());
        }

        // Actualizar estado
        enfrentamiento.setEstado(EstadoEnfrentamiento.CANCELADO);

        // Registrar motivo en observaciones
        String observacionAnterior = enfrentamiento.getObservaciones();
        String motivoCancelacion = "Partido cancelado" +
            (motivo != null && !motivo.isEmpty() ? ": " + motivo : ".");

        if (observacionAnterior != null && !observacionAnterior.isEmpty()) {
            enfrentamiento.setObservaciones(observacionAnterior + "\n" + motivoCancelacion);
        } else {
            enfrentamiento.setObservaciones(motivoCancelacion);
        }

        // Guardar cambios
        Enfrentamiento partidoCancelado = enfrentamientoRepository.save(enfrentamiento);

        logger.info("Partido ID: {} cancelado exitosamente", partidoCancelado.getId());

        return partidoCancelado;
    }

    // ==================== MÉTODOS AUXILIARES PRIVADOS ====================

    /**
     * Valida que un set tenga puntajes lógicos según las reglas del padel.
     *
     * Reglas básicas:
     * - Se gana un set con 6 juegos, debiendo haber al menos 2 de diferencia
     * - Si hay empate 6-6, se juega tiebreak
     * - El tiebreak se gana con 7 puntos mínimo, con al menos 2 de diferencia
     * - Puntajes comunes: 6-0, 6-1, 6-2, 6-3, 6-4, 7-5, 7-6
     */
    private void validarPuntajeSet(SetEntity set, int numeroSet) {
        Integer j1 = set.getJuegosEquipo1();
        Integer j2 = set.getJuegosEquipo2();

        logger.debug("Validando puntaje del set {}: {}-{}", numeroSet, j1, j2);

        // Ambos puntajes deben ser al menos 0
        if (j1 < 0 || j2 < 0) {
            throw new IllegalArgumentException(
                String.format("Puntaje inválido en set %d: %d-%d (no pueden ser negativos)",
                           numeroSet, j1, j2));
        }

        // Al menos uno debe haber ganado
        if (j1 == 0 && j2 == 0) {
            throw new IllegalArgumentException(
                String.format("Puntaje inválido en set %d: 0-0 (debe haber al menos un juego ganado)",
                           numeroSet));
        }

        // Verificar que hay un ganador
        int mayor = Math.max(j1, j2);
        int menor = Math.min(j1, j2);
        int diferencia = mayor - menor;

        // Casos válidos:
        // 1. Ganador con 6 o menos juegos: debe tener al menos 2 de diferencia (6-0, 6-1, 6-2, 6-3, 6-4)
        // 2. Ganador con 7 juegos: 7-5 o 7-6 (tiebreak)
        // 3. Para super tiebreak o formatos especiales: permitir hasta 10 juegos

        if (mayor <= 5) {
            // Puntaje muy bajo para ser un set completo (puede ser válido en algunos formatos)
            logger.debug("Set {} con puntaje bajo: {}-{} (puede ser formato corto)", numeroSet, j1, j2);
        } else if (mayor == 6) {
            // Ganador con 6 juegos
            if (menor > 4) {
                throw new IllegalArgumentException(
                    String.format("Puntaje inválido en set %d: %d-%d (con 6 juegos debe ser máximo 6-4)",
                               numeroSet, j1, j2));
            }
        } else if (mayor == 7) {
            // Ganador con 7 juegos (7-5 o 7-6 con tiebreak)
            if (menor != 5 && menor != 6) {
                throw new IllegalArgumentException(
                    String.format("Puntaje inválido en set %d: %d-%d (con 7 juegos debe ser 7-5 o 7-6)",
                               numeroSet, j1, j2));
            }
            // Si es 7-6, debe haber tiebreak
            if (menor == 6 && !set.hasTiebreak()) {
                logger.warn("Set {} con puntaje 7-6 pero sin tiebreak registrado", numeroSet);
            }
        } else if (mayor <= 10) {
            // Permitir hasta 10 para formatos especiales o super tiebreak
            logger.debug("Set {} con formato especial: {}-{}", numeroSet, j1, j2);
        } else {
            // Puntaje demasiado alto
            throw new IllegalArgumentException(
                String.format("Puntaje inválido en set %d: %d-%d (demasiados juegos)",
                           numeroSet, j1, j2));
        }

        // Validar tiebreak si existe
        if (set.hasTiebreak()) {
            validarTiebreak(set, numeroSet);
        }

        logger.debug("Puntaje del set {} validado correctamente", numeroSet);
    }

    /**
     * Valida que un tiebreak tenga puntajes válidos.
     */
    private void validarTiebreak(SetEntity set, int numeroSet) {
        Integer tb1 = set.getTiebreakEquipo1();
        Integer tb2 = set.getTiebreakEquipo2();

        logger.debug("Validando tiebreak del set {}: {}-{}", numeroSet, tb1, tb2);

        if (tb1 == null || tb2 == null) {
            logger.warn("Tiebreak del set {} tiene valores null", numeroSet);
            return; // Ya marcado como hasTiebreak, pero sin datos
        }

        if (tb1 < 0 || tb2 < 0) {
            throw new IllegalArgumentException(
                String.format("Puntaje de tiebreak inválido en set %d: %d-%d (no pueden ser negativos)",
                           numeroSet, tb1, tb2));
        }

        int mayor = Math.max(tb1, tb2);
        int menor = Math.min(tb1, tb2);
        int diferencia = mayor - menor;

        // Tiebreak se gana con al menos 7 puntos y 2 de diferencia
        if (mayor < 7) {
            throw new IllegalArgumentException(
                String.format("Puntaje de tiebreak inválido en set %d: %d-%d (se necesitan al menos 7 puntos)",
                           numeroSet, tb1, tb2));
        }

        if (diferencia < 2) {
            throw new IllegalArgumentException(
                String.format("Puntaje de tiebreak inválido en set %d: %d-%d (debe haber al menos 2 puntos de diferencia)",
                           numeroSet, tb1, tb2));
        }

        // Validar que si no se ganó con 7, el perdedor tiene 6 o más
        if (mayor > 7 && menor != mayor - 2) {
            throw new IllegalArgumentException(
                String.format("Puntaje de tiebreak inválido en set %d: %d-%d (debe mantenerse diferencia de 2)",
                           numeroSet, tb1, tb2));
        }

        logger.debug("Tiebreak del set {} validado correctamente", numeroSet);
    }
}
