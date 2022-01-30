package org.aksw.vaadin.jena.geo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.geojson.GeoJsonObject;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;

import com.vaadin.addon.leaflet4vaadin.LeafletMap;
import com.vaadin.addon.leaflet4vaadin.layer.groups.GeoJSON;
import com.vaadin.addon.leaflet4vaadin.layer.groups.LayerGroup;
import com.vaadin.addon.leaflet4vaadin.types.LatLngBounds;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.data.selection.SelectionListener;

/**
 * Utils for boilerplate that connects vaadin with spatial features
 * @author raven
 *
 */
public class VaadinGeoUtils {
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

                    Geometry geom = GeometryWrapper2.extractGeometryOrNull(node);
                    if (geom != null) {
                        detectedGeometries.add(geom);
                    }
                }
            }

            for (Geometry geom : detectedGeometries) {
                GeoJSON geoJson = toGeoJson(geom);
                geoJson.addTo(group);

            }

            if (!detectedGeometries.isEmpty()) {
                LatLngBounds bounds = JtsUtils.convert(JtsUtils.envelope(detectedGeometries));
                map.flyToBounds(bounds);
            }
        };
    }

    /** Discards values of nodes that are not geometries */
    public static List<Geometry> nodesToGeoms(Collection<Node> nodes) {
    	List<Geometry> result = nodes.stream()
    		.map(GeometryWrapper2::extractGeometryOrNull)
    		.filter(Objects::nonNull)
    		.collect(Collectors.toList());
    	return result;
    }
    
    public static LatLngBounds evelope(Node ... nodes) {
    	return evelope(Arrays.asList(nodes));
    }

    public static LatLngBounds evelope(Collection<Node> nodes) {
    	List<Geometry> geoms = nodesToGeoms(nodes);
        LatLngBounds result = JtsUtils.convert(JtsUtils.envelope(geoms));
        return result;
    }
    
    public static GeoJSON toGeoJson(Node node) {
    	Geometry geom = GeometryWrapper2.extractGeometryOrNull(node);
    	GeoJSON result = geom == null ? null : toGeoJson(geom);
    	return result;
    }

    public static GeoJSON toGeoJson(Geometry geom) {
    	GeoJsonObject gjo = JtsUtils.convert(geom);
        GeoJSON geoJson = new GeoJSON(gjo);
        if (geom instanceof LineString) {
            geoJson.getStyle().setFill(false);
            geoJson.setStyle(geoJson.getStyle());
        }
        GeoJSON result = new GeoJSON(gjo);
        return result;
    }

}
