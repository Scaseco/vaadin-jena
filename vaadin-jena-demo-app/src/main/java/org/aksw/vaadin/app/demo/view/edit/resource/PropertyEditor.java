package org.aksw.vaadin.app.demo.view.edit.resource;

import org.aksw.commons.rx.lookup.ListService;
import org.aksw.jenax.sparql.fragment.api.Fragment1;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.path.Path;

public class PropertyEditor {
    protected Node source;
    protected Path path;

    protected ListService<Fragment1, Node> listService;
}
