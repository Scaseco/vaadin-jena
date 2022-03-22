package org.aksw.vaadin.app.demo.view.edit.resource;

import org.aksw.commons.collection.observable.ObservableValue;
import org.aksw.commons.io.buffer.array.ArrayOps;
import org.aksw.commons.io.cache.AdvancedRangeCacheConfig;
import org.aksw.commons.io.cache.AdvancedRangeCacheConfigImpl;
import org.aksw.commons.io.input.DataStreamSources;
import org.aksw.commons.io.slice.Slice;
import org.aksw.commons.io.slice.SliceAccessor;
import org.aksw.commons.io.slice.SliceInMemoryCache;
import org.aksw.commons.path.core.Path;
import org.aksw.jena_sparql_api.concepts.RelationUtils;
import org.aksw.jena_sparql_api.vaadin.data.provider.DataProviderSparqlBinding;
import org.aksw.jena_sparql_api.vaadin.util.VaadinSparqlUtils;
import org.aksw.jenax.connection.query.QueryExecutionFactoryQuery;
import org.aksw.jenax.sparql.relation.api.Relation;
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

        propertyGrid.setSelectionMode(SelectionMode.SINGLE);
        propertyGrid.setSelectionMode(SelectionMode.MULTI);

        Query resourceQuery = QueryFactory.create("SELECT ?s { SELECT DISTINCT ?s { ?s ?p ?o } }");
        Relation resourceRelation = RelationUtils.fromQuery(resourceQuery);

        DataProvider<Binding, Expr> resourceDataProvider = new DataProviderSparqlBinding(resourceRelation, qef);

        resourceGrid.addComponentColumn(binding -> {
            System.out.println("Binding: " + binding);
            return new ResourceItem(qef, binding.get("s"));
        });
        resourceGrid.setDataProvider(resourceDataProvider);
        // resourceGrid.setDataProvider(new ListDataProvider<>(Arrays.asList(BindingFactory.binding(Vars.s, RDF.Nodes.type), BindingFactory.binding(Vars.s, RDF.Nodes.language))));// resourceDataProvider);


        // VaadinSparqlUtils.setQueryForGridBinding(resourceGrid, resourceGridHeaderRow, qef, resourceQuery);
        // VaadinSparqlUtils.configureGridFilter(resourceGrid, resourceGridFilterRow, resourceQuery.getProjectVars());



    }

    public void refresh() {

    }

}
