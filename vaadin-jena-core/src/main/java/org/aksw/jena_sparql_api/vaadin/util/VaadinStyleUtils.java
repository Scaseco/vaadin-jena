package org.aksw.jena_sparql_api.vaadin.util;

import com.vaadin.flow.dom.Style;

public class VaadinStyleUtils {
    public static void setResizeVertical(Style style) {
        style.set("resize", "vertical");
        style.set("overflow", "auto");
    }

    public static void setResizeHorizontal(Style style) {
        style.set("resize", "horizontal");
        style.set("overflow", "auto");
    }

    public static void setResizeBoth(Style style) {
        style.set("resize", "both");
        style.set("overflow", "auto");
    }
}
