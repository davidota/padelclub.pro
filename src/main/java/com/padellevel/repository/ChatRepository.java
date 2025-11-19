package com.padellevel.repository;

import com.padellevel.data.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Chat entity.
 */
@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    /**
     * Encuentra chats donde el usuario es participante.
     */
    @Query("SELECT c FROM Chat c JOIN c.participantes p WHERE p = :user AND c.activo = true ORDER BY c.ultimoMensaje DESC")
    List<Chat> findByParticipante(@Param("user") User user);

    /**
     * Encuentra chats por tipo.
     */
    List<Chat> findByTipoAndActivoTrue(TipoChat tipo);

    /**
     * Encuentra chat de un torneo.
     */
    Optional<Chat> findByTorneoAndActivoTrue(Torneo torneo);

    /**
     * Encuentra chat de un equipo.
     */
    Optional<Chat> findByEquipoAndActivoTrue(Equipo equipo);

    /**
     * Encuentra chat de un club.
     */
    Optional<Chat> findByClubAndActivoTrue(Club club);

    /**
     * Encuentra chat privado entre dos usuarios.
     */
    @Query("SELECT c FROM Chat c JOIN c.participantes p1 JOIN c.participantes p2 " +
           "WHERE c.tipo = 'PRIVADO' AND p1 = :user1 AND p2 = :user2 AND c.activo = true")
    Optional<Chat> findChatPrivadoEntreUsuarios(@Param("user1") User user1, @Param("user2") User user2);

    /**
     * Cuenta mensajes no leídos de un usuario en un chat.
     */
    @Query("SELECT COUNT(m) FROM ChatMensaje m WHERE m.chat = :chat AND m.remitente != :user AND m.leido = false")
    long countMensajesNoLeidos(@Param("chat") Chat chat, @Param("user") User user);
}
