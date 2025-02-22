package com.padellevel.views.rbacviews;

import com.padellevel.data.Enfrentamiento;
import com.padellevel.data.Equipo;
import com.padellevel.data.Pozo;
import com.padellevel.data.Torneo;
import com.padellevel.data.User;
import com.padellevel.services.TorneoService;
import com.padellevel.services.PozoService;
import com.padellevel.services.EquipoService;
import com.padellevel.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Route(value = "generarPozo", layout = MainLayout.class)
@PageTitle("Generar Pozo")
@RolesAllowed("ADMIN")
public class GenerarPozoView extends VerticalLayout implements BeforeEnterObserver {

    private final TorneoService torneoService;
    private final PozoService pozoService;
    private final EquipoService equipoService;
    
    private Grid<User> playersGrid = new Grid<>(User.class);
    private IntegerField pistasField = new IntegerField("Número de Pistas");
    private Button generarBtn = new Button("Generar Enfrentamientos");
    private VerticalLayout resultadosLayout = new VerticalLayout();
    private H1 title;

    private List<String> matchUps;
    private boolean generated = false;
    private Long currentPozoId; // new field to hold pozoId

    public GenerarPozoView(TorneoService torneoService, PozoService pozoService, EquipoService equipoService) {
        this.torneoService = torneoService;
        this.pozoService = pozoService;
        this.equipoService = equipoService;
        setSpacing(true);
        setWidthFull();
        title = new H1("Pozo: ");
        add(title);
        playersGrid.setColumns("name", "apellido");
        playersGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        pistasField.setMin(1);
        pistasField.setValue(1);
        add(new Span("Selecciona los jugadores para formar equipos en el pozo:"));
        add(playersGrid);
        add(pistasField, generarBtn, resultadosLayout);
        generarBtn.addClickListener(e -> generarEnfrentamientos());
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        event.getLocation().getQueryParameters().getParameters()
             .getOrDefault("pozoNombre", List.of())
             .stream().findFirst().ifPresent(pozoNom -> {
                 title.setText("Pozo: " + pozoNom);
             });
        event.getLocation().getQueryParameters().getParameters()
                .getOrDefault("torneoId", List.of())
                .stream().findFirst().ifPresent(idStr -> {
            try {
                Long torneoId = Long.parseLong(idStr);
                Torneo currentTorneo = torneoService.findById(torneoId);
                if (currentTorneo != null) {
                    playersGrid.setItems(currentTorneo.getJugadores());
                } else {
                    Notification.show("Torneo no encontrado.", 3000, Notification.Position.MIDDLE);
                }
            } catch (NumberFormatException ex) {
                Notification.show("Parámetro de torneo inválido.", 3000, Notification.Position.MIDDLE);
            }
        });
        // New: Retrieve and store pozoId
        event.getLocation().getQueryParameters().getParameters()
             .getOrDefault("pozoId", List.of())
             .stream().findFirst().ifPresent(idStr -> {
                 try {
                     currentPozoId = Long.parseLong(idStr);
                 } catch (NumberFormatException ex) {
                     Notification.show("PozoId inválido.", 3000, Notification.Position.MIDDLE);
                 }
             });
    }

    private void generarEnfrentamientos() {
        if (generated) {
            Notification.show("Los enfrentamientos ya se generaron.", 3000, Notification.Position.MIDDLE);
            return;
        }
        if (currentPozoId == null) {
            Notification.show("Falta el parámetro pozoId.", 3000, Notification.Position.MIDDLE);
            return;
        }
        // Retrieve equipos associated to the pozo
        List<Equipo> equipos = equipoService.findEquiposByPozo(currentPozoId);
        // If there are not enough equipos, try generating them from selected players
        if (equipos.size() < 2) {
            // Retrieve selected players from the grid
            var selectedPlayers = playersGrid.getSelectedItems();
            if (selectedPlayers.size() < 2) {
                Notification.show("Selecciona al menos 2 jugadores para formar equipos.", 3000, Notification.Position.MIDDLE);
                return;
            }
            // Create equipos by pairing players (teams of 2)
            List<User> playersList = new ArrayList<>(selectedPlayers);
            Collections.shuffle(playersList);
            List<Equipo> newEquipos = new ArrayList<>();
            // Get the pozo from database using currentPozoId; using orElse(null) to extract Pozo from Optional
            Pozo existingPozo = pozoService.findById(currentPozoId).orElse(null);
            int teamCount = playersList.size() / 2;
            for (int i = 0; i < teamCount; i++) {
                Equipo equipo = new Equipo();
                equipo.setNombreEquipo("Equipo " + (i + 1));
                // Set players using defined setters
                equipo.setParticipante1(playersList.get(2 * i));
                equipo.setParticipante2(playersList.get(2 * i + 1));
                equipo.setPozo(existingPozo);
                equipoService.save(equipo);
                newEquipos.add(equipo);
            }
            equipos = newEquipos;
            if (equipos.size() < 2) {
                Notification.show("No hay equipos suficientes para generar enfrentamientos.", 3000, Notification.Position.MIDDLE);
                return;
            }
        }
        
        // Proceed with enfrentamientos generation
        Collections.shuffle(equipos);
        int totalEnfrentamientosPosibles = equipos.size() / 2;
        int numPistas = (pistasField.getValue() != null) ? pistasField.getValue() : 1;
        int enfrentamientosARendir = Math.min(totalEnfrentamientosPosibles, numPistas);

        Pozo pozo = new Pozo();
        // Initialize list of enfrentamientos to avoid NullPointerException
        pozo.setEnfrentamientos(new java.util.ArrayList<>());
        
        String currentPozoName = title.getText().replace("Pozo: ", "").trim();
        pozo.setNombre(currentPozoName.isEmpty() ? "Nuevo Pozo" : currentPozoName);
        pozo.setTipoEnfrentamiento("Enfrentamientos Generados");

        // Optional: ensure we have an even number of equipos
        if (equipos.size() % 2 != 0) {
            equipos.remove(equipos.size() - 1);
        }
        
        for (int i = 0; i < enfrentamientosARendir; i++) {
            Equipo equipo1 = equipos.get(2 * i);
            Equipo equipo2 = equipos.get(2 * i + 1);
            Enfrentamiento enf = new Enfrentamiento();
            enf.setEquipo1(equipo1);
            enf.setEquipo2(equipo2);
            enf.setResultado("Pista " + (i + 1) + ": " + equipo1.getNombreEquipo() + " vs " + equipo2.getNombreEquipo());
            enf.setPozo(pozo);
            pozo.getEnfrentamientos().add(enf);
        }
        // Retrieve torneoId from current UI location (existing logic)
        String torneoIdStr = null;
        com.vaadin.flow.router.Location loc = getUI().map(ui -> ui.getInternals().getActiveViewLocation()).orElse(null);
        if (loc != null) {
            torneoIdStr = loc.getQueryParameters().getParameters()
                        .getOrDefault("torneoId", List.of()).stream().findFirst().orElse(null);
        }
        if (torneoIdStr != null) {
            try {
                Long torneoId = Long.parseLong(torneoIdStr);
                // Set the tournament for the pozo
                pozo.setTorneo(torneoService.findById(torneoId));
            } catch (NumberFormatException ex) {
                Notification.show("Torneo ID inválido.", 3000, Notification.Position.MIDDLE);
                return;
            }
        } else {
            Notification.show("Falta el parámetro torneoId.", 3000, Notification.Position.MIDDLE);
            return;
        }
        // Update each enfrentamiento to have the same torneo as the pozo
        for (Enfrentamiento enf : pozo.getEnfrentamientos()) {
            enf.setTorneo(pozo.getTorneo());
        }
        Pozo persistedPozo = pozoService.save(pozo);
        generated = true;
        generarBtn.setEnabled(false);
        getUI().ifPresent(ui -> ui.navigate("verPozoEquipos",
                com.vaadin.flow.router.QueryParameters.simple(
                        java.util.Map.of("pozoNombre", persistedPozo.getNombre())
                )));
    }
}
