package org.aksw.vaadin.common.component.util;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

public class NotificationUtils {
    public static void error(String text) {
        Notification n = new Notification(text, 5000);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
        n.open();
    }
}
