package org.aksw.jenax.treequery2.api;

import org.aksw.jenax.path.core.FacetPath;

/** Maps facet paths to scope names */
public interface FacetPathMapping {
    String allocate(FacetPath facetPath);
}
