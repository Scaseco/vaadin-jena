package org.aksw.jenax.vaadin.label;

import org.aksw.commons.rx.lookup.LookupService;
import org.apache.jena.graph.Node;

public class VaadinRdfLabelMgrImpl
    extends VaadinLabelMgr<Node, String>
    implements VaadinRdfLabelMgr
{
    public VaadinRdfLabelMgrImpl(LookupService<Node, String> lookupService) {
        super(lookupService);
    }
}
