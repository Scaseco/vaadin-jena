package org.aksw.facete.v4.impl;

import org.aksw.facete.v3.api.Direction;
import org.aksw.facete.v3.api.FacetCount;
import org.aksw.facete.v3.api.FacetDirNode;
import org.aksw.facete.v3.api.FacetMultiNode;
import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete.v3.api.FacetValueCount;
import org.aksw.facete.v3.api.FacetedDataQuery;
import org.aksw.jenax.sparql.relation.api.BinaryRelation;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

public class FacetDirNodeImpl
    implements FacetDirNode
{
    protected FacetNodeImpl parent;
    protected Direction direction;

    public FacetDirNodeImpl(FacetNodeImpl parent, Direction direction) {
        super();
        this.parent = parent;
        this.direction = direction;
    }

    @Override
    public FacetMultiNode via(Resource property, Integer component) {
        return new FacetMultiNodeImpl(this, property, component);
    }

    @Override
    public boolean isFwd() {
        return direction.isForward();
    }

    @Override
    public FacetNode parent() {
        return parent;
    }

    @Override
    public Direction dir() {
        return direction;
    }

    @Override
    public BinaryRelation facetValueRelation() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FacetedDataQuery<RDFNode> facets(boolean includeAbsent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FacetedDataQuery<FacetCount> facetCounts(boolean includeAbsent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FacetedDataQuery<FacetCount> facetFocusCounts(boolean includeAbsent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FacetedDataQuery<FacetValueCount> facetValueCounts() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FacetedDataQuery<FacetValueCount> facetValueTypeCounts() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FacetedDataQuery<FacetValueCount> facetValueCountsWithAbsent(boolean includeAbsent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FacetedDataQuery<FacetValueCount> nonConstrainedFacetValueCounts() {
        // TODO Auto-generated method stub
        return null;
    }

}
