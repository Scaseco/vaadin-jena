package org.aksw.jena_sparql_api.vaadin.data.provider;

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
import org.aksw.jenax.treequery2.impl.ElementGeneratorLateral;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.RDFNode;
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

    /** The query execution factory on which to run the queries */
    protected QueryExecutionFactoryQuery qef;

    /** The supplier for the specification of the initial set of RDF terms */
    protected Supplier<UnaryRelation> conceptSupplier;

    /** A classifier to determine what data to fetch */
    // protected MapService<Concept, Node, Set<Node>> classifierService;

    protected DataRetriever retriever;

    // protected NodeQuery nodeQuery;


    public DataProviderNodeQuery(QueryExecutionFactoryQuery qef, Supplier<UnaryRelation> conceptSupplier, DataRetriever retriever) {
        super();
        this.qef = qef;
        this.conceptSupplier = conceptSupplier;
        this.retriever = retriever;
    }

    @Override
    protected Stream<RDFNode> fetchFromBackEnd(Query<RDFNode, String> query) {

        UnaryRelation concept = conceptSupplier.get();
        // new ListServiceConcept()


        org.apache.jena.query.Query sparqlQuery = null; // ElementGeneratorLateral.toQuery(nodeQuery);
//        Quad templateQuad = sparqlQuery.getConstructTemplate().getQuads().get(0);
//        sparqlQuery.setQuerySelectType();
//        sparqlQuery.addProjectVars(Arrays.asList(templateQuad.getGraph(), templateQuad.getSubject(), templateQuad.getPredicate(), templateQuad.getObject()));
        // sparqlQuery = QueryGenerationUtils.constructToLateral(sparqlQuery, templateQuad, QueryType.SELECT, true, true);

        org.apache.jena.query.Query entityQuery = ConceptUtils.createQueryList(concept);
        entityQuery.setOffset(query.getOffset());
        entityQuery.setLimit(query.getLimit());

        // Retrieve the set of nodes
        List<Node> nodes = ServiceUtils.fetchList(qef.createQueryExecution(entityQuery), concept.getVar());

        // Pass the nodes through the classifier
        // EntityClassifier.




        // logger.info
        System.err.println("NodeQuery: " + sparqlQuery);
        System.err.println("GOT NODES: " + nodes);

        retriever.retrieve(null);

        LookupService<Node, DatasetOneNg> lookupService = new LookupServiceSparqlConstructQuads(qef, sparqlQuery);
        Map<Node, DatasetOneNg> map = lookupService.fetchMap(nodes);
        Collection<RDFNode> result = map.values().stream()
                .map(ds -> (RDFNode)new ResourceInDatasetImpl(ds, ds.getGraphName(), NodeFactory.createURI(ds.getGraphName()))).collect(Collectors.toList());
        return result.stream();
    }

    @Override
    protected int sizeInBackEnd(Query<RDFNode, String> query) {
        UnaryRelation concept = conceptSupplier.get();
        org.apache.jena.query.Query sparqlQuery = concept.asQuery();
        // org.apache.jena.query.Query sparqlQuery = ElementGeneratorLateral.toQuery(nodeQuery);
        Range<Long> range = SparqlRx.fetchCountQuery(qef, sparqlQuery, null, null).blockingGet();
        CountInfo countInfo = RangeUtils.toCountInfo(range);
        int result = Ints.saturatedCast(countInfo.getCount());
        return result;
    }
}
