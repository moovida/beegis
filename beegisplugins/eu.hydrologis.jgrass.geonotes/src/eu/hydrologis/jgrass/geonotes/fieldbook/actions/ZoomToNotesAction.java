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
package eu.hydrologis.jgrass.geonotes.fieldbook.actions;

import java.util.List;
import java.util.Properties;

import net.refractions.udig.project.IMap;
import net.refractions.udig.project.command.factory.NavigationCommandFactory;
import net.refractions.udig.project.ui.ApplicationGIS;

import org.eclipse.jface.action.Action;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import eu.hydrologis.jgrass.geonotes.GeonotesHandler;
import eu.hydrologis.jgrass.geonotes.GeonotesUI;
import eu.hydrologis.jgrass.geonotes.fieldbook.GeonotesListViewer;

public class ZoomToNotesAction extends Action {

    private final GeonotesListViewer geonotesViewer;
    private GeometryFactory gF = new GeometryFactory();

    public ZoomToNotesAction( GeonotesListViewer geonotesViewer ) {
        super("Zoom to geonotes");
        this.geonotesViewer = geonotesViewer;
    }

    public void run() {
        List<GeonotesHandler> currentGeonotesSelection = geonotesViewer.getCurrentGeonotesSelection();
        IMap map = ApplicationGIS.getActiveMap();
        if (map.getMapLayers().size() < 1) {
            return;
        }

        ReferencedEnvelope bounds = map.getViewportModel().getBounds();
        CoordinateReferenceSystem mapCrs = map.getViewportModel().getCRS();

        try {
            for( int i = 0; i < currentGeonotesSelection.size(); i++ ) {
                GeonotesHandler geonoteHandler = currentGeonotesSelection.get(i);
                Coordinate position = geonoteHandler.getPosition();
                String noteCrsString = geonoteHandler.getCrsWkt();
                if (!mapCrs.toWKT().trim().equals(noteCrsString.trim())) {
                    CoordinateReferenceSystem noteCrs = CRS.parseWKT(noteCrsString);
                    // transform coordinates before check
                    MathTransform transform = CRS.findMathTransform(noteCrs, mapCrs, true);
                    // jts geometry
                    Point pt = gF.createPoint(new Coordinate(position.x, position.y));
                    Geometry targetGeometry = JTS.transform(pt, transform);
                    position = targetGeometry.getCoordinate();
                }

                if (i == 0) {
                    Coordinate centre = bounds.centre();
                    double xTrans = position.x - centre.x;
                    double yTrans = position.y - centre.y;

                    bounds.translate(xTrans, yTrans);
                } else {
                    bounds.expandToInclude(position);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        map.sendCommandASync(NavigationCommandFactory.getInstance().createSetViewportBBoxCommand(
                bounds));

    }
}
