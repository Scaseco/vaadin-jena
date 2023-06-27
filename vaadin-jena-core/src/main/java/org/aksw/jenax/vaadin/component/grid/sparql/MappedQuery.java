package org.aksw.jenax.vaadin.component.grid.sparql;

import java.util.Map;

import org.aksw.facete.v3.api.TreeData;
import org.aksw.jenax.path.core.FacetPath;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;

import com.google.common.collect.BiMap;

/**
 * A query with information about which variable corresponds to which path
 *
 */
// FIXME This should go into the DataQuery api - but DataQuery currently lacks the new PathPPA support.
public class MappedQuery {
    protected Query query;

    /** These are the paths that have a corresponding variable in the query's projection */
    protected BiMap<Var, FacetPath> varToPath;
    protected TreeData<FacetPath> tree;

    public MappedQuery(TreeData<FacetPath> tree, Query query, BiMap<Var, FacetPath> varToPath) {
        super();
        this.tree = tree;
        this.query = query;
        this.varToPath = varToPath;
    }

    public TreeData<FacetPath> getTree() {
        return tree;
    }

    public Map<Var, FacetPath> getVarToPath() {
        return varToPath;
    }

    public Map<FacetPath, Var> getPathToVar() {
        return varToPath.inverse();
    }

    public Query getQuery() {
        return query;
    }
}
