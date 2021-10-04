package org.aksw.vaadin.datashape.provider;

import org.apache.jena.graph.Node;
import org.apache.jena.shared.PrefixMapping;

import com.vaadin.flow.data.provider.DataProvider;


public class DataProviders {
    public DataProvider<Node, String> forNamespaces(PrefixMapping prefixes) {
        return new DataProviderForPrefixBasedTypeAhead(prefixes);
    }
}
