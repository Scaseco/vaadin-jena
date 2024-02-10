package org.aksw.jena_sparql_api.vaadin.util;

import java.util.Objects;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.DataProviderListener;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.shared.Registration;

/**
 * Grid wrapper that shows a message when it is empty.
 *
 * Based on https://cookbook.vaadin.com/grid-message-when-empty
 */
@CssImport("./styles/gridex.css")
public class GridEx<T>
    // extends Composite<Div>
    extends Div // Expose the div to make it easy to control the size
    implements GridWrapper<T>
{
    private static final long serialVersionUID = 1L;
    // protected Div gridRoot;
    protected Grid<T> grid;

    protected DataProviderListener<T> dataProviderListener;
    protected Registration dataProviderListenerRegistration = null;

    public GridEx() {
        this(new Grid<>());
    }

    public GridEx(Class<T> beanType) {
        this(beanType, true);
    }

    public GridEx(Class<T> beanType, boolean autoCreateColumns) {
        this(new Grid<>(beanType, autoCreateColumns));
    }

    public GridEx(Grid<T> grid) {
        super();
        this.grid = Objects.requireNonNull(grid);

        // Not ideal adding our own class here - intrusive.
        grid.addClassName("grid");

        // Always make the grid fill the parent div
        grid.setSizeFull();

        // gridRoot = initContent();
        Div gridRoot = this;
//        gridRoot.getStyle().set("display", "flex");
        gridRoot.addClassName("gridex");

        Div warning = new Div(new Text("There is no data to display"));
//        warning.setSizeFull();
        warning.addClassName("warning");

        gridRoot.add(grid, warning);

//        applyStyle(grid.getStyle());
//        applyStyle(warning.getStyle());
//
//        warning.getStyle()
//            .set("display", "flex")
//            .set("justify-content", "center")
//            .set("align-items", "center")
//            .set("font-size", "32px");
//        position: relative;
//        height: var(--grid-heigth);
//        width: 100%;
//      }
//        gridRoot.getStyle()
//            .set("position", "relative");
            // .set("height", "50%")// "var(--grid-heigth)")
            // .set("width", "100%");


        // getContent().add(new VerticalLayout(gridRoot));

        dataProviderListener = e -> {
            if (grid.getDataProvider().size(new Query<>()) == 0) {
                 warning.removeClassName("hidden");
                // warning.setVisible(true);
                // warning.setStyle("display", "")
//                warning.getStyle().set("display", "flex");
            } else {
                 warning.addClassName("hidden");
                // warning.setVisible(false);
//                warning.getStyle().set("display", "none");
            }
            // grid.recalculateColumnWidths();
        };
        updateDataProviderListenerRegistration();
    }

//    protected void applyStyle(Style style) {
//          //height: var(--grid-heigth);
//        style
//            // .set("width", "100%")
//            .set("position", "absolute")
//            .set("top", "0px")
//            .set("left", "0px");
//    }

    @Override
    public Grid<T> getDelegate() {
        return grid;
    }

    @Override
    public void setDataProvider(DataProvider<T, ?> dataProvider) {
        GridWrapper.super.setDataProvider(dataProvider);
        updateDataProviderListenerRegistration();
    }

    /**
     * This method is called automatically on initialization.
     * Should the grid's dataProvider instance ever change (this can be avoided using a wrapper),
     * this method must be invoked.
     */
    public synchronized void updateDataProviderListenerRegistration() {
        if (dataProviderListenerRegistration != null) {
            dataProviderListenerRegistration.remove();
        }
        dataProviderListenerRegistration = grid.getDataProvider().addDataProviderListener(dataProviderListener);

        // Initial run of the listener, as there is no event fired for the initial state
        // of the data set that might be empty or not.
        dataProviderListener.onDataChange(null);
    }
}


