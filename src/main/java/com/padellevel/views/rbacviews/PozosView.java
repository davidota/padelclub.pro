package com.padellevel.views.rbacviews;

import com.padellevel.data.Pozo;
import com.padellevel.data.Torneo;
import com.padellevel.services.PozoService;
import com.padellevel.services.TorneoService;
import com.padellevel.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.QueryParameters;
import java.util.List;

@PageTitle("Gestión de Pozos")
@Route(value = "torneo", layout = MainLayout.class)
@Menu(order = 1, icon = LineAwesomeIconUrl.PENCIL_RULER_SOLID)
@RolesAllowed("ADMIN")
public class PozosView extends VerticalLayout implements BeforeEnterObserver {

    // Services
    private final TorneoService torneoService;
    private final PozoService pozoService;

    // UI Components
    private ComboBox<Torneo> torneoCombo;
    private Grid<Pozo> pozoGrid = new Grid<>(Pozo.class);
    private FormLayout pozoForm = new FormLayout();
    private Binder<Pozo> binder = new Binder<>(Pozo.class);
    private Button guardarBtn = new Button("Guardar");
    private Button nuevoBtn = new Button("Nuevo");
    private Button eliminarBtn = new Button("Eliminar");

    // Holds the currently selected Pozo
    private Pozo pozoSeleccionado;

    @Autowired
    public PozosView(TorneoService torneoService, PozoService pozoService) {
        this.torneoService = torneoService;
        this.pozoService = pozoService;
        add(createTorneoSelection(), createPozoManagement());
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Check for the "torneoId" query parameter
        event.getLocation().getQueryParameters().getParameters().getOrDefault("torneoId", List.of()).stream()
            .findFirst()
            .ifPresent(idStr -> {
                try {
                    Long id = Long.parseLong(idStr);
                    Torneo torneo = torneoService.findById(id);
                    if (torneo != null) {
                        torneoCombo.setValue(torneo);
                        loadPozos(torneo.getPozos());
                    }
                } catch (NumberFormatException ex) {
                    // Handle invalid parameter
                }
            });
    }

    private VerticalLayout createTorneoSelection() {
        torneoCombo = new ComboBox<>("Seleccionar Torneo");
        torneoCombo.setItems(torneoService.findAll());
        torneoCombo.setItemLabelGenerator(Torneo::getNombre);
        torneoCombo.addValueChangeListener(e -> {
            Torneo torneo = e.getValue();
            if (torneo != null) {
                // Load the selected tournament with its Pozos
                Torneo selectedTorneo = torneoService.findById(torneo.getId());
                loadPozos(selectedTorneo.getPozos());
            } else {
                pozoGrid.setItems();
            }
        });
        return new VerticalLayout(torneoCombo);
    }

    private HorizontalLayout createPozoManagement() {
        // Configure Grid
        pozoGrid.setColumns("id", "nombre", "tipoEnfrentamiento");
        pozoGrid.addSelectionListener(e -> {
            if (e.getFirstSelectedItem().isPresent()){
                pozoSeleccionado = e.getFirstSelectedItem().get();
                binder.readBean(pozoSeleccionado);
            } else {
                // Clear the form if nothing is selected
                pozoSeleccionado = new Pozo();
                binder.readBean(pozoSeleccionado);
            }
        });

        // Create form for Pozo
        com.vaadin.flow.component.textfield.TextField nombreField = new com.vaadin.flow.component.textfield.TextField("Nombre");
        com.vaadin.flow.component.textfield.TextField tipoField = new com.vaadin.flow.component.textfield.TextField("Tipo Enfrentamiento");

        pozoForm.add(nombreField, tipoField);
        binder.bind(nombreField, Pozo::getNombre, Pozo::setNombre);
        binder.bind(tipoField, Pozo::getTipoEnfrentamiento, Pozo::setTipoEnfrentamiento);

        // Button actions: Nuevo, Guardar y Eliminar
        nuevoBtn.addClickListener(e -> {
            pozoSeleccionado = new Pozo();
            binder.readBean(pozoSeleccionado);
        });

        guardarBtn.addClickListener(e -> {
            try {
                if (pozoSeleccionado == null) {
                    pozoSeleccionado = new Pozo();
                }
                binder.writeBean(pozoSeleccionado);
                Torneo torneo = torneoCombo.getValue();
                if (torneo == null) {
                    Notification.show("Seleccione un torneo primero.", 3000, Notification.Position.MIDDLE);
                    return;
                }
                pozoSeleccionado.setTorneo(torneo);
                // Save the pozo and update the torneo 
                pozoService.save(pozoSeleccionado);
                Torneo updatedTorneo = torneoService.addPozoToTorneo(torneo.getId(), pozoSeleccionado);
                loadPozos(updatedTorneo.getPozos());
                Notification.show("Pozo guardado.", 3000, Notification.Position.MIDDLE);
                // Clear the editor so that a new Pozo can be created immediately.
                pozoSeleccionado = new Pozo();
                binder.readBean(pozoSeleccionado);
            } catch (Exception ex) {
                Notification.show("Error al guardar: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
            }
        });

        eliminarBtn.addClickListener(e -> {
            if (pozoSeleccionado != null && pozoSeleccionado.getId() != null) {
                pozoService.delete(pozoSeleccionado);
                Torneo torneo = torneoCombo.getValue();
                if (torneo != null) {
                    Torneo updatedTorneo = torneoService.findById(torneo.getId());
                    loadPozos(updatedTorneo.getPozos());
                }
                pozoSeleccionado = new Pozo();
                binder.readBean(pozoSeleccionado);
                Notification.show("Pozo eliminado.", 3000, Notification.Position.MIDDLE);
            } else {
                Notification.show("Seleccione un pozo existente para eliminar.", 3000, Notification.Position.MIDDLE);
            }
        });

        // New button: Ir al Pozo
        Button irAlPozoBtn = new Button("Ir al Pozo", event -> {
            if (pozoSeleccionado != null && pozoSeleccionado.getTorneo() != null) {
                // Use the new method to re-fetch the pozo with enfrentamientos initialized.
                pozoService.findByIdWithEnfrentamientos(pozoSeleccionado.getId()).ifPresent(fullPozo -> {
                    if (fullPozo.getEnfrentamientos() != null && !fullPozo.getEnfrentamientos().isEmpty()) {
                        StringBuilder matchesBuilder = new StringBuilder();
                        fullPozo.getEnfrentamientos().forEach(enf -> {
                            if (enf.getResultado() != null) {
                                if (matchesBuilder.length() > 0) {
                                    matchesBuilder.append(";");
                                }
                                matchesBuilder.append(enf.getResultado());
                            }
                        });
                        String matchesParam = matchesBuilder.toString();
                        getUI().ifPresent(ui -> ui.navigate("verPozoEquipos",
                            com.vaadin.flow.router.QueryParameters.simple(
                                java.util.Map.of("pozoNombre", fullPozo.getNombre(), "matches", matchesParam)
                            )
                        ));
                    } else {
                        // Modify navigation to add "pozoId"
                        getUI().ifPresent(ui -> ui.navigate("generarPozo",
                            com.vaadin.flow.router.QueryParameters.simple(
                                java.util.Map.of(
                                    "pozoId", fullPozo.getId().toString(),
                                    "pozoNombre", fullPozo.getNombre(),
                                    "torneoId", pozoSeleccionado.getTorneo().getId().toString()
                                )
                            )
                        ));
                    }
                });
            } else {
                Notification.show("Seleccione un pozo con torneo asociado.", 3000, Notification.Position.MIDDLE);
            }
        });

        // Group all buttons together
        HorizontalLayout buttonsLayout = new HorizontalLayout(nuevoBtn, guardarBtn, eliminarBtn, irAlPozoBtn);
        VerticalLayout formLayout = new VerticalLayout(pozoForm, buttonsLayout);
        HorizontalLayout layout = new HorizontalLayout(pozoGrid, formLayout);
        layout.setSizeFull();
        pozoGrid.setWidth("50%");
        return layout;
    }

    @Transactional
    private void loadPozos(List<Pozo> pozos) {
        // Refresh grid with current pozos
        pozoGrid.setItems(pozos);
    }
}
