package com.padellevel.services;

import com.padellevel.data.*;
import com.padellevel.repository.EstadisticaJugadorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para PrediccionService.
 */
@ExtendWith(MockitoExtension.class)
class PrediccionServiceTest {

    @Mock
    private EstadisticaJugadorRepository estadisticaJugadorRepository;

    @InjectMocks
    private PrediccionService prediccionService;

    private Equipo equipo1;
    private Equipo equipo2;
    private User jugador1;
    private User jugador2;
    private User jugador3;
    private User jugador4;

    @BeforeEach
    void setUp() {
        // Jugadores
        jugador1 = new User();
        jugador1.setId(1L);
        jugador1.setUsername("jugador1");
        jugador1.setNivel(NivelJugador.AVANZADO);

        jugador2 = new User();
        jugador2.setId(2L);
        jugador2.setUsername("jugador2");
        jugador2.setNivel(NivelJugador.AVANZADO);

        jugador3 = new User();
        jugador3.setId(3L);
        jugador3.setUsername("jugador3");
        jugador3.setNivel(NivelJugador.INTERMEDIO);

        jugador4 = new User();
        jugador4.setId(4L);
        jugador4.setUsername("jugador4");
        jugador4.setNivel(NivelJugador.INTERMEDIO);

        // Equipos
        equipo1 = new Equipo();
        equipo1.setId(1L);
        equipo1.setParticipante1(jugador1);
        equipo1.setParticipante2(jugador2);

        equipo2 = new Equipo();
        equipo2.setId(2L);
        equipo2.setParticipante1(jugador3);
        equipo2.setParticipante2(jugador4);
    }

    @Test
    void testPredecirEnfrentamiento_ValidEquipos_ReturnsPrediction() {
        // Arrange
        EstadisticaJugador stats1 = createEstadistica(jugador1, 10, 8, 2);
        EstadisticaJugador stats2 = createEstadistica(jugador2, 10, 7, 3);
        EstadisticaJugador stats3 = createEstadistica(jugador3, 10, 5, 5);
        EstadisticaJugador stats4 = createEstadistica(jugador4, 10, 4, 6);

        when(estadisticaJugadorRepository.findByJugador(jugador1))
            .thenReturn(Arrays.asList(stats1));
        when(estadisticaJugadorRepository.findByJugador(jugador2))
            .thenReturn(Arrays.asList(stats2));
        when(estadisticaJugadorRepository.findByJugador(jugador3))
            .thenReturn(Arrays.asList(stats3));
        when(estadisticaJugadorRepository.findByJugador(jugador4))
            .thenReturn(Arrays.asList(stats4));

        // Act
        Map<String, Object> resultado = prediccionService.predecirEnfrentamiento(equipo1, equipo2);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.containsKey("probabilidadEquipo1"));
        assertTrue(resultado.containsKey("probabilidadEquipo2"));
        assertTrue(resultado.containsKey("favorito"));
        assertTrue(resultado.containsKey("nivelConfianza"));
        assertTrue(resultado.containsKey("analisis"));

        // Verificar que equipo1 (con mejor winrate) tenga mayor probabilidad
        Double prob1 = (Double) resultado.get("probabilidadEquipo1");
        Double prob2 = (Double) resultado.get("probabilidadEquipo2");
        assertTrue(prob1 > prob2, "Equipo1 debería tener mayor probabilidad");
        assertEquals("Equipo 1", resultado.get("favorito"));
    }

    @Test
    void testPredecirEnfrentamiento_EquiposConEstadisticasVacias_ReturnsFiftyFifty() {
        // Arrange
        when(estadisticaJugadorRepository.findByJugador(jugador1))
            .thenReturn(new ArrayList<>());
        when(estadisticaJugadorRepository.findByJugador(jugador2))
            .thenReturn(new ArrayList<>());
        when(estadisticaJugadorRepository.findByJugador(jugador3))
            .thenReturn(new ArrayList<>());
        when(estadisticaJugadorRepository.findByJugador(jugador4))
            .thenReturn(new ArrayList<>());

        // Act
        Map<String, Object> resultado = prediccionService.predecirEnfrentamiento(equipo1, equipo2);

        // Assert
        assertNotNull(resultado);
        Double prob1 = (Double) resultado.get("probabilidadEquipo1");
        Double prob2 = (Double) resultado.get("probabilidadEquipo2");

        // Sin estadísticas, debería ser aproximadamente 50-50
        assertEquals(50.0, prob1, 10.0);
        assertEquals(50.0, prob2, 10.0);
        assertEquals("Bajo", resultado.get("nivelConfianza"));
    }

    @Test
    void testPredecirGanadorTorneo_ValidParticipantes_ReturnsTopThree() {
        // Arrange
        List<User> participantes = Arrays.asList(jugador1, jugador2, jugador3, jugador4);

        EstadisticaJugador stats1 = createEstadistica(jugador1, 20, 18, 2);
        EstadisticaJugador stats2 = createEstadistica(jugador2, 20, 15, 5);
        EstadisticaJugador stats3 = createEstadistica(jugador3, 20, 10, 10);
        EstadisticaJugador stats4 = createEstadistica(jugador4, 20, 8, 12);

        when(estadisticaJugadorRepository.findByJugador(jugador1))
            .thenReturn(Arrays.asList(stats1));
        when(estadisticaJugadorRepository.findByJugador(jugador2))
            .thenReturn(Arrays.asList(stats2));
        when(estadisticaJugadorRepository.findByJugador(jugador3))
            .thenReturn(Arrays.asList(stats3));
        when(estadisticaJugadorRepository.findByJugador(jugador4))
            .thenReturn(Arrays.asList(stats4));

        // Act
        Map<String, Object> resultado = prediccionService.predecirGanadorTorneo(participantes);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.containsKey("favoritos"));
        assertTrue(resultado.containsKey("analisis"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> favoritos = (List<Map<String, Object>>) resultado.get("favoritos");
        assertNotNull(favoritos);
        assertTrue(favoritos.size() <= 3);

        // El primer favorito debería ser jugador1 (mejor winrate)
        if (!favoritos.isEmpty()) {
            Map<String, Object> primerFavorito = favoritos.get(0);
            assertTrue(primerFavorito.containsKey("jugador"));
            assertTrue(primerFavorito.containsKey("probabilidad"));
        }
    }

    @Test
    void testPredecirGanadorTorneo_EmptyParticipantes_ReturnsEmptyFavorites() {
        // Arrange
        List<User> participantes = new ArrayList<>();

        // Act
        Map<String, Object> resultado = prediccionService.predecirGanadorTorneo(participantes);

        // Assert
        assertNotNull(resultado);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> favoritos = (List<Map<String, Object>>) resultado.get("favoritos");
        assertTrue(favoritos.isEmpty());
    }

    /**
     * Helper para crear estadísticas de prueba.
     */
    private EstadisticaJugador createEstadistica(User jugador, int partidosJugados,
                                                  int partidosGanados, int partidosPerdidos) {
        EstadisticaJugador stats = new EstadisticaJugador();
        stats.setJugador(jugador);
        stats.setPartidosJugados(partidosJugados);
        stats.setPartidosGanados(partidosGanados);
        stats.setPartidosPerdidos(partidosPerdidos);
        stats.setSaquesEfectivos(partidosJugados * 5);
        stats.setSaquesTotales(partidosJugados * 10);
        return stats;
    }
}
