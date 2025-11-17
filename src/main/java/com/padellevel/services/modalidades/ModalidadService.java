package com.padellevel.services.modalidades;

import com.padellevel.data.Enfrentamiento;
import com.padellevel.data.Equipo;
import com.padellevel.data.Pozo;

import java.util.List;

/**
 * Interfaz base para generadores de enfrentamientos según modalidad de torneo.
 * Cada modalidad (Americano, Round Robin, Eliminación) implementa esta interfaz.
 */
public interface ModalidadService {

    /**
     * Genera los enfrentamientos para un pozo según la modalidad.
     *
     * @param pozo El pozo para el cual generar enfrentamientos
     * @param equipos Lista de equipos participantes
     * @return Lista de enfrentamientos generados
     */
    List<Enfrentamiento> generarEnfrentamientos(Pozo pozo, List<Equipo> equipos);

    /**
     * Valida si el número de equipos es válido para esta modalidad.
     *
     * @param numeroEquipos Número de equipos
     * @return true si es válido, false en caso contrario
     */
    boolean validarNumeroEquipos(int numeroEquipos);

    /**
     * Obtiene el número mínimo de equipos requeridos para esta modalidad.
     *
     * @return Número mínimo de equipos
     */
    int getMinEquipos();

    /**
     * Obtiene el número máximo recomendado de equipos para esta modalidad.
     *
     * @return Número máximo de equipos, o -1 si no hay límite
     */
    int getMaxEquiposRecomendado();

    /**
     * Calcula el número total de partidos que se jugarán.
     *
     * @param numeroEquipos Número de equipos participantes
     * @return Número total de partidos
     */
    int calcularNumeroPartidos(int numeroEquipos);

    /**
     * Obtiene una descripción de cómo funciona esta modalidad.
     *
     * @return Descripción de la modalidad
     */
    String getDescripcion();
}
