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

import i18n.geonotes.Messages;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import net.refractions.udig.ui.ExceptionDetailsDialog;
import net.refractions.udig.ui.PlatformGIS;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;

import eu.hydrologis.jgrass.beegisutils.database.annotatedclasses.GeonotesMediaboxTable;
import eu.hydrologis.jgrass.geonotes.actions.ClearEntriesAction;
import eu.hydrologis.jgrass.geonotes.actions.ClearSelectedEntriesAction;
import eu.hydrologis.jgrass.geonotes.actions.OpenSelectedEntryAction;
import eu.hydrologis.jgrass.geonotes.actions.OpenSelectedEntryWithDefaultAction;

/**
 * The table holding all the saved media. It supports dnd.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class MediaBoxUI {
    private final TableViewer lv;
    /**
     * the file entries to keep for visualization
     */
    private List<DndFile> dndfiles = null;

    private final GeonotesHandler geonotesHandler;

    public MediaBoxUI( Composite parent, int style, GeonotesHandler geonotesHandler ) {
        this.geonotesHandler = geonotesHandler;
        dndfiles = new ArrayList<DndFile>();

        lv = new TableViewer(parent, style);
        lv.setContentProvider(new ArrayContentProvider());
        lv.setLabelProvider(new MediaListLabelProvider());
        lv.setInput(null);
        lv.getControl().setBackground(geonotesHandler.getColor(parent.getDisplay()));

        // drop support
        Transfer[] transferTypes = new Transfer[]{TextTransfer.getInstance(), FileTransfer.getInstance()};
        int dndOperations = DND.DROP_MOVE | DND.DROP_LINK;
        lv.addDropSupport(dndOperations, transferTypes, new FileDropListener());

        IDoubleClickListener openSelectedAction = new OpenSelectedEntryAction(Messages
                .getString("GeonoteMediaBox.openselectedmedia"), lv); //$NON-NLS-1$
        lv.addDoubleClickListener(openSelectedAction);
        // add a popup
        MenuManager popManager = new MenuManager();
        Menu menu = popManager.createContextMenu(lv.getTable());
        lv.getTable().setMenu(menu);
        // open with default viewer
        IAction openDefaultAction = new OpenSelectedEntryWithDefaultAction("Open with system viewer", lv); //$NON-NLS-1$
        popManager.add(openDefaultAction);
        popManager.add(new Separator());
        // clear all entries
        IAction clearAction = new ClearEntriesAction(Messages.getString("GeonoteMediaBox.clearmediabox"), lv, dndfiles); //$NON-NLS-1$
        popManager.add(clearAction);
        // clear all entries
        IAction deleteSelectedAction = new ClearSelectedEntriesAction(
                Messages.getString("GeonoteMediaBox.deleteselectedmedi"), lv, //$NON-NLS-1$
                dndfiles);
        popManager.add(deleteSelectedAction);

    }

    public TableViewer getListViewer() {
        return lv;
    }

    private class MediaListLabelProvider extends LabelProvider {
        public Image getImage( Object arg0 ) {
            if (arg0 instanceof DndFile) {
                DndFile dndFile = (DndFile) arg0;
                ImageDescriptor imageFromFile = dndFile.getImage();
                if (imageFromFile == null)
                    return null;
                return imageFromFile.createImage();
            }
            return null;
        }

        public String getText( Object arg0 ) {
            if (arg0 instanceof DndFile) {
                DndFile dndFile = (DndFile) arg0;
                return dndFile.name;
            }
            return null;
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
            List<File> dataList = new ArrayList<File>();
            for( String string : data ) {
                File file = new File(string);
                if (file.exists() && !file.isDirectory()) {
                    dataList.add(file);
                }
            }
            addNewMediaFiles(dataList);
        }

        public void dropAccept( DropTargetEvent arg0 ) {
        }

    }

    private synchronized void updateTable() {
        Display.getDefault().syncExec(new Runnable(){
            public void run() {
                try {
                    lv.setInput(dndfiles.toArray(new DndFile[dndfiles.size()]));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public List<DndFile> getAllMediaList() {
        return dndfiles;
    }

    /**
     * Adds new media to the database and list.
     * 
     * @param mediaFiles a list of files to add.
     */
    public void addNewMediaFiles( final List<File> mediaFiles ) {
        IRunnableWithProgress operation = new IRunnableWithProgress(){
            public void run( IProgressMonitor pm ) throws InvocationTargetException, InterruptedException {
                pm.beginTask("Adding media...", mediaFiles.size());
                try {
                    for( File mediaFile : mediaFiles ) {
                        DndFile dndF = new DndFile(geonotesHandler);
                        dndF.file = mediaFile;
                        String fileName = mediaFile.getName();
                        dndF.name = fileName;

                        String ext = null;
                        if (mediaFile.isDirectory()) {
                            ext = "folder"; //$NON-NLS-1$
                        } else {
                            int lastDot = fileName.lastIndexOf('.');
                            ext = fileName.substring(lastDot + 1);
                        }
                        dndF.iconPath = DndFile.getIconForExtention(ext);
                        dndfiles.add(dndF);

                        try {
                            geonotesHandler.addMedia(mediaFile, fileName);
                        } catch (Exception e) {
                            e.printStackTrace();
                            String message = MessageFormat.format(
                                    "An error occurred while adding the file : {0} to the mediabox.", fileName);
                            ExceptionDetailsDialog.openError(null, message, IStatus.ERROR, GeonotesPlugin.PLUGIN_ID, e);
                        }
                        pm.worked(1);
                    }
                } finally {
                    pm.done();
                }
                updateTable();
            }
        };
        PlatformGIS.runInProgressDialog("Adding media...", true, operation, true);
    }

    /**
     * Loads the existing media names as {@link DndFile} to be visualized in the list.
     * 
     * <p>
     * This doesn't supply a path to the {@link DndFile}, since the data 
     * are still in the database.
     * </p> 
     * 
     * @throws Exception
     */
    public void loadExistingMedia() throws Exception {
        List<GeonotesMediaboxTable> geonotesMediaboxTables = geonotesHandler.getGeonotesMediaboxTables(null);
        for( GeonotesMediaboxTable geonotesMediaboxTable : geonotesMediaboxTables ) {
            String mediaName = geonotesMediaboxTable.getMediaName();

            DndFile dndF = new DndFile(geonotesHandler);
            dndF.name = mediaName;

            int lastDot = mediaName.lastIndexOf('.');
            String ext = mediaName.substring(lastDot + 1);
            dndF.iconPath = DndFile.getIconForExtention(ext);
            dndfiles.add(dndF);
        }
        updateTable();

    }
}
