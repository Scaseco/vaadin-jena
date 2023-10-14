package org.aksw.vaadin.datashape.provider;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.collection.observable.ObservableCollection;
import org.aksw.commons.path.core.Path;
import org.aksw.commons.rx.lookup.ListService;
import org.aksw.commons.rx.lookup.ListServiceFromList;
import org.aksw.commons.rx.lookup.MapService;
import org.aksw.commons.rx.lookup.MapServiceFromListService;
import org.aksw.jena_sparql_api.collection.observable.GraphChange;
import org.aksw.jena_sparql_api.rdf.collections.NodeMappers;
import org.aksw.jena_sparql_api.schema.ShapedNode;
import org.aksw.jena_sparql_api.schema.ShapedProperty;
import org.aksw.jenax.path.core.PathNode;
import org.aksw.jenax.path.datatype.RDFDatatypePPath;
import org.aksw.jenax.sparql.fragment.impl.Concept;
import org.aksw.jenax.sparql.fragment.impl.ConceptUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_Path0;

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
                        if (sp == null) {
                            ListService<Concept, ShapedNode> ls = new ListServiceFromList<>(Collections.emptyList(), (k, v) -> true);
                            MapServiceFromListService<Concept, ShapedNode, Node, ShapedNode> ms = new MapServiceFromListService<>(ls, ShapedNode::getSourceNode, x -> x);

                            current = ms;
                        } else {
                            current = sp.getValues();
                        }

                        result = current.createPaginator(null).fetchMap()
                                .values().stream().map(ShapedNode::getSourceNode).map(basePath::resolve);


                    }

                } else {

                    ObservableCollection<String> str = nodeState.getAdhocProperties(basePath);
                    System.out.println("ADHOC: " + str);

                    Collection<Node> nodes = str.convert(NodeMappers.uriString.asConverter().reverse());
                    Stream<org.apache.jena.sparql.path.Path> partA = nodes.stream().map(P_Link::new);


                    Stream<org.apache.jena.sparql.path.Path> partB = map.entrySet().stream()
                                .filter(e -> showEmptyProperties || !e.getValue().isEmpty()) // Filter out empty properties
                                .map(Entry::getKey);


                    result = Stream.concat(partA, partB)
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
                    .map(sn -> PathNode.newAbsolutePath().resolve(sn.getSourceNode()));
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
