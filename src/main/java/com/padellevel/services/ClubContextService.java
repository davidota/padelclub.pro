package com.padellevel.services;

import com.padellevel.data.Club;
import com.padellevel.data.User;
import com.padellevel.repository.ClubRepository;
import com.vaadin.flow.server.VaadinSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar el contexto del club actual del usuario.
 * Maneja el club seleccionado en la sesión para filtrar información relevante.
 */
@Service
public class ClubContextService {

    private static final Logger logger = LoggerFactory.getLogger(ClubContextService.class);
    private static final String CLUB_ACTUAL_KEY = "clubActual";

    private final ClubRepository clubRepository;

    public ClubContextService(ClubRepository clubRepository) {
        this.clubRepository = clubRepository;
    }

    /**
     * Obtiene el club actual de la sesión del usuario.
     *
     * @return el club actual o vacío si no hay uno seleccionado
     */
    public Optional<Club> getClubActual() {
        try {
            VaadinSession session = VaadinSession.getCurrent();
            if (session != null) {
                Long clubId = session.getAttribute(CLUB_ACTUAL_KEY);
                if (clubId != null) {
                    return clubRepository.findById(clubId);
                }
            }
        } catch (Exception e) {
            logger.warn("Error al obtener club actual de la sesión: {}", e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Establece el club actual en la sesión del usuario.
     *
     * @param club el club a establecer
     */
    public void setClubActual(Club club) {
        try {
            VaadinSession session = VaadinSession.getCurrent();
            if (session != null) {
                if (club != null) {
                    session.setAttribute(CLUB_ACTUAL_KEY, club.getId());
                    logger.info("Club actual establecido: {} (ID: {})", club.getNombre(), club.getId());
                } else {
                    session.setAttribute(CLUB_ACTUAL_KEY, null);
                    logger.info("Club actual removido de la sesión");
                }
            }
        } catch (Exception e) {
            logger.error("Error al establecer club actual en la sesión: {}", e.getMessage());
        }
    }

    /**
     * Limpia el club actual de la sesión.
     */
    public void clearClubActual() {
        setClubActual(null);
    }

    /**
     * Inicializa el club actual para un usuario.
     * Si el usuario no tiene un club seleccionado, selecciona el primero disponible.
     *
     * @param user el usuario
     * @return el club actual o vacío si el usuario no tiene clubes
     */
    public Optional<Club> initializeClubActual(User user) {
        // Verificar si ya hay un club en la sesión
        Optional<Club> clubActual = getClubActual();
        if (clubActual.isPresent()) {
            return clubActual;
        }

        // Obtener todos los clubes del usuario
        List<Club> clubes = clubRepository.findAllClubesByUsuario(user);

        if (!clubes.isEmpty()) {
            // Seleccionar el primer club
            Club primerClub = clubes.get(0);
            setClubActual(primerClub);
            logger.info("Club inicial establecido para usuario {}: {}", user.getUsername(), primerClub.getNombre());
            return Optional.of(primerClub);
        }

        logger.warn("Usuario {} no tiene clubes disponibles", user.getUsername());
        return Optional.empty();
    }

    /**
     * Verifica si el usuario tiene acceso al club actual.
     *
     * @param user el usuario
     * @param club el club
     * @return true si el usuario tiene acceso, false en caso contrario
     */
    public boolean usuarioTieneAccesoAlClub(User user, Club club) {
        if (club == null || user == null) {
            return false;
        }

        return club.esMiembro(user) || club.esStaff(user) || club.esAdministrador(user);
    }

    /**
     * Cambia el club actual del usuario.
     * Verifica que el usuario tenga acceso al nuevo club antes de cambiarlo.
     *
     * @param user el usuario
     * @param nuevoClubId el ID del nuevo club
     * @return true si el cambio fue exitoso, false en caso contrario
     */
    public boolean cambiarClubActual(User user, Long nuevoClubId) {
        Optional<Club> nuevoClubOpt = clubRepository.findById(nuevoClubId);

        if (nuevoClubOpt.isEmpty()) {
            logger.warn("Intento de cambiar a club inexistente: {}", nuevoClubId);
            return false;
        }

        Club nuevoClub = nuevoClubOpt.get();

        if (!usuarioTieneAccesoAlClub(user, nuevoClub)) {
            logger.warn("Usuario {} no tiene acceso al club {}", user.getUsername(), nuevoClub.getNombre());
            return false;
        }

        setClubActual(nuevoClub);
        return true;
    }

    /**
     * Obtiene todos los clubes disponibles para un usuario.
     *
     * @param user el usuario
     * @return lista de clubes disponibles
     */
    public List<Club> getClubesDisponibles(User user) {
        return clubRepository.findAllClubesByUsuario(user);
    }

    /**
     * Verifica si el club actual está establecido.
     *
     * @return true si hay un club actual, false en caso contrario
     */
    public boolean tieneClubActual() {
        return getClubActual().isPresent();
    }
}
