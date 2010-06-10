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

import net.refractions.udig.project.IBlackboard;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.ui.ApplicationGIS;
import net.refractions.udig.project.ui.commands.SelectionBoxCommand;
import net.refractions.udig.project.ui.render.displayAdapter.MapMouseEvent;
import net.refractions.udig.project.ui.tool.AbstractModalTool;
import net.refractions.udig.project.ui.tool.ModalTool;
import net.refractions.udig.ui.ExceptionDetailsDialog;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

import eu.hydrologis.jgrass.geonotes.GeonotesPlugin;
import eu.hydrologis.jgrass.geonotes.fieldbook.FieldbookView;

/**
 * The {@link ModalTool tool} through which geonotes are selected.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class GeoNoteSelectionTool extends AbstractModalTool implements ModalTool {

    /**
     * ID of the current tool.
     */
    public static final String ID = "eu.hydrologis.jgrass.geonotes.tools.geonoteselectiontool"; //$NON-NLS-1$

    public static final String SELECTIONID = "eu.hydrologis.jgrass.geonotes.tools.geonoteselectiontool.selectionbounds"; //$NON-NLS-1$

    public static final String CATEGORY_ID = "eu.hydrologis.jgrass.geonotes.category"; //$NON-NLS-1$

    private ILayer geonotesLayer;

    private SelectionBoxCommand draw = new SelectionBoxCommand();

    private int startX;

    private int startY;

    private ReferencedEnvelope selectionBox;

    private IBlackboard blackboard;

    private FieldbookView fieldbookView;

    /**
     * Creates an Geonotes selection {@link ModalTool tool}.
     */
    public GeoNoteSelectionTool() {
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
        try {
            int endX = e.x;
            int endY = e.y;

            IMap map = context.getMap();
            final CoordinateReferenceSystem mapCrs = map.getViewportModel().getCRS();

            if (endX == startX && endY == startY) {
                selectionBox = context.getBoundingBox(e.getPoint(), 5);
            } else {
                Coordinate startCoordinate = context.pixelToWorld(startX, startY);
                Coordinate endCoordinate = context.pixelToWorld(endX, endY);

                selectionBox = new ReferencedEnvelope(new Envelope(startCoordinate, endCoordinate), mapCrs);
            }
            blackboard = map.getBlackboard();
            blackboard.put(SELECTIONID, new ReferencedEnvelope[]{selectionBox});

            fieldbookView.refreshViewerOnEnvelope(selectionBox);
            geonotesLayer.refresh(selectionBox);

        } catch (Throwable e1) {
            String message = "An error occurred on Geonotes selection.";
            ExceptionDetailsDialog.openError(null, message, IStatus.ERROR, GeonotesPlugin.PLUGIN_ID, e1);
        } finally {
            draw.setValid(false);
            context.getViewportPane().repaint();
        }
    }

    public void dispose() {
        super.dispose();
    }

    public void setActive( boolean active ) {
        IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        if (active) {
            geonotesLayer = GeonotesPlugin.getDefault().getGeonotesLayer();
            try {
                fieldbookView = (FieldbookView) activePage.showView(FieldbookView.ID);
            } catch (PartInitException e) {
                e.printStackTrace();
            }
        } else {
            IMap map = ApplicationGIS.getActiveMap();
            IBlackboard blackboard = map.getBlackboard();
            selectionBox = new ReferencedEnvelope(new Envelope(new Coordinate(0, 0), new Coordinate(0.00001, 0.00001)), map
                    .getViewportModel().getCRS());
            blackboard.put(GeoNoteSelectionTool.SELECTIONID, new ReferencedEnvelope[]{selectionBox});
        }
        // else {
        // if (fieldbookView != null)
        // activePage.hideView(fieldbookView);
        // }
        super.setActive(active);
    }

}
