package org.aksw.jenax.treequery2;

import org.apache.jena.graph.Node;

public interface OrderNode
    extends QueryNode
{
    // OrderNode fwd(Node property);
    // OrderNode bwd(Node property);

    QueryNode asc();
    QueryNode desc();
}
