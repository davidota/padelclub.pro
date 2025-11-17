package com.padellevel.services.modalidades;

import com.padellevel.data.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio coordinador que selecciona y ejecuta la modalidad correcta
 * según la configuración del pozo.
 */
@Service
public class ModalidadCoordinatorService {

    private static final Logger logger = LoggerFactory.getLogger(ModalidadCoordinatorService.class);

    private final AmericanoService americanoService;
    private final RoundRobinService roundRobinService;
    private final EliminacionDirectaService eliminacionDirectaService;

    public ModalidadCoordinatorService(
            AmericanoService americanoService,
            RoundRobinService roundRobinService,
            EliminacionDirectaService eliminacionDirectaService) {
        this.americanoService = americanoService;
        this.roundRobinService = roundRobinService;
        this.eliminacionDirectaService = eliminacionDirectaService;
    }

    /**
     * Genera enfrentamientos para un pozo según su modalidad configurada.
     *
     * @param pozo El pozo para el cual generar enfrentamientos
     * @param equipos Lista de equipos participantes
     * @return Lista de enfrentamientos generados
     * @throws IllegalArgumentException si la modalidad no está configurada o no es válida
     */
    @Transactional
    public List<Enfrentamiento> generarEnfrentamientos(Pozo pozo, List<Equipo> equipos) {
        if (pozo.getModalidad() == null) {
            throw new IllegalArgumentException("El pozo no tiene modalidad configurada");
        }

        logger.info("Generando enfrentamientos para pozo '{}' con modalidad: {}",
                   pozo.getNombre(), pozo.getModalidad());

        ModalidadService modalidadService = seleccionarModalidad(pozo.getModalidad());

        // Validar número de equipos
        if (!modalidadService.validarNumeroEquipos(equipos.size())) {
            throw new IllegalArgumentException(
                String.format("Número de equipos inválido (%d) para modalidad %s. " +
                            "Mínimo: %d, Máximo recomendado: %d",
                            equipos.size(),
                            pozo.getModalidad(),
                            modalidadService.getMinEquipos(),
                            modalidadService.getMaxEquiposRecomendado())
            );
        }

        // Generar enfrentamientos según la modalidad
        List<Enfrentamiento> enfrentamientos = modalidadService.generarEnfrentamientos(pozo, equipos);

        logger.info("Generados {} enfrentamientos para pozo '{}'", enfrentamientos.size(), pozo.getNombre());

        return enfrentamientos;
    }

    /**
     * Selecciona el servicio de modalidad correcto según el tipo.
     *
     * @param modalidad Tipo de modalidad
     * @return Servicio de modalidad correspondiente
     */
    private ModalidadService seleccionarModalidad(ModalidadPozo modalidad) {
        switch (modalidad) {
            case AMERICANO:
                return americanoService;
            case TODOS_CONTRA_TODOS:
                return roundRobinService;
            case ELIMINACION_DIRECTA:
                return eliminacionDirectaService;
            case GRUPOS_ELIMINACION:
                // TODO: Implementar modalidad grupos + eliminación
                throw new UnsupportedOperationException(
                    "Modalidad Grupos + Eliminación aún no implementada");
            default:
                throw new IllegalArgumentException("Modalidad desconocida: " + modalidad);
        }
    }

    /**
     * Obtiene información sobre una modalidad específica.
     *
     * @param modalidad Tipo de modalidad
     * @return Descripción y características de la modalidad
     */
    public String getInformacionModalidad(ModalidadPozo modalidad) {
        ModalidadService modalidadService = seleccionarModalidad(modalidad);
        return String.format("%s\n\nMínimo de equipos: %d\nMáximo recomendado: %d\n\n%s",
                           modalidad.getDisplayName(),
                           modalidadService.getMinEquipos(),
                           modalidadService.getMaxEquiposRecomendado(),
                           modalidadService.getDescripcion());
    }

    /**
     * Calcula el número estimado de partidos para una modalidad y número de equipos.
     *
     * @param modalidad Tipo de modalidad
     * @param numeroEquipos Número de equipos
     * @return Número estimado de partidos
     */
    public int calcularNumeroPartidos(ModalidadPozo modalidad, int numeroEquipos) {
        ModalidadService modalidadService = seleccionarModalidad(modalidad);
        return modalidadService.calcularNumeroPartidos(numeroEquipos);
    }

    /**
     * Valida si un número de equipos es válido para una modalidad.
     *
     * @param modalidad Tipo de modalidad
     * @param numeroEquipos Número de equipos
     * @return true si es válido, false en caso contrario
     */
    public boolean validarNumeroEquipos(ModalidadPozo modalidad, int numeroEquipos) {
        ModalidadService modalidadService = seleccionarModalidad(modalidad);
        return modalidadService.validarNumeroEquipos(numeroEquipos);
    }

    /**
     * Avanza un ganador en modalidades que lo requieren (como eliminación directa).
     *
     * @param enfrentamiento Enfrentamiento completado
     * @param equipoGanador Equipo ganador
     */
    @Transactional
    public void avanzarGanador(Enfrentamiento enfrentamiento, Equipo equipoGanador) {
        Pozo pozo = enfrentamiento.getPozo();

        if (pozo.getModalidad() == ModalidadPozo.ELIMINACION_DIRECTA) {
            eliminacionDirectaService.avanzarGanador(enfrentamiento, equipoGanador);
        }
        // Otras modalidades no requieren avance de ganadores
    }
}
