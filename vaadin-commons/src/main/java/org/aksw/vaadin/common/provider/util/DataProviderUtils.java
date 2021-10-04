package org.aksw.vaadin.common.provider.util;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.data.provider.DataProvider;

public class DataProviderUtils {
    public static <T, F> DataProvider<T, F> wrapWithErrorHandler(DataProvider<T, F> dataProvider) {
        DataProvider<T, F> result = new DataProviderWrapperWithCustomErrorHandler<>(
                dataProvider,
                th -> {
                    Notification n = new Notification(ExceptionUtils.getRootCauseMessage(th), 5000);
                    n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    n.open();
                });
        return result;
    }
}
