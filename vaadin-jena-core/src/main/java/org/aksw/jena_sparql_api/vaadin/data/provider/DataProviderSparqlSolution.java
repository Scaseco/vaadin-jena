package org.aksw.jena_sparql_api.vaadin.data.provider;

import org.aksw.jenax.arq.util.binding.QuerySolutionWithEquals;
import org.aksw.jenax.connection.query.QueryExecutionFactoryQuery;
import org.aksw.jenax.sparql.query.rx.SparqlRx;
import org.aksw.jenax.sparql.relation.api.Relation;
import org.apache.jena.query.Query;
import org.apache.jena.query.QuerySolution;

import io.reactivex.rxjava3.core.Flowable;

public class DataProviderSparqlSolution
    extends DataProviderSparqlBase<QuerySolution>
{
    private static final long serialVersionUID = 1L;

    public DataProviderSparqlSolution(Relation relation, QueryExecutionFactoryQuery qef) {
        super(relation, qef);
    }

    @Override
    protected Flowable<QuerySolution> createSolutionFlow(Query query) {
        // Wrap all results with working equals/hashCode based on the underlying bindings
        return SparqlRx.execSelect(() -> qef.createQueryExecution(query))
                .map(QuerySolutionWithEquals::new);
    }
}
