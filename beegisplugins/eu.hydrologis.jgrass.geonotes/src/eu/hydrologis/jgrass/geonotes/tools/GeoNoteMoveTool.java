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
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;
import org.joda.time.DateTime;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import eu.hydrologis.jgrass.beegisutils.database.annotatedclasses.GeonotesTable;
import eu.hydrologis.jgrass.database.DatabasePlugin;
import eu.hydrologis.jgrass.geonotes.GeonoteConstants;
import eu.hydrologis.jgrass.geonotes.GeonotesHandler;
import eu.hydrologis.jgrass.geonotes.GeonotesPlugin;
import eu.hydrologis.jgrass.geonotes.GeonotesUI;
import eu.hydrologis.jgrass.geonotes.GeonoteConstants.NOTIFICATION;
import eu.hydrologis.jgrass.geonotes.fieldbook.FieldbookView;

/**
 * The {@link ModalTool tool} through which geonotes are opened or created.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class GeoNoteMoveTool extends AbstractModalTool implements ModalTool {

    /**
     * ID of the current tool.
     */
    public static final String ID = "eu.hydrologis.jgrass.geonotes.tools.geonotemovetool"; //$NON-NLS-1$

    public static final String CATEGORY_ID = "eu.hydrologis.jgrass.geonotes.category"; //$NON-NLS-1$

    private ReferencedEnvelope selectionBox;

    private GeonotesTable selectedGeonote = null;
    private Session session = null;

    private ILayer geonotesLayer;

    private static GeometryFactory gF = new GeometryFactory();

    private MathTransform inverseTransform;

    /**
     * Move a Geonote {@link ModalTool tool}.
     */
    public GeoNoteMoveTool() {
        super(MOUSE | MOTION);
    }

    public void mousePressed( MapMouseEvent e ) {
        final CoordinateReferenceSystem mapCrs = ApplicationGIS.getActiveMap().getViewportModel().getCRS();
        selectionBox = context.getBoundingBox(e.getPoint(), 25);

        try {
            Criteria criteria = session.createCriteria(GeonotesTable.class);
            List<GeonotesTable> geonotesDbList = criteria.list();
            for( GeonotesTable dbGeonote : geonotesDbList ) {
                double east = dbGeonote.getEast();
                double north = dbGeonote.getNorth();
                Coordinate reprojectedCoordinate = new Coordinate(east, north);
                CoordinateReferenceSystem noteCrs = dbGeonote.getGeonoteCrs();
                if (!CRS.equalsIgnoreMetadata(noteCrs, mapCrs)) {
                    // transform coordinates before check
                    MathTransform transform = CRS.findMathTransform(noteCrs, mapCrs, true);
                    inverseTransform = CRS.findMathTransform(mapCrs, noteCrs, true);
                    // jts geometry
                    Point pt = gF.createPoint(new Coordinate(east, north));
                    Geometry targetGeometry = JTS.transform(pt, transform);
                    reprojectedCoordinate = targetGeometry.getCoordinate();
                }
                if (selectionBox.contains(reprojectedCoordinate)) {
                    selectedGeonote = dbGeonote;
                    break;
                }
            }

        } catch (Exception ex) {
            String message = "An error occurred while selecting the geonote.";
            ExceptionDetailsDialog.openError(null, message, IStatus.ERROR, GeonotesPlugin.PLUGIN_ID, ex);
        }
    }

    public void mouseDragged( MapMouseEvent e ) {
        if (selectedGeonote == null) {
            return;
        }
        try {
            java.awt.Point point = e.getPoint();
            Envelope newPointEnv = context.getPixelBoundingBox(point);
            Coordinate centre = newPointEnv.centre();

            Coordinate reprojectedCoordinate = new Coordinate(centre.x, centre.y);
            if (inverseTransform != null) {
                Point pt = gF.createPoint(reprojectedCoordinate);
                Geometry targetGeometry = JTS.transform(pt, inverseTransform);
                reprojectedCoordinate = targetGeometry.getCoordinate();
            }

            selectedGeonote.setEast(reprojectedCoordinate.x);
            selectedGeonote.setNorth(reprojectedCoordinate.y);

            Transaction transaction = session.beginTransaction();
            session.update(selectedGeonote);
            transaction.commit();

            double w = newPointEnv.getWidth() * 10;
            double h = newPointEnv.getHeight() * 10;
            geonotesLayer.refresh(new Envelope(centre.x - w, centre.x + w, centre.y - h, centre.y + h));
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public void mouseReleased( final MapMouseEvent e ) {
        selectedGeonote = null;
        // geonotesLayer.refresh(null);
    }

    public void setActive( boolean active ) {
        if (active) {
            geonotesLayer = GeonotesPlugin.getDefault().getGeonotesLayer();
            try {
                session = DatabasePlugin.getDefault().getActiveDatabaseConnection().openSession();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (session.isOpen()) {
                session.close();
            }
        }
        super.setActive(active);
    }

}
