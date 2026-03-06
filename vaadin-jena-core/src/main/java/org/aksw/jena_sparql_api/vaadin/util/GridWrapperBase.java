package org.aksw.jena_sparql_api.vaadin.util;

import com.vaadin.flow.component.grid.Grid;

/**
 * Base class for wrapping Vaadin Grids.
 * Can also be used to wrap a {@link Grid} as a {@link GridLike}.
 */
public class GridWrapperBase<T>
    implements GridWrapper<T>
{
    private static final long serialVersionUID = 1L;
    protected Grid<T> grid;

    public GridWrapperBase(Grid<T> grid) {
        super();
        this.grid = grid;
    }

    @Override
    public Grid<T> getDelegate() {
        return grid;
    }

    public static <T> GridWrapper<T> wrap(Grid<T> grid) {
        return new GridWrapperBase<>(grid);
    }
}
