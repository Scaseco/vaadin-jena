package org.aksw.facete.v4.impl;

import org.aksw.facete.v3.api.ConstraintFacade;
import org.aksw.facete.v3.api.Direction;
import org.aksw.facete.v3.api.FacetDirNode;
import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete.v3.api.FacetedDataQuery;
import org.aksw.facete.v3.api.FacetedQuery;
import org.aksw.facete.v3.api.TreeQueryNode;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetStep;
import org.aksw.jenax.sparql.relation.api.BinaryRelation;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.Var;

public class FacetNodeImpl
    implements FacetNode
{
    protected FacetedQueryImpl facetedQuery;

    /** The tree query node that backs this views */
    protected TreeQueryNode node;

    public FacetNodeImpl(FacetedQueryImpl facetedQuery, TreeQueryNode node) {
        super();
        this.facetedQuery = facetedQuery;
        this.node = node;
    }

    @Override
    public FacetNode parent() {
        TreeQueryNode parent = node.getParent();
        FacetNode result = parent == null ? null : facetedQuery.wrapNode(parent);
        return result;
    }

    @Override
    public FacetDirNode fwd() {
        return new FacetDirNodeImpl(this, Direction.FORWARD);
    }

    @Override
    public FacetDirNode bwd() {
        return new FacetDirNodeImpl(this, Direction.BACKWARD);
    }

    @Override
    public FacetedQuery query() {
        return facetedQuery;
    }

    @Override
    public FacetNode chRoot() {
        node.chRoot();
        return this;
    }

    @Override
    public FacetNode chFocus() {
        facetedQuery.focus = node;
        return this;
    }

    @Override
    public FacetNode as(String varName) {
        throw new UnsupportedOperationException("this method will be removed");
    }

    @Override
    public FacetNode as(Var var) {
        throw new UnsupportedOperationException("this method will be removed");
    }

    @Override
    public Var alias() {
        throw new UnsupportedOperationException("this method will be removed");
    }

    public FacetStep reachingStep() {
        FacetPath path = node.getFacetPath();
        FacetStep result = path.getNameCount() == 0 ? null : path.getFileName().toSegment();
        return result;
    }

    @Override
    public Direction reachingDirection() {
        throw new UnsupportedOperationException("use reachingStep()");
    }

    @Override
    public Node reachingPredicate() {
        throw new UnsupportedOperationException("use reachingStep()");
    }

    @Override
    public String reachingAlias() {
        throw new UnsupportedOperationException("use reachingStep()");
    }

    @Override
    public Integer targetComponent() {
        throw new UnsupportedOperationException("use reachingStep()");
    }

    @Override
    public BinaryRelation getReachingRelation() {
        throw new UnsupportedOperationException();
    }

    @Override
    public FacetNode root() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ConstraintFacade<? extends FacetNode> constraints() {
        throw new UnsupportedOperationException();
    }

    @Override
    public FacetedDataQuery<RDFNode> availableValues() {
        throw new UnsupportedOperationException();
    }

    @Override
    public FacetedDataQuery<RDFNode> remainingValues() {
        throw new UnsupportedOperationException();
    }
}
