package org.aksw.vaadin.common.provider.util;

import org.aksw.vaadin.common.component.util.NotificationUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.DataProvider;

public class DataProviderUtils {
    private static final Logger logger = LoggerFactory.getLogger(DataProviderUtils.class);

    /**
     * Wraps the grid's current data provider with an error handling one.
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
                    th.printStackTrace();
                    logger.warn("A problem with a DataProvider was encountered.", th);
                    String msg = ExceptionUtils.getRootCauseMessage(th);
                    NotificationUtils.error(msg);
//                    Notification n = new Notification(ExceptionUtils.getRootCauseMessage(th), 5000);
//                    n.addThemeVariants(NotificationVariant.LUMO_ERROR);
//                    n.open();
                });
        return result;
    }
}
