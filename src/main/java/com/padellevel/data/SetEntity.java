package com.padellevel.data;

import jakarta.persistence.Embeddable;
import java.util.Objects;

/**
 * Clase embebible que representa un set en un enfrentamiento de padel.
 * Contiene los juegos ganados por cada equipo y opcionalmente el tiebreak.
 */
@Embeddable
public class SetEntity {

    /**
     * Número de juegos ganados por el equipo 1 en este set
     */
    private Integer juegosEquipo1;

    /**
     * Número de juegos ganados por el equipo 2 en este set
     */
    private Integer juegosEquipo2;

    /**
     * Puntos de tiebreak del equipo 1 (si hubo tiebreak)
     */
    private Integer tiebreakEquipo1;

    /**
     * Puntos de tiebreak del equipo 2 (si hubo tiebreak)
     */
    private Integer tiebreakEquipo2;

    // Constructors

    public SetEntity() {
    }

    public SetEntity(Integer juegosEquipo1, Integer juegosEquipo2) {
        this.juegosEquipo1 = juegosEquipo1;
        this.juegosEquipo2 = juegosEquipo2;
    }

    public SetEntity(Integer juegosEquipo1, Integer juegosEquipo2, Integer tiebreakEquipo1, Integer tiebreakEquipo2) {
        this.juegosEquipo1 = juegosEquipo1;
        this.juegosEquipo2 = juegosEquipo2;
        this.tiebreakEquipo1 = tiebreakEquipo1;
        this.tiebreakEquipo2 = tiebreakEquipo2;
    }

    // Getters and Setters

    public Integer getJuegosEquipo1() {
        return juegosEquipo1;
    }

    public void setJuegosEquipo1(Integer juegosEquipo1) {
        this.juegosEquipo1 = juegosEquipo1;
    }

    public Integer getJuegosEquipo2() {
        return juegosEquipo2;
    }

    public void setJuegosEquipo2(Integer juegosEquipo2) {
        this.juegosEquipo2 = juegosEquipo2;
    }

    public Integer getTiebreakEquipo1() {
        return tiebreakEquipo1;
    }

    public void setTiebreakEquipo1(Integer tiebreakEquipo1) {
        this.tiebreakEquipo1 = tiebreakEquipo1;
    }

    public Integer getTiebreakEquipo2() {
        return tiebreakEquipo2;
    }

    public void setTiebreakEquipo2(Integer tiebreakEquipo2) {
        this.tiebreakEquipo2 = tiebreakEquipo2;
    }

    // Utility methods

    /**
     * Determina si hubo tiebreak en este set
     */
    public boolean hasTiebreak() {
        return tiebreakEquipo1 != null && tiebreakEquipo2 != null;
    }

    /**
     * Retorna el ganador del set (1 o 2), o null si no hay ganador determinado
     */
    public Integer getGanador() {
        if (juegosEquipo1 == null || juegosEquipo2 == null) {
            return null;
        }
        if (juegosEquipo1 > juegosEquipo2) {
            return 1;
        } else if (juegosEquipo2 > juegosEquipo1) {
            return 2;
        }
        // En caso de empate en juegos, verificar tiebreak
        if (hasTiebreak()) {
            if (tiebreakEquipo1 > tiebreakEquipo2) {
                return 1;
            } else if (tiebreakEquipo2 > tiebreakEquipo1) {
                return 2;
            }
        }
        return null;
    }

    /**
     * Retorna una representación en string del set (ej: "6-4" o "7-6(5)")
     */
    @Override
    public String toString() {
        if (juegosEquipo1 == null || juegosEquipo2 == null) {
            return "-";
        }
        String result = juegosEquipo1 + "-" + juegosEquipo2;
        if (hasTiebreak()) {
            int tiebreakMenor = Math.min(tiebreakEquipo1, tiebreakEquipo2);
            result += "(" + tiebreakMenor + ")";
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SetEntity setEntity = (SetEntity) o;
        return Objects.equals(juegosEquipo1, setEntity.juegosEquipo1) &&
                Objects.equals(juegosEquipo2, setEntity.juegosEquipo2) &&
                Objects.equals(tiebreakEquipo1, setEntity.tiebreakEquipo1) &&
                Objects.equals(tiebreakEquipo2, setEntity.tiebreakEquipo2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(juegosEquipo1, juegosEquipo2, tiebreakEquipo1, tiebreakEquipo2);
    }
}
