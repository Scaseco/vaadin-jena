package org.aksw.vaadin.datashape.provider;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.collection.observable.CollectionChangedEvent;
import org.aksw.commons.collection.observable.ObservableCollection;
import org.aksw.commons.collection.observable.ObservableValue;
import org.aksw.commons.path.core.Path;
import org.aksw.jena_sparql_api.collection.observable.GraphChange;
import org.aksw.jena_sparql_api.collection.observable.ObservableGraph;
import org.aksw.jena_sparql_api.collection.observable.ObservableGraphImpl;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.lookup.MapService;
import org.aksw.jena_sparql_api.path.core.PathNode;
import org.aksw.jena_sparql_api.path.core.PathOpsNode;
import org.aksw.jena_sparql_api.path.datatype.RDFDatatypePPath;
import org.aksw.jena_sparql_api.path.datatype.RDFDatatypePathNode;
import org.aksw.jena_sparql_api.rdf.collections.NodeMapper;
import org.aksw.jena_sparql_api.rdf.collections.NodeMappers;
import org.aksw.jena_sparql_api.schema.ShapedNode;
import org.aksw.jena_sparql_api.schema.ShapedProperty;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.path.P_Path0;

import com.google.common.base.Converter;
import com.google.common.collect.Range;
import com.google.common.primitives.Ints;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;

public class HierarchicalDataProviderForShacl
    extends AbstractBackEndHierarchicalDataProvider<Path<Node>, String>
{
    private static final long serialVersionUID = 1L;


    /** A node that is returned as a child if the parent's filter condition yields no matches */
    public static final Node NO_MATCH = NodeFactory.createBlankNode("NO_MATCH");

    /** A node to indicate that a result set was cut off; typically due to a limit */
    public static final Node MORE_MATCHES = NodeFactory.createBlankNode("MORE_MATCHES");

    /**
     * Track filter and pagination for tree grid nodes
     *
     * @author raven
     *
     */
    public static class NodeState {
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
        public static Runnable adept(ObservableGraph observableGraph, Consumer<Set<PathNode>> listener) {
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

        public Runnable addPathListener(Consumer<Set<PathNode>> listener) {
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


    // protected ShapedNode rootNode;
    protected MapService<Concept, Node, ShapedNode> root;
    // protected Set<NodeSchema> rootSchemas;
    //protected SparqlQueryConnection conn;
    protected boolean showEmptyProperties = true;


    protected GraphChange graphEditorModel;

    protected NodeState nodeState;

//    public HierarchicalDataProviderForShacl(MapService<Concept, Node, ShapedNode> root) {
//        this(root, null);
//    }

    public HierarchicalDataProviderForShacl(MapService<Concept, Node, ShapedNode> root, GraphChange graphEditorModel) {
        super();
        this.root = root;
        this.graphEditorModel = graphEditorModel;
        this.nodeState = new NodeState();


        nodeState.addPathListener(paths -> {
            for (PathNode path : paths) {
                System.out.println("Refreshing " + path);
                this.refreshItem(path, true);
            }
//            Object o = ev.getNewValue();
//            System.out.println("NEW VALUE: " + o);
//            this.refreshAll();
        });
    }

    public NodeState getNodeState() {
        return nodeState;
    }


    @Override
    public int getChildCount(HierarchicalQuery<Path<Node>, String> query) {
        int result = Ints.saturatedCast(fetchChildrenFromBackEnd(query).count());
        return result;
    }


    @Override
    public boolean hasChildren(Path<Node> item) {
        HierarchicalQuery<Path<Node>, String> hq = new HierarchicalQuery<>(0, 1, Collections.emptyList(), null, null, item);

        boolean result = fetchChildrenFromBackEnd(hq, false).findAny().isPresent();
        return result;
    }


    @Override
    protected Stream<Path<Node>> fetchChildrenFromBackEnd(HierarchicalQuery<Path<Node>, String> query) {
        return fetchChildrenFromBackEnd(query, false);
    }

    protected Stream<Path<Node>> fetchChildrenFromBackEnd(HierarchicalQuery<Path<Node>, String> query, boolean excludeFilter) {


        Range<Long> range = Range.closedOpen((long)query.getOffset(), (long)(query.getOffset() + query.getLimit()));

        Stream<Path<Node>> result = null;

        Path<Node> basePath = query.getParent();


        Node lastSegment = basePath != null && basePath.getNameCount() > 0
                ? basePath.getFileName().toSegment()
                : null;

        if (NO_MATCH.equals(lastSegment) || MORE_MATCHES.equals(lastSegment)) {
            return Stream.empty();
        }

        MapService<Concept, Node, ShapedNode> current = root;


        // Resolve the node shape for the final resource in the path

        // Remember the last node and path
        Node s = null;
        org.apache.jena.sparql.path.Path path = null;

        int n = basePath == null ? 0 : basePath.getNameCount();
        for (int i = 0; i < n; i +=2) {
            s = basePath.getName(i).toSegment();

            ShapedNode sn = current.createPaginator(ConceptUtils.createConcept(s)).fetchMap().get(s);

            if (sn != null) {
                Map<org.apache.jena.sparql.path.Path, ShapedProperty> map = sn.getShapedProperties();
                if (i + 1 < n) {
                    Node p = basePath.getName(i + 1).toSegment();

                    if (p.isLiteral() && p.getLiteralValue() instanceof org.apache.jena.sparql.path.Path) {
                        path = (org.apache.jena.sparql.path.Path)p.getLiteralValue();

                        ShapedProperty sp = map.get(path);

                        current = sp.getValues();

                        result = current.createPaginator(null).fetchMap()
                                .values().stream().map(ShapedNode::getSourceNode).map(basePath::resolve);


                    }

                } else {

                    result = map.entrySet().stream()
                                .filter(e -> showEmptyProperties || !e.getValue().isEmpty()) // Filter out empty properties
                                .map(Entry::getKey)
                                .map(p -> NodeFactory.createLiteralByValue(p, RDFDatatypePPath.INSTANCE))
                                .map(basePath::resolve);

                }
            } else {
                current = null;
                result = Stream.empty();
                break;
            }
        }


        Collection<Path<Node>> addedPaths = Collections.emptyList();
        boolean isPropertyPath = n % 2 == 0;
        if (isPropertyPath && path != null) {

            P_Path0 p0 = (P_Path0)path;

            // RdfField rdfField =
            ObservableCollection<Node> addedValues = graphEditorModel.getAdditionGraph().createSetField(s, p0.getNode(), p0.isForward());

            // ObservableCollection<Node> existingValues = rdfField.getBaseAsSet();
            //ObservableCollection<Node> addedValues = rdfField.getAddedAsSet();

            addedPaths = addedValues.stream().map(basePath::resolve).collect(Collectors.toList());


            // System.out.println("Added paths: " + addedPaths);
            int m = addedValues.size();


        }


        if (result == null) {
            result = root.fetchData(null).values().stream()
                    .map(sn -> PathOpsNode.newAbsolutePath().resolve(sn.getSourceNode()));
            // result = Stream.empty();
        }

        List<Path<Node>> tmp = result.collect(Collectors.toList());
        tmp = Stream.concat(addedPaths.stream(), tmp.stream()).collect(Collectors.toList());

        if (!excludeFilter && !tmp.isEmpty()) {
            String filter = nodeState.getFilter(basePath, null).get();
            System.out.println("Got filter: " + basePath + ": " + filter);
            if (filter != null && !filter.isBlank()) {
                tmp = tmp.stream().filter(p -> p.getFileName().toString().contains(filter)).collect(Collectors.toList());

                if (tmp.isEmpty()) {
                    tmp = Collections.singletonList(basePath.resolve(NO_MATCH));
                }
            }
            System.out.println("Filtered to " + tmp.stream().map(Object::toString).collect(Collectors.joining("\n")));
        }


        Long itemsPerPage = nodeState.getItemsPerPage(basePath, null).get();
        long itemsBefore = tmp.size();
        if (itemsPerPage != null) {
            tmp = tmp.stream().limit(itemsPerPage).collect(Collectors.toList());
            long itemsAfter = tmp.size();

            if (itemsBefore != itemsAfter) {
                tmp.add(basePath.resolve(MORE_MATCHES));
            }
        }


        // System.out.println("Data provider for path " + basePath + ": " + tmp);
        return tmp.stream();
    }


}
