package com.padellevel.views.gamification;

import com.padellevel.data.LogroUsuario;
import com.padellevel.data.User;
import com.padellevel.security.AuthenticatedUser;
import com.padellevel.services.GamificacionService;
import com.padellevel.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Vista de logros del usuario.
 * Muestra badges, nivel actual, XP y progreso hacia el siguiente nivel.
 */
@PageTitle("Mis Logros")
@Route(value = "logros", layout = MainLayout.class)
@PermitAll
public class LogrosView extends VerticalLayout {

    private final GamificacionService gamificacionService;
    private final AuthenticatedUser authenticatedUser;

    private VerticalLayout logrosContainer;
    private VerticalLayout statsContainer;

    public LogrosView(GamificacionService gamificacionService,
                     AuthenticatedUser authenticatedUser) {
        this.gamificacionService = gamificacionService;
        this.authenticatedUser = authenticatedUser;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        User currentUser = authenticatedUser.get().orElse(null);
        if (currentUser == null) {
            add(new H2("Usuario no autenticado"));
            return;
        }

        // Título
        H2 title = new H2("🎖️ Mis Logros y Nivel");
        add(title);

        // Panel de estadísticas
        statsContainer = new VerticalLayout();
        statsContainer.setPadding(false);
        statsContainer.setSpacing(true);
        createStatsPanel(currentUser);
        add(statsContainer);

        // Logros desbloqueados
        H3 logrosTitle = new H3("Logros Desbloqueados");
        add(logrosTitle);

        logrosContainer = new VerticalLayout();
        logrosContainer.setPadding(false);
        logrosContainer.setSpacing(true);
        loadLogros(currentUser);
        add(logrosContainer);
    }

    /**
     * Crea el panel de estadísticas con nivel y XP.
     */
    private void createStatsPanel(User user) {
        Map<String, Object> stats = gamificacionService.obtenerEstadisticasGamificacion(user);

        // Card de nivel
        Div nivelCard = new Div();
        nivelCard.getStyle()
            .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
            .set("color", "white")
            .set("padding", "20px")
            .set("border-radius", "10px")
            .set("margin-bottom", "20px");

        int nivel = (int) stats.get("nivel");
        int xpTotal = (int) stats.get("experienciaTotal");
        int xpSiguiente = (int) stats.get("xpParaSiguienteNivel");
        double progreso = (double) stats.get("progresoNivel");

        H2 nivelText = new H2("⭐ Nivel " + nivel);
        nivelText.getStyle().set("margin", "0").set("color", "white");

        Paragraph xpText = new Paragraph(xpTotal + " XP total");
        xpText.getStyle().set("margin", "5px 0").set("color", "#e0e0e0");

        // Barra de progreso
        ProgressBar progressBar = new ProgressBar(0, 100, progreso);
        progressBar.setWidth("100%");

        Paragraph progresoText = new Paragraph(
            String.format("%.0f%% hacia nivel %d (%d XP restantes)",
                         progreso, nivel + 1, xpSiguiente - xpTotal)
        );
        progresoText.getStyle().set("margin", "5px 0 0 0").set("font-size", "0.9em").set("color", "#e0e0e0");

        nivelCard.add(nivelText, xpText, progressBar, progresoText);

        // Estadísticas de logros
        Div logrosStatsCard = new Div();
        logrosStatsCard.getStyle()
            .set("background", "#f8f9fa")
            .set("padding", "15px")
            .set("border-radius", "10px")
            .set("display", "flex")
            .set("gap", "30px");

        long logrosDesbloqueados = (long) stats.get("logrosDesbloqueados");
        long logrosNoVistos = (long) stats.get("logrosNoVistos");

        Div logrosStat = createStatItem("🏆", "Logros Desbloqueados", String.valueOf(logrosDesbloqueados));
        Div nuevosLogros = createStatItem("🆕", "Nuevos Logros", String.valueOf(logrosNoVistos));

        logrosStatsCard.add(logrosStat, nuevosLogros);

        statsContainer.add(nivelCard, logrosStatsCard);
    }

    /**
     * Crea un elemento de estadística.
     */
    private Div createStatItem(String icon, String label, String value) {
        Div container = new Div();
        container.getStyle().set("text-align", "center");

        Span iconSpan = new Span(icon);
        iconSpan.getStyle().set("font-size", "2em").set("display", "block");

        H3 valueText = new H3(value);
        valueText.getStyle().set("margin", "5px 0").set("color", "#333");

        Paragraph labelText = new Paragraph(label);
        labelText.getStyle().set("margin", "0").set("color", "#666").set("font-size", "0.9em");

        container.add(iconSpan, valueText, labelText);
        return container;
    }

    /**
     * Carga los logros desbloqueados del usuario.
     */
    private void loadLogros(User user) {
        List<LogroUsuario> logros = gamificacionService.obtenerLogrosDesbloqueados(user);

        if (logros.isEmpty()) {
            Paragraph emptyMessage = new Paragraph("Aún no has desbloqueado ningún logro. ¡Empieza a jugar para ganar logros!");
            emptyMessage.getStyle().set("color", "#666").set("text-align", "center").set("padding", "20px");
            logrosContainer.add(emptyMessage);
            return;
        }

        for (LogroUsuario logroUsuario : logros) {
            logrosContainer.add(createLogroCard(logroUsuario));
        }
    }

    /**
     * Crea una card para un logro.
     */
    private Component createLogroCard(LogroUsuario logroUsuario) {
        Div card = new Div();
        card.getStyle()
            .set("background", "white")
            .set("border", "1px solid #e0e0e0")
            .set("border-radius", "8px")
            .set("padding", "15px")
            .set("margin-bottom", "10px")
            .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)");

        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setAlignItems(Alignment.CENTER);

        // Icono del logro
        Span icon = new Span(logroUsuario.getLogro().getIcono());
        icon.getStyle()
            .set("font-size", "3em")
            .set("margin-right", "15px");

        // Información del logro
        VerticalLayout info = new VerticalLayout();
        info.setPadding(false);
        info.setSpacing(false);

        H4 nombre = new H4(logroUsuario.getLogro().getNombre());
        nombre.getStyle().set("margin", "0 0 5px 0");

        Paragraph descripcion = new Paragraph(logroUsuario.getLogro().getDescripcion());
        descripcion.getStyle().set("margin", "0 0 5px 0").set("color", "#666");

        // Fecha de desbloqueo
        Paragraph fecha = new Paragraph("Desbloqueado: " +
            logroUsuario.getFechaDesbloqueo().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        fecha.getStyle().set("margin", "0").set("font-size", "0.85em").set("color", "#999");

        info.add(nombre, descripcion, fecha);

        // Badge de nuevo si no ha sido visto
        if (!logroUsuario.getVisto()) {
            Span nuevoBadge = new Span("NUEVO");
            nuevoBadge.getStyle()
                .set("background", "#ff4444")
                .set("color", "white")
                .set("padding", "3px 10px")
                .set("border-radius", "12px")
                .set("font-size", "0.75em")
                .set("font-weight", "bold");

            // Marcar como visto cuando se muestra
            gamificacionService.marcarLogroComoVisto(logroUsuario.getId());
        }

        layout.add(icon, info);
        card.add(layout);

        return card;
    }
}
