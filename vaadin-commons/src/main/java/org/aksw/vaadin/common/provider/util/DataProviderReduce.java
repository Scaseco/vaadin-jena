package org.aksw.vaadin.common.provider.util;

import java.util.Map.Entry;
import java.util.stream.Stream;

import org.aksw.commons.collections.utils.StreamUtils;
import org.aksw.commons.util.delegate.Delegated;
import org.aksw.commons.util.delegate.Unwrappable;

import com.vaadin.flow.data.provider.AbstractDataProvider;
import com.vaadin.flow.data.provider.DataChangeEvent;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.DataProviderListener;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.shared.Registration;

/**
 *
 * @param <T>
 * @param <F>
 */
public class DataProviderReduce<T, F>
    extends AbstractDataProvider<Entry<T, T>, F>
    implements Unwrappable, Delegated<DataProvider<T, F>>
{
    private static final long serialVersionUID = 1L;

    protected DataProvider<T, F> delegate;

    protected DataProviderReduce(DataProvider<T, F> delegate) {
        super();
        this.delegate = delegate;
    }

    public static <T, F> DataProvider<Entry<T, T>, F> of(DataProvider<T, F> delegate) {
        return new DataProviderReduce<>(delegate);
    }

    @Override
    public int size(Query<Entry<T, T>, F> query) {
        Query<T, F> q = adapt(query, false);
        int result = delegate.size(q);
        return result;
    }

    public static <T, F> Query<T, F> adapt(Query<Entry<T, T>, F> query, boolean offsetMinusOne) {
        int offset = query.getOffset();
        if (offset > 0 && offsetMinusOne) {
            --offset;
        }
        return new Query<>(query.getOffset(), query.getLimit(), query.getSortOrders(), null, query.getFilter().orElse(null));
    }

    @Override
    public Stream<Entry<T, T>> fetch(Query<Entry<T, T>, F> query) {
        boolean offsetMinusOne = query.getOffset() > 0;
        Query<T, F> q = adapt(query, offsetMinusOne);
        Stream<Entry<T, T>> result = StreamUtils.streamToPairs(delegate.fetch(q));
        // If the offset was reduced by 1 then skip the first item
        if (offsetMinusOne) {
            result = result.skip(1);
        }
        return result;
    }

    @Override
    public boolean isInMemory() {
        return delegate.isInMemory();
    }

    @Override
    public DataProvider<T, F> delegate() {
        return delegate;
    }

    @Override
    public void refreshAll() {
        delegate().refreshAll();
    }

    @Override
    public void refreshItem(Entry<T, T> item) {
        delegate().refreshItem(item.getValue());
    }

    @Override
    public Object getId(Entry<T, T> item) {
        return delegate().getId(item.getValue());
    }

    @Override
    public Registration addDataProviderListener(
            DataProviderListener<Entry<T, T>> listener) {
        // TODO Adapt the event!
        return delegate().addDataProviderListener(ev -> listener.onDataChange(new DataChangeEvent<>(this)));
    }

}
