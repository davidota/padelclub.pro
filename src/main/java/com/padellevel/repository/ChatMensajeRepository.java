package com.padellevel.repository;

import com.padellevel.data.Chat;
import com.padellevel.data.ChatMensaje;
import com.padellevel.data.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for ChatMensaje entity.
 */
@Repository
public interface ChatMensajeRepository extends JpaRepository<ChatMensaje, Long> {

    /**
     * Encuentra mensajes de un chat ordenados por fecha.
     */
    Page<ChatMensaje> findByChatOrderByFechaHoraDesc(Chat chat, Pageable pageable);

    /**
     * Encuentra mensajes de un chat posteriores a una fecha.
     */
    List<ChatMensaje> findByChatAndFechaHoraAfterOrderByFechaHoraAsc(Chat chat, LocalDateTime fecha);

    /**
     * Encuentra mensajes de un remitente en un chat.
     */
    List<ChatMensaje> findByChatAndRemitenteOrderByFechaHoraDesc(Chat chat, User remitente);

    /**
     * Cuenta mensajes no leídos de un usuario en un chat.
     */
    @Query("SELECT COUNT(m) FROM ChatMensaje m WHERE m.chat = :chat AND m.remitente != :user AND m.leido = false")
    long countMensajesNoLeidosEnChat(@Param("chat") Chat chat, @Param("user") User user);

    /**
     * Marca todos los mensajes de un chat como leídos para un usuario.
     */
    @Query("UPDATE ChatMensaje m SET m.leido = true WHERE m.chat = :chat AND m.remitente != :user AND m.leido = false")
    void marcarMensajesComoLeidos(@Param("chat") Chat chat, @Param("user") User user);

    /**
     * Obtiene los últimos N mensajes de un chat.
     */
    List<ChatMensaje> findTop50ByChatOrderByFechaHoraDesc(Chat chat);
}
