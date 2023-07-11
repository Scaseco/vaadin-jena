package org.aksw.jenax.treequery2.api;

import org.aksw.facete.v3.api.ConstraintFacade;

public interface ConstraintNode<R>
    extends RootedFacetTraversable<R, ConstraintNode<R>>
{
    ConstraintFacade<? extends ConstraintNode<R>> enterConstraints();
}
