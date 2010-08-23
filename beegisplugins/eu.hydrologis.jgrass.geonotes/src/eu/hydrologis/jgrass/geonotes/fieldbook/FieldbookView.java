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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import net.refractions.udig.project.IBlackboard;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.ui.ApplicationGIS;
import net.refractions.udig.ui.ExceptionDetailsDialog;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import eu.hydrologis.jgrass.beegisutils.BeegisUtilsPlugin;
import eu.hydrologis.jgrass.beegisutils.database.annotatedclasses.GeonotesTextareaTable;
import eu.hydrologis.jgrass.database.DatabasePlugin;
import eu.hydrologis.jgrass.database.core.DatabaseConnectionProperties;
import eu.hydrologis.jgrass.database.interfaces.IDatabaseEventListener;
import eu.hydrologis.jgrass.geonotes.GeonoteConstants;
import eu.hydrologis.jgrass.geonotes.GeonotesHandler;
import eu.hydrologis.jgrass.geonotes.GeonotesObserver;
import eu.hydrologis.jgrass.geonotes.GeonotesPlugin;
import eu.hydrologis.jgrass.geonotes.GeonotesUI;
import eu.hydrologis.jgrass.geonotes.GeonoteConstants.NOTIFICATION;
import eu.hydrologis.jgrass.geonotes.fieldbook.actions.DumpNotesAction;
import eu.hydrologis.jgrass.geonotes.fieldbook.actions.DumpNotesBinaryAction;
import eu.hydrologis.jgrass.geonotes.fieldbook.actions.ExportToFeatureLayerAction;
import eu.hydrologis.jgrass.geonotes.fieldbook.actions.ImportNotesAction;
import eu.hydrologis.jgrass.geonotes.fieldbook.actions.RemoveNotesAction;
import eu.hydrologis.jgrass.geonotes.fieldbook.actions.SendNotesAction;
import eu.hydrologis.jgrass.geonotes.fieldbook.actions.SortNotesTimeAction;
import eu.hydrologis.jgrass.geonotes.fieldbook.actions.SortNotesTitleAction;
import eu.hydrologis.jgrass.geonotes.fieldbook.actions.ZoomToNotesAction;
import eu.hydrologis.jgrass.geonotes.tools.GeoNoteSelectionTool;

/**
 * The view that represents the beegis fieldbook.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 * @since 3.0
 * @see GeonotesUI
 */
public class FieldbookView extends ViewPart implements GeonotesObserver, IDatabaseEventListener {

    public final static String ID = "eu.hydrologis.jgrass.geonotes.fieldbook.view";

    /**
     * The possible search types that appear in the search types combo.
     */
    public static String[] SEARCHTYPES = {"Text search", "Color search", "Date interval search", "Type search"};

    public HashMap<String, Control> searchTypesMap = new HashMap<String, Control>();

    private List<GeonotesHandler> geonotesList;

    private GeonotesListViewer geonotesViewer;

    private Text searchTextWidget;

    private Composite mainGeonotesComposite;

    private GeometryFactory gF = new GeometryFactory();

    private ImportNotesAction importAction;

    public FieldbookView() {
    }

    public void createPartControl( Composite parent ) {

        SashForm sashForm = new SashForm(parent, SWT.VERTICAL);

        // left panel for list and search
        Composite mainListComposite = new Composite(sashForm, SWT.NONE);
        GridData gD1 = new GridData(SWT.LEFT, SWT.FILL, false, true);
        mainListComposite.setLayoutData(gD1);
        mainListComposite.setLayout(new GridLayout(1, false));

        mainGeonotesComposite = new Composite(sashForm, SWT.NONE);
        GridData gD2 = new GridData(SWT.FILL, SWT.FILL, true, true);
        mainGeonotesComposite.setLayoutData(gD2);
        StackLayout geonotesStackLayout = new StackLayout();
        mainGeonotesComposite.setLayout(geonotesStackLayout);

        sashForm.setWeights(new int[]{45, 55});
        /*
         * left panel
         */
        // the search types combo that leads the search types composite
        final Combo searchTypesCombo = new Combo(mainListComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
        searchTypesCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        searchTypesCombo.setItems(SEARCHTYPES);
        searchTypesCombo.select(0);

        // the search types composite, that will hold the different search mechanisms
        final Composite searchTypesComposite = new Composite(mainListComposite, SWT.NONE);
        GridData gD4 = new GridData(SWT.FILL, SWT.TOP, true, false);
        gD4.heightHint = 30;
        searchTypesComposite.setLayoutData(gD4);
        final StackLayout searchtypesStackLayout = new StackLayout();
        searchTypesComposite.setLayout(searchtypesStackLayout);

        searchTypesCombo.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                int index = searchTypesCombo.getSelectionIndex();
                Control control = searchTypesMap.get(SEARCHTYPES[index]);
                searchtypesStackLayout.topControl = control;
                searchTypesComposite.layout();

                // clean up the selection, view all
                if (geonotesList != null)
                    geonotesViewer.setInput(geonotesList);
                searchTextWidget.setText("");
            }
        });

        searchTextWidget = new Text(searchTypesComposite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        searchTextWidget.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        searchTextWidget.setText("");
        searchtypesStackLayout.topControl = searchTextWidget;
        searchTextWidget.addKeyListener(new KeyAdapter(){
            public void keyReleased( KeyEvent e ) {
                if (geonotesList != null) {
                    List<GeonotesHandler> toAdd = new ArrayList<GeonotesHandler>();
                    for( GeonotesHandler geonoteHandler : geonotesList ) {
                        String noteTitle = geonoteHandler.getTitle();
                        String userText = searchTextWidget.getText();
                        if (noteTitle.matches(".*" + userText + ".*")) {
                            toAdd.add(geonoteHandler);
                        } else {
                            // check also in text
                            GeonotesTextareaTable geonotesTextareaTable = geonoteHandler.getGeonotesTextareaTable();
                            if (geonotesTextareaTable != null) {
                                String textAreaText = geonotesTextareaTable.getText();
                                if (textAreaText != null && textAreaText.toLowerCase().indexOf(userText.toLowerCase()) != -1) {
                                    toAdd.add(geonoteHandler);
                                }
                            }
                        }
                    }
                    geonotesViewer.setInput(toAdd);
                    geonotesViewer.setRelatedToNeutral();
                }
            }
        });

        // type 2: search by note color
        Composite searchColorWidget = new Composite(searchTypesComposite, SWT.BORDER);
        searchColorWidget.setLayout(new GridLayout(6, true));
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
        searchColorWidget.setLayoutData(gridData);
        int[][] colors = GeonoteConstants.DEFAULTBACKGROUNDCOLORS;
        int size = 20;
        for( int i = 0; i < colors.length; i++ ) {
            int[] clr = colors[i];
            final Label l = new Label(searchColorWidget, SWT.None);
            l.setSize(size, size);
            RGB rgb = new RGB(clr[0], clr[1], clr[2]);
            l.setBackground(new Color(searchColorWidget.getDisplay(), rgb));
            l.addMouseListener(new MouseAdapter(){
                public void mouseDown( MouseEvent e ) {
                    Color geonoteColor = l.getBackground();
                    String selectedColor = geonoteColor.getRed() + ":" + geonoteColor.getGreen() + ":" + geonoteColor.getBlue()
                            + ":255";
                    if (geonotesList != null) {
                        List<GeonotesHandler> toAdd = new ArrayList<GeonotesHandler>();
                        for( GeonotesHandler geonoteHandler : geonotesList ) {
                            String geonoteColorRGB = geonoteHandler.getColorString();
                            if (geonoteColorRGB.equals(selectedColor)) {
                                toAdd.add(geonoteHandler);
                            }
                        }
                        geonotesViewer.setInput(toAdd);
                    }
                }
            });
            GridData gd = new GridData(GridData.FILL, GridData.FILL, true, false);
            l.setLayoutData(gd);
        }

        // type 3: search by date
        final Composite searchByDateWidget = new Composite(searchTypesComposite, SWT.BORDER);
        searchByDateWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        searchByDateWidget.setLayout(new FillLayout());

        final Button fromDateButton = new Button(searchByDateWidget, SWT.PUSH);
        fromDateButton.setText("from");
        final Button toDateButton = new Button(searchByDateWidget, SWT.PUSH);
        toDateButton.setText("to");
        final Button searchDateButton = new Button(searchByDateWidget, SWT.PUSH);
        searchDateButton.setText("search");
        searchDateButton.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                String fromDateString = fromDateButton.getToolTipText();
                String toDateString = toDateButton.getToolTipText();
                DateTime fromDate = BeegisUtilsPlugin.dateTimeFormatterYYYYMMDDHHMM.parseDateTime(fromDateString);
                DateTime toDate = BeegisUtilsPlugin.dateTimeFormatterYYYYMMDDHHMM.parseDateTime(toDateString);
                Interval interval = new Interval(fromDate, toDate);
                if (geonotesList != null) {
                    List<GeonotesHandler> toAdd = new ArrayList<GeonotesHandler>();
                    for( GeonotesHandler geonoteHandler : geonotesList ) {
                        DateTime dateTime = geonoteHandler.getCreationDate();
                        if (interval.contains(dateTime)) {
                            toAdd.add(geonoteHandler);
                        }
                    }
                    geonotesViewer.setInput(toAdd);
                }
            }
        });

        Listener dateListener = new Listener(){
            public void handleEvent( Event e ) {
                if (e.widget instanceof Button) {
                    final Button dateButton = (Button) e.widget;
                    final Shell dialog = new Shell(Display.getDefault(), SWT.DIALOG_TRIM);
                    dialog.setLayout(new GridLayout(3, false));

                    final org.eclipse.swt.widgets.DateTime date = new org.eclipse.swt.widgets.DateTime(dialog, SWT.DATE);
                    final org.eclipse.swt.widgets.DateTime time = new org.eclipse.swt.widgets.DateTime(dialog, SWT.TIME
                            | SWT.SHORT);

                    new Label(dialog, SWT.NONE);
                    new Label(dialog, SWT.NONE);
                    Button ok = new Button(dialog, SWT.PUSH);
                    ok.setText("OK");
                    ok.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
                    ok.addSelectionListener(new SelectionAdapter(){
                        public void widgetSelected( SelectionEvent e ) {
                            StringBuilder dateBuilder = new StringBuilder();
                            dateBuilder.append(date.getYear());
                            dateBuilder.append("-");
                            dateBuilder.append((date.getMonth() + 1));
                            dateBuilder.append("-");
                            dateBuilder.append(date.getDay());
                            dateBuilder.append(" ");
                            dateBuilder.append(time.getHours());
                            dateBuilder.append(":");
                            dateBuilder.append(time.getMinutes());
                            if (dateButton.equals(fromDateButton)) {
                                fromDateButton.setToolTipText(dateBuilder.toString());
                            } else {
                                toDateButton.setToolTipText(dateBuilder.toString());
                            }
                            dialog.close();
                        }
                    });
                    dialog.setDefaultButton(ok);
                    Point mouse = dialog.getDisplay().getCursorLocation();
                    dialog.setLocation(mouse.x, mouse.y);
                    dialog.pack();
                    dialog.open();
                }
            }
        };

        fromDateButton.addListener(SWT.Selection, dateListener);
        toDateButton.addListener(SWT.Selection, dateListener);

        // end of type 3

        // type 4: search by type
        final Composite isGpsWidget = new Composite(searchTypesComposite, SWT.BORDER);
        isGpsWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        isGpsWidget.setLayout(new FillLayout());

        final Button allNotesButton = new Button(isGpsWidget, SWT.TOGGLE);
        allNotesButton.setText("all");
        allNotesButton.setSelection(true);

        final Button normalNotesButton = new Button(isGpsWidget, SWT.TOGGLE);
        normalNotesButton.setText("normal");

        final Button gpsNotesButton = new Button(isGpsWidget, SWT.TOGGLE);
        gpsNotesButton.setText("gps");

        final Button photoButton = new Button(isGpsWidget, SWT.TOGGLE);
        photoButton.setText("photo");

        Listener gpsListener = new Listener(){
            public void handleEvent( Event e ) {
                Control[] children = isGpsWidget.getChildren();
                for( int i = 0; i < children.length; i++ ) {
                    Control child = children[i];
                    if (e.widget != child && child instanceof Button && (child.getStyle() & SWT.TOGGLE) != 0) {
                        ((Button) child).setSelection(false);
                    }
                }
                Button selectedButton = (Button) e.widget;
                selectedButton.setSelection(true);

                if (selectedButton.equals(allNotesButton)) {
                    if (geonotesList != null) {
                        List<GeonotesHandler> toAdd = new ArrayList<GeonotesHandler>();
                        toAdd.addAll(geonotesList);
                        geonotesViewer.setInput(toAdd);
                    }
                } else if (selectedButton.equals(normalNotesButton)) {
                    if (geonotesList != null) {
                        List<GeonotesHandler> toAdd = new ArrayList<GeonotesHandler>();
                        for( GeonotesHandler geoNote : geonotesList ) {
                            if (geoNote.getType() == GeonoteConstants.NORMAL) {
                                toAdd.add(geoNote);
                            }
                        }
                        geonotesViewer.setInput(toAdd);
                    }
                } else if (selectedButton.equals(photoButton)) {
                    if (geonotesList != null) {
                        List<GeonotesHandler> toAdd = new ArrayList<GeonotesHandler>();
                        for( GeonotesHandler geoNote : geonotesList ) {
                            if (geoNote.getType() == GeonoteConstants.PHOTO) {
                                toAdd.add(geoNote);
                            }
                        }
                        geonotesViewer.setInput(toAdd);
                    }
                } else if (selectedButton.equals(gpsNotesButton)) {
                    if (geonotesList != null) {
                        List<GeonotesHandler> toAdd = new ArrayList<GeonotesHandler>();
                        for( GeonotesHandler geoNote : geonotesList ) {
                            if (geoNote.getType() == GeonoteConstants.GPS) {
                                toAdd.add(geoNote);
                            }
                        }
                        geonotesViewer.setInput(toAdd);
                    }
                }

            }
        };
        allNotesButton.addListener(SWT.Selection, gpsListener);
        gpsNotesButton.addListener(SWT.Selection, gpsListener);
        photoButton.addListener(SWT.Selection, gpsListener);
        normalNotesButton.addListener(SWT.Selection, gpsListener);

        // end of type 4

        searchTypesMap.put(SEARCHTYPES[0], searchTextWidget);
        searchTypesMap.put(SEARCHTYPES[1], searchColorWidget);
        searchTypesMap.put(SEARCHTYPES[2], searchByDateWidget);
        searchTypesMap.put(SEARCHTYPES[3], isGpsWidget);

        geonotesViewer = new GeonotesListViewer(mainListComposite, mainGeonotesComposite, SWT.MULTI | SWT.BORDER);
        GridData gd3 = new GridData(SWT.FILL, SWT.FILL, true, true);
        geonotesViewer.getTable().setLayoutData(gd3);

        geonotesList = GeonotesHandler.getGeonotesHandlers();
        geonotesViewer.setInput(geonotesList);

        // add some actions
        Table table = geonotesViewer.getTable();
        MenuManager popManager = new MenuManager();

        IAction menuAction = new ZoomToNotesAction(geonotesViewer);
        popManager.add(menuAction);
        menuAction = new RemoveNotesAction(geonotesViewer);
        popManager.add(menuAction);
        popManager.add(new Separator());
        menuAction = new ExportToFeatureLayerAction(geonotesViewer);
        popManager.add(menuAction);
        menuAction = new DumpNotesAction(geonotesViewer);
        popManager.add(menuAction);
        menuAction = new DumpNotesBinaryAction(geonotesViewer);
        popManager.add(menuAction);
        importAction = new ImportNotesAction(geonotesViewer);
        popManager.add(importAction);
        popManager.add(new Separator());
        menuAction = new SendNotesAction(geonotesViewer);
        popManager.add(menuAction);
        popManager.add(new Separator());
        menuAction = new SortNotesTitleAction(geonotesViewer);
        popManager.add(menuAction);
        menuAction = new SortNotesTimeAction(geonotesViewer);
        popManager.add(menuAction);

        Menu menu = popManager.createContextMenu(table);
        table.setMenu(menu);

        // drop support
        Transfer[] transferTypes = new Transfer[]{TextTransfer.getInstance(), FileTransfer.getInstance()};
        int dndOperations = DND.DROP_MOVE | DND.DROP_LINK;
        geonotesViewer.addDropSupport(dndOperations, transferTypes, new FileDropListener());

        // add a close listener
        /*
         * the following is currently needed to solve the unable to load map error 
         * which is due to the fact that the reference envelope in the blackboard 
         * breaks the memento xml saving engine. So the reference envelopes
         * are currently removed every time the fieldbook is close, which in fact is 
         * even better, since it removes the pin selection.
         */
        this.getSite().getPage().addPartListener(new IPartListener2(){
            public void partClosed( IWorkbenchPartReference partRef ) {
                Collection< ? extends IMap> openMaps = ApplicationGIS.getOpenMaps();
                for( IMap map : openMaps ) {
                    IBlackboard blackboard = map.getBlackboard();
                    blackboard.put(GeoNoteSelectionTool.SELECTIONID, new ReferencedEnvelope[0]);
                }
            }
            public void partActivated( IWorkbenchPartReference partRef ) {
            }
            public void partBroughtToTop( IWorkbenchPartReference partRef ) {
            }
            public void partDeactivated( IWorkbenchPartReference partRef ) {
            }
            public void partHidden( IWorkbenchPartReference partRef ) {
            }
            public void partInputChanged( IWorkbenchPartReference partRef ) {
            }
            public void partOpened( IWorkbenchPartReference partRef ) {
            }
            public void partVisible( IWorkbenchPartReference partRef ) {
            }
        });

        DatabasePlugin.getDefault().addDatabaseEventListener(this);

    }

    @Override
    public void dispose() {
        DatabasePlugin.getDefault().removeDatabaseEventListener(this);

        super.dispose();
    }

    public void setFocus() {
    }

    public void init( IViewSite site ) throws PartInitException {
        super.init(site);

        // FieldbookView fieldbookView = GeonotesPlugin.getDefault().getFieldbookView();
        // System.out.println(fieldbookView);

    }

    public void refreshViewerOnEnvelope( ReferencedEnvelope selectionBox ) {
        IMap map = ApplicationGIS.getActiveMap();
        CoordinateReferenceSystem mapCrs = map.getViewportModel().getCRS();
        CoordinateReferenceSystem noteCrs = null;

        Coordinate point = null;
        try {
            List<GeonotesHandler> toAdd = new ArrayList<GeonotesHandler>();
            for( GeonotesHandler geonote : geonotesList ) {
                Coordinate position = geonote.getPosition();
                String crsWkt = geonote.getCrsWkt();

                double east = position.x;
                double north = position.y;
                point = new Coordinate(east, north);
                if (!mapCrs.toWKT().equals(crsWkt)) {
                    noteCrs = CRS.parseWKT(crsWkt);
                    // transform coordinates before check
                    MathTransform transform = CRS.findMathTransform(noteCrs, mapCrs, true);
                    // jts geometry
                    com.vividsolutions.jts.geom.Point pt = gF.createPoint(new Coordinate(east, north));
                    Geometry targetGeometry = JTS.transform(pt, transform);
                    point = targetGeometry.getCoordinate();
                }

                if (selectionBox != null && selectionBox.contains(point)) {
                    // is selected
                    toAdd.add(geonote);
                }
            }
            if (toAdd.size() > 0) {
                geonotesViewer.setInput(toAdd);
            } else {
                geonotesViewer.setInput(geonotesList);
            }
            geonotesViewer.setRelatedToNeutral();

        } catch (Exception e) {
            String message = "An error occurred while refreshing the entries.";
            ExceptionDetailsDialog.openError(null, message, IStatus.ERROR, GeonotesPlugin.PLUGIN_ID, e);
            e.printStackTrace();
        }
    }

    public void updateFromGeonotes( final GeonotesHandler handler, Object arg ) {
        if (arg instanceof NOTIFICATION) {
            NOTIFICATION notification = (NOTIFICATION) arg;

            switch( notification ) {
            case NOTEREMOVED:
                Display.getDefault().asyncExec(new Runnable(){
                    public void run() {
                        geonotesList.remove(handler);
                        geonotesViewer.setInput(geonotesList);
                        geonotesViewer.setRelatedToNeutral();
                        ILayer geonotesLayer = GeonotesPlugin.getDefault().getGeonotesLayer();
                        CoordinateReferenceSystem crs = ApplicationGIS.getActiveMap().getViewportModel().getCRS();
                        geonotesLayer.refresh(null);
                    }
                });

                break;
            case NOTEADDED:
                Display.getDefault().asyncExec(new Runnable(){
                    public void run() {
                        geonotesList.add(handler);
                        geonotesViewer.setInput(geonotesList);
                        geonotesViewer.setRelatedToNeutral();
                        ILayer geonotesLayer = GeonotesPlugin.getDefault().getGeonotesLayer();
                        CoordinateReferenceSystem crs = ApplicationGIS.getActiveMap().getViewportModel().getCRS();
                        geonotesLayer.refresh(handler.getBoundsAsReferenceEnvelope(crs));
                    }
                });
                break;
            case NOTESAVED:
                // TO CHECK this should now be handled in update of ui
                Display.getDefault().asyncExec(new Runnable(){
                    public void run() {
                        geonotesViewer.setRelatedToNeutral();
                        GeonotesUI.guiCache.clear();

                        Long hId = handler.getId();
                        int index = -1;
                        for( int i = 0; i < geonotesList.size(); i++ ) {
                            GeonotesHandler geonotesHandler = geonotesList.get(i);
                            if (hId.equals(geonotesHandler.getId())) {
                                index = i;
                                break;
                            }
                        }
                        if (index != -1) {
                            geonotesList.set(index, handler);
                            geonotesViewer.refresh(true);
                        }
                    }
                });
                break;

            default:
                break;
            }

        }

    }

    private class FileDropListener implements DropTargetListener {

        public void dragEnter( DropTargetEvent arg0 ) {
        }

        public void dragLeave( DropTargetEvent arg0 ) {
        }

        public void dragOperationChanged( DropTargetEvent arg0 ) {
        }

        public void dragOver( DropTargetEvent arg0 ) {
        }

        public void drop( DropTargetEvent arg0 ) {
            String[] data = (String[]) arg0.data;
            if (data.length > 0) {
                try {
                    importAction.importNotesArchive(data[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void dropAccept( DropTargetEvent arg0 ) {
        }

    }

    public void onDatabaseClosed( DatabaseConnectionProperties connectionProperties ) {
        geonotesList.clear();
    }

    public void onDatabaseOpened( DatabaseConnectionProperties connectionProperties ) {
        Display.getDefault().asyncExec(new Runnable(){
            public void run() {
                geonotesList = GeonotesHandler.getGeonotesHandlers();
                geonotesViewer.setInput(geonotesList);
                geonotesViewer.setRelatedToNeutral();
            }
        });
    }

}
