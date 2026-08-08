package org.aksw.jena_sparql_api.vaadin.util;

import java.util.List;

import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.HeaderRow;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

public class ColumnFactorySparql {

    private static final ColumnFactorySparql INSTANCE = new ColumnFactorySparql();

    public static ColumnFactorySparql getDefaultFactory() {
        return INSTANCE;
    }

    public void refreshColumns(GridLike<Binding> grid, HeaderRow headerRow, List<Var> vars) {
        beforeRefresh(grid, headerRow, vars);
        for (Var var : vars) {
            refreshColumn(grid, headerRow, var);
        }
        afterRefresh(grid, headerRow, vars);
    }

    protected void refreshColumn(GridLike<Binding> grid, HeaderRow headerRow, Var var) {
        Column<Binding> column = grid.addColumn(binding -> {
            Node node = binding.get(var);
            Object r;
            if (node == null) {
                r = null;
            } else {
                r = node.toString();
            }
//                } else if (node.isLiteral()) {
//                    r = node.getLiteralValue();
//                } else {
//                    r = node.toString();
//                }
            return r;
        }); //.setHeader(var.getName());

        headerRow.getCell(column).setText(var.getName());

        column.setKey(var.getName());
        column.setResizable(true);
        column.setSortable(true);
    }

    protected void beforeRefresh(GridLike<Binding> grid, HeaderRow headerRow, List<Var> vars) {
    }

    protected void afterRefresh(GridLike<Binding> grid, HeaderRow headerRow, List<Var> vars) {
    }
}
