package org.aksw.vaadin.common.provider.util;

import org.aksw.vaadin.common.component.util.NotificationUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.vaadin.flow.data.provider.DataProvider;

public class DataProviderUtils {
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
