package org.aksw.vaadin.app.demo.main;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.optimize.TransformEliminateAssignments;

public class MainShaclExperiments {
    public static void main(String[] args) {
        Query query = QueryFactory.create("SELECT DISTINCT ?s { ?s ?p ?o . FILTER(?p = <foo>)} LIMIT 10");
        Op op = Algebra.compile(query);

        op = TransformEliminateAssignments.eliminate(op, true);

        System.out.println(OpAsQuery.asQuery(op));
    }
}
