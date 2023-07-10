package org.aksw.jenax.treequery2.api;

public interface OrderNode
    extends FacetTraversable<OrderNode>
{
    /**
     * Return the NodeQuery which is the root of this traversal
     */
    NodeQuery getStartNode();

    NodeQuery asc();
    NodeQuery desc();
}
