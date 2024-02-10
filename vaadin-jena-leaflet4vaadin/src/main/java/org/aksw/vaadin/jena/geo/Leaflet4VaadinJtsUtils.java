package org.aksw.vaadin.jena.geo;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

import com.vaadin.addon.leaflet4vaadin.types.LatLng;
import com.vaadin.addon.leaflet4vaadin.types.LatLngBounds;

public class Leaflet4VaadinJtsUtils {

    public static LatLng extractLatLng(Geometry g) {
        LatLng result = null;
        if (g instanceof Point) {
            Point p = (Point)g;
            result = new LatLng(p.getY(), p.getX());
        }
        return result;
    }

    public static LatLngBounds convert(Envelope e) {
        return new LatLngBounds(
                new LatLng(e.getMinY(), e.getMinX()),
                new LatLng(e.getMaxY(), e.getMaxX()));
    }

    public static Envelope convert(LatLngBounds e) {
        return new Envelope(e.getWest(), e.getEast(), e.getSouth(), e.getNorth());
    }

}
