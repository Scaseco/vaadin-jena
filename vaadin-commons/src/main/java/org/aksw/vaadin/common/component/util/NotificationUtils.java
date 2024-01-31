package org.aksw.vaadin.common.component.util;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;

public class NotificationUtils {
    public static void success(String text) {
        open(text, 5000, NotificationVariant.LUMO_SUCCESS, Position.TOP_CENTER);
    }

    public static void error(String text) {
        open(text, 5000, NotificationVariant.LUMO_ERROR, Position.TOP_CENTER);
    }

    public static void open(String text, int timeout, NotificationVariant variant, Position position) {
        Notification n = new Notification(text, timeout, position);
        n.addThemeVariants(variant);
        n.open();
    }
}
