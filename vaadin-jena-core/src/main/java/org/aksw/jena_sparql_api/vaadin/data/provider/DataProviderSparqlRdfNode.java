package org.aksw.jena_sparql_api.vaadin.data.provider;

import java.util.Optional;
import java.util.function.Function;

import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactoryQuery;
import org.aksw.jenax.sparql.query.rx.SparqlRx;
import org.aksw.jenax.sparql.relation.api.Relation;
import org.apache.jena.query.Query;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.engine.binding.Binding;

import io.reactivex.rxjava3.core.Flowable;

public class DataProviderSparqlRdfNode<T extends RDFNode>
    extends DataProviderSparqlBase<T>
{
    private static final long serialVersionUID = 1L;

    protected Class<T> rdfNodeClass;
    protected String projectedVarName;
    protected Function<Binding, QuerySolution> customBindingMapper;

    public DataProviderSparqlRdfNode(
            Relation relation,
            QueryExecutionFactoryQuery qef,
            Class<T> rdfNodeClass,
            String projectedVarName,
            Function<Binding, QuerySolution> customBindingMapper) {
        super(relation, qef);
        this.projectedVarName = projectedVarName;
        this.rdfNodeClass = rdfNodeClass;
        this.customBindingMapper = customBindingMapper;
    }

    @Override
    protected Flowable<T> createSolutionFlow(Query query) {
        Flowable<QuerySolution> coreFlow = customBindingMapper == null
                ? SparqlRx.execSelect(() -> qef.createQueryExecution(query))
                : SparqlRx.execSelectRaw(() -> qef.createQueryExecution(query)).map(customBindingMapper::apply);

        return coreFlow
                .map(qs -> Optional.ofNullable(qs.get(projectedVarName)).map(rdfNode -> rdfNode.as(rdfNodeClass)).orElse(null));
    }
}
