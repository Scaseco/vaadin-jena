package org.aksw.jenax.treequery2.api;

import org.aksw.facete.v3.api.ConstraintFacade;
import org.aksw.jenax.path.core.FacetPath;

public interface ConstraintNode<R>
    extends RootedFacetTraversable<R, ConstraintNode<R>>
{
    ConstraintFacade<? extends ConstraintNode<R>> enterConstraints();
    
    public static ScopedFacetPath toScopedFacetPath(ConstraintNode<NodeQuery> constraintNode) {
    	NodeQuery nodeQuery = constraintNode.getRoot();
    	FacetPath facetPath = nodeQuery.getFacetPath();
    	RelationQuery relationQuery = nodeQuery.relationQuery();
    	VarScope varScope = VarScope.of(relationQuery.getScopeBaseName(), nodeQuery.var());
    	ScopedFacetPath scopedFacetPath = ScopedFacetPath.of(varScope, facetPath);
    	return scopedFacetPath;
    }
}
