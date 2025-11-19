package com.padellevel.events;

import com.padellevel.data.Enfrentamiento;
import com.padellevel.data.User;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * Evento publicado cuando se completa un partido.
 */
public class PartidoCompletadoEvent extends ApplicationEvent {

    private final Enfrentamiento enfrentamiento;
    private final List<User> ganadores;
    private final List<User> perdedores;

    public PartidoCompletadoEvent(Object source, Enfrentamiento enfrentamiento,
                                  List<User> ganadores, List<User> perdedores) {
        super(source);
        this.enfrentamiento = enfrentamiento;
        this.ganadores = ganadores;
        this.perdedores = perdedores;
    }

    public Enfrentamiento getEnfrentamiento() {
        return enfrentamiento;
    }

    public List<User> getGanadores() {
        return ganadores;
    }

    public List<User> getPerdedores() {
        return perdedores;
    }
}
