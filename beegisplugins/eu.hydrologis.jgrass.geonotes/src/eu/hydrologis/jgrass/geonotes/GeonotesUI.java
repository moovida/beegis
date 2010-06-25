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
package eu.hydrologis.jgrass.geonotes;

import static eu.hydrologis.jgrass.geonotes.GeonoteConstants.DEFAULTBACKGROUNDCOLORS;
import static eu.hydrologis.jgrass.geonotes.GeonoteConstants.GPS;
import static eu.hydrologis.jgrass.geonotes.GeonoteConstants.NORMAL;
import static eu.hydrologis.jgrass.geonotes.GeonoteConstants.PHOTO;
import i18n.geonotes.Messages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.refractions.udig.project.ui.ApplicationGIS;
import net.refractions.udig.project.ui.internal.dialogs.ColorEditor;
import net.refractions.udig.ui.ExceptionDetailsDialog;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import eu.hydrologis.jgrass.beegisutils.database.annotatedclasses.GeonotesDrawareaTable;
import eu.hydrologis.jgrass.beegisutils.database.annotatedclasses.GeonotesTable;
import eu.hydrologis.jgrass.beegisutils.database.annotatedclasses.GeonotesTextareaTable;
import eu.hydrologis.jgrass.beegisutils.jgrassported.DressedStroke;
import eu.hydrologis.jgrass.beegisutils.jgrassported.SimpleSWTImageEditor;
import eu.hydrologis.jgrass.beegisutils.jgrassported.WindowUtilities;
import eu.hydrologis.jgrass.geonotes.GeonoteConstants.NOTIFICATION;
import eu.hydrologis.jgrass.geonotes.util.ImageManager;

/**
 * Class holding the gui representation of the {@link GeonotesTable}.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class GeonotesUI implements GeonotesObserver {

    private Shell shell;

    private DragWidgetListener l;

    private Label titleLabel;

    private Composite geonoteWrappingComposite;

    private CTabFolder tabFolder;

    private SimpleSWTImageEditor drawArea;

    private TextAreaUI textArea;

    private MediaBoxUI mediaArea;

    private String textAreaString = null;

    private List<DressedStroke> lines = null;

    private Composite clrComp;

    private StackLayout stackLayout;

    private Composite stackComposite;

    private Composite innerPanel;

    private Color geonoteColor;

    private final GeonotesHandler geonotesHandler;

    /**
     * A cache for the gui objects.
     */
    public final static Map<GeonotesHandler, GeonotesUI> guiCache = new HashMap<GeonotesHandler, GeonotesUI>();

    /**
     * Defines if the {@link GeonotesUI} has also a shell, or if it is used just as {@link Composite}.
     */
    private boolean isWithShell = false;

    public GeonotesUI( GeonotesHandler geonotesHandler ) {
        this.geonotesHandler = geonotesHandler;
        geonotesHandler.addObserver(this);
    }

    /**
     * Create the note inside a shell.
     * 
     * @param location on screen where to put the shell or <code>null</code>.
     */
    public void openInShell( Point location ) {
        isWithShell = true;
        shell = new Shell(SWT.RESIZE);
        if (geonoteColor == null) {
            geonoteColor = geonotesHandler.getColor(shell.getDisplay());
        }
        shell.setBackground(geonoteColor);

        shell.setSize(geonotesHandler.getWidth(), geonotesHandler.getHeight());
        if (location == null) {
            Shell parent = PlatformUI.getWorkbench().getDisplay().getActiveShell();
            if (parent != null)
                WindowUtilities.placeDialogInCenterOfParent(parent, shell);
        } else {
            shell.setLocation(location);
        }
        shell.setLayout(new FillLayout());
        createNoteComposite(shell);
        shell.open();
    }

    /**
     * Create the note composite inside a given container.
     * 
     * <p>
     * This note can be embedded inside whatever control. As such it will
     * not have a close button if inside a composite, whereas it will if inside 
     * a shell.
     * </p>
     * 
     * @param parentControl the parent control
     */
    public void createNoteComposite( Composite parentControl ) {
        try {
            geonoteWrappingComposite = new Composite(parentControl, SWT.None);
            geonoteWrappingComposite.setLayout(new GridLayout(5, false));
            if (geonoteColor == null) {
                geonoteColor = geonotesHandler.getColor(parentControl.getDisplay());
            }
            geonoteWrappingComposite.setBackground(geonoteColor);
            l = new DragWidgetListener(parentControl);

            /*
             * title
             */
            createTitleLabel(geonoteWrappingComposite, geonoteColor);

            /*
             * save and closebutton
             */
            createSaveAndCloseButton(geonoteWrappingComposite);

            /*
             * closebutton
             */
            if (isWithShell) {
                createCloseButton(geonoteWrappingComposite);
            }

            stackComposite = new Composite(geonoteWrappingComposite, SWT.None);
            stackLayout = new StackLayout();
            stackComposite.setLayout(stackLayout);
            GridData gridDataSC = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL
                    | GridData.GRAB_VERTICAL);
            gridDataSC.horizontalSpan = 5;
            stackComposite.setLayoutData(gridDataSC);

            tabFolder = new CTabFolder(stackComposite, SWT.NO_TRIM);
            if (isWithShell) {
                tabFolder.addListener(SWT.MouseDown, l);
                tabFolder.addListener(SWT.MouseUp, l);
                tabFolder.addListener(SWT.MouseMove, l);
            }

            CTabItem item1 = new CTabItem(tabFolder, SWT.NONE);
            item1.setText(Messages.getString("GeoNote.draw")); //$NON-NLS-1$
            GeonotesDrawareaTable geonotesDrawareaTable = geonotesHandler
                    .getGeonotesDrawareaTable();
            if (geonotesDrawareaTable != null) {
                lines = geonotesDrawareaTable.getDrawings();
            }
            drawArea = new SimpleSWTImageEditor(tabFolder, SWT.None, lines, null, new Point(500,
                    500), false);
            drawArea.setBackgroundColor(geonoteColor);
            item1.setControl(drawArea.getMainControl());
            CTabItem item2 = new CTabItem(tabFolder, SWT.NONE);
            item2.setText(Messages.getString("GeoNote.text")); //$NON-NLS-1$
            textArea = new TextAreaUI(tabFolder, SWT.MULTI, geonoteColor);
            item2.setControl(textArea.getTextArea());
            CTabItem item3 = new CTabItem(tabFolder, SWT.NONE);
            item3.setText(Messages.getString("GeoNote.media")); //$NON-NLS-1$
            mediaArea = new MediaBoxUI(tabFolder, SWT.None, geonotesHandler);
            item3.setControl(mediaArea.getListViewer().getControl());
            item1.setImage(ImageManager.INSTANCE.getDrawImage());
            item2.setImage(ImageManager.INSTANCE.getTextImage());
            item3.setImage(ImageManager.INSTANCE.getMultimediaImage());

            tabFolder.setSimple(false);
            tabFolder.setUnselectedImageVisible(false);
            tabFolder.pack();
            GridData gridData = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL
                    | GridData.GRAB_VERTICAL);
            tabFolder.setLayoutData(gridData);
            tabFolder.setBackground(geonoteColor);

            /*
             * quick color change
             */
            createQuickBackColorButtons(geonoteWrappingComposite, geonoteColor);

            /*
             * the properties panel
             */
            createPropertiesPanel();
            stackLayout.topControl = tabFolder;

            /*
             * fill the geonote with stuff, if there is some available
             */
            GeonotesTextareaTable geonotesTextareaTable = geonotesHandler
                    .getGeonotesTextareaTable();
            if (geonotesTextareaTable != null) {
                textAreaString = geonotesTextareaTable.getText();
                textArea.getTextArea().setText(textAreaString);
            }

            mediaArea.loadExistingMedia();

            // add it to cache
            guiCache.put(geonotesHandler, this);
        } catch (Exception e) {
            GeonotesPlugin
                    .log(
                            "GeonotesPlugin problem: eu.hydrologis.jgrass.geonotes#GeoNote#createNoteComposite", e); //$NON-NLS-1$
            e.printStackTrace();
        }
    }

    /**
     * Creates the settings/properties panel.
     */
    private void createPropertiesPanel() {
        innerPanel = new Composite(stackComposite, SWT.None);
        innerPanel.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL
                | GridData.GRAB_VERTICAL));
        innerPanel.setLayout(new GridLayout(2, true));

        Group group = new Group(innerPanel, SWT.NONE);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        gridData.horizontalSpan = 2;
        group.setLayoutData(gridData);
        group.setLayout(new GridLayout(1, false));
        group.setText("Information");
        Text infoText = new Text(group, SWT.MULTI);
        infoText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
        infoText.setEditable(false);
        String info = geonotesHandler.getInfo();
        Double azimut = geonotesHandler.getAzimut();
        if (azimut != null) {
            info = info + "\nAzimuth = " + azimut;
        }
        infoText.setText(info);

        // title
        Label titleLabel = new Label(innerPanel, SWT.None);
        titleLabel.setText(Messages.getString("GeoNote.title")); //$NON-NLS-1$
        titleLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
        final Text titleText = new Text(innerPanel, SWT.BORDER);
        titleText.setText(geonotesHandler.getTitle());
        titleText.setToolTipText(geonotesHandler.getInfo());
        titleText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
        // postit color
        Label postitColorLabel = new Label(innerPanel, SWT.None);
        postitColorLabel.setText(Messages.getString("GeoNote.backcolor")); //$NON-NLS-1$
        postitColorLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
                | GridData.GRAB_HORIZONTAL));
        final ColorEditor postitColorEditor = new ColorEditor(innerPanel);
        String[] colorSplit = geonotesHandler.getColorString().split(":");
        int[] pColor = new int[]{Integer.parseInt(colorSplit[0]), Integer.parseInt(colorSplit[1]),
                Integer.parseInt(colorSplit[2])};
        postitColorEditor.setColorValue(new RGB(pColor[0], pColor[1], pColor[2]));
        postitColorEditor.getButton().setLayoutData(
                new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));

        Label filler = new Label(innerPanel, SWT.None);
        GridData fillGD = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL
                | GridData.GRAB_VERTICAL);
        fillGD.horizontalSpan = 2;
        filler.setLayoutData(fillGD);

        final Button okButton = new Button(innerPanel, SWT.PUSH);
        okButton.setText(Messages.getString("GeoNote.ok")); //$NON-NLS-1$
        okButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
        okButton.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {

                RGB colorValue = postitColorEditor.getColorValue();
                String colorString = colorValue.red + ":" + colorValue.green + ":"
                        + colorValue.blue + ":255";
                geonotesHandler.setColor(okButton.getDisplay(), colorString);
                geonotesHandler.setTitle(titleText.getText());

                stackLayout.topControl = tabFolder;
                stackComposite.layout();
            }
        });
        Button cancelButton = new Button(innerPanel, SWT.PUSH);
        cancelButton.setText(Messages.getString("GeoNote.cancel")); //$NON-NLS-1$
        cancelButton
                .setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
        cancelButton.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                stackLayout.topControl = tabFolder;
                stackComposite.layout();
            }
        });

        Button dumpButton = new Button(innerPanel, SWT.PUSH);
        dumpButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
        dumpButton.setText("Dump geonote to disk");
        dumpButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter(){
            public void widgetSelected( org.eclipse.swt.events.SelectionEvent e ) {
                DirectoryDialog directoryDialog = new DirectoryDialog(geonoteWrappingComposite
                        .getShell(), SWT.OPEN);
                String path = directoryDialog.open();

                if (path != null && path.length() > 0) {
                    try {
                        geonotesHandler.dumpNote(path, drawArea.getImage());
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        String message = "An error occurred while saving the note to disk.";
                        ExceptionDetailsDialog.openError(null, message, IStatus.ERROR,
                                GeonotesPlugin.PLUGIN_ID, e1);
                    }
                }

            }
        });

        final Button deleteNoteButton = new Button(innerPanel, SWT.BORDER | SWT.PUSH);
        deleteNoteButton.setImage(ImageManager.INSTANCE.getTrashImage());
        deleteNoteButton.setToolTipText(Messages.getString("GeoNote.deletenote")); //$NON-NLS-1$
        deleteNoteButton.setText(Messages.getString("GeoNote.deletenote")); //$NON-NLS-1$
        deleteNoteButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
                | GridData.GRAB_HORIZONTAL));
        deleteNoteButton.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                try {
                    geonotesHandler.deleteNote();
                } catch (Exception ex) {
                    String message = "An error occurred while removing the Geonote.";
                    ExceptionDetailsDialog.openError(null, message, IStatus.ERROR,
                            GeonotesPlugin.PLUGIN_ID, ex);
                }
                GeonotesPlugin.getDefault().getGeonotesLayer().refresh(
                        geonotesHandler.getBoundsAsReferenceEnvelope(ApplicationGIS.getActiveMap()
                                .getViewportModel().getCRS()));
                if (shell != null)
                    shell.close();
            }
        });
    }

    /**
     * Creates the quick color change bar at the bottom of the note.
     * 
     * @param parent the parent composite.
     * @param geonoteColor the current geonote color.
     */
    private void createQuickBackColorButtons( Composite parent, Color geonoteColor ) {
        clrComp = new Composite(parent, SWT.None);
        clrComp.setLayout(new GridLayout(6, true));
        clrComp.setBackground(geonoteColor);
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
        gridData.horizontalSpan = 5;
        clrComp.setLayoutData(gridData);
        int size = 10;
        for( int i = 0; i < DEFAULTBACKGROUNDCOLORS.length; i++ ) {
            int[] clr = DEFAULTBACKGROUNDCOLORS[i];
            final Label l = new Label(clrComp, SWT.None);
            l.setSize(size, size);
            RGB rgb = new RGB(clr[0], clr[1], clr[2]);
            l.setBackground(new Color(clrComp.getDisplay(), rgb));
            l.addMouseListener(new MouseAdapter(){
                public void mouseDown( MouseEvent e ) {
                    Color geonoteColor = l.getBackground();
                    String geonoteC = geonoteColor.getRed() + ":" + geonoteColor.getGreen() + ":"
                            + geonoteColor.getBlue() + ":255";
                    geonotesHandler.setColor(titleLabel.getDisplay(), geonoteC);
                }
            });
            GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER
                    | GridData.VERTICAL_ALIGN_CENTER);
            gd.heightHint = size;
            gd.widthHint = size;
            l.setLayoutData(gd);

        }
    }

    /**
     * Creates the geonote's close button.
     * 
     * @param parent the parent composite.
     */
    private void createCloseButton( Composite parent ) {

        Button closeItem = new Button(parent, SWT.FLAT);
        closeItem.setImage(ImageManager.INSTANCE.getCloseImage());
        closeItem.setToolTipText(Messages.getString("GeoNote.closenosave")); //$NON-NLS-1$
        closeItem.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                if (shell != null)
                    shell.close();
            }
        });
    }

    /**
     * Creates the geonote's button that closes the note after prior saving.
     * 
     * @param parent the parent composite.
     */
    private void createSaveAndCloseButton( Composite parent ) {

        Button closeItem = new Button(parent, SWT.FLAT);
        closeItem.setImage(ImageManager.INSTANCE.getSafeCloseImage());
        closeItem.setToolTipText(Messages.getString("GeoNote.closesave")); //$NON-NLS-1$
        closeItem.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                // drawing area
                List<DressedStroke> drawings = drawArea.getDrawing();
                geonotesHandler.setDrawarea((ArrayList<DressedStroke>) drawings);

                // text area
                if (textArea.isDirty()) {
                    String text = textArea.getText();
                    if (text != null)
                        geonotesHandler.setTextarea(text);
                }

                /*
                 * media are persisted directly when dragged on the mediabox,
                 * so no need for adding them now.
                 */
                geonotesHandler.persistNote();
                if (shell != null && !shell.isDisposed())
                    shell.close();
            }
        });
    }

    /**
     * Creates the title label.
     * 
     * <p>The title label, when double clicked opens the settings panel.</p>
     * 
     * @param parent the parent composite.
     * @param geonoteColor the current geonote color.
     */
    private void createTitleLabel( Composite parent, Color geonoteColor ) {
        Label iconLabel = new Label(parent, SWT.None);
        int type = geonotesHandler.getType();
        Image img = null;
        if (type == NORMAL) {
            img = ImageManager.INSTANCE.getPinImageSWT30();
        } else if (type == GPS) {
            img = ImageManager.INSTANCE.getGpsPinImageSWT16();
        } else if (type == PHOTO) {
            img = ImageManager.INSTANCE.getPhotoPinImageSWT30();
        }
        iconLabel.setImage(img);
        GridData gridDataImg = new GridData(GridData.BEGINNING);
        iconLabel.setLayoutData(gridDataImg);

        titleLabel = new Label(parent, SWT.None);
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
        titleLabel.setLayoutData(gridData);
        titleLabel.setToolTipText(geonotesHandler.getInfo());
        titleLabel.setText(geonotesHandler.getTitle());
        titleLabel.setBackground(geonoteColor);
        titleLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
        if (isWithShell) {
            titleLabel.addListener(SWT.MouseDown, l);
            titleLabel.addListener(SWT.MouseUp, l);
            titleLabel.addListener(SWT.MouseMove, l);
        }
        titleLabel.addMouseListener(new MouseAdapter(){
            public void mouseDoubleClick( MouseEvent arg0 ) {
                stackLayout.topControl = innerPanel;
                stackComposite.layout();
            }
        });
    }

    public Image getDrawareaImage() {
        if (drawArea != null) {
            return drawArea.getImage();
        }
        return null;
    }

    /**
     * Returns the geonote's shell.
     * 
     * @return the geonote's shell.
     */
    public Shell getShell() {
        return shell;
    }

    /**
     * Getter for the main composite if the geonote.
     * 
     * @return the main composite inside which the geonote is created.
     */
    public Composite getGeonoteWrappingComposite() {
        return geonoteWrappingComposite;
    }

    public String toString() {
        return geonotesHandler.toString();
    }

    public void updateFromGeonotes( GeonotesHandler handler, Object arg ) {
        if (arg instanceof NOTIFICATION) {
            NOTIFICATION notification = (NOTIFICATION) arg;

            switch( notification ) {
            case SIZECHANGED:
                shell.setSize(handler.getWidth(), handler.getHeight());
                break;
            case TITLECHANGED:
                titleLabel.setText(handler.getTitle());
                titleLabel.setToolTipText(handler.getInfo());
                break;
            case STYLECHANGED:
                geonoteColor = handler.getColor(titleLabel.getDisplay());
                if (shell != null && !shell.isDisposed())
                    shell.setBackground(geonoteColor);
                geonoteWrappingComposite.setBackground(geonoteColor);
                titleLabel.setBackground(geonoteColor);
                clrComp.setBackground(geonoteColor);
                stackComposite.setBackground(geonoteColor);
                tabFolder.setBackground(geonoteColor);
                textArea.getTextArea().setBackground(geonoteColor);
                drawArea.setBackgroundColor(geonoteColor);
                mediaArea.getListViewer().getControl().setBackground(geonoteColor);
                break;

            default:
                break;
            }
        }
    }

    public void dumpNote( String path ) throws Exception {
        geonotesHandler.dumpNote(path, drawArea.getImage());
    }

    public void dumpBinaryNote( String path ) throws Exception {
        geonotesHandler.dumpBinaryNote(path);
    }

}
