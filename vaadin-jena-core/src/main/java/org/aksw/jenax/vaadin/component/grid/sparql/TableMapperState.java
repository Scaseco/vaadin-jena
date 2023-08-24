package org.aksw.jenax.vaadin.component.grid.sparql;

import java.util.HashMap;
import java.util.Map;

import org.aksw.facete.v3.api.TreeData;
import org.aksw.jenax.path.core.FacetPath;

public class TableMapperState {
    protected TreeData<FacetPath> facetTree;
    protected Map<FacetPath, Boolean> pathToVisibility;

    public TableMapperState(TreeData<FacetPath> facetTree, Map<FacetPath, Boolean> pathToVisibility) {
        super();
        this.facetTree = facetTree;
        this.pathToVisibility = pathToVisibility;
    }

    public TreeData<FacetPath> getFacetTree() {
        return facetTree;
    }

    public Map<FacetPath, Boolean> getPathToVisibility() {
        return pathToVisibility;
    }

    public static TableMapperState ofRoot() {
        TreeData<FacetPath> tree = new TreeData<>();
        tree.addRootItems(FacetPath.newAbsolutePath());
        return new TableMapperState(tree, new HashMap<>());
    }
}
