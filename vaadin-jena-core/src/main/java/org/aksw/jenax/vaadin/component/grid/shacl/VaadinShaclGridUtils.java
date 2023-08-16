package org.aksw.jenax.vaadin.component.grid.shacl;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.aksw.commons.util.direction.Direction;
import org.aksw.commons.util.io.out.OutputStreamUtils;
import org.aksw.jena_sparql_api.algebra.expr.transform.ExprTransformVirtualBnodeUris;
import org.aksw.jena_sparql_api.vaadin.data.provider.DataProviderNodeQuery;
import org.aksw.jena_sparql_api.vaadin.data.provider.DataRetriever;
import org.aksw.jenax.arq.datashape.viewselector.EntityClassifier;
import org.aksw.jenax.arq.util.node.NodeUtils;
import org.aksw.jenax.arq.util.triple.GraphUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.connection.datasource.RdfDataSource;
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
import org.aksw.jenax.vaadin.label.VaadinLabelMgr;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.modify.request.QuadAcc;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.Template;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class VaadinShaclGridUtils {

    public static DataRetriever setupRetriever(RdfDataSource dataSource, Model shaclModel) {
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


        DataRetriever retriever = new DataRetriever(dataSource, entityClassifier);

        for (ShNodeShape nodeShape : nodeShapes) {
            Node nodeShapeNode = nodeShape.asNode();

            if (nodeShapeNode.isBlank()) {
                nodeShapeNode = ExprTransformVirtualBnodeUris.bnodeToIri(nodeShapeNode);
            }


            // NodeQuery nq = NodeQueryImpl.newRoot();
            NodeQuery nqq = NodeQueryImpl.newRoot();
            ElementGeneratorLateral.toNodeQuery(nqq, nodeShape);
            retriever.getClassToQuery().put(nodeShapeNode, nqq);
        }

        return retriever;
    }

    public static DataProviderNodeQuery fromShacl(RdfDataSource dataSource, Supplier<UnaryRelation> conceptSupplier, Model shaclModel) {
        DataRetriever dataRetriever = setupRetriever(dataSource, shaclModel);
        DataProviderNodeQuery dataProvider = new DataProviderNodeQuery(dataSource, conceptSupplier, dataRetriever);

//        List<RDFNode> list = dataProvider.fetch(new com.vaadin.flow.data.provider.Query<>()).collect(Collectors.toList());
//        for (RDFNode item : list) {
//            System.out.println(item);
//            RDFDataMgr.write(System.out, item.getModel(), RDFFormat.TURTLE_PRETTY);
//        }

        return dataProvider;

    }


    public static void configureGrid(Grid<RDFNode> grid, DataProviderNodeQuery dataProvider, LabelService<Node, String> labelService) {
        Supplier<UnaryRelation> conceptSupplier = dataProvider.getConceptSupplier();
        UnaryRelation concept = conceptSupplier.get();
        Var var = concept.getVar();
        String varName = var.getName();

        Column<RDFNode> column = grid.addComponentColumn(val -> {
            Graph g = val.getModel().getGraph();
            Set<Node> allNodes = GraphUtils.streamNodes(g).collect(Collectors.toSet());
            // str = str.replace("\\n", "<br />");
            Component head = VaadinLabelMgr.forHasText(labelService, new H3(), val.asNode());

            Component body = VaadinLabelMgr.forHasText(labelService, new Pre(), allNodes, map -> {
                Graph h = GraphFactory.createDefaultGraph();
                GraphUtils.stream(g).forEach(t -> {
                    Triple x = Triple.create(
                        labelNode(t.getSubject(), map, NodeFactory::createLiteral),
                        labelNode(t.getPredicate(), map, NodeFactory::createURI),
                        labelNode(t.getObject(), map, NodeFactory::createLiteral));
                    h.add(x);
                });
                String str = OutputStreamUtils.toString(out -> RDFDataMgr.write(out, ModelFactory.createModelForGraph(h), RDFFormat.TRIG_PRETTY), StandardCharsets.UTF_8);
                return str;
            });

            VerticalLayout card = new VerticalLayout();
            card.setWidthFull();
            card.addClassName("card");
            card.setSpacing(false);
            card.getThemeList().add("spacing-s");
            card.add(head);
            card.add(body);
            return card;
        });

        column.setKey(varName);
    }

    public static Node labelNode(Node node, Map<Node, String> nodeToLabel, Function<String, Node> ctor) {
        Node result;
        String label = nodeToLabel.get(node);
        result = label == null
                ? node
                : ctor.apply(label);
        return result;
    }
}
