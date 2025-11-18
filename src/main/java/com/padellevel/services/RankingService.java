package com.padellevel.services;

import com.padellevel.data.*;
import com.padellevel.repository.EstadisticaJugadorRepository;
import com.padellevel.repository.EstadisticaEquipoRepository;
import com.padellevel.repository.PremioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar rankings de jugadores y equipos.
 *
 * Proporciona rankings a tres niveles:
 * - Global: Ranking general de todos los jugadores/equipos
 * - Por Torneo: Ranking dentro de un torneo específico
 * - Por Pozo: Ranking dentro de un pozo específico
 *
 * Los rankings se ordenan según criterios específicos de la modalidad:
 * - Round Robin: puntos > victorias > diferencia de juegos
 * - Otras modalidades: victorias > diferencia de juegos > juegos ganados
 */
@Service
public class RankingService {

    private static final Logger logger = LoggerFactory.getLogger(RankingService.class);

    private final EstadisticaJugadorRepository estadisticaJugadorRepository;
    private final EstadisticaEquipoRepository estadisticaEquipoRepository;
    private final PremioRepository premioRepository;

    public RankingService(EstadisticaJugadorRepository estadisticaJugadorRepository,
                         EstadisticaEquipoRepository estadisticaEquipoRepository,
                         PremioRepository premioRepository) {
        this.estadisticaJugadorRepository = estadisticaJugadorRepository;
        this.estadisticaEquipoRepository = estadisticaEquipoRepository;
        this.premioRepository = premioRepository;
    }

    // ==================== RANKINGS DE JUGADORES ====================

    /**
     * Genera el ranking global de todos los jugadores.
     */
    public List<EstadisticaJugador> generarRankingJugadoresGlobal() {
        logger.info("Generando ranking global de jugadores");

        List<EstadisticaJugador> estadisticas = estadisticaJugadorRepository
            .findByTorneoIsNullAndPozoIsNull();

        return ordenarRankingJugadores(estadisticas, null);
    }

    /**
     * Genera el ranking de jugadores de un torneo específico.
     */
    public List<EstadisticaJugador> generarRankingJugadoresTorneo(Torneo torneo) {
        logger.info("Generando ranking de jugadores para torneo {}", torneo.getId());

        List<EstadisticaJugador> estadisticas = estadisticaJugadorRepository
            .findByTorneoAndPozoIsNull(torneo);

        return ordenarRankingJugadores(estadisticas, null);
    }

    /**
     * Genera el ranking de jugadores de un pozo específico.
     */
    public List<EstadisticaJugador> generarRankingJugadoresPozo(Pozo pozo) {
        logger.info("Generando ranking de jugadores para pozo {}", pozo.getId());

        List<EstadisticaJugador> estadisticas = estadisticaJugadorRepository
            .findByPozo(pozo);

        return ordenarRankingJugadores(estadisticas, pozo.getModalidad());
    }

    /**
     * Actualiza las posiciones en el ranking de un pozo.
     */
    @Transactional
    public void actualizarPosicionesJugadoresPozo(Pozo pozo) {
        logger.info("Actualizando posiciones de jugadores en pozo {}", pozo.getId());

        List<EstadisticaJugador> ranking = generarRankingJugadoresPozo(pozo);

        for (int i = 0; i < ranking.size(); i++) {
            EstadisticaJugador stats = ranking.get(i);
            stats.setPosicion(i + 1);
            estadisticaJugadorRepository.save(stats);
        }

        logger.info("Posiciones actualizadas para {} jugadores", ranking.size());
    }

    /**
     * Obtiene el podio (top 3) de jugadores de un pozo.
     */
    public List<User> obtenerPodioJugadores(Pozo pozo) {
        List<EstadisticaJugador> ranking = generarRankingJugadoresPozo(pozo);

        return ranking.stream()
            .limit(3)
            .map(EstadisticaJugador::getJugador)
            .collect(Collectors.toList());
    }

    // ==================== RANKINGS DE EQUIPOS ====================

    /**
     * Genera el ranking global de todos los equipos.
     */
    public List<EstadisticaEquipo> generarRankingEquiposGlobal() {
        logger.info("Generando ranking global de equipos");

        List<EstadisticaEquipo> estadisticas = estadisticaEquipoRepository
            .findByTorneoIsNullAndPozoIsNull();

        return ordenarRankingEquipos(estadisticas, null);
    }

    /**
     * Genera el ranking de equipos de un torneo específico.
     */
    public List<EstadisticaEquipo> generarRankingEquiposTorneo(Torneo torneo) {
        logger.info("Generando ranking de equipos para torneo {}", torneo.getId());

        List<EstadisticaEquipo> estadisticas = estadisticaEquipoRepository
            .findByTorneoAndPozoIsNull(torneo);

        return ordenarRankingEquipos(estadisticas, null);
    }

    /**
     * Genera el ranking de equipos de un pozo específico.
     */
    public List<EstadisticaEquipo> generarRankingEquiposPozo(Pozo pozo) {
        logger.info("Generando ranking de equipos para pozo {}", pozo.getId());

        List<EstadisticaEquipo> estadisticas = estadisticaEquipoRepository
            .findByPozo(pozo);

        return ordenarRankingEquipos(estadisticas, pozo.getModalidad());
    }

    /**
     * Actualiza las posiciones en el ranking de un pozo.
     */
    @Transactional
    public void actualizarPosicionesEquiposPozo(Pozo pozo) {
        logger.info("Actualizando posiciones de equipos en pozo {}", pozo.getId());

        List<EstadisticaEquipo> ranking = generarRankingEquiposPozo(pozo);

        for (int i = 0; i < ranking.size(); i++) {
            EstadisticaEquipo stats = ranking.get(i);
            stats.setPosicion(i + 1);
            estadisticaEquipoRepository.save(stats);
        }

        logger.info("Posiciones actualizadas para {} equipos", ranking.size());
    }

    /**
     * Obtiene el podio (top 3) de equipos de un pozo.
     */
    public List<Equipo> obtenerPodioEquipos(Pozo pozo) {
        List<EstadisticaEquipo> ranking = generarRankingEquiposPozo(pozo);

        return ranking.stream()
            .limit(3)
            .map(EstadisticaEquipo::getEquipo)
            .collect(Collectors.toList());
    }

    // ==================== GESTIÓN DE PREMIOS ====================

    /**
     * Asigna premios a los ganadores de un pozo.
     * Crea premios para el podio (1º, 2º y 3º lugar).
     */
    @Transactional
    public void asignarPremiosPozo(Pozo pozo) {
        logger.info("Asignando premios para pozo {}", pozo.getId());

        ModalidadPozo modalidad = pozo.getModalidad();

        if (modalidad == ModalidadPozo.AMERICANO) {
            // En Americano, los premios van a jugadores individuales
            asignarPremiosJugadores(pozo);
        } else {
            // En otras modalidades, los premios van a equipos
            asignarPremiosEquipos(pozo);
        }
    }

    /**
     * Asigna premios a jugadores individuales (para Americano).
     */
    private void asignarPremiosJugadores(Pozo pozo) {
        List<User> podio = obtenerPodioJugadores(pozo);

        if (podio.isEmpty()) {
            logger.warn("No hay jugadores en el podio del pozo {}", pozo.getId());
            return;
        }

        TipoPremio[] tipos = {TipoPremio.ORO, TipoPremio.PLATA, TipoPremio.BRONCE};
        String[] posiciones = {"1er", "2do", "3er"};

        for (int i = 0; i < Math.min(podio.size(), 3); i++) {
            User jugador = podio.get(i);

            Premio premio = new Premio();
            premio.setTorneo(pozo.getTorneo());
            premio.setPozo(pozo);
            premio.setGanadorJugador(jugador);
            premio.setTipo(tipos[i]);
            premio.setNombre(posiciones[i] + " Lugar - " + pozo.getNombre());
            premio.setDescripcion("Premio " + tipos[i] + " en " + pozo.getNombre());
            premio.setFechaOtorgado(LocalDateTime.now());

            premioRepository.save(premio);
            logger.info("Premio {} asignado a jugador {}", tipos[i], jugador.getUsername());
        }
    }

    /**
     * Asigna premios a equipos (para Round Robin y Eliminación).
     */
    private void asignarPremiosEquipos(Pozo pozo) {
        List<Equipo> podio = obtenerPodioEquipos(pozo);

        if (podio.isEmpty()) {
            logger.warn("No hay equipos en el podio del pozo {}", pozo.getId());
            return;
        }

        TipoPremio[] tipos = {TipoPremio.ORO, TipoPremio.PLATA, TipoPremio.BRONCE};
        String[] posiciones = {"1er", "2do", "3er"};

        for (int i = 0; i < Math.min(podio.size(), 3); i++) {
            Equipo equipo = podio.get(i);

            Premio premio = new Premio();
            premio.setTorneo(pozo.getTorneo());
            premio.setPozo(pozo);
            premio.setGanadorEquipo(equipo);
            premio.setTipo(tipos[i]);
            premio.setNombre(posiciones[i] + " Lugar - " + pozo.getNombre());
            premio.setDescripcion("Premio " + tipos[i] + " en " + pozo.getNombre());
            premio.setFechaOtorgado(LocalDateTime.now());

            premioRepository.save(premio);
            logger.info("Premio {} asignado a equipo {}", tipos[i], equipo.getNombreEquipo());
        }
    }

    // ==================== MÉTODOS PRIVADOS ====================

    /**
     * Ordena el ranking de jugadores según criterios de modalidad.
     */
    private List<EstadisticaJugador> ordenarRankingJugadores(List<EstadisticaJugador> estadisticas,
                                                            ModalidadPozo modalidad) {
        Comparator<EstadisticaJugador> comparator;

        if (modalidad == ModalidadPozo.TODOS_CONTRA_TODOS) {
            // Round Robin: ordenar por puntos, luego victorias, luego diferencia de juegos
            comparator = Comparator
                .comparing(EstadisticaJugador::getPuntos, Comparator.reverseOrder())
                .thenComparing(EstadisticaJugador::getPartidosGanados, Comparator.reverseOrder())
                .thenComparing(e -> (e.getJuegosGanados() - e.getJuegosPerdidos()), Comparator.reverseOrder())
                .thenComparing(EstadisticaJugador::getJuegosGanados, Comparator.reverseOrder());
        } else {
            // Otras modalidades: ordenar por victorias, luego diferencia de juegos
            comparator = Comparator
                .comparing(EstadisticaJugador::getPartidosGanados, Comparator.reverseOrder())
                .thenComparing(e -> (e.getJuegosGanados() - e.getJuegosPerdidos()), Comparator.reverseOrder())
                .thenComparing(EstadisticaJugador::getJuegosGanados, Comparator.reverseOrder())
                .thenComparing(e -> (e.getSetsGanados() - e.getSetsPerdidos()), Comparator.reverseOrder());
        }

        return estadisticas.stream()
            .sorted(comparator)
            .collect(Collectors.toList());
    }

    /**
     * Ordena el ranking de equipos según criterios de modalidad.
     */
    private List<EstadisticaEquipo> ordenarRankingEquipos(List<EstadisticaEquipo> estadisticas,
                                                         ModalidadPozo modalidad) {
        Comparator<EstadisticaEquipo> comparator;

        if (modalidad == ModalidadPozo.TODOS_CONTRA_TODOS) {
            // Round Robin: ordenar por puntos, luego victorias, luego diferencia de juegos
            comparator = Comparator
                .comparing(EstadisticaEquipo::getPuntos, Comparator.reverseOrder())
                .thenComparing(EstadisticaEquipo::getPartidosGanados, Comparator.reverseOrder())
                .thenComparing(e -> (e.getJuegosGanados() - e.getJuegosPerdidos()), Comparator.reverseOrder())
                .thenComparing(EstadisticaEquipo::getJuegosGanados, Comparator.reverseOrder());
        } else {
            // Otras modalidades: ordenar por victorias, luego diferencia de juegos
            comparator = Comparator
                .comparing(EstadisticaEquipo::getPartidosGanados, Comparator.reverseOrder())
                .thenComparing(e -> (e.getJuegosGanados() - e.getJuegosPerdidos()), Comparator.reverseOrder())
                .thenComparing(EstadisticaEquipo::getJuegosGanados, Comparator.reverseOrder())
                .thenComparing(e -> (e.getSetsGanados() - e.getSetsPerdidos()), Comparator.reverseOrder());
        }

        return estadisticas.stream()
            .sorted(comparator)
            .collect(Collectors.toList());
    }

    /**
     * Calcula la diferencia de juegos (game differential).
     */
    private int calcularDiferencialJuegos(int juegosGanados, int juegosPerdidos) {
        return juegosGanados - juegosPerdidos;
    }

    /**
     * Calcula la diferencia de sets (set differential).
     */
    private int calcularDiferencialSets(int setsGanados, int setsPerdidos) {
        return setsGanados - setsPerdidos;
    }
}
