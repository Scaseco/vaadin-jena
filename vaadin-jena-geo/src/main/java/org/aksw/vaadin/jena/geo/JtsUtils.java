package org.aksw.vaadin.jena.geo;

import com.google.common.collect.Streams;
import org.geojson.GeoJsonObject;
import org.locationtech.jts.algorithm.ConvexHull;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.geojson.GeoJsonWriter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.addon.leaflet4vaadin.types.LatLng;
import com.vaadin.addon.leaflet4vaadin.types.LatLngBounds;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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

	public static LatLngBounds convert(Envelope e) {
		return new LatLngBounds(
				new LatLng(e.getMinY(), e.getMinX()),
				new LatLng(e.getMaxY(), e.getMaxX()));
	}
	
	public static GeoJsonObject convert(Geometry geometry) {
		GeoJsonWriter geoJsonWriter = new GeoJsonWriter();
		String jsonStr = geoJsonWriter.write(geometry); 
		

	    ObjectMapper mapper = new ObjectMapper();
	    GeoJsonObject result;
		try {
			result = mapper.readValue(jsonStr, GeoJsonObject.class);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	public static GeoJsonObject convert(List<Geometry> geometries) {
		GeometryCollection collection = new GeometryFactory().createGeometryCollection(geometries.toArray(new Geometry[0]));
		GeoJsonWriter geoJsonWriter = new GeoJsonWriter();
		String jsonStr = geoJsonWriter.write(collection);

		ObjectMapper mapper = new ObjectMapper();
		GeoJsonObject result;
		try {
			result = mapper.readValue(jsonStr, GeoJsonObject.class);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
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
