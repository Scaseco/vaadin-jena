package org.aksw.vaadin.app.demo.view.shaclgrid;

import java.util.function.Supplier;

import org.aksw.commons.util.obj.Enriched;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.vaadin.data.provider.DataProviderNodeQuery;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.model.shacl.util.ShTemplateRegistry;
import org.aksw.jenax.sparql.relation.api.UnaryRelation;
import org.aksw.jenax.vaadin.component.grid.shacl.VaadinShaclGridUtils;
import org.aksw.jenax.vaadin.label.LabelService;
import org.aksw.vaadin.app.demo.MainLayout;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.topbraid.shacl.model.SHFactory;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "shacl", layout = MainLayout.class)
@PageTitle("Table Mapper Demo")
public class ShaclGridView
    extends VerticalLayout
{
    public DataProviderNodeQuery setup() {
        SHFactory.ensureInited();
        Model shaclModel = RDFDataMgr.loadModel("/home/raven/Projects/Eclipse/rmltk-parent/r2rml-resource-shacl/src/main/resources/r2rml.core.shacl.ttl");
        RdfDataSource dataSource = () -> RDFConnection.connect("http://localhost:8642/sparql");
        Supplier<UnaryRelation> conceptSupplier = () -> ConceptUtils.createSubjectConcept();

        return VaadinShaclGridUtils.fromShacl(dataSource, conceptSupplier, shaclModel);
    }

    // @Autowired
    // protected LabelService<Node, String> labelService;
    public ShaclGridView(LabelService<Node, String> labelService) {

        DataProviderNodeQuery dataProvider = setup();

        Grid<Enriched<RDFNode>> grid = new Grid<>();
        VaadinShaclGridUtils.configureGrid(grid, dataProvider, new ShTemplateRegistry(), labelService);
        grid.setDataProvider(dataProvider);
        // VaadinSparqlUtils.configureGridFilter(grid, filterRow, vars, var -> str -> VaadinSparqlUtils.createFilterExpr(var, str).orElse(null));

        HeaderRow filterRow = grid.appendHeaderRow();
        // grid.setDataProvider(VaadinSparqlUtils.withSparqlFilter(dataProvider));
        // grid.setDataProvider(dataProvider);
        // VaadinSparqlUtils.configureGridFilter(grid, filterRow, Arrays.asList(Vars.s), var -> str -> VaadinSparqlUtils.createFilterExpr(var, str).orElse(null));


        add(grid);


//        TableMapperComponent tableMapper = new TableMapperComponent(labelService);
//        add(tableMapper);

//        Dataset dataset = RDFDataMgr.loadDataset("linkedgeodata-2018-04-04.dcat.ttl");
//
//        List<Node> subjects = dataset.getDefaultModel().listSubjects().mapWith(RDFNode::asNode).toList();
//
//        // Node uri = NodeFactory.createURI("http://dcat.linkedgeodata.org/distribution/osm-bremen-2018-04-04-nodes-public-transport-thing");
//        RdfDataSource rdfDataSource = RdfDataEngineFromDataset.create(dataset, true);
//        QueryExecutionFactory qef = new QueryExecutionFactoryDataset(dataset); // RDFConnection.connect(dataset);
//
//
//        labelService = new VaadinLabelMgr<>(LabelUtils.getLabelLookupService(qef, DCTerms.description, DefaultPrefixes.get()));
//        for (Node node : subjects) {
//            add(labelService.forHasText(new H1("Welcome"), node));
//        }
    }
}
