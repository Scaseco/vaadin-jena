package org.aksw.jenax.treequery2;

import org.apache.jena.sparql.core.Var;

public interface RelationQuery {

    QueryNode nodeFor(Var var);
}
