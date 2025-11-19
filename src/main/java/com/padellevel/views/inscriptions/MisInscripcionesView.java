package com.padellevel.views.inscriptions;

import com.padellevel.data.Inscripcion;
import com.padellevel.data.User;
import com.padellevel.security.AuthenticatedUser;
import com.padellevel.services.InscripcionService;
import com.padellevel.services.CalendarioService;
import com.padellevel.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Vista de inscripciones del usuario.
 *
 * Muestra:
 * - Todas las inscripciones a torneos
 * - Estado de cada inscripción
 * - Información del torneo
 * - Opciones de descarga de calendario
 * - Estado de pago
 */
@PageTitle("Mis Inscripciones")
@Route(value = "mis-inscripciones", layout = MainLayout.class)
@PermitAll
public class MisInscripcionesView extends VerticalLayout {

    private final AuthenticatedUser authenticatedUser;
    private final InscripcionService inscripcionService;
    private final CalendarioService calendarioService;

    private Grid<Inscripcion> grid;
    private User currentUser;

    @Autowired
    public MisInscripcionesView(AuthenticatedUser authenticatedUser,
                                InscripcionService inscripcionService,
                                CalendarioService calendarioService) {
        this.authenticatedUser = authenticatedUser;
        this.inscripcionService = inscripcionService;
        this.calendarioService = calendarioService;

        addClassName("mis-inscripciones-view");
        setSpacing(true);
        setPadding(true);

        currentUser = authenticatedUser.get().orElse(null);
        if (currentUser == null) {
            add(new Paragraph("Usuario no autenticado"));
            return;
        }

        createHeader();
        createInscriptionsGrid();
        loadInscriptions();
    }

    private void createHeader() {
        H2 title = new H2("Mis Inscripciones");
        add(title);

        Paragraph info = new Paragraph(
            "Aquí puedes ver todas tus inscripciones a torneos, su estado y descargar el calendario.");
        info.getStyle().set("color", "var(--lumo-secondary-text-color)");
        add(info);
    }

    private void createInscriptionsGrid() {
        grid = new Grid<>(Inscripcion.class, false);
        grid.setWidthFull();
        grid.setHeight("600px");

        // Columna de torneo
        grid.addColumn(new ComponentRenderer<>(inscripcion -> {
            VerticalLayout layout = new VerticalLayout();
            layout.setSpacing(false);
            layout.setPadding(false);

            Span nombreTorneo = new Span(inscripcion.getTorneo().getNombre());
            nombreTorneo.getStyle().set("font-weight", "bold");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String fechas = String.format("%s - %s",
                inscripcion.getTorneo().getFechaInicio() != null
                    ? inscripcion.getTorneo().getFechaInicio().format(formatter)
                    : "?",
                inscripcion.getTorneo().getFechaFin() != null
                    ? inscripcion.getTorneo().getFechaFin().format(formatter)
                    : "?");

            Paragraph fechasTorneo = new Paragraph(fechas);
            fechasTorneo.getStyle().set("color", "var(--lumo-secondary-text-color)");
            fechasTorneo.getStyle().set("font-size", "var(--lumo-font-size-s)");
            fechasTorneo.getStyle().set("margin", "0");

            layout.add(nombreTorneo, fechasTorneo);
            return layout;
        })).setHeader("Torneo").setFlexGrow(1);

        // Columna de fecha de inscripción
        grid.addColumn(inscripcion -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return inscripcion.getFechaInscripcion() != null
                ? inscripcion.getFechaInscripcion().format(formatter)
                : "";
        }).setHeader("Inscrito").setWidth("120px").setFlexGrow(0);

        // Columna de estado
        grid.addColumn(new ComponentRenderer<>(inscripcion -> {
            Span badge = new Span(inscripcion.getEstado().toString());

            switch (inscripcion.getEstado()) {
                case CONFIRMADA:
                case PAGADA:
                    badge.getElement().getThemeList().add("badge success");
                    break;
                case PENDIENTE:
                    badge.getElement().getThemeList().add("badge");
                    break;
                case CANCELADA:
                    badge.getElement().getThemeList().add("badge error");
                    break;
            }

            return badge;
        })).setHeader("Estado").setWidth("120px").setFlexGrow(0);

        // Columna de pago
        grid.addColumn(inscripcion -> {
            if (inscripcion.getTorneo().getEsGratuito()) {
                return "Gratuito";
            } else if (inscripcion.getPago() != null) {
                return String.format("%.2f EUR", inscripcion.getPago().getMonto());
            } else {
                return "Pendiente";
            }
        }).setHeader("Pago").setWidth("100px").setFlexGrow(0);

        // Columna de acciones
        grid.addColumn(new ComponentRenderer<>(inscripcion -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);

            // Botón de descargar calendario
            Anchor downloadLink = createCalendarDownloadLink(inscripcion);
            actions.add(downloadLink);

            return actions;
        })).setHeader("Acciones").setWidth("150px").setFlexGrow(0);

        add(grid);
    }

    private Anchor createCalendarDownloadLink(Inscripcion inscripcion) {
        Button downloadButton = new Button("Calendario");
        downloadButton.setIcon(VaadinIcon.CALENDAR.create());
        downloadButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

        // Crear recurso de stream para el archivo .ics
        StreamResource resource = new StreamResource(
            "torneo-" + inscripcion.getTorneo().getId() + ".ics",
            () -> {
                String icsContent = calendarioService.generarIcsParaJugador(currentUser);
                return new ByteArrayInputStream(icsContent.getBytes(StandardCharsets.UTF_8));
            }
        );

        Anchor anchor = new Anchor(resource, "");
        anchor.getElement().setAttribute("download", true);
        anchor.add(downloadButton);

        return anchor;
    }

    private void loadInscriptions() {
        List<Inscripcion> inscripciones = inscripcionService.obtenerInscripcionesJugador(currentUser);

        // Ordenar por fecha de inscripción descendente
        inscripciones.sort((i1, i2) -> {
            if (i1.getFechaInscripcion() == null) return 1;
            if (i2.getFechaInscripcion() == null) return -1;
            return i2.getFechaInscripcion().compareTo(i1.getFechaInscripcion());
        });

        grid.setItems(inscripciones);
    }
}
