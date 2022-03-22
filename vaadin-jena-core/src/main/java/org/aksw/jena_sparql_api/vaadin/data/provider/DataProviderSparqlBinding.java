package org.aksw.jena_sparql_api.vaadin.data.provider;

import org.aksw.jenax.connection.query.QueryExecutionFactoryQuery;
import org.aksw.jenax.sparql.query.rx.SparqlRx;
import org.aksw.jenax.sparql.relation.api.Relation;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.engine.binding.Binding;

import io.reactivex.rxjava3.core.Flowable;

public class DataProviderSparqlBinding
        extends DataProviderSparqlBase<Binding> {

    private static final long serialVersionUID = 1L;

    public DataProviderSparqlBinding(Relation relation, QueryExecutionFactoryQuery qef) {
        super(relation, qef);
    }

    @Override
    protected Flowable<Binding> createSolutionFlow(Query query) {
        System.out.println(query);
        return SparqlRx.execSelectRaw(() -> qef.createQueryExecution(query));
    }

}
