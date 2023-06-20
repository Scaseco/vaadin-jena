package org.aksw.jenax.vaadin.label;

import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public interface LabelService<R, L> {
    <X> void register(X component, R resource, BiConsumer<? super X, Map<R, L>> callback);
    <X> void register(X component, Set<R> resources, BiConsumer<? super X, Map<R, L>> callback);
}
