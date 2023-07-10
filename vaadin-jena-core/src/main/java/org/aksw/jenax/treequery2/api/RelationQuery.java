package org.aksw.jenax.treequery2.api;

import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

import org.aksw.facete.v4.impl.PropertyResolverImpl;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.sparql.relation.api.Relation;
import org.aksw.jenax.treequery2.impl.Partition;
import org.aksw.jenax.treequery2.impl.QueryContextImpl;
import org.aksw.jenax.treequery2.impl.RelationQueryImpl;
import org.aksw.jenax.treequery2.old.NodeQueryOld;
import org.apache.jena.sparql.core.Var;

public interface RelationQuery
    extends HasSlice
{
    /**
     * Returns the parent relation and a variable of it to which this relation connects.
     * Returns null if there is no parent
     */
    NodeQuery getParentNode();


    /** Unlink this RelationQuery from its parent.
     *  Returns this.
     *  {@link #getParentNode()} will return null after this operation.
     */
    // RelationQuery detach();

    // RelationQuery clone();

    /** Get the parent relation of this instance */
    // RelationQuery parent();

    // FIXME resolve naming issue with roots()
    default RelationQuery root() {
        NodeQuery parentNode = getParentNode();
        RelationQuery r = parentNode == null ? this : parentNode.relationQuery().root();
        return r;
    }

    /**
     * Returns the relation on which this RelationQuery is based.
     */
    Relation getRelation();

    /**
     * The context of this query which for example holds the PropertyResolver
     * for mapping properties to graph patterns.
     */
    QueryContext getContext();

    /**
     * Return this relation's partitioning, null if not partitioned.
     */
    Partition partition();

    /**
     * Declare this relation to be partitioned by the given paths.
     */
    void partitionBy(NodeQueryOld ... paths);


    /** Each row of a relation can be viewed as a node that has the column values as properties */
    NodeQueryOld asNode();

    /**
     * Return the facet path.
     * By convention, a facet step with the targetComponent set to null refers to a relation.
     * The 'targetComponent' field is null for all steps
     */
    FacetPath getPath();

    /**
     * Return each of the relation's columns as a QueryNode.
     */
    List<NodeQuery> roots();

    NodeQuery source();
    NodeQuery target();
    NodeQuery predicate();

    /** Obtain a {@link NodeQuery} for one of the variables of the relation returned by {@link #getRelation()}. */
    NodeQuery nodeFor(Var var);

    public static RelationQuery of(Relation relation) {
        return of(() -> relation);
    }

    public static RelationQuery of(Supplier<Relation> relation) {
        return of(relation, new QueryContextImpl(new PropertyResolverImpl()));
    }

//    public static RelationQuery of(Relation relation, QueryContext queryContext) {
//        return new RelationQueryImpl(() -> relation, queryContext);
//    }

    public static RelationQuery of(Supplier<Relation> relation, QueryContext queryContext) {
        return new RelationQueryImpl(null, relation, null, queryContext, new HashMap<>());
    }

}