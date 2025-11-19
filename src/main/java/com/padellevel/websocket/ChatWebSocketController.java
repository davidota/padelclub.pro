package com.padellevel.websocket;

import com.padellevel.data.ChatMensaje;
import com.padellevel.data.User;
import com.padellevel.repository.UserRepository;
import com.padellevel.services.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

/**
 * Controlador WebSocket para mensajería en tiempo real.
 */
@Controller
public class ChatWebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(ChatWebSocketController.class);

    private final ChatService chatService;
    private final UserRepository userRepository;

    public ChatWebSocketController(ChatService chatService, UserRepository userRepository) {
        this.chatService = chatService;
        this.userRepository = userRepository;
    }

    /**
     * Maneja mensajes enviados a un chat específico.
     * Los clientes envían mensajes a /app/chat/{chatId}
     * Los mensajes se distribuyen a /topic/chat/{chatId}
     */
    @MessageMapping("/chat/{chatId}")
    @SendTo("/topic/chat/{chatId}")
    public ChatMensaje enviarMensaje(@DestinationVariable Long chatId,
                                     @Payload Map<String, String> mensaje,
                                     Principal principal) {
        try {
            String username = principal.getName();
            User remitente = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

            String contenido = mensaje.get("contenido");

            return chatService.enviarMensaje(chatId, remitente, contenido);

        } catch (Exception e) {
            logger.error("Error al enviar mensaje en chat {}: {}", chatId, e.getMessage());
            throw e;
        }
    }

    /**
     * Notifica cuando un usuario está escribiendo.
     */
    @MessageMapping("/chat/{chatId}/typing")
    @SendTo("/topic/chat/{chatId}/typing")
    public Map<String, String> usuarioEscribiendo(@DestinationVariable Long chatId,
                                                   Principal principal) {
        return Map.of("username", principal.getName(), "typing", "true");
    }
}
