package org.aksw.vaadin.app.demo.view.edit.resource;

import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.collection.observable.CollectionChangedEventImpl;
import org.aksw.commons.collection.observable.ObservableSet;
import org.aksw.commons.collection.observable.ObservableValue;
import org.aksw.commons.collection.observable.ObservableValueImpl;
import org.aksw.commons.collection.observable.Registration;
import org.aksw.commons.collections.PolaritySet;
import org.aksw.jena_sparql_api.collection.observable.GraphChange;
import org.aksw.jena_sparql_api.common.DefaultPrefixes;
import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.concepts.RelationUtils;
import org.aksw.jena_sparql_api.vaadin.data.provider.DataProviderSparqlBase;
import org.aksw.jena_sparql_api.vaadin.data.provider.DataProviderSparqlBinding;
import org.aksw.jena_sparql_api.vaadin.util.VaadinComponentUtils;
import org.aksw.jena_sparql_api.vaadin.util.VaadinSparqlUtils;
import org.aksw.jenax.arq.connection.core.QueryExecutionFactory;
import org.aksw.jenax.arq.datasource.RdfDataEngineFromDataset;
import org.aksw.jenax.arq.util.expr.ExprUtils;
import org.aksw.jenax.arq.util.node.PathUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.connection.datasource.RdfDataSource;
import org.aksw.jenax.connection.query.QueryExecutionFactoryDataset;
import org.aksw.jenax.dataaccess.LabelUtils;
import org.aksw.jenax.path.core.PathOpsPP;
import org.aksw.jenax.path.core.PathPP;
import org.aksw.jenax.sparql.path.SimplePath;
import org.aksw.jenax.sparql.relation.api.BinaryRelation;
import org.aksw.jenax.sparql.relation.api.Relation;
import org.aksw.jenax.sparql.relation.api.UnaryRelation;
import org.aksw.jenax.vaadin.component.breadcrumb.Breadcrumb;
import org.aksw.jenax.vaadin.label.VaadinLabelMgr;
import org.aksw.vaadin.common.component.util.ConfirmDialogUtils;
import org.aksw.vaadin.common.component.util.NotificationUtils;
import org.apache.commons.collections4.list.SetUniqueList;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.claspina.confirmdialog.ConfirmDialog;

import com.google.common.collect.ForwardingSet;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.GridMultiSelectionModel;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout.Orientation;
import com.vaadin.flow.data.provider.DataProvider;


class ObservableSelectionModel<T>
    extends ForwardingSet<T>
    implements ObservableSet<T>
{
    protected GridMultiSelectionModel<T> model;
//    propertyGridSelectionModel.addSelectionListener(ev -> {
//    	ev.getAllSelectedItems()
//    });

    public ObservableSelectionModel(GridMultiSelectionModel<T> model) {
        // super(model.getSelectedItems());
        this.model = model;
    }

    @Override
    public Runnable addVetoableChangeListener(VetoableChangeListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Registration addPropertyChangeListener(PropertyChangeListener listener) {
        com.vaadin.flow.shared.Registration x = model.addSelectionListener(ev -> {
            listener.propertyChange(new CollectionChangedEventImpl<Set<T>>(
                    this, model.getSelectedItems(), ev.getAllSelectedItems(),
                    null, null, null));
        });
        return Registration.from(x::remove, null);
    }

    @Override
    public boolean delta(Collection<? extends T> additions, Collection<?> removals) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected Set<T> delegate() {
        return model.getSelectedItems();
    }
}

public class ResourceEditor
    extends VerticalLayout
{
    protected UnaryRelation subjectConcept;
    protected UnaryRelation graphConcept;

    protected ObservableValue<List<Path>> visibleProperties = ObservableValueImpl.create(SetUniqueList.setUniqueList(new ArrayList<>()));


    // When a grid's multi-select mode is enabled then it is not possible to reorder selected rows
    // Therefore we need to manage the set of properties linked to the form editor ourself
    // protected ObservableSet<Binding> selectedProperties = ObservableSetImpl.decorate(new HashSet<>());


    // protected RDFConnection conn;
    protected RdfDataSource rdfDataSource;

    protected Grid<Binding> propertyGrid;
    protected HeaderRow propertyGridHeaderRow;
    protected HeaderRow propertyGridFilterRow;
    protected Binding draggedProperty = null;
    protected List<Binding> availableProperties = new ArrayList<>();


    protected Grid<Binding> resourceGrid;
    protected HeaderRow resourceGridHeaderRow;
    protected HeaderRow resourceGridFilterRow;

    protected GraphChange graphEditorModel = new GraphChange();

    // protected LabelMgr<Node, String> labelMgr;

    public ResourceEditor() {
        setSizeFull();


        List<Path> paths = visibleProperties.get();
        paths.add(PathUtils.createStep(RDF.type.asNode(), true));
        paths.add(PathUtils.createStep(DCAT.downloadURL.asNode(), true));

        Dataset dataset = RDFDataMgr.loadDataset("linkedgeodata-2018-04-04.dcat.ttl");
        RdfDataSource rdfDataSource = RdfDataEngineFromDataset.create(dataset, true);

        QueryExecutionFactory qef = new QueryExecutionFactoryDataset(dataset); // RDFConnection.connect(dataset);


        VaadinLabelMgr<Node, String> labelService = new VaadinLabelMgr<>(LabelUtils.getLabelLookupService(
                qef, DCTerms.description, DefaultPrefixes.get()));

        // labelMgr =

        Set<Node> nodes = new LinkedHashSet<>(Arrays.asList(NodeFactory.createURI("http://dcat.linkedgeodata.org/dataset/osm-bremen-2018-04-04")));
        // subjectConcept = ConceptUtils.createConcept(nodes);
        subjectConcept = ConceptUtils.createSubjectConcept();

        DataRetriever dataRetriever = new DataRetriever(rdfDataSource);

        // QueryExecutionFactoryRangeCache qef = QueryExecutionFactoryRangeCache.create(null, null, 0, null)


        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setSizeFull();
        splitLayout.setOrientation(Orientation.HORIZONTAL);

        propertyGrid = new Grid<>();
        propertyGrid.setSelectionMode(SelectionMode.MULTI);
        propertyGridHeaderRow = propertyGrid.appendHeaderRow();
        propertyGridFilterRow = propertyGrid.appendHeaderRow();
        propertyGrid.setRowsDraggable(true);
        GridMultiSelectionModel<Binding> propertyGridSelectionModel = (GridMultiSelectionModel<Binding>)propertyGrid.getSelectionModel();
        propertyGridSelectionModel.setSelectAllCheckboxVisibility(GridMultiSelectionModel.SelectAllCheckboxVisibility.VISIBLE);



        ObservableSelectionModel<Binding> selectedProperties = new ObservableSelectionModel<>(propertyGridSelectionModel);

        propertyGrid.addDragStartListener(event -> {
            // store current dragged item so we know what to drop
            List<Binding> draggedItems = event.getDraggedItems();
            if (draggedItems.size() > 1) {
                // This seems to be a dumb limitation of vaadin: We can not drag individual rows if multiple ones are selected.
                NotificationUtils.error("Please temporarily deselect the row you wish to drag or drag a non-selected row. A framework limitation prevents dragging of individual selected rows.");
            } else if (draggedItems.size() == 1){
                draggedProperty = draggedItems.get(0);
                propertyGrid.setDropMode(GridDropMode.BETWEEN);
            }
        });

        propertyGrid.addDragEndListener(event -> {
            draggedProperty = null;
            // Once dragging has ended, disable drop mode so that
            // it won't look like other dragged items can be dropped
            propertyGrid.setDropMode(null);
        });

        propertyGrid.addDropListener(event -> {
            Binding dropOverItem = event.getDropTargetItem().get();
            if (!dropOverItem.equals(draggedProperty)) {
                // reorder dragged item the backing gridItems container
                availableProperties.remove(draggedProperty);
                // calculate drop index based on the dropOverItem
                int dropIndex =
                        availableProperties.indexOf(dropOverItem) + (event.getDropLocation() == GridDropLocation.BELOW ? 1 : 0);
                availableProperties.add(dropIndex, draggedProperty);
                propertyGrid.getDataProvider().refreshAll();

                // Set<Binding> selectedProperties = propertyGrid.getSelectionModel().getSelectedItems();

                List<Path> orderedPaths = SetUniqueList.setUniqueList(availableProperties.stream()
                        .filter(selectedProperties::contains)
                        .map(b -> PathUtils.createStep(b.get(Vars.p), !NodeValue.FALSE.asNode().equals(b.get(Vars.d))))
                        .collect(Collectors.toList()));


                visibleProperties.set(orderedPaths);
            }
        });


        resourceGrid = new Grid<>();

        // Rendering resources is expensive - therefore only fetching small amounts of them
        resourceGrid.setPageSize(10);
        resourceGridHeaderRow = resourceGrid.appendHeaderRow();
        resourceGridFilterRow = resourceGrid.appendHeaderRow();


        Breadcrumb<P_Path0> breadcrumb = new Breadcrumb<>(PathOpsPP.get().newRoot(), labelService, Breadcrumb.labelAssemblerForPath0());
//        breadcrumb.getModel().set(PathOpsPP.get().newRoot()
//                .resolve(new P_Link(RDF.Nodes.type))
//                .resolve(new P_Link(RDFS.Nodes.label))$
//                );

        breadcrumb.addPathListener(ev -> {
            Notification.show("Clicked: " + ev.getPath());
        });



        Button addResourceBtn = new Button(VaadinIcon.PLUS.create());
        // addResourceBtn.add

        VerticalLayout resourcePanel = new VerticalLayout();
        resourcePanel.setSizeFull();
        resourcePanel.add(breadcrumb);
        resourcePanel.add(addResourceBtn);
        resourcePanel.add(resourceGrid);

        Button addPropertyButton = new Button(VaadinIcon.PLUS.create());
        addPropertyButton.addClickListener(ev -> {
            ConfirmDialogUtils.confirmInputDialog("Add Property", null, "Add Property",
                    iri -> {
                        if (iri == null || iri.isEmpty()) {
                            NotificationUtils.error("Ignoring empty IRI");
                        } else {
                            availableProperties.add(BindingFactory.binding(Vars.p, NodeFactory.createURI(iri), Vars.d, NodeValue.TRUE.asNode()));
                            propertyGrid.getDataProvider().refreshAll();
                        }
                    }, "Cancel", null).open();
        });



        splitLayout.addToPrimary(addPropertyButton, propertyGrid);
        // splitLayout.addToPrimary(propertyGrid);
        splitLayout.addToSecondary(resourcePanel);

        splitLayout.setSizeFull();
        resourceGrid.setSizeFull();
        propertyGrid.setSizeFull();


        resourceGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        // propertyGrid.setSelectionMode(SelectionMode.SINGLE);
        add(splitLayout);

        List<Var> propertyTableVars = Arrays.asList(Vars.p, Vars.d);

        // Query propertyQuery = QueryFactory.create("SELECT ?p ?type ?c { { SElECT ?p (COUNT(DISTINCT ?o) AS ?c) { ?this ?p ?o } GROUP BY ?p } BIND(<urn:fwd> AS ?type) }");
        // Query propertyQuery = QueryFactory.create("Select ?p ?d { }");
        DataProviderSparqlBinding propertyDataProvider = DataProviderSparqlBinding.create(propertyTableVars);


        // QueryExecutionFactoryQuery qef = q -> QueryExecutionFactory.create(q, dataset);

//        Dataset emptyDataset = DatasetFactory.empty();
//        QueryExecutionFactoryQuery propertyQef = q -> QueryExecutionFactory.create(q, emptyDataset);

        setQueryForGridBinding(propertyGrid, propertyGridHeaderRow, propertyDataProvider, labelService);
        VaadinSparqlUtils.configureGridFilter(propertyGrid, propertyGridFilterRow, propertyTableVars,
                var -> str -> VaadinSparqlUtils.createFilterExpr(var, str).orElse(null));

        // propertyGrid.setDataProvider(propertyDataProvider);

        // propertyGrid.setSelectionMode(SelectionMode.NONE);



        // Query resourceQuery = QueryFactory.create("SELECT ?s { { SELECT DISTINCT ?s { ?s ?p ?o } LIMIT 5 } }");
        Query resourceQuery = QueryFactory.create("SELECT ?o { BIND(<http://dcat.linkedgeodata.org/dataset/osm-bremen-2018-04-04> AS ?o) }");
        UnaryRelation resourceRelation = RelationUtils.fromQuery(resourceQuery).toUnaryRelation();


        Relation propertyRelation = pathToRelation(breadcrumb.getModel().get());



        // TODO replace with a guava cache
        Map<Node, ResourceInfo> globalResourceState = new HashMap<>();
        // propertyGrid.getSelectionModel();

        DataProviderSparqlBase<Binding> resourceDataProvider = new DataProviderSparqlBinding(resourceRelation, qef) {

            @Override
            public java.util.stream.Stream<Binding> fetch(com.vaadin.flow.data.provider.Query<Binding,Expr> t) {
                List<Binding> bindings;
                try (Stream<Binding> stream = super.fetch(t)){
                    bindings = stream.collect(Collectors.toList());
                }

                Var v = relation.getVars().get(0);
                Set<Node> nodes = bindings.stream().map(b -> b.get(v)).collect(Collectors.toSet());

                Map<Node, ResourceInfo> map = dataRetriever.fetch(nodes, PolaritySet.create(false));
                globalResourceState.putAll(map);
                System.out.println("Mapit: " + map);

                Set<Path> paths = map.values().stream().flatMap(ri -> ri.getKnownPaths().stream())
                        .collect(Collectors.toSet());

                // visibleProperties.set(paths);

                List<Binding> ps = new ArrayList<>();
                for (Path path : paths) {
                    P_Path0 p0 = PathUtils.asStep(path);
                    if (p0 != null) {
                        Binding b = BindingFactory.binding(Vars.p, p0.getNode(), Vars.d, NodeValue.makeBoolean(p0.isForward()).asNode());
                        ps.add(b);
                    }
                }

                availableProperties = ps;

                System.out.println("Derived properties: " + availableProperties);
//                ElementData elt = new ElementData(propertyTableVars, ps);
//                Relation rel = new RelationImpl(elt, propertyTableVars);
//                propertyDataProvider.setRelation(rel);
                propertyDataProvider.setRelation(propertyTableVars, availableProperties);

                return bindings.stream();
            };

        };


        breadcrumb.addPathListener(ev -> {
            org.aksw.commons.path.core.Path<P_Path0> pp = ev.getPath();
            BinaryRelation rel = pathToRelation(pp);
            UnaryRelation newRel =
                    rel.prependOn(rel.getSourceVar()).with(resourceRelation)
                    .project(rel.getTargetVar()).toUnaryRelation();
            // UnaryRelation newRel = resourceRelation.join().with(rel).toUnaryRelation();

            System.out.println("Combined concept: " + newRel);
            resourceDataProvider.setRelation(newRel);
            resourceDataProvider.refreshAll();
        });

        // resourceDataProvider.fi;

        resourceGrid.addComponentColumn(binding -> {
            System.out.println("Binding: " + binding);
            Node node = binding.get(Vars.o);
            ResourceInfo resourceInfo = globalResourceState.get(node);
            return new ResourceItem(resourceInfo, graphEditorModel, visibleProperties, breadcrumb.getModel(), labelService);
        });
        resourceGrid.setDataProvider(resourceDataProvider);
        // resourceGrid.setDa
        // VaadinSparqlUtils.setQueryForGridBinding(resourceGrid, resourceGridHeaderRow, qef, resourceQuery);
        // VaadinSparqlUtils.configureGridFilter(resourceGrid, resourceGridFilterRow, resourceQuery.getProjectVars(), var -> str -> VaadinSparqlUtils.createFilterExpr(var, str).orElse(null));



        //propertyGrid.getSelectionModel().addSelectionListener(sel -> { sel.getAllSelectedItems()
        selectedProperties.addPropertyChangeListener(ev -> {

            Set<Path> selPaths = selectedProperties.stream()
                    .map(b -> PathUtils.createStep(b.get(Vars.p), !NodeValue.FALSE.asNode().equals(b.get(Vars.d))))
                .collect(Collectors.toSet());

            // System.out.println("Selected paths: " + selPaths);
            List<Path> orderedPaths = SetUniqueList.setUniqueList(availableProperties.stream()
                    .map(b -> PathUtils.createStep(b.get(Vars.p), !NodeValue.FALSE.asNode().equals(b.get(Vars.d))))
                    .filter(selPaths::contains).collect(Collectors.toList()));
            visibleProperties.set(orderedPaths);

            VaadinComponentUtils.notifyResize(resourceGrid, false);

            // resourceGrid.r
        });


        Button submitBtn = new Button("Submit");
        submitBtn.addClickListener(ev -> {
            ConfirmDialog dialog = ConfirmDialogUtils.confirmDialog("Confirm Submit",
                    "" + graphEditorModel.toUpdateRequest(),
                    "Ok",
                    x -> {
                    },
                    "Cancel",
                    x -> {
                    });
            //dialog.setConfirmButtonTheme("error primary");
            dialog.setWidthFull();
            dialog.open();

        });
        resourcePanel.add(submitBtn);


    }

    public static BinaryRelation pathToRelation(org.aksw.commons.path.core.Path<P_Path0> path) {
        List<P_Path0> segments = path.getSegments();
        BinaryRelation e;
        if (segments.isEmpty()) {
            e = BinaryRelationImpl.empty(Vars.o);
        } else {
            Path pp = SimplePath.toPropertyPath(segments);
            e = BinaryRelationImpl.create(pp);
        }

        return e;
    }

    protected void syncPropertiesWithView(Relation relation, int offset, int limit) {

    }


    public void refresh() {
    }



    /** Configure a grid to be backed by the given data provider for sparql bindings -
     * thereby rendering the binding values using the given labelService */
    public void setQueryForGridBinding(
            Grid<Binding> grid,
            HeaderRow headerRow,
            DataProviderSparqlBinding dataProviderCore,
            VaadinLabelMgr<Node, String> labelService) {

        DataProvider<Binding, Expr> dataProvider = dataProviderCore
                .withConfigurableFilter((Expr e1, Expr e2) -> ExprUtils.andifyBalanced(
                        Arrays.asList(e1, e2).stream().filter(Objects::nonNull).collect(Collectors.toList()
                )));


        grid.setDataProvider(dataProvider);
        List<Var> vars = dataProviderCore.getRelation().getVars();
        grid.removeAllColumns();


//        grid.addComponentColumn(binding -> {
//            Checkbox cb = new Checkbox();
//            VaadinBindUtils.bind(cb, selectedProperties.fieldForPresence(binding));
//            // selectedProperties.addPropertyChangeListener(null)r;
//
//            return cb;
//        });

        for (Var var : vars) {
            Column<Binding> column = grid.addComponentColumn(binding -> {
                Node node = binding.get(var);
                Object r;
                if (node == null) {
                    r = null;
                } else {

                    r = node.toString(false);
                }
//                } else if (node.isLiteral()) {
//                    r = node.getLiteralValue();
//                } else {
//                    r = node.toString();
//                }

                // Component result = new Span("" + node);
                // Component result = labelService.forHasText(new Span("" + node), node);
                Component result = labelService.forHasText(new Span(), node);
                return result;
                // return r;
            }); //.setHeader(var.getName());

            headerRow.getCell(column).setText(var.getName());

            column.setKey(var.getName());
            column.setResizable(true);
            column.setSortable(true);
        }
    }

}
