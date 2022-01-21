package org.aksw.jena_sparql_api.vaadin.data.provider;

import java.util.stream.Stream;

import org.aksw.commons.util.range.CountInfo;
import org.aksw.commons.util.range.RangeUtils;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.jenax.connection.query.QueryExecutionFactoryQuery;
import org.aksw.jenax.sparql.query.rx.SparqlRx;
import org.aksw.jenax.sparql.relation.api.Relation;
import org.apache.jena.query.SortCondition;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;

import com.github.jsonldjava.shaded.com.google.common.primitives.Ints;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;

import io.reactivex.rxjava3.core.Flowable;

public abstract class DataProviderSparqlBase<T>
        extends AbstractBackEndDataProvider<T, Expr> {
    private static final long serialVersionUID = 1L;

    protected Relation relation;
    protected QueryExecutionFactoryQuery qef;

    public int predefinedSize = -1;

    public DataProviderSparqlBase(Relation relation,
    		QueryExecutionFactoryQuery qef) {
        super();
        this.relation = relation;
        this.qef = qef;
    }

    @Override
    protected Stream<T> fetchFromBackEnd(Query<T, Expr> query) {
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

        System.out.println(q);

        Flowable<T> solutionFlow = createSolutionFlow(q);
        Stream<T> result = solutionFlow.toList().blockingGet().stream();


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

        System.out.println("Computing resultset size for\n" + baseQuery);

        Range<Long> range = SparqlRx.fetchCountQuery(qef::createQueryExecution, baseQuery, null, null).blockingGet();
        CountInfo countInfo = RangeUtils.toCountInfo(range);
        long count = countInfo.getCount();

        int result = Ints.saturatedCast(count);
        System.out.println("Counted items: " + result);
        return result;
    }


    public static org.apache.jena.query.Query createEffectiveQuery(Relation relation, Query<?, Expr> query) {
        Expr expr = query.getFilter().orElse(null);

        org.apache.jena.query.Query result = relation.toQuery();
        result = result.cloneQuery();

        if (expr != null) {
            QueryUtils.injectFilter(result, expr);
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
