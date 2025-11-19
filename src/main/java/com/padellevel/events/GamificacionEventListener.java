package com.padellevel.events;

import com.padellevel.data.TipoLogro;
import com.padellevel.data.User;
import com.padellevel.services.GamificacionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Listener de eventos de gamificación.
 * Escucha eventos del sistema y otorga XP y logros automáticamente.
 */
@Component
public class GamificacionEventListener {

    private static final Logger logger = LoggerFactory.getLogger(GamificacionEventListener.class);

    private final GamificacionService gamificacionService;

    public GamificacionEventListener(GamificacionService gamificacionService) {
        this.gamificacionService = gamificacionService;
    }

    /**
     * Listener para cuando se completa un partido.
     */
    @Async
    @EventListener
    public void onPartidoCompletado(PartidoCompletadoEvent event) {
        logger.info("Procesando XP y logros para partido completado");

        // Otorgar XP a todos los jugadores por jugar
        List<User> ganadores = event.getGanadores();
        List<User> perdedores = event.getPerdedores();

        // XP por jugar para todos
        for (User jugador : ganadores) {
            gamificacionService.otorgarXPPartidoJugado(jugador);
            gamificacionService.actualizarProgresoLogro(jugador, TipoLogro.PARTIDOS_JUGADOS, 1);
        }

        for (User jugador : perdedores) {
            gamificacionService.otorgarXPPartidoJugado(jugador);
            gamificacionService.actualizarProgresoLogro(jugador, TipoLogro.PARTIDOS_JUGADOS, 1);
        }

        // XP extra por ganar
        for (User ganador : ganadores) {
            gamificacionService.otorgarXPPartidoGanado(ganador);
            gamificacionService.actualizarProgresoLogro(ganador, TipoLogro.VICTORIAS, 1);
            gamificacionService.actualizarProgresoLogro(ganador, TipoLogro.RACHA_VICTORIAS, 1);
        }

        // Resetear racha de perdedores (esto requeriría lógica adicional)
        // Por ahora solo incrementamos victorias para ganadores

        logger.info("XP y logros procesados para {} ganadores y {} perdedores",
                   ganadores.size(), perdedores.size());
    }

    /**
     * Listener para cuando se completa un torneo.
     */
    @Async
    @EventListener
    public void onTorneoCompletado(TorneoCompletadoEvent event) {
        logger.info("Procesando XP y logros para torneo completado: {}", event.getTorneo().getNombre());

        Map<Integer, User> ganadores = event.getGanadores();

        // Otorgar XP según posición
        for (Map.Entry<Integer, User> entry : ganadores.entrySet()) {
            Integer posicion = entry.getKey();
            User jugador = entry.getValue();

            // XP por posición
            gamificacionService.otorgarXPTorneoGanado(jugador, posicion);

            // Logro de torneo participado
            gamificacionService.actualizarProgresoLogro(jugador, TipoLogro.COLABORADOR, 1);

            // Logros según posición
            if (posicion == 1) {
                gamificacionService.actualizarProgresoLogro(jugador, TipoLogro.TORNEOS_GANADOS, 1);
                gamificacionService.actualizarProgresoLogro(jugador, TipoLogro.PRIMERA_VICTORIA, 1);
            }

            if (posicion <= 3) {
                gamificacionService.actualizarProgresoLogro(jugador, TipoLogro.PRIMERA_MEDALLA, 1);
            }

            logger.info("Jugador {} finalizó en posición {} - XP otorgado",
                       jugador.getUsername(), posicion);
        }
    }
}
