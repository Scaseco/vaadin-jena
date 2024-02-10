package org.aksw.jena_sparql_api.vaadin.data.provider;

import java.util.List;
import java.util.stream.Stream;

import org.aksw.commons.util.range.CountInfo;
import org.aksw.commons.util.range.RangeUtils;
import org.aksw.jenax.arq.util.syntax.QueryGenerationUtils;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactoryQuery;
import org.aksw.jenax.sparql.fragment.api.Fragment;
import org.aksw.jenax.sparql.fragment.impl.FragmentImpl;
import org.aksw.jenax.sparql.query.rx.SparqlRx;
import org.apache.jena.query.SortCondition;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.syntax.ElementData;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.primitives.Ints;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;

import io.reactivex.rxjava3.core.Flowable;

public abstract class DataProviderSparqlBase<T>
        // implements Unwrappable, Delegated<DataProvider<T, Expr>>
        extends AbstractBackEndDataProvider<T, Expr> {
    private static final Logger logger = LoggerFactory.getLogger(DataProviderSparqlBase.class);

    private static final long serialVersionUID = 1L;

    // This feels like a hack - it might be better to have the distinct flag part of the relation but this needs more thought
    protected boolean alwaysDistinct = false;

    protected Fragment relation;
    protected QueryExecutionFactoryQuery qef;

    public int predefinedSize = -1;

    public DataProviderSparqlBase(Fragment relation,
            QueryExecutionFactoryQuery qef) {
        super();
        this.relation = relation;
        this.qef = qef;
    }

    public void setAlwaysDistinct(boolean alwaysDistinct) {
        this.alwaysDistinct = alwaysDistinct;
    }

    public boolean getAlwaysDistinct() {
        return alwaysDistinct;
    }


    public void setRelation(Fragment relation) {
        this.relation = relation;
        this.refreshAll();
    }

    public void setRelation(List<Var> vars, List<Binding> bindings) {
        ElementData elt = new ElementData(vars, bindings);
        Fragment rel = new FragmentImpl(elt, vars);
        setRelation(rel);
    }

    public Fragment getRelation() {
        return relation;
    }

    public QueryExecutionFactoryQuery getQueryExecutionFactory() {
        return qef;
    }

    public void setQueryExecutionFactory(QueryExecutionFactoryQuery qef) {
        this.qef = qef;
    }

    public static org.apache.jena.query.Query toJena(Fragment relation, Query<?, Expr> query) {
        org.apache.jena.query.Query baseQuery = createEffectiveQuery(relation, query);
        org.apache.jena.query.Query q = QueryUtils.applySlice(
                baseQuery,
                (long) query.getOffset(),
                (long) query.getLimit(),
                false);


        // Any sort conditions on the vaadin query override those of the SPARQL query
        if (!query.getSortOrders().isEmpty()) {
            if (q.hasOrderBy()) {
                q.getOrderBy().clear();
            }

            for (QuerySortOrder qso : Lists.reverse(query.getSortOrders())) {
                q.addOrderBy(convertSortCondition(qso));
            }
        }


        return q;
    }

    @Override
    protected Stream<T> fetchFromBackEnd(Query<T, Expr> query) {
        org.apache.jena.query.Query q = toJena(relation, query);
        if (alwaysDistinct) {
            q.setDistinct(true);
        }

        logger.trace("Effective query: " + q);

        Flowable<T> solutionFlow = createSolutionFlow(q);
        List<T> items = solutionFlow.toList().blockingGet();
        logger.info("Items from backend: " + items.size());
        Stream<T> result = items.stream();
        // Stream<T> result = solutionFlow.toList().blockingGet().stream();


//        Stream<Binding> debug = toStream(SparqlRx.execSelectRaw(() -> qef.apply(q)));
//        long s = System.currentTimeMillis();
//        debug.forEach(b -> {int i = 1;});
//        System.out.println(System.currentTimeMillis() - s + "ms: ");
//        result = result.peek(b -> System.out.println(b));
        return result;
    }

    protected abstract Flowable<T> createSolutionFlow(org.apache.jena.query.Query query);


    @Override
    protected int sizeInBackEnd(Query<T, Expr> query) {
        if (predefinedSize != -1) {
            return predefinedSize;
        }

        org.apache.jena.query.Query baseQuery = createEffectiveQuery(relation, query);
        if (alwaysDistinct) {
            baseQuery.setDistinct(true);
        }

        logger.debug("Computing resultset size for\n" + baseQuery);

        Range<Long> range = SparqlRx.fetchCountQuery(qef, baseQuery, null, null).blockingGet();
        CountInfo countInfo = RangeUtils.toCountInfo(range);
        long count = countInfo.getCount();

        int result = Ints.saturatedCast(count);
        logger.debug("Counted items: " + result);
        return result;
    }


    public static org.apache.jena.query.Query createEffectiveQuery(Fragment relation, Query<?, Expr> query) {
        Expr expr = query.getFilter().orElse(null);

        org.apache.jena.query.Query result = relation.toQuery();
        result = result.cloneQuery();

        if (expr != null) {
            // QueryUtils.injectFilter(result, expr);
            // In general we need to wrap the query
            // FIXME Breaks for non-select queries!
            org.apache.jena.query.Query outerQuery = new org.apache.jena.query.Query();
            outerQuery.setQuerySelectType();
            outerQuery.setQueryResultStar(true);
            outerQuery.setQueryPattern(new ElementSubQuery(result));
            QueryUtils.injectFilter(outerQuery, expr);
            result = outerQuery;
        }

        return result;
    }

    public static int toJena(SortDirection sd) {
        int result = SortDirection.ASCENDING.equals(sd)
                ? org.apache.jena.query.Query.ORDER_ASCENDING
                : SortDirection.DESCENDING.equals(sd)
                ? org.apache.jena.query.Query.ORDER_DESCENDING
                : org.apache.jena.query.Query.ORDER_DEFAULT;

        return result;
    }

    public static SortCondition convertSortCondition(QuerySortOrder qso) {
        Var var = Var.alloc(qso.getSorted());
        int dir = toJena(qso.getDirection());

        return new SortCondition(var, dir);
    }

    public void setPredefinedSize(int predefinedSize) {
        this.predefinedSize = predefinedSize;
    }
}
