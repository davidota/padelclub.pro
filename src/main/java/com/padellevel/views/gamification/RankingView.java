package com.padellevel.views.gamification;

import com.padellevel.data.User;
import com.padellevel.services.GamificacionService;
import com.padellevel.views.MainLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Vista de rankings de gamificación.
 * Muestra los mejores jugadores por XP y por logros desbloqueados.
 */
@PageTitle("Rankings")
@Route(value = "ranking", layout = MainLayout.class)
@PermitAll
public class RankingView extends VerticalLayout {

    private final GamificacionService gamificacionService;

    private Grid<User> rankingXPGrid;
    private Grid<Map.Entry<User, Long>> rankingLogrosGrid;
    private VerticalLayout contentLayout;

    public RankingView(GamificacionService gamificacionService) {
        this.gamificacionService = gamificacionService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("🏆 Rankings");
        add(title);

        // Tabs para alternar entre rankings
        Tab tabXP = new Tab("🌟 Por Experiencia");
        Tab tabLogros = new Tab("🎖️ Por Logros");

        Tabs tabs = new Tabs(tabXP, tabLogros);
        add(tabs);

        // Contenedor de contenido
        contentLayout = new VerticalLayout();
        contentLayout.setSizeFull();
        contentLayout.setPadding(false);
        add(contentLayout);

        // Listener de cambio de tab
        tabs.addSelectedChangeListener(event -> {
            Tab selectedTab = event.getSelectedTab();
            if (selectedTab == tabXP) {
                showRankingXP();
            } else if (selectedTab == tabLogros) {
                showRankingLogros();
            }
        });

        // Mostrar ranking por XP por defecto
        showRankingXP();
    }

    /**
     * Muestra el ranking por experiencia.
     */
    private void showRankingXP() {
        contentLayout.removeAll();

        H3 subtitle = new H3("Top Jugadores por Experiencia");
        contentLayout.add(subtitle);

        rankingXPGrid = new Grid<>(User.class, false);
        rankingXPGrid.setHeight("600px");

        // Columna de posición
        AtomicInteger position = new AtomicInteger(1);
        rankingXPGrid.addColumn(new ComponentRenderer<>(user -> {
            int pos = position.getAndIncrement();
            return createPositionBadge(pos);
        })).setHeader("Pos.").setWidth("80px").setFlexGrow(0);

        // Columna de jugador
        rankingXPGrid.addColumn(User::getNombreCompleto)
            .setHeader("Jugador")
            .setAutoWidth(true);

        // Columna de nivel
        rankingXPGrid.addColumn(new ComponentRenderer<>(user -> {
            Span badge = new Span("Nivel " + user.getNivelGamificacion());
            badge.getStyle()
                .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                .set("color", "white")
                .set("padding", "5px 15px")
                .set("border-radius", "15px")
                .set("font-weight", "bold")
                .set("font-size", "0.9em");
            return badge;
        })).setHeader("Nivel").setWidth("150px").setFlexGrow(0);

        // Columna de XP
        rankingXPGrid.addColumn(user -> user.getExperienciaTotal() + " XP")
            .setHeader("Experiencia Total")
            .setAutoWidth(true);

        // Cargar datos
        List<User> topPlayers = gamificacionService.obtenerRankingPorExperiencia(50);
        rankingXPGrid.setItems(topPlayers);

        contentLayout.add(rankingXPGrid);
    }

    /**
     * Muestra el ranking por logros.
     */
    private void showRankingLogros() {
        contentLayout.removeAll();

        H3 subtitle = new H3("Top Jugadores por Logros Desbloqueados");
        contentLayout.add(subtitle);

        rankingLogrosGrid = new Grid<>();
        rankingLogrosGrid.setHeight("600px");

        // Columna de posición
        AtomicInteger position = new AtomicInteger(1);
        rankingLogrosGrid.addColumn(new ComponentRenderer<>(entry -> {
            int pos = position.getAndIncrement();
            return createPositionBadge(pos);
        })).setHeader("Pos.").setWidth("80px").setFlexGrow(0);

        // Columna de jugador
        rankingLogrosGrid.addColumn(entry -> entry.getKey().getNombreCompleto())
            .setHeader("Jugador")
            .setAutoWidth(true);

        // Columna de nivel
        rankingLogrosGrid.addColumn(new ComponentRenderer<>(entry -> {
            User user = entry.getKey();
            Span badge = new Span("Nivel " + user.getNivelGamificacion());
            badge.getStyle()
                .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                .set("color", "white")
                .set("padding", "5px 15px")
                .set("border-radius", "15px")
                .set("font-weight", "bold")
                .set("font-size", "0.9em");
            return badge;
        })).setHeader("Nivel").setWidth("150px").setFlexGrow(0);

        // Columna de logros
        rankingLogrosGrid.addColumn(new ComponentRenderer<>(entry -> {
            Long count = entry.getValue();
            Span badge = new Span("🎖️ " + count + " logros");
            badge.getStyle()
                .set("background", "#ffd700")
                .set("color", "#333")
                .set("padding", "5px 15px")
                .set("border-radius", "15px")
                .set("font-weight", "bold")
                .set("font-size", "0.9em");
            return badge;
        })).setHeader("Logros").setWidth("180px").setFlexGrow(0);

        // Columna de XP
        rankingLogrosGrid.addColumn(entry -> entry.getKey().getExperienciaTotal() + " XP")
            .setHeader("XP Total")
            .setAutoWidth(true);

        // Cargar datos
        List<Map.Entry<User, Long>> topPlayers = gamificacionService.obtenerRankingPorLogros(50);
        rankingLogrosGrid.setItems(topPlayers);

        contentLayout.add(rankingLogrosGrid);
    }

    /**
     * Crea un badge de posición con estilo según el ranking.
     */
    private Span createPositionBadge(int position) {
        String emoji;
        String backgroundColor;

        switch (position) {
            case 1:
                emoji = "🥇";
                backgroundColor = "#ffd700"; // Oro
                break;
            case 2:
                emoji = "🥈";
                backgroundColor = "#c0c0c0"; // Plata
                break;
            case 3:
                emoji = "🥉";
                backgroundColor = "#cd7f32"; // Bronce
                break;
            default:
                emoji = String.valueOf(position);
                backgroundColor = "#e0e0e0"; // Gris
        }

        Span badge = new Span(emoji);
        badge.getStyle()
            .set("background", backgroundColor)
            .set("color", position <= 3 ? "white" : "#333")
            .set("padding", "8px 12px")
            .set("border-radius", "50%")
            .set("font-weight", "bold")
            .set("font-size", "1.1em")
            .set("text-align", "center")
            .set("min-width", "40px")
            .set("display", "inline-block");

        return badge;
    }
}
