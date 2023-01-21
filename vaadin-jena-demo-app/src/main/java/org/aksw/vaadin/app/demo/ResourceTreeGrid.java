package org.aksw.vaadin.app.demo;

import java.util.Collections;
import java.util.List;

import org.aksw.commons.path.core.Path;
import org.aksw.commons.rx.lookup.LookupService;
import org.aksw.jena_sparql_api.collection.observable.GraphChange;
import org.aksw.jena_sparql_api.entity.graph.metamodel.ClassRelationModel;
import org.aksw.jena_sparql_api.entity.graph.metamodel.DatasetMetamodel;
import org.aksw.jena_sparql_api.entity.graph.metamodel.GraphPredicateStats;
import org.aksw.jena_sparql_api.entity.graph.metamodel.PredicateStats;
import org.aksw.jena_sparql_api.entity.graph.metamodel.RGDMetamodel;
import org.aksw.jena_sparql_api.entity.graph.metamodel.ResourceGraphMetamodel;
import org.aksw.jena_sparql_api.entity.graph.metamodel.ResourceGraphPropertyMetamodel;
import org.aksw.jena_sparql_api.entity.graph.metamodel.ResourceMetamodel;
import org.aksw.jena_sparql_api.schema.NodeSchema;
import org.aksw.jena_sparql_api.schema.NodeSchemaDataFetcher;
import org.aksw.jena_sparql_api.schema.NodeSchemaFromNodeShape;
import org.aksw.jena_sparql_api.schema.PropertySchemaFromPropertyShape;
import org.aksw.jena_sparql_api.schema.ResourceCache;
import org.aksw.jena_sparql_api.schema.SHAnnotatedClass;
import org.aksw.jena_sparql_api.schema.ShapedNode;
import org.aksw.jenax.arq.connection.core.QueryExecutionFactory;
import org.aksw.jenax.connection.query.QueryExecutionFactoryDataset;
import org.aksw.jenax.dataaccess.LabelUtils;
import org.aksw.jenax.path.datatype.RDFDatatypePPath;
import org.aksw.jenax.path.datatype.RDFDatatypePathNode;
import org.aksw.jenax.reprogen.core.JenaPluginUtils;
import org.aksw.vaadin.common.component.util.ConfirmDialogUtils;
import org.aksw.vaadin.shacl.ShaclTreeGrid;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.vocabulary.RDFS;
import org.claspina.confirmdialog.ConfirmDialog;
import org.topbraid.shacl.model.SHFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;

public class ResourceTreeGrid
    extends VerticalLayout
{
    public ResourceTreeGrid() {
        JenaSystem.init();
        ConfirmDialog.setButtonDefaultIconsVisible(false);


        HorizontalLayout navbarLayout = new HorizontalLayout();
        navbarLayout.setWidthFull();
        navbarLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        Button appSettingsBtn = new Button(new Icon(VaadinIcon.COG));
        navbarLayout.add(appSettingsBtn);

        add(navbarLayout);

        VerticalLayout mainPanel = new VerticalLayout();
        mainPanel.setSizeFull();

    //    RdfTermEditor editor = new RdfTermEditor();
    //    editor.setValue(RDF.Nodes.type);
    //
    //    mainPanel.add(editor);


        SHFactory.ensureInited();
        JenaPluginUtils.registerResourceClasses(
                NodeSchemaFromNodeShape.class,
                PropertySchemaFromPropertyShape.class,

                SHAnnotatedClass.class,

                ResourceMetamodel.class,
                ResourceGraphMetamodel.class,
                ResourceGraphPropertyMetamodel.class,
                PredicateStats.class,
                GraphPredicateStats.class,
                RGDMetamodel.class,

                ClassRelationModel.class,
                DatasetMetamodel.class

        );
        TypeMapper.getInstance().registerDatatype(new RDFDatatypePPath());
        TypeMapper.getInstance().registerDatatype(new RDFDatatypePathNode());


        Model shaclModel = RDFDataMgr.loadModel("dcat-ap_2.0.0_shacl_shapes.ttl");
        NodeSchema schema = shaclModel.createResource("http://data.europa.eu/r5r#Dataset_Shape").as(NodeSchemaFromNodeShape.class);
        GraphChange graphEditorModel = new GraphChange();
        Node datasetNode = NodeFactory.createURI("http://dcat.linkedgeodata.org/dataset/osm-bremen-2018-04-04");
        Dataset ds = RDFDataMgr.loadDataset("linkedgeodata-2018-04-04.dcat.ttl");
        ResourceCache resourceCache = new ResourceCache();
//        SparqlQueryConnection conn = RDFConnectionFactory.connect(ds);
        QueryExecutionFactory qef = new QueryExecutionFactoryDataset(ds);

        ShapedNode sn = ShapedNode.create(datasetNode, schema, resourceCache, qef);
    //    LookupService<Node, ResourceMetamodel> metaDataService = ResourceExplorer.createMetamodelLookup(conn);

        Multimap<NodeSchema, Node> mm = HashMultimap.create();
        mm.put(schema, datasetNode);

        NodeSchemaDataFetcher dataFetcher = new NodeSchemaDataFetcher();
    //    dataFetcher.sync(mm, conn, metaDataService, resourceCache);

        List<ShapedNode> rootNodes = Collections.singletonList(sn);

        Model prefixes = RDFDataMgr.loadModel("rdf-prefixes/prefix.cc.2019-12-17.ttl");
        LookupService<Node, String> labelService =
                LabelUtils.createLookupServiceForLabels(LabelUtils.getLabelLookupService(qef, RDFS.label, prefixes), prefixes, prefixes).cache();


        TreeGrid<Path<Node>> treeGrid = ShaclTreeGrid.createShaclEditor(
                graphEditorModel, rootNodes, labelService);

        treeGrid.addClassName("compact");

        treeGrid.setSizeFull();

        mainPanel.add(treeGrid);





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

        mainPanel.add(submitBtn);

        add(mainPanel);

    }
}
