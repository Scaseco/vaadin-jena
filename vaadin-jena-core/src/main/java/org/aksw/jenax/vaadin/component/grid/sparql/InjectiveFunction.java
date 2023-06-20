package org.aksw.jenax.vaadin.component.grid.sparql;

import java.util.function.Function;

/**
 * Marker interface for functions for which holds that if fn(x) = fn(y) then x = y.
 * In other words, for a given y (in the function's defined range) there is only one corresponding x.
 */
public interface InjectiveFunction<I, O>
    extends Function<I, O>
{
    @Override
    O apply(I i);
}
