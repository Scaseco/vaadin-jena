package org.aksw.vaadin.jena.geo;

import java.util.List;

import org.geojson.GeoJsonObject;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.io.geojson.GeoJsonWriter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.addon.leaflet4vaadin.layer.groups.GeoJSON;

public class GeoJsonJtsUtils {

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

	public static GeoJSON toGeoJson(Geometry geom) {
		GeoJsonObject gjo = convert(geom);
	    GeoJSON geoJson = new GeoJSON(gjo);
	    if (geom instanceof LineString) {
	        geoJson.getStyle().setFill(false);
	        geoJson.setStyle(geoJson.getStyle());
	    }
	    GeoJSON result = new GeoJSON(gjo);
	    return result;
	}

}
