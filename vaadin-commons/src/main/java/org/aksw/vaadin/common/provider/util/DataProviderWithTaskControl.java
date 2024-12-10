package org.aksw.vaadin.common.provider.util;

import java.awt.Component;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;


/**
 * A DataProvider where each request (fetch and count) creates a task
 * and registers it with a TaskControlRegistry for monitoring.
 *
 * TODO A failed request should trigger an event that can be
 * used to call refresh all on the provider; which effectively is a retry.
 */
public class DataProviderWithTaskControl<T, F>
    extends DataProviderWrapperBase<T, F, F>
{
    private static final long serialVersionUID = 1L;

    protected TaskControlRegistry taskControlRegistry;

    /** The component if this data provider is specific to one (e.g. a vaadin grid) - null otherwise */
    protected Object component;

    /** The name of the tasks to generate */
    protected String taskLabel;


    // Perhaps the taskHandler needs to be part of the SparqlDataProvider:
    // Reasons:
    // Access to the QueryExecution.abort() method is better than just Stream.close()
    //   Though stream.close() could be designed such that concurrent abort is possible.
    //

    // protected List<Consumer<TaskControl<DataProvider<T, F>>>> taskHandlers;
    // protected BiConsumer<DataProvider<T, F>> onorr


    protected DataProviderWithTaskControl(DataProvider<T, F> dataProvider, TaskControlRegistry taskControlRegistry, Object component, String taskLabel) {
        super(dataProvider);
        this.taskControlRegistry = taskControlRegistry;

        this.component = component;
        this.taskLabel = taskLabel;
    }

    public Object getComponent() {
        return component;
    }

    public String getTaskName() {
        return taskLabel;
    }

    @Override
    public Stream<T> fetch(Query<T, F> t) {
        List<T> items;
        Stream<T> result;
        DataFetchTask<DataProvider<T, F>> task = new DataFetchTask<>(this, taskLabel);

        taskControlRegistry.register(task);

        try (Stream<T> base = super.fetch(t)) {
            task.setAbortAction(() -> base.close());
            items = base.collect(Collectors.toList());
            task.complete(null);
            result = items.stream();
        } catch (Exception e) {
            task.complete(e);
            result = IntStream.range(0, t.getLimit()).mapToObj(i -> (T)null);
        }
        return result;
    }

    @Override
    public int size(Query<T, F> t) {
        return super.size(t);
    }

    @Override
    protected F getFilter(Query<T, F> query) {
        return query.getFilter().orElse(null);
    }

    public static <T, F> DataProvider<T, F> wrap(DataProvider<T, F> dataProvider, TaskControlRegistry taskControlRegistry) {
        return wrap(dataProvider, taskControlRegistry, null, "Data retrieval");
    }

    public static <T, F> DataProvider<T, F> wrap(DataProvider<T, F> dataProvider, TaskControlRegistry taskControlRegistry, Object component, String taskLabel) {
        return new DataProviderWithTaskControl<>(dataProvider, taskControlRegistry, component, taskLabel);
    }
}

