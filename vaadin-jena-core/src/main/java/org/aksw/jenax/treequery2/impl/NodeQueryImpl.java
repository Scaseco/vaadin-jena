package org.aksw.jenax.treequery2.impl;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetStep;
import org.aksw.jenax.sparql.relation.api.Relation;
import org.aksw.jenax.treequery2.api.NodeQuery;
import org.aksw.jenax.treequery2.api.QueryContext;
import org.aksw.jenax.treequery2.api.RelationQuery;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.ElementGroup;

/**
 * A root node corresponds to a variable of a graph pattern.
 */
public class NodeQueryImpl
    extends NodeQueryBase
    implements NodeQuery
{
    protected RelationQueryImpl relationQuery;
    protected Var var;
    protected FacetStep reachingStep;

    protected Map<FacetStep, RelationQuery> children = new LinkedHashMap<>();

    protected Map<FacetStep, NodeQuery> subPaths = new LinkedHashMap<>();

    public NodeQueryImpl(RelationQueryImpl relationQuery, Var var, FacetStep reachingStep) {
        super();
        this.relationQuery = relationQuery;
        this.var = var;
        this.reachingStep = reachingStep;
    }

    @Override
    public Map<FacetStep, RelationQuery> children() {
        return children;
    }

    @Override
    public Collection<NodeQuery> getChildren() {
        return subPaths.values();
    }

//    @Override
//    public RootNode getChild(FacetStep step) {
//        return children.get(step);
//    }

    @Override
    public FacetStep reachingStep() {
        return reachingStep;
    }

    /**
     * The empty path resolves to this node
     */
    @Override
    public NodeQuery resolve(FacetPath facetPath) {
        NodeQuery result;
        if (facetPath.isAbsolute()) {
            FacetPath relativePath = FacetPath.newAbsolutePath().relativize(facetPath);

            RelationQuery rootRelation = relationQuery.root();
            result = rootRelation.target().resolve(relativePath);
        } else {
            if (facetPath.getNameCount() == 0) {
                result = this;
            } else {
                FacetPath startPath = facetPath.subpath(0, 1);
                FacetStep step = startPath.toSegment();

                NodeQuery rn = getOrCreateChild(step);

//                // FacetStep step = Iterables.getFirst(facetPath.getSegments(), null);
//                // Set component to tuple
//                FacetStep relationStep = FacetStep.of(step.getNode(), step.getDirection(), step.getAlias(), FacetStep.TUPLE);
//                RelationQueryImpl tmp = (RelationQueryImpl)children.computeIfAbsent(relationStep, fs -> {
//                    Relation relation = relationQuery().getContext().getPropertyResolver().resolve(fs.getNode());
//                    Map<Var, Node> varToComponent = createVarToComponentMap(relation);
//                    return new RelationQueryImpl(this, () -> relation, relationStep, relationQuery.getContext(), varToComponent);
//                });
//                // We need to get the target node
//                Var tgtVar = resolveComponent(step.getTargetComponent(), tmp.getRelation());
//                RootNode rn = new RootNodeImpl(tmp, tgtVar, step);

                FacetPath remainingPath = startPath.relativize(facetPath);
                result = rn.resolve(remainingPath);
            }
        }
        return result;
    }

    @Override
    public NodeQuery getOrCreateChild(FacetStep step) {
        NodeQuery result = subPaths.computeIfAbsent(step, ss -> {

            FacetStep relationStep = FacetStep.of(step.getNode(), step.getDirection(), step.getAlias(), FacetStep.TUPLE);
            RelationQueryImpl tmp = (RelationQueryImpl)children.computeIfAbsent(relationStep, fs -> {
                Relation baseRelation = relationQuery().getContext().getPropertyResolver().resolve(fs.getNode());

                Var sourceVar = FacetRelationUtils.resolveComponent(FacetStep.SOURCE, baseRelation);
                Var targetVar = relationQuery.target().var();

                QueryContext cxt = relationQuery.getContext();
                String scopeName = cxt.getScopeNameGenerator().next();
                Set<Var> usedVars = cxt.getUsedVars();
                Relation relation = FacetRelationUtils.renameVariables(baseRelation, sourceVar, targetVar, scopeName, usedVars);
                usedVars.addAll(relation.getVarsMentioned());

                Map<Var, Node> varToComponent = FacetRelationUtils.createVarToComponentMap(relation);
                return new RelationQueryImpl(this, () -> relation, relationStep, relationQuery.getContext(), varToComponent);
            });

            // tmp.va
            // We need to get the target node
            Var tgtVar = FacetRelationUtils.resolveComponent(step.getTargetComponent(), tmp.getRelation());
            NodeQuery r = tmp.nodeFor(tgtVar);
            return r;
        });

        return result;
    }

    @Override
    public RelationQuery relationQuery() {
        return relationQuery;
    }

    @Override
    public Var var() {
        return var;
    }

    @Override
    public String toString() {
        return "RootNodeImpl [var=" + var + ", relationQuery=" + relationQuery + "]";
    }

    public static NodeQuery newRoot() {
        return new NodeQueryImpl((RelationQueryImpl)RelationQuery.of(new Concept(new ElementGroup(), Vars.s)), Vars.s, null);
    }
}
