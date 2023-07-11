package org.aksw.jenax.treequery2.impl;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import org.apache.jena.sparql.expr.Expr;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class FacetConstraints<T> {
    // protected Map<Expr, Boolean> exprToState;

    // TODO Should the final value be a wrapper that links back to the keys?
    protected Table<Set<T>, Expr, Boolean> model = HashBasedTable.create();

    public FacetConstraints() {
    }

    public Collection<Expr> getExprs() {
        return model.columnKeySet();
    }

    public ConstraintApi2Impl<T> getFacade(T node) {
        return new ConstraintApi2Impl<>(this, node);
    }

    @Override
    public String toString() {
        return Objects.toString(model);
    }
}
