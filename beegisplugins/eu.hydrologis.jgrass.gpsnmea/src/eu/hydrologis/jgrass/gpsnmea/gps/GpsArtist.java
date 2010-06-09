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
package eu.hydrologis.jgrass.gpsnmea.gps;

import java.awt.Point;

import net.refractions.udig.project.IMap;
import net.refractions.udig.project.ui.ApplicationGIS;
import net.refractions.udig.project.ui.tool.IToolContext;

import com.vividsolutions.jts.geom.Coordinate;

import eu.hydrologis.jgrass.gpsnmea.GpsActivator;
import eu.hydrologis.jgrass.gpsnmea.animation.GpsPositionDrawCommand;

/**
 * Class to take care of all the drawing and updating of gps data
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class GpsArtist {

    private GpsPositionDrawCommand gpsPositionDrawCommand;

    /**
     * Draws a point in the supplied {@link GpsPoint} on the screen.
     * 
     * @param gpsPoint the gps point to draw.
     */
    public void blink( GpsPoint gpsPoint ) {
        if (!GpsActivator.getDefault().isGpsLogging()) {
            return;
        }
        try {
            // to draw we need the screen coordinates
            final IMap activeMap = ApplicationGIS.getActiveMap();
            Point p = activeMap.getViewportModel().worldToPixel(gpsPoint.reproject(null));
            if (gpsPositionDrawCommand != null) {
                gpsPositionDrawCommand.setValid(false);
            }
            gpsPositionDrawCommand = new GpsPositionDrawCommand(p, gpsPoint.angle, GpsActivator
                    .getDefault().isInAutomaticMode());
            gpsPositionDrawCommand.setValid(true);
            IToolContext toolContext = ApplicationGIS.createContext(activeMap);
            toolContext.sendASyncCommand(gpsPositionDrawCommand);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clear() {
        if (gpsPositionDrawCommand != null)
            gpsPositionDrawCommand.setValid(false);
    }
}
