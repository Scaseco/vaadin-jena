package org.aksw.vaadin.common.provider.util;

import java.util.function.Function;

import com.vaadin.flow.data.provider.DataProvider;

@FunctionalInterface
public interface DataProviderTransform<T, F>
	extends Function<DataProvider<T, F>, DataProvider<T, F>>
{
}
