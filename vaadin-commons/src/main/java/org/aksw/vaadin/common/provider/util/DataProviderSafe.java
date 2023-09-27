package org.aksw.vaadin.common.provider.util;

import java.util.Optional;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import com.google.common.cache.Cache;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;

/** Unfinished - a wrapper that ensures that size and fetch are in sync even in case of errors. fetch would return errorneous items as 'null' */
public class DataProviderSafe<T, F>
    extends DataProviderWrapperBase<T, F, F>
{
    private static final long serialVersionUID = 1L;

    protected Cache<Query<T, F>, Future<Optional<Long>>> queryToSize;

    protected DataProviderSafe(DataProvider<T, F> dataProvider) {
        super(dataProvider);
    }

    @Override
    public int size(Query<T, F> t) {
        // TODO Auto-generated method stub
        return super.size(t);
    }

    @Override
    public Stream<T> fetch(Query<T, F> t) {
        // TODO Auto-generated method stub
        return super.fetch(t);
    }

    @Override
    protected F getFilter(Query<T, F> query) {
        return query.getFilter().orElse(null);
    }
}
