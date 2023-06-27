package org.aksw.facete.v4.impl;

import java.util.ArrayList;
import java.util.List;

import org.aksw.facete.v3.api.FacetPathMapping;
import org.aksw.facete.v3.api.TreeData;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.path.core.FacetPath;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementLateral;


public class ElementGeneratorLateral {
    /**
     * SELECT (?key1 ... ?keyN)    ?s ?p ?o
     *
     * (base relation with ?key1 ... ?keyN and roots ?root1 ... ?rootM) (keys and roots may overlap)
     * LATERAL { # For each unique key combination
     *     { # Union over all roots
     *       BIND("root1" AS ?root)
     *       ?root1 :p1 ?b .
     *       LATERAL {
     *           { BIND(?a AS ?s) BIND(:p1 AS ?p) BIND(?b AS ?o) }
     *         UNION
     *           {
     *             ?b :p2 ?c.
     *             LATERAL {
     *                 { BIND(?b AS ?s) BIND(:p2 AS ?p) BIND(?c AS ?o) }
     *               UNION
     *                 {
     *
     *                 }
     *             }
     *           }
     *         }
     *     }
     *   UNION
     *     {
     *     }
     *  }
     *}
     */
    public static Element createElement(TreeData<FacetPath> tree, String rootVar, FacetPathMapping pathMapping) {
        // createElement(tree, current);
        // pathMapping.allocate(null)

    }

    /**
     * The paths in the tree is what is being projected.
     *
     * @param tree
     * @param current
     * @return
     */
    public static Element createElement(TreeData<FacetPath> tree, FacetPath current) {



        List<FacetPath> children = tree.getChildren(current);
        List<Element> unionMemberElts = new ArrayList<>();

        // Add the element for the current path

        for (FacetPath child : children) {

        }
        Element unionElt = ElementUtils.unionIfNeeded(unionMemberElts);

        Element result = new ElementLateral();
        return result;
    }
}
