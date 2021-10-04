package org.aksw.vaadin.datashape.provider;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.vaadin.common.provider.util.DataProviderWithConversion;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.shared.PrefixMapping;

import com.google.common.primitives.Ints;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;


public class DataProviderForPrefixBasedTypeAhead
    extends AbstractBackEndDataProvider<Node, String>
{
    private static final long serialVersionUID = 1L;

    protected PrefixMapping prefixes;

    public DataProviderForPrefixBasedTypeAhead(PrefixMapping prefixes) {
        super();
        this.prefixes = prefixes;
    }

    @Override
    protected Stream<Node> fetchFromBackEnd(Query<Node, String> query) {
        String filter = query.getFilter()
                .map(x -> x.isEmpty() ? "" : x).orElse("").toLowerCase();

        String[] parts = filter.split(":", 2);

        List<String> matches;
        if (parts.length == 1) {
            matches = prefixes.getNsPrefixMap().keySet().stream()
                .filter(ns -> ns.toLowerCase().contains(parts[0]))
                .collect(Collectors.toList());
        } else {
            matches = Collections.singletonList(prefixes.expandPrefix(filter));
        }

        List<Node> tmp = matches.stream().map(NodeFactory::createURI).collect(Collectors.toList());

        return tmp.stream();
    }

    @Override
    protected int sizeInBackEnd(Query<Node, String> query) {
        int result = Ints.saturatedCast(fetchFromBackEnd(query).count());
        return result;
    }

    public DataProvider<String, String> forString() {
        return DataProviderWithConversion.wrap(this, Node::getURI, NodeFactory::createURI);
    }

};
