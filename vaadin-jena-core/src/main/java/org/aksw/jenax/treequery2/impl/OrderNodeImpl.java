package org.aksw.jenax.treequery2.impl;

import org.aksw.facete.v3.api.TreeQueryImpl;
import org.aksw.facete.v3.api.TreeQueryNode;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetStep;
import org.aksw.jenax.treequery2.api.NodeQuery;
import org.aksw.jenax.treequery2.api.OrderNode;
import org.apache.jena.query.Query;
import org.apache.jena.query.SortCondition;

public class OrderNodeImpl
    implements OrderNode
{
    protected NodeQuery startNode;
    protected TreeQueryNode traversalNode;

    public OrderNodeImpl(NodeQuery startNode) {
        this(startNode, new TreeQueryImpl().root());
    }

    public OrderNodeImpl(NodeQuery startNode, TreeQueryNode traversalNode) {
        super();
        this.startNode = startNode;
        this.traversalNode = traversalNode;
    }

    @Override
    public OrderNode getOrCreateChild(FacetStep step) {
        return new OrderNodeImpl(startNode, traversalNode.getOrCreateChild(step));
    }

    @Override
    public NodeQuery getStartNode() {
        return startNode;
    }

    @Override
    public NodeQuery asc() {
        return sort(Query.ORDER_ASCENDING);
    }

    @Override
    public NodeQuery desc() {
        return sort(Query.ORDER_DESCENDING);
    }

    protected NodeQuery sort(int sortDirection) {
        FacetPath facetPath = traversalNode.getFacetPath();
        NodeQuery tgt = startNode.resolve(facetPath);
        startNode.relationQuery().getSortConditions().add(new SortCondition(tgt.asJenaNode(), sortDirection));
        return startNode;
    }
}
