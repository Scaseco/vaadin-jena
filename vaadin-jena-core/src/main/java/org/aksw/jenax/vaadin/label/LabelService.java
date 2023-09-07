package org.aksw.jenax.vaadin.label;

import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * A label service keeps track of the relations between a set of components and resources.
 * Implementations should only make weak references to components.
 * Furthermore, the label service can resolve resources to labels.
 *
 * @param <R>
 * @param <L>
 */
public interface LabelService<R, L> {
    <X> void register(X component, R resource, BiConsumer<? super X, Map<R, L>> callback);
    <X> void register(X component, Set<R> resources, BiConsumer<? super X, Map<R, L>> callback);
}
