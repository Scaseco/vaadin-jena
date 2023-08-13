package org.aksw.vaadin.app.demo.view.shaclgrid;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.aksw.commons.util.direction.Direction;
import org.aksw.commons.util.io.out.OutputStreamUtils;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.vaadin.data.provider.DataProviderNodeQuery;
import org.aksw.jena_sparql_api.vaadin.data.provider.DataRetriever;
import org.aksw.jenax.arq.connection.core.QueryExecutionFactories;
import org.aksw.jenax.arq.datashape.viewselector.EntityClassifier;
import org.aksw.jenax.arq.datasource.RdfDataSourceWithBnodeRewrite;
import org.aksw.jenax.arq.util.node.NodeUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.connection.datasource.RdfDataSource;
import org.aksw.jenax.connection.query.QueryExecutionFactoryQuery;
import org.aksw.jenax.model.shacl.domain.ShNodeShape;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetStep;
import org.aksw.jenax.sparql.relation.api.UnaryRelation;
import org.aksw.jenax.treequery2.api.NodeQuery;
import org.aksw.jenax.treequery2.api.RelationQuery;
import org.aksw.jenax.treequery2.impl.ElementGeneratorLateral;
import org.aksw.jenax.treequery2.impl.FacetPathMappingImpl;
import org.aksw.jenax.treequery2.impl.NodeQueryImpl;
import org.aksw.jenax.vaadin.label.LabelService;
import org.aksw.vaadin.app.demo.MainLayout;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.modify.request.QuadAcc;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.Template;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.topbraid.shacl.model.SHFactory;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Pre;
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
        List<ShNodeShape> nodeShapes = org.aksw.jenax.model.shacl.util.ShUtils.listNodeShapes(shaclModel);

        EntityClassifier entityClassifier = new EntityClassifier(Arrays.asList(Vars.s));

        // https://www.w3.org/TR/shacl/#targets
        // "The target of a shape is the union of all RDF terms produced by the individual targets that are declared by the shape in the shapes graph."
        for (ShNodeShape nodeShape : nodeShapes) {
            EntityClassifier.registerNodeShape(entityClassifier, nodeShape);
        }

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


          org.aksw.jenax.treequery2.api.FacetPathMapping fpm = new FacetPathMappingImpl();
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
        RdfDataSource rdfDataSource = RdfDataSourceWithBnodeRewrite.wrapWithAutoBnodeProfileDetection(rdfDataSourceRaw);
        QueryExecutionFactoryQuery qef = QueryExecutionFactories.of(rdfDataSource);

        Supplier<UnaryRelation> conceptSupplier = () -> ConceptUtils.createSubjectConcept();
        DataRetriever retriever = new DataRetriever(qef, entityClassifier);



        for (ShNodeShape nodeShape : nodeShapes) {
            // NodeQuery nq = NodeQueryImpl.newRoot();
            NodeQuery nqq = NodeQueryImpl.newRoot();

            ElementGeneratorLateral.toNodeQuery(nqq, nodeShape);
            retriever.getClassToQuery().put(nodeShape.asNode(), nqq);
        }

        DataProviderNodeQuery dataProvider = new DataProviderNodeQuery(qef, conceptSupplier, retriever);

        List<RDFNode> list = dataProvider.fetch(new com.vaadin.flow.data.provider.Query<>()).collect(Collectors.toList());
        for (RDFNode item : list) {
            System.out.println(item);
            RDFDataMgr.write(System.out, item.getModel(), RDFFormat.TURTLE_PRETTY);
        }

        return dataProvider;

    }



    // @Autowired
    // protected LabelService<Node, String> labelService;

    public ShaclGridView(LabelService<Node, String> labelService) {

        DataProviderNodeQuery dataProvider = setup();

        Grid<RDFNode> grid = new Grid<>();
        // VaadinSparqlUtils.configureGridFilter(grid, filterRow, vars, var -> str -> VaadinSparqlUtils.createFilterExpr(var, str).orElse(null));

        Column<RDFNode> column = grid.addComponentColumn(val -> {
            String str = OutputStreamUtils.toString(out -> RDFDataMgr.write(out, val.getModel(), RDFFormat.TRIG_PRETTY), StandardCharsets.UTF_8);
            // str = str.replace("\\n", "<br />");
            Div r = new Div();

            r.add(new H2("" + val));
            // Paragraph p = new Paragraph(str);
            Pre p = new Pre();
            p.setText(str);
            // p.getStyle().set("white-space", "pre-line");
            r.add(p);

            return r;
        });

        column.setKey("s");

        HeaderRow filterRow = grid.appendHeaderRow();
        // grid.setDataProvider(VaadinSparqlUtils.withSparqlFilter(dataProvider));
        grid.setDataProvider(dataProvider);
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
