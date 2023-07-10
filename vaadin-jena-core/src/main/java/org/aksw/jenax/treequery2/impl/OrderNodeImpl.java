package org.aksw.jenax.treequery2.impl;

import org.aksw.jenax.path.core.FacetStep;
import org.aksw.jenax.treequery2.OrderNode;
import org.aksw.jenax.treequery2.old.NodeQueryOld;
import org.aksw.jenax.treequery2.old.NodeQueryOldImpl;

public class OrderNodeImpl
    extends NodeQueryOldImpl
    implements OrderNode
{
    // protected NodeQuery nodeQuery;

    public OrderNodeImpl(NodeQueryOld parent, FacetStep step) {
        super(parent, step);
    }

    @Override
    public NodeQueryOld asc() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NodeQueryOld desc() {
        // TODO Auto-generated method stub
        return null;
    }

}
