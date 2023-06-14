package org.aksw.vaadin.app.demo.view.edit.propertylist;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.concepts.RelationUtils;
import org.aksw.jena_sparql_api.vaadin.data.provider.DataProviderSparqlBinding;
import org.aksw.jenax.arq.util.expr.ExprUtils;
import org.aksw.jenax.connection.query.QueryExecutionFactoryQuery;
import org.aksw.jenax.sparql.relation.api.Relation;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.data.provider.DataProvider;

public class SparqlGrid {
//    public static void setQueryForGridBinding(
//            Grid<Binding> grid,
//            QueryExecutionFactoryQuery qef,
//            Query query,
//            List<Var> visibleColumns) {
//
//    	grid.header
//
//        Relation relation = RelationUtils.fromQuery(query);
//        DataProviderSparqlBinding coreDataProvider = new DataProviderSparqlBinding(relation, qef);
//        coreDataProvider.setAlwaysDistinct(true);
//
//        DataProvider<Binding, Expr> dataProvider = coreDataProvider
//                .withConfigurableFilter((Expr e1, Expr e2) -> ExprUtils.andifyBalanced(
//                        Arrays.asList(e1, e2).stream().filter(Objects::nonNull).collect(Collectors.toList()
//                )));
//
//        grid.setDataProvider(dataProvider);
//        List<Var> vars = visibleColumns == null ? query.getProjectVars() : visibleColumns;
//        grid.removeAllColumns();
//
//        for (Var var : vars) {
//            Column<Binding> column = grid.addColumn(binding -> {
//                Node node = binding.get(var);
//                Object r;
//                if (node == null) {
//                    r = null;
//                } else {
//                    r = node.toString(false);
//                }
////                } else if (node.isLiteral()) {
////                    r = node.getLiteralValue();
////                } else {
////                    r = node.toString();
////                }
//                return r;
//            }); //.setHeader(var.getName());
//
//            headerRow.getCell(column).setText(var.getName());
//
//            column.setKey(var.getName());
//            column.setResizable(true);
//            column.setSortable(true);
//        }
//    }
}
