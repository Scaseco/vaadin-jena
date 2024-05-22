package org.aksw.jena_sparql_api.vaadin.util;

import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;

/**
 * A subclass of TreeGrid that also implements GridLike.
 * Intended as a drop-in replacement for Grid such that methods that operate on the
 * GridLike abstraction work out-of-the-box.
 */
public class TreeGrid2<T>
    extends TreeGrid<T>
    implements GridLike<T>
{
    private static final long serialVersionUID = 1L;

    public TreeGrid2() {
        super();
    }

    public TreeGrid2(Class<T> beanType) {
        super(beanType);
    }

    public TreeGrid2(HierarchicalDataProvider<T, ?> dataProvider) {
        super(dataProvider);
    }
}
