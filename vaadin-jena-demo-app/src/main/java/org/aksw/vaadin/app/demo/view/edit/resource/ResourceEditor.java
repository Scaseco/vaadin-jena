package org.aksw.vaadin.app.demo.view.edit.resource;

import org.aksw.commons.collection.observable.ObservableValue;
import org.aksw.commons.path.core.Path;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.vaadin.data.provider.DataProviderSparqlBinding;
import org.aksw.jena_sparql_api.vaadin.util.VaadinSparqlUtils;
import org.aksw.jenax.connection.query.QueryExecutionFactoryQuery;
import org.aksw.jenax.sparql.relation.api.UnaryRelation;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout.Orientation;
import com.vaadin.flow.data.provider.DataProvider;

public class ResourceEditor
    extends VerticalLayout
{
    protected UnaryRelation subjectConcept;
    protected UnaryRelation graphConcept;

    protected ObservableValue<Path<Node>> path;
    protected RDFConnection conn;

    protected Grid<Binding> propertyGrid;
    protected HeaderRow propertyGridHeaderRow;
    protected HeaderRow propertyGridFilterRow;


    protected Grid<Binding> resourceGrid;
    protected HeaderRow resourceGridHeaderRow;
    protected HeaderRow resourceGridFilterRow;

    public ResourceEditor() {




        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setSizeFull();
        splitLayout.setOrientation(Orientation.HORIZONTAL);

        propertyGrid = new Grid<>();
        propertyGrid.setSelectionMode(SelectionMode.MULTI);
        propertyGridHeaderRow = propertyGrid.appendHeaderRow();
        propertyGridFilterRow = propertyGrid.appendHeaderRow();

        resourceGrid = new Grid<>();
        resourceGridHeaderRow = resourceGrid.appendHeaderRow();
        resourceGridFilterRow = resourceGrid.appendHeaderRow();

        splitLayout.addToPrimary(propertyGrid);
        splitLayout.addToSecondary(resourceGrid);


        add(splitLayout);


        Query propertyQuery = QueryFactory.create("SELECT ?p ?type ?c { { SElECT ?p (COUNT(DISTINCT ?o) AS ?c) { ?this ?p ?o } GROUP BY ?p } BIND(<urn:fwd> AS ?type) }");

        Dataset dataset = RDFDataMgr.loadDataset("linkedgeodata-2018-04-04.dcat.ttl");
        QueryExecutionFactoryQuery qef = q -> QueryExecutionFactory.create(q, dataset);

        VaadinSparqlUtils.setQueryForGridBinding(propertyGrid, propertyGridHeaderRow, qef, propertyQuery);
        VaadinSparqlUtils.configureGridFilter(propertyGrid, propertyGridFilterRow, propertyQuery.getProjectVars());


        Query resourceQuery = QueryFactory.create("SELECT ?s { ?s ?p ?o }");

        DataProvider<Binding, Expr> resourceDataProvider = new DataProviderSparqlBinding(Concept.createFromQuery(resourceQuery), qef);

        resourceGrid.addComponentColumn(binding -> {
            System.out.println("Binding: " + binding);
            return new ResourceItem(qef, binding.get("s"));
        });
        resourceGrid.setDataProvider(resourceDataProvider);


        // VaadinSparqlUtils.setQueryForGridBinding(resourceGrid, resourceGridHeaderRow, qef, resourceQuery);
        // VaadinSparqlUtils.configureGridFilter(resourceGrid, resourceGridFilterRow, resourceQuery.getProjectVars());



    }

    public void refresh() {

    }

}
