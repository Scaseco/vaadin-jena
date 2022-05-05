package org.aksw.vaadin.app.demo.view.edit.resource;

import org.aksw.jenax.vaadin.label.LabelMgr;
import org.apache.jena.graph.Node;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class Breadcrumb
    extends HorizontalLayout
{
    protected LabelMgr<Node, String> labelService;

    public Breadcrumb(LabelMgr<Node, String> labelService) {
        this.labelService = labelService;
        add(new Span("foo"));
        add(new Span("foo"));
    }
}
