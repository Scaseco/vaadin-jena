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

import org.aksw.commons.util.delegate.Delegated;
import org.aksw.commons.util.delegate.Unwrappable;
import org.aksw.commons.util.obj.ObjectUtils;
import org.aksw.jena_sparql_api.vaadin.data.provider.DataProviderSparqlBinding;
import org.aksw.jena_sparql_api.vaadin.data.provider.DataProviderSparqlRdfNode;
import org.aksw.jena_sparql_api.vaadin.data.provider.DataProviderSparqlResource;
import org.aksw.jena_sparql_api.vaadin.data.provider.DataProviderSparqlSolution;
import org.aksw.jenax.arq.util.expr.ExprUtils;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactoryQuery;
import org.aksw.jenax.sparql.fragment.api.Fragment;
import org.aksw.jenax.sparql.fragment.impl.FragmentUtils;
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
import com.vaadin.flow.data.provider.ConfigurableFilterDataProviderWrapper;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.FilterUtils;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.SerializableBiFunction;
import com.vaadin.flow.function.SerializableFunction;

public class VaadinSparqlUtils {

    public static <T extends Resource> void setQueryForGridResource(
            Grid<T> grid,
            QueryExecutionFactoryQuery qef,
            Query query,
            Class<T> rdfNodeClass,
            String varName,
            Function<Binding, QuerySolution> customBindingMapper) {

        Fragment relation = FragmentUtils.fromQuery(query);
        varName = varName == null ? relation.toFragment1().getVar().getName() : varName;


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

        Fragment relation = FragmentUtils.fromQuery(query);
        varName = varName == null ? relation.toFragment1().getVar().getName() : varName;
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
            HeaderRow headerRow,
            QueryExecutionFactoryQuery qef,
            Query query,
            Function<DataProvider<QuerySolution, Expr>, DataProvider<QuerySolution, Expr>> dataProviderDecorizer
            ) {
        // Relation relation = RelationUtils.fromQuery(query); // Relations currently don't support a distinct flag - but they should
        // DataProviderSparqlSolution dataProviderRaw = new DataProviderSparqlSolution(relation, qef);
        DataProvider<QuerySolution, Expr> dataProviderRaw = createDataProviderQs(qef, query, query.isDistinct());

        // dataProviderRaw.setAlwaysDistinct(query.isDistinct());
        DataProvider<QuerySolution, Expr> dataProvider = dataProviderDecorizer == null
                ? dataProviderRaw
                : dataProviderDecorizer.apply(dataProviderRaw);

        grid.setDataProvider(dataProvider);
        List<Var> vars = query.getProjectVars();
        List<String> varNames = Var.varNames(vars);
        grid.removeAllColumns();

        for (String varName : varNames) {
            Column<QuerySolution> column = grid.addColumn(qs -> {
                RDFNode rdfNode = qs.get(varName);
                Node node = rdfNode == null ? null : rdfNode.asNode();
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
            }); //.setHeader(varName);
            headerRow.getCell(column).setText(varName);

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



    /** Util method that sets up a view by calling base.withConfigurableFilter() */
    public static <T> DataProvider<T, Expr> withSparqlFilter(DataProvider<T, Expr> base) {
        DataProvider<T, Expr> result = base
                .withConfigurableFilter((Expr e1, Expr e2) -> ExprUtils.andifyBalanced(
                        Arrays.asList(e1, e2).stream().filter(Objects::nonNull).collect(Collectors.toList()
                )));

        return result;
    }

    public static void setQueryForGridBinding(
            Grid<Binding> grid,
            HeaderRow headerRow,
            DataProviderSparqlBinding dataProviderCore) {

        DataProvider<Binding, Expr> dataProvider = withSparqlFilter(dataProviderCore);
//                .withConfigurableFilter((Expr e1, Expr e2) -> ExprUtils.andifyBalanced(
//                        Arrays.asList(e1, e2).stream().filter(Objects::nonNull).collect(Collectors.toList()
//                )));


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

    public static class UnwrappableConfigurableFilterDataProvider<T, Q, C, F>
        extends ConfigurableFilterDataProviderWrapper<T, Q, C, F>
        implements Delegated<DataProvider<T, F>>, Unwrappable
    {
        private static final long serialVersionUID = 1L;

        protected SerializableBiFunction<Q, C, F> filterCombiner;

        public UnwrappableConfigurableFilterDataProvider(DataProvider<T, F> dataProvider, SerializableBiFunction<Q, C, F> filterCombiner) {
            super(dataProvider);
            this.filterCombiner = filterCombiner;
        }

        @Override
        public DataProvider<T, F> delegate() {
            return dataProvider;
        }

        @Override
        protected F combineFilters(Q queryFilter, C configuredFilter) {
            return FilterUtils.combineFilters(filterCombiner, queryFilter,
                    configuredFilter);
        }

        public static <T, Q, C, F> DataProvider<T, Q> of(DataProvider<T, F> dataProvider, SerializableBiFunction<Q, C, F> filterCombiner) {
            return new UnwrappableConfigurableFilterDataProvider<>(dataProvider, filterCombiner);
        }
    }

    public static <T> DataProvider<T, Expr> wrapDataProviderWithFilter(DataProvider<T, Expr> delegate) {
        DataProvider<T, Expr> result = UnwrappableConfigurableFilterDataProvider.of(delegate,
                (Expr e1, Expr e2) -> ExprUtils.andifyBalanced(
                        Arrays.asList(e1, e2).stream().filter(Objects::nonNull).collect(Collectors.toList())));
        return result;
    }

    public static DataProvider<Binding, Expr> createDataProvider(QueryExecutionFactoryQuery qef, Query query, boolean alwaysDistinct) {
        Fragment relation = FragmentUtils.fromQuery(query);
        DataProviderSparqlBinding coreDataProvider = new DataProviderSparqlBinding(relation, qef);
        coreDataProvider.setAlwaysDistinct(alwaysDistinct);
        DataProvider<Binding, Expr> dataProvider = wrapDataProviderWithFilter(coreDataProvider);
        return dataProvider;
    }

    public static DataProvider<QuerySolution, Expr> createDataProviderQs(QueryExecutionFactoryQuery qef, Query query, boolean alwaysDistinct) {
        Fragment relation = FragmentUtils.fromQuery(query);
        DataProviderSparqlSolution coreDataProvider = new DataProviderSparqlSolution(relation, qef);
        coreDataProvider.setAlwaysDistinct(alwaysDistinct);
        DataProvider<QuerySolution, Expr> dataProvider = wrapDataProviderWithFilter(coreDataProvider);
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

        DataProvider<Binding, Expr> dataProvider = createDataProvider(qef, query, true);
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

        // Relation relation = RelationUtils.fromQuery(query);
//        DataProvider<Binding, Expr> dataProvider = new DataProviderSparqlBinding(relation, qef)
//                .withConfigurableFilter((Expr e1, Expr e2) -> ExprUtils.andifyBalanced(
//                        Arrays.asList(e1, e2).stream().filter(Objects::nonNull).collect(Collectors.toList()
//                )));
        DataProvider<Binding, Expr> dataProvider = createDataProvider(qef, query, false);

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
            Grid<?> grid, HeaderRow filterRow, Collection<Var> vars) {
        return configureGridFilter(grid, filterRow, vars, var -> str -> VaadinSparqlUtils.createFilterExpr(var, str).orElse(null));
    }

    /** Must be called AFTER columns have been added. */
    public static Map<Var, TextField> configureGridFilter(
            Grid<?> grid, HeaderRow filterRow, Collection<Var> vars, Function<Var, Function<String, Expr>> varToStrToExpr) {
        // HeaderRow filterRow = grid.appendHeaderRow();

        Map<Var, TextField> result = new LinkedHashMap<>();
        for (Var var : vars) {
            Function<String, Expr> strToExpr = varToStrToExpr.apply(var);

            String columnKey = var.getName();
            Column<?> column = grid.getColumnByKey(columnKey);
            if (column != null) {
                HeaderCell cell = filterRow.getCell(column);
                if (cell != null) {
                    TextField tf = new TextField();
                    result.put(var, tf);
                    tf.addValueChangeListener(event -> {
                        // if (grid.getDataProvider() instanceof InMemoryDataProvider) {
                            registerGridFilters(grid, result, varToStrToExpr);
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

    public static void registerGridFilters(Grid<?> grid, Map<Var, ? extends HasValue<?, String>> filterFields, Function<Var, Function<String, Expr>> varToStrToExpr) {
        DataProvider<?, ?> rawDataProvider = grid.getDataProvider();
        ConfigurableFilterDataProvider<?, Expr, Expr> dataProvider = Unwrappable.unwrap(rawDataProvider, ConfigurableFilterDataProvider.class, true).orElse(null);

        // if (rawDataProvider instanceof ConfigurableFilterDataProvider) {
        if (dataProvider != null) {
            // ConfigurableFilterDataProvider<?, Expr, Expr> dataProvider = (ConfigurableFilterDataProvider<?, Expr, Expr>)rawDataProvider;
            List<Expr> exprs = filterFields.entrySet().stream()
                .flatMap(e -> {
                    Var var = e.getKey();
                    String str = e.getValue().getValue();
                    Function<String, Expr> strToExpr = varToStrToExpr.apply(var);
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
        } else {
            throw new IllegalArgumentException("The grid's DataProvider was expected to be of type ConfigurableFilterDataProvider but was: " + ObjectUtils.getClass(rawDataProvider));
        }
    }
}
