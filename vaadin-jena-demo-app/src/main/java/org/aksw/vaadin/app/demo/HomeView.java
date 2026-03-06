package org.aksw.vaadin.app.demo;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("")
public class HomeView extends VerticalLayout {
    private static final long serialVersionUID = 1L;

    public HomeView() {
        add(new H2("Welcome to the Home Page"));
    }
}

