package org.aksw.jenax.treequery2.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import org.aksw.commons.collections.generator.Generator;
import org.aksw.commons.collections.generator.GeneratorFromFunction;
import org.aksw.commons.util.direction.Direction;
import org.aksw.commons.util.list.ListUtils;
import org.aksw.facete.v3.api.FacetPathMapping;
import org.aksw.facete.v3.api.TreeData;
import org.aksw.facete.v4.impl.ElementGenerator;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.rx.entity.engine.EntityQueryRx;
import org.aksw.jena_sparql_api.rx.entity.model.EntityBaseQuery;
import org.aksw.jena_sparql_api.rx.entity.model.EntityQueryImpl;
import org.aksw.jena_sparql_api.rx.entity.model.EntityTemplateImpl;
import org.aksw.jena_sparql_api.schema.ShUtils;
import org.aksw.jenax.arq.connection.core.QueryExecutionFactories;
import org.aksw.jenax.arq.util.node.NodeUtils;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.model.shacl.domain.ShNodeShape;
import org.aksw.jenax.model.shacl.domain.ShPropertyShape;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetStep;
import org.aksw.jenax.sparql.path.PathUtils;
import org.aksw.jenax.treequery2.api.NodeQuery;
import org.aksw.jenax.treequery2.api.RelationQuery;
import org.aksw.jenax.treequery2.old.NodeQueryOld;
import org.aksw.jenax.vaadin.component.grid.sparql.DynamicInjectiveFunction;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.modify.request.QuadAcc;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementLateral;
import org.apache.jena.sparql.syntax.ElementService;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.Template;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.topbraid.shacl.model.SHFactory;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;


public class ElementGeneratorLateral {

    static { JenaSystem.init(); }
    // protected PropertyResolver propertyResolver;

    /**
     * SELECT (?key1 ... ?keyN)    ?s ?p ?o
     *
     * (base relation with ?key1 ... ?keyN and roots ?root1 ... ?rootM) (the sets of keys and roots are not required to be disjoint)
     * LATERAL { # For each unique key combination
     *     { # Union over all roots
     *       BIND("root1" AS ?root)
     *       ?root1 :p1 ?b .
     *       LATERAL {
     *           { BIND(?a AS ?s) BIND(:p1 AS ?p) BIND(?b AS ?o) }
     *         UNION
     *           {
     *             ?b :p2 ?c.
     *             LATERAL {
     *                 { BIND(?b AS ?s) BIND(:p2 AS ?p) BIND(?c AS ?o) }
     *               UNION
     *                 {
     *
     *                 }
     *             }
     *           }
     *         }
     *     }
     *   UNION
     *     {
     *     }
     *  }
     *}
     */
    public static Element createElement(TreeData<FacetPath> tree, String rootVar, FacetPathMapping pathMapping) {
        // createElement(tree, current);
        // pathMapping.allocate(null)
        throw new UnsupportedOperationException("finish this");
    }

    public Element createElement(NodeQueryOld current) {
        // worker.allocateElement(null)
        // UnaryRelation baseConcept = new Concept(baseRelation.getElement(), rootVar);
        Var rootVar = Var.alloc("root");
        Generator<Var> varGen = GeneratorFromFunction.createInt().map(i -> Var.alloc("vv" + i));
        DynamicInjectiveFunction<FacetPath, Var> ifn = DynamicInjectiveFunction.of(varGen);
        ifn.getMap().put(FacetPath.newAbsolutePath(), rootVar);

        FacetPathMapping fpm = ifn::apply;
        ElementGenerator eltGen = new ElementGenerator(fpm, HashMultimap.create(), FacetPath.newAbsolutePath());

        ElementGenerator.Worker worker = eltGen.createWorker();

        // Traverser.forTree(treeData::getChildren).depthFirstPreOrder(treeData.getRootItems()).forEach(eltGen::addPath);
        Element result = createElementOld(worker, rootVar, current);
        // MappedElement result = worker.createElement();
        return result;
    }

    /**
     * The paths in the tree is what is being projected.
     *
     * @param tree
     * @param current
     * @return
     */
    public  Element createElementOld(ElementGenerator.Worker worker, Var parentVar, NodeQueryOld current) {
        FacetPath path = current.getPath();
        FacetStep step = ListUtils.lastOrNull(path.getSegments());

        List<Element> unionMembers = new ArrayList<>();

        Var targetVar;
        Element nodeElement;
        if (step != null) {
            // Node p = step.getNode();
            // Create the element for this node
            targetVar = worker.getElementGenerator().getPathMapping().allocate(path);
            Var predicateVar = worker.getElementGenerator().getPathMapping().allocate(path.resolveSibling(FacetStep.of(step.getNode(), step.getDirection(), step.getAlias(), FacetStep.PREDICATE)));

            nodeElement = worker.createElementForLastStep(parentVar, targetVar, path); // createElement(worker, targetVar, child);

            Long limit = current.limit();
            Long offset = current.offset();
            if (limit != null || offset != null) {
                Query subQuery = new Query();
                subQuery.setQuerySelectType();
                subQuery.addProjectVars(Arrays.asList(parentVar, targetVar));
                subQuery.setLimit(limit == null ? Query.NOLIMIT : limit);
                subQuery.setOffset(offset == null ? Query.NOLIMIT : offset);
                subQuery.setQueryPattern(nodeElement);
                nodeElement = new ElementSubQuery(subQuery);
            }

            boolean applyCache = false;
            if (applyCache) {
                nodeElement = new ElementService("bulk+10:cache:", nodeElement);
            }

            // If there is limit, slice, filter or order then create an appropriate sub-query
            // Bind the parent variable to '?s'
            ElementBind bindS = new ElementBind(Vars.s, new ExprVar(parentVar));

            // Bind the predicate to ?p
            ElementBind bindP = new ElementBind(Vars.p, NodeValue.makeString(predicateVar.getName()));

            // Bind element's target to ?o
            ElementBind bindO = new ElementBind(Vars.o, new ExprVar(targetVar));

            ElementGroup bindSpoGroup = new ElementGroup();
            bindSpoGroup.addElement(bindS);
            bindSpoGroup.addElement(bindP);
            bindSpoGroup.addElement(bindO);


            // AggBuilder.map


            // Add the bindSpoGroup as the first union member
            unionMembers.add(bindSpoGroup);
        } else {
            nodeElement = new ElementGroup();
            targetVar = parentVar;
        }

        // Add any children
        Collection<NodeQueryOld> children = current.getChildren();
        for (NodeQueryOld child : children) {
            Element elt = createElementOld(worker, targetVar, child);
            unionMembers.add(elt);
        }
        Element union = ElementUtils.unionIfNeeded(unionMembers);

        // Create the lateral group for this node
        ElementLateral lateralUnion = new ElementLateral(union);

        // Create the group for this node
        ElementGroup group = new ElementGroup();
        ElementUtils.copyElements(group, nodeElement);
        ElementUtils.copyElements(group, lateralUnion);
        Element result = ElementUtils.flatten(group);
        return result;
    }



    public  Element createElement(RelationQuery current) {
        // FacetPath path = current.getPath();
        // FacetStep step = ListUtils.lastOrNull(path.getSegments());

        List<Element> unionMembers = new ArrayList<>();

        Var parentVar = current.source().var();

        Var targetVar = current.target().var();
        Element nodeElement;
        // if (step != null) {
            // Node p = step.getNode();
            // Create the element for this node

            nodeElement = current.getRelation().getElement();

            Long limit = current.limit();
            Long offset = current.offset();
            if (limit != null || offset != null) {
                Query subQuery = new Query();
                subQuery.setQuerySelectType();
                subQuery.addProjectVars(Arrays.asList(parentVar, targetVar));
                subQuery.setLimit(limit == null ? Query.NOLIMIT : limit);
                subQuery.setOffset(offset == null ? Query.NOLIMIT : offset);
                subQuery.setQueryPattern(nodeElement);
                nodeElement = new ElementSubQuery(subQuery);
            }

            boolean applyCache = false;
            if (applyCache) {
                nodeElement = new ElementService("bulk+10:cache:", nodeElement);
            }

            Var s = current.source().var();
            Var p = current.target().var();
            Var o = current.target().var();

            // If there is limit, slice, filter or order then create an appropriate sub-query
            // Bind the parent variable to '?s'
            ElementBind bindS = new ElementBind(Vars.s, new ExprVar(s));

            // Bind the predicate to ?p
            ElementBind bindP = new ElementBind(Vars.p, NodeValue.makeString(p.getName()));

            // Bind element's target to ?o
            ElementBind bindO = new ElementBind(Vars.o, new ExprVar(targetVar));

            ElementGroup bindSpoGroup = new ElementGroup();
            bindSpoGroup.addElement(bindS);
            bindSpoGroup.addElement(bindP);
            bindSpoGroup.addElement(bindO);


            // AggBuilder.map


            // Add the bindSpoGroup as the first union member
            unionMembers.add(bindSpoGroup);
//        } else {
//            nodeElement = new ElementGroup();
//            targetVar = parentVar;
//        }

        // Add any children
        Collection<NodeQuery> children = current.roots();
        for (NodeQuery child : children) {
            for (RelationQuery subRq : child.children().values()) {
                Element elt = createElement(subRq);
                unionMembers.add(elt);
            }
        }
        Element union = ElementUtils.unionIfNeeded(unionMembers);

        // Create the lateral group for this node
        ElementLateral lateralUnion = new ElementLateral(union);

        // Create the group for this node
        ElementGroup group = new ElementGroup();
        ElementUtils.copyElements(group, nodeElement);
        ElementUtils.copyElements(group, lateralUnion);
        Element result = ElementUtils.flatten(group);
        return result;
    }

    public static void toNodeQuery(NodeQuery nodeQuery, ShNodeShape nodeShape) {
        for (ShPropertyShape propertyShape : nodeShape.getProperties()) {
            Resource pathResource = propertyShape.getPath();
            Path sparqlPath = ShUtils.assemblePath(pathResource);
            System.err.println("GOT PATH: " + sparqlPath);

            BiMap<Node, Path> iriToPath = HashBiMap.create();
            Generator<Node> pGen = Generator.create("urn:p")
                    .map(NodeFactory::createURI).filterDrop(iriToPath::containsKey);
            try {
                List<P_Path0> steps = PathUtils.toList(sparqlPath);
                NodeQuery current = nodeQuery;
                for (P_Path0 step : steps) {
                    FacetStep facetStep = FacetStep.of(step.getNode(), Direction.ofFwd(step.isForward()), null, FacetStep.TARGET);
                    current = current.getOrCreateChild(facetStep);
                }
            } catch (UnsupportedOperationException e) {
                Node iri = iriToPath.inverse().computeIfAbsent(sparqlPath, sp -> pGen.next());
                nodeQuery.getOrCreateChild(FacetStep.fwd(iri));
                Element elt = ElementUtils.createElementPath(Vars.s, sparqlPath, Vars.o);
                // FIXME Register the iri with sparql path in the nodeQuery context
            }
        }
    }

    /**
     * Util method to extract all properties regardless of the
     * Logical Constraint Components sh:not, sh:and, sh:or and sh:xone
     *
     * TODO Introduce a visitor?
     *
     * https://www.w3.org/TR/shacl/#shapes-recursion
     */
    public static List<ShPropertyShape> getPropertyShapes(ShNodeShape nodeShape) {
        for (ShPropertyShape propertyShape : nodeShape.getProperties()) {
            Resource pathResource = propertyShape.getPath();
            Path sparqlPath = ShUtils.assemblePath(pathResource);
            System.err.println("GOT PATH: " + sparqlPath);
        }

        //

        return null;
    }

    public static void main2(String[] args) {
        Query concept = QueryFactory.create("SELECT ?s ?p ?o { ?s ?p ?o }");

//        boolean materialize = false;
//        if (materialize) {
//            Query c = concept;
//            Table table = QueryExecutionUtils.execSelectTable(() -> QueryExecutionFactory.create(c, model));
//
//            Query tmp = QueryFactory.create("SELECT DISTINCT ?s {}");
//            tmp.setQueryPattern(new ElementData(table.getVars(), Lists.newArrayList(table.rows())));
//            concept = tmp;
//        }

        // raw.setValuesDataBlock(table.getVars(), Lists.newArrayList(table.rows()));
        EntityBaseQuery ebq = new EntityBaseQuery(Arrays.asList(Vars.s, Vars.o), new EntityTemplateImpl(), concept);
        System.out.println(ebq);

        EntityQueryImpl eq = new EntityQueryImpl();
        eq.setBaseQuery(ebq);
        System.out.println(eq);

        RDFConnection conn = RDFConnection.connect(DatasetFactory.create());
        EntityQueryRx.execConstructEntitiesNg(QueryExecutionFactories.of(conn), eq).toList().blockingGet();

    }

    public static void main(String[] args) {
        SHFactory.ensureInited();
        Model shaclModel = RDFDataMgr.loadModel("/home/raven/Projects/Eclipse/rmltk-parent/r2rml-resource-shacl/src/main/resources/r2rml.core.shacl.ttl");
        List<ShNodeShape> nodeShapes = org.aksw.jenax.model.shacl.util.ShUtils.listNodeShapes(shaclModel);

        if (false) {
            for (ShNodeShape nodeShape : nodeShapes) {
                // NodeQuery nq = NodeQueryImpl.newRoot();
                NodeQuery nq = NodeQueryImpl.newRoot();

                toNodeQuery(nq, nodeShape);
                Element elt = new ElementGeneratorLateral().createElement(nq.relationQuery());
                System.out.println("Shape: " + nodeShape.asNode() + " --------");
                System.out.println(elt);
            }
        }

//        NodeQuery nq = NodeQueryImpl.newRoot();


        RelationQuery rq = RelationQuery.of(ConceptUtils.createSubjectConcept());
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

        FacetPath ppath = p.getPath();
        System.out.println("p path: " + ppath);
        System.out.println("x path: " + x.getPath());

        System.out.println("p relation: " + p.relationQuery().getRelation());

        Var rootVar = Var.alloc("root");
        NodeQuery nq = rq.roots().get(0);

          nq
          .fwd("urn:p1_1")
              .bwd("urn:p2_1").limit(10l)
          .getRoot()
              .fwd("urn:1_2").limit(30l);

        Element elt = new ElementGeneratorLateral().createElement(nq.relationQuery());

        Query query = new Query();
        query.setConstructTemplate(new Template(new QuadAcc(Arrays.asList(Quad.create(rootVar, Vars.s, Vars.p, Vars.o)))));
        query.setQueryConstructType();
        query.setQueryPattern(elt);


        System.out.println(query);
    }
}
