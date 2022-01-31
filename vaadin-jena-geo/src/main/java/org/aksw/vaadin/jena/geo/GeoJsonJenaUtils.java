package org.aksw.vaadin.jena.geo;

import org.aksw.jena_sparql_api.sparql.ext.geosparql.GeometryWrapperUtils;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.locationtech.jts.geom.Geometry;

import com.vaadin.addon.leaflet4vaadin.layer.groups.GeoJSON;

public class GeoJsonJenaUtils {

	public static GeoJSON toWgs84GeoJson(GeometryWrapper gw) {
		Geometry geom = GeometryWrapperUtils.getWgs84Geometry(gw);
		GeoJSON result = geom == null ? null : GeoJsonJtsUtils.toGeoJson(geom);
		return result;
	}

}
