package org.aksw.jenax.treequery2;

import org.aksw.jenax.treequery2.old.NodeQueryOld;
import org.apache.jena.graph.Node;

public interface OrderNode
    extends NodeQueryOld
{
    // OrderNode fwd(Node property);
    // OrderNode bwd(Node property);

    NodeQueryOld asc();
    NodeQueryOld desc();
}
