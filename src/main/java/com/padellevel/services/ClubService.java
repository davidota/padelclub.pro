package com.padellevel.services;

import com.padellevel.data.Club;
import com.padellevel.data.User;
import com.padellevel.repository.ClubRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestión de clubes.
 * Proporciona operaciones CRUD y lógica de negocio relacionada con clubes.
 */
@Service
public class ClubService {

    private static final Logger logger = LoggerFactory.getLogger(ClubService.class);

    private final ClubRepository clubRepository;

    public ClubService(ClubRepository clubRepository) {
        this.clubRepository = clubRepository;
    }

    /**
     * Obtiene todos los clubes activos.
     */
    @Transactional(readOnly = true)
    public List<Club> findAllActive() {
        return clubRepository.findByActivo(true);
    }

    /**
     * Obtiene un club por su ID.
     */
    @Transactional(readOnly = true)
    public Optional<Club> findById(Long id) {
        return clubRepository.findById(id);
    }

    /**
     * Guarda o actualiza un club.
     */
    @Transactional
    public Club save(Club club) {
        Club saved = clubRepository.save(club);
        logger.info("Club guardado: {} (ID: {})", saved.getNombre(), saved.getId());
        return saved;
    }

    /**
     * Crea un nuevo club.
     */
    @Transactional
    public Club crearClub(Club club, User administrador) {
        club.setAdministrador(administrador);
        club.setActivo(true);

        // El administrador también es miembro del club
        club.agregarMiembro(administrador);

        Club saved = clubRepository.save(club);
        logger.info("Club creado: {} por administrador: {}", saved.getNombre(), administrador.getUsername());
        return saved;
    }

    /**
     * Actualiza un club existente.
     */
    @Transactional
    public Club actualizarClub(Club club) {
        if (club.getId() == null) {
            throw new IllegalArgumentException("El club debe tener un ID para ser actualizado");
        }

        Club updated = clubRepository.save(club);
        logger.info("Club actualizado: {} (ID: {})", updated.getNombre(), updated.getId());
        return updated;
    }

    /**
     * Desactiva un club (soft delete).
     */
    @Transactional
    public void desactivarClub(Long clubId) {
        Club club = clubRepository.findById(clubId)
            .orElseThrow(() -> new IllegalArgumentException("Club no encontrado"));

        club.setActivo(false);
        clubRepository.save(club);
        logger.info("Club desactivado: {} (ID: {})", club.getNombre(), club.getId());
    }

    /**
     * Agrega un miembro a un club.
     */
    @Transactional
    public void agregarMiembro(Long clubId, User user) {
        Club club = clubRepository.findById(clubId)
            .orElseThrow(() -> new IllegalArgumentException("Club no encontrado"));

        club.agregarMiembro(user);
        clubRepository.save(club);
        logger.info("Miembro {} agregado al club {}", user.getUsername(), club.getNombre());
    }

    /**
     * Elimina un miembro de un club.
     */
    @Transactional
    public void eliminarMiembro(Long clubId, User user) {
        Club club = clubRepository.findById(clubId)
            .orElseThrow(() -> new IllegalArgumentException("Club no encontrado"));

        // No permitir eliminar al administrador
        if (club.esAdministrador(user)) {
            throw new IllegalStateException("No se puede eliminar al administrador del club");
        }

        club.eliminarMiembro(user);
        clubRepository.save(club);
        logger.info("Miembro {} eliminado del club {}", user.getUsername(), club.getNombre());
    }

    /**
     * Agrega un staff a un club.
     */
    @Transactional
    public void agregarStaff(Long clubId, User user) {
        Club club = clubRepository.findById(clubId)
            .orElseThrow(() -> new IllegalArgumentException("Club no encontrado"));

        // Asegurar que el staff también sea miembro
        club.agregarMiembro(user);
        club.agregarStaff(user);
        clubRepository.save(club);
        logger.info("Staff {} agregado al club {}", user.getUsername(), club.getNombre());
    }

    /**
     * Elimina un staff de un club.
     */
    @Transactional
    public void eliminarStaff(Long clubId, User user) {
        Club club = clubRepository.findById(clubId)
            .orElseThrow(() -> new IllegalArgumentException("Club no encontrado"));

        club.eliminarStaff(user);
        clubRepository.save(club);
        logger.info("Staff {} eliminado del club {}", user.getUsername(), club.getNombre());
    }

    /**
     * Obtiene todos los clubes donde el usuario es miembro.
     */
    @Transactional(readOnly = true)
    public List<Club> findClubesByMiembro(User user) {
        return clubRepository.findClubesByMiembro(user);
    }

    /**
     * Obtiene todos los clubes donde el usuario es staff.
     */
    @Transactional(readOnly = true)
    public List<Club> findClubesByStaff(User user) {
        return clubRepository.findClubesByStaff(user);
    }

    /**
     * Obtiene todos los clubes administrados por un usuario.
     */
    @Transactional(readOnly = true)
    public List<Club> findClubesByAdministrador(User user) {
        return clubRepository.findByAdministrador(user);
    }

    /**
     * Obtiene todos los clubes donde el usuario tiene algún rol.
     */
    @Transactional(readOnly = true)
    public List<Club> findAllClubesByUsuario(User user) {
        return clubRepository.findAllClubesByUsuario(user);
    }

    /**
     * Busca clubes por nombre o ciudad.
     */
    @Transactional(readOnly = true)
    public List<Club> searchClubes(String searchTerm) {
        return clubRepository.searchClubes(searchTerm);
    }

    /**
     * Verifica si un usuario tiene permisos de gestión en un club.
     */
    @Transactional(readOnly = true)
    public boolean usuarioTienePermisosGestion(User user, Club club) {
        return club.tienePermisosGestion(user);
    }

    /**
     * Obtiene estadísticas de un club.
     */
    @Transactional(readOnly = true)
    public ClubStats getClubStats(Long clubId) {
        Club club = clubRepository.findById(clubId)
            .orElseThrow(() -> new IllegalArgumentException("Club no encontrado"));

        long totalMiembros = clubRepository.countMiembrosByClub(club);
        long torneosActivos = clubRepository.countTorneosActivosByClub(club);

        return new ClubStats(totalMiembros, torneosActivos, club.getPistas().size());
    }

    /**
     * Clase interna para estadísticas del club.
     */
    public static class ClubStats {
        private final long totalMiembros;
        private final long torneosActivos;
        private final int totalPistas;

        public ClubStats(long totalMiembros, long torneosActivos, int totalPistas) {
            this.totalMiembros = totalMiembros;
            this.torneosActivos = torneosActivos;
            this.totalPistas = totalPistas;
        }

        public long getTotalMiembros() {
            return totalMiembros;
        }

        public long getTorneosActivos() {
            return torneosActivos;
        }

        public int getTotalPistas() {
            return totalPistas;
        }
    }
}
