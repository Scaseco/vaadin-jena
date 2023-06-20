package org.aksw.vaadin.app.demo.view.edit.propertylist;

import org.aksw.jenax.vaadin.component.grid.sparql.TableMapperComponent;
import org.aksw.jenax.vaadin.label.VaadinLabelMgr;
import org.aksw.vaadin.app.demo.MainLayout;
import org.apache.jena.graph.Node;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "table", layout = MainLayout.class)
@PageTitle("Table Mapper Demo")
public class TableMapperView
    extends VerticalLayout
{
    protected VaadinLabelMgr<Node, String> labelService;

    public TableMapperView() {
        TableMapperComponent tableMapper = new TableMapperComponent();
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
