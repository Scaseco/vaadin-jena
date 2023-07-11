package org.aksw.jenax.treequery2.impl;

import org.aksw.facete.v3.api.ConstraintFacade;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetStep;
import org.aksw.jenax.treequery2.api.ConstraintNode;
import org.aksw.jenax.treequery2.api.NodeQuery;

/**
 * This implementation features support for traversals along facet paths
 * as well as setting constraints on the paths.
 */
public class ConstraintNodeImpl
    extends RootedFacetTraversableBase<NodeQuery, ConstraintNode<NodeQuery>>
    implements ConstraintNode<NodeQuery>
{
    public ConstraintNodeImpl(NodeQuery root, FacetPath path) {
        super(root, path);
    }

    @Override
    public ConstraintNodeImpl getOrCreateChild(FacetStep step) {
        FacetPath newPath = path.resolve(step);
        return new ConstraintNodeImpl(root, newPath);
    }

    @Override
    public ConstraintFacade<ConstraintNode<NodeQuery>> enterConstraints() {
        RelationQueryImpl rq = (RelationQueryImpl)root.relationQuery();
        ConstraintApi2Impl<ConstraintNode<NodeQuery>> constraintView = new ConstraintApi2Impl<>(rq.facetConstraints, this);
        return new ConstraintFacade2Impl<ConstraintNode<NodeQuery>>(this, constraintView);
    }
}
