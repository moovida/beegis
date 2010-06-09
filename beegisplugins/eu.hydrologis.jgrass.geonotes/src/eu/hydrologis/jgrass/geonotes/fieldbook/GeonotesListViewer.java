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
package eu.hydrologis.jgrass.geonotes.fieldbook;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.refractions.udig.project.IBlackboard;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.render.IViewportModel;
import net.refractions.udig.project.ui.ApplicationGIS;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import eu.hydrologis.jgrass.geonotes.GeonoteConstants;
import eu.hydrologis.jgrass.geonotes.GeonotesHandler;
import eu.hydrologis.jgrass.geonotes.GeonotesPlugin;
import eu.hydrologis.jgrass.geonotes.GeonotesUI;
import eu.hydrologis.jgrass.geonotes.tools.GeoNoteSelectionTool;
import eu.hydrologis.jgrass.geonotes.util.ImageManager;

/**
 * List viewer of the geonotes fieldbook.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class GeonotesListViewer extends TableViewer implements ISelectionChangedListener {

    /**
     * different types of points
     */
    private Image geonoteImage;
    private Image geonoteGpsImage;
    private Image geonotePhotoImage;
    private final Composite related;
    private List<GeonotesHandler> selectedNotesList;
    private GeometryFactory gF = new GeometryFactory();
    private Composite neutralComposite;

    public GeonotesListViewer( Composite parent, Composite related, int multi ) {
        super(parent, multi);
        this.related = related;

        geonoteImage = ImageManager.INSTANCE.getPinImageSWT30();
        geonoteGpsImage = ImageManager.INSTANCE.getGpsPinImageSWT30();
        geonotePhotoImage = ImageManager.INSTANCE.getPhotoPinImageSWT30();

        this.addSelectionChangedListener(this);

        GeonotesListContentProvider glCP = new GeonotesListContentProvider();
        this.setContentProvider(glCP);
        GeonotesListLabelProvider glLP = new GeonotesListLabelProvider();
        this.setLabelProvider(glLP);

        setRelatedToNeutral();
    }

    public void selectionChanged( SelectionChangedEvent event ) {
        if (!(event.getSelection() instanceof StructuredSelection)) {
            return;
        }
        StructuredSelection sel = (StructuredSelection) event.getSelection();

        Iterator< ? > geonotesIterator = sel.iterator();
        if (!geonotesIterator.hasNext()) {
            return;
        }

        selectedNotesList = new ArrayList<GeonotesHandler>();
        int selectedNumber = 0;
        while( geonotesIterator.hasNext() ) {
            Object note = geonotesIterator.next();
            if (note instanceof GeonotesHandler) {
                GeonotesHandler geonoteHandler = (GeonotesHandler) note;
                selectedNotesList.add(geonoteHandler);
                selectedNumber++;
            }
        }

        if (selectedNumber == 1) {
            Layout layout = related.getLayout();
            if (layout instanceof StackLayout) {
                GeonotesHandler geonoteHandler = selectedNotesList.get(0);
                StackLayout sl = (StackLayout) layout;

                GeonotesUI geonotesUI = GeonotesUI.guiCache.get(geonoteHandler);
                if (geonotesUI == null) {
                    geonotesUI = new GeonotesUI(geonoteHandler);
                }
                geonotesUI.createNoteComposite(related);
                Composite composite = geonotesUI.getGeonoteWrappingComposite();
                sl.topControl = composite;
                related.layout();
            }
        }

        Display.getDefault().asyncExec(new Runnable(){
            public void run() {
                highlightSelectedPins();
            }
        });

    }

    /**
     * Puts the geonotes panel to be empty. 
     * 
     * <p>
     * This is needed for example if a note is removed and the geonotes
     * panel should be empty
     * </p>
     */
    public void setRelatedToNeutral() {
        if (neutralComposite == null) {
            neutralComposite = new Group(related, SWT.None);
            neutralComposite.setLayout(new GridLayout(1, false));
            Label neutralLabel = new Label(neutralComposite, SWT.NONE);
            neutralLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
            neutralLabel.setText("No geonote selected.");
        }
        Layout layout = related.getLayout();
        if (layout instanceof StackLayout) {
            StackLayout sl = (StackLayout) layout;
            sl.topControl = neutralComposite;
            related.layout();
        }
    }

    /**
     * Getter for the currently selected Geonotes list.
     *  
     * @return the currently selected geonotes list.
     */
    public List<GeonotesHandler> getCurrentGeonotesSelection() {
        if (selectedNotesList != null) {
            return selectedNotesList;
        }
        return null;
    }

    private void highlightSelectedPins() {
        IMap map = ApplicationGIS.getActiveMap();
        List<ILayer> mapLayers = map.getMapLayers();
        if (mapLayers.size() < 1) {
            return;
        }
        IViewportModel viewportModel = map.getViewportModel();
        CoordinateReferenceSystem mapCrs = viewportModel.getCRS();

        List<ReferencedEnvelope> refList = new ArrayList<ReferencedEnvelope>();
        for( int i = 0; i < selectedNotesList.size(); i++ ) {
            GeonotesHandler geonoteHandler = selectedNotesList.get(i);
            Coordinate position = geonoteHandler.getPosition();
            String noteCrsString = geonoteHandler.getCrsWkt();
            try {
                if (!mapCrs.toWKT().trim().equals(noteCrsString.trim())) {
                    CoordinateReferenceSystem noteCrs = CRS.parseWKT(noteCrsString);
                    // transform coordinates before check
                    MathTransform transform = CRS.findMathTransform(noteCrs, mapCrs, true);
                    // jts geometry
                    Point pt = gF.createPoint(new Coordinate(position.x, position.y));
                    Geometry targetGeometry = JTS.transform(pt, transform);
                    position = targetGeometry.getCoordinate();
                }
            } catch (Exception e) {
                // if transform doesn't work, try to go on with the current prj
            }

            java.awt.Point positionInPixels = viewportModel.worldToPixel(position);

            Coordinate start = viewportModel.pixelToWorld(positionInPixels.x - 3,
                    positionInPixels.y - 3);
            Coordinate end = viewportModel.pixelToWorld(positionInPixels.x + 3,
                    positionInPixels.y + 3);

            ReferencedEnvelope selectionBox = new ReferencedEnvelope(new Envelope(start, end),
                    mapCrs);
            refList.add(selectionBox);
        }

        IBlackboard blackboard = map.getBlackboard();
        blackboard.put(GeoNoteSelectionTool.SELECTIONID, (ReferencedEnvelope[]) refList
                .toArray(new ReferencedEnvelope[refList.size()]));

        ILayer geonotesLayer = GeonotesPlugin.getDefault().getGeonotesLayer();
        for( ReferencedEnvelope referencedEnvelope : refList ) {
            geonotesLayer.refresh(referencedEnvelope);
        }

    }

    private class GeonotesListContentProvider implements ITreeContentProvider {

        public Object[] getChildren( Object parentElement ) {
            return new Object[]{parentElement};
        }

        public Object getParent( Object element ) {
            return null;
        }

        public boolean hasChildren( Object element ) {
            return false;
        }

        public Object[] getElements( Object inputElement ) {
            if (inputElement instanceof List) {
                List< ? > list = (List< ? >) inputElement;
                Object[] array = (Object[]) list.toArray(new Object[list.size()]);
                return array;
            }
            return null;
        }

        public void dispose() {
        }

        public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
        }

    }

    private class GeonotesListLabelProvider implements ILabelProvider {

        public Image getImage( Object element ) {
            if (element instanceof GeonotesHandler) {
                GeonotesHandler note = (GeonotesHandler) element;
                if (note.getType() == GeonoteConstants.GPS) {
                    return geonoteGpsImage;
                } else if (note.getType() == GeonoteConstants.PHOTO) {
                    return geonotePhotoImage;
                } else if (note.getType() == GeonoteConstants.NORMAL) {
                    return geonoteImage;
                } else {
                    return geonoteImage;
                }
            }
            return null;
        }
        public String getText( Object element ) {
            if (element instanceof GeonotesHandler) {
                return ((GeonotesHandler) element).getTitle();
            }
            return null;
        }

        public void addListener( ILabelProviderListener listener ) {
        }

        public void dispose() {
        }

        public boolean isLabelProperty( Object element, String property ) {
            return false;
        }

        public void removeListener( ILabelProviderListener listener ) {
        }

    }
}
