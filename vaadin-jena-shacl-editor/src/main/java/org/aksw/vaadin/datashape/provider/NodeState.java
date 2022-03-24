package org.aksw.vaadin.datashape.provider;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.aksw.commons.collection.observable.CollectionChangedEvent;
import org.aksw.commons.collection.observable.ObservableCollection;
import org.aksw.commons.collection.observable.ObservableValue;
import org.aksw.commons.collection.observable.Registration;
import org.aksw.commons.path.core.Path;
import org.aksw.jena_sparql_api.collection.observable.ObservableGraph;
import org.aksw.jena_sparql_api.collection.observable.ObservableGraphImpl;
import org.aksw.jena_sparql_api.rdf.collections.NodeMapper;
import org.aksw.jena_sparql_api.rdf.collections.NodeMappers;
import org.aksw.jenax.path.core.PathNode;
import org.aksw.jenax.path.core.PathOpsNode;
import org.aksw.jenax.path.datatype.RDFDatatypePathNode;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.graph.GraphFactory;

import com.google.common.base.Converter;

/**
 * Track filter and pagination for tree grid nodes
 *
 * @author raven
 *
 */
public class NodeState {
    public static final Node FILTER = NodeFactory.createURI("urn:filter");
    public static final Node ITEMS_PER_PAGE = NodeFactory.createURI("urn:items_per_page");
    public static final Node ADHOC_PROPERTIES = NodeFactory.createURI("urn:adhoc-properties");

    protected Graph graph = GraphFactory.createGraphMem();
    protected ObservableGraph state = new ObservableGraphImpl(graph);

    public <S> ObservableCollection<S> getSetValue(Path<Node> path, NodeMapper<S> nodeMapper, Node predicate) {
        Node s = RDFDatatypePathNode.createNode(path);
        Converter<Node, S> converter = nodeMapper.asConverter();
        ObservableCollection<S> result = state.createSetField(s, predicate, true).convert(converter);
        return result;
    }

    public ObservableCollection<String> getAdhocProperties(Path<Node> path) {
        return getSetValue(path, NodeMappers.string, ADHOC_PROPERTIES);
    }


    public <S> ObservableValue<S> getValue(Path<Node> path, NodeMapper<S> nodeMapper, Node predicate, S initialValue) {
        if (path == null) {
            path = PathOpsNode.newAbsolutePath();
        }
        Node s = RDFDatatypePathNode.createNode(path);

        ObservableValue<S> result = state.createValueField(s, predicate, true).convert(nodeMapper.asConverter());

        if (result.get() == null && initialValue != null) {
            Node o = nodeMapper.toNode(initialValue);
            graph.add(s, FILTER, o);
        }

        return result;
    }

    public ObservableValue<String> getString(Path<Node> path, Node predicate, String initialValue) {
        return getValue(path, NodeMappers.string, predicate, initialValue);
    }

    public ObservableValue<Long> getLong(Path<Node> path, Node predicate, Long initialValue) {
        return getValue(path, NodeMappers.xlong, predicate, initialValue);
    }

    public ObservableValue<String> getFilter(Path<Node> path, String initialValue) {
        return getString(path, FILTER, initialValue);
    }

    public ObservableValue<Long> getItemsPerPage(Path<Node> path, Long initialValue) {
        return getLong(path, ITEMS_PER_PAGE, initialValue);
    }

    /** Connect a listener for affected paths to the events of an observable graph */
    public static Registration adept(ObservableGraph observableGraph, Consumer<Set<PathNode>> listener) {
        return observableGraph.addPropertyChangeListener(ev -> {
            CollectionChangedEvent<Triple> e = (CollectionChangedEvent<Triple>)ev;
            Set<Node> modified = new LinkedHashSet<>();
            e.getAdditions().stream().map(Triple::getSubject).forEach(modified::add);
            e.getDeletions().stream().map(Triple::getSubject).forEach(modified::add);

            Set<PathNode> tmp = modified.stream()
                    .map(RDFDatatypePathNode::extractPath)
                    // .map(p -> PathOpsNode.newAbsolutePath().equals(p) ? null : p)
                    .collect(Collectors.toSet());
            listener.accept(tmp);
        });
    }

    public Registration addPathListener(Consumer<Set<PathNode>> listener) {
        return state.addPropertyChangeListener(ev -> {
            CollectionChangedEvent<Triple> e = (CollectionChangedEvent<Triple>)ev;
            Set<Node> modified = new LinkedHashSet<>();
            e.getAdditions().stream().map(Triple::getSubject).forEach(modified::add);
            e.getDeletions().stream().map(Triple::getSubject).forEach(modified::add);

            Set<PathNode> tmp = modified.stream()
                    .map(RDFDatatypePathNode::extractPath)
                    .map(p -> PathOpsNode.newAbsolutePath().equals(p) ? null : p)
                    .collect(Collectors.toSet());
            listener.accept(tmp);
        });
    }


    public ObservableGraph getState() {
        return state;
    }

    // public void put(Path<Node>,)
    // protected ObservableMap<Path<Node>, >
}