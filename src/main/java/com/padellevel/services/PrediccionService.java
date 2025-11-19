package com.padellevel.services;

import com.padellevel.data.Equipo;
import com.padellevel.data.EstadisticaJugador;
import com.padellevel.data.User;
import com.padellevel.repository.EstadisticaJugadorRepository;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio de predicciones usando Machine Learning básico.
 * Utiliza estadísticas históricas para predecir resultados de partidos.
 */
@Service
public class PrediccionService {

    private static final Logger logger = LoggerFactory.getLogger(PrediccionService.class);

    private final EstadisticaJugadorRepository estadisticaRepository;

    // Pesos para diferentes factores
    private static final double PESO_WINRATE = 0.40;
    private static final double PESO_RACHA = 0.25;
    private static final double PESO_PARTIDOS_JUGADOS = 0.15;
    private static final double PESO_EFECTIVIDAD_SAQUE = 0.10;
    private static final double PESO_NIVEL = 0.10;

    public PrediccionService(EstadisticaJugadorRepository estadisticaRepository) {
        this.estadisticaRepository = estadisticaRepository;
    }

    /**
     * Predice el resultado de un enfrentamiento entre dos equipos.
     *
     * @param equipo1 Primer equipo
     * @param equipo2 Segundo equipo
     * @return Mapa con probabilidades y análisis
     */
    public Map<String, Object> predecirEnfrentamiento(Equipo equipo1, Equipo equipo2) {
        logger.info("Prediciendo enfrentamiento entre equipos");

        // Calcular score de cada equipo
        double scoreEquipo1 = calcularScoreEquipo(equipo1);
        double scoreEquipo2 = calcularScoreEquipo(equipo2);

        // Normalizar probabilidades
        double total = scoreEquipo1 + scoreEquipo2;
        double probabilidadEquipo1 = (scoreEquipo1 / total) * 100;
        double probabilidadEquipo2 = (scoreEquipo2 / total) * 100;

        // Construir respuesta
        Map<String, Object> prediccion = new HashMap<>();
        prediccion.put("equipo1Probabilidad", Math.round(probabilidadEquipo1 * 100.0) / 100.0);
        prediccion.put("equipo2Probabilidad", Math.round(probabilidadEquipo2 * 100.0) / 100.0);
        prediccion.put("favorito", probabilidadEquipo1 > probabilidadEquipo2 ? "equipo1" : "equipo2");
        prediccion.put("confianza", calcularConfianza(probabilidadEquipo1, probabilidadEquipo2));
        prediccion.put("analisis", generarAnalisis(equipo1, equipo2, probabilidadEquipo1, probabilidadEquipo2));

        logger.info("Predicción: Equipo1={}%, Equipo2={}%",
                   Math.round(probabilidadEquipo1), Math.round(probabilidadEquipo2));

        return prediccion;
    }

    /**
     * Calcula un score general para un equipo basado en estadísticas.
     */
    private double calcularScoreEquipo(Equipo equipo) {
        if (equipo == null || equipo.getJugadores() == null || equipo.getJugadores().isEmpty()) {
            return 50.0; // Score neutral si no hay datos
        }

        List<User> jugadores = equipo.getJugadores();

        double scoreTotal = 0.0;
        int jugadoresConEstadisticas = 0;

        for (User jugador : jugadores) {
            List<EstadisticaJugador> stats = estadisticaRepository.findByJugador(jugador);

            if (stats != null && !stats.isEmpty()) {
                double scoreJugador = calcularScoreJugador(stats);
                scoreTotal += scoreJugador;
                jugadoresConEstadisticas++;
            }
        }

        if (jugadoresConEstadisticas == 0) {
            return 50.0; // Score neutral
        }

        return scoreTotal / jugadoresConEstadisticas;
    }

    /**
     * Calcula el score de un jugador individual.
     */
    private double calcularScoreJugador(List<EstadisticaJugador> stats) {
        DescriptiveStatistics winrates = new DescriptiveStatistics();
        DescriptiveStatistics partidosJugados = new DescriptiveStatistics();

        for (EstadisticaJugador stat : stats) {
            int total = stat.getPartidosGanados() + stat.getPartidosPerdidos();
            if (total > 0) {
                double winrate = (double) stat.getPartidosGanados() / total;
                winrates.addValue(winrate);
                partidosJugados.addValue(total);
            }
        }

        double score = 50.0; // Base score

        // Factor 1: Win rate
        if (winrates.getN() > 0) {
            score += (winrates.getMean() - 0.5) * 100 * PESO_WINRATE;
        }

        // Factor 2: Partidos jugados (experiencia)
        if (partidosJugados.getN() > 0) {
            double experiencia = Math.min(partidosJugados.getSum() / 100.0, 1.0); // Normalizar a 1
            score += experiencia * 20 * PESO_PARTIDOS_JUGADOS;
        }

        // Factor 3: Racha (últimas estadísticas)
        if (stats.size() > 0) {
            EstadisticaJugador ultimaStat = stats.get(stats.size() - 1);
            int total = ultimaStat.getPartidosGanados() + ultimaStat.getPartidosPerdidos();
            if (total > 0) {
                double winrateReciente = (double) ultimaStat.getPartidosGanados() / total;
                score += (winrateReciente - 0.5) * 40 * PESO_RACHA;
            }
        }

        return Math.max(10.0, Math.min(90.0, score)); // Limitar entre 10 y 90
    }

    /**
     * Calcula el nivel de confianza de la predicción.
     */
    private String calcularConfianza(double prob1, double prob2) {
        double diferencia = Math.abs(prob1 - prob2);

        if (diferencia < 10) {
            return "BAJA"; // Muy parejo
        } else if (diferencia < 30) {
            return "MEDIA";
        } else {
            return "ALTA"; // Diferencia clara
        }
    }

    /**
     * Genera un análisis textual de la predicción.
     */
    private String generarAnalisis(Equipo eq1, Equipo eq2, double prob1, double prob2) {
        StringBuilder analisis = new StringBuilder();

        if (prob1 > prob2) {
            double diferencia = prob1 - prob2;
            if (diferencia > 30) {
                analisis.append("El Equipo 1 es favorito claro con una ventaja significativa. ");
            } else if (diferencia > 15) {
                analisis.append("El Equipo 1 tiene ventaja moderada. ");
            } else {
                analisis.append("Partido parejo con ligera ventaja para el Equipo 1. ");
            }
        } else {
            double diferencia = prob2 - prob1;
            if (diferencia > 30) {
                analisis.append("El Equipo 2 es favorito claro con una ventaja significativa. ");
            } else if (diferencia > 15) {
                analisis.append("El Equipo 2 tiene ventaja moderada. ");
            } else {
                analisis.append("Partido parejo con ligera ventaja para el Equipo 2. ");
            }
        }

        analisis.append("Predicción basada en estadísticas históricas, win rate, racha reciente y experiencia.");

        return analisis.toString();
    }

    /**
     * Predice el ganador potencial de un torneo basándose en los participantes.
     */
    public Map<String, Object> predecirGanadorTorneo(List<User> participantes) {
        logger.info("Prediciendo ganador de torneo con {} participantes", participantes.size());

        Map<User, Double> scores = new HashMap<>();

        for (User jugador : participantes) {
            List<EstadisticaJugador> stats = estadisticaRepository.findByJugador(jugador);
            double score = calcularScoreJugador(stats);
            scores.put(jugador, score);
        }

        // Encontrar top 3
        List<Map.Entry<User, Double>> ranking = scores.entrySet().stream()
            .sorted(Map.Entry.<User, Double>comparingByValue().reversed())
            .limit(3)
            .toList();

        Map<String, Object> prediccion = new HashMap<>();
        prediccion.put("favorito", ranking.size() > 0 ? ranking.get(0).getKey().getNombreCompleto() : "N/A");
        prediccion.put("probabilidadFavorito", ranking.size() > 0 ? Math.round(ranking.get(0).getValue()) : 0);

        if (ranking.size() > 1) {
            prediccion.put("segundoFavorito", ranking.get(1).getKey().getNombreCompleto());
        }

        if (ranking.size() > 2) {
            prediccion.put("terceroFavorito", ranking.get(2).getKey().getNombreCompleto());
        }

        return prediccion;
    }
}
