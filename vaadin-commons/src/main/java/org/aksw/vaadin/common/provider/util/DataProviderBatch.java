package org.aksw.vaadin.common.provider.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.aksw.commons.collections.utils.StreamUtils;

import com.google.common.base.Preconditions;
import com.vaadin.flow.data.provider.DataChangeEvent;
import com.vaadin.flow.data.provider.DataChangeEvent.DataRefreshEvent;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.DataProviderListener;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.shared.Registration;

/**
 * A data provider wrapper that batches results into lists of a given size.
 * Primary use case is pagination, where each page corresponds to a batch.
 *
 * @param <T>
 * @param <F>
 */
public class DataProviderBatch<T, F>
    implements DataProvider<List<T>, F>
{
    public static final int DEFAULT_BATCHSIZE = 5;
    private static final long serialVersionUID = 1L;

    /**
     * The actual data provider behind this wrapper.
     */
    protected DataProvider<T, F> dataProvider;
    protected int batchSize;

    /**
     * Constructs a filtering wrapper for a data provider.
     *
     * @param dataProvider
     *            the wrapped data provider, not <code>null</code>
     */
    protected DataProviderBatch(DataProvider<T, F> dataProvider) {
        this(dataProvider, DEFAULT_BATCHSIZE);
    }

    protected DataProviderBatch(DataProvider<T, F> dataProvider, int batchSize) {
        this.dataProvider = Objects.requireNonNull(dataProvider,
                "The wrapped data provider cannot be null.");
        setBatchSize(batchSize);
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        Preconditions.checkArgument(batchSize > 0, "BatchSize must be greater than 0");
        this.batchSize = batchSize;
    }

    public DataProvider<T, F> getDelegate() {
        return dataProvider;
    }

    @Override
    public boolean isInMemory() {
        return dataProvider.isInMemory();
    }

    @Override
    public void refreshAll() {
        dataProvider.refreshAll();
    }

    @Override
    public void refreshItem(List<T> items) {
        for (T item : items) {
            dataProvider.refreshItem(item);
        }
    }

    @Override
    public Object getId(List<T> items) {
        List<Object> result = new ArrayList<>(items);
        for (T item : items) {
            Object id = dataProvider.getId(item);
            result.add(id);
        }
        return result;
    }

    @Override
    public Registration addDataProviderListener(
            DataProviderListener<List<T>> listener) {
        return dataProvider.addDataProviderListener(ev -> listener.onDataChange(adaptDataChangeEvent(ev)));
    }

    public static <T> Comparator<T> adaptComparator(Comparator<List<T>> listCmp) {
        return listCmp == null
                ? null
                : (x, y) -> listCmp.compare(Collections.singletonList(x), Collections.singletonList(y));
    }

    protected DataChangeEvent<List<T>> adaptDataChangeEvent(DataChangeEvent<T> event) {
        DataChangeEvent<List<T>> result;
        if (event instanceof DataRefreshEvent) {
            DataRefreshEvent<T> ev = (DataRefreshEvent<T>)event;
            result = new DataRefreshEvent<>(this, Collections.singletonList(ev.getItem()));
        } else {
            result = new DataChangeEvent<>(this);
        }
        return result;
    }

    protected Query<T, F> adaptQuery(Query<List<T>, F> t) {
        int n = batchSize;
        Comparator<T> memSorting = adaptComparator(t.getSortingComparator().orElse(null));
        int adaptedOffset = t.getOffset() * n;
        int adaptedLimit = t.getLimit() * n;
        Query<T, F> result = new Query<>(adaptedOffset, adaptedLimit, t.getSortOrders(),
                memSorting, t.getFilter().orElse(null));
        return result;
    }

    @Override
    public int size(Query<List<T>, F> t) {
        int n = batchSize;
        Query<T, F> adaptedQuery = adaptQuery(t);
        int rawSize = dataProvider.size(adaptedQuery);
        int result = rawSize / n;
        if (rawSize % n != 0) {
            ++result;
        }
        return result;
    }

    @Override
    public Stream<List<T>> fetch(Query<List<T>, F> t) {
        int n = batchSize;
        Query<T, F> adaptedQuery = adaptQuery(t);
        Stream<T> rawStream = dataProvider.fetch(adaptedQuery);

        Stream<List<T>> result = StreamUtils.mapToBatch(rawStream.sequential(), n);
        return result;
    }

    /**
     * Gets the filter that should be used in the modified Query.
     *
     * @param query
     *            the current query
     * @return filter for the modified Query
     */
    // protected abstract M getFilter(Query<List<T>, F> query);}
}

