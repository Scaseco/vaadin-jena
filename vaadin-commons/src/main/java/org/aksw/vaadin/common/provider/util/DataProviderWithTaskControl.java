package org.aksw.vaadin.common.provider.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.aksw.commons.util.exception.FinallyRunAll;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;


class DataFetchTask<S>
	implements TaskControl<S>
{
	protected S source;
	protected String name;
	// XXX description
	
	protected List<Consumer<Throwable>> completionHandlers = new ArrayList<>();
	protected Runnable abortAction;

	protected Throwable throwable;
	protected boolean isComplete;
	
	protected volatile boolean hasBeenAborted = false;
		
	public DataFetchTask(S source, String name) {
		super();
		this.source = source;
		this.name = name;
	}

	// listener map
	// request
	// sourceState
	// response

	@Override
	public S getSource() {
		return source;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public synchronized void abort() {
		hasBeenAborted = true;
		
		// The task may have failed before the abort action became available
		if (abortAction != null) {
			abortAction.run();
		}
	}

	@Override
	public boolean isComplete() {
		return isComplete;
	}

	@Override
	public Throwable getThrowable() {
		return throwable;
	}

	@Override
	public Registration whenComplete(Consumer<Throwable> handler) {
		// If completed then trigger immediately, otherwise enqueue until done.
		if (isComplete) {
			handler.accept(throwable);
		} else {
			completionHandlers.add(handler);
		}
		return () -> completionHandlers.remove(handler);
	}

	synchronized void setAbortAction(Runnable action) {
		this.abortAction = action;
		
		if (hasBeenAborted) {
			abort();
		}
	}
	
	void complete(Throwable throwable) {
		if (isComplete()) {
			throw new IllegalStateException("Must not complete more than once.");
		}

		this.throwable = throwable;
		this.isComplete = true;
		
		fireEvents();
	}
	
	protected void fireEvents() {
		FinallyRunAll.runAll(
			completionHandlers,
			handler -> handler.accept(throwable),
			() -> completionHandlers.clear()
		);
	}
}

/** Unfinished. A failed request should trigger an event that can be
 * used to call refresh all on the provider; which effectively is a retry.
 */
public class DataProviderWithTaskControl<T, F>
	extends DataProviderWrapperBase<T, F, F>
{
	protected TaskControlRegistry taskControlRegistry;
	
	// Perhaps the taskHandler needs to be part of the SparqlDataProvider:
	// Reasons:
	// Access to the QueryExecution.abort() method is better than just Stream.close()
	//   Though stream.close() could be designed such that concurrent abort is possible.
	//
	
	// protected List<Consumer<TaskControl<DataProvider<T, F>>>> taskHandlers;
	// protected BiConsumer<DataProvider<T, F>> onorr
	
	
	protected DataProviderWithTaskControl(DataProvider<T, F> dataProvider, TaskControlRegistry taskControlRegistry) {
		super(dataProvider);
		this.taskControlRegistry = taskControlRegistry;
		
//		this.addTaskHandler(taskControl -> {
//			taskControl.wh
//		})
	}
	
//	public Registration addTaskHandler(Consumer<TaskControl<DataProvider<T, F>>> listener) {
//		taskHandlers.add(listener);
//		return () -> taskHandlers.remove(listener);
//	}

	@Override
	public Stream<T> fetch(Query<T, F> t) {
		UI ui = UI.getCurrent();
		
		List<T> items;
		Stream<T> result;
		DataFetchTask<DataProvider<T, F>> task = new DataFetchTask<>(this, "Data retrieval");
//		new Thread(() -> {
//			ui.access(() -> {
//				taskControlRegistry.register(task);
//			});
//		}).start();
		taskControlRegistry.register(task);

		try (Stream<T> base = super.fetch(t)) {
			// Thread.sleep(10000);

			task.setAbortAction(() -> base.close());
			// fireEvent(task);
			items = base.collect(Collectors.toList());
			task.complete(null);
			result = items.stream();
		} catch (Exception e) {
			task.complete(e);
			result = IntStream.range(0, t.getLimit()).mapToObj(i -> (T)null);
		}
		return result;
	}
//	protected void fireEvent(TaskControl<DataProvider<T, F>> event) {
//		FinallyRunAll.runAll(
//			taskHandlers,
//			handler -> handler.accept(event),
//			null
//		);
//	}

	@Override
	public int size(Query<T, F> t) {
		return super.size(t);
	}
	
    @Override
    protected F getFilter(Query<T, F> query) {
        return query.getFilter().orElse(null);
    }
    
    public static <T, F> DataProvider<T, F> wrap(DataProvider<T, F> dataProvider, TaskControlRegistry taskControlRegistry) {
    	return new DataProviderWithTaskControl<>(dataProvider, taskControlRegistry);
    }
}

