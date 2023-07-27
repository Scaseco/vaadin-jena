package org.aksw.vaadin.app.demo.view.edit.resource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.aksw.commons.collection.observable.ObservableCollection;
import org.aksw.commons.collection.observable.ObservableValue;
import org.aksw.commons.util.page.Page;
import org.aksw.commons.util.page.Paginator;
import org.aksw.commons.util.page.PaginatorImpl;
import org.aksw.jena_sparql_api.collection.observable.GraphChange;
import org.aksw.jena_sparql_api.collection.observable.RdfField;
import org.aksw.jenax.arq.util.node.PathUtils;
import org.aksw.jenax.arq.util.triple.TripleUtils;
import org.aksw.jenax.path.core.PathPP;
import org.aksw.jenax.vaadin.label.VaadinLabelMgr;
import org.aksw.vaadin.common.bind.VaadinBindUtils;
import org.aksw.vaadin.component.rdf_term_editor.RdfTermEditor;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.path.Path;

import com.google.common.collect.Range;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class ResourceItem
    extends VerticalLayout
{
    protected ResourceInfo state;
    protected GraphChange graphEditorModel;

    protected VaadinLabelMgr<Node, String> labelService;

    protected ObservableValue<List<Path>> visibleProperties;



    // protected Map<Path, Component> attachedComponents = new LinedHashMap<>();

    // Path for which components have been attached
    protected Set<Path> activePaths = new LinkedHashSet<>();

    // Components that have been created and which may or may not be attached to this component
    protected Map<Path, Component> pathToComponentCache = new LinkedHashMap<>();


    protected ObservableValue<org.aksw.commons.path.core.Path<P_Path0>> breadcrumb;

    public ResourceItem(ResourceInfo state, GraphChange graphEditorModel,
            ObservableValue<List<Path>> visibleProperties,
            ObservableValue<org.aksw.commons.path.core.Path<P_Path0>> breadcrumb,
            VaadinLabelMgr<Node, String> labelService) {
        addClassName("card");

        this.labelService = labelService;
        this.graphEditorModel = graphEditorModel;

        this.state = state;
        this.visibleProperties = visibleProperties;

        this.breadcrumb = breadcrumb;


        this.add(new H3("Resource: " + state.getNode()));
        // Set<Path> paths = visibleProperties.get();
        // Set<Path> paths = state.getKnownPaths();

        // System.out.println("Known paths: " + paths);

        visibleProperties.addValueChangeListener(values -> {
            refreshPropertyList();
        });

        refreshPropertyList();
    }

    public void refreshPropertyList() {
        List<Path> requestedPaths = visibleProperties.get();

        Iterator<Path> itAttached = activePaths.iterator();

        outer: for (Path path : requestedPaths) {
            // Path nextAttachedPath = itAttached.hasNext() ? itAttached.next() : null;

            // Hide all components until there is one that equals path
            while (itAttached.hasNext()) {
                Path x = itAttached.next();

                if (path.equals(x)) {
                    continue outer;
                } else {
                    Component c = pathToComponentCache.get(x);
                    if (c != null) {
                        c.setVisible(false);
                    }

                }
            }

            // Add the new one
            Component c = pathToComponentCache.get(path);
            if (c != null) {
                c.setVisible(true);
            } else {
                VerticalLayout contentRow = new VerticalLayout();
                updatePropertyValues(contentRow, path, 0, 5);
                c = contentRow;
                pathToComponentCache.put(path, c);
            }
            this.add(c);
        }


        // Clear any remaining attached paths
        while (itAttached.hasNext()) {
            Path x = itAttached.next();
            Component c = pathToComponentCache.get(x);
            if (c != null) {
                c.setVisible(false);
                // this.remove(c);
            }
        }

        this.activePaths = new LinkedHashSet<>(requestedPaths);
    }


    public void updatePropertyValues(VerticalLayout contentRow, Path path, long offset, long limit) {
        P_Path0 step = PathUtils.asStep(path);
        Node p = step.getNode();
        boolean isFwd = step.isForward();
        Long itemCount = state.getCountForPath(path);

        if (itemCount == null) {
            System.out.println("null item count for " + path);
            itemCount = 0l;
        }

        contentRow.removeAll();


        HorizontalLayout headerRow = new HorizontalLayout();
        H4 pHeading = new H4("" + p);
        headerRow.add(labelService.forHasText(pHeading, p));

        ContextMenu contextMenu = new ContextMenu(pHeading);
        contextMenu.addItem("Navigate to " + p, ev -> {
            org.aksw.commons.path.core.Path<P_Path0> pp = breadcrumb.get();
            org.aksw.commons.path.core.Path<P_Path0> newPP = pp.resolve(step);
            breadcrumb.set(newPP);
        });

        Button addValueBtn = new Button(VaadinIcon.PLUS.create());
        RdfField rdfField = graphEditorModel.createSetField(state.getNode(), p, isFwd);
        ObservableCollection<Node> newValues = rdfField.getAddedAsSet();

        addValueBtn.addClickListener(event -> {
            // graphEditorModel.createSetField(p, p, isAttached());
            Node newNode = graphEditorModel.freshNode();
            newValues.add(newNode);
        });

        newValues.addPropertyChangeListener(ev -> {
            for (Node o : newValues) {
                Component editor = createEditor(state.getNode(), path, o);
                contentRow.add(editor);
            }
        });

        headerRow.add(addValueBtn);

        // contextMenu.



        if (itemCount != null && itemCount > 1) {

            Span countSpan = new Span("" + itemCount);
            countSpan.getElement().getThemeList().add("badge contrast");
            headerRow.add(countSpan);


            Paginator<Page> paginator = new PaginatorImpl(limit);
            List<Page> pages = paginator.createPages(itemCount, offset);


            for (Page page : pages) {
                // System.out.println("Page " + page.isActive() + " " + page.getPageOffset());
                Button pageBtn = new Button("" + page.getPageNumber());
                if (!page.isActive()) {
                    pageBtn.addClickListener(ev -> {
                        updatePropertyValues(contentRow, path, page.getPageOffset(), limit);
                    });
                } else {
                    pageBtn.setEnabled(false);
                }

                headerRow.add(pageBtn);
            }
        }

        contentRow.add(headerRow);


        List<Node> values = state.getData(path, Range.closedOpen(offset, offset + limit));
        if (values != null) {
            for (Node o : values) {
    //            RdfTermEditor termEditor = new RdfTermEditor();
    //            termEditor.setValue(o);
    //            contentRow.add(termEditor);
                Component editor = createEditor(state.getNode(), path, o);
                contentRow.add(editor);
            }
        }
    }


    public Component createEditor(Node srcNode, Path path, Node o) {

        HorizontalLayout r = new HorizontalLayout();
        r.setWidthFull();

        P_Path0 p0 = PathUtils.asStep(path);
        // List<Component> renameComponents = new ArrayList<>();
        List<Component> suffixComponents = new ArrayList<>();

        RdfTermEditor rdfTermEditor = new RdfTermEditor();

        Triple t = TripleUtils.create(srcNode, p0.getNode(), o, p0.isForward());
        int component = p0.isForward() ? 2 : 0;
        ObservableValue<Node> value = graphEditorModel.createFieldForExistingTriple(t, component);

        // boolean hasChanged = !Objects.equals(o, value.get());


        Button resetValueBtn = new Button(new Icon(VaadinIcon.ROTATE_LEFT));
        resetValueBtn.getElement().setProperty("title", "Reset this field to its original value");

        suffixComponents.add(resetValueBtn);

        Checkbox markAsDeleted = new Checkbox();
        markAsDeleted.getElement().setProperty("title", "Mark the original value as deleted");
        //target.addFormItem(markAsDeleted, "Delete");

        suffixComponents.add(markAsDeleted);

        ObservableValue<Boolean> isDeleted = graphEditorModel.getDeletionGraph().asSet()
                .filter(c -> c.equals(t))
                .mapToValue(c -> !c.isEmpty(), b -> b ? null : t);


        markAsDeleted.setValue(isDeleted.get());
        //newC.add(bind(markAsDeleted, isDeleted));


        // Red background for resources marked as deleted
        isDeleted.addValueChangeListener(ev -> {
            boolean deleted = Boolean.TRUE.equals(ev.getNewValue());
            if (deleted) {
                rdfTermEditor.setEnabled(false);
                r.getStyle().set("background-color", "var(--lumo-error-color-50pct)");
            } else {
                rdfTermEditor.setEnabled(true);
                r.getStyle().set("background-color", null);
            }
        }).fire();


        markAsDeleted.addValueChangeListener(event -> {
            Boolean state = event.getValue();
            if (Boolean.TRUE.equals(state)) {
                graphEditorModel.getDeletionGraph().add(t);
            } else {
                graphEditorModel.getDeletionGraph().delete(t);
            }
        });

        // graphEditorModel.getDeletionGraph().tr


        Node originalValue = o; // value.get();
        resetValueBtn.setVisible(false);


        value.addValueChangeListener(ev -> {
            boolean valueChanged = !Objects.equals(originalValue, ev.getNewValue());

            resetValueBtn.setVisible(valueChanged);
            markAsDeleted.setVisible(!valueChanged);

            if (valueChanged) {
                r.getStyle().set("background-color", "var(--lumo-primary-color-50pct)");
            } else {
                r.getStyle().set("background-color", null);
            }

        }).fire();
        VaadinBindUtils.bind(rdfTermEditor, value);


        resetValueBtn.addClickListener(ev -> {
            value.set(originalValue);
        });

        r.add(rdfTermEditor);
        r.setFlexGrow(1, rdfTermEditor);


        r.add(suffixComponents.toArray(new Component[0]));

        return r;
    }
}
