package org.aksw.jenax.vaadin.label;

import java.util.List;

import org.aksw.commons.rx.lookup.LookupService;

/**
 * This interface exists mainly because some proxying implementations
 * (e.g. with spring-cloud) do not support class casting to implementations.
 *
 * This does not work while
 * {@code (LabelServiceSwitchableImpl)proxyiedLabelServiceSwitchableImpl}
 *
 * Casting to the interface works:
 * {@code (LabelServiceSwitchable)proxyiedLabelServiceSwitchableImpl}
 */
public interface LabelServiceSwitchable<R, L>
    extends LabelService<R, L>
{
    List<LookupService<R, L>> getLookupServices();
    void next();
    void refreshAll();
}
