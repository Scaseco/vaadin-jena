package org.aksw.jenax.treequery2.api;

import org.aksw.jenax.path.core.FacetPath;

public interface FacetPathResolver<T> {
    void resolve(T root, FacetPath path);
}
