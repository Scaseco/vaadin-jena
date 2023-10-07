package org.aksw.jena_sparql_api.vaadin.data.provider;

import java.util.function.Function;

import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactoryQuery;
import org.aksw.jenax.sparql.relation.api.Relation;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.engine.binding.Binding;

public class DataProviderSparqlResource<T extends Resource>
	extends DataProviderSparqlRdfNode<T>
{
	private static final long serialVersionUID = 1L;

	public DataProviderSparqlResource(
			Relation relation, QueryExecutionFactoryQuery qef, Class<T> resourceClass, String projectedVarName, Function<Binding, QuerySolution> customBindingMapper) {
		super(relation, qef, resourceClass, projectedVarName, customBindingMapper);
	}
}
