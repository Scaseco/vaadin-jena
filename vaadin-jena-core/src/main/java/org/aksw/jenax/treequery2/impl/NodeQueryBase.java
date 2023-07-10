package org.aksw.jenax.treequery2.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.aksw.jenax.path.core.FacetStep;
import org.aksw.jenax.treequery2.old.NodeQueryOld;
import org.apache.jena.query.SortCondition;

public class NodeQueryBase {
    protected Map<FacetStep, NodeQueryOld> children = new LinkedHashMap<>();
    protected List<SortCondition> sortConditions = new ArrayList<>();

    public NodeQueryBase() {
        super();
    }
}
