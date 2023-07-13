package org.aksw.facete.v4.impl;

import java.util.HashMap;
import java.util.Map;

import org.aksw.facete.v3.api.TreeDataMap;
import org.aksw.jenax.treequery2.api.ScopedFacetPath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;

/** An element with a mapping of FacetPaths to ElementAccs and their variables */
public class MappedElement {
    /** Mapping of element paths (FacetPaths with the component set to the TUPLE constant) */
    // protected Map<FacetPath, ElementAcc> eltPathToAcc = new LinkedHashMap<>();
    // protected ElementAcc elementAcc = new ElementAcc(null, getElement(), null);

    protected Map<ScopedFacetPath, Var> pathToVar = new HashMap<>();
    protected TreeDataMap<ScopedFacetPath, ElementAcc> eltPathToAcc;

    protected Element element;

    public MappedElement(TreeDataMap<ScopedFacetPath, ElementAcc> eltPathToAcc, Map<ScopedFacetPath, Var> pathToVar, Element element) {
        super();
        this.eltPathToAcc = eltPathToAcc;
        this.pathToVar = pathToVar;
        this.element = element;
    }

    public TreeDataMap<ScopedFacetPath, ElementAcc> getEltPathToAcc() {
        return eltPathToAcc;
    }

    public Element getElement() {
        return element;
    }

    public Var getVar(ScopedFacetPath facetPath) {
        return pathToVar.get(facetPath);
    }
}
