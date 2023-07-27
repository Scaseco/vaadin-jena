package org.aksw.jenax.vaadin.component.breadcrumb;

import org.aksw.commons.path.core.Path;

import com.vaadin.flow.component.ComponentEvent;

public class BreadcrumbEvent<T>
    extends ComponentEvent<Breadcrumb<T>>
{
    private static final long serialVersionUID = 1L;

    protected Path<T> path;

    public BreadcrumbEvent(Breadcrumb<T> source, boolean fromClient, Path<T> path) {
        super(source, fromClient);
        this.path = path;
    }

    public Path<T> getPath() {
        return path;
    }
}
