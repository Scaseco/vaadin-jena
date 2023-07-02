package org.aksw.facete.v4.impl;

import java.util.HashMap;
import java.util.Map;

import org.aksw.facete.v3.api.TreeDataMap;
import org.aksw.jenax.path.core.FacetPath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;

/** An element with a mapping of FacetPaths to ElementAccs and their variables */
public class MappedElement {
    /** Mapping of element paths (FacetPaths with the component set to the TUPLE constant) */
    // protected Map<FacetPath, ElementAcc> eltPathToAcc = new LinkedHashMap<>();
    // protected ElementAcc elementAcc = new ElementAcc(null, getElement(), null);

    protected Map<FacetPath, Var> pathToVar = new HashMap<>();
    protected TreeDataMap<FacetPath, ElementAcc> eltPathToAcc;

    protected Element element;

    public MappedElement(TreeDataMap<FacetPath, ElementAcc> eltPathToAcc, Map<FacetPath, Var> pathToVar, Element element) {
        super();
        this.eltPathToAcc = eltPathToAcc;
        this.pathToVar = pathToVar;
        this.element = element;
    }

    public TreeDataMap<FacetPath, ElementAcc> getEltPathToAcc() {
        return eltPathToAcc;
    }

    public Element getElement() {
        return element;
    }

    public Var getVar(FacetPath facetPath) {
        return pathToVar.get(facetPath);
    }
}
