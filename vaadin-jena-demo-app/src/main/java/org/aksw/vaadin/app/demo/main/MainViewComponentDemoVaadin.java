package org.aksw.vaadin.app.demo.main;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.aksw.commons.path.core.Path;
import org.aksw.jena_sparql_api.collection.observable.GraphChange;
import org.aksw.jena_sparql_api.entity.graph.metamodel.ClassRelationModel;
import org.aksw.jena_sparql_api.entity.graph.metamodel.DatasetMetamodel;
import org.aksw.jena_sparql_api.entity.graph.metamodel.GraphPredicateStats;
import org.aksw.jena_sparql_api.entity.graph.metamodel.PredicateStats;
import org.aksw.jena_sparql_api.entity.graph.metamodel.RGDMetamodel;
import org.aksw.jena_sparql_api.entity.graph.metamodel.ResourceGraphMetamodel;
import org.aksw.jena_sparql_api.entity.graph.metamodel.ResourceGraphPropertyMetamodel;
import org.aksw.jena_sparql_api.entity.graph.metamodel.ResourceMetamodel;
import org.aksw.jena_sparql_api.lookup.LookupService;
import org.aksw.jena_sparql_api.mapper.proxy.JenaPluginUtils;
import org.aksw.jena_sparql_api.mapper.util.LabelUtils;
import org.aksw.jena_sparql_api.path.datatype.RDFDatatypePPath;
import org.aksw.jena_sparql_api.path.datatype.RDFDatatypePathNode;
import org.aksw.jena_sparql_api.schema.NodeSchema;
import org.aksw.jena_sparql_api.schema.NodeSchemaDataFetcher;
import org.aksw.jena_sparql_api.schema.NodeSchemaFromNodeShape;
import org.aksw.jena_sparql_api.schema.PropertySchemaFromPropertyShape;
import org.aksw.jena_sparql_api.schema.ResourceCache;
import org.aksw.jena_sparql_api.schema.ResourceExplorer;
import org.aksw.jena_sparql_api.schema.SHAnnotatedClass;
import org.aksw.jena_sparql_api.schema.ShapedNode;
import org.aksw.vaadin.component.rdf_term_editor.RdfTermEditor;
import org.aksw.vaadin.shacl.ShaclTreeGrid;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.claspina.confirmdialog.ButtonOption;
import org.claspina.confirmdialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.topbraid.shacl.model.SHFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

@Route("")
@PWA(name = "Semantic Components Vaadin Demonstrator", shortName = "Scaseco Demo",
        description = "This is a demonstrator of components for semantic data.", enableInstallPrompt = true)
@CssImport(value = "./styles/shared-styles.css", include = "lumo-badge")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
@CssImport(value = "./styles/vaadin-grid-styles.css", themeFor = "vaadin-grid")
//@CssImport(value = "./styles/vaadin-tab-styles.css", themeFor = "vaadin-tab")
@CssImport(value = "./styles/vaadin-select-text-field-styles.css", themeFor = "vaadin-select-text-field")
@CssImport(value = "./styles/vaadin-select-styles.css", themeFor = "vaadin-select")
@CssImport(value = "./styles/vaadin-text-area-styles.css", themeFor = "vaadin-text-area")
@CssImport(value = "./styles/flow-component-renderer-styles.css", themeFor = "flow-component-renderer")
@CssImport(value = "./styles/vaadin-grid-tree-toggle-styles.css", themeFor = "vaadin-grid-tree-toggle")
@JsModule("@vaadin/vaadin-lumo-styles/presets/compact.js")
@Theme(value = Lumo.class)
@JsModule("@vaadin/vaadin-lumo-styles/badge.js")
//@HtmlImport(value="frontend://bower_components/vaadin-lumo-styles/badge.html")
public class MainViewComponentDemoVaadin extends AppLayout {
        protected static final long serialVersionUID = 1;

        @Autowired
        public MainViewComponentDemoVaadin() {
            JenaSystem.init();
            ConfirmDialog.setButtonDefaultIconsVisible(false);


            HorizontalLayout navbarLayout = new HorizontalLayout();
            navbarLayout.setWidthFull();
            navbarLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

            Button appSettingsBtn = new Button(new Icon(VaadinIcon.COG));
            navbarLayout.add(appSettingsBtn);

            addToNavbar(navbarLayout);

            VerticalLayout mainPanel = new VerticalLayout();
            mainPanel.setSizeFull();

//            RdfTermEditor editor = new RdfTermEditor();
//            editor.setValue(RDF.Nodes.type);
//
//            mainPanel.add(editor);


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
            SparqlQueryConnection conn = RDFConnectionFactory.connect(ds);
            ShapedNode sn = ShapedNode.create(datasetNode, schema, resourceCache, conn);
//            LookupService<Node, ResourceMetamodel> metaDataService = ResourceExplorer.createMetamodelLookup(conn);

            Multimap<NodeSchema, Node> mm = HashMultimap.create();
            mm.put(schema, datasetNode);

            NodeSchemaDataFetcher dataFetcher = new NodeSchemaDataFetcher();
//            dataFetcher.sync(mm, conn, metaDataService, resourceCache);

            List<ShapedNode> rootNodes = Collections.singletonList(sn);

            Model prefixes = RDFDataMgr.loadModel("rdf-prefixes/prefix.cc.2019-12-17.ttl");
            LookupService<Node, String> labelService =
                    LabelUtils.createLookupServiceForLabels(LabelUtils.getLabelLookupService(conn, RDFS.label, prefixes), prefixes, prefixes).cache();


            TreeGrid<Path<Node>> treeGrid = ShaclTreeGrid.createShaclEditor(
                    graphEditorModel, rootNodes, labelService);

            treeGrid.addClassName("compact");

            treeGrid.setSizeFull();

            mainPanel.add(treeGrid);





            Button submitBtn = new Button("Submit");
            submitBtn.addClickListener(ev -> {
                ConfirmDialog dialog = confirmDialog("Confirm Submit",
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

            setContent(mainPanel);



        }


        // Signature compatible with ConfirmDialog of vaadin pro
        public ConfirmDialog confirmDialog(String header, String text, String confirmText,
                Consumer<?> confirmListener,
                String cancelText,
                Consumer<?> cancelListener) {

            TextArea textArea = new TextArea();
            textArea.setValue(text);
            textArea.setReadOnly(true);
            // textArea.setEnabled(false);

            ConfirmDialog result = ConfirmDialog
                .create()
                .withCaption(header)
                .withMessage(textArea)
                .withOkButton(() -> confirmListener.accept(null), ButtonOption.focus(), ButtonOption.caption(confirmText), ButtonOption.closeOnClick(true))
                .withCancelButton(() -> cancelListener.accept(null), ButtonOption.caption(cancelText), ButtonOption.closeOnClick(true));

            return result;

        }

}