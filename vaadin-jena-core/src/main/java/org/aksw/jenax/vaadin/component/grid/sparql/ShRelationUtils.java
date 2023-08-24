package org.aksw.jenax.vaadin.component.grid.sparql;

import org.aksw.facete.v3.api.TreeData;
import org.aksw.jenax.model.shacl.domain.ShNodeShape;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.sparql.relation.api.UnaryRelation;
import org.apache.jena.rdf.model.Resource;

/** FIXME Add to shacl, relations or a new module (shacl + relations) */
public class ShRelationUtils {

    /**
     * Creates a shacl target specification from the given Element.
     * Tries to detect:
     * <ul>
     *   <li>sh:targetClass</li>
     *   <li>sh:targetSubjectsOf</li>
     *   <li>sh:targetObjectsOf</li>
     *   <li>sh:targetNode</li>
     * </ul>
     * If none of the description match, then this method falls back to a SPARQL target of the form:
     * <pre>
     * [] sh:target [
     *   a sh:SPARQLTarget ;
     *   sh:select "SELECT ?this { ...}"
     * ]
     * </pre>
     *
     * @param relation
     * @return
     */
    public static Resource toShTarget(Resource shape, UnaryRelation relation) {
        return null;
    }

    public static ShNodeShape toShacl(UnaryRelation relation, TreeData<FacetPath> facetTree) {
        return null;
    }
}
