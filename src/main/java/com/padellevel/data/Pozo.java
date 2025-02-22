package com.padellevel.data;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Pozo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    
    private String tipoEnfrentamiento; // e.g., "Round-Robin", "Tie-Break", "Clásico"

    @ManyToOne
    @JoinColumn(name = "torneo_id", nullable = false)
    private Torneo torneo;

    @OneToMany(mappedBy = "pozo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Enfrentamiento> enfrentamientos = new ArrayList<>();

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTipoEnfrentamiento() { return tipoEnfrentamiento; }
    public void setTipoEnfrentamiento(String tipoEnfrentamiento) { this.tipoEnfrentamiento = tipoEnfrentamiento; }

    public Torneo getTorneo() { return torneo; }
    public void setTorneo(Torneo torneo) { this.torneo = torneo; }

    public List<Enfrentamiento> getEnfrentamientos() { return enfrentamientos; }
    public void setEnfrentamientos(List<Enfrentamiento> enfrentamientos) { this.enfrentamientos = enfrentamientos; }
}
