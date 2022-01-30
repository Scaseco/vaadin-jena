package org.aksw.vaadin.jena.geo;

import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.NodeValue;
import org.locationtech.jts.geom.Geometry;

/** TODO This class should be provided by a module such as jenax-arq-plugins-geosparqlx ; but the module has not been created yet */
public class GeometryWrapper2 {

	public static Geometry extractGeometryOrNull(NodeValue nv) {
		Geometry result = null;
		try {
			GeometryWrapper wrapper = GeometryWrapper.extract(nv);
			result = wrapper.getParsingGeometry();
		} catch (Exception e) {
			// Nothing to do
		}
		
		return result;
	}
	
	public static Geometry extractGeometryOrNull(Node node) {
		Geometry result = null;
		try {
			GeometryWrapper wrapper = GeometryWrapper.extract(node);
			result = wrapper.getParsingGeometry();
		} catch (Exception e) {
			// Nothing to do
		}
		
		return result;
	}

}
