package org.aksw.vaadin.jena.geo.leafletflow;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

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

}
