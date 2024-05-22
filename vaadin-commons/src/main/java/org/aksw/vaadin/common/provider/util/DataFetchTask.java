package org.aksw.vaadin.common.provider.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.aksw.commons.util.exception.FinallyRunAll;

public class DataFetchTask<S>
    implements TaskControl<S>
{
    /** The DataProvider instance that is the source of this task */
    protected S source;
    protected String label;
    // XXX description
    // XXX the component that is affected by the task - could be accessible via the source though

    protected List<Consumer<Throwable>> completionHandlers = new ArrayList<>();
    protected Runnable abortAction;

    protected Throwable throwable;
    protected boolean isComplete;

    protected volatile boolean hasBeenAborted = false;

    public DataFetchTask(S source, String label) {
        super();
        this.source = source;
        this.label = label;
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
    public String getLabel() {
        return label;
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
