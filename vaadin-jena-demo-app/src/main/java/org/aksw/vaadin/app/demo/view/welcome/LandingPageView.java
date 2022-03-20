package org.aksw.vaadin.app.demo.view.welcome;

import org.aksw.vaadin.app.demo.MainLayout;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "home", layout = MainLayout.class)
@PageTitle("Demo")
public class LandingPageView
    extends VerticalLayout
{
    public LandingPageView() {
        add(new H1("Welcome"));
    }
}
