package com.padellevel.api.controllers;

import com.padellevel.api.dto.InscripcionDTO;
import com.padellevel.api.response.ApiResponse;
import com.padellevel.data.Inscripcion;
import com.padellevel.data.Torneo;
import com.padellevel.data.User;
import com.padellevel.repository.InscripcionRepository;
import com.padellevel.repository.TorneoRepository;
import com.padellevel.repository.UserRepository;
import com.padellevel.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * API REST para gestión de inscripciones.
 */
@RestController
@RequestMapping("/api/v1/inscripciones")
@Tag(name = "Inscripciones", description = "API para gestión de inscripciones a torneos")
public class InscripcionRestController {

    private final InscripcionRepository inscripcionRepository;
    private final TorneoRepository torneoRepository;
    private final UserRepository userRepository;
    private final AuthenticatedUser authenticatedUser;

    public InscripcionRestController(InscripcionRepository inscripcionRepository,
                                    TorneoRepository torneoRepository,
                                    UserRepository userRepository,
                                    AuthenticatedUser authenticatedUser) {
        this.inscripcionRepository = inscripcionRepository;
        this.torneoRepository = torneoRepository;
        this.userRepository = userRepository;
        this.authenticatedUser = authenticatedUser;
    }

    /**
     * Obtiene las inscripciones del usuario autenticado.
     */
    @GetMapping("/mis-inscripciones")
    @Operation(summary = "Mis inscripciones", description = "Obtiene las inscripciones del usuario autenticado")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<InscripcionDTO>>> misInscripciones() {
        User currentUser = authenticatedUser.get()
            .orElseThrow(() -> new IllegalStateException("Usuario no autenticado"));

        List<Inscripcion> inscripciones = inscripcionRepository.findByJugador(currentUser);

        List<InscripcionDTO> inscripcionesDTO = inscripciones.stream()
            .map(InscripcionDTO::new)
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Inscripciones obtenidas", inscripcionesDTO));
    }

    /**
     * Obtiene una inscripción por ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener inscripción", description = "Obtiene los detalles de una inscripción")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<InscripcionDTO>> obtenerInscripcion(
            @Parameter(description = "ID de la inscripción")
            @PathVariable Long id) {

        Inscripcion inscripcion = inscripcionRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Inscripción no encontrada"));

        InscripcionDTO dto = new InscripcionDTO(inscripcion);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    /**
     * Obtiene las inscripciones de un torneo específico.
     */
    @GetMapping("/torneo/{torneoId}")
    @Operation(summary = "Inscripciones de torneo", description = "Obtiene todas las inscripciones de un torneo")
    public ResponseEntity<ApiResponse<List<InscripcionDTO>>> inscripcionesTorneo(
            @Parameter(description = "ID del torneo")
            @PathVariable Long torneoId) {

        Torneo torneo = torneoRepository.findById(torneoId)
            .orElseThrow(() -> new NoSuchElementException("Torneo no encontrado"));

        List<Inscripcion> inscripciones = inscripcionRepository.findByTorneo(torneo);

        List<InscripcionDTO> inscripcionesDTO = inscripciones.stream()
            .map(InscripcionDTO::new)
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Inscripciones obtenidas", inscripcionesDTO));
    }

    /**
     * Obtiene las inscripciones de un jugador específico.
     */
    @GetMapping("/jugador/{jugadorId}")
    @Operation(summary = "Inscripciones de jugador", description = "Obtiene todas las inscripciones de un jugador")
    public ResponseEntity<ApiResponse<List<InscripcionDTO>>> inscripcionesJugador(
            @Parameter(description = "ID del jugador")
            @PathVariable Long jugadorId) {

        User jugador = userRepository.findById(jugadorId)
            .orElseThrow(() -> new NoSuchElementException("Jugador no encontrado"));

        List<Inscripcion> inscripciones = inscripcionRepository.findByJugador(jugador);

        List<InscripcionDTO> inscripcionesDTO = inscripciones.stream()
            .map(InscripcionDTO::new)
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Inscripciones obtenidas", inscripcionesDTO));
    }
}
