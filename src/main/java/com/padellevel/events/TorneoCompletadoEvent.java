package com.padellevel.events;

import com.padellevel.data.Torneo;
import com.padellevel.data.User;
import org.springframework.context.ApplicationEvent;

import java.util.Map;

/**
 * Evento publicado cuando se completa un torneo.
 */
public class TorneoCompletadoEvent extends ApplicationEvent {

    private final Torneo torneo;
    private final Map<Integer, User> ganadores; // Posición -> Jugador

    public TorneoCompletadoEvent(Object source, Torneo torneo, Map<Integer, User> ganadores) {
        super(source);
        this.torneo = torneo;
        this.ganadores = ganadores;
    }

    public Torneo getTorneo() {
        return torneo;
    }

    public Map<Integer, User> getGanadores() {
        return ganadores;
    }
}
