package org.aksw.jena_sparql_api.vaadin.data.provider;

import java.util.ArrayList;
import java.util.List;

import org.aksw.jena_sparql_api.concepts.RelationImpl;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactoryDataset;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactoryQuery;
import org.aksw.jenax.sparql.query.rx.SparqlRx;
import org.aksw.jenax.sparql.relation.api.Relation;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.syntax.ElementData;

import io.reactivex.rxjava3.core.Flowable;

public class DataProviderSparqlBinding
        extends DataProviderSparqlBase<Binding> {

    private static final long serialVersionUID = 1L;

    public DataProviderSparqlBinding(Relation relation, QueryExecutionFactoryQuery qef) {
        super(relation, qef);
    }

    public static DataProviderSparqlBinding create(List<Var> vars) {
        QueryExecutionFactoryQuery qef = new QueryExecutionFactoryDataset();
        ElementData elt = new ElementData(vars, new ArrayList<>());
        Relation relation = new RelationImpl(elt, vars);
        return new DataProviderSparqlBinding(relation, qef);
    }

    @Override
    protected Flowable<Binding> createSolutionFlow(Query query) {
        // System.out.println(query);
        return SparqlRx.execSelectRaw(() -> qef.createQueryExecution(query));
    }
}
