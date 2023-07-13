package org.aksw.facete.v4.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.aksw.facete.v3.api.TreeData;
import org.aksw.facete.v3.api.TreeDataMap;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.treequery2.api.VarScope;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

public class ElementGeneratorContext {
    protected VarScope scope;
    protected TreeData<FacetPath> facetTree;

    protected SetMultimap<FacetPath, Expr> localConstraintIndex;

    /** The FacetPaths on this tree are purely element ids (they reference relations rather than components) */
    protected Set<FacetPath> mandatoryElementIds = new HashSet<>();
    protected TreeDataMap<FacetPath, ElementAcc> facetPathToAcc = new TreeDataMap<>();
    protected Map<FacetPath, Var> pathToVar = new HashMap<>();

    public ElementGeneratorContext(Var rootVar, TreeData<FacetPath> facetTree, SetMultimap<FacetPath, Expr> localConstraintIndex) {
        this(new VarScope("", rootVar), facetTree, localConstraintIndex);
    }

    public ElementGeneratorContext(VarScope scope) {
        this(scope, new TreeData<>(), HashMultimap.create());
    }

    public ElementGeneratorContext(VarScope scope, TreeData<FacetPath> facetTree, SetMultimap<FacetPath, Expr> localConstraintIndex) {
        super();
        this.scope = scope;
        this.facetTree = facetTree;
        this.localConstraintIndex = localConstraintIndex;
    }

    public ElementGeneratorContext setFacetTree(TreeData<FacetPath> facetTree) {
		this.facetTree = facetTree;
		return this;
	}
    
    public ElementGeneratorContext setConstraintIndex(SetMultimap<FacetPath, Expr> localConstraintIndex) {
    	this.localConstraintIndex = localConstraintIndex;
    	return this;
    }
    
    
    public void addPath(FacetPath facetPath) {
        facetTree.putItem(facetPath, FacetPath::getParent);
    }
    
    public Map<FacetPath, Var> getPathToVar() {
		return pathToVar;
	}
}