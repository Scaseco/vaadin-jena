package org.aksw.jena_sparql_api.vaadin.util;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.jena.jgrapht.PseudoGraphJenaGraph;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ManyToManyShortestPathsAlgorithm.ManyToManyShortestPaths;
import org.jgrapht.alg.shortestpath.DijkstraManyToManyShortestPaths;
import org.jgrapht.graph.AsUndirectedGraph;

public class PredicateTreeSearch {
    public static final String joinsWithTerm = "http://www.example.org/joinsWith";
    public static final Node joinsWithNode = NodeFactory.createURI(joinsWithTerm);
    public static final Property joinsWith = ResourceFactory.createProperty(joinsWithTerm);

    public static Stream<GraphPath<Node, Triple>> find(Set<Node> startNodes, Graph predicateJoinSummary, Predicate<String> matcher) {
        Set<Node> targetNodes = predicateJoinSummary.stream(Node.ANY, joinsWithNode, Node.ANY)
                .flatMap(t -> Stream.of(t.getSubject(), t.getObject()))
                .collect(Collectors.toSet());

        return find(startNodes, targetNodes, predicateJoinSummary);
    }

    public static Stream<GraphPath<Node, Triple>> find(Set<Node> startNodes, Set<Node> targetNodes, Graph predicateJoinSummary) {
        org.jgrapht.Graph<Node, Triple> g = new AsUndirectedGraph<>(new PseudoGraphJenaGraph(predicateJoinSummary, joinsWithNode));

        DijkstraManyToManyShortestPaths<Node, Triple> algo = new DijkstraManyToManyShortestPaths<>(g);
        ManyToManyShortestPaths<Node, Triple> paths = algo.getManyToManyPaths(startNodes, targetNodes);

        Stream<GraphPath<Node, Triple>> result = startNodes.stream().flatMap(startNode ->
            targetNodes.stream().flatMap(targetNode ->
                Optional.ofNullable(paths.getPath(startNode, targetNode)).stream()));

        return result;
    }

    public static void main(String[] args) {
        Graph joinSummaryGraph = RDFDataMgr.loadGraph("/home/raven/Datasets/pmd/4Kristain/join-summary.ttl");
        List<GraphPath<Node, Triple>> paths = find(
                Set.of(NodeFactory.createURI("https://w3id.org/pmd/co/executes")),
                Set.of(NodeFactory.createURI("https://w3id.org/pmd/co/value")),
                joinSummaryGraph).collect(Collectors.toList());
        paths.forEach(System.out::println);

    }
}
