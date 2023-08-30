package org.aksw.vaadin.common.provider.util;

import java.util.stream.Stream;

import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.DataProviderWrapper;
import com.vaadin.flow.data.provider.Query;

/**
 * Data Provider that delegates to an empty data provider if
 * a condition is not match.
 */
public class DataProviderActivatable<T, F>
    extends DataProviderWrapper<T, F, F>
{
    private static final long serialVersionUID = 1L;

    protected boolean isEnabled;

    protected DataProviderActivatable(DataProvider<T, F> dataProvider) {
        super(dataProvider);
    }

    public DataProvider<T, F> getDelegate() {
        return this.dataProvider;
    }

    @Override
    public int size(Query<T, F> t) {
        int result = isEnabled
                ? super.size(t)
                : 0;
        return result;
    }

    @Override
    public Stream<T> fetch(Query<T, F> t) {
        Stream<T> result = isEnabled
                ? super.fetch(t)
                : Stream.empty();
        return result;
    }

    @Override
    protected F getFilter(Query<T, F> query) {
        return query.getFilter().orElse(null);
    }

    /**
     * After calling {@link #setEnabled(boolean)} with argument {@code true}, typically {@link #refreshAll()}
     * should be called.
     */
    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public boolean isEnabled() {
        return isEnabled;
    }
}
