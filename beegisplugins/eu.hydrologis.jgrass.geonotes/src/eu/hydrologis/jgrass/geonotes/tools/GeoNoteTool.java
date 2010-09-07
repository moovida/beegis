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
package eu.hydrologis.jgrass.geonotes.tools;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.ui.ApplicationGIS;
import net.refractions.udig.project.ui.commands.SelectionBoxCommand;
import net.refractions.udig.project.ui.render.displayAdapter.MapMouseEvent;
import net.refractions.udig.project.ui.tool.AbstractModalTool;
import net.refractions.udig.project.ui.tool.ModalTool;
import net.refractions.udig.ui.ExceptionDetailsDialog;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Display;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.hibernate.Criteria;
import org.hibernate.classic.Session;
import org.joda.time.DateTime;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import eu.hydrologis.jgrass.beegisutils.database.annotatedclasses.GeonotesTable;
import eu.hydrologis.jgrass.database.DatabasePlugin;
import eu.hydrologis.jgrass.geonotes.GeonoteConstants;
import eu.hydrologis.jgrass.geonotes.GeonoteConstants.NOTIFICATION;
import eu.hydrologis.jgrass.geonotes.GeonotesHandler;
import eu.hydrologis.jgrass.geonotes.GeonotesPlugin;
import eu.hydrologis.jgrass.geonotes.GeonotesUI;
import eu.hydrologis.jgrass.geonotes.fieldbook.FieldbookView;

/**
 * The {@link ModalTool tool} through which geonotes are opened or created.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class GeoNoteTool extends AbstractModalTool implements ModalTool {

    /**
     * ID of the current tool.
     */
    public static final String ID = "eu.hydrologis.jgrass.geonotes.tools.geonotetool"; //$NON-NLS-1$

    public static final String CATEGORY_ID = "eu.hydrologis.jgrass.geonotes.category"; //$NON-NLS-1$

    private SelectionBoxCommand draw = new SelectionBoxCommand();

    private int startX;

    private int startY;

    private ReferencedEnvelope selectionBox;

    /**
     * Creates an Geonotes {@link ModalTool tool}.
     */
    public GeoNoteTool() {
        super(MOUSE | MOTION);
    }

    public void mousePressed( MapMouseEvent e ) {
        startX = e.x;
        startY = e.y;
        /*
         *  make sure context.getViewportPane().repaint()
         *  knows about us.
         */
        draw.setValid(true);
        context.sendASyncCommand(draw);
        // draw.setShape(new Rectangle(e.x - 3, e.y - 3, 5, 5));
        // context.getViewportPane().repaint(e.x - 4, e.y - 4, 7, 7);
        draw.setShape(new Rectangle(startX, startY, 1, 1));
        context.getViewportPane().repaint(startX, startY, 1, 1);
    }

    public void mouseDragged( MapMouseEvent e ) {
        int currentX = e.x;
        int currentY = e.y;

        int x1 = currentX > startX ? startX : currentX;
        int y1 = currentY > startY ? startY : currentY;
        int width = Math.abs(startX - currentX);
        int height = Math.abs(startY - currentY);

        draw.setShape(new Rectangle(x1, y1, width, height));
        context.getViewportPane().repaint(x1, y1, width, height);
    }

    public void mouseReleased( final MapMouseEvent e ) {
        int endX = e.x;
        int endY = e.y;

        final CoordinateReferenceSystem mapCrs = ApplicationGIS.getActiveMap().getViewportModel().getCRS();

        if (endX == startX && endY == startY) {
            selectionBox = context.getBoundingBox(e.getPoint(), 5);
        } else {
            Coordinate startCoordinate = context.pixelToWorld(startX, startY);
            Coordinate endCoordinate = context.pixelToWorld(endX, endY);

            selectionBox = new ReferencedEnvelope(new Envelope(startCoordinate, endCoordinate), mapCrs);
        }

        final Display display = Display.getDefault();
        display.asyncExec(new Runnable(){

            public void run() {

                GeometryFactory gF = new GeometryFactory();
                List<GeonotesTable> selectedGeonotesTable = new ArrayList<GeonotesTable>();
                Coordinate point = null;
                Session session = null;
                GeonotesHandler geonotesHandler = null;
                try {
                    session = DatabasePlugin.getDefault().getActiveDatabaseConnection().openSession();
                    Criteria criteria = session.createCriteria(GeonotesTable.class);
                    List<GeonotesTable> geonotesDbList = criteria.list();
                    for( GeonotesTable dbGeonote : geonotesDbList ) {
                        CoordinateReferenceSystem noteCrs = dbGeonote.getGeonoteCrs();
                        double east = dbGeonote.getEast();
                        double north = dbGeonote.getNorth();
                        point = new Coordinate(east, north);
                        if (!CRS.equalsIgnoreMetadata(noteCrs, mapCrs)) {
                            // transform coordinates before check
                            MathTransform transform = CRS.findMathTransform(noteCrs, mapCrs, true);
                            // jts geometry
                            Point pt = gF.createPoint(new Coordinate(east, north));
                            Geometry targetGeometry = JTS.transform(pt, transform);
                            point = targetGeometry.getCoordinate();
                        }
                        if (selectionBox.contains(point)) {
                            selectedGeonotesTable.add(dbGeonote);
                        }

                    }

                    String projectName = ApplicationGIS.getActiveProject().getName();
                    String mapName = ApplicationGIS.getActiveMap().getName();

                    // new notes go into the db as lat/long
                    MathTransform transform = CRS.findMathTransform(mapCrs, GeonoteConstants.DEFAULT_GEONOTE_CRS, true);
                    Point pt = gF.createPoint(context.pixelToWorld(e.x, e.y));
                    Geometry latlongGeometry = JTS.transform(pt, transform);
                    Coordinate latlongPoint = latlongGeometry.getCoordinate();

                    if (selectedGeonotesTable.size() == 0) {
                        // create a new note
                        geonotesHandler = new GeonotesHandler(latlongPoint.x, latlongPoint.y, mapName + " - " + projectName,
                                null, null, new DateTime(), null, null, null, null);

                        GeonotesUI geonoteUI = new GeonotesUI(geonotesHandler);
                        geonoteUI.openInShell(null);

                        FieldbookView fieldBookView = GeonotesPlugin.getDefault().getFieldbookView();
                        if (fieldBookView != null) {
                            geonotesHandler.addObserver(fieldBookView);
                        }
                        geonotesHandler.notifyObservers(NOTIFICATION.NOTEADDED);
                    } else {
                        // open the selected ones
                        for( GeonotesTable geonotesTable : selectedGeonotesTable ) {
                            if (geonotesTable != null) {
                                geonotesHandler = new GeonotesHandler(geonotesTable);
                            } else {
                                geonotesHandler = new GeonotesHandler(latlongPoint.x, latlongPoint.y, mapName + " - "
                                        + projectName, null, null, new DateTime(), null, null, null, null);
                            }

                            GeonotesUI geonoteUI = new GeonotesUI(geonotesHandler);
                            geonoteUI.openInShell(null);

                            FieldbookView fieldBookView = GeonotesPlugin.getDefault().getFieldbookView();
                            if (fieldBookView != null) {
                                geonotesHandler.addObserver(fieldBookView);
                            }
                        }
                    }

                } catch (Exception e) {
                    String message = "An error occurred while opening the geonote.";
                    ExceptionDetailsDialog.openError(null, message, IStatus.ERROR, GeonotesPlugin.PLUGIN_ID, e);
                } finally {
                    session.close();
                    draw.setValid(false);
                    ILayer geonotesLayer = GeonotesPlugin.getDefault().getGeonotesLayer();
                    geonotesLayer.refresh(geonotesHandler.getBoundsAsReferenceEnvelope(ApplicationGIS.getActiveMap()
                            .getViewportModel().getCRS()));
                }
            }

        });
    }
    public void dispose() {
        super.dispose();
    }

    public void setActive( boolean active ) {
        if (active) {
            GeonotesPlugin.getDefault().getGeonotesLayer();
        }
        // else {
        // IMap map = ApplicationGIS.getActiveMap();
        // IBlackboard blackboard = map.getBlackboard();
        // selectionBox = new ReferencedEnvelope(new Envelope(new Coordinate(0, 0),
        // new Coordinate(0.00001, 0.00001)), map.getViewportModel().getCRS());
        // blackboard
        // .put(GeoNoteSelectionTool.SELECTIONID, new ReferencedEnvelope[]{selectionBox});
        // }
        super.setActive(active);
    }

}
