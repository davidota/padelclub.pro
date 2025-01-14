package com.padellevel.views.rbacviews;

import com.padellevel.data.Role;
import com.padellevel.data.User;
import com.padellevel.services.UserService;
import com.padellevel.views.MainLayout;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import com.vaadin.flow.component.checkbox.Checkbox;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("User Manager")
@Route(value = "usermgr", layout = MainLayout.class)
@Menu(order = 3, icon = LineAwesomeIconUrl.PENCIL_RULER_SOLID)
@PermitAll
public class UsermgrView extends Composite<VerticalLayout> {

    private final Grid<User> grid = new Grid<>(User.class);
    private final TextField searchField = new TextField();
    private final UserService userService;
    private ListDataProvider<User> dataProvider;
    
    @Autowired
    public UsermgrView(UserService userService) {
        this.userService = userService;
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");

        // Initialize data provider once
        dataProvider = new ListDataProvider<>(userService.findAll());
        grid.setDataProvider(dataProvider);

        grid.removeAllColumns();
        grid.addColumn(User::getUsername).setHeader("Username");
        grid.addColumn(User::getName).setHeader("Nombre");
        grid.addColumn(User::getApellido).setHeader("Apellido");
        // ...agregar más columnas si se desea...
        grid.addItemDoubleClickListener(e -> openEditDialog(e.getItem()));
    
        grid.setSelectionMode(SelectionMode.MULTI);
        // ...handle selection if needed...
    
        // Initialize buttons with listeners
        Button searchButton = new Button("Buscar", e -> updateGrid());
        Button addButton = new Button("Agregar", e -> openEditDialog(new User()));
        Button deleteButton = new Button("Eliminar", e -> deleteSelectedUsers());
        searchField.addKeyPressListener(Key.ENTER, e -> updateGrid());
        // Add buttons to the toolbar
        HorizontalLayout toolbar = new HorizontalLayout(searchField, searchButton, addButton, deleteButton);
        getContent().add(toolbar, grid);
    }

    private void updateGrid() {
        String term = searchField.getValue() == null ? "" : searchField.getValue().trim();
        List<User> users = userService.searchUsers(term);
        dataProvider.getItems().clear();
        dataProvider.getItems().addAll(users);
        dataProvider.refreshAll();
    }

    private void openEditDialog(User user) {
        Dialog dialog = new Dialog();
        FormLayout formLayout = new FormLayout();
        Binder<User> binder = new Binder<>(User.class);

        TextField usernameField = new TextField("Username");
        TextField nameField = new TextField("Nombre");
        TextField apellidoField = new TextField("Apellido");
        PasswordField passwordField = new PasswordField("Contraseña");
        ComboBox<Role> roleComboBox = new ComboBox<>("Rol");
        roleComboBox.setItems(Role.values());

        binder.forField(usernameField)
              .asRequired("Campo requerido")
              .bind(User::getUsername, User::setUsername);
        binder.forField(nameField)
              .asRequired("Campo requerido")
              .bind(User::getName, User::setName);
        binder.forField(apellidoField)
              .asRequired("Campo requerido")
              .bind(User::getApellido, User::setApellido);
        binder.forField(passwordField)
              .bind(User::getPassword, User::setPassword); // Bind to transient password field
        binder.forField(roleComboBox)
              .asRequired("Campo requerido")
              .bind(
                  userEntity -> 
                      (userEntity.getRoles() != null && !userEntity.getRoles().isEmpty()) 
                          ? userEntity.getRoles().iterator().next() 
                          : null, 
                  (userEntity, role) -> userEntity.setRoles(Set.of(role))
              );

        binder.readBean(user);
        formLayout.add(usernameField, nameField, apellidoField, passwordField, roleComboBox);

        Button saveButton = new Button("Guardar", e -> {
            try {
                binder.writeBean(user);
                userService.save(user);
                if (!dataProvider.getItems().contains(user)) {
                    dataProvider.getItems().add(user);
                }
                dataProvider.refreshAll();
                dialog.close();
            } catch (ValidationException ex) {
                Notification notification = new Notification("Error: " + ex.getMessage(), 3000);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                notification.open();
            }
        });

        Button cancelButton = new Button("Cancelar", e -> dialog.close());
        dialog.add(formLayout, new HorizontalLayout(saveButton, cancelButton));
        dialog.open();
    }

    private void deleteSelectedUsers() {
        Set<User> selectedUsers = grid.getSelectedItems();
        for (User user : selectedUsers) {
            userService.delete(user);
            dataProvider.getItems().remove(user);
        }
        dataProvider.refreshAll();
    }
}