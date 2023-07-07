package org.aksw.jenax.treequery2;

import java.util.Collection;
import java.util.List;

import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetStep;
import org.apache.jena.graph.Node;
import org.apache.jena.query.SortCondition;

public interface QueryNode
    extends HasSlice
{
    QueryNode getParent();

    default QueryNode getRoot() {
        QueryNode parent = getParent();
        return parent == null ? this : parent.getRoot();
    }

    FacetPath getPath();

    // List<FacetStep> getChildren();
    Collection<QueryNode> getChildren();


    OrderNode order();

    default QueryNode fwd(String property) {
        return getOrCreateChild(FacetStep.fwd(property));
    }

    default QueryNode fwd(Node property) {
        return getOrCreateChild(FacetStep.fwd(property));
    }

    default QueryNode bwd(String property) {
        return getOrCreateChild(FacetStep.bwd(property));
    }

    default QueryNode bwd(Node property) {
        return getOrCreateChild(FacetStep.bwd(property));
    }

    /** Returns null if there is no child reachable with the given step. */
    QueryNode getChild(FacetStep step);
    QueryNode getOrCreateChild(FacetStep step);

    // NodeQuery getSubQuery(); // Filter the set of resources

    List<SortCondition> getSortConditions();

    // Issue: Limit and offset only generally make sense for relations but not for individual variables of a relation
    // a) maybe navigate from a node to the underlying relation such as queryNode().getRelation().limit() ?
    // b) navigate from the relation to the node - relationNode().limit().getTargetNode()
    // Well, actually the limit / offset methods of this node could just be shortcuts for getRelation().limit()

    @Override
    QueryNode offset(Long offset);

    @Override
    QueryNode limit(Long limit);
}
