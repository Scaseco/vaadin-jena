package org.aksw.jenax.vaadin.component.grid.sparql;

import org.aksw.commons.collections.generator.Generator;
import org.aksw.commons.collections.generator.GeneratorBlacklist;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * An injective function hat tracks which output mapped to which input.
 */
public class DynamicInjectiveFunction<I, O>
    implements InjectiveFunction<I, O>
{
    protected BiMap<I, O> map;
    protected Generator<O> generator;

    protected DynamicInjectiveFunction(BiMap<I, O> map, Generator<O> generator) {
        super();
        this.map = map;
        this.generator = generator;
    }

    public static <I, O> DynamicInjectiveFunction<I, O> of(Generator<O> generator) {
        return new DynamicInjectiveFunction<>(HashBiMap.create(), generator);
    }

    @Override
    public O apply(I i) {
        O result = map.computeIfAbsent(i, key -> {
            // Return the next value from the generator
            O r = GeneratorBlacklist.create(generator, map.inverse().keySet()).next();
            return r;
        });
        return result;
    }

    public BiMap<I, O> getMap() {
        return map;
    }

}
