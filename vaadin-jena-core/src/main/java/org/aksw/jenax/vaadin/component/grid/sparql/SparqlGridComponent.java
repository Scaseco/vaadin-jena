package org.aksw.jenax.vaadin.component.grid.sparql;

import java.util.HashMap;
import java.util.Map;

import org.aksw.facete.v4.impl.ElementGenerator;
import org.aksw.facete.v4.impl.TreeDataUtils;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jenax.connection.query.QueryExecutionFactoryDataset;
import org.aksw.jenax.connection.query.QueryExecutionFactoryQuery;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetPathOps;
import org.aksw.jenax.sparql.relation.api.UnaryRelation;
import org.aksw.jenax.vaadin.label.LabelService;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;

public class SparqlGridComponent extends VerticalLayout {
    protected QueryExecutionFactoryQuery qef;
    protected UnaryRelation baseConcept;
    protected LabelService<Node, String> labelMgr;
    protected TreeDataProvider<FacetPath> treeDataProvider = new TreeDataProvider<>(new TreeData<>());
    protected Map<FacetPath, Boolean> pathToVisibility = new HashMap<>();

    protected Button settingsBtn;

    // TableMapperComponent tm = new TableMapperComponent();

    public SparqlGridComponent() {
        // Dataset dummy = DatasetFactory.create();
        this.qef = new QueryExecutionFactoryDataset();
        this.baseConcept = ConceptUtils.createSubjectConcept();

        FacetPath rootPath = FacetPathOps.get().newRoot();
        treeDataProvider.getTreeData().addRootItems(rootPath);

//        settingsBtn = new Button(VaadinIcon.COG.create()); //"Available columns");
//        settingsBtn.addClickListener(event -> {
//
//            Dialog dialog = new Dialog();
//
//            TableMapperComponent tm = new TableMapperComponent();
//
//
//            // dialog.add(new Text("Close me with the esc-key or an outside click"));
//            dialog.add(tm);
//            dialog.open();
//        });

        // Style tableSettingsStyle = settingsBtn.getStyle();
//        tableSettingsStyle.set("position", "absolute");
//        tableSettingsStyle.set("top", "0");
//        tableSettingsStyle.set("right", "0");

    }

    public SparqlGridComponent(QueryExecutionFactoryQuery qef, UnaryRelation baseConcept,
            LabelService<Node, String> labelMgr) {
        this();
        this.qef = qef;
        this.baseConcept = baseConcept;
        this.labelMgr = labelMgr;

        this.resetGrid();
    }

    public QueryExecutionFactoryQuery getQef() {
        return qef;
    }

    public void setQef(QueryExecutionFactoryQuery qef) {
        this.qef = qef;
    }

    public UnaryRelation getBaseConcept() {
        return baseConcept;
    }

    public void setBaseConcept(UnaryRelation baseConcept) {
        this.baseConcept = baseConcept;
    }

    public LabelService<Node, String> getLabelMgr() {
        return labelMgr;
    }

    public void setLabelMgr(LabelService<Node, String> labelMgr) {
        this.labelMgr = labelMgr;
    }

    public TreeDataProvider<FacetPath> getTreeDataProvider() {
        return treeDataProvider;
    }

    public void setTreeDataProvider(TreeDataProvider<FacetPath> treeDataProvider) {
        this.treeDataProvider = treeDataProvider;
    }

    public void resetGrid() {

        this.removeAll();

        Grid<Binding> sparqlGrid = new Grid<>();
        sparqlGrid.setPageSize(1000);
        sparqlGrid.setWidthFull();

        SetMultimap<FacetPath, Expr> constraintIndex = HashMultimap.create();

        // MappedQuery mappedQuery = ElementGenerator.createQuery(baseConcept,
        // treeDataProvider.getTreeData(), constraintIndex, path ->
        // !Boolean.FALSE.equals(pathToVisibility.get(path)));

        org.aksw.facete.v3.api.TreeData<FacetPath> treeProjection = TreeDataUtils
                .toFacete(treeDataProvider.getTreeData());

        MappedQuery mappedQuery = ElementGenerator.createQuery(baseConcept, treeProjection, constraintIndex,
                path -> !Boolean.FALSE.equals(pathToVisibility.get(path)));

//        Query query =
//        RelationUtils.createQuery(null);
        // VaadinSparqlUtils.setQueryForGridBinding(sparqlGrid, headerRow, qef, query);
        // VaadinSparqlUtils.configureGridFilter(sparqlGrid, filterRow,
        // query.getProjectVars(), var -> str -> VaadinSparqlUtils.createFilterExpr(var,
        // str).orElse(null));
        SparqlGrid.setQueryForGridBinding(sparqlGrid, qef, labelMgr, mappedQuery);

        TableMapperComponent tm = new TableMapperComponent(qef, baseConcept, labelMgr);
        this.add(tm);

        this.add(sparqlGrid);
    }
}
