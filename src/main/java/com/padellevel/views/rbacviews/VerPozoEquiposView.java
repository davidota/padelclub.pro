package com.padellevel.views.rbacviews;

import com.padellevel.views.MainLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;
import java.util.List;
import java.util.Arrays;
import com.vaadin.flow.component.html.H1;

@PageTitle("Equipos en el Pozo")
@Route(value = "verPozoEquipos", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class VerPozoEquiposView extends VerticalLayout implements BeforeEnterObserver {

    public VerPozoEquiposView() {
        setSpacing(true);
        setWidthFull();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Display title using pozoNombre parameter if provided
        String pozoNom = event.getLocation().getQueryParameters().getParameters()
                .getOrDefault("pozoNombre", List.of()).stream().findFirst().orElse("Pozo");
        removeAll();
        add(new H1("Pozo: " + pozoNom));
        // Then process and list matches if provided
        String matchesParam = event.getLocation().getQueryParameters().getParameters()
                .getOrDefault("matches", List.of()).stream().findFirst().orElse("");
        if (!matchesParam.isEmpty()) {
            List<String> matches = Arrays.asList(matchesParam.split(";"));
            matches.forEach(match -> add(new Span(match)));
        } else {
            add(new Span("No se han generado enfrentamientos para este pozo."));
        }
    }
}
