package org.aksw.vaadin.jena.geo.leafletflow;

import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.operation.union.UnaryUnionOp;

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
        return envelope(geoms.iterator());
    }

    public static Envelope envelope(Iterator<Geometry> geoms) {
        Envelope result = new Envelope();
        while (geoms.hasNext()) {
            Geometry geom = geoms.next();
            result.expandToInclude(geom.getEnvelopeInternal());
        }
        return result;
    }

    public static Envelope envelope(Geometry ... geoms) {
        Envelope result = Arrays.asList(geoms).stream().map(Geometry::getEnvelopeInternal).collect(unionEnvelope());
        return result;
    }

    public static Collector<Geometry, ?, Geometry> union() {
        return Collectors.collectingAndThen(Collectors.toList(), UnaryUnionOp::union);
    }

    public static Collector<Envelope, ?, Envelope> unionEnvelope() {
        return Collector.of(
            Envelope::new,
            Envelope::expandToInclude,
            (a, b) -> {
                a.expandToInclude(b);
                return a;
            }
        );
    }

    public static Collector<Geometry, ?, Envelope> unionEnvelopeGeometry() {
        return Collectors.mapping(Geometry::getEnvelopeInternal, unionEnvelope());
    }
}
