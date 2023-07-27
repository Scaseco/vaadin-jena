package org.aksw.jenax.vaadin.label;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;


/** Interface to bundle resource extraction and label assembly. */
public interface LabelAssembler<T, R, L> {
    Set<R> extractResources(T object);
    String toString(T object, Map<R, L> resourceToLabel);

    /** Simple version where an item is mapped to a single intermediate value from which the label is derived. */
    public static <T, R, L> LabelAssembler<T, R, L> of(Function<T, R> resourceExtractor, BiFunction<T, L, String> stringAssembler) {
        return LabelAssembler.ofSet(item -> Collections.singleton(resourceExtractor.apply(item)), (item, map) -> {
            R resource = resourceExtractor.apply(item);
            L label = map.get(resource);
            String r = stringAssembler.apply(item, label);
            return r;
        });
    }

    public static <T, R, L> LabelAssembler<T, R, L> ofSet(Function<T, Set<R>> resourceExtractor, BiFunction<T, Map<R, L>, String> stringAssembler) {
        return new LabelAssemblerImpl<>(resourceExtractor, stringAssembler);
    }

    public static class LabelAssemblerImpl<T, R, L>
        implements LabelAssembler<T, R, L>
    {
        protected Function<T, Set<R>> resourceExtractor;
        protected BiFunction<T, Map<R, L>, String> stringAssembler;

        public LabelAssemblerImpl(Function<T, Set<R>> resourceExtractor,
                BiFunction<T, Map<R, L>, String> stringAssembler) {
            super();
            this.resourceExtractor = resourceExtractor;
            this.stringAssembler = stringAssembler;
        }

        @Override
        public Set<R> extractResources(T object) {
            return resourceExtractor.apply(object);
        }

        @Override
        public String toString(T object, Map<R, L> resourceToLabel) {
            return stringAssembler.apply(object, resourceToLabel);
        }
    }
}
