package org.aksw.vaadin.jena.geo.leafletflow;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.aksw.commons.util.obj.ObjectUtils;
import org.aksw.jenax.arq.util.node.NodeUtils;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.data.selection.SelectionListener;

import software.xdev.vaadin.maps.leaflet.basictypes.LLatLngBounds;
import software.xdev.vaadin.maps.leaflet.layer.LLayer;
import software.xdev.vaadin.maps.leaflet.layer.LLayerGroup;
import software.xdev.vaadin.maps.leaflet.layer.vector.LPath;
import software.xdev.vaadin.maps.leaflet.layer.vector.LPolylineOptions;
import software.xdev.vaadin.maps.leaflet.map.LMap;

public class ResultSetMapRendererL {
    public static Set<Geometry> addBindingsToLayer(JtsToLMapConverter converter, LLayerGroup group, Binding binding) {
        return addBindingsToLayer(converter, group, List.of(binding));
    }

    public static Set<Geometry> addBindingsToLayer(JtsToLMapConverter converter, LLayerGroup group, Collection<Binding> bindings) {
        Set<Geometry> detectedGeometries = new LinkedHashSet<>();
        for (Binding b : bindings) {
            addBindingToLayer(converter, group, b, detectedGeometries);
        }
        return detectedGeometries;
    }

    public static void addBindingToLayer(JtsToLMapConverter converter, LLayerGroup group, Binding b, Set<Geometry> detectedGeometries) {
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

                LLayer<?> layer = converter.convert(geom);
                if (layer != null) {
                    // GeoJsonObject gjo = JtsUtils.convert(geom);
                    LPolylineOptions options = new LPolylineOptions();
                    // GeoJSON geoJson = new GeoJSON(gjo);
                    boolean fill = !(geom instanceof LineString || geom instanceof MultiLineString);
                    options.setFill(fill);

                    // Color
                    String colorVarName = v.getName() + "Color";
                    Node color = b.get(colorVarName);
                    if (color != null) {
                        String colorStr = NodeUtils.getUnquotedForm(color);
                        options.setColor(colorStr);
                        // geoJson.getStyle().setColor(colorStr);

                        //if (!(geom instanceof LineString)) {
                            options.setFillColor(colorStr);
                        //}
                    }

                    // Label (apparently not supported by vaadin plugin)
                    String labelVarName = v.getName() + "Label";
                    Node label = b.get(labelVarName);
                    if (label != null) {
                        String tooltipStr = NodeUtils.getUnquotedForm(label);
                        layer.bindPopup(tooltipStr);
                    }

                    // Tooltip
                    String tooltipVarName = v.getName() + "Tooltip";
                    Node tooltip = b.get(tooltipVarName);
                    if (tooltip != null) {
                        String tooltipStr = NodeUtils.getUnquotedForm(tooltip);
                        layer.bindTooltip(tooltipStr);
                    }

                    LPath<?> path = ObjectUtils.castAsOrNull(LPath.class, layer);
                    if (path != null) {
                        path.setStyle(options);
                    }

                    // geoJson.setStyle(geoJson.getStyle());
                    layer.addTo(group);
                }
            }
        }
    }

    public static void addAndFly(JtsToLMapConverter converter, LMap map, LLayerGroup group, Collection<Binding> bindings) {
        Set<Geometry> detectedGeometries = addBindingsToLayer(converter, group, bindings);
        if (!detectedGeometries.isEmpty()) {
            LLatLngBounds bounds = converter.convert(JtsUtils.envelope(detectedGeometries));
            map.flyToBounds(bounds);
            // Setting options is currently broken:
            //   https://github.com/xdev-software/vaadin-maps-leaflet-flow/issues/330
//            LMapZoomPanOptions opts = new LMapZoomPanOptions();
//            opts.setDuration(1.0);
//            map.flyToBounds(bounds, opts);
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
    public static <C extends Component> SelectionListener<C, Binding> createGridListener(
            JtsToLMapConverter converter, LMap map, LLayerGroup group) {
        return ev -> {
            group.clearLayers();
            addAndFly(converter, map, group, ev.getAllSelectedItems());
        };
    }
}
