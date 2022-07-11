package org.aksw.vaadin.app.demo.view.edit.resource;

import org.aksw.jenax.path.core.PathPP;

import com.vaadin.flow.component.ComponentEvent;

public class BreadcrumbEvent
    extends ComponentEvent<Breadcrumb>
{
    protected PathPP path;

    public BreadcrumbEvent(Breadcrumb source, boolean fromClient, PathPP path) {
        super(source, fromClient);
        this.path = path;
    }

    public PathPP getPath() {
        return path;
    }
}