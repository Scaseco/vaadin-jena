package org.aksw.vaadin.common.provider.util;

import org.aksw.vaadin.common.component.util.NotificationUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.DataProvider;

public class DataProviderUtils {
    /**
     * Gets a grid's data provider, wraps it with the error handler and sets the wrapped instance on the grid.
     * This method does nothing if the data provider is already wrapped.
     */
    public static <T> void wrapWithErrorHandler(Grid<T> grid) {
        DataProvider<T, ?> dataProvider = grid.getDataProvider();
        if (!(dataProvider instanceof DataProviderWrapperWithCustomErrorHandler)) {
            dataProvider = wrapWithErrorHandler(dataProvider);
        }
        grid.setDataProvider(dataProvider);
    }

    public static <T, F> DataProvider<T, F> wrapWithErrorHandler(DataProvider<T, F> dataProvider) {
        DataProvider<T, F> result = new DataProviderWrapperWithCustomErrorHandler<>(
                dataProvider,
                th -> {
                    String msg = ExceptionUtils.getRootCauseMessage(th);
                    NotificationUtils.error(msg);
//                    Notification n = new Notification(ExceptionUtils.getRootCauseMessage(th), 5000);
//                    n.addThemeVariants(NotificationVariant.LUMO_ERROR);
//                    n.open();
                });
        return result;
    }
}
