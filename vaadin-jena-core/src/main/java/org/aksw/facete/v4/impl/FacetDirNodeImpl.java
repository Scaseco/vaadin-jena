package org.aksw.facete.v4.impl;

import java.util.Map;

import org.aksw.facete.v3.api.Direction;
import org.aksw.facete.v3.api.FacetCount;
import org.aksw.facete.v3.api.FacetDirNode;
import org.aksw.facete.v3.api.FacetMultiNode;
import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete.v3.api.FacetValueCount;
import org.aksw.facete.v3.api.FacetedDataQuery;
import org.aksw.facete.v3.bgp.api.BgpNode;
import org.aksw.facete.v3.bgp.utils.PathAccessorImpl;
import org.aksw.facete.v3.impl.FacetedDataQueryImpl;
import org.aksw.jena_sparql_api.data_query.impl.FacetedQueryGenerator;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.sparql.relation.api.BinaryRelation;
import org.aksw.jenax.sparql.relation.api.TernaryRelation;
import org.aksw.jenax.sparql.relation.api.UnaryRelation;
import org.aksw.jenax.treequery2.api.ScopedFacetPath;
import org.apache.jena.graph.Node;
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
    public FacetMultiNode via(Resource property, Node component) {
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
        throw new UnsupportedOperationException();
    }

    @Override
    public FacetedDataQuery<RDFNode> facets(boolean includeAbsent) {
        // parent().step(direction).via(NodeUtils.ANY_IRI, FacetStep.PREDICATE).one().enterConstraints().exists().activate();
        // parent().step(direction).via(NodeUtils.ANY_IRI, FacetStep.TARGET).one().enterConstraints().exists().activate();
        // ElementGenerator.createQuery(parent.facetedQuery.relationQuery, x -> true);
        ElementGenerator eltGen = ElementGenerator.configure(parent.facetedQuery);

        // FIXME The variable must match the one of the base relation!
        ScopedFacetPath sfp = ScopedFacetPath.of(Vars.x, parent.node.getFacetPath());
        TernaryRelation relation = eltGen.createRelationFacetValue(null, sfp, org.aksw.commons.util.direction.Direction.ofFwd(direction.isForward()), null, null, false, false);

        UnaryRelation concept = relation.project(relation.getP()).toUnaryRelation();
        // UnaryRelation concept = FacetedQueryGenerator.createConceptFacets(relations, null);

//		UnaryRelation concept = qgen.createConceptFacets(parent.state(), !this.state.isFwd(), false, null);

//		BinaryRelation br = FacetedQueryGenerator.createRelationFacetsAndCounts(relations, pConstraint)(relations, null);
//
//
//		BasicPattern bgp = new BasicPattern();
//		bgp.add(new Triple(br.getSourceVar(), Vocab.facetCount.asNode(), br.getTargetVar()));
//		Template template = new Template(bgp);
//
        FacetedDataQuery<RDFNode> result = new FacetedDataQueryImpl<>(
                parent.query().connection(),
                concept.getElement(),
                concept.getVar(),
                null,
                RDFNode.class);
//
        return result;


//        System.out.println(relation);
        //return new FacetedDataQueryImpl<>(null, null, null, null)
        // throw new UnsupportedOperationException();
    }

    @Override
    public FacetedDataQuery<FacetCount> facetCounts(boolean includeAbsent) {
        // parent().step(direction).via(NodeUtils.ANY_IRI, FacetStep.PREDICATE).one().enterConstraints().exists().activate();
        // parent().step(direction).via(NodeUtils.ANY_IRI, FacetStep.TARGET).one().enterConstraints().exists().activate();
        // ElementGenerator.createQuery(parent.facetedQuery.relationQuery, x -> true);
        ElementGenerator eltGen = ElementGenerator.configure(parent.facetedQuery);
        ScopedFacetPath sfp = ScopedFacetPath.of(Vars.s, parent.node.getFacetPath());
        TernaryRelation relation = eltGen.createRelationFacetValue(null, sfp, org.aksw.commons.util.direction.Direction.ofFwd(direction.isForward()), null, null, false, false);
        System.out.println(relation);
        // Map<String, BinaryRelation> map = eltGen.createMapFacetsAndValues(parent.node.getFacetPath(), org.aksw.commons.util.direction.Direction.ofFwd(direction.isForward()), false, false, false);
        //map.entrySet().forEach(x -> System.out.println("Entry: " + x));
        throw new UnsupportedOperationException();
    }

    @Override
    public FacetedDataQuery<FacetCount> facetFocusCounts(boolean includeAbsent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FacetedDataQuery<FacetValueCount> facetValueCounts() {
        throw new UnsupportedOperationException();
    }

    @Override
    public FacetedDataQuery<FacetValueCount> facetValueTypeCounts() {
        throw new UnsupportedOperationException();
    }

    @Override
    public FacetedDataQuery<FacetValueCount> facetValueCountsWithAbsent(boolean includeAbsent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FacetedDataQuery<FacetValueCount> nonConstrainedFacetValueCounts() {
        throw new UnsupportedOperationException();
    }

}
