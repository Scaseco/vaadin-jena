package org.aksw.vaadin.app.demo.view.edit.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.commons.cache.plain.ClaimingCache;
import org.aksw.commons.collections.PolaritySet;
import org.aksw.commons.collector.core.AggBuilder;
import org.aksw.commons.collector.domain.Accumulator;
import org.aksw.commons.io.cache.AdvancedRangeCacheImpl;
import org.aksw.commons.rx.lookup.LookupService;
import org.aksw.jena_sparql_api.lookup.LookupServiceSparqlQuery;
import org.aksw.jenax.arq.util.expr.ExprUtils;
import org.aksw.jenax.arq.util.node.PathUtils;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.arq.util.triple.TripleUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.dataaccess.sparql.exec.query.QueryExecFactories;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RdfDataEngineFromDataset;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactoryOverSparqlQueryConnection;
import org.aksw.jenax.stmt.core.SparqlStmtMgr;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.syntax.ElementFilter;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

public class DataRetriever {

    protected RdfDataSource rdfDataSource;
    // protected UnaryRelation sourceConcept;
    protected Query metaModelQuery = SparqlStmtMgr.loadQuery("resource-metamodel.rq");

    // protected PolaritySet<Path> paths;

    // protected Table<Node, Node, Slice<Node[]>>
    // protected Path<Node> basePath;
    protected ClaimingCache<org.aksw.commons.path.core.Path<Node>, AdvancedRangeCacheImpl<Node[]>> cache;

    // Accumulator for paths - simple predicates are placed into fwd/bwd containers



    public static void main(String[] args) {

        Set<Node> nodes = new LinkedHashSet<>(Arrays.asList(NodeFactory.createURI("http://dcat.linkedgeodata.org/dataset/osm-bremen-2018-04-04")));

        Dataset dataset = RDFDataMgr.loadDataset("linkedgeodata-2018-04-04.dcat.ttl");
        RdfDataSource rdfDataSource = RdfDataEngineFromDataset.create(dataset, false);

        DataRetriever retriever = new DataRetriever(rdfDataSource);

        PolaritySet<Path> baseFilter = PolaritySet.create(false);
        Map<Node, ResourceInfo> map = retriever.fetch(nodes, baseFilter);

        // map.values().iterator().next().
    }


    public DataRetriever(RdfDataSource rdfDataSource) {
        this.rdfDataSource = rdfDataSource;
    }

    public Map<Node, ResourceInfo> fetch(Set<Node> nodes, PolaritySet<Path> basePathFilter) {

        Map<Node, ResourceInfo> nodeToState = new HashMap<>();

        try (SparqlQueryConnection conn = rdfDataSource.getConnection()) {
            // SparqlQueryConnection conn = null;

            LookupService<Node, Table> ls = new LookupServiceSparqlQuery(
                    new QueryExecutionFactoryOverSparqlQueryConnection(conn), metaModelQuery, Var.alloc("src"));
            Map<Node, Table> map = ls.fetchMap(nodes);

            for (Entry<Node, Table> e : map.entrySet()) {
                Node s = e.getKey();

                ResourceInfo state = nodeToState.computeIfAbsent(s, key -> new ResourceInfo(s, rdfDataSource));

                ResultSet rs = ResultSet.adapt(e.getValue().toRowSet());
                while (rs.hasNext()) {
                    QuerySolution qs = rs.next();
                    System.out.println(qs);
                    // Node src = qs.get("src").asNode();
                    Node p = qs.get("p").asNode();
                    boolean isFwd = qs.get("isFwd").asLiteral().getBoolean();
                    long valueCount = qs.get("vc").asLiteral().getLong();

                    state.set(p, isFwd, valueCount);
                }
            }
        }



        // do not batch-fetch properties having more than threshold values
        long threshold = 20;


        // Map of path to the resources where they are black listed
        // Blacklisted paths match the filter criteria
        Map<Node, PolaritySet<Path>> srcToPaths = new HashMap<>();


//        Set<Path> allKnownPaths = new HashSet<>();
//        PathAcc pathAcc = new PathAcc();

        for (Node node : nodes) {
            ResourceInfo state = nodeToState.get(node);

            // Set<Path> knownPaths = state.getKnownPaths();
            PolaritySet<Path> knownPaths = new PolaritySet<>(true, state.getKnownPaths());
            // allKnownPaths.addAll(knownPaths.getValue());

            PolaritySet<Path> matchingPaths = knownPaths.intersect(basePathFilter);

            Set<Path> blacklistPaths = matchingPaths.getValue().stream()
                    .filter(item ->  state.getCountForPath(item) > threshold)
                    .collect(Collectors.toSet());

            if (blacklistPaths.isEmpty()) {
                srcToPaths.put(node, basePathFilter);
            } else {
                PolaritySet<Path> exceptions = new PolaritySet<>(false, blacklistPaths);
                srcToPaths.put(node, basePathFilter.intersect(exceptions));
            }
        }

        Multimap<PolaritySet<Path>, Node> inverted = HashMultimap.create();
        srcToPaths.forEach((k, v) -> inverted.put(v, k));

        System.out.println(inverted);
        Multimap<PolaritySet<Node>, Node> srcToFwdPaths = index(inverted, true);

        Element fwdElt = createSimplePattern(nodes, true, srcToFwdPaths, null);

        System.out.println(fwdElt);

        Query query = new Query();
        query.setQuerySelectType();
        query.setQueryPattern(fwdElt);
        query.addOrderBy(Vars.s, Query.ORDER_DESCENDING);
        query.addOrderBy(Vars.d, Query.ORDER_DESCENDING);
        query.addOrderBy(Vars.p, Query.ORDER_DESCENDING);
        query.addOrderBy(Vars.o, Query.ORDER_DESCENDING);

        Accumulator<Binding, ?, Map<Node, Map<Path, List<Node>>>> acc =
            AggBuilder.inputSplit((Binding b) -> b.get(Vars.s),
                AggBuilder.inputSplit((Binding b) -> (Path)PathUtils.createStep(b.get(Vars.p), !NodeValue.FALSE.asNode().equals(b.get(Vars.d))),
                    AggBuilder.inputTransform((Binding b) -> b.get(Vars.o),
                        AggBuilder.arrayListSupplier()))).createAccumulator();

        try (SparqlQueryConnection conn = rdfDataSource.getConnection()) {
            try (QueryExecution qe = conn.query(query)) {
                RowSet rs = RowSet.adapt(qe.execSelect());
                while (rs.hasNext()) {
                    Binding b = rs.next();
                    acc.accumulate(b);
                }
            }
        }

        Map<Node, Map<Path, List<Node>>> map = acc.getValue();

        for (Entry<Node, Map<Path, List<Node>>> se : map.entrySet()) {
            Node s = se.getKey();
            ResourceInfo ri = nodeToState.get(s);
            for (Entry<Path, List<Node>> pe : se.getValue().entrySet()) {
                Path path = pe.getKey();
                Node[] os = pe.getValue().toArray(new Node[0]);

                ri.putData(path, os);
            }
        }


        return nodeToState;
    }


    public static Multimap<PolaritySet<Node>, Node> index(Multimap<PolaritySet<Path>, Node> map, boolean isFwd) {
        Multimap<PolaritySet<Node>, Node> result = HashMultimap.create();
        for (Entry<PolaritySet<Path>, Collection<Node>> en : map.asMap().entrySet()) {
            Set<Node> predicates = en.getKey().getValue().stream()
                    .map(PathUtils::asStep)
                    .filter(Objects::nonNull)
                    .filter(p -> p.isForward() == isFwd)
                    .map(P_Path0::getNode)
                    .collect(Collectors.toSet());
            PolaritySet<Node> pset = new PolaritySet<>(en.getKey().isPositive(), predicates);
            result.putAll(pset, en.getValue());
        }

        return result;
    }

    public static class Factorization<K, V> {
        protected Set<V> commonFeatures;
        protected Multimap<K, V> remainingFeatures;
        public Factorization(Set<V> commonFeatures, Multimap<K, V> remainingFeatures) {
            super();
            this.commonFeatures = commonFeatures;
            this.remainingFeatures = remainingFeatures;
        }

        public Set<V> getCommonFeatures() {
            return commonFeatures;
        }

        public Multimap<K, V> getRemainingFeatures() {
            return remainingFeatures;
        }
    }

    public static <K, V> Factorization<K, V> factorize(SetMultimap<K, V> resToFeatures) {
        Set<V> allFeatures = resToFeatures.values().stream().collect(Collectors.toSet());
        Set<V> commonFeatures = new HashSet<>(allFeatures);

        resToFeatures.asMap().values().forEach(set -> commonFeatures.retainAll(set));

        Multimap<K, V> remaining = HashMultimap.create();
        for (Entry<K, Collection<V>> entry : resToFeatures.asMap().entrySet()) {
            remaining.putAll(entry.getKey(), Sets.difference((Set<V>)entry.getValue(), commonFeatures));
        }

        return new Factorization<>(commonFeatures, remaining);
    }

    public static <K, V> void greedyCover(Multimap<K, V> resToFeatures) {
        // Check if there is any set of features common to all resources
        Set<V> allFeatures = resToFeatures.values().stream().collect(Collectors.toSet());
        Set<V> commonFeatures = new HashSet<>(allFeatures);

        resToFeatures.asMap().values().forEach(set -> commonFeatures.retainAll(set));

        // Invert featureToRes
        Multimap<V, K> inverted = Multimaps.invertFrom(resToFeatures, HashMultimap.create());


        return;
    }

    // Group all properties having the same set of resources
    public static Element createSimplePattern(
            Set<Node> srcs,
            boolean isFwd,
            // PolaritySet<Node> predicates,
            // Multimap<Node, Node> srcToExceptions,
            Multimap<PolaritySet<Node>, Node> predicatesToSrcs,
            Long limit) {


        List<Expr> disjunctionMembers = new ArrayList<>();
        for (Entry<PolaritySet<Node>, Collection<Node>> pToS : predicatesToSrcs.asMap().entrySet()) {
            PolaritySet<Node> tmp = pToS.getKey();
            Set<Node> ps = tmp.getValue();
            boolean isPositive = tmp.isPositive();

            List<Expr> pe;
            if (isPositive) {
                if (ps.isEmpty()) {
                    continue;
                } else {
                    pe = Arrays.asList(ExprUtils.oneOf(Vars.p, ps));
                }
            } else {
                if (ps.isEmpty()) {
                    pe = Collections.emptyList();
                } else {
                    pe = Arrays.asList(ExprUtils.notOneOf(Vars.p, ps));
                }
            }

            Expr e = ExprUtils.andifyBalanced(Iterables.concat(
                    Collections.singleton(ExprUtils.oneOf(Vars.s, pToS.getValue())),
                    pe));

            disjunctionMembers.add(e);
        }

        Expr expr = ExprUtils.orifyBalanced(disjunctionMembers);
        Triple t = TripleUtils.create(Vars.s, Vars.p, Vars.o, isFwd);

        Element result = ElementUtils.groupIfNeeded(
                ElementUtils.createElement(t),
                new ElementBind(Vars.d, NodeValue.booleanReturn(isFwd)),
                new ElementFilter(expr));

        return result;
    }


    /** */
    public Query createQuery(PathAcc pathAcc) {



        return null;
    }

}
