package org.aksw.facete.v4.impl;

import org.aksw.jenax.sparql.relation.api.Relation;
import org.apache.jena.graph.Node;

public interface PropertyResolver {
    Relation resolve(Node property);
}
