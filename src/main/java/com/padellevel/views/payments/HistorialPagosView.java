package com.padellevel.views.payments;

import com.padellevel.data.EstadoPago;
import com.padellevel.data.Pago;
import com.padellevel.data.User;
import com.padellevel.security.AuthenticatedUser;
import com.padellevel.services.PagoService;
import com.padellevel.views.MainLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Vista de historial de pagos del usuario.
 *
 * Muestra:
 * - Todos los pagos realizados
 * - Estado de cada pago (completado, pendiente, fallido, reembolsado)
 * - Detalles: fecha, monto, torneo, método de pago
 * - Comprobantes de pago
 */
@PageTitle("Historial de Pagos")
@Route(value = "pagos", layout = MainLayout.class)
@PermitAll
public class HistorialPagosView extends VerticalLayout {

    private final AuthenticatedUser authenticatedUser;
    private final PagoService pagoService;

    private Grid<Pago> grid;
    private User currentUser;

    @Autowired
    public HistorialPagosView(AuthenticatedUser authenticatedUser,
                             PagoService pagoService) {
        this.authenticatedUser = authenticatedUser;
        this.pagoService = pagoService;

        addClassName("historial-pagos-view");
        setSpacing(true);
        setPadding(true);

        currentUser = authenticatedUser.get().orElse(null);
        if (currentUser == null) {
            add(new Paragraph("Usuario no autenticado"));
            return;
        }

        createHeader();
        createPaymentsGrid();
        loadPayments();
    }

    private void createHeader() {
        H2 title = new H2("Historial de Pagos");
        add(title);

        // Calcular total pagado
        List<Pago> pagos = pagoService.obtenerPagosPorJugador(currentUser);
        BigDecimal totalPagado = pagos.stream()
            .filter(p -> p.getEstado() == EstadoPago.COMPLETADO)
            .map(Pago::getMonto)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        Paragraph totalInfo = new Paragraph(
            String.format("Total pagado: %.2f EUR", totalPagado));
        totalInfo.getStyle().set("color", "var(--lumo-secondary-text-color)");
        add(totalInfo);
    }

    private void createPaymentsGrid() {
        grid = new Grid<>(Pago.class, false);
        grid.setWidthFull();
        grid.setHeight("600px");

        // Columna de fecha
        grid.addColumn(pago -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return pago.getFechaPago() != null
                ? pago.getFechaPago().format(formatter)
                : "";
        }).setHeader("Fecha").setWidth("150px").setFlexGrow(0);

        // Columna de torneo
        grid.addColumn(pago -> {
            if (pago.getTorneo() != null) {
                return pago.getTorneo().getNombre();
            }
            return "N/A";
        }).setHeader("Torneo").setFlexGrow(1);

        // Columna de monto
        grid.addColumn(pago -> {
            return String.format("%.2f %s",
                pago.getMonto(),
                pago.getMoneda() != null ? pago.getMoneda().toUpperCase() : "EUR");
        }).setHeader("Monto").setWidth("120px").setFlexGrow(0);

        // Columna de método de pago
        grid.addColumn(pago -> {
            return pago.getMetodo() != null ? pago.getMetodo().toString() : "N/A";
        }).setHeader("Método").setWidth("100px").setFlexGrow(0);

        // Columna de estado
        grid.addColumn(new ComponentRenderer<>(pago -> {
            Span badge = new Span(pago.getEstado().toString());

            switch (pago.getEstado()) {
                case COMPLETADO:
                    badge.getElement().getThemeList().add("badge success");
                    break;
                case PENDIENTE:
                    badge.getElement().getThemeList().add("badge");
                    break;
                case FALLIDO:
                case CANCELADO:
                    badge.getElement().getThemeList().add("badge error");
                    break;
                case REEMBOLSADO:
                    badge.getElement().getThemeList().add("badge contrast");
                    break;
            }

            return badge;
        })).setHeader("Estado").setWidth("120px").setFlexGrow(0);

        // Columna de comprobante
        grid.addColumn(pago -> {
            return pago.getComprobante() != null ? pago.getComprobante() : "-";
        }).setHeader("Comprobante").setWidth("200px").setFlexGrow(0);

        add(grid);
    }

    private void loadPayments() {
        List<Pago> pagos = pagoService.obtenerPagosPorJugador(currentUser);
        // Ordenar por fecha descendente (más recientes primero)
        pagos.sort((p1, p2) -> {
            if (p1.getFechaPago() == null) return 1;
            if (p2.getFechaPago() == null) return -1;
            return p2.getFechaPago().compareTo(p1.getFechaPago());
        });
        grid.setItems(pagos);
    }
}
