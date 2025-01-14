package com.padellevel.views.rbacviews;

import com.padellevel.data.Torneo;
import com.padellevel.data.Role;
import com.padellevel.data.TipoTorneo;
import com.padellevel.data.User;
import com.padellevel.services.TorneoService;
import com.padellevel.services.UserService;
import com.padellevel.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.vaadin.lineawesome.LineAwesomeIconUrl;
import jakarta.annotation.security.RolesAllowed;

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
    private Dialog nuevoTorneoDialog;
    private TextField nombreField;
    private ComboBox<TipoTorneo> tipoComboBox;
    private NumberField numeroEnfrentamientosField;
    private NumberField juegosPorEnfrentamientoField;
    private Grid<User> participantesGrid;
    private TorneoService torneoService;
    private UserService userService;
    private List<User> allParticipantes;

    public ConfigurarTorneoView(TorneoService torneoService, UserService userService) {
        this.torneoService = torneoService;
        this.userService = userService;

        // Configurar el Grid de Torneos
        torneoGrid = new Grid<>(Torneo.class);
        torneoGrid.setColumns("nombre", "tipo", "numeroEnfrentamientos", "juegosPorEnfrentamiento");
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

        nuevoTorneoBtn = new Button("Nuevo Torneo", e -> nuevoTorneoDialog.open());
        borrarTorneosBtn = new Button("Borrar Torneos", ev -> borrarTorneos());
        buttonsLayout.add(nuevoTorneoBtn, borrarTorneosBtn);

        searchAndButtonsLayout.add(buttonsLayout);
        add(searchAndButtonsLayout);

        add(torneoGrid);

        // Configurar el diálogo para crear o editar un nuevo torneo
        configurarNuevoTorneoDialog();

        // Configurar Tabs (si es necesario)
        configurarTabs();
    }

    private void configurarNuevoTorneoDialog() {
        nuevoTorneoDialog = new Dialog();
        nuevoTorneoDialog.setWidth("800px");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(true);
        dialogLayout.setSpacing(true);

        // Layout principal en el diálogo
        HorizontalLayout formLayout = new HorizontalLayout();
        formLayout.setWidthFull();
        formLayout.setSpacing(true);

        // Panel izquierdo con los campos del torneo
        VerticalLayout camposLayout = new VerticalLayout();
        camposLayout.setWidth("40%");
        camposLayout.setSpacing(true);

        // Campo Nombre del Torneo
        nombreField = new TextField("Nombre del Torneo");
        nombreField.setRequired(true);
        camposLayout.add(nombreField);

        // ComboBox Tipo de Torneo
        tipoComboBox = new ComboBox<>("Tipo de Torneo");
        tipoComboBox.setItems(TipoTorneo.values());
        tipoComboBox.setItemLabelGenerator(this::getTipoTorneoLabel);
        tipoComboBox.setPlaceholder("Selecciona el tipo de torneo");
        tipoComboBox.setRequired(true);
        camposLayout.add(tipoComboBox);

        // NumberField Número de Enfrentamientos por Torneo
        numeroEnfrentamientosField = new NumberField("Número de Enfrentamientos por Torneo");
        numeroEnfrentamientosField.setMin(1);
        numeroEnfrentamientosField.setValue(1.0);
        numeroEnfrentamientosField.setRequired(true);
        camposLayout.add(numeroEnfrentamientosField);

        // NumberField Número de Juegos por Enfrentamiento
        juegosPorEnfrentamientoField = new NumberField("Número de Juegos por Enfrentamiento");
        juegosPorEnfrentamientoField.setMin(1);
        juegosPorEnfrentamientoField.setValue(1.0);
        juegosPorEnfrentamientoField.setRequired(true);
        camposLayout.add(juegosPorEnfrentamientoField);

        formLayout.add(camposLayout);

        // Panel derecho con el Grid de participantes
        VerticalLayout participantesLayout = new VerticalLayout();
        participantesLayout.setWidth("60%");
        participantesLayout.setSpacing(true);

        participantesLayout.add(new Span("Selecciona los participantes:"));

        participantesGrid = new Grid<>(User.class);
        participantesGrid.setColumns("username", "name", "apellido");
        participantesGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        participantesGrid.setWidthFull();
        participantesGrid.setHeight("300px");

        try {
            allParticipantes = userService.findByRole(Role.PLAYER);
            participantesGrid.setItems(allParticipantes);
        } catch (Exception e) {
            // Manejo de excepciones al obtener participantes
            System.err.println("Error al obtener participantes: " + e.getMessage());
            e.printStackTrace();
            participantesGrid.setItems(new ArrayList<>()); // Lista vacía en caso de error
        }

        participantesLayout.add(participantesGrid);
        formLayout.add(participantesLayout);

        dialogLayout.add(formLayout);

        // Botones Guardar y Cancelar
        HorizontalLayout botonesLayout = new HorizontalLayout();
        botonesLayout.setWidthFull();
        botonesLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        botonesLayout.setSpacing(true);

        Button guardarBtn = new Button("Guardar", e -> guardarNuevoTorneo());
        Button cancelarBtn = new Button("Cancelar", e -> nuevoTorneoDialog.close());
        botonesLayout.add(guardarBtn, cancelarBtn);

        dialogLayout.add(botonesLayout);
        nuevoTorneoDialog.add(dialogLayout);
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
            numeroEnfrentamientosField.isEmpty() || juegosPorEnfrentamientoField.isEmpty()) {
            Notification.show("Por favor, completa todos los campos requeridos.", 3000, Notification.Position.MIDDLE);
            return;
        }

        // Crear o actualizar Torneo
        Torneo nuevoTorneo = nuevoTorneoDialog.getElement().getProperty("isEdit", false) ? obtenerTorneoEditado() : new Torneo();
        nuevoTorneo.setNombre(nombreField.getValue());
        nuevoTorneo.setTipo(tipoComboBox.getValue());
        nuevoTorneo.setNumeroEnfrentamientos(numeroEnfrentamientosField.getValue().intValue());
        nuevoTorneo.setJuegosPorEnfrentamiento(juegosPorEnfrentamientoField.getValue().intValue());

        // Obtener participantes seleccionados
        List<User> seleccionados = participantesGrid.getSelectedItems().stream().toList();
        nuevoTorneo.setJugadores(seleccionados);

        // Guardar Torneo
        try {
            torneoService.save(nuevoTorneo);
            nuevoTorneoDialog.close();
            actualizarListaTorneos();
            Notification.show("Torneo guardado exitosamente.", 3000, Notification.Position.MIDDLE);
        } catch (Exception e) {
            // Manejo de excepciones al guardar torneo
            System.err.println("Error al guardar torneo: " + e.getMessage());
            e.printStackTrace();
            Notification.show("Error al guardar el torneo. Por favor, intenta nuevamente.", 5000, Notification.Position.MIDDLE);
        }
    }

    private Torneo obtenerTorneoEditado() {
        // Implementa la lógica para obtener el torneo actualmente editado si es necesario
        // Esto puede requerir mantener una referencia al torneo en edición
        return new Torneo();
    }

    private void actualizarListaTorneos() {
        try {
            torneoGrid.setItems(torneoService.findAll());
        } catch (Exception e) {
            // Manejo de excepciones al actualizar lista
            System.err.println("Error al actualizar lista de torneos: " + e.getMessage());
            e.printStackTrace();
            torneoGrid.setItems(new ArrayList<>()); // Lista vacía en caso de error
        }
    }

    private void borrarTorneos() {
        List<Torneo> seleccionados = torneoGrid.getSelectedItems().stream().toList();
        if (seleccionados.isEmpty()) {
            // Mostrar mensaje de que no se seleccionó ningún torneo
            Notification.show("No se ha seleccionado ningún torneo para borrar.", 3000, Notification.Position.MIDDLE);
            return;
        }

        try {
            seleccionados.forEach(t -> torneoService.delete(t.getId()));
            actualizarListaTorneos();
            Notification.show("Torneos borrados exitosamente.", 3000, Notification.Position.MIDDLE);
        } catch (Exception e) {
            // Manejo de excepciones al borrar torneos
            System.err.println("Error al borrar torneos: " + e.getMessage());
            e.printStackTrace();
            Notification.show("Error al borrar los torneos. Por favor, intenta nuevamente.", 5000, Notification.Position.MIDDLE);
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

    private void configurarTabs() {
        Tabs tabs = new Tabs();
        Tab ganadorTab = new Tab("Ganador");
        Tab clasificacionTab = new Tab("Clasificación");
        Tab enfrentamientosTab = new Tab("Enfrentamientos");

        tabs.add(ganadorTab, clasificacionTab, enfrentamientosTab);
        add(tabs);

        VerticalLayout ganadorLayout = new VerticalLayout();
        ganadorLayout.setWidthFull();
        ganadorLayout.add(new Span("Configuración de Ganador"));

        VerticalLayout clasificacionLayout = new VerticalLayout();
        clasificacionLayout.setWidthFull();
        clasificacionLayout.add(new Span("Configuración de Clasificación"));

        VerticalLayout enfrentamientosLayout = new VerticalLayout();
        enfrentamientosLayout.setWidthFull();
        enfrentamientosLayout.add(new Span("Configuración de Enfrentamientos"));

        tabs.addSelectedChangeListener(event -> {
            // Remover todos los componentes excepto las tabs
            removeAll();
            add(tabs);
            // Agregar el layout correspondiente al tab seleccionado
            if (event.getSelectedTab().equals(ganadorTab)) {
                add(ganadorLayout);
            } else if (event.getSelectedTab().equals(clasificacionTab)) {
                add(clasificacionLayout);
            } else if (event.getSelectedTab().equals(enfrentamientosTab)) {
                add(enfrentamientosLayout);
            }
        });

        // Inicialmente mostrar el primer tab
        add(ganadorLayout);
    }

    private void editarTorneo(Torneo torneo) {
        // Obtener el torneo con la colección 'jugadores' inicializada
        Torneo torneoConJugadores = torneoService.findByIdWithJugadores(torneo.getId());
        if (torneoConJugadores == null) {
            Notification.show("Torneo no encontrado.", 3000, Notification.Position.MIDDLE);
            return;
        }

        nombreField.setValue(torneoConJugadores.getNombre());
        tipoComboBox.setValue(torneoConJugadores.getTipo());
        numeroEnfrentamientosField.setValue((double) torneoConJugadores.getNumeroEnfrentamientos());
        juegosPorEnfrentamientoField.setValue((double) torneoConJugadores.getJuegosPorEnfrentamiento());
        participantesGrid.deselectAll();
        torneoConJugadores.getJugadores().forEach(participantesGrid::select);
        nuevoTorneoDialog.open();
        // Marcar el diálogo como edición
        nuevoTorneoDialog.getElement().setProperty("isEdit", true);
    }
}