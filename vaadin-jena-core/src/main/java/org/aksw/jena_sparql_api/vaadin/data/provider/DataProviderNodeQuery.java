package org.aksw.jena_sparql_api.vaadin.data.provider;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.rx.lookup.LookupService;
import org.aksw.commons.util.range.CountInfo;
import org.aksw.commons.util.range.RangeUtils;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.core.utils.ServiceUtils;
import org.aksw.jena_sparql_api.lookup.LookupServiceSparqlConstructQuads;
import org.aksw.jenax.arq.dataset.api.DatasetOneNg;
import org.aksw.jenax.arq.dataset.impl.ResourceInDatasetImpl;
import org.aksw.jenax.connection.query.QueryExecutionFactoryQuery;
import org.aksw.jenax.sparql.query.rx.SparqlRx;
import org.aksw.jenax.sparql.relation.api.UnaryRelation;
import org.aksw.jenax.treequery2.api.NodeQuery;
import org.aksw.jenax.treequery2.impl.ElementGeneratorLateral;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.Quad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Range;
import com.google.common.primitives.Ints;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.Query;


public class DataProviderNodeQuery
    extends AbstractBackEndDataProvider<RDFNode, String> {
    private static final Logger logger = LoggerFactory.getLogger(DataProviderNodeQuery.class);

    private static final long serialVersionUID = 1L;

    // This feels like a hack - it might be better to have the distinct flag part of the relation but this needs more thought
    protected boolean alwaysDistinct = false;

    protected Supplier<UnaryRelation> conceptSupplier;

    protected NodeQuery nodeQuery;
    protected QueryExecutionFactoryQuery qef;

    // public int predefinedSize = -1;

    public DataProviderNodeQuery(QueryExecutionFactoryQuery qef, Supplier<UnaryRelation> conceptSupplier,
            NodeQuery nodeQuery) {
        super();
        this.qef = qef;
        this.conceptSupplier = conceptSupplier;
        this.nodeQuery = nodeQuery;
    }

    @Override
    protected Stream<RDFNode> fetchFromBackEnd(Query<RDFNode, String> query) {

        UnaryRelation concept = conceptSupplier.get();
        // new ListServiceConcept()


        org.apache.jena.query.Query sparqlQuery = ElementGeneratorLateral.toQuery(nodeQuery);
//        Quad templateQuad = sparqlQuery.getConstructTemplate().getQuads().get(0);
//        sparqlQuery.setQuerySelectType();
//        sparqlQuery.addProjectVars(Arrays.asList(templateQuad.getGraph(), templateQuad.getSubject(), templateQuad.getPredicate(), templateQuad.getObject()));
        // sparqlQuery = QueryGenerationUtils.constructToLateral(sparqlQuery, templateQuad, QueryType.SELECT, true, true);

        org.apache.jena.query.Query entityQuery = ConceptUtils.createQueryList(concept);
        entityQuery.setOffset(query.getOffset());
        entityQuery.setLimit(query.getLimit());

        List<Node> nodes = ServiceUtils.fetchList(qef.createQueryExecution(entityQuery), concept.getVar());
        // logger.info
        System.err.println("NodeQuery: " + sparqlQuery);
        System.err.println("GOT NODES: " + nodes);

        LookupService<Node, DatasetOneNg> lookupService = new LookupServiceSparqlConstructQuads(qef, sparqlQuery);
        Map<Node, DatasetOneNg> map = lookupService.fetchMap(nodes);
        Collection<RDFNode> result = map.values().stream()
                .map(ds -> (RDFNode)new ResourceInDatasetImpl(ds, ds.getGraphName(), NodeFactory.createURI(ds.getGraphName()))).collect(Collectors.toList());
        return result.stream();
    }

    @Override
    protected int sizeInBackEnd(Query<RDFNode, String> query) {
        org.apache.jena.query.Query sparqlQuery = ElementGeneratorLateral.toQuery(nodeQuery);
        Range<Long> range = SparqlRx.fetchCountQuery(qef, sparqlQuery, null, null).blockingGet();
        CountInfo countInfo = RangeUtils.toCountInfo(range);
        int result = Ints.saturatedCast(countInfo.getCount());
        return result;
    }
}
