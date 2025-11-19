package com.padellevel.api.controllers;

import com.padellevel.api.response.ApiResponse;
import com.padellevel.data.Equipo;
import com.padellevel.data.Torneo;
import com.padellevel.data.User;
import com.padellevel.repository.EquipoRepository;
import com.padellevel.repository.InscripcionRepository;
import com.padellevel.repository.TorneoRepository;
import com.padellevel.services.PrediccionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * API REST para predicciones con Machine Learning.
 */
@RestController
@RequestMapping("/api/v1/predicciones")
@Tag(name = "Predicciones", description = "API para predicciones con ML")
public class PrediccionRestController {

    private final PrediccionService prediccionService;
    private final EquipoRepository equipoRepository;
    private final TorneoRepository torneoRepository;
    private final InscripcionRepository inscripcionRepository;

    public PrediccionRestController(PrediccionService prediccionService,
                                   EquipoRepository equipoRepository,
                                   TorneoRepository torneoRepository,
                                   InscripcionRepository inscripcionRepository) {
        this.prediccionService = prediccionService;
        this.equipoRepository = equipoRepository;
        this.torneoRepository = torneoRepository;
        this.inscripcionRepository = inscripcionRepository;
    }

    /**
     * Predice el resultado de un enfrentamiento entre dos equipos.
     */
    @GetMapping("/enfrentamiento")
    @Operation(summary = "Predecir enfrentamiento", description = "Predice el resultado de un enfrentamiento entre dos equipos")
    public ResponseEntity<ApiResponse<Map<String, Object>>> predecirEnfrentamiento(
            @Parameter(description = "ID del equipo 1") @RequestParam Long equipo1Id,
            @Parameter(description = "ID del equipo 2") @RequestParam Long equipo2Id) {

        Equipo equipo1 = equipoRepository.findById(equipo1Id)
            .orElseThrow(() -> new NoSuchElementException("Equipo 1 no encontrado"));

        Equipo equipo2 = equipoRepository.findById(equipo2Id)
            .orElseThrow(() -> new NoSuchElementException("Equipo 2 no encontrado"));

        Map<String, Object> prediccion = prediccionService.predecirEnfrentamiento(equipo1, equipo2);

        return ResponseEntity.ok(ApiResponse.success("Predicción generada", prediccion));
    }

    /**
     * Predice el ganador de un torneo.
     */
    @GetMapping("/torneo/{torneoId}")
    @Operation(summary = "Predecir ganador de torneo", description = "Predice los favoritos para ganar un torneo")
    public ResponseEntity<ApiResponse<Map<String, Object>>> predecirGanadorTorneo(
            @Parameter(description = "ID del torneo") @PathVariable Long torneoId) {

        Torneo torneo = torneoRepository.findById(torneoId)
            .orElseThrow(() -> new NoSuchElementException("Torneo no encontrado"));

        // Obtener participantes del torneo
        List<User> participantes = inscripcionRepository.findByTorneo(torneo).stream()
            .map(inscripcion -> inscripcion.getJugador())
            .distinct()
            .collect(Collectors.toList());

        if (participantes.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.error("El torneo no tiene participantes"));
        }

        Map<String, Object> prediccion = prediccionService.predecirGanadorTorneo(participantes);

        return ResponseEntity.ok(ApiResponse.success("Predicción generada", prediccion));
    }
}
