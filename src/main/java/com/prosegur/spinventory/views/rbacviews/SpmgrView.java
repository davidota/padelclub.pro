package com.prosegur.spinventory.views.rbacviews;

import com.prosegur.spinventory.data.ServicePrincipal;
import com.prosegur.spinventory.services.ServicePrincipalService;
import com.prosegur.spinventory.views.MainLayout;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.vaadin.lineawesome.LineAwesomeIconUrl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;

import java.util.List;

@PageTitle("Spmgr")
@Route(value = "spmgr", layout = MainLayout.class)
@Menu(order = 1, icon = LineAwesomeIconUrl.PENCIL_RULER_SOLID)
@RolesAllowed("ADMIN")
public class SpmgrView extends Composite<VerticalLayout> {

    private final Grid<ServicePrincipal> grid = new Grid<>(ServicePrincipal.class);
    private final ListDataProvider<ServicePrincipal> dataProvider;
    private final TextField searchField = new TextField();
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Declare form fields as instance variables
    private final TextField appId = new TextField("App ID");
    private final PasswordField secret = new PasswordField("Secret");
    private final TextField tenantId = new TextField("Tenant ID");
    private final DateTimePicker expirationDate = new DateTimePicker("Expiration Date");
    private final TextField comments = new TextField("Comments");
    private final Select<ServicePrincipal.Entorno> entorno = new Select<>();
    private final TextField uso = new TextField("Uso");

    private final ServicePrincipalService servicePrincipalService;

    @Autowired
    public SpmgrView(ServicePrincipalService servicePrincipalService) {
        this.servicePrincipalService = servicePrincipalService;

        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");

        // Initialize grid
        grid.setColumns("appId", "tenantId", "expirationDate", "comments", "entorno", "uso");
        grid.addColumn(servicePrincipal -> "********").setHeader("Secret");
        grid.addItemDoubleClickListener(event -> openEditDialog(event.getItem()));

        // Initialize search field
        searchField.setPlaceholder("Buscar...");
        searchField.addValueChangeListener(e -> updateGrid());

        Button searchButton = new Button("Buscar", e -> updateGrid());
        Button addButton = new Button("Agregar", e -> openEditDialog(new ServicePrincipal()));

        HorizontalLayout searchLayout = new HorizontalLayout(searchField, searchButton, addButton);

        // Add components to layout
        getContent().add(searchLayout, grid);

        // Initialize data provider
        List<ServicePrincipal> servicePrincipals = servicePrincipalService.findAll();
        dataProvider = new ListDataProvider<>(servicePrincipals);
        grid.setDataProvider(dataProvider);

        // Initialize entorno select
        entorno.setLabel("Entorno");
        entorno.setItems(ServicePrincipal.Entorno.values());
    }

    private void updateGrid() {
        String searchTerm = searchField.getValue().trim();
        dataProvider.setFilter(servicePrincipal -> 
            servicePrincipal.getAppId().contains(searchTerm) ||
            servicePrincipal.getTenantId().contains(searchTerm) ||
            servicePrincipal.getComments().contains(searchTerm) ||
            servicePrincipal.getEntorno().name().contains(searchTerm)
        );
    }

    private void openEditDialog(ServicePrincipal servicePrincipal) {
        Dialog dialog = new Dialog();
        FormLayout formLayout = new FormLayout();

        Binder<ServicePrincipal> binder = new Binder<>(ServicePrincipal.class);
        binder.bindInstanceFields(this);

        binder.forField(appId).asRequired("App ID is required").bind(ServicePrincipal::getAppId, ServicePrincipal::setAppId);
        binder.forField(secret).asRequired("Secret is required").bind(ServicePrincipal::getSecret, ServicePrincipal::setSecret);
        binder.forField(tenantId).asRequired("Tenant ID is required").bind(ServicePrincipal::getTenantId, ServicePrincipal::setTenantId);
        binder.forField(expirationDate)
            .asRequired("Expiration Date is required")
            .withConverter(
                localDateTime -> localDateTime == null ? null : localDateTime.toLocalDate().atStartOfDay(),
                dateTime -> dateTime
            )
            .bind(ServicePrincipal::getExpirationDate, ServicePrincipal::setExpirationDate);
        binder.forField(comments).bind(ServicePrincipal::getComments, ServicePrincipal::setComments);
        binder.forField(entorno).asRequired("Entorno is required").bind(ServicePrincipal::getEntorno, ServicePrincipal::setEntorno);
        binder.forField(uso)
            .asRequired("Uso is required")
            .bind(ServicePrincipal::getUso, ServicePrincipal::setUso);

        binder.readBean(servicePrincipal);

        Button saveButton = new Button("Guardar", e -> {
            try {
                binder.writeBean(servicePrincipal);
                servicePrincipal.setSecret(passwordEncoder.encode(servicePrincipal.getSecret()));
                servicePrincipalService.save(servicePrincipal);
                if (!dataProvider.getItems().contains(servicePrincipal)) {
                    dataProvider.getItems().add(servicePrincipal);
                }
                dataProvider.refreshAll();
                dialog.close();
            } catch (ValidationException ex) {
                Notification notification = new Notification("Error de validación: " + ex.getMessage(), 3000, Position.MIDDLE);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                notification.open();
            }
        });

        Button cancelButton = new Button("Cancelar", e -> dialog.close());

        formLayout.add(appId, secret, tenantId, expirationDate, comments, entorno, uso);
        dialog.add(formLayout, new HorizontalLayout(saveButton, cancelButton));
        dialog.open();
    }
}