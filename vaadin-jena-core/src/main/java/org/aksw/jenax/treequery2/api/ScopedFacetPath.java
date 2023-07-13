package org.aksw.jenax.treequery2.api;

import java.util.Objects;
import java.util.function.Function;

import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.treequery2.impl.FacetPathMappingImpl;
import org.apache.jena.sparql.core.Var;

public class ScopedFacetPath {
    protected VarScope scope;
    protected FacetPath facetPath;

    public ScopedFacetPath(VarScope scope, FacetPath facetPath) {
        super();
        this.scope = scope;
        this.facetPath = facetPath;
    }

    public ScopedFacetPath getParent() {
        return transformPath(FacetPath::getParent);
    }

    public VarScope getScope() {
        return scope;
    }

//    public String getScopeName() {
//        return scopeName;
//    }
//
//    public Var getStartVar() {
//        return startVar;
//    }

    public FacetPath getFacetPath() {
        return facetPath;
    }

    /**
     * Return a new ScopedFacetPath with the path transformed by the given function.
     * If the path is null then this function returns null.
     */
    public ScopedFacetPath transformPath(Function<? super FacetPath, ? extends FacetPath> facetPathFn) {
        FacetPath newPath = facetPathFn.apply(facetPath);
        return newPath == null ? null : new ScopedFacetPath(scope, newPath);
    }

    public ScopedVar toScopedVar(FacetPathMapping facetPathMapping) {
        return toScopedVar(this, facetPathMapping);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scope, facetPath);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ScopedFacetPath other = (ScopedFacetPath) obj;
        if (facetPath == null) {
            if (other.facetPath != null)
                return false;
        } else if (!facetPath.equals(other.facetPath))
            return false;
        if (scope == null) {
            if (other.scope != null)
                return false;
        } else if (!scope.equals(other.scope))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ScopedFacetPath [scope=" + scope + ", facetPath=" + facetPath + "]";
    }

    public static ScopedVar toScopedVar(ScopedFacetPath scopedFacetPath, FacetPathMapping facetPathMapping) {
        String scopeName = scopedFacetPath.getScope().getScopeName();
        Var startVar = scopedFacetPath.getScope().getStartVar();
        FacetPath facetPath = scopedFacetPath.getFacetPath();
        return FacetPathMappingImpl.resolveVar(facetPathMapping, scopeName, startVar, facetPath);
    }

    public static ScopedFacetPath of(VarScope scope, FacetPath facetPath) {
    	return new ScopedFacetPath(scope, facetPath);
    }

    public static ScopedFacetPath of(Var startVar, FacetPath facetPath) {
    	return of(VarScope.of(startVar), facetPath);
    }
}
