package org.aksw.vaadin.common.provider.util;

import org.aksw.commons.util.delegate.Delegated;
import org.aksw.commons.util.delegate.Unwrappable;

import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.DataProviderWrapper;

/** DataProvider with a public {@link #getDelegate()} method. */
public abstract class DataProviderWrapperBase<T, F, M>
    extends DataProviderWrapper<T, F, M>
    implements Delegated<DataProvider<T, M>>, Unwrappable
{
    private static final long serialVersionUID = 1L;

    protected DataProviderWrapperBase(DataProvider<T, M> dataProvider) {
        super(dataProvider);
    }

    @Override
    public DataProvider<T, M> delegate() {
        return this.dataProvider;
    }

    public DataProvider<T, M> getDelegate() {
        return this.dataProvider;
    }
}
