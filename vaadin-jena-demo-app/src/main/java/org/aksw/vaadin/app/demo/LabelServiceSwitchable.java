package org.aksw.vaadin.app.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.BiConsumer;

import org.aksw.commons.rx.lookup.LookupService;
import org.aksw.jenax.vaadin.label.LabelService;
import org.aksw.jenax.vaadin.label.VaadinLabelMgr;

public class LabelServiceSwitchable<R, L>
    implements LabelService<R, L>
{
    protected VaadinLabelMgr<R, L> delegate;
    protected List<LookupService<R, L>> lookupServices;
    protected int currentIdx;

    public LabelServiceSwitchable(VaadinLabelMgr<R, L> delegate) {
        this(delegate, new ArrayList<>(), 0);
    }

    public LabelServiceSwitchable(VaadinLabelMgr<R, L> delegate, List<LookupService<R, L>> services, int currentIdx) {
        super();
        this.delegate = delegate;
        this.lookupServices = services;
        this.currentIdx = currentIdx;
    }

    public List<LookupService<R, L>> getLookupServices() {
        return lookupServices;
    }

    public void next() {
        ++currentIdx;
        if (currentIdx >= lookupServices.size()) {
            currentIdx = 0;
        }

        LookupService<R, L> activeLooupService;
        try {
            activeLooupService = lookupServices.get(currentIdx);
        } catch (NoSuchElementException e) {
            activeLooupService = null; // Use default service
        }

        delegate.setLookupService(activeLooupService);
    }

    public void refreshAll() {
        delegate.refreshAll();
    }

    @Override
    public <X> void register(X component, R resource, BiConsumer<? super X, Map<R, L>> callback) {
        delegate.register(component, resource, callback);
    }

    @Override
    public <X> void register(X component, Set<R> resources, BiConsumer<? super X, Map<R, L>> callback) {
        delegate.register(component, resources, callback);
    }
}
