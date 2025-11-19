package com.padellevel.api.controllers;

import com.padellevel.api.dto.JugadorGamificacionDTO;
import com.padellevel.api.dto.LogroDTO;
import com.padellevel.api.response.ApiResponse;
import com.padellevel.data.LogroUsuario;
import com.padellevel.data.User;
import com.padellevel.repository.LogroUsuarioRepository;
import com.padellevel.repository.UserRepository;
import com.padellevel.security.AuthenticatedUser;
import com.padellevel.services.GamificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * API REST para gamificación.
 */
@RestController
@RequestMapping("/api/v1/gamificacion")
@Tag(name = "Gamificación", description = "API para logros, rankings y experiencia")
public class GamificacionRestController {

    private final GamificacionService gamificacionService;
    private final LogroUsuarioRepository logroUsuarioRepository;
    private final UserRepository userRepository;
    private final AuthenticatedUser authenticatedUser;

    public GamificacionRestController(GamificacionService gamificacionService,
                                     LogroUsuarioRepository logroUsuarioRepository,
                                     UserRepository userRepository,
                                     AuthenticatedUser authenticatedUser) {
        this.gamificacionService = gamificacionService;
        this.logroUsuarioRepository = logroUsuarioRepository;
        this.userRepository = userRepository;
        this.authenticatedUser = authenticatedUser;
    }

    /**
     * Obtiene el perfil de gamificación del usuario autenticado.
     */
    @GetMapping("/mi-perfil")
    @Operation(summary = "Mi perfil de gamificación", description = "Obtiene las estadísticas de gamificación del usuario autenticado")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<JugadorGamificacionDTO>> miPerfil() {
        User currentUser = authenticatedUser.get()
            .orElseThrow(() -> new IllegalStateException("Usuario no autenticado"));

        JugadorGamificacionDTO dto = new JugadorGamificacionDTO(currentUser);

        // Agregar logros desbloqueados
        long logrosCount = logroUsuarioRepository.countByJugador(currentUser);
        dto.setLogrosDesbloqueados(logrosCount);

        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    /**
     * Obtiene los logros desbloqueados del usuario autenticado.
     */
    @GetMapping("/mis-logros")
    @Operation(summary = "Mis logros", description = "Obtiene los logros desbloqueados del usuario autenticado")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<LogroDTO>>> misLogros() {
        User currentUser = authenticatedUser.get()
            .orElseThrow(() -> new IllegalStateException("Usuario no autenticado"));

        List<LogroUsuario> logros = gamificacionService.obtenerLogrosDesbloqueados(currentUser);

        List<LogroDTO> logrosDTO = logros.stream()
            .map(LogroDTO::new)
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Logros obtenidos", logrosDTO));
    }

    /**
     * Obtiene el ranking de jugadores por experiencia.
     */
    @GetMapping("/ranking/experiencia")
    @Operation(summary = "Ranking por experiencia", description = "Obtiene el ranking de jugadores ordenados por XP")
    public ResponseEntity<ApiResponse<List<JugadorGamificacionDTO>>> rankingExperiencia(
            @Parameter(description = "Límite de resultados", example = "50")
            @RequestParam(defaultValue = "50") int limite) {

        List<User> topPlayers = gamificacionService.obtenerRankingPorExperiencia(limite);

        List<JugadorGamificacionDTO> rankingDTO = topPlayers.stream()
            .map(user -> {
                JugadorGamificacionDTO dto = new JugadorGamificacionDTO(user);
                long logrosCount = logroUsuarioRepository.countByJugador(user);
                dto.setLogrosDesbloqueados(logrosCount);
                return dto;
            })
            .collect(Collectors.toList());

        // Agregar posición en ranking
        for (int i = 0; i < rankingDTO.size(); i++) {
            rankingDTO.get(i).setRankingXP(i + 1);
        }

        return ResponseEntity.ok(ApiResponse.success("Ranking obtenido", rankingDTO));
    }

    /**
     * Obtiene el ranking de jugadores por logros desbloqueados.
     */
    @GetMapping("/ranking/logros")
    @Operation(summary = "Ranking por logros", description = "Obtiene el ranking de jugadores ordenados por logros desbloqueados")
    public ResponseEntity<ApiResponse<List<JugadorGamificacionDTO>>> rankingLogros(
            @Parameter(description = "Límite de resultados", example = "50")
            @RequestParam(defaultValue = "50") int limite) {

        List<Map.Entry<User, Long>> topPlayers = gamificacionService.obtenerRankingPorLogros(limite);

        List<JugadorGamificacionDTO> rankingDTO = topPlayers.stream()
            .map(entry -> {
                JugadorGamificacionDTO dto = new JugadorGamificacionDTO(entry.getKey());
                dto.setLogrosDesbloqueados(entry.getValue());
                return dto;
            })
            .collect(Collectors.toList());

        // Agregar posición en ranking
        for (int i = 0; i < rankingDTO.size(); i++) {
            rankingDTO.get(i).setRankingLogros(i + 1);
        }

        return ResponseEntity.ok(ApiResponse.success("Ranking obtenido", rankingDTO));
    }

    /**
     * Obtiene el perfil de gamificación de un jugador específico.
     */
    @GetMapping("/jugador/{jugadorId}")
    @Operation(summary = "Perfil de jugador", description = "Obtiene las estadísticas de gamificación de un jugador")
    public ResponseEntity<ApiResponse<JugadorGamificacionDTO>> perfilJugador(
            @Parameter(description = "ID del jugador")
            @PathVariable Long jugadorId) {

        User jugador = userRepository.findById(jugadorId)
            .orElseThrow(() -> new NoSuchElementException("Jugador no encontrado"));

        JugadorGamificacionDTO dto = new JugadorGamificacionDTO(jugador);

        long logrosCount = logroUsuarioRepository.countByJugador(jugador);
        dto.setLogrosDesbloqueados(logrosCount);

        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    /**
     * Obtiene los logros de un jugador específico.
     */
    @GetMapping("/jugador/{jugadorId}/logros")
    @Operation(summary = "Logros de jugador", description = "Obtiene los logros desbloqueados de un jugador")
    public ResponseEntity<ApiResponse<List<LogroDTO>>> logrosJugador(
            @Parameter(description = "ID del jugador")
            @PathVariable Long jugadorId) {

        User jugador = userRepository.findById(jugadorId)
            .orElseThrow(() -> new NoSuchElementException("Jugador no encontrado"));

        List<LogroUsuario> logros = gamificacionService.obtenerLogrosDesbloqueados(jugador);

        List<LogroDTO> logrosDTO = logros.stream()
            .map(LogroDTO::new)
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Logros obtenidos", logrosDTO));
    }

    /**
     * Obtiene las estadísticas completas de gamificación de un jugador.
     */
    @GetMapping("/jugador/{jugadorId}/estadisticas")
    @Operation(summary = "Estadísticas de gamificación", description = "Obtiene las estadísticas completas de gamificación de un jugador")
    public ResponseEntity<ApiResponse<Map<String, Object>>> estadisticasJugador(
            @Parameter(description = "ID del jugador")
            @PathVariable Long jugadorId) {

        User jugador = userRepository.findById(jugadorId)
            .orElseThrow(() -> new NoSuchElementException("Jugador no encontrado"));

        Map<String, Object> stats = gamificacionService.obtenerEstadisticasGamificacion(jugador);

        return ResponseEntity.ok(ApiResponse.success("Estadísticas obtenidas", stats));
    }
}
