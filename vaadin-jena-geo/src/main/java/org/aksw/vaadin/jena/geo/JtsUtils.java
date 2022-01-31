package org.aksw.vaadin.jena.geo;

import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import com.google.common.collect.Streams;

public class JtsUtils {
	
	/**
	 * Compute an envelope for the given set of geometries
	 * 
	 * https://stackoverflow.com/questions/8520692/minimal-bounding-rectangle-with-jts
	 * 
	 * Note: Use of getEnvelopeInternal() ignores the precision model of the geometry.
	 * 
	 * @param geoms
	 * @return
	 */
	public static Envelope envelope(Iterable<Geometry> geoms) {
		Envelope result = new Envelope();

		for (Geometry geom : geoms) {
			result.expandToInclude(geom.getEnvelopeInternal());
		}
		return result;
	}

	public static Point computeCentroid(List<Point> points, List<Double> weights) {
		if (points.size() != weights.size()) {
			throw new IllegalArgumentException("list of points and weights do not have the same size");
		}

		double m = weights.stream().mapToDouble(Double::doubleValue).sum();
		double x = Streams.zip(points.stream(), weights.stream(), (p, w) -> p.getX() * w).mapToDouble(Double::doubleValue).sum() / m;
		double y = Streams.zip(points.stream(), weights.stream(), (p, w) -> p.getY() * w).mapToDouble(Double::doubleValue).sum() / m;

		Point centroid = new GeometryFactory().createPoint(new Coordinate(x, y));

		return centroid;
	}

	public static Point computeCentroid(List<Point> points) {
		double m = points.size();
		double x = points.stream().map(Point::getX).mapToDouble(Double::doubleValue).sum() / m;
		double y = points.stream().map(Point::getY).mapToDouble(Double::doubleValue).sum() / m;

		Point centroid = new GeometryFactory().createPoint(new Coordinate(x, y));

		return centroid;
	}

	public static Point computeCentroidForConvexHull(List<Point> points) {
		GeometryCollection geometry = new GeometryFactory().createGeometryCollection(points.toArray(new Geometry[0]));
		Geometry hull = geometry.convexHull();
		Point centroid = hull.getCentroid();
		return centroid;
	}

}
