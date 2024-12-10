package org.aksw.vaadin.common.provider.util;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

import com.vaadin.flow.data.provider.AbstractDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;

public class DataProviderUnion<T>
    extends AbstractDataProvider<T, Void>
{
    private static final long serialVersionUID = 1L;

    private final Set<T> externalItems = new LinkedHashSet<>();
    private final DataProvider<T, Void> originalDataProvider;

    public DataProviderUnion(DataProvider<T, Void> originalDataProvider) {
        this.originalDataProvider = originalDataProvider;
    }

    public void addExternalItems(Set<T> items) {
        externalItems.addAll(items);
        refreshAll();
    }

    public void clearExternalItems() {
        externalItems.clear();
        refreshAll();
    }

    @Override
    public boolean isInMemory() {
        return originalDataProvider.isInMemory();
    }

    @Override
    public int size(Query<T, Void> query) {
        return (int) fetch(query).count();
    }

    @Override
    public Stream<T> fetch(Query<T, Void> query) {
        Stream<T> originalStream = originalDataProvider.fetch(query);
        return Stream.concat(originalStream, externalItems.stream())
                .distinct()
                .skip(query.getOffset())
                .limit(query.getLimit());
    }
}
