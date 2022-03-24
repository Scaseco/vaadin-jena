package org.aksw.vaadin.app.demo.view.label;

import org.aksw.jena_sparql_api.common.DefaultPrefixes;
import org.aksw.jenax.arq.datasource.RdfDataSourceFromDataset;
import org.aksw.jenax.connection.datasource.RdfDataSource;
import org.aksw.jenax.dataaccess.LabelUtils;
import org.aksw.jenax.vaadin.label.VaadinLabelService;
import org.aksw.vaadin.app.demo.MainLayout;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
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
    protected VaadinLabelService<Node, String> labelService;

    public LabelView() {
        Dataset dataset = RDFDataMgr.loadDataset("linkedgeodata-2018-04-04.dcat.ttl");
        Node uri = NodeFactory.createURI("http://dcat.linkedgeodata.org/distribution/osm-bremen-2018-04-04-nodes-public-transport-thing");
        RdfDataSource rdfDataSource = RdfDataSourceFromDataset.create(dataset, true);
        SparqlQueryConnection conn = RDFConnection.connect(dataset);


        labelService = new VaadinLabelService<>(LabelUtils.getLabelLookupService(conn, DCTerms.description, DefaultPrefixes.get()));
        add(labelService.forHasText(new H1("Welcome"), uri));
    }
}
