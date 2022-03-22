package org.aksw.vaadin.app.demo.view.edit.resource;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import org.aksw.commons.util.page.Page;
import org.aksw.commons.util.page.Paginator;
import org.aksw.commons.util.page.PaginatorImpl;
import org.aksw.jena_sparql_api.concepts.RelationUtils;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.jenax.connection.query.QueryExecutionFactoryQuery;
import org.aksw.jenax.sparql.relation.api.Relation;
import org.aksw.vaadin.component.rdf_term_editor.RdfTermEditor;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.RowSet;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class ResourceItem
    extends VerticalLayout
{
    protected Node node;
    protected QueryExecutionFactoryQuery qef;

    public ResourceItem(QueryExecutionFactoryQuery qef, Node node) {
        this.add(new Span("Some Resource: " + node));

        Query q = QueryFactory.create("SELECT ?s ?p ?o { ?s ?p ?o } ORDER By ?s ?p ?o");
        if (node != null) {
            QueryUtils.injectFilter(q, "s", node);
        }

        Relation r = RelationUtils.fromQuery(q);

        Multimap<Node, Node> pToO = ArrayListMultimap.create();
        try (QueryExecution qe = qef.createQueryExecution(r.toQuery())) {
            RowSet rs = RowSet.adapt(qe.execSelect());
            while (rs.hasNext()) {
                Binding b = rs.next();
                Node p = b.get("p");
                Node o = b.get("o");
                pToO.put(p, o);
            }
        }

        for (Entry<Node, Collection<Node>> e : pToO.asMap().entrySet()) {
            Node p = e.getKey();
            HorizontalLayout pRow = new HorizontalLayout();
            pRow.add(new Span("" + p));

            Paginator<Page> paginator = new PaginatorImpl(10);
            List<Page> pages = paginator.createPages(10000, 5);

            for (Page page : pages) {
                pRow.add(new Span("" + page.getPageNumber()));
            }

            add(pRow);

            for (Node o : e.getValue()) {
                RdfTermEditor termEditor = new RdfTermEditor();
                termEditor.setValue(o);
                add(termEditor);
            }
        }
    }
}
