package org.aksw.jenax.vaadin.component.grid.sparql;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.aksw.commons.collections.generator.Generator;
import org.aksw.commons.collections.generator.GeneratorBlacklist;
import org.aksw.commons.collections.generator.GeneratorFromFunction;
import org.aksw.facete.v3.api.Direction;
import org.aksw.facete.v3.api.FacetDirNode;
import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete.v3.api.FacetedDataQuery;
import org.aksw.facete.v3.api.FacetedQuery;
import org.aksw.facete.v4.impl.FacetedRelationQuery;
import org.aksw.facete.v4.impl.PropertyResolverImpl;
import org.aksw.jena_sparql_api.concepts.RelationUtils;
import org.aksw.jena_sparql_api.vaadin.data.provider.DataProviderSparqlRdfNode;
import org.aksw.jena_sparql_api.vaadin.util.VaadinSparqlUtils;
import org.aksw.jenax.arq.connection.core.QueryExecutionFactories;
import org.aksw.jenax.arq.connection.core.RDFConnections;
import org.aksw.jenax.arq.util.node.NodeUtils;
import org.aksw.jenax.connection.query.QueryExecutionFactoryQuery;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetStep;
import org.aksw.jenax.sparql.relation.api.Relation;
import org.aksw.jenax.sparql.relation.api.UnaryRelation;
import org.aksw.jenax.vaadin.label.LabelService;
import org.aksw.jenax.vaadin.label.VaadinLabelMgr;
import org.aksw.vaadin.common.component.tab.TabSheet;
import org.aksw.vaadin.common.component.util.NotificationUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.vaadin.addons.componentfactory.PaperSlider;

import com.google.common.math.LongMath;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;

/**
 * Options for how to derive a set of predicates from a given set of subjects.
 *
 * Specifically:
 * <ul>
 *   <li>Forward/Backward property switch</li>
 *   <li>Sample options for how to derive a set of properties</li>
 *   <li>Filter over the property name</li>
 *   <li>Pagination</li>
 *   <li>Declaration of virtual predicates at this point in the tree</li>
 * </ul>
 *
 */
public class TableMapperDetailsView
    extends VerticalLayout
{
    protected LabelService<Node, String> labelMgr;

    protected TreeDataProvider<FacetPath> treeDataProvider;

    // protected RdfDataSource dataSource;
    protected QueryExecutionFactoryQuery qef;
    protected UnaryRelation baseConcept;
    // protected FacetTreeModel model;
    protected FacetPath activePath;

    protected Button removePathBtn = new Button(VaadinIcon.TRASH.create());

    /** The grid of values of the selected property */
    protected Grid<RDFNode> valueGrid = new Grid<>();


    protected Grid<RDFNode> predicateValuesGrid = new Grid<>();

    protected Grid<PredicateRecord> predicateGrid = new Grid<>();

    protected Grid<Binding> functionsGrid = new Grid<>();
    protected Grid<Binding> virtualPropertiesGrid = new Grid<>();

    public FacetPath getActivePath() {
        return activePath;
    }

    public void setActivePath(FacetPath activePath) {
        this.activePath = activePath;
    }

    private static AtomicInteger debugCounter = new AtomicInteger();

    public void refresh() {
        title.setText("(none)");

        // QueryExecutionFactoryQuery qef = QueryExecutionFactories.of(dataSource);

        // FacetedQuery fq = FacetedQueryImpl.create(null).baseConcept(model.getBaseConcept());
        FacetedRelationQuery frq = FacetedRelationQuery.of(baseConcept);
        FacetedQuery fq = frq.getFacetedQuery();


        FacetNode fn = fq.root().traverse(activePath);
        UnaryRelation rel = fn.availableValues().baseRelation().toUnaryRelation();
        Query query = rel.toQuery();
        query.setDistinct(true);

        // VaadinSparqlUtils.setQueryForGridRdfNode(valueGrid, qef, query, RDFNode.class, null, null);
        Relation relation = RelationUtils.fromQuery(query);
        String varName = relation.toUnaryRelation().getVar().getName();
        DataProviderSparqlRdfNode<RDFNode> dataProvider = new DataProviderSparqlRdfNode<>(relation, qef, RDFNode.class, varName, null);
        dataProvider.setAlwaysDistinct(true);

        valueGrid.setDataProvider(dataProvider);
        valueGrid.removeAllColumns();
          Column<RDFNode> column = valueGrid.addComponentColumn(rdfNode -> {
              Span r = new Span("" + debugCounter.getAndIncrement());
              VaadinLabelMgr.forHasText(labelMgr, r, rdfNode.asNode());
              // labelMgr.forHasText(r, rdfNode.asNode());
              return r;
            }); //.setHeader(varName);

        column.setKey(varName);
        column.setResizable(true);
        column.setSortable(true);


        {
            predicateValuesGrid.setDataProvider(dataProvider);
            predicateValuesGrid.removeAllColumns();
              Column<RDFNode> xcolumn = predicateValuesGrid.addComponentColumn(rdfNode -> {
                  Span r = new Span("" + debugCounter.getAndIncrement());
                  VaadinLabelMgr.forHasText(labelMgr, r, rdfNode.asNode());
                  // labelMgr.forHasText(r, rdfNode.asNode());
                  return r;
                }); //.setHeader(varName);

            xcolumn.setKey(varName);
            xcolumn.setResizable(true);
            xcolumn.setSortable(true);
        }

        // title.setText(activePath.toString() + " - " + query);
        TableMapperComponent.labelForAliasedPath(labelMgr, title, activePath);

        // InMemoryDataProvider<RDFNode> predicateDataProvider = new ListDataProvider<>();

        predicateGrid.setDataProvider(new ListDataProvider<>(Collections.emptyList()));

        fetchPredicates();
    }

    protected Span title = new Span();

    protected Checkbox isReverseToggle;
    // TODO Controls for sampling the set of resources for which to derive the predicates
    protected TextField filterField;


    protected PaperSlider scanOffsetSlider;
    protected PaperSlider scanLimitSlider;
    protected PaperSlider entityOffsetSlider;
    protected PaperSlider entityLimitSlider;


    public static PaperSlider createSlider(String label, int min, int max, int markers, int value) {
        PaperSlider slider = new PaperSlider(label);
        slider.setMin(min);
        slider.setMax(max);
        slider.setMaxMarkers(markers);
        slider.setValue(value);
        slider.setSnaps(true);
        slider.setPinned(true);

        // slider.setWidthFull();

        return slider;
    }


    public static Long tickToAmount(Integer value) {
        Long result = value == null || value.intValue() == 0 ? null : LongMath.pow(10, value);
        return result;
    }

    public SampleRange getSampleRange() {
        Long scanOffset = tickToAmount(scanOffsetSlider.getValue());
        Long scanLimit = tickToAmount(scanLimitSlider.getValue());
        Long entityOffset = tickToAmount(entityOffsetSlider.getValue());
        Long entityLimit = tickToAmount(entityLimitSlider.getValue());
        return new SampleRange(scanOffset, scanLimit, entityOffset, entityLimit);
    }


    // TODO Slider for which predicates to retrieve

    public TableMapperDetailsView(LabelService<Node, String> labelMgr, QueryExecutionFactoryQuery qef, UnaryRelation baseConcept, TreeDataProvider<FacetPath> treeDataProvider) {
        super();
        this.labelMgr = labelMgr;
        this.qef = qef;
        this.baseConcept = baseConcept;
        this.treeDataProvider = treeDataProvider;

        TabSheet tabSheet = new TabSheet(Tabs.Orientation.VERTICAL);

        HorizontalLayout titleRow = new HorizontalLayout();
        titleRow.setAlignItems(FlexComponent.Alignment.CENTER);

        title = new Span();
        titleRow.add(title);

        VerticalLayout valuesLayout = new VerticalLayout();

        VerticalLayout predicatesLayout = new VerticalLayout();
        SplitLayout predicatesSplitLayout = new SplitLayout();

        isReverseToggle = new Checkbox();
        filterField = new TextField();


        removePathBtn.addClickListener(ev -> {
            TableMapperComponent.removePath(treeDataProvider, activePath, false);
//            List<FacetPath> children = treeDataProvider.getTreeData().getChildren(activePath);
//            Consumer<Object> action = x -> {
//                TreeData<FacetPath> treeData = treeDataProvider.getTreeData();
//                treeData.removeItem(activePath);
//                // Make sure to add a root if we deleted it
//                if (treeData.getRootItems().isEmpty()) {
//                    treeData.addRootItems(FacetPath.newAbsolutePath());
//                }
//                treeDataProvider.refreshAll();
//            };
//            if (children != null && children.size() >= 1) {
//                ConfirmDialogUtils.confirmDialog(
//                        "Remove path",
//                        "Removing this path removes all children. Proceed?",
//                        "Delete", action,
//                        "Cancel", null).open();
//            } else {
//                action.accept(null);
//            }
//            setActivePath(activePath.getParent());
//            refresh();
        });


        titleRow.add(removePathBtn);


        // valuesLayout.add(isReverseToggle);
        valuesLayout.add(valueGrid);


        /** Adds two columns for the predicates and values */
        Button addAnyBtn = new Button("Add ANY");
        addAnyBtn.addClickListener(ev -> {
            boolean isFwd = !Boolean.TRUE.equals(isReverseToggle.getValue());
            {
                FacetPath newPath = allocate(treeDataProvider.getTreeData(), activePath, NodeUtils.ANY_IRI, isFwd, FacetStep.PREDICATE);
                treeDataProvider.getTreeData().addItem(activePath, newPath);
            }
            {
                FacetPath newPath = allocate(treeDataProvider.getTreeData(), activePath, NodeUtils.ANY_IRI, isFwd, FacetStep.TARGET);
                treeDataProvider.getTreeData().addItem(activePath, newPath);
            }
            treeDataProvider.refreshAll();

        });
        predicatesLayout.add(addAnyBtn);


        HorizontalLayout samplerLayout = new HorizontalLayout();
        scanOffsetSlider = createSlider("Scan Offset", 0, 9, 9, 0);
        scanLimitSlider = createSlider("Scan Limit", 0, 9, 9, 6);
        entityOffsetSlider = createSlider("Entity Limit", 0, 9, 9, 0);
        entityLimitSlider = createSlider("Entity Offset", 0, 9, 9, 0);

        samplerLayout.add(scanOffsetSlider);
        samplerLayout.add(scanLimitSlider);
//        samplerLayout.add(entityOffsetSlider);
//        samplerLayout.add(entityLimitSlider);

        Button testSamplerBtn = new Button("test");
        testSamplerBtn.addClickListener(ev -> {
            NotificationUtils.error("" + getSampleRange());
        });
        samplerLayout.add(testSamplerBtn);
        predicatesLayout.add(samplerLayout);



        Column<?> predicateColumn = predicateGrid.addComponentColumn(predicateRecord -> {
            Node predicateNode = predicateRecord.predicate.asNode();
            Span label = new Span(predicateNode.toString());

            // labelMgr.forHasText(label, predicateNode);
            VaadinLabelMgr.forHasText(labelMgr, label, predicateNode);

            Button addInstanceBtn = new Button(VaadinIcon.PLUS_CIRCLE_O.create());
            addInstanceBtn.addClickListener(ev -> {
                // Allocate the next alias in the tree data
                FacetPath newPath = allocate(treeDataProvider.getTreeData(), predicateRecord.activePath, predicateRecord.predicate.asNode(), predicateRecord.isForward, FacetStep.TARGET);

                treeDataProvider.getTreeData().addItem(predicateRecord.activePath, newPath);
                treeDataProvider.refreshAll();
            });

//            VerticalLayout column = new VerticalLayout(label, addInstanceBtn);
//            column.getStyle().set("line-height", "var(--lumo-line-height-m)");
//            column.setPadding(false);
//            column.setSpacing(false);
//
            HorizontalLayout row = new HorizontalLayout(); //avatar, column);
            row.setAlignItems(FlexComponent.Alignment.CENTER);
            row.setSpacing(true);
            // row.add(column);
            row.add(label, addInstanceBtn);
            return row;
        }).setHeader("Predicate");

        HeaderRow predicateFilterRow = predicateGrid.appendHeaderRow();

        predicatesLayout.add(predicateGrid);

        TextField predicateSearch = new TextField();
        predicateSearch.setPlaceholder("Search...");
        predicateSearch.setValueChangeMode(ValueChangeMode.TIMEOUT);
        predicateFilterRow.getCell(predicateColumn).setComponent(predicateSearch);


        FormLayout form = new FormLayout();
        form.addFormItem(isReverseToggle, "show reverse predicates");
        form.addFormItem(filterField, "Filter predicates");

        predicatesLayout.add(form);

        Button samplePredicatesBtn = new Button("Sample Predicates");

        samplePredicatesBtn.addClickListener(ev -> {
            fetchPredicates();
        });

        predicatesLayout.add(samplePredicatesBtn);

        predicatesSplitLayout.addToPrimary(predicatesLayout);

        // Functions
        // Streams.stream(FunctionRegistry.get().keys()).collect(Collectors.toList());
        // RelationUtils.creat
        // new DataProviderSparqlBinding(null, qef);

        VerticalLayout functionsLayout = new VerticalLayout();
        {
            Query query = QueryFactory.create("SELECT * { ?function <http://jsa.aksw.org/fn/sys/listFunctions> () }");

            // QueryExecutionFactoryQuery fnQef = QueryExecutionFactories.of(RdfDataEngines.of(DatasetFactory.empty()));
            // HeaderRow headerRow = functionsGrid.appendHeaderRow();
            // VaadinSparqlUtils.setQueryForGridBinding(functionsGrid, headerRow, qef, query);
            functionsGrid.setDataProvider(VaadinSparqlUtils.createDataProvider(qef, query));
            Var fnVar = Var.alloc("function");
            functionsGrid.addComponentColumn(binding -> {

                Node predicateNode = binding.get(fnVar);
                Span label = new Span(predicateNode.toString());

                // labelMgr.forHasText(label, predicateNode);
                VaadinLabelMgr.forHasText(labelMgr, label, predicateNode);

                Button addInstanceBtn = new Button(VaadinIcon.PLUS_CIRCLE_O.create());
                addInstanceBtn.addClickListener(ev -> {
                    if (activePath != null) {
                        Node node = NodeFactory.createURI("fn:" + predicateNode.getURI());
                        // Allocate the next alias in the tree data
                        FacetPath newPath = allocate(treeDataProvider.getTreeData(), activePath, node, true, FacetStep.TARGET);

                        treeDataProvider.getTreeData().addItem(activePath, newPath);
                        treeDataProvider.refreshAll();
                    }
                });

//                VerticalLayout column = new VerticalLayout(label, addInstanceBtn);
//                column.getStyle().set("line-height", "var(--lumo-line-height-m)");
//                column.setPadding(false);
//                column.setSpacing(false);
    //
                HorizontalLayout row = new HorizontalLayout(); //avatar, column);
                row.setAlignItems(FlexComponent.Alignment.CENTER);
                row.setSpacing(true);
                // row.add(column);
                row.add(label, addInstanceBtn);
                return row;
            })
            .setHeader(fnVar.getName())
            .setKey(fnVar.getName()); // Column key must match the var name for configureGridFilter

            HeaderRow filterRow = functionsGrid.appendHeaderRow();
            VaadinSparqlUtils.configureGridFilter(functionsGrid, filterRow, query.getProjectVars(), var -> str -> VaadinSparqlUtils.createFilterExpr(var, str).orElse(null));
            functionsLayout.add(functionsGrid);

        }


        // Custom Predicates
        VerticalLayout virtualPropertiesLayout = new VerticalLayout();
        {
            Query query = QueryFactory.create("SELECT ?customIri ?definition { ?customIri <https://w3id.org/aksw/norse#sparqlElement> ?definition }");

            // QueryExecutionFactoryQuery fnQef = QueryExecutionFactories.of(RdfDataEngines.of(DatasetFactory.empty()));
            // HeaderRow headerRow = functionsGrid.appendHeaderRow();
            // VaadinSparqlUtils.setQueryForGridBinding(functionsGrid, headerRow, qef, query);
            virtualPropertiesGrid.setDataProvider(VaadinSparqlUtils.createDataProvider(QueryExecutionFactories.of(PropertyResolverImpl.virtualProperties), query));
            Var iriVar = Var.alloc("customIri");
            virtualPropertiesGrid.addComponentColumn(binding -> {

                Node iriNode = binding.get(iriVar);
                Span label = new Span(iriNode.toString());

                // labelMgr.forHasText(label, predicateNode);
                VaadinLabelMgr.forHasText(labelMgr, label, iriNode);

                Button addInstanceBtn = new Button(VaadinIcon.PLUS_CIRCLE_O.create());
                addInstanceBtn.addClickListener(ev -> {
                    if (activePath != null) {
                        // Allocate the next alias in the tree data
                        FacetPath newPath = allocate(treeDataProvider.getTreeData(), activePath, iriNode, true, FacetStep.TARGET);

                        treeDataProvider.getTreeData().addItem(activePath, newPath);
                        treeDataProvider.refreshAll();
                    }
                });

//                VerticalLayout column = new VerticalLayout(label, addInstanceBtn);
//                column.getStyle().set("line-height", "var(--lumo-line-height-m)");
//                column.setPadding(false);
//                column.setSpacing(false);
    //
                HorizontalLayout row = new HorizontalLayout(); //avatar, column);
                row.setAlignItems(FlexComponent.Alignment.CENTER);
                row.setSpacing(true);
                // row.add(column);
                row.add(label, addInstanceBtn);
                return row;
            })
            .setHeader(iriVar.getName())
            .setKey(iriVar.getName()); // Column key must match the var name for configureGridFilter

            HeaderRow filterRow = virtualPropertiesGrid.appendHeaderRow();
            VaadinSparqlUtils.configureGridFilter(virtualPropertiesGrid, filterRow, Collections.singleton(iriVar), var -> str -> VaadinSparqlUtils.createFilterExpr(var, str).orElse(null));
            virtualPropertiesLayout.add(virtualPropertiesGrid);
        }




        Component predicatesIcon = VaadinIcon.MENU.create();
        Component valuesIcon = VaadinIcon.TEXT_LABEL.create();
        Component functionsIcon = VaadinIcon.FUNCION.create();
        Component customPredicatesIcon = VaadinIcon.PLUS_CIRCLE.create();
        tabSheet.add(predicatesIcon, predicatesSplitLayout);
        tabSheet.add(valuesIcon, valuesLayout);
        tabSheet.add(functionsIcon, functionsLayout);
        tabSheet.add(customPredicatesIcon, virtualPropertiesLayout);

        // VaadinComponentUtils.notifyResize(tabSheet, true);

        add(titleRow);

        add(tabSheet);
    }

    public void fetchPredicates() {

        // Bridge qef to conn
        RDFConnection conn = RDFConnections.of(qef);

        try {
            // FacetedQuery fq = FacetedQueryImpl.create(conn).baseConcept(model.getBaseConcept());
            FacetedRelationQuery frq = FacetedRelationQuery.of(baseConcept);
            FacetedQuery fq = frq.getFacetedQuery();
            fq.connection(conn);
            FacetNode fn = fq.root().traverse(activePath);
            boolean isForward = !Boolean.TRUE.equals(isReverseToggle.getValue());
            Direction dir = Direction.ofFwd(isForward);
            FacetDirNode fdn = fn.step(dir);
            FacetedDataQuery<RDFNode> facetsProvider = fdn.facets();
            List<RDFNode> facets = facetsProvider.exec().toList().blockingGet();

            List<PredicateRecord> list = facets.stream().map(facet -> {
                return new PredicateRecord(activePath, isForward, facet);
            }).collect(Collectors.toList());

            predicateGrid.setDataProvider(new ListDataProvider<>(list));
        } finally {
            conn.close();
        }
    }

//    public static TreeData<PathPPA> resolve(TreeData<PathPPA> start, PathPPA path) {
//    	start.get
//    }

    public static FacetPath allocate(TreeData<FacetPath> treeData, FacetPath parent, Node predicate, boolean isForward, Node targetComponent) {
        List<FacetPath> children = treeData.getChildren(parent);

        // Collect all taken aliases
        Set<String> usedAliases = children.stream()
                .map(item -> item.getFileName().toSegment())
                .filter(step -> step.getNode().equals(predicate) && step.isForward() == isForward && Objects.equals(step.getTargetComponent(), targetComponent))
                .map(FacetStep::getAlias)
                .collect(Collectors.toSet());

        Generator<String> aliasGen =
                GeneratorBlacklist.create(
                    GeneratorFromFunction.createInt().map(i -> i == 0 ? null : Integer.toString(i)),
                    usedAliases);
        String nextAlias = aliasGen.next();
        FacetPath result = parent.resolve(new FacetStep(predicate, isForward, nextAlias, targetComponent));
        return result;
    }
}
