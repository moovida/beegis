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
package eu.hydrologis.jgrass.geonotes.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.refractions.udig.ui.ExceptionDetailsDialog;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

import eu.hydrologis.jgrass.beegisutils.jgrassported.DressedStroke;
import eu.hydrologis.jgrass.beegisutils.jgrassported.SimpleSWTImageEditor;
import eu.hydrologis.jgrass.geonotes.DndFile;
import eu.hydrologis.jgrass.geonotes.MediaBoxUI;
import eu.hydrologis.jgrass.geonotes.GeonotesPlugin;
import eu.hydrologis.jgrass.geonotes.MimeTypes;
import eu.hydrologis.jgrass.geonotes.GeonotesHandler;

/**
 * Action to open media from the {@link MediaBoxUI media area}.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class OpenSelectedEntryAction implements IDoubleClickListener {
    private final TableViewer lv;
    private SimpleSWTImageEditor simpleSWTImageEditor;

    public OpenSelectedEntryAction( String title, TableViewer lv ) {
        this.lv = lv;
    }

    public void doubleClick( DoubleClickEvent arg0 ) {
        try {
            IStructuredSelection selection = (IStructuredSelection) lv.getSelection();
            DndFile sel = (DndFile) selection.getFirstElement();

            // need to extract the file to temporary folder
            final String name = sel.name;
            GeonotesHandler geonotesHandler = sel.getGeonotesHandler();
            final List<DressedStroke> drawingList = new ArrayList<DressedStroke>();
            File mediaFile = geonotesHandler.extractMediaToPath(null, name, drawingList);
            if (mediaFile.exists()) {
                // if it is an image use the own editor
                String absolutePath = mediaFile.getAbsolutePath();
                if (absolutePath.toLowerCase().endsWith(MimeTypes.T_JPG) || absolutePath.toLowerCase().endsWith(MimeTypes.T_PNG)
                        || absolutePath.toLowerCase().endsWith(MimeTypes.T_GIF)
                        || absolutePath.toLowerCase().endsWith(MimeTypes.T_TIF)) {
                    openImage(name, geonotesHandler, drawingList, mediaFile);
                } else {
                    // else just open it with the system's default
                    Program.launch(mediaFile.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            String message = "An error occurred while opening the selected media.";
            ExceptionDetailsDialog.openError(null, message, IStatus.ERROR, GeonotesPlugin.PLUGIN_ID, e);
            e.printStackTrace();
        }
    }
    /**
     * @param name
     * @param geonotesHandler
     * @param drawingList
     * @param path
     */
    private void openImage( final String name, final GeonotesHandler geonotesHandler, final List<DressedStroke> drawingList,
            final File file ) {
        final Shell shell = new Shell(SWT.SHELL_TRIM);
        shell.setText("Editing of " + name); //$NON-NLS-1$
        shell.setLayout(new GridLayout());

        Composite editorComposite = new Composite(shell, SWT.None);
        editorComposite.setLayout(new FillLayout());
        editorComposite.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
        ImageData imgD = new ImageData(file.getAbsolutePath());
        final Image img = new Image(shell.getDisplay(), imgD);
        int width = img.getBounds().width;
        int height = img.getBounds().height;
        simpleSWTImageEditor = new SimpleSWTImageEditor(editorComposite, SWT.None, drawingList, img, new Point(1000, 1000), true,
                true);

        Composite buttonComposite = new Composite(shell, SWT.None);
        buttonComposite.setLayout(new RowLayout());
        buttonComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
        Button okButton = new Button(buttonComposite, SWT.BORDER | SWT.PUSH);
        okButton.setText("Save");
        okButton.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                try {
                    if (simpleSWTImageEditor.isImageGotRotated()) {
                        ImageData rotatedImage = simpleSWTImageEditor.getRotatedImageData();
                        ImageLoader imageLoader = new ImageLoader();
                        imageLoader.data = new ImageData[]{rotatedImage};
                        File tempFile = File.createTempFile("imageeditor", "");
                        if (file.getName().toLowerCase().endsWith("jpg")) {
                            imageLoader.save(tempFile.getAbsolutePath(), SWT.IMAGE_JPEG);
                        } else if (file.getName().toLowerCase().endsWith("png")) {
                            imageLoader.save(tempFile.getAbsolutePath(), SWT.IMAGE_PNG);
                        }

                        geonotesHandler.deleteMedia(name);
                        geonotesHandler.addMedia(tempFile, name);

                        FileUtils.forceDelete(tempFile);
                    }
                    ArrayList<DressedStroke> drawing = (ArrayList<DressedStroke>) simpleSWTImageEditor.getDrawing();
                    geonotesHandler.setMediaDrawings(drawing, name);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                shell.dispose();
            }
        });
        Button cancelButton = new Button(buttonComposite, SWT.BORDER | SWT.PUSH);
        cancelButton.setText("Cancel");
        cancelButton.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                shell.dispose();
            }
        });

        Monitor primary = Display.getDefault().getPrimaryMonitor();
        Rectangle bounds = primary.getBounds();
        Rectangle rect = shell.getBounds();
        int x = bounds.x + (bounds.width - rect.width) / 2;
        int y = bounds.y + (bounds.height - rect.height) / 2;
        shell.setLocation(x, y);
        shell.setSize(600, 400);
        shell.open();
    }
}
