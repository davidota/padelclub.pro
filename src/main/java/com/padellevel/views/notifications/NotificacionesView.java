package com.padellevel.views.notifications;

import com.padellevel.data.Notificacion;
import com.padellevel.data.User;
import com.padellevel.security.AuthenticatedUser;
import com.padellevel.services.NotificacionService;
import com.padellevel.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Vista de notificaciones del usuario.
 *
 * Muestra:
 * - Todas las notificaciones del usuario
 * - Estado de lectura (leída/no leída)
 * - Opción para marcar como leída
 * - Contador de notificaciones no leídas
 */
@PageTitle("Notificaciones")
@Route(value = "notificaciones", layout = MainLayout.class)
@PermitAll
public class NotificacionesView extends VerticalLayout {

    private final AuthenticatedUser authenticatedUser;
    private final NotificacionService notificacionService;

    private Grid<Notificacion> grid;
    private Span badge;
    private User currentUser;

    @Autowired
    public NotificacionesView(AuthenticatedUser authenticatedUser,
                             NotificacionService notificacionService) {
        this.authenticatedUser = authenticatedUser;
        this.notificacionService = notificacionService;

        addClassName("notificaciones-view");
        setSpacing(true);
        setPadding(true);

        currentUser = authenticatedUser.get().orElse(null);
        if (currentUser == null) {
            add(new Paragraph("Usuario no autenticado"));
            return;
        }

        createHeader();
        createNotificationsGrid();
        loadNotifications();
    }

    private void createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);

        H2 title = new H2("Notificaciones");

        badge = new Span();
        badge.getElement().getThemeList().add("badge error pill");
        updateBadge();

        Button markAllReadButton = new Button("Marcar todas como leídas");
        markAllReadButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        markAllReadButton.addClickListener(e -> markAllAsRead());

        header.add(title, badge);
        header.setFlexGrow(1, title);
        header.add(markAllReadButton);

        add(header);
    }

    private void createNotificationsGrid() {
        grid = new Grid<>(Notificacion.class, false);
        grid.setWidthFull();
        grid.setHeight("600px");

        // Columna de icono y estado
        grid.addColumn(new ComponentRenderer<>(notificacion -> {
            Icon icon;
            switch (notificacion.getTipo()) {
                case PAGO_CONFIRMADO:
                    icon = VaadinIcon.MONEY.create();
                    icon.setColor("green");
                    break;
                case INSCRIPCION_CONFIRMADA:
                    icon = VaadinIcon.CHECK_CIRCLE.create();
                    icon.setColor("blue");
                    break;
                case PARTIDO_PROGRAMADO:
                    icon = VaadinIcon.CALENDAR.create();
                    icon.setColor("orange");
                    break;
                case RESULTADO_PARTIDO:
                    icon = VaadinIcon.TROPHY.create();
                    icon.setColor("gold");
                    break;
                default:
                    icon = VaadinIcon.BELL.create();
                    icon.setColor("gray");
            }

            HorizontalLayout layout = new HorizontalLayout(icon);
            layout.setAlignItems(Alignment.CENTER);

            if (!notificacion.getLeida()) {
                Span unreadBadge = new Span("●");
                unreadBadge.getStyle().set("color", "var(--lumo-primary-color)");
                unreadBadge.getStyle().set("font-size", "20px");
                layout.add(unreadBadge);
            }

            return layout;
        })).setHeader("").setWidth("80px").setFlexGrow(0);

        // Columna de título y mensaje
        grid.addColumn(new ComponentRenderer<>(notificacion -> {
            VerticalLayout layout = new VerticalLayout();
            layout.setSpacing(false);
            layout.setPadding(false);

            Span titulo = new Span(notificacion.getTitulo());
            titulo.getStyle().set("font-weight", notificacion.getLeida() ? "normal" : "bold");
            titulo.getStyle().set("font-size", "var(--lumo-font-size-m)");

            Paragraph mensaje = new Paragraph(notificacion.getMensaje());
            mensaje.getStyle().set("color", "var(--lumo-secondary-text-color)");
            mensaje.getStyle().set("font-size", "var(--lumo-font-size-s)");
            mensaje.getStyle().set("margin", "0");

            layout.add(titulo, mensaje);
            return layout;
        })).setHeader("Notificación").setFlexGrow(1);

        // Columna de fecha
        grid.addColumn(notificacion -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return notificacion.getFechaEnvio() != null
                ? notificacion.getFechaEnvio().format(formatter)
                : "";
        }).setHeader("Fecha").setWidth("150px").setFlexGrow(0);

        // Columna de acciones
        grid.addColumn(new ComponentRenderer<>(notificacion -> {
            Button markReadButton = new Button("Marcar leída");
            markReadButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            markReadButton.setVisible(!notificacion.getLeida());
            markReadButton.addClickListener(e -> markAsRead(notificacion));
            return markReadButton;
        })).setHeader("Acciones").setWidth("120px").setFlexGrow(0);

        add(grid);
    }

    private void loadNotifications() {
        List<Notificacion> notificaciones = notificacionService.obtenerNotificacionesUsuario(currentUser);
        grid.setItems(notificaciones);
        updateBadge();
    }

    private void markAsRead(Notificacion notificacion) {
        notificacionService.marcarComoLeida(notificacion.getId());
        loadNotifications();
    }

    private void markAllAsRead() {
        List<Notificacion> noLeidas = notificacionService.obtenerNotificacionesNoLeidas(currentUser);
        for (Notificacion notificacion : noLeidas) {
            notificacionService.marcarComoLeida(notificacion.getId());
        }
        loadNotifications();
    }

    private void updateBadge() {
        long unreadCount = notificacionService.contarNotificacionesNoLeidas(currentUser);
        badge.setText(String.valueOf(unreadCount));
        badge.setVisible(unreadCount > 0);
    }
}
