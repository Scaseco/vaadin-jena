package org.aksw.jena_sparql_api.vaadin.util;

import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;

import com.google.common.collect.Sets;
import com.google.common.graph.Traverser;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.Style;

public class VaadinComponentUtils {
    /**
     *
     * @param component
     * @param deferred If true then resize is applied in a separate animation frame
     */
    public static void notifyResize(Component component, boolean deferred) {
        notifyResize(component.getElement(), deferred);
    }

    public static void notifyResize(Element element, boolean deferred) {
        if (deferred) {
            element.executeJs("requestAnimationFrame((function() { this.notifyResize(); }).bind(this))");
        } else {
            element.executeJs("this.notifyResize()");
        }
    }


    /**
     * Resize components and or any of their ancestors
     *
     * @param components The components whose ancestors to notify of resize
     * @param deferred If true then resize is applied in a separate animation frame
     * @param predicate Predicate for whether to notify a matching component
     */
    public static void notifyResizeAncestors(Iterable<Component> components, boolean deferred,
            Predicate<? super Component> predicate) {
        // Certain components (especially grids) may need to be resized
        // after setting the labels
        Set<Component> componentsToResize = Sets.newIdentityHashSet();
        for (Object component : components) {
            Component c = (Component)component;

            Iterable<Component> contribs = Traverser.forTree((Component x) -> x.getParent()
                    .map(Collections::singleton)
                    .orElse(Collections.emptySet()))
                .depthFirstPreOrder(c);

            for (Component contrib : contribs) {
                if (predicate.test(contrib)) {
                    componentsToResize.add(contrib);
                }
            }
        }

        for (Component cc : componentsToResize) {
            VaadinComponentUtils.notifyResize(cc, deferred);
        }
    }

//    public static void notifyResize(Component component) {
//        component.getElement().executeJs("this.notifyResize()");
//    }
}
