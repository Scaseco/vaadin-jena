package org.aksw.vaadin.app.demo.view.edit.resource;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.commons.io.buffer.array.ArrayOps;
import org.aksw.commons.io.cache.AdvancedRangeCacheConfig;
import org.aksw.commons.io.cache.AdvancedRangeCacheConfigImpl;
import org.aksw.commons.io.cache.AdvancedRangeCacheImpl;
import org.aksw.commons.io.input.ReadableChannel;
import org.aksw.commons.io.input.ReadableChannelSource;
import org.aksw.commons.io.input.ReadableChannelSources;
import org.aksw.commons.io.input.ReadableChannels;
import org.aksw.commons.io.slice.Slice;
import org.aksw.commons.io.slice.SliceAccessor;
import org.aksw.commons.io.slice.SliceInMemoryCache;
import org.aksw.commons.rx.io.ReadableChannelSourceRx;
import org.aksw.commons.rx.lookup.ListPaginator;
import org.aksw.jena_sparql_api.lookup.ListPaginatorSparql;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.dataaccess.sparql.execution.query.QueryExecutionDecoratorBase;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactoryQuery;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_ReverseLink;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.syntax.Element;

import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Iterables;
import com.google.common.collect.Range;

public class ResourceInfo {
        protected Node src;
        protected RdfDataSource rdfDataSource;
        // protected Map<Path, Long> pathToCount = new LinkedHashMap<>();

        protected Map<Path, ReadableChannelSource<Node[]>> pathToValues = new LinkedHashMap<>();


        public ResourceInfo(Node src, RdfDataSource rdfDataSource) {
            this.src = Objects.requireNonNull(src);
            this.rdfDataSource = Objects.requireNonNull(rdfDataSource);
        }

        public Node getNode() {
            return src;
        }

        public List<Node> getData(Path path, Range<Long> range) {
            ReadableChannelSource<Node[]> cache = pathToValues.get(path);
            List<Node> result = null;
            if (cache != null) {
                try (ReadableChannel<Node[]> dataStream = cache.newReadableChannel(range)) {
                    result = ReadableChannels.newStream(dataStream).collect(Collectors.toList());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return result;
        }

        public ReadableChannelSource<Node[]> setup(Path path) {
            ReadableChannelSource<Node[]> cache = pathToValues.computeIfAbsent(path, p -> {

                TriplePath t = new TriplePath(src, p, Vars.o);
                Element elt = ElementUtils.createElement(t);
                Query query = new Query();
                query.setQuerySelectType();
                query.addResultVar(Vars.o);
                query.setQueryPattern(elt);
                query.addOrderBy(Vars.o, Query.ORDER_DESCENDING);

                QueryExecutionFactoryQuery qef = q -> {
                    RDFConnection conn = rdfDataSource.getConnection();
                    return new QueryExecutionDecoratorBase<QueryExecution>(conn.query(q)) {
                        @Override
                        public void close() {
                            try {
                                super.close();
                            } finally {
                                conn.close();
                            }
                        }
                    };
                };

                ListPaginator<Node> paginator = new ListPaginatorSparql(query, qef)
                        .map(binding -> binding.get(Vars.o));

                ReadableChannelSourceRx<Node> source = new ReadableChannelSourceRx<>(ArrayOps.createFor(Node.class), paginator);

                AdvancedRangeCacheConfig arcc = AdvancedRangeCacheConfigImpl.newDefaultsForObjects(10000);
                Slice<Node[]> slice = SliceInMemoryCache.create(ArrayOps.createFor(Node.class), 50000, 20);
                ReadableChannelSource<Node[]> dss = ReadableChannelSources.cache(source, slice, arcc);
                return dss;
            });

            return cache;
        }

        public void set(Node p, boolean isFwd, long valueCount) {
            Path path = isFwd
                    ? new P_Link(p)
                    : new P_ReverseLink(p);

//            pathToCount.put(path, valueCount);
             setKnownSize(path, valueCount);
        }

        public void setKnownSize(Path path, Long knownSize) {
            ReadableChannelSource<Node[]> cache = setup(path);

            System.err.println("Set known size for " + path + " to " + knownSize);
            AdvancedRangeCacheImpl<Node[]> arc = (AdvancedRangeCacheImpl<Node[]>)cache;
            Slice<Node[]> slice = arc.getSlice();
            slice.mutateMetaData(md -> md.setKnownSize(knownSize));
        }

        public void putData(Path path, Node[] nodes) {
            ReadableChannelSource<Node[]> cache = setup(path);

            AdvancedRangeCacheImpl<Node[]> arc = (AdvancedRangeCacheImpl<Node[]>)cache;
            Slice<Node[]> slice = arc.getSlice();
            try (SliceAccessor<Node[]> accessor = slice.newSliceAccessor()) {
                accessor.claimByOffsetRange(0, nodes.length);
                try {
                    accessor.write(0, nodes, 0, nodes.length);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        // If there is sufficient initial data available then pre-fetching a property can be skippeds
        public long getInitialDataLength(Path path) {
            AdvancedRangeCacheImpl<Node[]> item = (AdvancedRangeCacheImpl<Node[]>)pathToValues.get(path);
            Slice<Node[]> slice = item.getSlice();

            // Return the length of the range starting from 0
            long count = slice.computeFromMetaData(false, md -> {
                Long r = 0l;
                Range<Long> firstRange = Iterables.getFirst(md.getLoadedRanges().asRanges(), Range.closedOpen(0l, 0l));
                firstRange = firstRange.canonical(DiscreteDomain.longs());
                if (firstRange.lowerEndpoint() == 0) {
                    r = firstRange.upperEndpoint();
                }
                return r;
            });

            return count;
        }

        public Long getCountForPath(Path path) {
            Long result = null;
            ReadableChannelSource<Node[]> cache = pathToValues.get(path);
            if (cache != null) {
                AdvancedRangeCacheImpl<Node[]> arc = (AdvancedRangeCacheImpl<Node[]>)cache;
                Slice<Node[]> slice = arc.getSlice();
                result = slice.computeFromMetaData(false, md -> md.getKnownSize());
            }
            return result;
        }
//        public Map<Path, Long> getPathToCount() {
//            return pathToCount;
//        }

        public Set<Path> getKnownPaths() {
            // return pathToCount.keySet();
            return pathToValues.keySet();
        }
    }