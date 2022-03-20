package org.aksw.vaadin.app.demo.view.edit.resource;

import org.aksw.vaadin.app.demo.MainLayout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "edit", layout = MainLayout.class)
@PageTitle("Resource Editor")
public class ResourceEditorView
    extends VerticalLayout
{
    public ResourceEditorView() {
        Component editor = new ResourceEditor();
        add(editor);
    }
}
