package org.aksw.vaadin.common.provider.util;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;


public class DataProviderWrapperWithCustomErrorHandler<T, F>
    extends DataProviderWrapperBase<T, F, F>
{
    private static final Logger logger = LoggerFactory.getLogger(DataProviderWrapperWithCustomErrorHandler.class);

    private static final long serialVersionUID = 1L;

    protected Consumer<? super Throwable> customErrorHandler;

    public DataProviderWrapperWithCustomErrorHandler(
            DataProvider<T, F> dataProvider,
            Consumer<? super Throwable> customErrorHandler) {
        super(dataProvider);
        this.customErrorHandler = Objects.requireNonNull(customErrorHandler, "Error handler must not be null");
    }

    // TODO We may have to add a cache for size(query) such that if fetch fails it fills up missing data up to size

    @Override
    public int size(Query<T, F> t) {
        int result;
        try {
            result = super.size(t);
        } catch (Exception e) {
            logger.warn("An unexpected exception was raised:", e);
            customErrorHandler.accept(e);
            result = 0;
        }
        return result;
    }

    @Override
    public Stream<T> fetch(Query<T, F> t) {
        Stream<T> result;
        try {
            result = super.fetch(t);
        } catch (Exception e) {
            logger.warn("An unexpected exception was raised:", e);
            customErrorHandler.accept(e);
            result = Stream.empty();
        }
        return result;
    }

    @Override
    protected F getFilter(Query<T, F> query) {
        return query.getFilter().orElse(null);
    }
}
