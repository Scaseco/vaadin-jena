package org.aksw.jena_sparql_api.vaadin.data.provider;

import org.aksw.jenax.arq.util.binding.QuerySolutionWithEquals;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactoryQuery;
import org.aksw.jenax.sparql.fragment.api.Fragment;
import org.aksw.jenax.sparql.query.rx.SparqlRx;
import org.apache.jena.query.Query;
import org.apache.jena.query.QuerySolution;

import io.reactivex.rxjava3.core.Flowable;

public class DataProviderSparqlSolution
    extends DataProviderSparqlBase<QuerySolution>
{
    private static final long serialVersionUID = 1L;

    public DataProviderSparqlSolution(Fragment relation, QueryExecutionFactoryQuery qef) {
        super(relation, qef);
    }

    @Override
    protected Flowable<QuerySolution> createSolutionFlow(Query query) {
        // Wrap all results with working equals/hashCode based on the underlying bindings
        return SparqlRx.execSelect(() -> qef.createQueryExecution(query))
                .map(QuerySolutionWithEquals::new);
    }
}
