package org.aksw.vaadin.app.demo.view.edit.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.aksw.commons.collection.observable.ObservableValue;
import org.aksw.commons.util.page.Page;
import org.aksw.commons.util.page.Paginator;
import org.aksw.commons.util.page.PaginatorImpl;
import org.aksw.jena_sparql_api.collection.observable.GraphChange;
import org.aksw.jenax.arq.util.node.PathUtils;
import org.aksw.jenax.arq.util.triple.TripleUtils;
import org.aksw.vaadin.app.demo.view.edit.resource.DataRetriever.ResourceInfo;
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

    protected ObservableValue<Set<Path>> visibleProperties;


    public ResourceItem(ResourceInfo state, GraphChange graphEditorModel, ObservableValue<Set<Path>> visibleProperties) {

        this.graphEditorModel = graphEditorModel;

        this.state = state;
        this.visibleProperties = visibleProperties;

        this.add(new H3("Resource: " + state.getNode()));

        visibleProperties.addValueChangeListener(values -> {
            refreshProperties();
        });

        refreshProperties();
    }

    public void refreshProperties() {
        // Set<Path> paths = visibleProperties.get();
        Set<Path> paths = state.getKnownPaths();

        System.out.println("Known paths: " + paths);

        for (Path path : paths) {
            VerticalLayout contentRow = new VerticalLayout();
            updateProperties(contentRow, path, 0, 5);
            add(contentRow);
        }
    }


    public void updateProperties(VerticalLayout contentRow, Path path, long offset, long limit) {
        Node p = PathUtils.asStep(path).getNode();
        Long itemCount = state.getCountForPath(path);

        contentRow.removeAll();


        HorizontalLayout headerRow = new HorizontalLayout();
        headerRow.add(new H4("" + p));

        Span countSpan = new Span("" + itemCount);
        countSpan.getElement().getThemeList().add("badge contrast");
        headerRow.add(countSpan);

        Paginator<Page> paginator = new PaginatorImpl(limit);
        List<Page> pages = paginator.createPages(itemCount, offset);

        for (Page page : pages) {
            // System.out.println("Page " + page.isActive() + " " + page.getPageOffset());
            Component pageCpt;

            if (!page.isActive()) {
                Button pageBtn = new Button("" + page.getPageNumber());
                pageBtn.addClickListener(ev -> {
                    updateProperties(contentRow, path, page.getPageOffset(), limit);
                });
                pageCpt = pageBtn;
            } else {
                pageCpt = new Span("" + page.getPageNumber());
            }

            headerRow.add(pageCpt);
        }

        contentRow.add(headerRow);

        List<Node> values = state.getData(path, Range.closedOpen(offset, offset + limit));
        for (Node o : values) {
//            RdfTermEditor termEditor = new RdfTermEditor();
//            termEditor.setValue(o);
//            contentRow.add(termEditor);
            Component editor = createEditor(state.getNode(), path, o);
            contentRow.add(editor);
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


        Button resetValueBtn = new Button(new Icon(VaadinIcon.ROTATE_LEFT));
        resetValueBtn.getElement().setProperty("title", "Reset this field to its original value");

        suffixComponents.add(resetValueBtn);

        Checkbox markAsDeleted = new Checkbox(false);
        markAsDeleted.getElement().setProperty("title", "Mark the original value as deleted");
        //target.addFormItem(markAsDeleted, "Delete");

        suffixComponents.add(markAsDeleted);

        ObservableValue<Boolean> isDeleted = graphEditorModel.getDeletionGraph().asSet()
                .filter(c -> c.equals(t))
                .mapToValue(c -> !c.isEmpty(), b -> b ? null : t);


        //newC.add(bind(markAsDeleted, isDeleted));


        // Red background for resources marked as deleted
        isDeleted.addValueChangeListener(ev -> {
            if (Boolean.TRUE.equals(ev.getNewValue())) {
                rdfTermEditor.setEnabled(false);
                r.getStyle().set("background-color", "var(--lumo-error-color-50pct)");
            } else {
                rdfTermEditor.setEnabled(true);
                r.getStyle().set("background-color", null);
            }
        });

        markAsDeleted.addValueChangeListener(event -> {
            Boolean state = event.getValue();
            if (Boolean.TRUE.equals(state)) {
                graphEditorModel.getDeletionGraph().add(t);
            } else {
                graphEditorModel.getDeletionGraph().delete(t);
            }
        });

        // graphEditorModel.getDeletionGraph().tr


        Node originalValue = value.get();
        resetValueBtn.setVisible(false);
        value.addValueChangeListener(ev -> {
            boolean newValueDiffersFromOriginal = !Objects.equals(originalValue, ev.getNewValue());

            resetValueBtn.setVisible(newValueDiffersFromOriginal);
            markAsDeleted.setVisible(!newValueDiffersFromOriginal);
        });
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
