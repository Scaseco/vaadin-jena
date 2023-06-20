package org.aksw.facete.v4.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete.v3.api.FacetPathMapping;
import org.aksw.facete.v3.api.TreeQuery;
import org.aksw.facete.v3.api.TreeQueryImpl;
import org.aksw.facete.v3.api.TreeQueryNode;
import org.aksw.jenax.arq.util.binding.TableUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetPathOps;
import org.aksw.jenax.path.core.FacetStep;
import org.aksw.jenax.sparql.relation.api.Relation;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.core.Var;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * A relation together with a tree structure much like a rooted construct query.
 */
class TreeRelation {
    protected Relation relation;
    protected TreeQuery tree;
    protected BiMap<TreeQueryNode, Var> pathToVar;
    // protected ListMultimap<TreeQueryNode, TreeQueryNode> foo;

    public TreeRelation(Relation relation, TreeQuery tree, BiMap<TreeQueryNode, Var> pathToVar) {
        super();
        this.relation = relation;
        this.tree = tree;
        this.pathToVar = pathToVar;
    }

    public static Node varToIri(Var var) {
        Node result = NodeFactory.createURI("urn:x-jenax:var:" + var.getName());
        return result;
    }

    /** Turn each variable of the relation into a property of the tree */
    public static TreeRelation of(Relation relation) {
        TreeQuery tree = new TreeQueryImpl();
        List<Var> vars = relation.getVars();
        BiMap<TreeQueryNode, Var> pathToVar = HashBiMap.create();
        for (Var v : vars) {
            Node property = varToIri(v);
            FacetPath facetPath = FacetPathOps.newRelativePath(FacetStep.fwd(property, null));
            TreeQueryNode varNode = tree.root().resolve(facetPath);
            pathToVar.put(varNode, v);
        }
        return new TreeRelation(relation, tree, pathToVar);
    }

    public static Table varsToTable(Collection<Var> vars) {
        List<Node> nodes = vars.stream().map(TreeRelation::varToIri).collect(Collectors.toList());
        Table result = TableUtils.createTable(Vars.p, nodes);
        return result;
    }
}

public class FacetedRelationQuery {
    protected Supplier<TreeRelation> baseRelation;
    protected Map<Var, TreeQueryNode> varToRoot;


    // An injective mapping of facet paths to sparql variables
    // TODO Paths need to eventually be resolved against variables - so probably we need to scope variables by their root.
    // Yet, we could use a common name for every path
    // So the first level of FacetNodes that refers to the root variables is different from the other FacetNodes. But how to capture that?!
    protected FacetPathMapping pathMapping;


    /**
     * Return a facet node whose facetValues query yields the variables of the baseRelation as IRIs
     * @return
     */
    public FacetNode superRoot() {
        return null;
    }
}


