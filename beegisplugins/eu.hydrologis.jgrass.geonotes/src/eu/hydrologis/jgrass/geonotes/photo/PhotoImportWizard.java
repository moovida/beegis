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
package eu.hydrologis.jgrass.geonotes.photo;

import static eu.hydrologis.jgrass.geonotes.GeonoteConstants.PHOTO;
import static org.jgrasstools.gears.libs.modules.JGTConstants.utcDateFormatterYYYYMMDDHHMMSS;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import net.refractions.udig.ui.ExceptionDetailsDialog;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.jgrasstools.gears.utils.time.UtcTimeUtilities;
import org.joda.time.DateTime;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import eu.hydrologis.jgrass.beegisutils.jgrassported.FeatureUtilities;
import eu.hydrologis.jgrass.geonotes.GeonoteConstants.NOTIFICATION;
import eu.hydrologis.jgrass.geonotes.GeonotesHandler;
import eu.hydrologis.jgrass.geonotes.GeonotesPlugin;
import eu.hydrologis.jgrass.geonotes.fieldbook.FieldbookView;
import eu.hydrologis.jgrass.geonotes.util.ExifHandler;

/**
 * Photo import wizard.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class PhotoImportWizard extends Wizard implements IImportWizard {
    private PhotoImportWizardPage mainPage;
    private float shift = 0;
    private int intervalMinutes;
    private boolean doNotImport;
    private boolean createFeatureLayer;
    private GeometryFactory gf = new GeometryFactory();

    public PhotoImportWizard() {
        super();
    }

    public boolean performFinish() {
        final FieldbookView fieldBookView = GeonotesPlugin.getDefault().getFieldbookView();
        final String path = mainPage.getPhotoFolder();
        shift = mainPage.getTime();
        intervalMinutes = mainPage.getIntervalMinutes();
        doNotImport = mainPage.getDoNotImport();
        createFeatureLayer = mainPage.doCreateFeatureLayer();

        Display.getDefault().asyncExec(new Runnable(){
            public void run() {
                try {
                    IWorkbench wb = PlatformUI.getWorkbench();
                    IProgressService ps = wb.getProgressService();
                    ps.busyCursorWhile(new IRunnableWithProgress(){
                        public void run( IProgressMonitor pm ) {
                            File f = new File(path);
                            File[] listFiles = f.listFiles(new FilenameFilter(){
                                public boolean accept( File dir, String name ) {
                                    return name.endsWith(".jpg") || name.endsWith(".JPG");
                                }
                            });
                            HashMap<DateTime, List<File>> imageFiles = new HashMap<DateTime, List<File>>();
                            HashMap<DateTime, Coordinate> timestamp2Coordinates = new HashMap<DateTime, Coordinate>();
                            List<String> nonTakenFilesList = new ArrayList<String>();

                            pm.beginTask("Browsing pictures...", listFiles.length);
                            for( File file : listFiles ) {
                                try {
                                    HashMap<String, String> metaData = ExifHandler.readMetaData(file);
                                    DateTime creationDatetimeUtc = ExifHandler.getCreationDatetimeUtc(metaData);

                                    // correct with the given shift
                                    int secShift = (int) (shift / 1000f);
                                    creationDatetimeUtc = creationDatetimeUtc.plusSeconds(secShift);
                                    // search for gps points of that timestamp
                                    Coordinate coordinate = GeonotesHandler.getGpsCoordinateForTimeStamp(creationDatetimeUtc,
                                            intervalMinutes);

                                    if (coordinate == null) {
                                        // could not find date
                                        nonTakenFilesList.add(file.getAbsolutePath());
                                    } else {
                                        List<File> fileList = imageFiles.get(creationDatetimeUtc);
                                        if (fileList == null) {
                                            fileList = new ArrayList<File>();
                                            imageFiles.put(creationDatetimeUtc, fileList);
                                        }
                                        fileList.add(file);
                                        timestamp2Coordinates.put(creationDatetimeUtc, coordinate);
                                    }
                                    pm.worked(1);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            pm.done();

                            List<Object[]> featureObjectList = new ArrayList<Object[]>();

                            Set<Entry<DateTime, List<File>>> timeSet = imageFiles.entrySet();
                            if (!doNotImport) {
                                pm.beginTask("Adding EXIF tags and importing matching photos...", timeSet.size());
                            } else {
                                pm.beginTask("Adding EXIF tags to matching photos...", timeSet.size());
                            }
                            for( Entry<DateTime, List<File>> entry : timeSet ) {
                                try {

                                    DateTime timestamp = entry.getKey();
                                    List<File> fileList = entry.getValue();

                                    Coordinate coordinate = timestamp2Coordinates.get(timestamp);

                                    // set gps exif tags
                                    StringBuilder sB = new StringBuilder("");
                                    for( File file : fileList ) {
                                        sB.append(file.getName());
                                        sB.append(" ");

                                        ExifHandler.writeGPSTagsToImage(coordinate.y, coordinate.x, file);

                                        if (createFeatureLayer) {
                                            // handle feature obj
                                            Object[] featureObjects = new Object[6];
                                            featureObjects[0] = gf.createPoint(coordinate);
                                            featureObjects[1] = file.getName();
                                            featureObjects[2] = timestamp.toString(utcDateFormatterYYYYMMDDHHMMSS);
                                            featureObjectList.add(featureObjects);
                                        }
                                    }

                                    if (!doNotImport) {
                                        String title = sB.toString();
                                        String info = "Date:" + UtcTimeUtilities.toStringWithMinutes(timestamp) + "\nN:"
                                                + coordinate.y + "\nE:" + coordinate.x;

                                        GeonotesHandler geonotesHandler = new GeonotesHandler(coordinate.x, coordinate.y, title,
                                                info, PHOTO, timestamp, null, null, null, null);
                                        for( File mFile : fileList ) {
                                            geonotesHandler.addMedia(mFile, mFile.getName());
                                        }

                                        if (fieldBookView != null) {
                                            geonotesHandler.addObserver(fieldBookView);
                                        }
                                        geonotesHandler.notifyObservers(NOTIFICATION.NOTEADDED);
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                pm.worked(1);
                            }
                            pm.done();

                            /*
                             * dump feature layer
                             */
                            if (createFeatureLayer) {
                                SimpleFeatureCollection newCollection = FeatureCollections.newCollection();
                                SimpleFeatureTypeBuilder ftypeBuilder = new SimpleFeatureTypeBuilder();
                                ftypeBuilder.setName("pictureslayer");
                                ftypeBuilder.setCRS(DefaultGeographicCRS.WGS84);
                                ftypeBuilder.add("the_geom", Point.class);
                                ftypeBuilder.add("name", String.class);
                                ftypeBuilder.add("date", String.class);
                                SimpleFeatureType ftype = ftypeBuilder.buildFeatureType();
                                int id = 0;
                                for( Object[] objects : featureObjectList ) {
                                    SimpleFeatureBuilder builder = new SimpleFeatureBuilder(ftype);
                                    builder.addAll(objects);
                                    SimpleFeature feature = builder.buildFeature(ftype.getTypeName() + "." + id++);
                                    newCollection.add(feature);
                                }
                                try {
                                    FeatureUtilities.featureCollectionToTempLayer(newCollection);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            /*
                             * handle not matched pics
                             */
                            if (nonTakenFilesList.size() > 0) {
                                final StringBuilder sB = new StringBuilder();
                                sB.append("For the following images no gps point within the threshold could be found:\n");
                                for( String p : nonTakenFilesList ) {
                                    sB.append(p).append("\n");
                                }

                                Display.getDefault().asyncExec(new Runnable(){
                                    public void run() {
                                        Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
                                        MessageDialog.openWarning(shell, "Warning", sB.toString());
                                    }
                                });
                            } else {
                                Display.getDefault().asyncExec(new Runnable(){
                                    public void run() {
                                        Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
                                        if (!doNotImport) {
                                            MessageDialog.openInformation(shell, "Info",
                                                    "All photos were successfully tagged and imported.");
                                        } else {
                                            MessageDialog.openInformation(shell, "Info", "All photos were successfully tagged.");
                                        }
                                    }
                                });
                            }
                        }
                    });
                } catch (Exception e1) {
                    e1.printStackTrace();
                    String message = "An error occurred while importing pictures";
                    ExceptionDetailsDialog.openError(null, message, IStatus.ERROR, GeonotesPlugin.PLUGIN_ID, e1);
                }
            }
        });

        return true;
    }
    public void init( IWorkbench workbench, IStructuredSelection selection ) {
        setWindowTitle("Photo Import Wizard"); // NON-NLS-1
        setNeedsProgressMonitor(true);
        mainPage = new PhotoImportWizardPage("Import Photos", selection); // NON-NLS-1
    }

    public void addPages() {
        super.addPages();
        addPage(mainPage);
    }

}
