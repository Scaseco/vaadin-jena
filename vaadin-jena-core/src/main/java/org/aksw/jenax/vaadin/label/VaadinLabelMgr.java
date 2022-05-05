package org.aksw.jenax.vaadin.label;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.aksw.commons.collections.trees.TreeUtils;
import org.aksw.commons.rx.lookup.LookupService;
import org.aksw.commons.util.concurrent.ScheduleOnce2;
import org.aksw.jena_sparql_api.vaadin.util.VaadinComponentUtils;

import com.google.common.collect.Sets;
import com.google.common.graph.Traverser;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;

public class VaadinLabelMgr<R, L>
    extends LabelMgr<R, L>
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

    public VaadinLabelMgr(LookupService<R, L> lookupService) {
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
        UI.getCurrent().access(() -> {
            super.scheduleApplication(components);

            // TODO Argument should have component type
            Collection<Component> cs = components.stream().map(x -> (Component)x).collect(Collectors.toList());
            VaadinComponentUtils.notifyResizeAncestors(cs, false,
                    c -> c instanceof Grid);
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
