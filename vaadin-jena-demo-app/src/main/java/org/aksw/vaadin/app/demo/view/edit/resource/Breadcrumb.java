package org.aksw.vaadin.app.demo.view.edit.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.aksw.commons.collection.observable.ObservableValue;
import org.aksw.jenax.path.core.PathOpsPP;
import org.aksw.jenax.path.core.PathPP;
import org.aksw.jenax.vaadin.label.LabelMgr;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.path.P_Path0;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.shared.Registration;


public class Breadcrumb
    extends HorizontalLayout
{
    protected LabelMgr<Node, String> labelService;
    // protected PathPP path;
    protected ObservableValue<PathPP> pathModel;


    protected List<Registration> registrations = new ArrayList<>();


    public Breadcrumb(LabelMgr<Node, String> labelService) {
        this.labelService = labelService;

        pathModel = PathPPModelImpl.create();
        pathModel.addValueChangeListener(ev -> {
            refresh();
            PathPP newValue = ev.getNewValue();
            if (!Objects.equals(newValue, ev.getOldValue())) {
                fireEvent(new BreadcrumbEvent(this, false, newValue));
            }
        }).fire();

        // pathModel.set(PathOpsPP.get().newRoot());

        // add(new Span("foo"));
        // add(new Span("foo"));
    }

    public void refresh() {
        registrations.forEach(Registration::remove);
        registrations.clear();
        removeAll();

        PathPP path = pathModel.get();

        List<PathPP> ancestors = new ArrayList<>();
        PathPP current = path;
        while (current != null) {
            ancestors.add(current);
            current = current.getParent();
        }
        Collections.reverse(ancestors);

        for (int i = 0; i < ancestors.size(); ++i) {
            PathPP item = ancestors.get(i);

            if (i != 0) {
                add(new Span("/"));
            }
            Button span = new Button();
            // span.setHref("");
            if (item.getSegments().size() == 0) {
                // span.add(VaadinIcon.HOME.create());
                span.setText("⌂");
            } else {
                P_Path0 lastSegment = item.getFileName().toSegment();
                Node node = lastSegment.getNode();
                // span.getElement().addEventListener("click", () -> {)
                labelService.register(span, Collections.singleton(node), (c, map) ->
                    c.setText(map.get(node) + (lastSegment.isForward() ? "»" : "«")));
            }
//            registrations.add(span.getElement().addEventListener("click", ev -> {
//                fireEvent(new BreadcrumbEvent(this, false, item));
//            }));
            registrations.add(span.addClickListener(ev -> {
                pathModel.set(item);
                fireEvent(new BreadcrumbEvent(this, false, item));
            }));
            add(span);
        }
    }

    public Registration addPathListener(ComponentEventListener<BreadcrumbEvent> listener) {
        return addListener(BreadcrumbEvent.class, listener);
    }

    public ObservableValue<PathPP> getModel() {
        return pathModel;
    }

}
