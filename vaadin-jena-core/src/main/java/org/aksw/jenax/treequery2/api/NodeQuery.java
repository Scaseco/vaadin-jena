package org.aksw.jenax.treequery2.api;

import java.util.Collection;
import java.util.Map;

import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetStep;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

public interface NodeQuery
    extends HasSlice
    // extends NodeQuery
{
    default FacetPath getPath() {
        RelationQuery relationQuery = relationQuery();
        NodeQuery parentNode = relationQuery.getParentNode();

        FacetPath result;
        if (parentNode == null) {
            result = FacetPath.newAbsolutePath();
        } else {
            FacetPath base = parentNode.getPath();
            FacetStep step = reachingStep();
            if (step == null) {
                throw new NullPointerException();
            }
            // Objects.requireNonNull(step); // Sanity check - only root nodes may return null for steps
            result = base.resolve(step);
        }
        return result;
    }

    default NodeQuery getRoot() {
        RelationQuery relationQuery = relationQuery();
        NodeQuery parentNode = relationQuery.getParentNode();

        NodeQuery result;
        if (parentNode == null) {
            result = this;
        } else {
            result = parentNode.getRoot();
        }
        return result;
    }

    /**
     * A collection of sub-paths of this node
     */
    Collection<NodeQuery> getChildren();

    default NodeQuery fwd(String property) {
        return getOrCreateChild(FacetStep.fwd(property));
    }

    default NodeQuery fwd(Node property) {
        return getOrCreateChild(FacetStep.fwd(property));
    }

    default NodeQuery bwd(String property) {
        return getOrCreateChild(FacetStep.bwd(property));
    }

    default NodeQuery bwd(Node property) {
        return getOrCreateChild(FacetStep.bwd(property));
    }

    /** Returns null if there is no child reachable with the given step. */
    // RootNode getChild(FacetStep step);
    NodeQuery getOrCreateChild(FacetStep step);


//	default boolean isChildOf(RelationNode relationNode) {
//
//	}

    RelationQuery relationQuery();
    Var var();
    FacetStep reachingStep();

    NodeQuery resolve(FacetPath facetPath);

    Map<FacetStep, RelationQuery> children();

    /** Convenience delegates which set limit/offset on the underlying relation */

    @Override
    default Long offset() {
        RelationQuery relationQuery = relationQuery();
        return relationQuery.offset();
    }

    @Override
    default NodeQuery offset(Long offset) {
        RelationQuery relationQuery = relationQuery();
        relationQuery.offset(offset);
        return this;
    }

    @Override
    default Long limit() {
        RelationQuery relationQuery = relationQuery();
        return relationQuery.limit();
    }

    @Override
    default NodeQuery limit(Long limit) {
        RelationQuery relationQuery = relationQuery();
        relationQuery.limit(limit);
        return this;
    }
}
