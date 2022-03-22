package org.aksw.vaadin.app.demo.view.edit.resource;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.path.Path;

/**
 * Accumulator for paths that places them into disjoint bins depending
 * on their characteristics. Distinguishes: fwd/bwd predicates and
 * all other paths.
 *
 * TODO We could further discriminate between paths that can be
 * expanded to sparql 1.0 graph patterns
 *
 * @author raven
 *
 */
public class PathAcc {
    protected Set<Node> fwdPaths;
    protected Set<Node> bwdPaths;

    protected Set<Path> paths;


    public PathAcc() {
        this(new LinkedHashSet<>(), new LinkedHashSet<>(), new LinkedHashSet<>());
    }

    public PathAcc(Set<Node> fwdPaths, Set<Node> bwdPaths, Set<Path> paths) {
        super();
        this.fwdPaths = fwdPaths;
        this.bwdPaths = bwdPaths;
        this.paths = paths;
    }

    public void accumulate(Path path) {
        if (path instanceof P_Path0) {
            P_Path0 p0 = ((P_Path0) path);
            Node p = p0.getNode();
            if (p0.isForward()) {
                fwdPaths.add(p);
            } else {
                bwdPaths.add(p);
            }

        } else {
            paths.add(path);
        }
    }

    public Set<Node> getFwdPaths() {
        return fwdPaths;
    }

    public Set<Node> getBwdPaths() {
        return bwdPaths;
    }

    public Set<Path> getPaths() {
        return paths;
    }
}
