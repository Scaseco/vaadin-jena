package org.aksw.jena_sparql_api.vaadin.data.provider;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.commons.rx.lookup.LookupService;
import org.aksw.commons.util.obj.Enriched;
import org.aksw.jena_sparql_api.lookup.LookupServiceSparqlConstructQuads;
import org.aksw.jena_sparql_api.rx.entity.engine.EntityQueryRx;
import org.aksw.jena_sparql_api.rx.entity.model.EntityBaseQuery;
import org.aksw.jena_sparql_api.rx.entity.model.EntityGraphFragment;
import org.aksw.jena_sparql_api.rx.entity.model.EntityQueryBasic;
import org.aksw.jena_sparql_api.rx.entity.model.EntityQueryImpl;
import org.aksw.jena_sparql_api.rx.entity.model.EntityTemplateImpl;
import org.aksw.jena_sparql_api.rx.entity.model.GraphPartitionJoin;
import org.aksw.jenax.arq.dataset.api.DatasetOneNg;
import org.aksw.jenax.arq.datashape.viewselector.EntityClassifier;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.facete.treequery2.api.NodeQuery;
import org.aksw.jenax.facete.treequery2.api.RelationQuery;
import org.aksw.jenax.facete.treequery2.impl.ElementGeneratorLateral;
import org.aksw.jenax.sparql.fragment.api.Fragment1;
import org.aksw.jenax.sparql.fragment.impl.Concept;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.SortCondition;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Str;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.aggregate.AggMin;
import org.apache.jena.sparql.modify.request.QuadAcc;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import io.reactivex.rxjava3.core.Flowable;

public class DataRetriever
    implements LookupService<Node, Enriched<RDFNode>>
{
    protected Logger logger = LoggerFactory.getLogger(DataRetriever.class);

    protected EntityClassifier entityClassifier; //  = new EntityClassifier(Arrays.asList(Vars.s));

    /** A mapping of classifier id to NodeQuery in order to fetch the appropriate data */
    protected Map<Node, NodeQuery> classToQuery = new LinkedHashMap<>();

    /** The query execution factory on which to run the queries */
    // protected QueryExecutionFactoryQuery qef;
    protected RdfDataSource dataSource;

    public DataRetriever(RdfDataSource dataSource, EntityClassifier entityClassifier) {
        this.dataSource = dataSource;
        this.entityClassifier = entityClassifier;
    }

    public Map<Node, NodeQuery> getClassToQuery() {
        return classToQuery;
    }

    @Override
    public Flowable<Entry<Node, Enriched<RDFNode>>> apply(Iterable<Node> nodes) {
    // public Flowable<Entry<Node, RDFNode> apply(Iterable<Node> nodes) {

        // TODO Abstract as lookupservice in order to reuse partitioning


        Map<Node, Enriched<RDFNode>> result = new LinkedHashMap<>();

        EntityGraphFragment entityGraphFragment = entityClassifier.createGraphFragment();

        // UnaryRelation concept = Concept.create(Vars.s, nodes);
        Fragment1 concept = Concept.createFilteredSubjects(Vars.s, nodes);
        EntityBaseQuery ebq = new EntityBaseQuery(Collections.singletonList(Vars.s), new EntityTemplateImpl(), concept.asQuery());

        Expr partitionSortExpr = new ExprAggregator(Var.alloc("dummy"),
                new AggMin(new E_Str(new ExprVar(Vars.o))));
        ebq.getPartitionOrderBy().add(new SortCondition(partitionSortExpr, org.apache.jena.query.Query.ORDER_ASCENDING));


        EntityQueryImpl eq = new EntityQueryImpl();
        eq.setBaseQuery(ebq);
        eq.getMandatoryJoins().add(new GraphPartitionJoin(entityGraphFragment));

        EntityQueryBasic basic = EntityQueryRx.assembleEntityAndAttributeParts(eq);

//        // Classify the entities
        // List<Quad> rawQuads = EntityQueryRx.execConstructEntitiesNg(qef::createQueryExecution, basic).toList().blockingGet();
        List<RDFNode> rawEntities = EntityQueryRx.execConstructRooted(dataSource.asQef(), basic).toList().blockingGet(); //     execConstructEntitiesNg(qef::createQueryExecution, basic).toList().blockingGet();
//
//        Dataset ds = DatasetFactory.create();
//        rawQuads.forEach(ds.asDatasetGraph()::add);
//        List<ResourceInDataset> entries = FlowOfRdfNodesInDatasetsOps.naturalResources(ds).map(r -> r.as()).toList().blockingGet();
//        RDFNodeInDatasetUtils.na

        // FIXME I think the concept with EntityQueryRx was that the 'result set' is a set of RDF resources whose properties can be traversed.
        Multimap<Node, Node> entityToClasses = // EntityQueryRx.execConstructEntitiesNg(qef::createQueryExecution, basic)
                rawEntities.stream().flatMap(r -> r.asResource().listProperties(EntityClassifier.classifier).toList().stream().map(Statement::getObject).map(RDFNode::asNode).map(n -> Map.entry(r.asNode(), n)))
                .collect(Multimaps.toMultimap(Entry::getKey, Entry::getValue, HashMultimap::create));

//		Multimap<Node, Node> entityToClasses = // EntityQueryRx.execConstructEntitiesNg(qef::createQueryExecution, basic)
//				rawQuads.stream().collect(Multimaps.toMultimap(Quad::getGraph, Quad::getObject, HashMultimap::create));
        // ListMultimap<Node, Node> ll = ArrayListMultimap.create();
        // entityToClasses.forEach(ll::putAll);
        Multimap<Node, Node> classToEntities = Multimaps.invertFrom(entityToClasses, HashMultimap.create());
        Collection<Node> detectedClasses = new HashSet<>(entityToClasses.values());

        if (logger.isDebugEnabled()) {
            logger.debug("Detected classes: " + detectedClasses);
        }

        // .forEach(quad -> System.out.println(quad));
        // TODO Create a Multimap<Node, Node> entityToClasses
        // For each detected class perform the lookups with the respective node sets
        // for (Node classification : detectedClasses) {
        for (Entry<Node, Collection<Node>> e : classToEntities.asMap().entrySet()) {
            Node classification = e.getKey();
            Collection<Node> entities = e.getValue();
            if (logger.isDebugEnabled()) {
                logger.debug("Entities: " + entities);
            }
            NodeQuery nodeQuery = classToQuery.get(classification);
            if (nodeQuery != null) {
                Var rootVar = Vars.s;
                RelationQuery rrq = nodeQuery.relationQuery();
                Element elt = new ElementGeneratorLateral().createElement(rrq);
                Query query = new Query();
                query.setConstructTemplate(new Template(new QuadAcc(Arrays.asList(Quad.create(rootVar, Vars.x, Vars.y, Vars.z)))));
                query.setQueryConstructType();
                query.setQueryPattern(elt);
                if (logger.isDebugEnabled()) {
                    logger.debug("Generated Query: " + query);
                }
                LookupService<Node, DatasetOneNg> ls = new LookupServiceSparqlConstructQuads(dataSource.asQef(), query)
                        .partition(30);
                Map<Node, RDFNode> data = ls
                        .mapNonNullValues(ds -> {
                            RDFNode r = ds.getSelfResource();
                            // RDFNode r = ds == null ? null : ds.getSelfResource().listProperties(EntityClassifier.classifier).getObject();
                            // RDFDataMgr.write(System.out, r.getModel(), RDFFormat.TURTLE_PRETTY);
                            return r;
                        })
                        .fetchMap(entities);

                for (Entry<Node, RDFNode> f : data.entrySet()) {
                    result.compute(f.getKey(), (k, v) -> {
                        if (v == null) {
                            v = Enriched.of(f.getValue());
                        }

                        v.getOrCreateInstance(Classification.class, Classification::new)
                                .getClasses().add(classification);

                        return v;
                    });

                    // EnrichedItem<RDFNode> item = Enirched f.getValue();

                    // result.putAll(data);
                }
                // System.out.println("Data: " + data);
            } else {
                if (logger.isWarnEnabled()) {
                    logger.warn("No query registered for classification: " + classification);
                }
            }
            // nodeQuery
        }

        // TODO Add all unclassified items
        for (Node node : nodes) {
            if (!result.containsKey(node)) {
                // Used DatasetOneNg for consistency?
                Model model = ModelFactory.createDefaultModel();
                RDFNode rdfNode = model.asRDFNode(node);
                Enriched<RDFNode> item = Enriched.of(rdfNode, new Classification());
                result.put(node, item);
            }
        }

        return Flowable.fromIterable(result.entrySet());

//        Relation r = entityClassifier.createClassifyingRelation();
//
//        UnaryRelation testConcept = ConceptUtils.createForRdfType("http://foo.bar/baz");
//        Relation s = testConcept.join().with(r, r.getVars().get(0)); //r.joinOn(r.getVars().get(0)).with(testConcept);
//
//
//        Relation grouped =  RelationUtils.groupBy(s, s.getVars().iterator().next(), Vars.c, false);
//        System.out.println("Grouped relation: " + grouped);
//
//        Op op = Algebra.optimize(Algebra.compile(grouped.getElement()));
//        System.out.println(op);
        // return null;
    }

}
