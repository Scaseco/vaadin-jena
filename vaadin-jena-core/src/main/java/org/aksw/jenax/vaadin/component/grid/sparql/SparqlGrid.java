package org.aksw.jenax.vaadin.component.grid.sparql;

import java.io.Serializable;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.util.stream.SequentialGroupBySpec;
import org.aksw.commons.util.stream.StreamOperatorSequentialGroupBy;
import org.aksw.jena_sparql_api.concepts.RelationUtils;
import org.aksw.jena_sparql_api.vaadin.data.provider.DataProviderSparqlBinding;
import org.aksw.jena_sparql_api.vaadin.util.VaadinSparqlUtils;
import org.aksw.jenax.arq.util.expr.ExprUtils;
import org.aksw.jenax.connection.query.QueryExecutionFactoryQuery;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.sparql.relation.api.Relation;
import org.aksw.jenax.vaadin.label.LabelMgr;
import org.aksw.jenax.vaadin.label.LabelService;
import org.aksw.jenax.vaadin.label.VaadinLabelMgr;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Streams;
import com.google.common.graph.Traverser;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.HeaderRow.HeaderCell;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.provider.DataProvider;

public class SparqlGrid {


    protected LabelMgr<Node, String> labelMgr;


    public static <T> Stream<T> leafStream(Collection<T> roots, Function<? super T, ? extends Iterable<? extends T>> successors) {
        // SuccessorsFunction<T>
        return Streams.stream(Traverser.forTree(successors::apply).depthFirstPreOrder(roots))
            .filter(node -> !successors.apply(node).iterator().hasNext());
    }

    public static <T> List<T> leafList(Collection<T> roots, Function<? super T, ? extends Iterable<? extends T>> successors) {
        return leafStream(roots, successors).collect(Collectors.toList());
    }

    public static class Header<T> {
        protected Map<T, Integer> idToLevel;
        protected List<Map<T, HeaderCell>> levelToIdToCell;
        protected List<HeaderRow> levelToHeader;

        public Header(Map<T, Integer> idToLevel, List<Map<T, HeaderCell>> levelToIdToCell, List<HeaderRow> levelToHeader) {
            super();
            this.idToLevel = idToLevel;
            this.levelToHeader = levelToHeader;
            this.levelToIdToCell = levelToIdToCell;
        }

        public Map<T, Integer> getIdToLevel() {
            return idToLevel;
        }

        public List<HeaderRow> getLevelToHeader() {
            return levelToHeader;
        }

        public List<Map<T, HeaderCell>> getLevelToIdToCell() {
            return levelToIdToCell;
        }

        /** Get the header cell by id */
        public HeaderCell getCell(T id) {
            Integer level = idToLevel.get(id);
            HeaderCell result = level == null ? null : levelToIdToCell.get(level).get(id);
            return result;
        }
    }


    public static void setQueryForGridBinding(
            Grid<Binding> grid,
            QueryExecutionFactoryQuery qef,
            LabelService<Node, String> labelMgr,
            MappedQuery mappedQuery
    ) {
        grid.removeAllColumns();
        grid.setMultiSort(true);
        Query query = mappedQuery.getQuery();

        List<Var> vars = mappedQuery.getQuery().getProjectVars();
        // List<PathPPA> leafs = vars.stream().map(mappedQuery.getVarToPath()::get).collect(Collectors.toList());

        HeaderRow primaryHeaderRow = grid.appendHeaderRow();
        List<Entry<Column<?>, FacetPath>> pathToColumn = new ArrayList<>();
        for (Var var : vars) {
            FacetPath path = mappedQuery.getVarToPath().get(var);

            Column<Binding> column = grid.addComponentColumn(binding -> {
                Node node = binding.get(var);
                Span r = new Span();
                if (node != null) {
                    r.setText(NodeFmtLib.strTTL(node));
                    VaadinLabelMgr.forHasText(labelMgr, r, node);
                }
                return r;
            });
            // .setHeader(var.getName());

            column.setKey(var.getName());
            column.setResizable(true);
            column.setSortable(true);

            pathToColumn.add(new SimpleImmutableEntry<>(column, path));
        }

        HeaderRow filterRow = grid.appendHeaderRow();
        VaadinSparqlUtils.configureGridFilter(grid, filterRow, vars, var -> str -> VaadinSparqlUtils.createFilterExpr(var, str).orElse(null));

        Multimap<FacetPath, HeaderCell> pathToCells = setupHeaders(grid, primaryHeaderRow, FacetPath::getParent, pathToColumn);

        for (Entry<FacetPath, HeaderCell> entry : pathToCells.entries()) {
            TableMapperComponent.labelForAliasPathLastStep(labelMgr, entry.getValue(), entry.getKey());
            // entry.getValue().setText("" + entry.getKey().toString());
        }


        Relation relation = RelationUtils.fromQuery(query);
        DataProviderSparqlBinding coreDataProvider = new DataProviderSparqlBinding(relation, qef);
        coreDataProvider.setAlwaysDistinct(true);

        DataProvider<Binding, Expr> dataProvider = coreDataProvider
                .withConfigurableFilter((Expr e1, Expr e2) -> ExprUtils.andifyBalanced(
                        Arrays.asList(e1, e2).stream().filter(Objects::nonNull).collect(Collectors.toList()
                )));

        grid.setDataProvider(dataProvider);
        // List<Var> vars = visibleColumns == null ? query.getProjectVars() : visibleColumns;
    }


    public static <T> Multimap<T, HeaderCell> setupHeaders(
            Grid<Binding> grid,
            HeaderRow primaryHeaderRow,
            Function<T, T> getParent,
            List<Entry<Column<?>, T>> leafs) {

        // Hierarchical headers can be repeated if paths are in arbitrary order
        Multimap<T, HeaderCell> pathToCells = HashMultimap.create();

        // Create the columns
        // HeaderRow primaryHeaderRow = grid.prependHeaderRow();


        List<Entry<T, HeaderCell>> headerLevel = new ArrayList<>();
        for (Entry<Column<?>, T> leaf : leafs) {
            T key = leaf.getValue();
            Column<?> column = leaf.getKey();
            HeaderCell cell = primaryHeaderRow.getCell(column);
            pathToCells.put(key, cell);

            headerLevel.add(new SimpleImmutableEntry<>(key, cell));
        }

        // Create the hierarchical header rows by moving from the leafs upwards
        List<Entry<T, HeaderCell>> currentLevel = headerLevel;
        while (currentLevel.stream().map(Entry::getKey).filter(path -> path != null && getParent.apply(path) != null).count() > 1l) { // As long as there are non null paths as headers

            // Group the children's cells by their respective parents
            List<Entry<T, List<Entry<T, HeaderCell>>>> groups =
                    StreamOperatorSequentialGroupBy.create(SequentialGroupBySpec.<Entry<T, HeaderCell>, T, List<Entry<T, HeaderCell>>>create(
                            entry -> entry.getKey() == null ? null : getParent.apply(entry.getKey()),
                            (x, y) -> x == null && y == null ? false : Objects.equals(x, y),
                            groupKey -> new ArrayList<>(), (acc, item) -> { acc.add(item); return acc; }))
                    .transform(currentLevel.stream())
                    .collect(Collectors.toList());

            // The new hrow will be modified in place
            HeaderRow hrow = grid.prependHeaderRow();
            List<HeaderCell> copy = new ArrayList<>(hrow.getCells());

            List<Entry<T, HeaderCell>> parentLevel = new ArrayList<>();
            int offset = 0;
            for (Entry<T, List<Entry<T, HeaderCell>>> group : groups) {
                List<HeaderCell> cells = group.getValue().stream().map(Entry::getValue).collect(Collectors.toList());
                // Listhrow.getCells()
                HeaderCell mergedCell;
                T groupKey = group.getKey();
                if (cells.size() > 1) {
                    int n = cells.size();
                    int nextOffset = offset + n;
                    List<HeaderCell> toMerge = copy.subList(offset, nextOffset);
                    offset = nextOffset;
                    mergedCell = hrow.join(toMerge);
                    parentLevel.add(new SimpleImmutableEntry<>(groupKey, mergedCell));
                } else {
                    mergedCell = copy.get(offset);
                    parentLevel.add(new SimpleImmutableEntry<>(groupKey, mergedCell));
                    ++offset;
                }

                if (groupKey != null) {
                    pathToCells.put(groupKey, mergedCell);
                }
            }

            currentLevel = parentLevel;
        }

        // Header<T> header = new Header<>(idToLevel, levelToIdToCell, levelToHeader);
        return pathToCells;
    }


    public static class Point implements Serializable {
        private static final long serialVersionUID = 1L;

        private final int x;
        private final int y;

        public Point(int x, int y) {
            super();
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        @Override
        public String toString() {
            return "Point [x=" + x + ", y=" + y + "]";
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Point other = (Point) obj;
            return x == other.x && y == other.y;
        }
    }

}

