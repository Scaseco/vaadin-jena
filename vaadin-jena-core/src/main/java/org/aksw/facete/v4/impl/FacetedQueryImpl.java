package org.aksw.facete.v4.impl;

import java.util.Collection;
import java.util.function.Supplier;

import org.aksw.commons.util.cache.CacheUtils;
import org.aksw.facete.v3.api.FacetConstraint;
import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete.v3.api.FacetedQuery;
import org.aksw.facete.v3.api.TreeQueryNode;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jenax.sparql.relation.api.Relation;
import org.aksw.jenax.sparql.relation.api.UnaryRelation;
import org.apache.jena.ext.com.google.common.base.Preconditions;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.sparql.core.Var;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class FacetedQueryImpl
    implements FacetedQuery
{
    protected FacetedRelationQuery relationQuery;
    // protected FacetConstraints constraints;
    protected TreeQueryNode focus;

    protected Cache<TreeQueryNode, FacetNode> viewCache = CacheBuilder.newBuilder().maximumSize(1000).build();


    public FacetedQueryImpl(FacetedRelationQuery relationQuery, TreeQueryNode focus) {
        super();
        this.relationQuery = relationQuery;
        // this.constraints = constraints;
        this.focus = focus;
    }

    public FacetedRelationQuery relationQuery() {
        return relationQuery;
    }

//    public FacetedQueryImpl() {
//        super();
//        TreeQuery treeQuery = new TreeQueryImpl();
//        this.constraints = new FacetConstraints(treeQuery);
//        this.focus = treeQuery.root();
//    }


    FacetNode wrapNode(TreeQueryNode node) {
        return CacheUtils.get(viewCache, node, () -> new FacetNodeImpl(this, node));
    }

    @Override
    public FacetNode root() {
        return wrapNode(focus);
        // return wrapNode(constraints.getTreeQuery().root());
    }

    @Override
    public FacetNode focus() {
        return wrapNode(focus);
    }

    @Override
    public void focus(FacetNode node) {
        Preconditions.checkArgument(node.query() == this, "Facet Node must belong to this query");
        focus = ((FacetNodeImpl)node).node;
    }

    @Override
    public Concept toConcept() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<FacetConstraint> constraints() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FacetedQuery baseConcept(Supplier<? extends UnaryRelation> conceptSupplier) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FacetedQuery baseConcept(UnaryRelation concept) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UnaryRelation baseConcept() {
        Var rootVar = relationQuery.varToRoot.inverse().get(focus);
        Relation r = relationQuery.baseRelation.get();
        return r.project(rootVar).toUnaryRelation();

        // throw new UnsupportedOperationException("Use relationQuery().baseRelation()");
    }

    @Override
    public FacetedQuery connection(SparqlQueryConnection conn) {
        throw new UnsupportedOperationException("Execution API is now separate from the faceted query model");
    }

    @Override
    public SparqlQueryConnection connection() {
        throw new UnsupportedOperationException("Execution API is now separate from the faceted query model");
    }
}
