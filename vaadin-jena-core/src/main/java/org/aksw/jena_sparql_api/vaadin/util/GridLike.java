package org.aksw.jena_sparql_api.vaadin.util;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasStyle;
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
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.shared.Registration;

/**
 * Vaadin's Grid is a concrete class.
 * This class is an interface that exposes several relevant methods.
 */
public interface GridLike<T>
    extends HasStyle
    // extends Component ?
{
    /** @see Grid#getDataCommunicator() */
    DataCommunicator<T> getDataCommunicator();

    /** @see Grid#getDataProvider() */
    DataProvider<T, ?> getDataProvider();

    /** @see Grid#setDataProvider(DataProvider) */
    void setDataProvider(DataProvider<T, ?> dataProvider);

    /** @see Grid#prependHeaderRow() */
    HeaderRow prependHeaderRow();

    /** @see Grid#appendHeaderRow() */
    HeaderRow appendHeaderRow();


    /** @see Grid#setMultiSort(boolean) */
    void setMultiSort(boolean multiSort);

    /** @see Grid#setSelectionMode(SelectionMode) */
    GridSelectionModel<T> setSelectionMode(SelectionMode selectionMode);

    /** @see Grid#getSelectionModel() */
    GridSelectionModel<T> getSelectionModel();

    /** @see Grid#setPageSize(int) */
    void setPageSize(int pageSize);

    /** @see Grid#removeAllColumns(int) */
    void removeAllColumns();

    /** @see Grid#addColumn(ValueProvider) */
    Column<T> addColumn(ValueProvider<T, ?> valueProvider);

    /** @see Grid#addColumn(Renderer) */
    Column<T> addColumn(Renderer<T> renderer);

    /** @see Grid#addColumn(String) */
    Column<T> addColumn(String propertyName);

    /** @see Grid#getColumnByKey(String) */
    Column<T> getColumnByKey(String columnKey);

    /** @see Grid#addComponentColumn(ValueProvider) */
    <V extends Component> Column<T> addComponentColumn(ValueProvider<T, V> componentProvider);

    /** @see Grid#addContextMenu(ValueProvider) */
    GridContextMenu<T> addContextMenu();

    /** @see Grid#asSingleSelect() */
    SingleSelect<Grid<T>, T> asSingleSelect();

    /** @see Grid#addItemClickListener(ComponentEventListener) */
    Registration addItemClickListener(ComponentEventListener<ItemClickEvent<T>> listener);

    /** @see Grid#addItemDoubleClickListener(ComponentEventListener) */
    Registration addItemDoubleClickListener(ComponentEventListener<ItemDoubleClickEvent<T>> listener);
}
