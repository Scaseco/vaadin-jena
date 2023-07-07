package org.aksw.jenax.treequery2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetStep;
import org.apache.jena.query.SortCondition;

public class QueryNodeImpl
    implements QueryNode
{
    protected FacetStep step;
    protected QueryNode parent;
    protected Long offset;
    protected Long limit;
    protected Map<FacetStep, QueryNode> children = new LinkedHashMap<>();
    protected List<SortCondition> sortConditions = new ArrayList<>();

    public QueryNodeImpl(QueryNode parent, FacetStep step) {
        super();
        this.parent = parent;
        this.step = step;
    }

    @Override
    public Long offset() {
        return offset;
    }

    @Override
    public QueryNode offset(Long offset) {
        this.offset = offset;
        return this;
    }

    @Override
    public Long limit() {
        return limit;
    }

    @Override
    public QueryNode limit(Long limit) {
        this.limit = limit;
        return this;
    }

    @Override
    public QueryNode getParent() {
        return parent;
    }

    @Override
    public FacetPath getPath() {
        FacetPath result = parent == null
                ? FacetPath.newAbsolutePath(step == null ? Collections.emptyList() : Collections.singletonList(step))
                : parent.getPath().resolve(step);
        return result;
    }

    @Override
    public Collection<QueryNode> getChildren() {
        return children.values();
    }

    @Override
    public List<SortCondition> getSortConditions() {
        return sortConditions;
    }

    @Override
    public QueryNode getChild(FacetStep step) {
        return children.get(step);
    }

    @Override
    public QueryNode getOrCreateChild(FacetStep step) {
        QueryNode result = children.computeIfAbsent(step, s -> new QueryNodeImpl(this, s));
        return result;
    }

    public static QueryNode newRoot() {
        return new QueryNodeImpl(null, null);
    }

    /** Start a traversal for a order. Orders are only applied when .asc() or .desc() is called. */
    @Override
    public OrderNode order() {
        return null;
    }
}
