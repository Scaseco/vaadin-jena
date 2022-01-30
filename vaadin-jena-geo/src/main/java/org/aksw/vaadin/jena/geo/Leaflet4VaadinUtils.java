package org.aksw.vaadin.jena.geo;

import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.graph.Node;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

import com.vaadin.addon.leaflet4vaadin.types.LatLng;

public class Leaflet4VaadinUtils {
	
	public static LatLng extractLatLng(Geometry g) {
		LatLng result = null;
		if (g instanceof Point) {
			Point p = (Point)g;
			result = new LatLng(p.getY(), p.getX());
		}
		return result;
	}
	
	public static LatLng extractLatLng(Node node) {
		GeometryWrapper w = GeometryWrapper.extract(node);
		Geometry geom = w.getParsingGeometry();
		return extractLatLng(geom);
	}
}
