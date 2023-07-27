package org.aksw.jenax.vaadin.component.breadcrumb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.aksw.commons.collection.observable.ObservableValue;
import org.aksw.commons.collection.observable.ObservableValueImpl;
import org.aksw.commons.path.core.Path;
import org.aksw.jenax.path.core.FacetStep;
import org.aksw.jenax.vaadin.label.LabelAssembler;
import org.aksw.jenax.vaadin.label.LabelService;
import org.aksw.jenax.vaadin.label.VaadinLabelMgr;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.path.P_Path0;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.shared.Registration;

public class Breadcrumb<T>
    extends HorizontalLayout
{
    private static final long serialVersionUID = 1L;

    protected LabelService<Node, String> labelService;
    protected ObservableValue<Path<T>> pathModel;
    protected LabelAssembler<T, Node, String> labelAssembler;

    protected List<Registration> registrations = new ArrayList<>();

    public ObservableValue<Path<T>> getModel() {
        return pathModel;
    }

    public void setValue(Path<T> value) {
        pathModel.set(value);
    }

    public Breadcrumb(Path<T> basePath, LabelService<Node, String> labelService, LabelAssembler<T, Node, String> labelAssembler) {
        this.labelService = labelService;
        this.labelAssembler = labelAssembler;

        pathModel = ObservableValueImpl.create(basePath); // PathPPModelImpl.create();
        pathModel.addValueChangeListener(ev -> {
            refresh();
            Path<T> newValue = ev.getNewValue();
            if (!Objects.equals(newValue, ev.getOldValue())) {
                fireEvent(new BreadcrumbEvent<>(this, false, newValue));
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

        Path<T> path = pathModel.get();

        List<Path<T>> ancestors = new ArrayList<>();
        Path<T> current = path;
        while (current != null) {
            ancestors.add(current);
            current = current.getParent();
        }
        Collections.reverse(ancestors);

        for (int i = 0; i < ancestors.size(); ++i) {
            Path<T> item = ancestors.get(i);

            if (i != 0) {
                add(new Span("/"));
            }
            Button span = new Button();
            // span.setHref("");
            if (item.getSegments().size() == 0) {
                // span.add(VaadinIcon.HOME.create());
                span.setText("⌂");
            } else {
                T lastSegment = item.getFileName().toSegment();
                // Set<Node> resources = labelAssembler.extractResources(lastSegment);
                VaadinLabelMgr.forHasText(labelService, span, lastSegment, labelAssembler);
//                labelService.register(span, resources, (c, map) -> {
//
//                });
//
//                Node node = lastSegment.getNode();
//                // span.getElement().addEventListener("click", () -> {)
//                labelService.register(span, Collections.singleton(node), (c, map) ->
//                    c.setText(map.get(node) + (lastSegment.isForward() ? "»" : "«")));
            }
//            registrations.add(span.getElement().addEventListener("click", ev -> {
//                fireEvent(new BreadcrumbEvent(this, false, item));
//            }));
            registrations.add(span.addClickListener(ev -> {
                pathModel.set(item);
                fireEvent(new BreadcrumbEvent<>(this, false, item));
            }));
            add(span);
        }
    }

    public Registration addPathListener(ComponentEventListener<BreadcrumbEvent<T>> listener) {
        // ComponentEventListener<BreadcrumbEvent<?>> tmp = ev -> listener.onComponentEvent((BreadcrumbEvent<T>)ev);
        ComponentEventListener wrapper = ev -> listener.onComponentEvent((BreadcrumbEvent<T>)ev);
        return addListener(BreadcrumbEvent.class, wrapper);
    }

    public static LabelAssembler<FacetStep, Node, String> labelAssemblerForFacetPath() {
        return LabelAssembler.of(FacetStep::getNode, (segment, label) -> {
            return label + (segment.isForward() ? "»" : "«");
        });
    }


    public static LabelAssembler<P_Path0, Node, String> labelAssemblerForPath0() {
        return LabelAssembler.of(P_Path0::getNode, (segment, label) -> {
            return label + (segment.isForward() ? "»" : "«");
        });
    }

        // Function<PathPP, Node> pathToSegment = path -> path.getFileName().toSegment().getNode();
        // Function<PathPP, Node> pathToSegment = path -> path.getFileName().toSegment().getNode();
        // T lastSegment = item.getFileName().toSegment();
        // Set<Node> resources = labelAssembler.extractResources(lastSegment);
        // VaadinLabelMgr.forHasText(labelService, span, lastSegment, labelAssembler);
//        labelService.register(span, resources, (c, map) -> {
//
//        });
//
//        Node node = lastSegment.getNode();
//        // span.getElement().addEventListener("click", () -> {)
//        labelService.register(span, Collections.singleton(node), (c, map) ->
//            c.setText(map.get(node) + (lastSegment.isForward() ? "»" : "«")));

}
