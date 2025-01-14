package com.padellevel.views.rbacviews;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.avatar.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.component.grid.Grid;
import java.util.Map;
import com.vaadin.flow.component.textfield.TextField;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Padel Level")
@Route("")
@Menu(order = 0, icon = LineAwesomeIconUrl.PENCIL_RULER_SOLID)
@AnonymousAllowed
public class HomeView extends Composite<VerticalLayout> {

    public HomeView() {
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        List<Map.Entry<String,String>> allVars = new ArrayList<>(System.getenv().entrySet());
        Grid<Map.Entry<String, String>> envGrid = new Grid<>();
        envGrid.setItems(allVars);
        envGrid.addColumn(Map.Entry::getKey).setHeader("Key");
        envGrid.addColumn(Map.Entry::getValue).setHeader("Value");

        TextField filterField = new TextField("Filter environment variables");
        filterField.addValueChangeListener(e -> {
            String filter = e.getValue().toLowerCase();
            envGrid.setItems(allVars.stream()
                    .filter(entry -> entry.getKey().toLowerCase().contains(filter)
                        || entry.getValue().toLowerCase().contains(filter))
                    .collect(Collectors.toList()));
        });

        getContent().add(filterField, envGrid);
    }
}
