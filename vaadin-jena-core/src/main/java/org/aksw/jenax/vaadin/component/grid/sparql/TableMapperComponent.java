package org.aksw.jenax.vaadin.component.grid.sparql;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.collections.generator.Generator;
import org.aksw.commons.collections.generator.GeneratorBlacklist;
import org.aksw.commons.collections.generator.GeneratorFromFunction;
import org.aksw.commons.rx.lookup.LookupService;
import org.aksw.facete.v3.api.Direction;
import org.aksw.facete.v3.api.FacetDirNode;
import org.aksw.facete.v3.api.FacetMultiNode;
import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete.v3.api.FacetedDataQuery;
import org.aksw.facete.v3.api.FacetedQuery;
import org.aksw.facete.v3.impl.FacetedQueryImpl;
import org.aksw.facete.v4.impl.ElementGenerator;
import org.aksw.jena_sparql_api.common.DefaultPrefixes;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.RelationUtils;
import org.aksw.jena_sparql_api.vaadin.data.provider.DataProviderSparqlBinding;
import org.aksw.jena_sparql_api.vaadin.data.provider.DataProviderSparqlRdfNode;
import org.aksw.jenax.arq.connection.core.QueryExecutionFactories;
import org.aksw.jenax.arq.connection.core.RDFConnections;
import org.aksw.jenax.arq.dataset.api.ResourceInDataset;
import org.aksw.jenax.arq.datasource.RdfDataSourceWithBnodeRewrite;
import org.aksw.jenax.arq.util.node.NodeUtils;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.connection.datasource.RdfDataSource;
import org.aksw.jenax.connection.query.QueryExecutionFactoryQuery;
import org.aksw.jenax.dataaccess.LabelUtils;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetPathOps;
import org.aksw.jenax.path.core.FacetStep;
import org.aksw.jenax.path.core.PathPP;
import org.aksw.jenax.sparql.relation.api.Relation;
import org.aksw.jenax.sparql.relation.api.UnaryRelation;
import org.aksw.jenax.vaadin.label.LabelService;
import org.aksw.jenax.vaadin.label.VaadinLabelMgr;
import org.aksw.jenax.vaadin.label.VaadinRdfLabelMgrImpl;
import org.aksw.vaadin.common.component.tab.TabSheet;
import org.aksw.vaadin.common.component.util.ConfirmDialogUtils;
import org.aksw.vaadin.common.component.util.NotificationUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.vocabulary.RDFS;
import org.vaadin.addons.componentfactory.PaperSlider;

import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.math.LongMath;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.HeaderRow.HeaderCell;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;

import io.reactivex.rxjava3.core.Flowable;

/** A predicate tree's node types alternate between 'subject' and 'predicate'. The root is always a subject node. */
//class PredicateNode
//{
//    protected UnaryRelation specification;
//
//    public PredicateNode(UnaryRelation specification) {
//        super();
//        this.specification = specification;
//    }
//
//    public UnaryRelation getSpecification() {
//        return specification;
//    }
//}

class FacetTreeModel {
    /** Specification of the initial set of nodes */
    protected UnaryRelation baseConcept;

    // Clonable prototoype
    protected FacetNodeModel defaultNodeModel;

    protected Map<PathPP, FacetNodeModel> pathToConfig = new HashMap<>();

    protected Map<PathPP, RuntimeModel> pathToData = new HashMap<>();

    public FacetTreeModel(UnaryRelation baseConcept) {
        super();
        this.baseConcept = baseConcept;
    }

    public UnaryRelation getBaseConcept() {
        return baseConcept;
    }

    public FacetNodeModel getOrCreateConfig(PathPP path) {
        FacetNodeModel result = pathToConfig.get(path);
        if (result == null) {
            // Ensure that all parents are created
            getOrCreateConfig(path.getParent());
        }

        result = (FacetNodeModel)defaultNodeModel.clone();
        pathToConfig.put(path, result);
        return result;
    }

    public RuntimeModel getOrCreateData(PathPP path) {
        RuntimeModel result = pathToData.get(path);
        if (result == null) {
            // Ensure that all parents are created
            getOrCreateConfig(path.getParent());
        }

        result = new RuntimeModel();

        pathToData.put(path, result);
        return result;
    }
}

class RuntimeModel {
    protected Set<ResourceInDataset> foundPredicates = new HashSet<>();

    public RuntimeModel() {
        super();
    }

    public Set<ResourceInDataset> getFoundPredicates() {
        return foundPredicates;
    }
}

class FacetNodeModel
    implements Cloneable
{
    // protected PathPP
    // protected PredicateNode parent;
    // protected Node predicate;
    // protected boolean isForward;
    protected String alias;

    // Filters the targets of this predicate
    // If this node is the root then it specifies the initial set of resources
    protected UnaryRelation filter;

    // protected Range<Long> slice;
    protected Long offset;
    protected Long limit;

    public FacetNodeModel(String alias, UnaryRelation filter, Long offset, Long limit) {
        super();
        this.alias = alias;
        this.filter = filter;
        this.offset = offset;
        this.limit = limit;
    }

    @Override
    public Object clone(){
        return new FacetNodeModel(alias, filter, offset, limit);
    }

}

interface Sampler<I, O> {
    Stream<O> sample(I spec);
}



class NestedGrid
    extends VerticalLayout
{
    protected TreeDataProvider<FacetPath> schema;
    protected Grid<RDFNode> grid = new Grid<>();

    public NestedGrid() {
        add(grid);
    }



    public void refresh() {



    }
}





// Can we separate slice/order/projection from the predicateNode?
//class PredicateNodeState {
//	PredicateNode predicateNode;
//}


class PredicateRecord {
    public FacetPath activePath;
    public boolean isForward;
    public RDFNode predicate;
    public PredicateRecord(FacetPath activePath, boolean isForward, RDFNode predicate) {
        super();
        this.activePath = activePath;
        this.isForward = isForward;
        this.predicate = predicate;
    }
}

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
class DetailsView
    extends VerticalLayout
{
    protected LabelService<Node, String> labelMgr;

    protected TreeDataProvider<FacetPath> treeDataProvider;

    // protected RdfDataSource dataSource;
    protected QueryExecutionFactoryQuery qef;
    protected FacetTreeModel model;
    protected FacetPath activePath;

    protected Button removePathBtn = new Button(VaadinIcon.TRASH.create());

    /** The grid of values of the selected property */
    protected Grid<RDFNode> valueGrid = new Grid<>();


    protected Grid<RDFNode> predicateValuesGrid = new Grid<>();

    protected Grid<PredicateRecord> predicateGrid = new Grid<>();

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

        FacetedQuery fq = FacetedQueryImpl.create(null).baseConcept(model.getBaseConcept());
        FacetNode fn = goTo(fq.root(), activePath);
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

    public DetailsView() {
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
        add(samplerLayout);

        removePathBtn.addClickListener(ev -> {
            List<FacetPath> children = treeDataProvider.getTreeData().getChildren(activePath);
            Consumer<Object> action = x -> {
                    treeDataProvider.getTreeData().removeItem(activePath);
                    treeDataProvider.refreshAll();
            };
            if (children != null && children.size() >= 1) {
                ConfirmDialogUtils.confirmDialog(
                        "Remove path",
                        "Removing this path removes all children. Proceed?",
                        "Delete", action,
                        "Cancel", null).open();
            } else {
                action.accept(null);
            }
            setActivePath(activePath.getParent());
            refresh();
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

        predicateGrid.addComponentColumn(predicateRecord -> {
            Node predicateNode = predicateRecord.predicate.asNode();
            Span label = new Span(predicateNode.toString());

            // labelMgr.forHasText(label, predicateNode);
            VaadinLabelMgr.forHasText(labelMgr, label, predicateNode);

            Button addInstanceBtn = new Button(VaadinIcon.PLUS_CIRCLE_O.create());
            addInstanceBtn.addClickListener(ev -> {
                // Allocate the next alias in the tree data
                FacetPath newPath = allocate(treeDataProvider.getTreeData(), predicateRecord.activePath, predicateRecord.predicate.asNode(), predicateRecord.isForward, null);

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


        predicatesLayout.add(predicateGrid);


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
        tabSheet.add("Predicates", predicatesSplitLayout);
        tabSheet.add("Values", valuesLayout);
        // VaadinComponentUtils.notifyResize(tabSheet, true);

        add(titleRow);

        add(tabSheet);
    }

    public void fetchPredicates() {

        // Bridge qef to conn
        RDFConnection conn = RDFConnections.of(qef);

        try {
            FacetedQuery fq = FacetedQueryImpl.create(conn).baseConcept(model.getBaseConcept());
            FacetNode fn = goTo(fq.root(), activePath);
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

    public static FacetPath allocate(TreeData<FacetPath> treeData, FacetPath parent, Node predicate, boolean isForward, Integer targetComponent) {
        List<FacetPath> children = treeData.getChildren(parent);

        // Collect all taken aliases
        Set<String> usedAliases = children.stream()
                .map(item -> item.getFileName().toSegment())
                .filter(step -> step.getNode().equals(predicate) && step.isForward() == isForward && step.getTargetComponent() == targetComponent)
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

    public static FacetNode goTo(FacetNode start, FacetPath path) {
        FacetNode current = path.isAbsolute() ? start.root() : start;
        for (FacetStep step : path.getSegments()) {
            Direction dir = Direction.ofFwd(step.isForward());
            Node node = step.getNode();
            FacetMultiNode fmn = current.step(node, dir);

            String alias = step.getAlias();
            if (alias == null || alias.isEmpty()) {
                current = fmn.one();
            } else {
                current = fmn.viaAlias(alias);
            }
        }
        return current;
    }


    public DetailsView(LabelService<Node, String> labelMgr, QueryExecutionFactoryQuery qef, FacetTreeModel model, TreeDataProvider<FacetPath> treeDataProvider) {
        this();
        this.labelMgr = labelMgr;
        this.qef = qef;
        this.model = model;
        this.treeDataProvider = treeDataProvider;
    }

}


/**
 * Options for how to derive a set of predicates from a given set of predicates.
 *
 * Specifically:
 * <ul>
 *   <li>Which columns this predicate is linked to - in SQL this would be how many aliases exist of that predicate</li>
 * </ul>
 *
 */
class PredicateDetailsView {

}

class TreeRuntimeModel {
}

//
//class PropertyDataProvider
//    extends AbstractBackEndHierarchicalDataProvider<PathPPA, UnaryRelation>
//{
//    protected FacetTreeModel model = new FacetTreeModel();
//    protected RdfDataSource dataSource;
//
//    public PropertyDataProvider(RdfDataSource dataSource) {
//        super();
//        this.dataSource = dataSource;
//    }
//
//    @Override
//    public int getChildCount(HierarchicalQuery<PathPPA, UnaryRelation> query) {
//        int result;
//        PathPPA parent = query.getParent();
//        if (parent == null) {
//            result = 1;
//        } else {
//            // Consult cache or backend for data
//            result = Ints.saturatedCast(fetchChildrenFromBackEnd(query).count());
//        }
//
//        UnaryRelation filter = query.getFilter().orElse(null);
//        // TODO Auto-generated method stub
//        return result;
//    }
//
//    @Override
//    public boolean hasChildren(PathPPA path) {
//        RuntimeModel node = model.getOrCreateData(path);
//        Set<ResourceInDataset> knownData = node.getFoundPredicates();
//        boolean result = !knownData.isEmpty();
//        return result;
//    }
//
//    @Override
//    protected Stream<PathPPA> fetchChildrenFromBackEnd(HierarchicalQuery<PathPPA, UnaryRelation> query) {
//        PathPPA path = query.getParent();
//        RuntimeModel node = model.getOrCreateData(path);
//        Set<ResourceInDataset> knownData = node.getFoundPredicates();
//        Stream<PathPP> result = knownData.stream().map(item -> {
//
//        });
//        return result;
//    }
//}


public class TableMapperComponent
    extends VerticalLayout
{
    protected LabelService<Node, String> labelMgr;

    protected TreeDataProvider<FacetPath> treeDataProvider = new TreeDataProvider<>(new TreeData<>());

    protected TreeGrid<FacetPath> treeGrid = new TreeGrid<>();
    protected DetailsView detailsView;

    protected Map<FacetPath, Boolean> pathToVisibility = new HashMap<>();

    // protected SubjectDetailsView subjectDetailsView = new SubjectDetailsView();
    protected PredicateDetailsView predicateDetailsView = new PredicateDetailsView();


    // protected RdfDataSource dataSource;
    protected QueryExecutionFactoryQuery qef;


    protected DataProviderSparqlBinding sparqlDataProvider;

    protected UnaryRelation baseConcept;

    public TableMapperComponent() {

        // QueryExecutionFactoryQuery qef = query -> RDFConnection.connect("http://localhost:8642/sparql").query(query);
        baseConcept = new Concept(ElementUtils.createElementTriple(Vars.x, Vars.y, Vars.z), Vars.x);

        RdfDataSource base = () -> RDFConnection.connect("http://localhost:8642/sparql");

        RdfDataSource dataSource = base
                .decorate(RdfDataSourceWithBnodeRewrite::wrapWithAutoBnodeProfileDetection)
                // .decorate(RdfDataSourceWithLocalCache::new)
                ;

        RDFConnection conn = dataSource.getConnection();
        FacetedQuery fq = FacetedQueryImpl.create(conn);

        System.out.println("BaseConcept: " + baseConcept);

        if(baseConcept != null) {
            fq.baseConcept(baseConcept);
        }

        // fq.root().fwd(RDF.type).one().availableValues().exec().forEach(rdfNode -> System.out.println(rdfNode));

        // SparqlQueryConnection c2 = fq.root().fwd().facetCounts().resolver().fwd().toFacetedQuery().root().fwd().facetCounts().resolver().fwd().virtualConn();

        // SparqlQueryConnection c2 = fq.root().fwd().facetCounts().resolver().fwd().virtualConn();
        SparqlQueryConnection c2 = fq.connection();



        this.qef = QueryExecutionFactories.of(c2); // QueryExecutionFactories.of(dataSource);
        Property labelProperty = RDFS.label;// DCTerms.description;

        LookupService<Node, String> ls1 = LabelUtils.getLabelLookupService(qef, labelProperty, DefaultPrefixes.get());
        LookupService<Node, String> ls2 = keys -> Flowable.fromIterable(keys).map(k -> Map.entry(k, Objects.toString(k)));

        VaadinRdfLabelMgrImpl labelMgr = new VaadinRdfLabelMgrImpl(ls1);


        Button resetLabelsBtn = new Button("Toggle Labels");
        resetLabelsBtn.addClickListener(ev -> {
            LookupService<Node, String> ls = labelMgr.getLookupService() == ls1 ? ls2 : ls1;
            labelMgr.setLookupService(ls);
            labelMgr.refreshAll();
        });

        // this.dataSource = dataSource;
        this.labelMgr = labelMgr;

        add(resetLabelsBtn);



        this.detailsView = new DetailsView(labelMgr, qef, new FacetTreeModel(baseConcept), treeDataProvider);
        // FacetedQuery fq = new XFacetedQueryImpl(null, null)

        //this.sparqlDataProvider = new ListDataProvider<>(Collections.emptyList());
        // this.sparqlDataProvider = new DataProviderSparqlBinding(RelationUtils.create, qef)

        initComponent();
    }

    public TableMapperComponent(QueryExecutionFactoryQuery qef, UnaryRelation baseConcept, LabelService<Node, String> labelService) {
        this.qef = qef;
        this.baseConcept = baseConcept;
        this.labelMgr = labelService;


        this.detailsView = new DetailsView(labelMgr, qef, new FacetTreeModel(baseConcept), treeDataProvider);


        initComponent();
    }


    // HeaderCell is not derived from HasText!
    public static void labelForAliasPathLastStep(LabelService<Node, String> labelMgr, HeaderCell headerCell, FacetPath path) {
        if (path.getSegments().isEmpty()) {
            headerCell.setText("");
        } else {
            FacetStep step = path.getFileName().toSegment();
            labelMgr.register(headerCell, step.getNode(), (c, map) -> {
                String label = toString(step, map::get);
                c.setText(label);
            });
        }
    }

    public static void labelForAliasedPath(LabelService<Node, String> labelMgr, HasText hasText, FacetPath path) {
        Set<Node> nodes = path.streamNodes().collect(Collectors.toSet());
        if (nodes.isEmpty()) {
            String label = toLabel(path, Collections.emptyMap());
            hasText.setText(label);
        } else {
            labelMgr.register(hasText, nodes, (c, map) -> {
                String label = toLabel(path, map);
                c.setText(label);
            });
        }
    }

    public static String toLabel(FacetPath path, Map<Node, String> map) {
        String result = "";
        if (path.isAbsolute()) {
             result += "/ ";
        }

        result += path.getSegments().stream()
                .map(step -> toString(step, map::get))
                .collect(Collectors.joining(" / "));

        return result;
    }

    public static String toString(FacetStep step, Function<Node, String> nodeToLabel) {
        String result = ""
            + nodeToLabel.apply(step.getNode())
            + (step.isForward() ? "" : " -1")
            + (Strings.isNullOrEmpty(step.getAlias()) ? "" : " " + step.getAlias())
            + (FacetStep.PREDICATE.equals(step.getTargetComponent()) ? " #" : "");

        return result;
    }

    protected FacetPath draggedProperty = null;


    public void initComponent() {
        setWidthFull();

        SplitLayout layout = new SplitLayout();
        layout.setSplitterPosition(20);
        layout.setWidthFull();

        FacetPath rootPath = FacetPathOps.get().newRoot();
        treeDataProvider.getTreeData().addRootItems(rootPath);

        treeGrid.setDataProvider(treeDataProvider);
        treeGrid.expand(rootPath);

        treeGrid.setRowsDraggable(true);
        // GridMultiSelectionModel<PathPPA> treeGridSelectionModel = (GridMultiSelectionModel<PathPPA>)treeGrid.getSelectionModel();
        // treeGridSelectionModel.setSelectAllCheckboxVisibility(GridMultiSelectionModel.SelectAllCheckboxVisibility.VISIBLE);



        // ObservableSelectionModel<PathPPA> selectedProperties = new ObservableSelectionModel<>(treeGridSelectionModel);

        treeGrid.addDragStartListener(event -> {
            // store current dragged item so we know what to drop
            List<FacetPath> draggedItems = event.getDraggedItems();
            if (draggedItems.size() > 1) {
                // This seems to be a dumb limitation of vaadin: We can not drag individual rows if multiple ones are selected.
                NotificationUtils.error("Please temporarily deselect the row you wish to drag or drag a non-selected row. A framework limitation prevents dragging of individual selected rows.");
            } else if (draggedItems.size() == 1) {
                draggedProperty = draggedItems.get(0);
                treeGrid.setDropMode(GridDropMode.BETWEEN);
            }
        });

        treeGrid.addDragEndListener(event -> {
            draggedProperty = null;
            // Once dragging has ended, disable drop mode so that
            // it won't look like other dragged items can be dropped
            treeGrid.setDropMode(null);
        });

        treeGrid.addDropListener(event -> {
            TreeData<FacetPath> treeData = treeDataProvider.getTreeData();
            FacetPath dropOverItem = event.getDropTargetItem().get();
            if (draggedProperty != null && !draggedProperty.equals(dropOverItem)) {
                FacetPath dropOverParent = dropOverItem.getParent();
                FacetPath dragParent = draggedProperty.getParent();

                if (dragParent.equals(dropOverParent)) {
                    List<FacetPath> children = treeData.getChildren(dragParent);
                    int dropIndex = children.indexOf(dropOverItem) + (event.getDropLocation() == GridDropLocation.BELOW ? 1 : 0);
                    FacetPath sibling = dropIndex <= 0 ? null : children.get(dropIndex - 1);

                    treeData.moveAfterSibling(draggedProperty, sibling);

                    treeDataProvider.refreshAll();
                }


                // reorder dragged item the backing gridItems container
                // availableProperties.remove(draggedProperty);
                // calculate drop index based on the dropOverItem
//                int dropIndex =
//                        availableProperties.indexOf(dropOverItem) + (event.getDropLocation() == GridDropLocation.BELOW ? 1 : 0);
//                availableProperties.add(dropIndex, draggedProperty);

                // Set<Binding> selectedProperties = propertyGrid.getSelectionModel().getSelectedItems();

//                List<Path> orderedPaths = SetUniqueList.setUniqueList(availableProperties.stream()
//                        .filter(selectedProperties::contains)
//                        .map(b -> PathUtils.createStep(b.get(Vars.p), !NodeValue.FALSE.asNode().equals(b.get(Vars.d))))
//                        .collect(Collectors.toList()));
//
//
//                visibleProperties.set(orderedPaths);
            }
        });





        treeGrid.addComponentHierarchyColumn(node -> {
//            Avatar avatar = new Avatar();
//            avatar.setName(person.getFullName());
//            avatar.setImage(person.getPictureUrl());
//
            List<FacetStep> steps = node.getSegments();
            FacetStep lastStep = steps.isEmpty() ? null : steps.get(steps.size() - 1);

            Span label = new Span();
            if (lastStep == null) {
                String str = "Root";
                label.setText(str);
            } else {
                Node n = lastStep.getNode();
                boolean isFwd = lastStep.isForward();
                boolean targetsPredicate = FacetStep.PREDICATE.equals(lastStep.getTargetComponent());
                labelMgr.register(label, n, (c, map) -> {
                    String s = map.get(n);
                    c.setText(s + (!isFwd ? " -1" : "") + (targetsPredicate ? " # " : ""));
                });
            }

////
////            Span profession = new Span(person.getProfession());
////            profession.getStyle()
////                    .set("color", "var(--lumo-secondary-text-color)")
////                    .set("font-size", "var(--lumo-font-size-s)");
////
            VerticalLayout column = new VerticalLayout(label);
            column.getStyle().set("line-height", "var(--lumo-line-height-m)");
            column.setPadding(false);
            column.setSpacing(false);
////
            HorizontalLayout row = new HorizontalLayout(); //avatar, column);
            row.setAlignItems(FlexComponent.Alignment.CENTER);
            row.setSpacing(true);
            row.add(column);
            return row;
        }).setHeader("Property Tree").setResizable(true);

        treeGrid.addComponentColumn(path -> {
            boolean isVisible = pathToVisibility.computeIfAbsent(path, x -> true);

            // Icon eyeIcon = VaadinIcon.EYE.create();
            Checkbox cb = new Checkbox();
            cb.setValue(isVisible);
            cb.addClickListener(ev -> {
                pathToVisibility.put(path, cb.getValue());
            });

            HorizontalLayout row = new HorizontalLayout(); //avatar, column);
            row.setAlignItems(FlexComponent.Alignment.CENTER);
            row.setSpacing(true);
            row.add(cb);
            return row;

//            VerticalLayout column = new VerticalLayout();
//            column.getStyle().set("font-size", "var(--lumo-font-size-s)")
//                    .set("line-height", "var(--lumo-line-height-m)");
//            column.setPadding(false);
//            column.setSpacing(false);
//            return column;
        }).setHeader("Visible").setResizable(true);

        treeGrid.addItemClickListener(ev -> {
            FacetPath path = ev.getItem();
            detailsView.setActivePath(path);

            detailsView.refresh();
        });


        treeGrid.setWidthFull();
        detailsView.setWidthFull();
        layout.addToPrimary(treeGrid);
        layout.addToSecondary(detailsView);
//        layout.setFlexGrow(1, treeGrid);
//        layout.setFlexGrow(3, detailsView);
        add(layout);

        // HeaderRow headerRow = sparqlGrid.appendHeaderRow();
        // HeaderRow filterRow = sparqlGrid.appendHeaderRow();

        VerticalLayout sparqlGridContainer = new VerticalLayout();

        Button refreshTableBtn = new Button("Update table");
        refreshTableBtn.addClickListener(ev -> {

            Grid<Binding> sparqlGrid = new Grid<>();
            sparqlGrid.setPageSize(10000);
            sparqlGrid.setWidthFull();
            sparqlGrid.setColumnReorderingAllowed(true);

            // QueryExecutionFactoryQuery qef = QueryExecutionFactories.of(dataSource);


            SetMultimap<FacetPath, Expr> constraintIndex = HashMultimap.create();

            MappedQuery mappedQuery = ElementGenerator.createQuery(baseConcept, treeDataProvider.getTreeData(), constraintIndex, path -> !Boolean.FALSE.equals(pathToVisibility.get(path)));
//            Query query =
//            RelationUtils.createQuery(null);
            // VaadinSparqlUtils.setQueryForGridBinding(sparqlGrid, headerRow, qef, query);
            // VaadinSparqlUtils.configureGridFilter(sparqlGrid, filterRow, query.getProjectVars(), var -> str -> VaadinSparqlUtils.createFilterExpr(var, str).orElse(null));
            SparqlGrid.setQueryForGridBinding(sparqlGrid, qef, labelMgr, mappedQuery);

            sparqlGridContainer.removeAll();
            sparqlGridContainer.add(sparqlGrid);

        });

        add(refreshTableBtn);
        add(sparqlGridContainer);

    }
}
