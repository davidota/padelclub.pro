package com.padellevel.views.rbacviews;

import com.padellevel.data.Torneo;
import com.padellevel.data.User;
import com.padellevel.services.TorneoService;
import com.padellevel.views.MainLayout;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.RolesAllowed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.vaadin.lineawesome.LineAwesomeIconUrl;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.html.Div;
import com.padellevel.data.Enfrentamiento;
import com.padellevel.data.Equipo;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.data.converter.StringToDoubleConverter;

import org.springframework.transaction.annotation.Transactional;
import com.padellevel.services.EnfrentamientoService;
import org.springframework.beans.factory.annotation.Autowired;

@PageTitle("Padel Level")
@Route(value = "torneo", layout = MainLayout.class)
@Menu(order = 1, icon = LineAwesomeIconUrl.PENCIL_RULER_SOLID)
@RolesAllowed("ADMIN")
public class TorneoView extends VerticalLayout {

    private final TorneoService torneoService;
    private final EnfrentamientoService enfrentamientoService; // Added
    private Tab configuracionTab, enfrentamientosTab, clasificacionTab, ganadorTab;
    private Div configuracionContent, enfrentamientosContent, clasificacionContent, ganadorContent;

    // ComboBox y campos del Torneo
    private ComboBox<Torneo> torneoCombo;
    private TextField nombreTorneoField;
    private IntegerField numeroEnfrentamientosField; // Changed from NumberField
    private IntegerField juegosPorEnfrentamientoField; // Changed from NumberField
    private TextField numeroEnfrentamientosSimultaneosField; // Reference to the new field
    private IntegerField numeroDeVueltasField; // Changed from NumberField
    private Binder<Torneo> binder = new Binder<>(Torneo.class); // Added Binder
    private Torneo torneoEditado; // Declared torneoEditado


    @Autowired
    public TorneoView(TorneoService torneoService, EnfrentamientoService enfrentamientoService) { // Modified
        this.torneoService = torneoService;
        this.enfrentamientoService = enfrentamientoService; // Added
        crearTabs();
        prepararConfiguracionTab();
    }

    private void crearTabs() {
        configuracionTab = new Tab("Configuración");
        enfrentamientosTab = new Tab("Enfrentamientos");
        clasificacionTab = new Tab("Clasificación");
        ganadorTab = new Tab("Ganador");

        configuracionContent = new Div();
        enfrentamientosContent = new Div();
        clasificacionContent = new Div();
        ganadorContent = new Div();

        Tabs tabs = new Tabs(configuracionTab, enfrentamientosTab, clasificacionTab, ganadorTab);
        Div pages = new Div(configuracionContent, enfrentamientosContent, clasificacionContent, ganadorContent);

        add(tabs, pages);

        tabs.addSelectedChangeListener(event -> {
            Tab selected = event.getSelectedTab();
            configuracionContent.setVisible(selected == configuracionTab);
            enfrentamientosContent.setVisible(selected == enfrentamientosTab);
            clasificacionContent.setVisible(selected == clasificacionTab);
            ganadorContent.setVisible(selected == ganadorTab);
        });

        configuracionContent.setVisible(true);
        enfrentamientosContent.setVisible(false);
        clasificacionContent.setVisible(false);
        ganadorContent.setVisible(false);
    }

    private void prepararConfiguracionTab() {
        // Layout principal centrado
        VerticalLayout layout = new VerticalLayout();
        layout.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);

        // ComboBox para elegir Torneo
        torneoCombo = new ComboBox<>("Seleccionar Torneo");
        torneoCombo.setItems(torneoService.findAll());
        torneoCombo.setItemLabelGenerator(Torneo::getNombre);
        torneoCombo.addValueChangeListener(e -> {
            Torneo seleccionado = e.getValue();
            if (seleccionado != null) {
                binder.readBean(seleccionado);
            } else {
                binder.readBean(null);
            }
        });

        // Campos para editar el Torneo
        nombreTorneoField = new TextField("Nombre del Torneo");
        numeroEnfrentamientosField = new IntegerField("Número de Enfrentamientos");
        numeroEnfrentamientosField.setMin(1);
        juegosPorEnfrentamientoField = new IntegerField("Juegos por Enfrentamiento");
        juegosPorEnfrentamientoField.setMin(1);
        numeroDeVueltasField = new IntegerField("Número de Vueltas"); // Changed to IntegerField
        numeroDeVueltasField.setMin(1);

        // Bind UI fields to Torneo properties
        binder.bind(nombreTorneoField, Torneo::getNombre, Torneo::setNombre);
        binder.bind(numeroEnfrentamientosField, Torneo::getNumeroEnfrentamientos, Torneo::setNumeroEnfrentamientos);
        binder.bind(juegosPorEnfrentamientoField, Torneo::getJuegosPorEnfrentamiento, Torneo::setJuegosPorEnfrentamiento);
        binder.bind(numeroDeVueltasField, Torneo::getNumeroDeVueltas, Torneo::setNumeroDeVueltas);

        // Botones Guardar y Generar
        Button guardarBtn = new Button("Guardar", ev -> {
            try {
                Torneo torneo = torneoCombo.getValue();
                if (torneo == null) { // Added null check
                    Notification.show("Selecciona un torneo para guardar.", 3000, Notification.Position.MIDDLE)
                               .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }
                binder.writeBean(torneo); // Removed writeBeanIfValid
                torneoService.actualizarTorneo(torneo); // Modified to use service
                Notification.show("Torneo guardado exitosamente.", 3000, Notification.Position.MIDDLE);
                torneoCombo.setItems(torneoService.findAll()); // Refresh ComboBox items
            } catch (ValidationException ex) {
                Notification.show("Por favor, completa los campos requeridos.", 3000, Notification.Position.MIDDLE)
                           .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        Button generarBtn = new Button("Generar", ev -> {
            Torneo selectedTorneo = torneoCombo.getValue();
            if (selectedTorneo == null) {
                Notification.show("No se seleccionó ningún torneo", 3000, Notification.Position.MIDDLE);
                return;
            }

            try {
                List<Enfrentamiento> enfrentamientos = torneoService.generarEnfrentamientos(selectedTorneo);
                Notification.show("Torneo generado exitosamente.", 3000, Notification.Position.MIDDLE)
                           .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                // Seleccionar el tab de Enfrentamientos
                Tabs tabs = getTabs();
                Tab enfTab = enfrentamientosTab;
                tabs.setSelectedTab(enfTab);

                // Mostrar los enfrentamientos en el contenido correspondiente
                mostrarEnfrentamientos(enfrentamientos, selectedTorneo.getJuegosPorEnfrentamiento());
            } catch (IllegalArgumentException e) {
                Notification.show(e.getMessage(), 3000, Notification.Position.MIDDLE)
                           .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        // Disponer elementos en layouts
        HorizontalLayout fieldsLayout = new HorizontalLayout(
            nombreTorneoField,
            numeroEnfrentamientosField,
            juegosPorEnfrentamientoField,
            numeroDeVueltasField // Added
        );
        fieldsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        HorizontalLayout buttonsLayout = new HorizontalLayout(guardarBtn, generarBtn);
        buttonsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        layout.add(torneoCombo, fieldsLayout, buttonsLayout);
        configuracionContent.add(layout);
    }

    private Tabs getTabs() {
        // Implementa este método para retornar el objeto Tabs si no está accesible directamente
        // Puede implicar almacenar una referencia de Tabs como campo de clase
        return this.getChildren()
                   .filter(component -> component instanceof Tabs)
                   .map(component -> (Tabs) component)
                   .findFirst()
                   .orElseThrow(() -> new IllegalStateException("Tabs not found"));
    }

    private void mostrarEnfrentamientos(List<Enfrentamiento> enfrentamientos, int juegosPorEnfrentamiento) {
        enfrentamientosContent.removeAll(); // Clear existing content
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setWidthFull();
        mainLayout.setSpacing(true);

        // Crear layout para los enfrentamientos
        for (Enfrentamiento e : enfrentamientos) {
            HorizontalLayout enfrentamientoLayout = new HorizontalLayout();
            enfrentamientoLayout.setWidthFull();
            enfrentamientoLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

            VerticalLayout equipoIzqLayout = new VerticalLayout();
            equipoIzqLayout.setAlignItems(Alignment.CENTER);
            equipoIzqLayout.add(new Span("Equipo Izq: " + e.getEquipo1().getParticipante1().getUsername() + " & " +
                                         e.getEquipo1().getParticipante2().getUsername()));

            VerticalLayout juegosLayout = new VerticalLayout();
            for (int k = 0; k < juegosPorEnfrentamiento; k++) {
                ComboBox<String> combo = new ComboBox<>();
                combo.setItems("Empate", "Gana Izq", "Gana Der");
                combo.setPlaceholder("Juego " + (k + 1));
                juegosLayout.add(combo);
            }

            VerticalLayout equipoDerLayout = new VerticalLayout();
            equipoDerLayout.setAlignItems(Alignment.CENTER);
            equipoDerLayout.add(new Span("Equipo Der: " + e.getEquipo2().getParticipante1().getUsername() + " & " +
                                         e.getEquipo2().getParticipante2().getUsername()));

            enfrentamientoLayout.add(equipoIzqLayout, juegosLayout, equipoDerLayout);
            mainLayout.add(enfrentamientoLayout);
        }

        enfrentamientosContent.add(mainLayout);
    }

    private void mostrarEnfrentamientosAgrupados(List<Enfrentamiento> enfrentamientos, int juegosPorEnfrentamiento) {
        enfrentamientosContent.removeAll(); // Clear existing content
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setWidthFull();
        mainLayout.setSpacing(true);

        // Obtener el número de enfrentamientos simultáneos del torneo editado
        int enfrentamientosSimultaneos = torneoEditado.getNumeroEnfrentamientosSimultaneos(); // Corrected

        // Agrupar enfrentamientos según el número de enfrentamientos simultáneos
        for (int i = 0; i < enfrentamientos.size(); i += enfrentamientosSimultaneos) {
            HorizontalLayout grupoLayout = new HorizontalLayout();
            grupoLayout.setWidthFull();
            grupoLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
            grupoLayout.setSpacing(true);

            for (int j = i; j < i + enfrentamientosSimultaneos && j < enfrentamientos.size(); j++) {
                Enfrentamiento e = enfrentamientos.get(j);

                VerticalLayout enfrentamientoLayout = new VerticalLayout();
                enfrentamientoLayout.setAlignItems(Alignment.CENTER);
                enfrentamientoLayout.add(new Span("Equipo Izq: " + e.getEquipo1().getNombreEquipo()));
                
                // ComboBoxes para juegos
                VerticalLayout juegosLayout = new VerticalLayout();
                for (int k = 0; k < juegosPorEnfrentamiento; k++) {
                    ComboBox<String> combo = new ComboBox<>();
                    combo.setItems("Empate", "Gana Izq", "Gana Der");
                    combo.setPlaceholder("Juego " + (k + 1));
                    juegosLayout.add(combo);
                }

                enfrentamientoLayout.add(juegosLayout);
                enfrentamientoLayout.add(new Span("Equipo Der: " + e.getEquipo2().getNombreEquipo()));

                grupoLayout.add(enfrentamientoLayout);
            }

            mainLayout.add(grupoLayout);
        }

        enfrentamientosContent.add(mainLayout);
    }
}
