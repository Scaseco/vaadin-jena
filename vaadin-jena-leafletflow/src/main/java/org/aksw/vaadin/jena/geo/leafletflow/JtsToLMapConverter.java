package org.aksw.vaadin.jena.geo.leafletflow;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.aksw.commons.util.obj.ObjectUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import software.xdev.vaadin.maps.leaflet.basictypes.LLatLng;
import software.xdev.vaadin.maps.leaflet.basictypes.LLatLngBounds;
import software.xdev.vaadin.maps.leaflet.layer.LLayer;
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

    public LLayer<?> convert(Geometry geom) {
        LLayer<?> result = null;
        Polygon polygon = ObjectUtils.castAsOrNull(Polygon.class, geom);
        if (polygon != null) {
            result = convertPolygon(polygon);
        } else {
            LineString lineString = ObjectUtils.castAsOrNull(LineString.class, geom);
            if (lineString != null) {
                result = convertLineString(lineString);
            } else {
                Point point = ObjectUtils.castAsOrNull(Point.class, geom);
                if (point != null) {
                    result = convertPoint(point);
                }
            }
        }
        return result;
    }
}
