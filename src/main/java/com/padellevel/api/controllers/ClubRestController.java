package com.padellevel.api.controllers;

import com.padellevel.api.dto.ClubDTO;
import com.padellevel.api.response.ApiResponse;
import com.padellevel.data.Club;
import com.padellevel.data.User;
import com.padellevel.repository.ClubRepository;
import com.padellevel.security.AuthenticatedUser;
import com.padellevel.services.ClubService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * API REST para gestión de clubes.
 */
@RestController
@RequestMapping("/api/v1/clubes")
@Tag(name = "Clubes", description = "API para gestión de clubes")
public class ClubRestController {

    private final ClubService clubService;
    private final ClubRepository clubRepository;
    private final AuthenticatedUser authenticatedUser;

    public ClubRestController(ClubService clubService,
                             ClubRepository clubRepository,
                             AuthenticatedUser authenticatedUser) {
        this.clubService = clubService;
        this.clubRepository = clubRepository;
        this.authenticatedUser = authenticatedUser;
    }

    /**
     * Obtiene todos los clubes activos.
     */
    @GetMapping
    @Operation(summary = "Listar clubes", description = "Obtiene todos los clubes activos")
    public ResponseEntity<ApiResponse<List<ClubDTO>>> listarClubes() {
        List<Club> clubes = clubService.findAllActive();

        List<ClubDTO> clubesDTO = clubes.stream()
            .map(ClubDTO::new)
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Clubes obtenidos", clubesDTO));
    }

    /**
     * Obtiene un club por ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener club", description = "Obtiene los detalles de un club específico")
    public ResponseEntity<ApiResponse<ClubDTO>> obtenerClub(
            @Parameter(description = "ID del club")
            @PathVariable Long id) {

        Club club = clubRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Club no encontrado con ID: " + id));

        ClubDTO dto = new ClubDTO(club);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    /**
     * Obtiene los clubes del usuario autenticado.
     */
    @GetMapping("/mis-clubes")
    @Operation(summary = "Mis clubes", description = "Obtiene los clubes donde el usuario es miembro")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ClubDTO>>> misClubes() {
        User currentUser = authenticatedUser.get()
            .orElseThrow(() -> new IllegalStateException("Usuario no autenticado"));

        List<Club> clubes = clubService.findAllClubesByUsuario(currentUser);

        List<ClubDTO> clubesDTO = clubes.stream()
            .map(ClubDTO::new)
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Clubes obtenidos", clubesDTO));
    }

    /**
     * Busca clubes por nombre o ciudad.
     */
    @GetMapping("/buscar")
    @Operation(summary = "Buscar clubes", description = "Busca clubes por nombre o ciudad")
    public ResponseEntity<ApiResponse<List<ClubDTO>>> buscarClubes(
            @Parameter(description = "Término de búsqueda")
            @RequestParam String query) {

        List<Club> clubes = clubService.searchClubes(query);

        List<ClubDTO> clubesDTO = clubes.stream()
            .map(ClubDTO::new)
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Clubes encontrados", clubesDTO));
    }

    /**
     * Obtiene estadísticas de un club.
     */
    @GetMapping("/{id}/estadisticas")
    @Operation(summary = "Estadísticas del club", description = "Obtiene estadísticas de un club")
    public ResponseEntity<ApiResponse<ClubService.ClubStats>> obtenerEstadisticas(
            @Parameter(description = "ID del club")
            @PathVariable Long id) {

        ClubService.ClubStats stats = clubService.getClubStats(id);

        return ResponseEntity.ok(ApiResponse.success("Estadísticas obtenidas", stats));
    }

    /**
     * Crea un nuevo club.
     */
    @PostMapping
    @Operation(summary = "Crear club", description = "Crea un nuevo club")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ClubDTO>> crearClub(@RequestBody Club club) {
        User currentUser = authenticatedUser.get()
            .orElseThrow(() -> new IllegalStateException("Usuario no autenticado"));

        Club nuevoClub = clubService.crearClub(club, currentUser);
        ClubDTO dto = new ClubDTO(nuevoClub);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Club creado exitosamente", dto));
    }

    /**
     * Actualiza un club existente.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar club", description = "Actualiza la información de un club")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ClubDTO>> actualizarClub(
            @Parameter(description = "ID del club")
            @PathVariable Long id,
            @RequestBody Club clubActualizado) {

        User currentUser = authenticatedUser.get()
            .orElseThrow(() -> new IllegalStateException("Usuario no autenticado"));

        Club club = clubRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Club no encontrado"));

        // Verificar permisos de gestión
        if (!clubService.usuarioTienePermisosGestion(currentUser, club)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("No tienes permisos para actualizar este club"));
        }

        // Actualizar campos permitidos
        club.setNombre(clubActualizado.getNombre());
        club.setDescripcion(clubActualizado.getDescripcion());
        club.setDireccion(clubActualizado.getDireccion());
        club.setCiudad(clubActualizado.getCiudad());
        club.setPais(clubActualizado.getPais());
        club.setTelefono(clubActualizado.getTelefono());
        club.setEmail(clubActualizado.getEmail());
        club.setWebsite(clubActualizado.getWebsite());

        Club updated = clubService.actualizarClub(club);
        ClubDTO dto = new ClubDTO(updated);

        return ResponseEntity.ok(ApiResponse.success("Club actualizado exitosamente", dto));
    }

    /**
     * Desactiva un club.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar club", description = "Desactiva un club")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> desactivarClub(
            @Parameter(description = "ID del club")
            @PathVariable Long id) {

        clubService.desactivarClub(id);

        return ResponseEntity.ok(ApiResponse.success("Club desactivado exitosamente", null));
    }

    /**
     * Agrega un miembro a un club.
     */
    @PostMapping("/{clubId}/miembros/{userId}")
    @Operation(summary = "Agregar miembro", description = "Agrega un miembro a un club")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> agregarMiembro(
            @Parameter(description = "ID del club")
            @PathVariable Long clubId,
            @Parameter(description = "ID del usuario")
            @PathVariable Long userId) {

        User currentUser = authenticatedUser.get()
            .orElseThrow(() -> new IllegalStateException("Usuario no autenticado"));

        Club club = clubRepository.findById(clubId)
            .orElseThrow(() -> new NoSuchElementException("Club no encontrado"));

        // Verificar permisos
        if (!clubService.usuarioTienePermisosGestion(currentUser, club)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("No tienes permisos para agregar miembros"));
        }

        User nuevoMiembro = new User();
        nuevoMiembro.setId(userId);

        clubService.agregarMiembro(clubId, nuevoMiembro);

        return ResponseEntity.ok(ApiResponse.success("Miembro agregado exitosamente", null));
    }

    /**
     * Elimina un miembro de un club.
     */
    @DeleteMapping("/{clubId}/miembros/{userId}")
    @Operation(summary = "Eliminar miembro", description = "Elimina un miembro de un club")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> eliminarMiembro(
            @Parameter(description = "ID del club")
            @PathVariable Long clubId,
            @Parameter(description = "ID del usuario")
            @PathVariable Long userId) {

        User currentUser = authenticatedUser.get()
            .orElseThrow(() -> new IllegalStateException("Usuario no autenticado"));

        Club club = clubRepository.findById(clubId)
            .orElseThrow(() -> new NoSuchElementException("Club no encontrado"));

        // Verificar permisos
        if (!clubService.usuarioTienePermisosGestion(currentUser, club)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("No tienes permisos para eliminar miembros"));
        }

        User miembro = new User();
        miembro.setId(userId);

        clubService.eliminarMiembro(clubId, miembro);

        return ResponseEntity.ok(ApiResponse.success("Miembro eliminado exitosamente", null));
    }
}
