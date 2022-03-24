package org.aksw.jenax.vaadin.label;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import org.aksw.commons.rx.lookup.LookupService;
import org.aksw.commons.util.concurrent.ScheduleOnce2;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.UI;

public class VaadinLabelService<R, L>
    extends LabelService<Component, R, L>
{
    protected ScheduleOnce2 retrieval = new ScheduleOnce2(x -> UI.getCurrent().access(() -> {
        try {
            x.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }), () -> {
        super.scheduleRetrieval();
        return null;
    });
/*
    protected ScheduleOnce2 application = new ScheduleOnce2(x -> UI.getCurrent().access(() -> {
        try {
            x.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }), () -> {
        super.scheduleApplication();
        return null;
    });
*/

    public VaadinLabelService(LookupService<R, L> lookupService) {
        super(lookupService);
    }

    @Override
    public void scheduleRetrieval() {
        retrieval.scheduleTask();
//        UI.getCurrent().access(() -> {
//            super.scheduleRetrieval();
//        });
    }

    @Override
    protected void scheduleApplication(Collection<Object> components) {
//        application.scheduleTask();
        UI.getCurrent().access(() -> {
            super.scheduleApplication(components);
        });
    }

    public <X extends HasText> X forHasText(X component, R resource) {
        register(component, Collections.singleton(resource), (c, lmap) -> {
            L label = lmap.get(resource);
            String text = Objects.toString(label);
            c.setText(text);
        });

        return component;
    }
}
