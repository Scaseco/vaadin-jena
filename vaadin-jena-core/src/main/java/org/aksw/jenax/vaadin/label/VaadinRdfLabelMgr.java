package org.aksw.jenax.vaadin.label;

import org.apache.jena.graph.Node;

import com.vaadin.flow.component.HasText;

public interface VaadinRdfLabelMgr
    extends LabelService<Node, String>
{
    <X extends HasText> X forHasText(X component, Node resource);
}
