package org.aksw.jenax.treequery2;

import java.util.stream.Stream;

import org.aksw.jenax.connection.datasource.RdfDataSource;
import org.aksw.jenax.sparql.relation.api.UnaryRelation;
import org.aksw.jenax.treequery2.impl.ResultNode2;
import org.aksw.jenax.treequery2.old.NodeQueryOld;

import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;

public class DataProviderNodeQuery
    extends AbstractBackEndHierarchicalDataProvider<ResultNode2, UnaryRelation>
{
    protected RdfDataSource dataSource;
    protected NodeQueryOld nodeQuery;

    @Override
    public int getChildCount(HierarchicalQuery<ResultNode2, UnaryRelation> query) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean hasChildren(ResultNode2 item) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected Stream<ResultNode2> fetchChildrenFromBackEnd(HierarchicalQuery<ResultNode2, UnaryRelation> query) {
        ResultNode2 rsn = query.getParent();
        // TODO Auto-generated method stub
        return null;
    }
}
