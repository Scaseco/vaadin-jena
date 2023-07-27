package org.aksw.jena_sparql_api.vaadin.data.provider;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.rx.entity.engine.EntityQueryRx;
import org.aksw.jena_sparql_api.rx.entity.model.EntityBaseQuery;
import org.aksw.jena_sparql_api.rx.entity.model.EntityGraphFragment;
import org.aksw.jena_sparql_api.rx.entity.model.EntityQueryBasic;
import org.aksw.jena_sparql_api.rx.entity.model.EntityQueryImpl;
import org.aksw.jena_sparql_api.rx.entity.model.EntityTemplateImpl;
import org.aksw.jena_sparql_api.rx.entity.model.GraphPartitionJoin;
import org.aksw.jenax.arq.datashape.viewselector.EntityClassifier;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.connection.query.QueryExecutionFactoryQuery;
import org.aksw.jenax.sparql.relation.api.UnaryRelation;
import org.aksw.jenax.treequery2.api.NodeQuery;
import org.apache.jena.graph.Node;
import org.apache.jena.query.SortCondition;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Str;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.aggregate.AggMin;

public class DataRetriever {
    protected EntityClassifier entityClassifier = new EntityClassifier(Arrays.asList(Vars.s));

    /** A mapping of classifier id to NodeQuery in order to fetch the appropriate data */
    protected Map<Node, NodeQuery> classToQuery = new LinkedHashMap<>();

    /** The query execution factory on which to run the queries */
    protected QueryExecutionFactoryQuery qef;


    public Map<Node, RDFNode> retrieve(List<Node> nodes) {

        EntityGraphFragment entityGraphFragment = entityClassifier.createGraphFragment();

        UnaryRelation concept = Concept.create(Vars.s, nodes);
        EntityBaseQuery ebq = new EntityBaseQuery(Collections.singletonList(Vars.s), new EntityTemplateImpl(), concept.asQuery());

        Expr partitionSortExpr = new ExprAggregator(Var.alloc("dummy"),
                new AggMin(new E_Str(new ExprVar(Vars.o))));
        ebq.getPartitionOrderBy().add(new SortCondition(partitionSortExpr, org.apache.jena.query.Query.ORDER_ASCENDING));


        EntityQueryImpl eq = new EntityQueryImpl();
        eq.setBaseQuery(ebq);
        eq.getMandatoryJoins().add(new GraphPartitionJoin(entityGraphFragment));

        EntityQueryBasic basic = EntityQueryRx.assembleEntityAndAttributeParts(eq);

        EntityQueryRx.execConstructEntitiesNg(qef::createQueryExecution, basic).forEach(quad -> System.out.println(quad));



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
        return null;

    }
}
