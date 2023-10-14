package org.aksw.jena_sparql_api.vaadin.data.provider.observable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.aksw.commons.util.range.CountInfo;
import org.aksw.commons.util.range.RangeUtils;
import org.aksw.jena_sparql_api.vaadin.data.provider.DataProviderSparqlBase;
import org.aksw.jenax.arq.util.syntax.QueryGenerationUtils;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactory;
import org.aksw.jenax.sparql.datasource.observable.ObservableSource;
import org.aksw.jenax.sparql.fragment.api.Fragment;
import org.aksw.jenax.sparql.query.rx.SparqlRx;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jsonldjava.shaded.com.google.common.primitives.Ints;
import com.google.common.collect.Iterables;
import com.google.common.collect.Range;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.Query;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.Disposable;

public class DataProviderSparqlObservableBase<T>
    extends AbstractBackEndDataProvider<T, Expr>
    implements AutoCloseable
{

    private static final Logger logger = LoggerFactory.getLogger(DataProviderSparqlObservableBase.class);

    private static final long serialVersionUID = 1L;

    protected Fragment relation;
    protected ObservableSource<org.apache.jena.query.Query, Collection<T>> observableSource;

    protected Disposable dataDisposable;
    protected Disposable countDisposable;

    @Override
    protected Stream<T> fetchFromBackEnd(Query<T, Expr> query) {
        org.apache.jena.query.Query q = DataProviderSparqlBase.toJena(relation, query);

        Disposable newDisposable = observableSource.observe(q).subscribe(rows -> {
            refreshAll();
        });

        if (dataDisposable != null) {
            dataDisposable.dispose();
        }

        dataDisposable = newDisposable;

        Flowable<Collection<T>> flow = observableSource.observe(q);
        Iterable<Collection<T>> tmp = flow.blockingLatest();
        Collection<T> items = Iterables.getFirst(tmp, Collections.emptyList());

        return items.stream();
    }
    @Override
    protected int sizeInBackEnd(Query<T, Expr> query) {

        org.apache.jena.query.Query baseQuery = DataProviderSparqlBase.createEffectiveQuery(relation, query);
        Entry<Var, org.apache.jena.query.Query> countQuery = QueryGenerationUtils.createQueryCountPartition(baseQuery, baseQuery.getProjectVars(), null, null);

        logger.debug("Computing resultset size for\n" + baseQuery);

//      System.out.println("Given: " + query);
//      System.out.println(partitionVars);
//      System.out.println("Generated count query: " + countQuery);

          Var v = countQuery.getKey();
          org.apache.jena.query.Query q = countQuery.getValue();

            Flowable<Long> flow = observableSource.observe(q)
                    .map(rows -> 1l);

            Iterable<Long> tmp = flow.blockingNext();
            long count = Iterables.getFirst(tmp, 0l);



        Range<Long> range = SparqlRx.fetchCountQuery((QueryExecutionFactory)null, baseQuery, null, null).blockingGet();
        CountInfo countInfo = RangeUtils.toCountInfo(range);
        long count1 = countInfo.getCount();

        int result = Ints.saturatedCast(count1);
        logger.debug("Counted items: " + result);
        return result;
    }

    @Override
    public void close() {
        // Unsubscribe from the flows
    }
}
