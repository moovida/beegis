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
package eu.hydrologis.jgrass.geonotes.mapgraphic;

import static eu.hydrologis.jgrass.geonotes.GeonoteConstants.GPS;
import static eu.hydrologis.jgrass.geonotes.GeonoteConstants.NORMAL;
import static eu.hydrologis.jgrass.geonotes.GeonoteConstants.PHOTO;
import static java.lang.Math.PI;

import java.awt.Color;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.util.HashMap;
import java.util.List;

import net.refractions.udig.mapgraphic.MapGraphic;
import net.refractions.udig.mapgraphic.MapGraphicContext;
import net.refractions.udig.project.IBlackboard;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;
import net.refractions.udig.ui.graphics.ViewportGraphics;

import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import eu.hydrologis.jgrass.beegisutils.database.annotatedclasses.GeonotesTable;
import eu.hydrologis.jgrass.geonotes.GeonotesHandler;
import eu.hydrologis.jgrass.geonotes.tools.GeoNoteSelectionTool;
import eu.hydrologis.jgrass.geonotes.util.ImageManager;

/**
 * The mapgraphic layer visualizing the geotones
 * 
 * @author Andrea Antonello - www.hydrologis.com
 */
public class GeonotesMapGraphic implements MapGraphic {
    public static final String ID = "eu.hydrologis.jgrass.geonotes.mapgraphic"; //$NON-NLS-1$

    private static java.awt.Image pinImage;
    private static java.awt.Image selectedPinImage;
    private static Image gpsPinImage;
    private static Image photoPinImage;
    private static HashMap<Integer, Image> types = new HashMap<Integer, Image>();

    private double deg3radFactor = 2.0 * PI / 360.0;

    static {
        pinImage = ImageManager.INSTANCE.getPinImage30();
        types.put(NORMAL, pinImage);
        selectedPinImage = ImageManager.INSTANCE.getSelectedPinImage30();
        gpsPinImage = ImageManager.INSTANCE.getGpsPinImage30();
        types.put(GPS, gpsPinImage);
        photoPinImage = ImageManager.INSTANCE.getPhotoPinImage30();
        types.put(PHOTO, photoPinImage);
    }

    public void draw( MapGraphicContext context ) {

        ViewportGraphics g = context.getGraphics();

        IMap map = context.getMap();
        IBlackboard blackboard = map.getBlackboard();
        Object object = blackboard.get(GeoNoteSelectionTool.SELECTIONID);
        ReferencedEnvelope[] refEnvelope = (ReferencedEnvelope[]) object;

        CoordinateReferenceSystem mapCrs = map.getViewportModel().getCRS();
        CoordinateReferenceSystem noteCrs = null;
        GeometryFactory gF = new GeometryFactory();
        Coordinate point = null;
        try {
            List<GeonotesTable> geonotesDbList = GeonotesHandler.getGeonotesTables();
            for( GeonotesTable dbGeonote : geonotesDbList ) {
                double east = dbGeonote.getEast();
                double north = dbGeonote.getNorth();
                point = new Coordinate(east, north);

                int type = dbGeonote.getType();
                noteCrs = dbGeonote.getGeonoteCrs();
                if (!CRS.equalsIgnoreMetadata(noteCrs, mapCrs)) {
                    // transform coordinates before check
                    MathTransform transform = CRS.findMathTransform(noteCrs, mapCrs, true);
                    // jts geometry
                    Point pt = gF.createPoint(new Coordinate(east, north));
                    Geometry targetGeometry = JTS.transform(pt, transform);
                    point = targetGeometry.getCoordinate();
                }

                Double azimuth = dbGeonote.getAzimut();
                java.awt.Point pointinPixel = context.worldToPixel(point);

                if (azimuth != null) {
                    GeneralPath arrow = new GeneralPath();
                    float length = 40f;
                    arrow.moveTo(0f, 0f);
                    arrow.lineTo(0f, -length);
                    arrow.lineTo(length / 12f, -length + length / 4f);
                    arrow.moveTo(0f, -length);
                    arrow.lineTo(-length / 12f, 0f - length + length / 4f);
                    AffineTransform aT = new AffineTransform();
                    aT.translate(pointinPixel.x, pointinPixel.y);
                    aT.rotate(azimuth * deg3radFactor);
                    Shape transformedShape = arrow.createTransformedShape(aT);

                    g.setStroke(ViewportGraphics.LINE_SOLID_ROUNDED, 2);
                    g.setColor(Color.BLACK);
                    g.draw(transformedShape);
                }

                boolean drawn = false;
                if (refEnvelope != null) {
                    for( ReferencedEnvelope referencedEnvelope : refEnvelope ) {
                        // draw the pin
                        if (referencedEnvelope != null && referencedEnvelope.contains(point)) {
                            // is selected
                            g.drawImage(selectedPinImage, pointinPixel.x - 2, pointinPixel.y - 15);
                            drawn = true;
                        }
                    }
                }
                if (!drawn) {
                    g.drawImage(types.get(type), pointinPixel.x - 2, pointinPixel.y - 15);
                }

            }

            context.getLayer().setStatus(ILayer.DONE);
            context.getLayer().setStatusMessage("Layer rendered");
        } catch (Exception e) {
            context.getLayer().setStatus(ILayer.ERROR);
            context.getLayer().setStatusMessage(e.getLocalizedMessage());
            e.printStackTrace();
        }

    }

}