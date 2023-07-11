package org.aksw.jenax.treequery2.api;

import org.aksw.jenax.path.core.FacetStep;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;

/**
 * Base interface for traversals along FacetSteps.
 *
 * @param <T>
 */
public interface FacetTraversable<T extends FacetTraversable<T>> {
    default T fwd(String property) {
        return getOrCreateChild(FacetStep.fwd(property));
    }

    default T fwd(Node property) {
        return getOrCreateChild(FacetStep.fwd(property));
    }

    default T fwd(Resource property) {
        return getOrCreateChild(FacetStep.fwd(property));
    }

    default T bwd(String property) {
        return getOrCreateChild(FacetStep.bwd(property));
    }

    default T bwd(Node property) {
        return getOrCreateChild(FacetStep.bwd(property));
    }

    default T bwd(Resource property) {
        return getOrCreateChild(FacetStep.bwd(property));
    }

    /** Returns null if there is no child reachable with the given step. */
    // RootNode getChild(FacetStep step);
    T getOrCreateChild(FacetStep step);
}
