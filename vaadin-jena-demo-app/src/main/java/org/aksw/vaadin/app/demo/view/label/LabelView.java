package org.aksw.vaadin.app.demo.view.label;

import java.util.List;

import org.aksw.jena_sparql_api.common.DefaultPrefixes;
import org.aksw.jenax.arq.connection.core.QueryExecutionFactory;
import org.aksw.jenax.arq.datasource.RdfDataEngineFromDataset;
import org.aksw.jenax.connection.datasource.RdfDataSource;
import org.aksw.jenax.connection.query.QueryExecutionFactoryDataset;
import org.aksw.jenax.dataaccess.LabelUtils;
import org.aksw.jenax.vaadin.label.VaadinLabelMgr;
import org.aksw.vaadin.app.demo.MainLayout;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.DCTerms;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "label", layout = MainLayout.class)
@PageTitle("Label Demo")
public class LabelView
    extends VerticalLayout
{
    protected VaadinLabelMgr<Node, String> labelService;

    public LabelView() {
        Dataset dataset = RDFDataMgr.loadDataset("linkedgeodata-2018-04-04.dcat.ttl");

        List<Node> subjects = dataset.getDefaultModel().listSubjects().mapWith(RDFNode::asNode).toList();

        // Node uri = NodeFactory.createURI("http://dcat.linkedgeodata.org/distribution/osm-bremen-2018-04-04-nodes-public-transport-thing");
        RdfDataSource rdfDataSource = RdfDataEngineFromDataset.create(dataset, true);
        QueryExecutionFactory qef = new QueryExecutionFactoryDataset(dataset); // RDFConnection.connect(dataset);


        labelService = new VaadinLabelMgr<>(LabelUtils.getLabelLookupService(qef, DCTerms.description, DefaultPrefixes.get(), 30));
        for (Node node : subjects) {
            add(labelService.forHasText(new H1("Welcome"), node));
        }
    }
}
