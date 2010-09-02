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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import eu.hydrologis.jgrass.beegisutils.database.annotatedclasses.GeonotesTable;
import eu.hydrologis.jgrass.beegisutils.jgrassported.CompressionUtilities;
import eu.hydrologis.jgrass.beegisutils.jgrassported.DressedStroke;
import eu.hydrologis.jgrass.beegisutils.jgrassported.FileUtilities;
import eu.hydrologis.jgrass.geonotes.GeonoteConstants;
import eu.hydrologis.jgrass.geonotes.GeonotesHandler;
import eu.hydrologis.jgrass.geonotes.GeonotesPlugin;
import eu.hydrologis.jgrass.geonotes.GeonotesUI;
import eu.hydrologis.jgrass.geonotes.GeonoteConstants.NOTIFICATION;
import eu.hydrologis.jgrass.geonotes.fieldbook.FieldbookView;
import eu.hydrologis.jgrass.geonotes.fieldbook.GeonotesListViewer;

/**
 * Action to import {@link GeonotesUI}s.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class ImportNotesAction extends Action {

    private static final String NO_GEONOTE_ARCHIVE = "The supplied archive doesn't seem to be a geonotes archive.";
    private final GeonotesListViewer geonotesViewer;

    public ImportNotesAction( GeonotesListViewer geonotesViewer ) {
        super("Import geonotes archive");
        this.geonotesViewer = geonotesViewer;
    }

    public void run() {
        try {
            FileDialog fileDialog = new FileDialog(geonotesViewer.getTable().getShell(), SWT.OPEN);
            fileDialog.setFilterExtensions(new String[]{"*.zip"});
            String path = fileDialog.open();
            if (path != null && path.length() > 0) {
                importNotesArchive(path);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

    }

    /**
     * Imports an archive of geonotes into the database. 
     * 
     * @param path the path to the archive of geonotes.
     * @throws IOException
     * @throws FileNotFoundException
     * @throws ClassNotFoundException
     * @throws Exception
     */
    public void importNotesArchive( String path ) throws IOException, FileNotFoundException,
            ClassNotFoundException, Exception {
        File geonotesZipFile = new File(path);
        if (!geonotesZipFile.exists()) {
            throw new IOException("Geonotes zip file doesn't exist.");
        }

        String nameWithoutExtention = FileUtilities.getNameWithoutExtention(geonotesZipFile);
        String parentFolder = geonotesZipFile.getParent();
        CompressionUtilities.unzipFolder(geonotesZipFile.getAbsolutePath(), parentFolder);

        /*
         * see GeoNote class documentation to see folder structure.
         */
        File geonoteFolderFile = new File(geonotesZipFile.getParentFile(), nameWithoutExtention);
        if (!geonoteFolderFile.exists()) {
            throw new IOException(NO_GEONOTE_ARCHIVE);
        }

        if (geonoteFolderFile.isDirectory()) {

            File[] singleGeonoteFolders = geonoteFolderFile.listFiles();
            for( File singleGeonoteFolderFile : singleGeonoteFolders ) {
                /*
                 * only folders that are numbers can be used, others are 
                 * simply ignored
                 */
                try {
                    String name = singleGeonoteFolderFile.getName();
                    Integer.parseInt(name);
                } catch (NumberFormatException e) {
                    continue;
                }

                // check geonote properties serialized bin
                File geonoteBin = new File(singleGeonoteFolderFile,
                        GeonoteConstants.GEONOTE_BIN_PROPERTIES);
                if (!geonoteBin.exists()) {
                    throw new IOException(NO_GEONOTE_ARCHIVE);
                }

                FileInputStream fis = new FileInputStream(geonoteBin);
                ObjectInputStream in = new ObjectInputStream(fis);
                Object readObject = in.readObject();
                GeonotesTable geonotesTable = (GeonotesTable) readObject;
                in.close();
                fis.close();

                // persist the note
                GeonotesHandler geonotesHandler = new GeonotesHandler(geonotesTable.getEast(),
                        geonotesTable.getNorth(), geonotesTable.getTitle(),
                        geonotesTable.getInfo(), geonotesTable.getType(), geonotesTable
                                .getCreationDateTime(), geonotesTable
                                .getAzimut(), geonotesTable.getColor(), geonotesTable.getWidth(),
                        geonotesTable.getHeight());
                
                FieldbookView fieldBookView = GeonotesPlugin.getDefault().getFieldbookView();
                if (fieldBookView != null) {
                    geonotesHandler.addObserver(fieldBookView);
                }

                // check geonote drawings serialized bin
                File geonoteBinStrokes = new File(singleGeonoteFolderFile,
                        GeonoteConstants.GEONOTE_BIN_DRAWINGS);
                if (!geonoteBinStrokes.exists()) {
                    throw new IOException(NO_GEONOTE_ARCHIVE);
                }

                fis = new FileInputStream(geonoteBinStrokes);
                in = new ObjectInputStream(fis);
                readObject = in.readObject();
                DressedStroke[] noteDrawing = (DressedStroke[]) readObject;
                in.close();
                fis.close();

                ArrayList<DressedStroke> drawingsList = new ArrayList<DressedStroke>();
                for( DressedStroke dressedStroke : noteDrawing ) {
                    drawingsList.add(dressedStroke);
                }
                // insert drawings in db
                geonotesHandler.setDrawarea(drawingsList);

                // check geonote text
                File geonoteTextFile = new File(singleGeonoteFolderFile,
                        GeonoteConstants.GEONOTE_BIN_TEXT);
                String text = "";
                if (geonoteTextFile.exists()) {
                    BufferedReader bR = new BufferedReader(new FileReader(geonoteTextFile));
                    String line = null;
                    while( (line = bR.readLine()) != null ) {
                        text = text == null ? line : text + "\n" + line;
                    }
                }
                // insert text in db
                geonotesHandler.setTextarea(text);

                // check if there are medias
                File mediaFolderFile = new File(singleGeonoteFolderFile,
                        GeonoteConstants.MEDIA_FOLDER);
                if (mediaFolderFile.exists()) {
                    File[] mediaFiles = mediaFolderFile.listFiles();
                    for( File f : mediaFiles ) {
                        String fileName = f.getName();
                        if (!fileName.endsWith(GeonoteConstants.DRAWING_EXTENTION)) {
                            // add media in db
                            geonotesHandler.addMedia(f, fileName);
                        }
                    }
                }

                geonotesHandler.notifyObservers(NOTIFICATION.NOTEADDED);
            }

            File f = new File(parentFolder + File.separator + nameWithoutExtention);
            if (!FileUtilities.deleteFileOrDir(f)) {
                FileUtilities.deleteFileOrDirOnExit(f);
            }

        } else {
            throw new Exception(NO_GEONOTE_ARCHIVE);
        }
    }
}
