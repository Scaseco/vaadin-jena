package org.aksw.jena_sparql_api.vaadin.util;

import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;

public class TreeGridEx<T>
    extends GridEx<T>
{
    private static final long serialVersionUID = 1L;

    public TreeGridEx() {
        super(new TreeGrid<>());
    }

    public TreeGridEx(Class<T> beanType) {
        super(new TreeGrid<>(beanType));
    }

    public TreeGridEx(HierarchicalDataProvider<T, ?> dataProvider) {
        super(new TreeGrid<>(dataProvider));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected Query<T, ?> newQuery() {
        return new HierarchicalQuery(null, null);
    }
}
