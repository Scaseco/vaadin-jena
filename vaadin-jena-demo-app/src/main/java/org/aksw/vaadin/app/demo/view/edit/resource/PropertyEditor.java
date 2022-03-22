package org.aksw.vaadin.app.demo.view.edit.resource;

import org.aksw.commons.rx.lookup.ListService;
import org.aksw.jenax.sparql.relation.api.UnaryRelation;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.path.Path;

public class PropertyEditor {
    protected Node source;
    protected Path path;

    protected ListService<UnaryRelation, Node> listService;
}
