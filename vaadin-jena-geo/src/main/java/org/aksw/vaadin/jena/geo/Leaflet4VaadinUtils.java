package org.aksw.vaadin.jena.geo;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.geojson.GeoJsonObject;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;

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
public class Leaflet4VaadinUtils {

    public static void addBindingToLayer(LayerGroup group, Binding b, Set<Geometry> detectedGeometries) {
        Iterator<Var> it = b.vars();
        while (it.hasNext()) {
            Var v = it.next();
            Node node = b.get(v);

            Geometry geom;
            try {
                geom = GeometryWrapper.extract(node).getParsingGeometry();
            } catch (Exception e) {
                geom = null;
            }

            if (geom != null) {

                if (detectedGeometries != null) {
                    detectedGeometries.add(geom);
                }

                GeoJsonObject gjo = JtsUtils.convert(geom);
                GeoJSON geoJson = new GeoJSON(gjo);
                if (geom instanceof LineString || geom instanceof MultiLineString) {
                    geoJson.getStyle().setFill(false);
                }

                // Color
                String colorVarName = v.getName() + "Color";
                Node color = b.get(colorVarName);
                if (color != null) {
                    String colorStr = color.getLiteralLexicalForm();
                    geoJson.getStyle().setColor(colorStr);

                    //if (!(geom instanceof LineString)) {
                        geoJson.getStyle().setFillColor(colorStr);
                    //}
                }

                // Label (apparently not supported by vaadin plugin)
//                String labelVarName = v.getName() + "Label";
//                Node label = b.get(labelVarName);
//                if (label != null) {
//                    String tooltipStr = color.getLiteralLexicalForm();
//                }

                // Tooltip
                String tooltipVarName = v.getName() + "Tooltip";
                Node tooltip = b.get(tooltipVarName);
                if (tooltip != null) {
                    String tooltipStr = tooltip.getLiteralLexicalForm();
                    geoJson.bindPopup(tooltipStr);
                }


                geoJson.setStyle(geoJson.getStyle());
                geoJson.addTo(group);
            }
        }
    }

    public static void addAndFly(LeafletMap map, LayerGroup group, Collection<Binding> bindings) {
        Set<Geometry> detectedGeometries = new LinkedHashSet<>();
        for (Binding b : bindings) {
            addBindingToLayer(group, b, detectedGeometries);
        }

        if (!detectedGeometries.isEmpty()) {
            LatLngBounds bounds = JtsUtils.convert(JtsUtils.envelope(detectedGeometries));
            map.flyToBounds(bounds);
        }
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
            addAndFly(map, group, ev.getAllSelectedItems());
        };
    }
}
