package org.aksw.facete.v4.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.aksw.commons.collections.generator.Generator;
import org.aksw.commons.collections.generator.GeneratorFromFunction;
import org.aksw.facete.v3.api.FacetPathMapping;
import org.aksw.facete.v3.api.NodeFacetPath;
import org.aksw.jenax.arq.util.node.NodeUtils;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetPathOps;
import org.aksw.jenax.path.core.FacetStep;
import org.aksw.jenax.sparql.relation.api.Relation;
import org.aksw.jenax.sparql.relation.api.UnaryRelation;
import org.aksw.jenax.vaadin.component.grid.sparql.DynamicInjectiveFunction;
import org.aksw.jenax.vaadin.component.grid.sparql.MappedQuery;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Bound;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementOptional;
import org.apache.jena.vocabulary.VOID;

import com.vaadin.flow.data.provider.hierarchy.TreeData;

/**
 * Accumulator for elements. Elements are added to an ElementGroup that acts as a container
 * whereas the final Element may be a different element, such as an ElementOptional.
 */
class ElementAcc {
    protected Element rootElement;
    protected ElementGroup container;

    public ElementAcc(Element rootElement, ElementGroup container) {
        super();
        this.rootElement = rootElement;
        this.container = container;
    }

    public Element getRootElement() {
        return rootElement;
    }

    public ElementGroup getContainer() {
        return container;
    }
}


public class ElementGenerator {

    protected FacetPathMapping pathMapping;
    protected Element baseElement;
    protected Set<FacetPath> mandatoryElementIds = new HashSet<>();

    protected Map<FacetPath, ElementAcc> pathToAcc = new LinkedHashMap<>();


    public ElementGenerator(Element baseElement, FacetPathMapping pathMapping) {
        super();
        this.baseElement = baseElement;
        this.pathMapping = pathMapping;

        mandatoryElementIds.add(FacetPathOps.newAbsolutePath());
    }


    /** Mark a path as mandatory. This makes all parents also mandatory. */
    public void declareMandatoryPath(FacetPath path) {
        FacetPath current = path;
        while (current != null) {
            FacetPath eltId = makeAny(current);
            if (!mandatoryElementIds.contains(eltId)) {
                mandatoryElementIds.add(eltId);

                current = current.getParent();
            } else {
                break;
            }
        }
    }


    public static FacetStep makeAny(FacetStep step) {
        return FacetStep.isAny(step.getTargetComponent())
                ? step
                : new FacetStep(step.getNode(), step.isForward(), step.getAlias(), FacetStep.ANY);
    }

    public static FacetPath makeAny(FacetPath path) {
        FacetPath result = path.getNameCount() == 0
                ? path
                : path.resolveSibling(makeAny(path.getFileName().toSegment()));
        return result;
    }

    // protected Map<FacetPath, Element> pathToContrib;

    //
    protected Map<FacetPath, Relation> pathToRelation;

    /** Analyse the constraints for which paths are mandatory and which are optional */
    public void analysePathModality(Collection<Expr> constraints) {
        for (Expr expr : constraints) {
            analysePathModality(expr);
        }
    }

    public void analysePathModality(Expr expr) {
        Set<FacetPath> paths = NodeFacetPath.mentionedPaths(expr);
        for (FacetPath path : paths) {
            // TODO Check for absent / !bound constraints
            boolean isMandatory = true;

            if (isMandatory) {
                declareMandatoryPath(path);
            }
            //Traverser.<FacetPath>forTree(x -> x.getParent() == null ? Collections.emptyList() : Collections.singleton(x.getParent())).depthFirstPreOrder(elementId);
            // Get all parent elements and make them mandatory
            // mandatoryElementIds.add(elementId);
        }
    }

    public ElementAcc allocateEltAcc(Var parentVar, Var targetVar, FacetPath path) {
        FacetPath eid = makeAny(path);

        ElementGroup container = new ElementGroup();
        boolean isMandatory = mandatoryElementIds.contains(eid);
        Element root = isMandatory  ? container : new ElementOptional(container);

        Element result;
        Element coreElt = null;
        Node secondaryNode;
        if (!path.getSegments().isEmpty()) {
            FacetStep step = path.getFileName().toSegment();
            Node predicateNode = step.getNode();
            boolean isFwd = step.isForward();

            Integer c = step.getTargetComponent();

            if (NodeUtils.ANY_IRI.equals(predicateNode)) {
                Integer toggledComponent = FacetStep.isTarget(c) ? FacetStep.PREDICATE : FacetStep.TARGET;
                FacetStep s = step.copyStep(toggledComponent);
            // FacetStep toggledTarget = step.toggleTarget();
                secondaryNode = pathMapping.allocate(path.resolveSibling(s));
            } else {
                secondaryNode = predicateNode;
            }

            if (FacetStep.isTarget(c)) {
                coreElt = ElementUtils.createElementTriple(parentVar, secondaryNode, targetVar, isFwd);
            } else {
                coreElt = ElementUtils.createElementTriple(parentVar, targetVar, secondaryNode, isFwd);
            }

            container.addElement(coreElt);
        } else {
            // Add the base element if this is the root path
            container.addElement(baseElement);
        }
        return new ElementAcc(root, container);
    }

    public void accumulate(
            ElementGroup parentAcc,
            Var parentVar,
            FacetPath path, // TODO This should be the FacetNode and there might be subqueries
            Function<FacetPath, ? extends Iterable<FacetPath>> getChildren) {

        Element result;

        FacetPath eltId = makeAny(path);

        Var targetVar = pathMapping.allocate(path);
        ElementAcc elementAcc = pathToAcc.get(eltId);
        if (elementAcc == null) {
            elementAcc = allocateEltAcc(parentVar, targetVar, path);
            pathToAcc.put(eltId, elementAcc);
            // Create the ElementAcc for the path if it hasn't happened yet
            Iterable<FacetPath> children = getChildren.apply(path);
            if (children != null && children.iterator().hasNext()) {
                for (FacetPath subPath : children) {
                    // If there is no accumulator for the child then visit it
                    accumulate(elementAcc.getContainer(), targetVar, subPath, getChildren);
                }
            }

            if (!elementAcc.getContainer().isEmpty()) {
                ElementUtils.copyElements(parentAcc, elementAcc.getRootElement());
            }
        }
    }




    public static MappedQuery createQuery(UnaryRelation baseConcept, TreeData<FacetPath> treeData, Predicate<FacetPath> isProjected) {

        Generator<Var> varGen = GeneratorFromFunction.createInt().map(i -> Var.alloc("vv" + i));

        Var rootVar = baseConcept.getVar();
        DynamicInjectiveFunction<FacetPath, Var> ifn = DynamicInjectiveFunction.of(varGen);
        ifn.getMap().put(FacetPathOps.get().newRoot(), rootVar);

        FacetPathMapping fpm = ifn::apply;
        // Var rootVar = ifn.apply(PathOpsPPA.get().newRoot());


        ElementGenerator eltGen = new ElementGenerator(baseConcept.getElement(), fpm);

        FacetPath testPath = FacetPathOps.newAbsolutePath(FacetStep.fwd(VOID.classPartition, null), FacetStep.fwd(VOID._class, null));
        Expr testExprPath =  NodeFacetPath.asExpr(testPath);
        Expr testConstraint = new E_Bound(testExprPath);

        eltGen.analysePathModality(testConstraint);

        // Make sure to generate the elements for the mandatory paths

        ElementGroup group = new ElementGroup();
        // baseConcept.getElements().forEach(group::addElement);
        for (FacetPath rootPath : treeData.getRootItems()) {
            eltGen.accumulate(group, rootVar, rootPath, treeData::getChildren);
            // ElementUtils.toElementList(elt).forEach(group::addElement);
            // group.addElement(elt);
        }
        Element elt = group.size() == 1 ? group.get(0) : group;

        List<Var> visibleVars = ifn.getMap().entrySet().stream()
                .filter(e -> isProjected.test(e.getKey()))
                .map(Entry::getValue)
                .collect(Collectors.toList());

        Query query = new Query();
        query.setQuerySelectType();
        query.setQueryPattern(elt);
        query.addProjectVars(visibleVars);

        System.err.println("Generated Query: " + query);
        MappedQuery result = new MappedQuery(treeData, query, ifn.getMap().inverse());

        return result;
    }
}


