package org.aksw.vaadin.app.demo.view.edit.resource;

import org.aksw.jena_sparql_api.concepts.RelationUtils;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.jenax.connection.query.QueryExecutionFactoryQuery;
import org.aksw.jenax.sparql.relation.api.Relation;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.exec.RowSet;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class ResourceItem
    extends VerticalLayout
{
    protected Node node;
    protected QueryExecutionFactoryQuery qef;

    public ResourceItem(QueryExecutionFactoryQuery qef, Node node) {
        this.add(new Span("Some Resource: " + node));

        Query q = QueryFactory.create("SELECT ?s ?p ?o { ?s ?p ?o }");
        QueryUtils.injectFilter(q, "s", node);

        Relation r = RelationUtils.fromQuery(q);

        try (QueryExecution qe = qef.createQueryExecution(r.toQuery())) {
            RowSet rs = RowSet.adapt(qe.execSelect());
            while (rs.hasNext()) {
                add(new Span("" + rs.next()));
            }
        }

        // qef.createQueryExecution()
    }
}
