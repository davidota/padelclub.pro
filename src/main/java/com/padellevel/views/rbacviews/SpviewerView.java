package com.padellevel.views.rbacviews;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Spviewer")
@Route("spviewer")
@Menu(order = 2, icon = LineAwesomeIconUrl.PENCIL_RULER_SOLID)
@RolesAllowed("USER")
public class SpviewerView extends Composite<VerticalLayout> {

    public SpviewerView() {
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
    }
}
