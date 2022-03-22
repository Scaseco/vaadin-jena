package org.aksw.vaadin.app.demo.view.edit.resource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.collection.observable.ObservableValue;
import org.aksw.commons.collection.observable.ObservableValueImpl;
import org.aksw.commons.collections.PolaritySet;
import org.aksw.jena_sparql_api.collection.observable.GraphChange;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.concepts.RelationUtils;
import org.aksw.jena_sparql_api.vaadin.data.provider.DataProviderSparqlBinding;
import org.aksw.jena_sparql_api.vaadin.util.VaadinSparqlUtils;
import org.aksw.jenax.arq.datasource.RdfDataSourceFromDataset;
import org.aksw.jenax.arq.util.node.PathUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.connection.datasource.RdfDataSource;
import org.aksw.jenax.connection.query.QueryExecutionFactoryQuery;
import org.aksw.jenax.sparql.relation.api.Relation;
import org.aksw.jenax.sparql.relation.api.UnaryRelation;
import org.aksw.vaadin.app.demo.view.edit.resource.DataRetriever.ResourceInfo;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.RDF;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.GridVariant;
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

    protected ObservableValue<Set<Path>> visibleProperties = ObservableValueImpl.create(new LinkedHashSet<>());
    // protected RDFConnection conn;
    protected RdfDataSource rdfDataSource;

    protected Grid<Binding> propertyGrid;
    protected HeaderRow propertyGridHeaderRow;
    protected HeaderRow propertyGridFilterRow;


    protected Grid<Binding> resourceGrid;
    protected HeaderRow resourceGridHeaderRow;
    protected HeaderRow resourceGridFilterRow;

    public ResourceEditor() {
        setSizeFull();

        GraphChange graphEditorModel = new GraphChange();

        Set<Path> paths = visibleProperties.get();
        paths.add(PathUtils.createStep(RDF.type.asNode(), true));
        paths.add(PathUtils.createStep(DCAT.downloadURL.asNode(), true));

        Dataset dataset = RDFDataMgr.loadDataset("linkedgeodata-2018-04-04.dcat.ttl");
        RdfDataSource rdfDataSource = RdfDataSourceFromDataset.create(dataset, true);

        Set<Node> nodes = new LinkedHashSet<>(Arrays.asList(NodeFactory.createURI("http://dcat.linkedgeodata.org/dataset/osm-bremen-2018-04-04")));
        subjectConcept = ConceptUtils.createConcept(nodes);

        DataRetriever dataRetriever = new DataRetriever(rdfDataSource);

        // QueryExecutionFactoryRangeCache qef = QueryExecutionFactoryRangeCache.create(null, null, 0, null)


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

        splitLayout.setSizeFull();
        resourceGrid.setSizeFull();
        propertyGrid.setSizeFull();


        resourceGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        add(splitLayout);


        Query propertyQuery = QueryFactory.create("SELECT ?p ?type ?c { { SElECT ?p (COUNT(DISTINCT ?o) AS ?c) { ?this ?p ?o } GROUP BY ?p } BIND(<urn:fwd> AS ?type) }");

        QueryExecutionFactoryQuery qef = q -> QueryExecutionFactory.create(q, dataset);

        VaadinSparqlUtils.setQueryForGridBinding(propertyGrid, propertyGridHeaderRow, qef, propertyQuery);
        VaadinSparqlUtils.configureGridFilter(propertyGrid, propertyGridFilterRow, propertyQuery.getProjectVars());

        propertyGrid.setSelectionMode(SelectionMode.SINGLE);
        propertyGrid.setSelectionMode(SelectionMode.MULTI);

        Query resourceQuery = QueryFactory.create("SELECT ?s { { SELECT DISTINCT ?s { ?s ?p ?o } LIMIT 10 } }");
        // Query resourceQuery = QueryFactory.create("SELECT ?s { BIND(<http://dcat.linkedgeodata.org/dataset/osm-bremen-2018-04-04> AS ?s) }");
        Relation resourceRelation = RelationUtils.fromQuery(resourceQuery);


        // TODO replace with a guava cache
        Map<Node, ResourceInfo> globalResourceState = new HashMap<>();
        // propertyGrid.getSelectionModel();

        DataProvider<Binding, Expr> resourceDataProvider = new DataProviderSparqlBinding(resourceRelation, qef) {

            @Override
            public java.util.stream.Stream<Binding> fetch(com.vaadin.flow.data.provider.Query<Binding,Expr> t) {
                List<Binding> bindings;
                try (Stream<Binding> stream = super.fetch(t)){
                    bindings = stream.collect(Collectors.toList());
                }

                Set<Node> nodes = bindings.stream().map(b -> b.get(Vars.s)).collect(Collectors.toSet());

                Map<Node, ResourceInfo> map = dataRetriever.fetch(nodes, PolaritySet.create(false));
                globalResourceState.putAll(map);
                System.out.println("Mapit: " + map);

                return bindings.stream();
            };

        };


        // resourceDataProvider.fi;

        resourceGrid.addComponentColumn(binding -> {
            System.out.println("Binding: " + binding);
            Node node = binding.get("s");
            ResourceInfo resourceInfo = globalResourceState.get(node);
            return new ResourceItem(resourceInfo, graphEditorModel, visibleProperties);
        });
        resourceGrid.setDataProvider(resourceDataProvider);
        // resourceGrid.setDa
        // VaadinSparqlUtils.setQueryForGridBinding(resourceGrid, resourceGridHeaderRow, qef, resourceQuery);
        // VaadinSparqlUtils.configureGridFilter(resourceGrid, resourceGridFilterRow, resourceQuery.getProjectVars());



    }

    protected void syncPropertiesWithView(Relation relation, int offset, int limit) {

    }


    public void refresh() {

    }

}
