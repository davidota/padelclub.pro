package com.padellevel.api.dto;

import com.padellevel.data.User;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO para exponer información de gamificación de un jugador.
 */
@Schema(description = "Información de gamificación de un jugador")
public class JugadorGamificacionDTO {

    @Schema(description = "ID del jugador", example = "5")
    private Long id;

    @Schema(description = "Nombre completo del jugador", example = "Juan Pérez")
    private String nombreCompleto;

    @Schema(description = "Username del jugador", example = "juanp")
    private String username;

    @Schema(description = "Nivel de gamificación", example = "15")
    private Integer nivel;

    @Schema(description = "Experiencia total", example = "1250")
    private Integer experienciaTotal;

    @Schema(description = "XP para el siguiente nivel", example = "1600")
    private Integer xpParaSiguienteNivel;

    @Schema(description = "Progreso hacia el siguiente nivel (%)", example = "65.5")
    private Double progresoNivel;

    @Schema(description = "Número de logros desbloqueados", example = "12")
    private Long logrosDesbloqueados;

    @Schema(description = "Ranking por experiencia", example = "5")
    private Integer rankingXP;

    @Schema(description = "Ranking por logros", example = "3")
    private Integer rankingLogros;

    // Constructor vacío
    public JugadorGamificacionDTO() {}

    // Constructor desde entidad User
    public JugadorGamificacionDTO(User user) {
        this.id = user.getId();
        this.nombreCompleto = user.getNombreCompleto();
        this.username = user.getUsername();
        this.nivel = user.getNivelGamificacion();
        this.experienciaTotal = user.getExperienciaTotal();
        this.xpParaSiguienteNivel = user.getXPParaSiguienteNivel();
        this.progresoNivel = user.getProgresoNivel();
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getNivel() {
        return nivel;
    }

    public void setNivel(Integer nivel) {
        this.nivel = nivel;
    }

    public Integer getExperienciaTotal() {
        return experienciaTotal;
    }

    public void setExperienciaTotal(Integer experienciaTotal) {
        this.experienciaTotal = experienciaTotal;
    }

    public Integer getXpParaSiguienteNivel() {
        return xpParaSiguienteNivel;
    }

    public void setXpParaSiguienteNivel(Integer xpParaSiguienteNivel) {
        this.xpParaSiguienteNivel = xpParaSiguienteNivel;
    }

    public Double getProgresoNivel() {
        return progresoNivel;
    }

    public void setProgresoNivel(Double progresoNivel) {
        this.progresoNivel = progresoNivel;
    }

    public Long getLogrosDesbloqueados() {
        return logrosDesbloqueados;
    }

    public void setLogrosDesbloqueados(Long logrosDesbloqueados) {
        this.logrosDesbloqueados = logrosDesbloqueados;
    }

    public Integer getRankingXP() {
        return rankingXP;
    }

    public void setRankingXP(Integer rankingXP) {
        this.rankingXP = rankingXP;
    }

    public Integer getRankingLogros() {
        return rankingLogros;
    }

    public void setRankingLogros(Integer rankingLogros) {
        this.rankingLogros = rankingLogros;
    }
}
