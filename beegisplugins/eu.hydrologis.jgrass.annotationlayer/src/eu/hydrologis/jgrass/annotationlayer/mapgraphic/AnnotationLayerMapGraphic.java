/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.hydrologis.jgrass.annotationlayer.mapgraphic;

import java.awt.Color;
import java.util.List;

import net.refractions.udig.mapgraphic.MapGraphic;
import net.refractions.udig.mapgraphic.MapGraphicContext;
import net.refractions.udig.project.render.IViewportModel;
import net.refractions.udig.project.ui.ApplicationGIS;
import net.refractions.udig.ui.graphics.ViewportGraphics;

import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.widgets.Display;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

import eu.hydrologis.jgrass.annotationlayer.AnnotationPlugin;
import eu.hydrologis.jgrass.beegisutils.jgrassported.DressedWorldStroke;

/**
 * @author Andrea Antonello - www.hydrologis.com
 */
public class AnnotationLayerMapGraphic implements MapGraphic {

    private boolean doReporject = true;
    private Display display = Display.getDefault();
    private int priorW2PX = -1;
    private int priorW2PY = -1;
    private int jumped = 0;

    public void draw( MapGraphicContext context ) {
        try {
            IViewportModel viewportModel = ApplicationGIS.getActiveMap().getViewportModel();

            List<DressedWorldStroke> strokes = AnnotationPlugin.getDefault().getStrokes();
            if (strokes == null || strokes.size() == 0) {
                return;
            }

            priorW2PX = -1;
            priorW2PY = -1;
            jumped = 0;

            ViewportGraphics g = context.getGraphics();
            CoordinateReferenceSystem mapCrs = viewportModel.getCRS();
            for( DressedWorldStroke d : strokes ) {
                Double[] nodes = d.nodes;
                if (nodes.length < 4) {
                    // at least 2 coords for a line
                    continue;
                }
                Path p = new Path(display);
                int screenNodeNum = 0;
                CoordinateReferenceSystem annotationCrs = CRS.parseWKT(d.crsWKT);
                /*
                 * if the crs was different, we need to transform it
                 */
                // if (!mapCrs.equals(annotationCrs, false)) {
                if (!mapCrs.getName().equals(annotationCrs.getName())) {

                    // transform coordinates before check
                    MathTransform transform = CRS.findMathTransform(annotationCrs, mapCrs, true);
                    // first check if the stroke is inside the window
                    if (d.bounds != null) {
                        Envelope screenBounds = viewportModel.getBounds();
                        ReferencedEnvelope strokeEnvelopeTransformed = d.bounds.transform(mapCrs,
                                true);
                        if (!screenBounds.intersects(strokeEnvelopeTransformed)) {
                            continue;
                        }
                    }
                    // jts geometry
                    for( int i = 0; i < nodes.length; i = i + 2 ) {
                        Double x = nodes[i];
                        Double y = nodes[i + 1];
                        Coordinate newC = JTS.transform(new Coordinate(x, y), null, transform);
                        java.awt.Point worldToPixel = viewportModel.worldToPixel(newC);
                        if (worldToPixel.x == priorW2PX && worldToPixel.y == priorW2PY) {
                            // System.out.println(jumped++);
                            continue;
                        }
                        priorW2PX = worldToPixel.x;
                        priorW2PY = worldToPixel.y;
                        if (i == 0) {
                            p.moveTo(priorW2PX, priorW2PY);
                        } else {
                            p.lineTo(priorW2PX, priorW2PY);
                        }
                        screenNodeNum++;
                    }
                } else {
                    // no need to transform
                    if (d.bounds != null) {
                        Envelope screenBounds = viewportModel.getBounds();
                        if (!screenBounds.intersects(d.bounds)) {
                            return;
                        }
                    }
                    for( int i = 0; i < nodes.length; i = i + 2 ) {
                        Double x = nodes[i];
                        Double y = nodes[i + 1];
                        java.awt.Point worldToPixel = viewportModel.worldToPixel(new Coordinate(x,
                                y));
                        if (worldToPixel.x == priorW2PX && worldToPixel.y == priorW2PY) {
                            // System.out.println(jumped++);
                            continue;
                        }
                        priorW2PX = worldToPixel.x;
                        priorW2PY = worldToPixel.y;
                        if (i == 0) {
                            p.moveTo(priorW2PX, priorW2PY);
                        } else {
                            p.lineTo(priorW2PX, priorW2PY);
                        }
                        screenNodeNum++;
                    }
                }

                if (screenNodeNum > 1) {
                    int width = d.strokeWidth[0];
                    double mapScale = viewportModel.getScaleDenominator();
                    double lineScale = d.scale;
                    width = (int) Math.ceil(((double) width) * lineScale / mapScale);
                    g.setStroke(d.lineStyle[0], width);
                    int[] rgb = d.rgb;
                    g.setColor(new Color(rgb[0], rgb[1], rgb[2], rgb[3]));
                    g.drawPath(p);
                }
            }
        } catch (Exception e) {
            AnnotationPlugin
                    .log(
                            "AnnotationPlugin problem: eu.hydrologis.jgrass.annotationlayer.mapgraphic#AnnotationLayerMapGraphic#draw", e); //$NON-NLS-1$
            e.printStackTrace();
        }
    }
}
