package org.aksw.vaadin.app.demo.view.tablemapper;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.dataaccess.sparql.polyfill.datasource.RdfDataSourceWithBnodeRewrite;
import org.aksw.jenax.sparql.relation.api.UnaryRelation;
import org.aksw.jenax.vaadin.component.grid.sparql.TableMapperComponent;
import org.aksw.jenax.vaadin.label.LabelService;
import org.aksw.vaadin.app.demo.MainLayout;
import org.apache.jena.graph.Node;
import org.apache.jena.rdfconnection.RDFConnection;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "table", layout = MainLayout.class)
@PageTitle("Table Mapper Demo")
public class TableMapperView
    extends VerticalLayout
{
    // @Autowired
    // protected LabelService<Node, String> labelService;

    public TableMapperView(LabelService<Node, String> labelService) {

        // QueryExecutionFactoryQuery qef = query -> RDFConnection.connect("http://localhost:8642/sparql").query(query);
        UnaryRelation baseConcept = new Concept(ElementUtils.createElementTriple(Vars.x, Vars.y, Vars.z), Vars.x);

        RdfDataSource base = () -> RDFConnection.connect("http://localhost:8642/sparql");

        RdfDataSource dataSource = base
                .decorate(RdfDataSourceWithBnodeRewrite::wrapWithAutoBnodeProfileDetection)
                // .decorate(RdfDataSourceWithLocalCache::new)
                ;


        TableMapperComponent tableMapper = new TableMapperComponent(dataSource, baseConcept, labelService);
        add(tableMapper);

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
