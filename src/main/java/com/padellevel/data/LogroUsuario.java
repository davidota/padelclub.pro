package com.padellevel.data;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Entidad intermedia que registra qué logros ha desbloqueado cada usuario
 * y cuándo los desbloqueó.
 */
@Entity
@Table(name = "logro_usuario", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"jugador_id", "logro_id"})
})
public class LogroUsuario extends AbstractEntity {

    /**
     * Usuario que desbloqueó el logro.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jugador_id", nullable = false)
    private User jugador;

    /**
     * Logro desbloqueado.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "logro_id", nullable = false)
    private Logro logro;

    /**
     * Fecha y hora en que se desbloqueó el logro.
     */
    @NotNull
    @Column(name = "fecha_desbloqueo", nullable = false)
    private LocalDateTime fechaDesbloqueo;

    /**
     * Progreso actual hacia el logro (0-meta).
     * Útil para logros acumulativos.
     */
    @Column(name = "progreso_actual")
    private Integer progresoActual = 0;

    /**
     * Indica si el jugador ha visto la notificación del logro.
     */
    @Column(name = "visto")
    private Boolean visto = false;

    // Constructores
    public LogroUsuario() {
        this.fechaDesbloqueo = LocalDateTime.now();
    }

    public LogroUsuario(User jugador, Logro logro) {
        this();
        this.jugador = jugador;
        this.logro = logro;
    }

    // Getters y Setters
    public User getJugador() {
        return jugador;
    }

    public void setJugador(User jugador) {
        this.jugador = jugador;
    }

    public Logro getLogro() {
        return logro;
    }

    public void setLogro(Logro logro) {
        this.logro = logro;
    }

    public LocalDateTime getFechaDesbloqueo() {
        return fechaDesbloqueo;
    }

    public void setFechaDesbloqueo(LocalDateTime fechaDesbloqueo) {
        this.fechaDesbloqueo = fechaDesbloqueo;
    }

    public Integer getProgresoActual() {
        return progresoActual != null ? progresoActual : 0;
    }

    public void setProgresoActual(Integer progresoActual) {
        this.progresoActual = progresoActual;
    }

    public Boolean getVisto() {
        return visto != null ? visto : false;
    }

    public void setVisto(Boolean visto) {
        this.visto = visto;
    }

    /**
     * Verifica si el logro está completo (progreso >= meta).
     */
    public boolean isCompleto() {
        if (logro == null || logro.getMeta() == null) {
            return true;
        }
        return getProgresoActual() >= logro.getMeta();
    }

    /**
     * Calcula el porcentaje de progreso (0-100%).
     */
    public double getPorcentajeProgreso() {
        if (logro == null || logro.getMeta() == null || logro.getMeta() == 0) {
            return 100.0;
        }
        return Math.min(100.0, (getProgresoActual() * 100.0) / logro.getMeta());
    }
}
