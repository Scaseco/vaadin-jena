package org.aksw.vaadin.common.provider.util;

// XXX Consider renaming to TaskMonitor or having TaskMonitor a subclass of a TaskControlRegistry
public interface TaskControlRegistry {
    void register(TaskControl<?> taskControl);
}
