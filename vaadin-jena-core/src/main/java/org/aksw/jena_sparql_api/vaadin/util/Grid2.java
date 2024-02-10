package org.aksw.jena_sparql_api.vaadin.util;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridArrayUpdater;
import com.vaadin.flow.component.grid.GridArrayUpdater.UpdateQueueData;
import com.vaadin.flow.function.SerializableBiFunction;

/**
 * A subclass of Grid that also implements GridLike.
 * Intended as a drop-in replacement for Grid such that methods that operate on the
 * GridLike abstraction work out-of-the-box.
 */
public class Grid2<T>
    extends Grid<T>
    implements GridLike<T>
{
    private static final long serialVersionUID = 1L;

    public Grid2() {
        super();
    }

    public Grid2(Class<T> beanType, boolean autoCreateColumns) {
        super(beanType, autoCreateColumns);
    }

    public <U extends GridArrayUpdater, B extends DataCommunicatorBuilder<T, U>> Grid2(Class<T> beanType,
            SerializableBiFunction<UpdateQueueData, Integer, UpdateQueue> updateQueueBuilder,
            B dataCommunicatorBuilder) {
        super(beanType, updateQueueBuilder, dataCommunicatorBuilder);
    }

    public Grid2(Class<T> beanType) {
        super(beanType);
    }

    public <U extends GridArrayUpdater, B extends DataCommunicatorBuilder<T, U>> Grid2(int pageSize,
            SerializableBiFunction<UpdateQueueData, Integer, UpdateQueue> updateQueueBuilder,
            B dataCommunicatorBuilder) {
        super(pageSize, updateQueueBuilder, dataCommunicatorBuilder);
    }

    public Grid2(int pageSize) {
        super(pageSize);
    }
}
