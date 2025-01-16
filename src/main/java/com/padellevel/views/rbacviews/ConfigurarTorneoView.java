package com.padellevel.views.rbacviews;

import com.padellevel.data.Torneo;
import com.padellevel.data.Enfrentamiento;
import com.padellevel.data.TipoTorneo;
import com.padellevel.data.User;
import com.padellevel.services.TorneoService;
import com.padellevel.services.UserService;
import com.padellevel.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.vaadin.lineawesome.LineAwesomeIconUrl;
import jakarta.annotation.security.RolesAllowed;
import net.datafaker.Faker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.IntegerField;

import java.util.ArrayList;
import java.util.List;

@Route(value = "configTorneo", layout = MainLayout.class)
@Menu(order = 1, icon = LineAwesomeIconUrl.PENCIL_RULER_SOLID)
@PageTitle("Configurar Torneo")
@RolesAllowed("ADMIN")
public class ConfigurarTorneoView extends VerticalLayout {
    private Grid<Torneo> torneoGrid;
    private Button nuevoTorneoBtn;
    private Button borrarTorneosBtn;
    private TextField nombreField;
    private ComboBox<TipoTorneo> tipoComboBox;
    private NumberField numeroEnfrentamientosField;
    private NumberField juegosPorEnfrentamientoField;
    private Grid<User> participantesGrid;
    private TorneoService torneoService;
    private UserService userService;
    private Torneo torneoEditado;
    private Grid<Enfrentamiento> enfrentamientosGrid;
    private NumberField numeroEnfrentamientosSimultaneosField;
    private IntegerField numeroDeVueltasField; // Changed from NumberField
    private Binder<Torneo> binder = new Binder<>(Torneo.class);
    private Dialog editDialog;

    public ConfigurarTorneoView(TorneoService torneoService, UserService userService) {
        this.torneoService = torneoService;
        this.userService = userService;

        // Configurar el Grid de Torneos
        torneoGrid = new Grid<>(Torneo.class);
        torneoGrid.setColumns("nombre", "tipo", "numeroEnfrentamientos", "juegosPorEnfrentamiento", "numeroDeVueltas"); // Added 'numeroDeVueltas'
        torneoGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        torneoGrid.setItems(torneoService.findAll());

        // Agregar listener para doble clic en el grid de torneos
        torneoGrid.addItemDoubleClickListener(event -> {
            Torneo selectedTorneo = event.getItem();
            editarTorneo(selectedTorneo);
        });

        // Campo de búsqueda y botones
        HorizontalLayout searchAndButtonsLayout = new HorizontalLayout();
        searchAndButtonsLayout.setWidthFull();
        searchAndButtonsLayout.setSpacing(true);
        searchAndButtonsLayout.setAlignItems(FlexComponent.Alignment.BASELINE);

        TextField searchField = new TextField();
        searchField.setPlaceholder("Buscar torneos...");
        searchField.setWidth("50%");
        searchField.addValueChangeListener(e -> filtrarTorneos(e.getValue()));
        searchAndButtonsLayout.add(searchField);

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);

        nuevoTorneoBtn = new Button("Nuevo Torneo", e -> openEditDialog(new Torneo()));
        borrarTorneosBtn = new Button("Borrar Torneos", ev -> deleteSelectedTorneos());
        buttonsLayout.add(nuevoTorneoBtn, borrarTorneosBtn);

        searchAndButtonsLayout.add(buttonsLayout);
        add(searchAndButtonsLayout);

        add(torneoGrid);

        // Initialize Edit Dialog
        initEditDialog();
    }

    private void openEditDialog(Torneo torneo) {
        torneoEditado = torneo;
        binder.readBean(torneoEditado);
        participantesGrid.deselectAll();
        torneoEditado.getJugadores().forEach(participantesGrid::select);
        editDialog.open();
    }

    private String getTipoTorneoLabel(TipoTorneo tipo) {
        switch (tipo) {
            case ROUND_ROBIN:
                return "Round-Robin";
            case ELIMINACION_DIRECTA:
                return "Eliminación Directa";
            case GRUPOS:
                return "Grupos";
            default:
                return tipo.name();
        }
    }

    private void guardarNuevoTorneo() {
        // Validar campos requeridos
        if (nombreField.isEmpty() || tipoComboBox.isEmpty() ||
                numeroEnfrentamientosField.isEmpty() || juegosPorEnfrentamientoField.isEmpty() ||
                numeroDeVueltasField.isEmpty()) { // Added validation
            Notification.show("Por favor, completa todos los campos requeridos.", 3000, Notification.Position.MIDDLE);
            return;
        }

        // Crear o actualizar Torneo
        Torneo nuevoTorneo = obtenerTorneoEditado() != null ? obtenerTorneoEditado() : new Torneo();
        nuevoTorneo.setNombre(nombreField.getValue());
        nuevoTorneo.setTipo(tipoComboBox.getValue());
        nuevoTorneo.setNumeroEnfrentamientos(numeroEnfrentamientosField.getValue().intValue());
        nuevoTorneo.setJuegosPorEnfrentamiento(juegosPorEnfrentamientoField.getValue().intValue());
        nuevoTorneo.setNumeroEnfrentamientosSimultaneos(numeroEnfrentamientosSimultaneosField.getValue().intValue());
        nuevoTorneo.setNumeroDeVueltas(numeroDeVueltasField.getValue()); // Set new field

        // Obtener participantes seleccionados
        List<User> seleccionados = participantesGrid.getSelectedItems().stream().toList();
        nuevoTorneo.setJugadores(seleccionados);

        // Guardar Torneo
        try {
            torneoService.save(nuevoTorneo);
            actualizarListaTorneos();
            Notification.show("Torneo guardado exitosamente.", 3000, Notification.Position.MIDDLE);

            // Reset the edit tracking field
            torneoEditado = null;
        } catch (Exception e) {
            // Manejo de excepciones al guardar torneo
            System.err.println("Error al guardar torneo: " + e.getMessage());
            e.printStackTrace();
            Notification.show("Error al guardar el torneo. Por favor, intenta nuevamente.", 5000,
                    Notification.Position.MIDDLE);
        }
    }

    private Torneo obtenerTorneoEditado() {
        return torneoEditado;
    }

    private void actualizarListaTorneos() {
        try {
            torneoGrid.setItems(torneoService.findAll());
        } catch (Exception e) {
            // Manejo de excepciones al actualizar lista
            System.err.println("Error al actualizar lista de torneos: " + e.getMessage());
            e.printStackTrace();
            torneoGrid.setItems(new ArrayList<>());
        }
    }

    private void deleteSelectedTorneos() {
        List<Torneo> seleccionados = torneoGrid.getSelectedItems().stream().toList();
        if (seleccionados.isEmpty()) {
            Notification.show("No se ha seleccionado ningún torneo para borrar.", 3000, Notification.Position.MIDDLE);
            return;
        }

        try {
            seleccionados.forEach(t -> torneoService.delete(t.getId()));
            actualizarListaTorneos();
            Notification.show("Torneos borrados exitosamente.", 3000, Notification.Position.MIDDLE);
        } catch (Exception e) {
            System.err.println("Error al borrar torneos: " + e.getMessage());
            e.printStackTrace();
            Notification.show("Error al borrar los torneos. Por favor, intenta nuevamente.", 5000,
                    Notification.Position.MIDDLE);
        }
    }

    private void filtrarTorneos(String query) {
        if (query == null || query.trim().isEmpty()) {
            torneoGrid.setItems(torneoService.findAll());
        } else {
            List<Torneo> torneosFiltrados = torneoService.searchTorneos(query);
            torneoGrid.setItems(torneosFiltrados);
        }
    }

    private void editarTorneo(Torneo torneo) {
        torneoEditado = torneoService.findByIdWithJugadores(torneo.getId());
        if (torneoEditado == null) {
            Notification.show("Torneo no encontrado.", 3000, Notification.Position.MIDDLE);
            return;
        }

        binder.readBean(torneoEditado);

        // Select the jugadores in the grid
        participantesGrid.deselectAll();
        torneoEditado.getJugadores().forEach(participantesGrid::select);

        // Open the edit dialog
        editDialog.open();
    }

    private void initEditDialog() {
        editDialog = new Dialog();
        FormLayout formLayout = new FormLayout();

        nombreField = new TextField("Nombre del Torneo");
        tipoComboBox = new ComboBox<>("Tipo de Torneo");
        tipoComboBox.setItems(TipoTorneo.values());
        tipoComboBox.setItemLabelGenerator(TipoTorneo::name);

        numeroEnfrentamientosField = new NumberField("Número de Enfrentamientos");
        numeroEnfrentamientosField.setMin(1);
        juegosPorEnfrentamientoField = new NumberField("Juegos por Enfrentamiento");
        juegosPorEnfrentamientoField.setMin(1);
        numeroEnfrentamientosSimultaneosField = new NumberField("Número de Enfrentamientos Simultáneos");
        numeroEnfrentamientosSimultaneosField.setMin(1);
        
        numeroDeVueltasField = new IntegerField("Número de Vueltas"); // Changed to IntegerField
        numeroDeVueltasField.setMin(1);

        participantesGrid = new Grid<>(User.class);
        participantesGrid.setItems(userService.findAll());
        participantesGrid.removeAllColumns();
        participantesGrid.addColumn(User::getName).setHeader("Nombre");
        participantesGrid.addColumn(User::getApellido).setHeader("Apellido");
        participantesGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        participantesGrid.setHeight("200px");

        binder.bind(nombreField, Torneo::getNombre, Torneo::setNombre);
        binder.bind(tipoComboBox, Torneo::getTipo, Torneo::setTipo);
        binder.bind(numeroEnfrentamientosField,
                torneo -> (double) torneo.getNumeroEnfrentamientos(),
                (torneo, value) -> torneo.setNumeroEnfrentamientos(value.intValue()));

        binder.bind(juegosPorEnfrentamientoField,
                torneo -> (double) torneo.getJuegosPorEnfrentamiento(),
                (torneo, value) -> torneo.setJuegosPorEnfrentamiento(value.intValue()));
        binder.bind(numeroEnfrentamientosSimultaneosField,
                torneo -> (double)torneo.getNumeroEnfrentamientosSimultaneos(),
                (torneo, value) -> torneo.setNumeroEnfrentamientosSimultaneos(value.intValue()));
        binder.bind(numeroDeVueltasField,
                Torneo::getNumeroDeVueltas,
                Torneo::setNumeroDeVueltas); // Updated binding to use IntegerField

        binder.addStatusChangeListener(e -> {
            // Enable save button only when the form is valid
            // saveButton.setEnabled(binder.isValid());
        });

        Button guardarBtn = new Button("Guardar", event -> {
            try {
                binder.writeBean(torneoEditado);
                torneoEditado.setJugadores(participantesGrid.getSelectedItems().stream().toList());
                torneoService.save(torneoEditado);
                torneoGrid.setItems(torneoService.findAll());
                Notification.show("Torneo actualizado exitosamente.", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                editDialog.close();
            } catch (ValidationException | RuntimeException ex) {
                Notification.show("Error al actualizar el torneo.", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                ex.printStackTrace();
            }
        });

        Button cancelarBtn = new Button("Cancelar", event -> editDialog.close());

        HorizontalLayout botonesDialogo = new HorizontalLayout(guardarBtn, cancelarBtn);
        formLayout.add(nombreField, tipoComboBox, numeroEnfrentamientosField,
                juegosPorEnfrentamientoField, numeroEnfrentamientosSimultaneosField,
                numeroDeVueltasField, participantesGrid, botonesDialogo);
        editDialog.add(formLayout);
    }

    private void generarTorneo() {
        List<User> seleccionados = participantesGrid.getSelectedItems().stream().toList();

        if (seleccionados.size() % 2 != 0) {
            Notification.show("El número de participantes debe ser par.", 3000, Notification.Position.MIDDLE);
            return;
        }

        int participantesTotal = seleccionados.size();

        // Parámetro capturado: cantidad de partidos que pueden producirse
        // simultáneamente
        int partidosSimultaneos = numeroEnfrentamientosField.getValue().intValue();

        // Lógica para generar enfrentamientos
        // ...

        Notification.show("Torneo generado exitosamente.", 3000, Notification.Position.MIDDLE);
    }

    private void actualizarEnfrentamientosTab(List<Enfrentamiento> enfrentamientos) {
        enfrentamientosGrid.setItems(enfrentamientos);
    }

    private void mostrarEnfrentamientosAgrupados(List<Enfrentamiento> enfrentamientos, int juegosPorEnfrentamiento) {
        removeAll(); // Clear or replace with a container if you'd like to keep existing layout
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setWidthFull();

        Faker faker = new Faker();

        // Group confrontations in rows of 2 pairs
        for (int i = 0; i < enfrentamientos.size(); i += 2) {
            HorizontalLayout rowLayout = new HorizontalLayout();
            rowLayout.setWidthFull();
            rowLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

            if (i < enfrentamientos.size()) {
                Enfrentamiento e1 = enfrentamientos.get(i);
                String leftTeamName = faker.team().name();
                e1.setEquipoGanador(""); // Initialize if needed

                VerticalLayout leftTeamLayout = new VerticalLayout();
                leftTeamLayout.add(new Span("Equipo Izquierdo: " + leftTeamName));
                rowLayout.add(leftTeamLayout);

                // Create ComboBoxes for each game
                HorizontalLayout gamesLayout = new HorizontalLayout();
                for (int j = 0; j < juegosPorEnfrentamiento; j++) {
                    ComboBox<String> combo = new ComboBox<>();
                    combo.setItems("", "empate", leftTeamName, "");
                    combo.setValue("");
                    gamesLayout.add(combo);
                }
                rowLayout.add(gamesLayout);
            }

            // Right pair
            if (i + 1 < enfrentamientos.size()) {
                Enfrentamiento e2 = enfrentamientos.get(i + 1);
                String rightTeamName = faker.team().name();
                e2.setEquipoGanador(""); // Initialize if needed

                VerticalLayout rightTeamLayout = new VerticalLayout();
                rightTeamLayout.add(new Span("Equipo Derecho: " + rightTeamName));
                rowLayout.add(rightTeamLayout);
            }

            mainLayout.add(rowLayout);
        }

        add(mainLayout);
    }
}