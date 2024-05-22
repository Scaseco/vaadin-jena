package org.aksw.jena_sparql_api.vaadin.data.provider;

import org.aksw.jena_sparql_api.vaadin.util.GridLike;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.DataProvider;

/**
 * Interface for configuring a component with a data provider.
 * In contrast to calling e.g. gridComponent.setDataProvider(dataProvider) directly,
 * using the connector allows "intercepting" setting of a data provider such as to
 * show a list of running data provider requests in a UI.
 */
public interface DataProviderConnector {
    void connectRaw(Object component, DataProvider<?, ?> dataProvider, String taskName);
    <T> void connectGrid(Grid<T> grid, DataProvider<T, ?> dataProvider, String taskName);
    <T> void connect(GridLike<T> grid, DataProvider<T, ?> dataProvider, String taskName);

    // DataProvider<?, ?> extractRaw(Object component);
    // <T> DataProvider<T, ?> extract(Grid<T> grid);
    // <T> DataProvider<T, ?> extract(GridLike<T> grid);
}
