package com.padellevel.repository;

import com.padellevel.data.Club;
import com.padellevel.data.Notificacion;
import com.padellevel.data.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Notificacion entity operations.
 * Manages notifications for users.
 */
@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long>,
                                                 JpaSpecificationExecutor<Notificacion> {

    /**
     * Find all notifications for a specific user.
     *
     * @param destinatario the recipient user
     * @return list of notifications for the specified user
     */
    List<Notificacion> findByDestinatario(User destinatario);

    /**
     * Find notifications by user ID ordered by date.
     *
     * @param userId the user ID
     * @return list of notifications ordered by date descending
     */
    @Query("SELECT n FROM Notificacion n WHERE n.destinatario.id = :userId ORDER BY n.fechaEnvio DESC")
    List<Notificacion> findByDestinatarioId(@Param("userId") Long userId);

    /**
     * Find notifications for a user filtered by read status.
     *
     * @param destinatario the recipient user
     * @param leida the read status
     * @return list of notifications matching the criteria
     */
    List<Notificacion> findByDestinatarioAndLeida(User destinatario, Boolean leida);

    /**
     * Find notifications for a user ordered by date.
     *
     * @param destinatario the recipient user
     * @return list of notifications ordered by date descending
     */
    List<Notificacion> findByDestinatarioOrderByFechaEnvioDesc(User destinatario);

    /**
     * Count unread notifications for a user.
     *
     * @param destinatario the recipient user
     * @param leida the read status (false for unread)
     * @return the number of unread notifications
     */
    Long countByDestinatarioAndLeida(User destinatario, Boolean leida);

    /**
     * Count unread notifications by user ID.
     *
     * @param userId the user ID
     * @return the number of unread notifications
     */
    @Query("SELECT COUNT(n) FROM Notificacion n WHERE n.destinatario.id = :userId AND n.leida = false")
    Long countUnreadByUserId(@Param("userId") Long userId);

    /**
     * Find unread notifications for a user.
     *
     * @param userId the user ID
     * @return list of unread notifications
     */
    @Query("SELECT n FROM Notificacion n WHERE n.destinatario.id = :userId AND n.leida = false ORDER BY n.fechaEnvio DESC")
    List<Notificacion> findUnreadByUserId(@Param("userId") Long userId);

    /**
     * Find notifications by type for a user.
     *
     * @param userId the user ID
     * @param tipo the notification type
     * @return list of notifications matching the criteria
     */
    @Query("SELECT n FROM Notificacion n WHERE n.destinatario.id = :userId AND n.tipo = :tipo ORDER BY n.fechaEnvio DESC")
    List<Notificacion> findByUserIdAndTipo(@Param("userId") Long userId, @Param("tipo") String tipo);

    /**
     * Mark all notifications as read for a user.
     *
     * @param userId the user ID
     */
    @Query("UPDATE Notificacion n SET n.leida = true WHERE n.destinatario.id = :userId AND n.leida = false")
    void markAllAsReadByUserId(@Param("userId") Long userId);

    /**
     * Encuentra notificaciones de un usuario en un club específico.
     *
     * @param destinatario el destinatario
     * @param club el club
     * @return lista de notificaciones del club
     */
    @Query("SELECT n FROM Notificacion n WHERE n.destinatario = :destinatario AND n.club = :club ORDER BY n.fechaEnvio DESC")
    List<Notificacion> findByDestinatarioAndClub(@Param("destinatario") User destinatario, @Param("club") Club club);

    /**
     * Encuentra notificaciones de un club.
     *
     * @param club el club
     * @return lista de notificaciones del club
     */
    List<Notificacion> findByClub(Club club);

    /**
     * Cuenta notificaciones no leídas de un usuario en un club.
     *
     * @param destinatario el destinatario
     * @param club el club
     * @return número de notificaciones no leídas del club
     */
    @Query("SELECT COUNT(n) FROM Notificacion n WHERE n.destinatario = :destinatario AND n.club = :club AND n.leida = false")
    long countUnreadByDestinatarioAndClub(@Param("destinatario") User destinatario, @Param("club") Club club);

    /**
     * Encuentra notificaciones de un usuario filtradas por club (incluye notificaciones sin club).
     *
     * @param destinatario el destinatario
     * @param club el club (puede ser null)
     * @return lista de notificaciones del club o sin club específico
     */
    @Query("SELECT n FROM Notificacion n WHERE n.destinatario = :destinatario AND (n.club = :club OR n.club IS NULL) ORDER BY n.fechaEnvio DESC")
    List<Notificacion> findByDestinatarioAndClubOrGlobal(@Param("destinatario") User destinatario, @Param("club") Club club);
}
