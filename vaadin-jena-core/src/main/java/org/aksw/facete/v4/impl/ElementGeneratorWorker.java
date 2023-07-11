package org.aksw.facete.v4.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.facete.v3.api.FacetPathMapping;
import org.aksw.facete.v3.api.NodeFacetPath;
import org.aksw.facete.v3.api.TreeData;
import org.aksw.facete.v3.api.TreeDataMap;
import org.aksw.jena_sparql_api.data_query.impl.FacetedQueryGenerator;
import org.aksw.jenax.arq.util.expr.ExprUtils;
import org.aksw.jenax.arq.util.node.NodeUtils;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetStep;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_LogicalNot;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprTransformer;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformExpr;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;

import com.google.common.collect.SetMultimap;

/**
 * This class should supersede {@link FacetedQueryGenerator}
 */
public class ElementGeneratorWorker {
    protected SetMultimap<FacetPath, Expr> localConstraintIndex;
    protected FacetPathMapping pathMapping;
    protected PropertyResolverImpl propertyResolver;

    /** Mapping of element paths (FacetPaths with the component set to the TUPLE constant) */
    // protected Map<FacetPath, ElementAcc> eltPathToAcc = new LinkedHashMap<>();
    // ElementAcc rootEltAcc = ElementAcc.newRoot(); // null; //new ElementAcc();
    protected TreeData<FacetPath> facetTree;

    /** The FacetPaths on this tree are purely element ids (they reference relations rather than components) */
    protected Set<FacetPath> mandatoryElementIds = new HashSet<>();
    protected TreeDataMap<FacetPath, ElementAcc> facetPathToAcc = new TreeDataMap<>();
    protected Map<FacetPath, Var> pathToVar = new HashMap<>();

    public ElementGeneratorWorker(TreeData<FacetPath> facetTree, SetMultimap<FacetPath, Expr> localConstraintIndex, FacetPathMapping pathMapping, PropertyResolverImpl propertyResolver) {
        this.facetTree = facetTree;
        this.localConstraintIndex = localConstraintIndex;
        this.mandatoryElementIds.add(FacetPath.newAbsolutePath());
        this.pathMapping = pathMapping;
        this.propertyResolver = propertyResolver;

        for (Expr expr : new ArrayList<>(localConstraintIndex.values())) {
            analysePathModality(expr);
        }
    }

    public FacetPathMapping getPathMapping() {
        return pathMapping;
    }

    public PropertyResolverImpl getPropertyResolver() {
        return propertyResolver;
    }

    /**
     * Checks for whether this expression references any paths that need to be mandatory.
     * Elements created for the segments along such a path will not be wrapped in OPTIONAL blocks.
     */
    public void analysePathModality(Expr expr) {
        //public void addExpr(Expr expr) {
        Set<FacetPath> paths = NodeFacetPath.mentionedPaths(expr);

        boolean isMandatory = !FacetedQueryGenerator.isAbsent(expr);

        for (FacetPath path : paths) {
            // addPath(path);
            // constraintIndex.put(path, expr);

            if (isMandatory) {
                declareMandatoryPath(path);
            }
        }
    }

    /** Mark a path as mandatory. This makes all parents also mandatory. */
    public void declareMandatoryPath(FacetPath path) {
        FacetPath current = path;
        while (current != null) {
            FacetPath eltId = FacetPathUtils.toElementId(current);
            if (!mandatoryElementIds.contains(eltId)) {
                mandatoryElementIds.add(eltId);

                current = current.getParent();
            } else {
                break;
            }
        }
    }

    /**
     * Create the element for the last facet step of a facet path (without recursion)
     */
    public ElementAcc allocateEltAcc(Var parentVar, Var targetVar, FacetPath path) {

        // FIXME Naively adding optionl elements does not work when facet paths are mapped to BIND elements
        // In the example below, ?bar will be unbound:
        // BIND("foo" AS ?foo) OPTIONAL { BIND(?foo AS ?bar) }

        FacetPath eid = FacetPathUtils.toElementId(path);
        // ElementGroup container = new ElementGroup();
        boolean isMandatory = mandatoryElementIds.contains(eid);

        Element coreElt = null;
        Node secondaryNode;
        if (!path.getSegments().isEmpty()) {
            coreElt = createElementForLastStep(parentVar, targetVar, path);

            //container.addElement(coreElt);
        } else {
            coreElt = new ElementGroup();
            // Add the base element if this is the root path
            // container.addElement(baseElement);
        }

        // Element root = isMandatory || coreElt instanceof ElementBind ? container : new ElementOptional(container);
        BiFunction<Element, List<Element>, Element> combiner =  isMandatory || coreElt instanceof ElementBind
                ? ElementAcc::collectIntoGroup
                : ElementAcc::collectIntoOptionalGroup;


        return new ElementAcc(coreElt, combiner);
    }

    public Element createElementForLastStep(Var parentVar, Var targetVar, FacetPath path) {
        Element coreElt;
        FacetStep step = path.getFileName().toSegment();

        Node secondaryNode;
        Node predicateNode = step.getNode();
        boolean isFwd = step.isForward();

        Node c = step.getTargetComponent();

        if (NodeUtils.ANY_IRI.equals(predicateNode)) {
            Node toggledComponent = FacetStep.isTarget(c) ? FacetStep.PREDICATE : FacetStep.TARGET;
            FacetStep s = step.copyStep(toggledComponent);
        // FacetStep toggledTarget = step.toggleTarget();
            secondaryNode = pathMapping.allocate(path.resolveSibling(s));
        } else {
            secondaryNode = predicateNode;
        }

        if (FacetStep.isTarget(c)) {
            coreElt = propertyResolver.resolve(parentVar, secondaryNode, targetVar, isFwd);
            // coreElt = ElementUtils.createElementTriple(parentVar, secondaryNode, targetVar, isFwd);
        } else {
            coreElt = ElementUtils.createElementTriple(parentVar, targetVar, secondaryNode, isFwd);
        }
        return coreElt;
    }

    public void allocateElements(Expr expr) {
        Collection<FacetPath> paths = NodeFacetPath.mentionedPaths(expr);
        for(FacetPath path : paths) {
            allocateElement(path);
        }
    }

    public Var allocateElement(FacetPath path) {
        FacetPath parentPath = path.getParent();
        FacetPath eltId = FacetPathUtils.toElementId(path);

        ElementAcc elementAcc = facetPathToAcc.get(eltId);
        Var targetVar;
        if (elementAcc == null) {
            Var parentVar;
            targetVar = pathToVar.computeIfAbsent(path, pathMapping::allocate);

            if (parentPath != null) {
                parentVar = allocateElement(parentPath);
            } else {
                parentVar = targetVar;
            }

            elementAcc = allocateEltAcc(parentVar, targetVar, path);
            facetPathToAcc.addItem(eltId.getParent(), eltId);
            facetPathToAcc.put(eltId, elementAcc);

            // Create the ElementAcc for the path if it hasn't happened yet
//                Iterable<FacetPath> children = getChildren.apply(path);
//                if (children != null && children.iterator().hasNext()) {
//                    for (FacetPath subPath : children) {
//                        // If there is no accumulator for the child then visit it
//                        accumulate(elementAcc.getContainer(), targetVar, subPath, getChildren);
//                    }
//                }
//
//                if (!elementAcc.getContainer().isEmpty()) {
//                    ElementUtils.copyElements(parentAcc, elementAcc.getResultElement());
//                }
        } else {
            targetVar = pathToVar.computeIfAbsent(path, pathMapping::allocate);
        }

        return targetVar;
    }

    /**
     * TODO 'global' here means global to the current the sub-tree
     *
     * @param parentAcc Container to add elements to the parent
     * @param globalAcc Container to add 'global' elements, such as filter expressions
     * @param parentVar
     * @param path
     * @param getChildren
     */
    public void accumulate(
            TreeDataMap<FacetPath, ElementAcc> facetPathToAcc,
            ElementGroup globalAcc,
            Var parentVar,
            FacetPath path, // TODO This should be the FacetNode and there might be subqueries
            Function<FacetPath, ? extends Iterable<FacetPath>> getChildren) {

//            FacetPath path = ElementGeneratorUtils.cleanPath(rawPath);
//            if (path != rawPath) {
//                accumulate(parentAcc, globalAcc, parentVar, path, getChildren);
//            }
        FacetPath parentPath = path.getParent();
        FacetPath parentEltId = parentPath == null ? null : FacetPathUtils.toElementId(parentPath);
        FacetPath eltId = FacetPathUtils.toElementId(path);

        Var targetVar = pathMapping.allocate(path);

        pathToVar.put(path, targetVar);

        ElementAcc elementAcc = facetPathToAcc.get(eltId);
        if (elementAcc == null) {
            elementAcc = allocateEltAcc(parentVar, targetVar, path);
            // The element may exist if eltId is the empty path
            if (!facetPathToAcc.contains(eltId)) {
                facetPathToAcc.addItem(parentEltId, eltId);
            }
            facetPathToAcc.put(eltId, elementAcc);
        }

        // Create the ElementAcc for the path if it hasn't happened yet
        Iterable<FacetPath> children = getChildren.apply(path);
        if (children != null && children.iterator().hasNext()) {
            for (FacetPath subPath : children) {
                // If there is no accumulator for the child then visit it
                accumulate(facetPathToAcc, globalAcc, targetVar, subPath, getChildren);
            }
        }

//            if (!elementAcc.getContainer().isEmpty()) {
//                ElementUtils.copyElements(parentAcc, elementAcc.getResultElement());
//            }

        // Create FILTER elements
        Set<Expr> exprs = localConstraintIndex.get(path);
        createElementsForExprs2(globalAcc, exprs, false);
    }


    public MappedElement createElement() {
        FacetPath rootPath = FacetPath.newAbsolutePath();
        Var rootVar = pathMapping.allocate(rootPath);
        // ElementGroup group = new ElementGroup();


        // TreeDataMap<FacetPath, ElementAcc> tree;
        ElementGroup filterGroup = new ElementGroup();

        // baseConcept.getElements().forEach(group::addElement);
        for (FacetPath path : facetTree.getRootItems()) {
            accumulate(facetPathToAcc, filterGroup, rootVar, path, facetTree::getChildren);
//                accumulate(facetPathToAcc, filterGroup, null, null, facetTree::getChildren);
            // ElementUtils.toElementList(elt).forEach(group::addElement);
            // group.addElement(elt);
        }

        Element elt = collect(facetPathToAcc, rootPath);
        elt = ElementUtils.flatten(elt);

        //ElementUtils.copyElements(group, filterGroup);

        // Add filters for the constraints
        // Element elt = group.size() == 1 ? group.get(0) : group;

        return new MappedElement(facetPathToAcc, pathToVar, elt);
    }


    public void createElementsForExprs2(ElementGroup globalAcc, Collection<Expr> baseExprs, boolean negate) {

        NodeTransform resolveFacetPaths = NodeFacetPath.createNodeTransform(pathMapping);
//          //NodeTransform xform = NodeTransformLib2.wrapWithNullAsIdentity();
  //
  //
//              Expr finalExpr = NodeTransformLib.transform(subst, expr);
//              ElementFilter eltFilter = new ElementFilter(finalExpr);
//              group.addElement(eltFilter);
//          }

      Set<Element> result = new LinkedHashSet<>();
      Set<Expr> resolvedExprs = new LinkedHashSet<>();

      // Sort base exprs - absent ones last
      List<Expr> tmp = baseExprs.stream()
              .map(e -> FacetedQueryGenerator.isAbsent(e) ? FacetedQueryGenerator.internalRewriteAbsent(e) : e)
              .collect(Collectors.toList());

      List<Expr> exprs = new ArrayList<>(tmp);
      Collections.sort(exprs, FacetedQueryGenerator::compareAbsent);
        // Resolve the expression
        for(Expr expr : exprs) {

            // TODO We need to add the elements of the paths
            //ExprTransformer.transform(new ExprTransform, expr)
            //Expr resolved = expr.applyNodeTransform(nodeTransform); //ExprTransformer.transform(exprTransform, expr);
            Expr resolved = ExprTransformer.transform(new NodeTransformExpr(resolveFacetPaths), expr);

            resolvedExprs.add(resolved);
        }


        Expr resolvedPathExpr = ExprUtils.orifyBalanced(resolvedExprs);

        if(resolvedPathExpr != null) {
            if(negate) {
                resolvedPathExpr = new E_LogicalNot(resolvedPathExpr);
            }

            // Skip adding constraints that equal TRUE
            if(!NodeValue.TRUE.equals(resolvedPathExpr)) {
                result.add(new ElementFilter(resolvedPathExpr));
            }
        }

        result.forEach(globalAcc::addElement);
    }


    // Does not seem to be used (yet) - Process all paths referenced by the given expressions
    public void createElementsForExprs(Collection<Expr> baseExprs, boolean negate) {

        Set<Expr> seenExprs = new LinkedHashSet<>();
        NodeTransform resolveFacetPaths = NodeFacetPath.createNodeTransform(pathMapping);
    //        //NodeTransform xform = NodeTransformLib2.wrapWithNullAsIdentity();
    //
    //
    //            Expr finalExpr = NodeTransformLib.transform(subst, expr);
    //            ElementFilter eltFilter = new ElementFilter(finalExpr);
    //            group.addElement(eltFilter);
    //        }

        Set<Element> result = new LinkedHashSet<>();
        Set<Expr> resolvedExprs = new LinkedHashSet<>();

        // Sort base exprs - absent ones last
        List<Expr> tmp = baseExprs.stream()
                .map(e -> FacetedQueryGenerator.isAbsent(e) ? FacetedQueryGenerator.internalRewriteAbsent(e) : e)
                .collect(Collectors.toList());

        List<Expr> exprs = new ArrayList<>(tmp);
        Collections.sort(exprs, FacetedQueryGenerator::compareAbsent);

        // Collect all mentioned paths so we can getOrCreate their elements


        for(Expr expr : exprs) {
            // Ensure the elements for the paths are created
            allocateElements(expr);
        }

        // Resolve the expression
        for(Expr expr : exprs) {

            // TODO We need to add the elements of the paths
            //ExprTransformer.transform(new ExprTransform, expr)
            //Expr resolved = expr.applyNodeTransform(nodeTransform); //ExprTransformer.transform(exprTransform, expr);
            Expr resolved = ExprTransformer.transform(new NodeTransformExpr(resolveFacetPaths), expr);

            resolvedExprs.add(resolved);
        }


        Expr resolvedPathExpr = ExprUtils.orifyBalanced(resolvedExprs);

        if(resolvedPathExpr != null) {
            if(negate) {
                resolvedPathExpr = new E_LogicalNot(resolvedPathExpr);
            }

            // Skip adding constraints that equal TRUE
            if(!NodeValue.TRUE.equals(resolvedPathExpr)) {
                result.add(new ElementFilter(resolvedPathExpr));
            }
        }

        //BinaryRelation result = new BinaryRelationImpl(ElementUtils.groupIfNeeded(elts), br.getSourceVar(), br.getTargetVar());
        // return result;
    }

    /**
     * Create an Element for the sub-tree of that starts at a given path at the given tree.
     */
    public static Element collect(TreeDataMap<FacetPath, ElementAcc> tree, FacetPath currentPath) {
        ElementAcc eltAcc = tree.get(currentPath);
        Element elt = eltAcc.getElement();

        // Create the ElementAcc for the path if it hasn't happened yet
        Iterable<FacetPath> children = tree.getChildren(currentPath);
        List<Element> childElts = new ArrayList<>();
        if (children != null && children.iterator().hasNext()) {
            for (FacetPath childPath : children) {
                Element childElt = collect(tree, childPath);
                // If there is no accumulator for the child then visit it
                childElts.add(childElt);
            }
        }

        Element result = eltAcc.getFactory().apply(elt, childElts);
        return result;
    }

}