package org.aksw.jena_sparql_api.vaadin.util;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.GridSelectionModel;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.grid.ItemDoubleClickEvent;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.selection.SingleSelect;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.shared.Registration;

/** Interface to wrap a Grid */
public interface GridWrapper<T>
    extends GridLike<T>
{
    Grid<T> getDelegate();

    /** {@inheritDoc} */
    @Override
    default DataCommunicator<T> getDataCommunicator() {
        return getDelegate().getDataCommunicator();
    }

    /** {@inheritDoc} */
    @Override
    default DataProvider<T, ?> getDataProvider() {
        return getDelegate().getDataProvider();
    }

    /** {@inheritDoc} */
    @Override
    default void setDataProvider(DataProvider<T, ?> dataProvider) {
        getDelegate().setDataProvider(dataProvider);
    }

    /** {@inheritDoc} */
    @Override
    default HeaderRow prependHeaderRow() {
        return getDelegate().prependHeaderRow();
    }

    /** {@inheritDoc} */
    @Override
    default HeaderRow appendHeaderRow() {
        return getDelegate().appendHeaderRow();
    }

    /** {@inheritDoc} */
    @Override
    default void setPageSize(int pageSize) {
        getDelegate().setPageSize(pageSize);
    }

    /** {@inheritDoc} */
    @Override
    default  GridSelectionModel<T> setSelectionMode(SelectionMode selectionMode) {
        return getDelegate().setSelectionMode(selectionMode);
    }

    /** {@inheritDoc} */
    @Override
    default void setMultiSort(boolean multiSort) {
        getDelegate().setMultiSort(multiSort);
    }

    /** {@inheritDoc} */
    @Override
    default GridSelectionModel<T> getSelectionModel() {
        return getDelegate().getSelectionModel();
    }

    /** {@inheritDoc} */
    @Override
    default void removeAllColumns() {
        getDelegate().removeAllColumns();
    }

    /** {@inheritDoc} */
    @Override
    default Column<T> addColumn(ValueProvider<T, ?> valueProvider) {
        return getDelegate().addColumn(valueProvider);
    }

    /** {@inheritDoc} */
    @Override
    default Column<T> addColumn(Renderer<T> renderer) {
        return getDelegate().addColumn(renderer);
    }

    /** {@inheritDoc} */
    @Override
    default Column<T> addColumn(String propertyName) {
        return getDelegate().addColumn(propertyName);
    }

    /** {@inheritDoc} */
    @Override
    default Column<T> getColumnByKey(String columnKey) {
        return getDelegate().getColumnByKey(columnKey);
    }

    /** {@inheritDoc} */
    @Override
    default <V extends Component> Column<T> addComponentColumn(ValueProvider<T, V> componentProvider) {
        return getDelegate().addComponentColumn(componentProvider);
    }

    /** {@inheritDoc} */
    @Override
    default Element getElement() {
        return getDelegate().getElement();
    }

    /** {@inheritDoc} */
    @Override
    default GridContextMenu<T> addContextMenu() {
        return getDelegate().addContextMenu();
    }

    /** {@inheritDoc} */
    @Override
    default SingleSelect<Grid<T>, T> asSingleSelect() {
        return getDelegate().asSingleSelect();
    }

    /** {@inheritDoc} */
    @Override
    default Registration addItemClickListener(ComponentEventListener<ItemClickEvent<T>> listener) {
        return getDelegate().addItemClickListener(listener);
    }

    /** {@inheritDoc} */
    @Override
    default Registration addItemDoubleClickListener(ComponentEventListener<ItemDoubleClickEvent<T>> listener) {
        return getDelegate().addItemDoubleClickListener(listener);
    }
}
