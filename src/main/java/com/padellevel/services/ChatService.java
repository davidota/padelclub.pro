package com.padellevel.services;

import com.padellevel.data.*;
import com.padellevel.repository.ChatMensajeRepository;
import com.padellevel.repository.ChatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de gestión de chats y mensajes.
 */
@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    private final ChatRepository chatRepository;
    private final ChatMensajeRepository mensajeRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatService(ChatRepository chatRepository,
                      ChatMensajeRepository mensajeRepository,
                      SimpMessagingTemplate messagingTemplate) {
        this.chatRepository = chatRepository;
        this.mensajeRepository = mensajeRepository;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Crea un chat privado entre dos usuarios.
     */
    @Transactional
    public Chat crearChatPrivado(User user1, User user2) {
        // Verificar si ya existe un chat entre estos usuarios
        Optional<Chat> existente = chatRepository.findChatPrivadoEntreUsuarios(user1, user2);
        if (existente.isPresent()) {
            return existente.get();
        }

        Chat chat = new Chat();
        chat.setTipo(TipoChat.PRIVADO);
        chat.setParticipantes(Arrays.asList(user1, user2));

        return chatRepository.save(chat);
    }

    /**
     * Crea o obtiene el chat de un torneo.
     */
    @Transactional
    public Chat obtenerChatTorneo(Torneo torneo) {
        Optional<Chat> existente = chatRepository.findByTorneoAndActivoTrue(torneo);
        if (existente.isPresent()) {
            return existente.get();
        }

        Chat chat = new Chat();
        chat.setTipo(TipoChat.TORNEO);
        chat.setTorneo(torneo);
        chat.setNombre("Chat de " + torneo.getNombre());

        return chatRepository.save(chat);
    }

    /**
     * Envía un mensaje en un chat.
     */
    @Transactional
    public ChatMensaje enviarMensaje(Long chatId, User remitente, String contenido) {
        Chat chat = chatRepository.findById(chatId)
            .orElseThrow(() -> new IllegalArgumentException("Chat no encontrado"));

        if (!chat.esParticipante(remitente)) {
            throw new IllegalStateException("El usuario no es participante de este chat");
        }

        ChatMensaje mensaje = new ChatMensaje(chat, remitente, contenido);
        mensaje = mensajeRepository.save(mensaje);

        // Actualizar timestamp del chat
        chat.setUltimoMensaje(LocalDateTime.now());
        chatRepository.save(chat);

        // Enviar por WebSocket
        messagingTemplate.convertAndSend("/topic/chat/" + chatId, mensaje);

        logger.info("Mensaje enviado en chat {}: {}", chatId, contenido.substring(0, Math.min(50, contenido.length())));

        return mensaje;
    }

    /**
     * Obtiene los chats de un usuario.
     */
    @Transactional(readOnly = true)
    public List<Chat> obtenerChatsUsuario(User user) {
        return chatRepository.findByParticipante(user);
    }

    /**
     * Obtiene los mensajes de un chat.
     */
    @Transactional(readOnly = true)
    public List<ChatMensaje> obtenerMensajesChat(Long chatId) {
        Chat chat = chatRepository.findById(chatId)
            .orElseThrow(() -> new IllegalArgumentException("Chat no encontrado"));

        return mensajeRepository.findTop50ByChatOrderByFechaHoraDesc(chat);
    }

    /**
     * Agrega un participante a un chat.
     */
    @Transactional
    public void agregarParticipante(Long chatId, User user) {
        Chat chat = chatRepository.findById(chatId)
            .orElseThrow(() -> new IllegalArgumentException("Chat no encontrado"));

        chat.agregarParticipante(user);
        chatRepository.save(chat);
    }

    /**
     * Cuenta mensajes no leídos en todos los chats del usuario.
     */
    @Transactional(readOnly = true)
    public long contarMensajesNoLeidos(User user) {
        List<Chat> chats = chatRepository.findByParticipante(user);
        return chats.stream()
            .mapToLong(chat -> mensajeRepository.countMensajesNoLeidosEnChat(chat, user))
            .sum();
    }
}
