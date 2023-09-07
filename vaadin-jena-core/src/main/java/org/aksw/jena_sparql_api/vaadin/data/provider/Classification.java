package org.aksw.jena_sparql_api.vaadin.data.provider;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.jena.graph.Node;

public class Classification {
    private static final Classification EMPTY = new Classification(Collections.emptySet());

    /** An immutable empty classification */
    public static Classification empty() {
        return EMPTY;
    }

    protected Set<Node> classes;

    public Classification() {
        this(new LinkedHashSet<>());
    }

    public Classification(Set<Node> classes) {
        super();
        this.classes = classes;
    }

    public Set<Node> getClasses() {
        return classes;
    }

    @Override
    public String toString() {
        return Objects.toString(classes);
    }


    @Override
    public int hashCode() {
        return Objects.hash(classes);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Classification other = (Classification) obj;
        return Objects.equals(classes, other.classes);
    }
}
