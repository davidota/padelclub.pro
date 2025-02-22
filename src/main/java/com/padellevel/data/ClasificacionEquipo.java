package com.padellevel.data;

import jakarta.persistence.*;

@Entity
public class ClasificacionEquipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Estadísticas
    private int partidosJugados;
    private int partidosGanados;
    private int partidosEmpatados;
    private int partidosPerdidos;
    private int juegosGanados;
    private int juegosPerdidos;
    private int puntos;

    // Relaciones
    @ManyToOne
    @JoinColumn(name = "torneo_id")
    private Torneo torneo;

    @ManyToOne
    @JoinColumn(name = "pozo_id")
    private Pozo pozo;

    @ManyToOne
    @JoinColumn(name = "equipo_id")
    private Equipo equipo;

    // Método para actualizar clasificación tras un enfrentamiento
    public void actualizarClasificacion(boolean gana, boolean empate, int juegosGanadosEnfrentamiento, int juegosPerdidosEnfrentamiento) {
        partidosJugados++;
        if (gana) {
            partidosGanados++;
            puntos += 3; // Ejemplo: 3 puntos por victoria
        } else if (empate) {
            partidosEmpatados++;
            puntos += 1; // 1 punto por empate
        } else {
            partidosPerdidos++;
        }
        juegosGanados += juegosGanadosEnfrentamiento;
        juegosPerdidos += juegosPerdidosEnfrentamiento;
    }

    // Getters and setters  
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public int getPartidosJugados() { return partidosJugados; }
    public void setPartidosJugados(int partidosJugados) { this.partidosJugados = partidosJugados; }
    
    public int getPartidosGanados() { return partidosGanados; }
    public void setPartidosGanados(int partidosGanados) { this.partidosGanados = partidosGanados; }
    
    public int getPartidosEmpatados() { return partidosEmpatados; }
    public void setPartidosEmpatados(int partidosEmpatados) { this.partidosEmpatados = partidosEmpatados; }
    
    public int getPartidosPerdidos() { return partidosPerdidos; }
    public void setPartidosPerdidos(int partidosPerdidos) { this.partidosPerdidos = partidosPerdidos; }
    
    public int getJuegosGanados() { return juegosGanados; }
    public void setJuegosGanados(int juegosGanados) { this.juegosGanados = juegosGanados; }
    
    public int getJuegosPerdidos() { return juegosPerdidos; }
    public void setJuegosPerdidos(int juegosPerdidos) { this.juegosPerdidos = juegosPerdidos; }
    
    public int getPuntos() { return puntos; }
    public void setPuntos(int puntos) { this.puntos = puntos; }
    
    public Torneo getTorneo() { return torneo; }
    public void setTorneo(Torneo torneo) { this.torneo = torneo; }
    
    public Pozo getPozo() { return pozo; }
    public void setPozo(Pozo pozo) { this.pozo = pozo; }
    
    public Equipo getEquipo() { return equipo; }
    public void setEquipo(Equipo equipo) { this.equipo = equipo; }
}
