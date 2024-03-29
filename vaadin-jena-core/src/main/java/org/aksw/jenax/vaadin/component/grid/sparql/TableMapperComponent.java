package org.aksw.jenax.vaadin.component.grid.sparql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.aksw.commons.util.direction.Direction;
import org.aksw.commons.util.obj.Enriched;
import org.aksw.facete.v4.impl.ElementGenerator;
import org.aksw.facete.v4.impl.MappedQuery;
import org.aksw.jena_sparql_api.algebra.expr.transform.ExprTransformVirtualBnodeUris;
import org.aksw.jena_sparql_api.vaadin.data.provider.DataProviderNodeQuery;
import org.aksw.jena_sparql_api.vaadin.data.provider.DataProviderSparqlBinding;
import org.aksw.jena_sparql_api.vaadin.data.provider.DataRetriever;
import org.aksw.jena_sparql_api.vaadin.util.VaadinSparqlUtils;
import org.aksw.jena_sparql_api.vaadin.util.VaadinStyleUtils;
import org.aksw.jenax.arq.datashape.viewselector.EntityClassifier;
import org.aksw.jenax.arq.util.node.NodeUtils;
import org.aksw.jenax.arq.util.syntax.QueryGenerationUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.dataaccess.sparql.polyfill.datasource.RdfDataSourceWithBnodeRewrite;
import org.aksw.jenax.facete.treequery2.api.NodeQuery;
import org.aksw.jenax.facete.treequery2.api.RelationQuery;
import org.aksw.jenax.facete.treequery2.impl.ElementGeneratorLateral;
import org.aksw.jenax.facete.treequery2.impl.FacetPathMappingImpl;
import org.aksw.jenax.facete.treequery2.impl.NodeQueryImpl;
import org.aksw.jenax.model.shacl.domain.ShNodeShape;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetStep;
import org.aksw.jenax.sparql.fragment.api.Fragment1;
import org.aksw.jenax.sparql.fragment.impl.ConceptUtils;
import org.aksw.jenax.vaadin.label.LabelService;
import org.aksw.vaadin.common.component.util.ConfirmDialogUtils;
import org.aksw.vaadin.common.component.util.NotificationUtils;
import org.aksw.vaadin.common.provider.util.DataProviderUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.modify.request.QuadAcc;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.Template;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.topbraid.shacl.model.SHFactory;

import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Streams;
import com.google.common.graph.Traverser;
import com.google.common.primitives.Ints;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.HeaderRow.HeaderCell;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.dom.Style;

/** Can we make the facet path part of the RDFNode? */
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

public class TableMapperComponent
    extends VerticalLayout
{
    protected LabelService<Node, String> labelService;

    protected TreeDataProvider<FacetPath> treeDataProvider = new TreeDataProvider<>(new TreeData<>());

    protected TreeGrid<FacetPath> propertyTreeGrid = new TreeGrid<>();
    protected TableMapperDetailsView detailsView;

    protected Set<FacetPath> expandedPaths = new HashSet<>();
    protected Map<FacetPath, Boolean> pathToVisibility = new HashMap<>();

    protected RdfDataSource dataSource;
    // protected QueryExecutionFactoryQuery qef;


    protected DataProviderSparqlBinding sparqlDataProvider;

    VerticalLayout sparqlGridContainer = new VerticalLayout();

    protected Fragment1 baseConcept;

//    public TableMapperComponent(RdfDataSource dataSource, UnaryRelation baseConcept, LabelService<Node, String> labelService) {
//
//
//        // RDFConnection conn = dataSource.getConnection();
//        //FacetedQuery fq = FacetedQueryImpl.create(conn);
//
////        System.out.println("BaseConcept: " + baseConcept);
////
////        if(baseConcept != null) {
////            fq.baseConcept(baseConcept);
////        }
////
////        this.qef = QueryExecutionFactories.of(conn);
//
//        this.dataSource = dataSource;
//
//        // this.dataSource = dataSource;
//        this.labelMgr = labelService;
//
//        // add(resetLabelsBtn);
//
//
//
//        this.detailsView = new TableMapperDetailsView(labelMgr, qef, baseConcept, treeDataProvider);
//        // FacetedQuery fq = new XFacetedQueryImpl(null, null)
//
//        //this.sparqlDataProvider = new ListDataProvider<>(Collections.emptyList());
//        // this.sparqlDataProvider = new DataProviderSparqlBinding(RelationUtils.create, qef)
//
//        initComponent();
//    }

    public TableMapperComponent(RdfDataSource dataSource, Fragment1 baseConcept, LabelService<Node, String> labelService) {
        this.dataSource = dataSource;
        this.baseConcept = baseConcept;
        this.labelService = labelService;
        this.detailsView = new TableMapperDetailsView(labelService, dataSource, baseConcept, treeDataProvider);
        initComponent();
    }


    // HeaderCell is not derived from HasText!
    public static void labelForAliasPathLastStep(LabelService<Node, String> labelMgr, HeaderCell headerCell, FacetPath path, boolean isLeafPath) {
        if (path.getSegments().isEmpty()) {
            headerCell.setText("");
        } else {
            Div span = new Div();
            span.setWidthFull();

            if (!isLeafPath) {
                Style style = span.getStyle();
                style.set("text-align", "center");
                style.set("background-color", "hsla(214, 53%, 23%, 0.16)");
            }

            headerCell.setComponent(span);

            FacetStep step = path.getFileName().toSegment();
            // labelMgr.register(headerCell, step.getNode(), (c, map) -> {
            labelMgr.register(span, step.getNode(), (c, map) -> {
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

    public static <T> int countDescendents(TreeData<T> tree, T node) {
        List<T> children = Optional.ofNullable(tree.getChildren(node)).orElse(Collections.emptyList());
        int result = Ints.saturatedCast(Streams.stream(Traverser.<T>forTree(x -> tree.getChildren(x)).depthFirstPreOrder(children)).count());
        return result;
    }

    public static void removePath(TreeDataProvider<FacetPath> treeDataProvider, FacetPath node, boolean removeChildrenOnly) {
        // Don't delete the root node!
        TreeData<FacetPath> treeData = treeDataProvider.getTreeData();
        boolean _removeChildrenOnly = treeData.getParent(node) == null ? true : removeChildrenOnly;

        Consumer<Object> action = unused -> {
            removePathCore(treeData, node, _removeChildrenOnly);
            treeDataProvider.refreshAll();
        };

        int numDescendents = countDescendents(treeData, node);
        boolean showConfirmDlg = removeChildrenOnly ?
                numDescendents > 1
                : numDescendents > 0;

        if (showConfirmDlg) {
            ConfirmDialogUtils.confirmDialog(
                    "Remove path",
                    "Removal affects multiple paths. Proceed?",
                    "Delete", action,
                    "Cancel", null).open();
        } else {
            action.accept(null);
        }
    }

    // TODO Move to TreeDataUtils
    public static <T> void removePathCore(TreeData<T> treeData, T node, boolean removeChildrenOnly) {
        if (removeChildrenOnly) {
            List<T> children = new ArrayList<>(treeData.getChildren(node));
            for (T child : children) {
                treeData.removeItem(child);
            }
        } else {
            treeData.removeItem(node);
        }
    }



    protected FacetPath draggedProperty = null;


    public void initComponent() {
        setWidthFull();

        SplitLayout layout = new SplitLayout();
        layout.setSplitterPosition(20);
        layout.setWidthFull();

        FacetPath rootPath = FacetPath.newAbsolutePath();
        treeDataProvider.getTreeData().addRootItems(rootPath);

        propertyTreeGrid.setDataProvider(treeDataProvider);

        VaadinStyleUtils.setResizeVertical(propertyTreeGrid.getStyle());

        propertyTreeGrid.expand(rootPath);

        propertyTreeGrid.setRowsDraggable(true);
        // GridMultiSelectionModel<PathPPA> treeGridSelectionModel = (GridMultiSelectionModel<PathPPA>)treeGrid.getSelectionModel();
        // treeGridSelectionModel.setSelectAllCheckboxVisibility(GridMultiSelectionModel.SelectAllCheckboxVisibility.VISIBLE);



        // ObservableSelectionModel<PathPPA> selectedProperties = new ObservableSelectionModel<>(treeGridSelectionModel);

        propertyTreeGrid.addDragStartListener(event -> {
            // store current dragged item so we know what to drop
            List<FacetPath> draggedItems = event.getDraggedItems();
            if (draggedItems.size() > 1) {
                // This seems to be a dumb limitation of vaadin: We can not drag individual rows if multiple ones are selected.
                NotificationUtils.error("Please temporarily deselect the row you wish to drag or drag a non-selected row. A framework limitation prevents dragging of individual selected rows.");
            } else if (draggedItems.size() == 1) {
                draggedProperty = draggedItems.get(0);
                propertyTreeGrid.setDropMode(GridDropMode.BETWEEN);
            }
        });

        propertyTreeGrid.addDragEndListener(event -> {
            draggedProperty = null;
            // Once dragging has ended, disable drop mode so that
            // it won't look like other dragged items can be dropped
            propertyTreeGrid.setDropMode(null);
        });

        propertyTreeGrid.addDropListener(event -> {
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

        propertyTreeGrid.addComponentHierarchyColumn(node -> {
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
                labelService.register(label, n, (c, map) -> {
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

        propertyTreeGrid.addComponentColumn(path -> {
            boolean isVisible = pathToVisibility.getOrDefault(path, true); //  .computeIfAbsent(path, x -> true);

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

        propertyTreeGrid.addItemClickListener(ev -> {
            FacetPath path = ev.getItem();
            detailsView.setActivePath(path);

            detailsView.refresh();
        });

        propertyTreeGrid.setWidthFull();
        detailsView.setWidthFull();
        layout.addToPrimary(propertyTreeGrid);
        layout.addToSecondary(detailsView);
//        layout.setFlexGrow(1, treeGrid);
//        layout.setFlexGrow(3, detailsView);
        add(layout);

        // HeaderRow headerRow = sparqlGrid.appendHeaderRow();
        // HeaderRow filterRow = sparqlGrid.appendHeaderRow();


        Button refreshTableBtn = new Button("Update table");
        refreshTableBtn.addClickListener(ev -> refreshTable());

        add(refreshTableBtn);
        add(sparqlGridContainer);

        GridContextMenu<FacetPath> cxtMenu = propertyTreeGrid.addContextMenu();
        cxtMenu.setDynamicContentHandler(facetPath -> {
            cxtMenu.removeAll();
            TreeData<FacetPath> treeData = treeDataProvider.getTreeData();
            List<?> children = Optional.ofNullable(treeData.getChildren(facetPath)).orElse(Collections.emptyList());
            int numOptions = 0;
            if (facetPath != null) {
                if (facetPath.getParent() != null) {
                    String text = children.isEmpty() ? "Remove this node" : "Remove this node and all children";
                    cxtMenu
                        .addItem(text)
                        .addMenuItemClickListener(ev -> {
                            removePath(treeDataProvider, facetPath, false);
                        });
                    ++numOptions;
                }

                if (!children.isEmpty()) {
                    cxtMenu
                        .addItem("Remove all children of this node")
                        .addMenuItemClickListener(ev -> {
                            removePath(treeDataProvider, facetPath, true);
                        });
                    ++numOptions;
                }
            }
            return numOptions != 0;
        });

        propertyTreeGrid.addExpandListener(ev -> expandedPaths.addAll(ev.getItems()));
        propertyTreeGrid.addCollapseListener(ev -> expandedPaths.removeAll(ev.getItems()));

        // Add a listener that expands new nodes in the grid
        treeDataProvider.addDataProviderListener(ev -> {
            TreeData<FacetPath> treeData = treeDataProvider.getTreeData();
            for (FacetPath path : Traverser.forTree(treeData::getChildren)
                    .depthFirstPreOrder(treeData.getRootItems())) {
                if (!pathToVisibility.containsKey(path)) {
                    pathToVisibility.put(path, true);
                    expandedPaths.add(path);
                }

                if (expandedPaths.contains(path)) {
                    propertyTreeGrid.expand(path);
                }
            }
        });

        propertyTreeGrid.select(rootPath);
        detailsView.setActivePath(rootPath);
        detailsView.refresh();
    }

    public TreeGrid<FacetPath> getPropertyTreeGrid() {
        return propertyTreeGrid;
    }

    public void refreshTable() {
        Predicate<FacetPath> isVisible = toPredicateAbsentAsTrue(pathToVisibility);
        TreeData<FacetPath> treeData = treeDataProvider.getTreeData();
        Grid<Binding> sparqlGrid = buildGrid(dataSource, baseConcept, treeData, isVisible, labelService);
        sparqlGridContainer.removeAll();
        sparqlGridContainer.add(sparqlGrid);
    }

    public static Grid<Binding> buildGrid(
            RdfDataSource dataSource, Fragment1 baseConcept, TreeData<FacetPath> treeData,
            Predicate<FacetPath> isVisible, LabelService<Node, String> labelService)
    {
        Grid<Binding> sparqlGrid = new Grid<>();
        sparqlGrid.setMultiSort(true);
        // sparqlGrid.setPageSize(1000);
        sparqlGrid.setWidthFull();
        sparqlGrid.setColumnReorderingAllowed(true);

        SetMultimap<FacetPath, Expr> constraintIndex = HashMultimap.create();
        org.aksw.facete.v3.api.TreeData<FacetPath> treeProjection = TreeDataUtils.toFacete(treeData);
        MappedQuery mappedQuery = ElementGenerator.createQuery(baseConcept, treeProjection, constraintIndex, isVisible);
        mappedQuery = new MappedQuery(mappedQuery.getTree(), QueryGenerationUtils.discardUnbound(mappedQuery.getQuery()), mappedQuery.getVarToPath());

        SparqlGrid.setQueryForGridBinding(sparqlGrid, dataSource.asQef(), labelService, mappedQuery);
        DataProviderUtils.wrapWithErrorHandler(sparqlGrid);

        return sparqlGrid;
    }

    /** Returns a snapshot (a copy) of the current tree data */
    public org.aksw.facete.v3.api.TreeData<FacetPath> getTreeDataSnapshot() {
        org.aksw.facete.v3.api.TreeData<FacetPath> result = TreeDataUtils.toFacete(treeDataProvider.getTreeData())
                .cloneTree();
        return result;
    }

    public Map<FacetPath, Boolean> getPathVisibilitySnapshot() {
        return new HashMap<>(pathToVisibility);
    }

    public static Predicate<FacetPath> toPredicateAbsentAsTrue(Map<FacetPath, Boolean> pathToVisibility) {
        return path -> !Boolean.FALSE.equals(pathToVisibility.get(path));
    }

    // TODO Add "mergeState" method
    public void setState(TableMapperState state) {
        pathToVisibility.clear();
        pathToVisibility.putAll(state.getPathToVisibility());

        org.aksw.facete.v3.api.TreeData<FacetPath> src = state.getFacetTree();

        TreeData<FacetPath> tgt = treeDataProvider.getTreeData();
        tgt.clear();
        tgt.addItems(src.getRootItems(), src::getChildren);
    }

    public TableMapperState getState() {
        return new TableMapperState(getTreeDataSnapshot(), getPathVisibilitySnapshot());
    }

    // TODO How to abstract the sparql grid for reuse?
    public static Grid<Binding> createSparqlGrid(
            RdfDataSource dataSource,
            Fragment1 baseConcept,
            TreeDataProvider<FacetPath> treeDataProvider,
            Map<FacetPath, Boolean> pathToVisibility,
            LabelService<Node, String> labelMgr
    ) {
        Grid<Binding> sparqlGrid = new Grid<>();
        // sparqlGrid.setPageSize(1000);
        sparqlGrid.setWidthFull();
        sparqlGrid.setColumnReorderingAllowed(true);

        SetMultimap<FacetPath, Expr> constraintIndex = HashMultimap.create();
        org.aksw.facete.v3.api.TreeData<FacetPath> treeProjection = TreeDataUtils.toFacete(treeDataProvider.getTreeData());
        MappedQuery mappedQuery = ElementGenerator.createQuery(baseConcept, treeProjection, constraintIndex, path -> !Boolean.FALSE.equals(pathToVisibility.get(path)));
        mappedQuery = new MappedQuery(mappedQuery.getTree(), QueryGenerationUtils.discardUnbound(mappedQuery.getQuery()), mappedQuery.getVarToPath());
        SparqlGrid.setQueryForGridBinding(sparqlGrid, dataSource.asQef(), labelMgr, mappedQuery);
        return sparqlGrid;

//        Query query =
//        RelationUtils.createQuery(null);
        // VaadinSparqlUtils.setQueryForGridBinding(sparqlGrid, headerRow, qef, query);
        // VaadinSparqlUtils.configureGridFilter(sparqlGrid, filterRow, query.getProjectVars(), var -> str -> VaadinSparqlUtils.createFilterExpr(var, str).orElse(null));
        // QueryExecutionFactoryQuery qef = QueryExecutionFactories.of(dataSource);
    }


    public static void main(String[] args) {
        SHFactory.ensureInited();
        Model shaclModel = RDFDataMgr.loadModel("/home/raven/Projects/Eclipse/rmltk-parent/r2rml-resource-shacl/src/main/resources/r2rml.core.shacl.ttl");
        List<ShNodeShape> nodeShapes = org.aksw.jenax.model.shacl.util.ShUtils.listNodeShapes(shaclModel);
        // List<ShNodeShape> nodeShapes = shaclModel.listSubjects().mapWith(r -> r.as(ShNodeShape.class)).toList();

        EntityClassifier entityClassifier = new EntityClassifier(Arrays.asList(Vars.s));

        // https://www.w3.org/TR/shacl/#targets
        // "The target of a shape is the union of all RDF terms produced by the individual targets that are declared by the shape in the shapes graph."
        for (ShNodeShape nodeShape : nodeShapes) {
            EntityClassifier.registerNodeShape(entityClassifier, nodeShape);
        }

        if (false) {
            for (ShNodeShape nodeShape : nodeShapes) {
                // NodeQuery nq = NodeQueryImpl.newRoot();
                NodeQuery nq = NodeQueryImpl.newRoot();

                ElementGeneratorLateral.toNodeQuery(nq, nodeShape);
                Element elt = new ElementGeneratorLateral().createElement(nq.relationQuery());
                System.out.println("Shape: " + nodeShape.asNode() + " --------");
                System.out.println(elt);
            }
        }

//        NodeQuery nq = NodeQueryImpl.newRoot();


        RelationQuery rq = RelationQuery.of(Vars.s); // RelationQuery.of(ConceptUtils.createSubjectConcept());
        System.out.println("Roots:" +  rq.roots());
        NodeQuery tgtNode = rq.target().resolve(FacetPath.newAbsolutePath().resolve(FacetStep.fwd(RDF.type.asNode())).resolve(FacetStep.fwd(RDFS.label.asNode())));

        System.out.println(tgtNode);
        for (Entry<FacetStep, RelationQuery> child : rq.target().children().entrySet()) {
            System.out.println(child);
        }

        NodeQuery tgtNode2 = rq.target().resolve(FacetPath.newAbsolutePath().resolve(FacetStep.fwd(RDF.type.asNode())).resolve(FacetStep.fwd(RDFS.label.asNode())));
        tgtNode2.limit(10l);

        NodeQuery o = tgtNode2.resolve(FacetPath.newRelativePath().resolve(FacetStep.fwd(NodeUtils.ANY_IRI)));
        NodeQuery p = tgtNode2.resolve(FacetPath.newRelativePath().resolve(FacetStep.of(NodeUtils.ANY_IRI, Direction.FORWARD, null, FacetStep.PREDICATE)));

        NodeQuery x = p.fwd("urn:foo").fwd("urn:bar");

        p.limit(100l);

        // Both nodes should be backed by the same relation
        System.out.println("o limit: " + o.limit());

        FacetPath ppath = p.getFacetPath();
        System.out.println("p path: " + ppath);
        System.out.println("x path: " + x.getFacetPath());

        System.out.println("p relation: " + p.relationQuery().getRelation());

        Var rootVar = Var.alloc("root");
        NodeQuery nq = rq.roots().get(0);

        nq
          .fwd("urn:p1_1")
              .bwd("urn:test").limit(15l).sortAsc().sortNone()
                  // .orderBy().fwd("urn:orderProperty").asc() // Not yet supported
                  // .constraints().fwd(NodeUtils.ANY_IRI)
                  .constraints().fwd("urn:constraint").enterConstraints().eq(RDFS.seeAlso).activate().leaveConstraints().getRoot()
              .getRoot()
              .fwd("urn:1_2").limit(30l).sortAsc(); //.orderBy().fwd(RDFS.comment.asNode()).asc();
          ;


          org.aksw.jenax.facete.treequery2.api.FacetPathMapping fpm = new FacetPathMappingImpl();
System.out.println(fpm.allocate(nq
          .fwd("urn:p1_1")
              .bwd("urn:p2_1").getFacetPath()));

        // RelationQuery rrq = nq.relationQuery();
        // NodeQuery target = nq.fwd("urn:1_2");
        NodeQuery target = nq;
        // NodeQuery target = nq.fwd("urn:1_2");

        RelationQuery rrq = target.relationQuery();
        Element elt = new ElementGeneratorLateral().createElement(rrq);

        Query query = new Query();
        query.setConstructTemplate(new Template(new QuadAcc(Arrays.asList(Quad.create(rootVar, Vars.x, Vars.y, Vars.z)))));
        query.setQueryConstructType();
        query.setQueryPattern(elt);

        System.out.println(query);


        RdfDataSource rdfDataSourceRaw = () -> RDFConnection.connect("http://localhost:8642/sparql");
        RdfDataSource dataSource = RdfDataSourceWithBnodeRewrite.wrapWithAutoBnodeProfileDetection(rdfDataSourceRaw);
        // QueryExecutionFactoryQuery qef = QueryExecutionFactories.of(rdfDataSource);

        Supplier<Fragment1> conceptSupplier = () -> ConceptUtils.createSubjectConcept();
        DataRetriever retriever = new DataRetriever(dataSource, entityClassifier);



        for (ShNodeShape nodeShape : nodeShapes) {
            Node nodeShapeNode = nodeShape.asNode();
            if (nodeShapeNode.isBlank()) {
                nodeShapeNode = ExprTransformVirtualBnodeUris.bnodeToIri(nodeShapeNode);
            }

            // NodeQuery nq = NodeQueryImpl.newRoot();
            NodeQuery nqq = NodeQueryImpl.newRoot();

            ElementGeneratorLateral.toNodeQuery(nqq, nodeShape);


            retriever.getClassToQuery().put(nodeShapeNode, nqq);
        }

        DataProvider<Enriched<RDFNode>, String> dataProvider = new DataProviderNodeQuery(dataSource, conceptSupplier, retriever);

        com.vaadin.flow.data.provider.Query<Enriched<RDFNode>, String> q = new com.vaadin.flow.data.provider.Query<>();
        List<Enriched<RDFNode>> list = dataProvider.fetch(q).collect(Collectors.toList());
        int itemCount = list.size();
        int size = dataProvider.size(q);

        System.out.println("Retrieved items vs counted items: " + itemCount + " / " + size);



//        for (RDFNode item : list) {
//            System.out.println(item);
//            RDFDataMgr.write(System.out, item.getModel(), RDFFormat.TURTLE_PRETTY);
//        }


//        org.apache.jena.query.Query sparqlQuery = ElementGeneratorLateral.toQuery(target);
//        LookupService<Node, DatasetOneNg> lookupService = new LookupServiceSparqlConstructQuads(qef, sparqlQuery);
//        Map<Node, DatasetOneNg> map = lookupService.defaultForAbsentKeys(n -> null).fetchMap(Arrays.asList("http://foo", "http://bar").stream().map(NodeFactory::createURI).collect(Collectors.toList()));
//        System.out.println("Result: " + map);
    }
}

