package org.aksw.jenax.treequery2.impl;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import org.aksw.jenax.arq.util.node.NodeCustom;
import org.apache.jena.sparql.expr.Expr;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
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

    // TODO Where is the method that builds a tree by just adding FacetPaths?
    // TreeData<FacetPath> treeData

    public static <T> SetMultimap<T, Expr> createConstraintIndex(FacetConstraints<T> constraints) {
        Collection<Expr> exprs = constraints.getExprs();
        SetMultimap<T, Expr> constraintIndex = HashMultimap.create();
        for (Expr expr : exprs) {
            Set<T> paths = NodeCustom.mentionedValues(expr);
            for (T path : paths) {
                constraintIndex.put(path, expr);
            }
        }
        return constraintIndex;
    }

}
