package com.padellevel.api.controllers;

import com.padellevel.api.dto.TorneoDTO;
import com.padellevel.api.response.ApiResponse;
import com.padellevel.api.response.PaginationMeta;
import com.padellevel.data.EstadoTorneo;
import com.padellevel.data.Torneo;
import com.padellevel.repository.InscripcionRepository;
import com.padellevel.repository.TorneoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * API REST para gestión de torneos.
 * Proporciona endpoints para consultar información de torneos.
 */
@RestController
@RequestMapping("/api/v1/torneos")
@Tag(name = "Torneos", description = "API para gestión de torneos")
public class TorneoRestController {

    private final TorneoRepository torneoRepository;
    private final InscripcionRepository inscripcionRepository;

    public TorneoRestController(TorneoRepository torneoRepository,
                               InscripcionRepository inscripcionRepository) {
        this.torneoRepository = torneoRepository;
        this.inscripcionRepository = inscripcionRepository;
    }

    /**
     * Lista todos los torneos con paginación.
     */
    @GetMapping
    @Operation(summary = "Listar torneos", description = "Obtiene un listado paginado de todos los torneos")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Listado de torneos obtenido exitosamente",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<ApiResponse<List<TorneoDTO>>> listarTorneos(
            @Parameter(description = "Número de página (base 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Tamaño de página", example = "20")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Campo por el cual ordenar", example = "fechaInicio")
            @RequestParam(defaultValue = "fechaInicio") String sortBy,

            @Parameter(description = "Dirección de orden (ASC/DESC)", example = "DESC")
            @RequestParam(defaultValue = "DESC") String direction) {

        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Torneo> torneosPage = torneoRepository.findAll(pageable);

        List<TorneoDTO> torneosDTO = torneosPage.getContent().stream()
            .map(torneo -> {
                TorneoDTO dto = new TorneoDTO(torneo);
                // Agregar número de inscritos
                long inscritos = inscripcionRepository.countByTorneo(torneo);
                dto.setInscritosActuales((int) inscritos);
                return dto;
            })
            .collect(Collectors.toList());

        PaginationMeta pagination = new PaginationMeta(
            torneosPage.getNumber(),
            torneosPage.getSize(),
            torneosPage.getTotalElements(),
            torneosPage.getTotalPages(),
            torneosPage.hasNext(),
            torneosPage.hasPrevious()
        );

        return ResponseEntity.ok(ApiResponse.successWithPagination(torneosDTO, pagination));
    }

    /**
     * Obtiene un torneo por ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener torneo", description = "Obtiene los detalles de un torneo específico")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Torneo encontrado",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Torneo no encontrado"
        )
    })
    public ResponseEntity<ApiResponse<TorneoDTO>> obtenerTorneo(
            @Parameter(description = "ID del torneo", required = true)
            @PathVariable Long id) {

        Torneo torneo = torneoRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Torneo no encontrado con ID: " + id));

        TorneoDTO dto = new TorneoDTO(torneo);
        long inscritos = inscripcionRepository.countByTorneo(torneo);
        dto.setInscritosActuales((int) inscritos);

        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    /**
     * Filtra torneos por estado.
     */
    @GetMapping("/estado/{estado}")
    @Operation(summary = "Filtrar por estado", description = "Obtiene torneos filtrados por estado")
    public ResponseEntity<ApiResponse<List<TorneoDTO>>> filtrarPorEstado(
            @Parameter(description = "Estado del torneo", example = "ACTIVO")
            @PathVariable EstadoTorneo estado,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaInicio").descending());
        Page<Torneo> torneosPage = torneoRepository.findByEstado(estado, pageable);

        List<TorneoDTO> torneosDTO = torneosPage.getContent().stream()
            .map(torneo -> {
                TorneoDTO dto = new TorneoDTO(torneo);
                long inscritos = inscripcionRepository.countByTorneo(torneo);
                dto.setInscritosActuales((int) inscritos);
                return dto;
            })
            .collect(Collectors.toList());

        PaginationMeta pagination = new PaginationMeta(
            torneosPage.getNumber(),
            torneosPage.getSize(),
            torneosPage.getTotalElements(),
            torneosPage.getTotalPages(),
            torneosPage.hasNext(),
            torneosPage.hasPrevious()
        );

        return ResponseEntity.ok(ApiResponse.successWithPagination(torneosDTO, pagination));
    }

    /**
     * Obtiene torneos próximos (activos o por iniciar).
     */
    @GetMapping("/proximos")
    @Operation(summary = "Torneos próximos", description = "Obtiene torneos activos o por iniciar")
    public ResponseEntity<ApiResponse<List<TorneoDTO>>> torneosProximos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaInicio").ascending());
        Page<Torneo> torneosPage = torneoRepository.findByEstadoNotIn(
            List.of(EstadoTorneo.FINALIZADO, EstadoTorneo.CANCELADO),
            pageable
        );

        List<TorneoDTO> torneosDTO = torneosPage.getContent().stream()
            .map(torneo -> {
                TorneoDTO dto = new TorneoDTO(torneo);
                long inscritos = inscripcionRepository.countByTorneo(torneo);
                dto.setInscritosActuales((int) inscritos);
                return dto;
            })
            .collect(Collectors.toList());

        PaginationMeta pagination = new PaginationMeta(
            torneosPage.getNumber(),
            torneosPage.getSize(),
            torneosPage.getTotalElements(),
            torneosPage.getTotalPages(),
            torneosPage.hasNext(),
            torneosPage.hasPrevious()
        );

        return ResponseEntity.ok(ApiResponse.successWithPagination(torneosDTO, pagination));
    }

    /**
     * Busca torneos por nombre (búsqueda parcial).
     */
    @GetMapping("/buscar")
    @Operation(summary = "Buscar torneos", description = "Busca torneos por nombre (búsqueda parcial)")
    public ResponseEntity<ApiResponse<List<TorneoDTO>>> buscarTorneos(
            @Parameter(description = "Término de búsqueda", example = "Verano")
            @RequestParam String query,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());
        Page<Torneo> torneosPage = torneoRepository.findByNombreContainingIgnoreCase(query, pageable);

        List<TorneoDTO> torneosDTO = torneosPage.getContent().stream()
            .map(torneo -> {
                TorneoDTO dto = new TorneoDTO(torneo);
                long inscritos = inscripcionRepository.countByTorneo(torneo);
                dto.setInscritosActuales((int) inscritos);
                return dto;
            })
            .collect(Collectors.toList());

        PaginationMeta pagination = new PaginationMeta(
            torneosPage.getNumber(),
            torneosPage.getSize(),
            torneosPage.getTotalElements(),
            torneosPage.getTotalPages(),
            torneosPage.hasNext(),
            torneosPage.hasPrevious()
        );

        return ResponseEntity.ok(ApiResponse.successWithPagination(torneosDTO, pagination));
    }
}
