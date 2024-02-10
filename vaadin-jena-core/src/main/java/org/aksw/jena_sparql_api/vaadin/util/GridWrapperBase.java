package org.aksw.jena_sparql_api.vaadin.util;

import com.vaadin.flow.component.grid.Grid;

/**
 * Base class for wrapping Vaadin Grids.
 * Can also be used to wrap a {@link Grid} as a {@link GridLike}.
 */
public class GridWrapperBase<T>
    implements GridWrapper<T>
{
    protected Grid<T> grid;

    public GridWrapperBase(Grid<T> grid) {
        super();
        this.grid = grid;
    }

    @Override
    public Grid<T> getDelegate() {
        return grid;
    }
}
