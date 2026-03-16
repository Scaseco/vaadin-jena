package org.aksw.vaadin.jena.geo.leafletflow;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import software.xdev.vaadin.maps.leaflet.basictypes.LLatLng;
import software.xdev.vaadin.maps.leaflet.basictypes.LLatLngBounds;
import software.xdev.vaadin.maps.leaflet.layer.LLayer;
import software.xdev.vaadin.maps.leaflet.layer.LLayerGroup;
import software.xdev.vaadin.maps.leaflet.layer.ui.LMarker;
import software.xdev.vaadin.maps.leaflet.layer.vector.LPolygon;
import software.xdev.vaadin.maps.leaflet.layer.vector.LPolyline;
import software.xdev.vaadin.maps.leaflet.registry.LComponentManagementRegistry;

/** Convert from JTS to vaadin-leaflet-map-flow */
public class JtsToLMapConverter {
    protected LComponentManagementRegistry reg;

    public JtsToLMapConverter(LComponentManagementRegistry reg) {
        super();
        this.reg = Objects.requireNonNull(reg);
    }

    public LLatLngBounds convert(Envelope envelope) {
        return new LLatLngBounds(reg,
            new LLatLng(reg, envelope.getMinY(), envelope.getMinX()),
            new LLatLng(reg, envelope.getMaxY(), envelope.getMaxX()));
    }

    public LLatLng convert(Coordinate coord) {
        return new LLatLng(reg, coord.y, coord.x);
    }

    public List<LLatLng> convert(Coordinate[] coord) {
        List<LLatLng> result = Arrays.asList(coord).stream()
                .map(this::convert)
                .collect(Collectors.toList());
        return result;
    }

    public LPolygon convertPolygon(Polygon polygon) {
        List<LLatLng> latLngs = convert(polygon.getCoordinates());
        return new LPolygon(reg, latLngs);
    }

    public LPolyline convertLineString(LineString polygon) {
        List<LLatLng> latLngs = convert(polygon.getCoordinates());
        return new LPolyline(reg, latLngs);
    }

    public LMarker convertPoint(Point point) {
        LLatLng latLng = convert(point.getCoordinate());
        return new LMarker(reg, latLng);
    }

    public LLayerGroup convertGeometryCollection(GeometryCollection gc) {
        LLayerGroup group = new LLayerGroup(reg);
        for (int i = 0; i < gc.getNumGeometries(); ++i) {
            Geometry g = gc.getGeometryN(i);
            LLayer<?> converted = convert(g);
            group.addLayer(converted);
        }
        return group;
    }

    public LLayer<?> convert(Geometry geom) {
        LLayer<?> result = null;
        if (geom instanceof Point point) {
            result = convertPoint(point);
        } else if (geom instanceof Polygon polygon) {
            result = convertPolygon(polygon);
        } else if (geom instanceof LineString lineString) {
            result = convertLineString(lineString);
        } else if (geom instanceof GeometryCollection gc) {
            result = convertGeometryCollection(gc);
        }
        return result;
    }
}
