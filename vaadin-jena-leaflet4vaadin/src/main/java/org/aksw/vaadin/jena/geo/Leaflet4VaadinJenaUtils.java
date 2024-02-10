package org.aksw.vaadin.jena.geo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.aksw.jena_sparql_api.sparql.ext.geosparql.GeometryWrapperUtils;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import com.vaadin.addon.leaflet4vaadin.LeafletMap;
import com.vaadin.addon.leaflet4vaadin.layer.groups.GeoJSON;
import com.vaadin.addon.leaflet4vaadin.layer.groups.LayerGroup;
import com.vaadin.addon.leaflet4vaadin.types.LatLng;
import com.vaadin.addon.leaflet4vaadin.types.LatLngBounds;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.data.selection.SelectionListener;

public class Leaflet4VaadinJenaUtils {

    public static LatLng extractLatLng(Node node) {
        GeometryWrapper w = GeometryWrapper.extract(node);
        Geometry geom = w.getParsingGeometry();
        return Leaflet4VaadinJtsUtils.extractLatLng(geom);
    }

    /**
     * Create a generic listener on a grid of bindings that adds any geometry data in the selection
     * to a map and zooms the map to the bounding box of all detected geometries
     *
     * @param <C>
     * @param map
     * @param group
     * @return
     */
    public static <C extends Component> SelectionListener<C, Binding> createGridListener(LeafletMap map, LayerGroup group) {
        return ev -> {
            group.clearLayers();

            List<Geometry> detectedGeometries = new ArrayList<>();

            for (Binding b : ev.getAllSelectedItems()) {
                Iterator<Var> it = b.vars();
                while (it.hasNext()) {
                    Var v = it.next();
                    Node node = b.get(v);

                    Geometry geom = GeometryWrapperUtils.extractWgs84GeometryOrNull(node);
                    if (geom != null) {
                        detectedGeometries.add(geom);
                    }
                }
            }

            for (Geometry geom : detectedGeometries) {
                GeoJSON geoJson = GeoJsonJtsUtils.toGeoJson(geom);
                geoJson.addTo(group);

            }

            if (!detectedGeometries.isEmpty()) {
                LatLngBounds bounds = Leaflet4VaadinJtsUtils.convert(JtsUtils.envelope(detectedGeometries));
                map.flyToBounds(bounds);
            }
        };
    }

    public static LatLngBounds evelope(Node ... nodes) {
        return Leaflet4VaadinJenaUtils.evelope(Arrays.asList(nodes));
    }

    public static LatLngBounds evelope(Collection<Node> nodes) {
        List<Geometry> geoms = GeometryWrapperUtils.nodesToGeoms(nodes);
        LatLngBounds result = Leaflet4VaadinJtsUtils.convert(JtsUtils.envelope(geoms));
        return result;
    }

    public static LatLngBounds getWgs84Envelope(GeometryWrapper gw) {
        Envelope envelope = GeometryWrapperUtils.toWgs84(gw).getXYGeometry().getEnvelopeInternal();
        LatLngBounds result = Leaflet4VaadinJtsUtils.convert(envelope);
        return result;
    }
}
