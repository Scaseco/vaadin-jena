package org.aksw.jenax.vaadin.component.grid.sparql;

import java.util.HashMap;
import java.util.Map;

import org.aksw.facete.v4.impl.ElementGenerator;
import org.aksw.facete.v4.impl.MappedQuery;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RdfDataEngines;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.sparql.relation.api.UnaryRelation;
import org.aksw.jenax.vaadin.label.LabelService;
import org.apache.jena.graph.Node;
import org.apache.jena.query.DatasetFactory;
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
    protected RdfDataSource dataSource;
    protected UnaryRelation baseConcept;
    protected LabelService<Node, String> labelMgr;
    protected TreeDataProvider<FacetPath> treeDataProvider = new TreeDataProvider<>(new TreeData<>());
    protected Map<FacetPath, Boolean> pathToVisibility = new HashMap<>();

    /** Every reset creates a new instance of the grid! */
    protected Grid<Binding> sparqlGrid = new Grid<>();

    protected Button settingsBtn;

    protected int pageSize = 50;

    // TableMapperComponent tm = new TableMapperComponent();

    public SparqlGridComponent() {
        // Dataset dummy = DatasetFactory.create();
        this.dataSource = RdfDataEngines.of(DatasetFactory.create()); // new QueryExecutionFactoryDataset();
        this.baseConcept = ConceptUtils.createSubjectConcept();

        FacetPath rootPath = FacetPath.newAbsolutePath();
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

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
        if (sparqlGrid != null) {
            sparqlGrid.setPageSize(pageSize);
        }
    }

    public SparqlGridComponent(RdfDataSource dataSource, UnaryRelation baseConcept,
            LabelService<Node, String> labelMgr) {
        this();
        this.dataSource = dataSource;
        this.baseConcept = baseConcept;
        this.labelMgr = labelMgr;

        this.resetGrid();
    }

//    public QueryExecutionFactoryQuery getQef() {
//        return qef;
//    }
//
//    public void setQef(QueryExecutionFactoryQuery qef) {
//        this.qef = qef;
//    }

    public RdfDataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(RdfDataSource dataSource) {
        this.dataSource = dataSource;
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

        sparqlGrid.setPageSize(pageSize);
        sparqlGrid.setWidthFull();

        SetMultimap<FacetPath, Expr> constraintIndex = HashMultimap.create();

        org.aksw.facete.v3.api.TreeData<FacetPath> treeProjection = TreeDataUtils
                .toFacete(treeDataProvider.getTreeData());

        MappedQuery mappedQuery = ElementGenerator.createQuery(baseConcept, treeProjection, constraintIndex,
                path -> !Boolean.FALSE.equals(pathToVisibility.get(path)));

        SparqlGrid.setQueryForGridBinding(sparqlGrid, dataSource.asQef(), labelMgr, mappedQuery);

        TableMapperComponent tm = new TableMapperComponent(dataSource, baseConcept, labelMgr);
        this.add(tm);

        this.add(sparqlGrid);
    }
}

// MappedQuery mappedQuery = ElementGenerator.createQuery(baseConcept,
// treeDataProvider.getTreeData(), constraintIndex, path ->
// !Boolean.FALSE.equals(pathToVisibility.get(path)));
//Query query =
//RelationUtils.createQuery(null);
// VaadinSparqlUtils.setQueryForGridBinding(sparqlGrid, headerRow, qef, query);
// VaadinSparqlUtils.configureGridFilter(sparqlGrid, filterRow,
// query.getProjectVars(), var -> str -> VaadinSparqlUtils.createFilterExpr(var,
// str).orElse(null));
