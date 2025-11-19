package com.padellevel.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Metadata de paginación para respuestas de la API.
 */
@Schema(description = "Información de paginación")
public class PaginationMeta {

    @Schema(description = "Página actual (base 0)", example = "0")
    private int currentPage;

    @Schema(description = "Tamaño de página", example = "20")
    private int pageSize;

    @Schema(description = "Total de elementos", example = "150")
    private long totalElements;

    @Schema(description = "Total de páginas", example = "8")
    private int totalPages;

    @Schema(description = "Indica si hay página siguiente", example = "true")
    private boolean hasNext;

    @Schema(description = "Indica si hay página anterior", example = "false")
    private boolean hasPrevious;

    public PaginationMeta() {}

    public PaginationMeta(int currentPage, int pageSize, long totalElements, int totalPages,
                         boolean hasNext, boolean hasPrevious) {
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.hasNext = hasNext;
        this.hasPrevious = hasPrevious;
    }

    // Getters y Setters
    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }
}
