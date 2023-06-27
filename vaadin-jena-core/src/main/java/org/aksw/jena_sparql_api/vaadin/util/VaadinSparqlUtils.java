package org.aksw.jena_sparql_api.vaadin.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.concepts.RelationUtils;
import org.aksw.jena_sparql_api.vaadin.data.provider.DataProviderSparqlBinding;
import org.aksw.jena_sparql_api.vaadin.data.provider.DataProviderSparqlRdfNode;
import org.aksw.jena_sparql_api.vaadin.data.provider.DataProviderSparqlResource;
import org.aksw.jena_sparql_api.vaadin.data.provider.DataProviderSparqlSolution;
import org.aksw.jenax.arq.util.expr.ExprUtils;
import org.aksw.jenax.connection.query.QueryExecutionFactoryQuery;
import org.aksw.jenax.sparql.relation.api.Relation;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.E_Str;
import org.apache.jena.sparql.expr.E_StrContains;
import org.apache.jena.sparql.expr.E_StrLowerCase;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.HeaderRow.HeaderCell;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.SerializableFunction;

public class VaadinSparqlUtils {

    public static <T extends Resource> void setQueryForGridResource(
            Grid<T> grid,
            QueryExecutionFactoryQuery qef,
            Query query,
            Class<T> rdfNodeClass,
            String varName,
            Function<Binding, QuerySolution> customBindingMapper) {

        Relation relation = RelationUtils.fromQuery(query);
        varName = varName == null ? relation.toUnaryRelation().getVar().getName() : varName;


        DataProvider<T, Expr> dataProvider = new DataProviderSparqlResource<>(relation, qef, rdfNodeClass, varName, customBindingMapper);

        grid.setDataProvider(dataProvider);
        grid.removeAllColumns();
          Column<T> column = grid.addColumn(rdfNode -> {
              // RDFNode rdfNode = qs.get(varName);
              Node node = rdfNode.asNode();
              Object r;
              if (node == null) {
                  r = null;
              } else if (node.isLiteral()) {
                  r = node.getLiteralValue();
              } else {
                  r = node.toString();
              }
              return r;
            }); //.setHeader(varName);

            column.setKey(varName);
            column.setResizable(true);
            column.setSortable(true);
    }

    public static <T extends RDFNode> void setQueryForGridRdfNode(
            Grid<T> grid,
            QueryExecutionFactoryQuery qef,
            Query query,
            Class<T> rdfNodeClass,
            String varName,
            Function<Binding, QuerySolution> customBindingMapper) {

        Relation relation = RelationUtils.fromQuery(query);
        varName = varName == null ? relation.toUnaryRelation().getVar().getName() : varName;
        DataProvider<T, Expr> dataProvider = new DataProviderSparqlRdfNode<>(relation, qef, rdfNodeClass, varName, customBindingMapper);

        grid.setDataProvider(dataProvider);
        grid.removeAllColumns();
          Column<T> column = grid.addColumn(rdfNode -> {
              // RDFNode rdfNode = qs.get(varName);
              Node node = rdfNode.asNode();
              Object r;
              if (node == null) {
                  r = null;
              } else {
                  r = node.toString(false);
              }
//              } else if (node.isLiteral()) {
//                  r = node.getLiteralLexicalForm();
//              } else {
//                  r = node.toString(false);
//              }
              return r;
            }); //.setHeader(varName);

            column.setKey(varName);
            column.setResizable(true);
            column.setSortable(true);
    }

    public static void setQueryForGridSolution(
            Grid<QuerySolution> grid,
            QueryExecutionFactoryQuery qef,
            Query query) {
        Relation relation = RelationUtils.fromQuery(query);
        DataProvider<QuerySolution, Expr> dataProvider = new DataProviderSparqlSolution(relation, qef);

        grid.setDataProvider(dataProvider);
        List<Var> vars = query.getProjectVars();
        List<String> varNames = Var.varNames(vars);
        grid.removeAllColumns();

        for (String varName : varNames) {
            Column<QuerySolution> column = grid.addColumn(qs -> {
                RDFNode rdfNode = qs.get(varName);
                Node node = rdfNode.asNode();
                Object r;
                if (node == null) {
                    r = null;
                } else {
                    r = node.toString(false);
                }
//                } else if (node.isLiteral()) {
//                    r = node.getLiteralValue();
//                } else {
//                    r = node.toString();
//                }
                return r;
            }).setHeader(varName);

            column.setKey(varName);
            column.setResizable(true);
            column.setSortable(true);
        }
    }

    public static void setQueryForGridBinding(
            Grid<Binding> grid,
            HeaderRow headerRow,
            QueryExecutionFactoryQuery qef,
            Query query) {
        setQueryForGridBinding(grid, headerRow, qef, query, null);
    }


    public static void setQueryForGridBinding(
            Grid<Binding> grid,
            HeaderRow headerRow,
            DataProviderSparqlBinding dataProviderCore) {

        DataProvider<Binding, Expr> dataProvider = dataProviderCore
                .withConfigurableFilter((Expr e1, Expr e2) -> ExprUtils.andifyBalanced(
                        Arrays.asList(e1, e2).stream().filter(Objects::nonNull).collect(Collectors.toList()
                )));


        grid.setDataProvider(dataProvider);
        List<Var> vars = dataProviderCore.getRelation().getVars();
        grid.removeAllColumns();

        for (Var var : vars) {
            Column<Binding> column = grid.addColumn(binding -> {
                Node node = binding.get(var);
                Object r;
                if (node == null) {
                    r = null;
                } else {
                    r = node.toString(false);
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
    }

    public static DataProvider<Binding, Expr> createDataProvider(QueryExecutionFactoryQuery qef, Query query) {
        Relation relation = RelationUtils.fromQuery(query);
        DataProviderSparqlBinding coreDataProvider = new DataProviderSparqlBinding(relation, qef);
        coreDataProvider.setAlwaysDistinct(true);

        DataProvider<Binding, Expr> dataProvider = coreDataProvider
                .withConfigurableFilter((Expr e1, Expr e2) -> ExprUtils.andifyBalanced(
                        Arrays.asList(e1, e2).stream().filter(Objects::nonNull).collect(Collectors.toList()
                )));
        return dataProvider;
    }

    /**
     * Configure a grid's data provider based on a SPARQL SELECT query such that
     * pagination, sorting (TODO: and filtering) works out of the box.
     *
     * @param grid
     * @param qef
     * @param query
     */
    public static void setQueryForGridBinding(
            Grid<Binding> grid,
            HeaderRow headerRow,
            QueryExecutionFactoryQuery qef,
            Query query,
            List<Var> visibleColumns) {

//        Relation relation = RelationUtils.fromQuery(query);
//        DataProviderSparqlBinding coreDataProvider = new DataProviderSparqlBinding(relation, qef);
//        coreDataProvider.setAlwaysDistinct(true);
//
//        DataProvider<Binding, Expr> dataProvider = coreDataProvider
//                .withConfigurableFilter((Expr e1, Expr e2) -> ExprUtils.andifyBalanced(
//                        Arrays.asList(e1, e2).stream().filter(Objects::nonNull).collect(Collectors.toList()
//                )));

        DataProvider<Binding, Expr> dataProvider = createDataProvider(qef, query);
        grid.setDataProvider(dataProvider);
        List<Var> vars = visibleColumns == null ? query.getProjectVars() : visibleColumns;
        grid.removeAllColumns();

        for (Var var : vars) {
            Column<Binding> column = grid.addColumn(binding -> {
                Node node = binding.get(var);
                Object r;
                if (node == null) {
                    r = null;
                } else {
                    r = node.toString(false);
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
    }


    public static void setQueryForGridBindingComponent(
            Grid<Binding> grid,
            HeaderRow headerRow,
            QueryExecutionFactoryQuery qef,
            Query query,
            Function<Var, SerializableFunction<Binding, Component>> varToRenderer) {

        Relation relation = RelationUtils.fromQuery(query);
        DataProvider<Binding, Expr> dataProvider = new DataProviderSparqlBinding(relation, qef)
                .withConfigurableFilter((Expr e1, Expr e2) -> ExprUtils.andifyBalanced(
                        Arrays.asList(e1, e2).stream().filter(Objects::nonNull).collect(Collectors.toList()
                )));

        grid.setDataProvider(dataProvider);
        // List<Var> vars = visibleColumns == null ? query.getProjectVars() : visibleColumns;
        List<Var> vars = query.getProjectVars();
        grid.removeAllColumns();

        for (Var var : vars) {
            Function<Binding, Component> renderer = varToRenderer.apply(var);
            if (renderer == null) {
                continue;
            }

            Column<Binding> column = grid.addColumn(new ComponentRenderer<Component, Binding>(renderer::apply));
            headerRow.getCell(column).setText(var.getName());

            column.setKey(var.getName());
            column.setResizable(true);
            column.setSortable(true);
        }
    }


    public static Map<Var, TextField> configureGridFilter(
            Grid<Binding> grid, HeaderRow filterRow, Collection<Var> vars, Function<Var, Function<String, Expr>> varToStrToExpr) {
        // HeaderRow filterRow = grid.appendHeaderRow();

        Map<Var, TextField> result = new LinkedHashMap<>();
        for (Var var : vars) {
            Function<String, Expr> strToExpr = varToStrToExpr.apply(var);

            Column<Binding> column = grid.getColumnByKey(var.getName());
            if (column != null) {
                HeaderCell cell = filterRow.getCell(column);
                if (cell != null) {
                    TextField tf = new TextField();
                    result.put(var, tf);
                    tf.addValueChangeListener(event -> {
                        // if (grid.getDataProvider() instanceof InMemoryDataProvider) {
                            registerGridFilters(grid, result, strToExpr);
                            grid.getDataProvider().refreshAll();
                        // }
                    });

                    tf.setValueChangeMode(ValueChangeMode.LAZY);

                    tf.setSizeFull();
                    tf.setPlaceholder("Filter");
                    tf.getElement().setAttribute("focus-target", "");
                    cell.setComponent(tf);
                }
            }
        }

        return result;
    }

    public static Optional<Expr> createFilterExpr(Var var, String str) {
        Optional<Expr> result = str == null || str.isBlank()
                ? Optional.empty()
                : Optional.of(new E_StrContains(new E_StrLowerCase(new E_Str(new ExprVar(var))), NodeValue.makeString(str.toLowerCase())));
        return result;
    }

    public static void registerGridFilters(Grid<Binding> grid, Map<Var, ? extends HasValue<?, String>> filterFields, Function<String, Expr> strToExpr) {
        DataProvider<Binding, ?> rawDataProvider = grid.getDataProvider();
        if (rawDataProvider instanceof ConfigurableFilterDataProvider) {
            ConfigurableFilterDataProvider<Binding, Expr, Expr> dataProvider = (ConfigurableFilterDataProvider<Binding, Expr, Expr>)rawDataProvider;
            List<Expr> exprs = filterFields.entrySet().stream()
                .flatMap(e -> {
                    String str = e.getValue().getValue();
                    Expr expr = strToExpr.apply(str);
                    Stream<Expr> r = expr == null ? Stream.empty() : Stream.of(expr);

//                    Stream<Expr> r = str == null || str.isBlank()
//                            ? Stream.empty()
//                            : Stream.of(new E_StrContains(new E_StrLowerCase(new E_Str(new ExprVar(e.getKey()))), NodeValue.makeString(str.toLowerCase())));

                    return r;
                })
                .collect(Collectors.toList());

            Expr expr = ExprUtils.andifyBalanced(exprs);
            dataProvider.setFilter(expr);
        }
    }

}
