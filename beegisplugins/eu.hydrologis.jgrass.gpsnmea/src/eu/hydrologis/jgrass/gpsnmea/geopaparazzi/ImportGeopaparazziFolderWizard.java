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
package eu.hydrologis.jgrass.gpsnmea.geopaparazzi;

import static eu.hydrologis.jgrass.geonotes.GeonoteConstants.PHOTO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import net.refractions.udig.project.ui.ApplicationGIS;
import net.refractions.udig.ui.ExceptionDetailsDialog;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
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
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;

import eu.hydrologis.jgrass.beegisutils.database.annotatedclasses.GpsLogTable;
import eu.hydrologis.jgrass.beegisutils.jgrassported.FeatureUtilities;
import eu.hydrologis.jgrass.database.DatabasePlugin;
import eu.hydrologis.jgrass.geonotes.GeonotesHandler;
import eu.hydrologis.jgrass.geonotes.GeonotesPlugin;
import eu.hydrologis.jgrass.geonotes.GeonoteConstants.NOTIFICATION;
import eu.hydrologis.jgrass.geonotes.fieldbook.FieldbookView;
import eu.hydrologis.jgrass.gpsnmea.GpsActivator;

/**
 * The wizard to import for geopaparazzi data.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class ImportGeopaparazziFolderWizard extends Wizard implements IImportWizard {

    private static final String[] GEOPAPARAZZI_NOTES_DESCRIPTIONFIELDS = {"DESCRIPTION",
            "TIMESTAMP"};
    private static final String GEOPAPARAZZI_NOTES_OUTPUTSHAPEFILENAME = "notes.shp";
    private static final String GEOPAPARAZZI_NOTES_FOLDERNAME = "notes";

    private ImportGeopaparazziFolderWizardPage mainPage;
    private static GeometryFactory gF = new GeometryFactory();
    private CoordinateReferenceSystem mapCrs;

    private DateTimeFormatter dateTimeFormatterYYYYMMDDHHMM = DateTimeFormat
            .forPattern("yyyy-MM-dd HH:mm");

    public ImportGeopaparazziFolderWizard() {
        super();
    }

    public boolean performFinish() {
        Display.getDefault().asyncExec(new Runnable(){
            public void run() {
                try {
                    IWorkbench wb = PlatformUI.getWorkbench();
                    IProgressService ps = wb.getProgressService();
                    ps.busyCursorWhile(new IRunnableWithProgress(){

                        public void run( IProgressMonitor pm ) {

                            String geopaparazziFolderPath = mainPage.getGeopaparazziFolderPath();
                            File geopapFolderFile = new File(geopaparazziFolderPath);
                            String outputFolderPath = mainPage.getOutputFolderPath();
                            File outputFolderFile = new File(outputFolderPath);

                            mapCrs = ApplicationGIS.getActiveMap().getViewportModel().getCRS();
                            /*
                             * import notes as shapefile
                             */
                            notesToShapefile(geopapFolderFile, outputFolderFile, pm);

                            /*
                             * import gps logs as shapefiles, once as lines and once as points
                             */
                            gpsLogToShapefiles(geopapFolderFile, outputFolderFile, pm);

                            /*
                             * import photos to geonotes
                             */
                            picturesToGeonotes(geopapFolderFile, pm);

                        }

                    });
                } catch (InvocationTargetException e1) {
                    e1.printStackTrace();
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        });

        return true;
    }

    public void init( IWorkbench workbench, IStructuredSelection selection ) {
        setWindowTitle("Geopaparazzi Import Wizard"); // NON-NLS-1
        setNeedsProgressMonitor(true);
        mainPage = new ImportGeopaparazziFolderWizardPage("Import GeoPaparazzi Data Folder",
                selection);
    }

    public void addPages() {
        super.addPages();
        addPage(mainPage);
    }

    private void notesToShapefile( File geopapFolderFile, File outputFolderFile, IProgressMonitor pm ) {
        File folder = new File(geopapFolderFile, GEOPAPARAZZI_NOTES_FOLDERNAME);
        if (!folder.exists()) {
            // ignoring non existing things
            return;
        }
        File outputShapeFile = new File(outputFolderFile, GEOPAPARAZZI_NOTES_OUTPUTSHAPEFILENAME);
        File[] listFiles = folder.listFiles();

        SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
        b.setName("geopaparazzinotes"); //$NON-NLS-1$
        b.setCRS(mapCrs);
        b.add("the_geom", Point.class); //$NON-NLS-1$
        for( String fieldName : GEOPAPARAZZI_NOTES_DESCRIPTIONFIELDS ) {
            b.add(fieldName, String.class);
        }
        SimpleFeatureType featureType = b.buildFeatureType();

        try {
            MathTransform transform = CRS.findMathTransform(DefaultGeographicCRS.WGS84, mapCrs);
            pm.beginTask("Import notes...", listFiles.length);
            FeatureCollection<SimpleFeatureType, SimpleFeature> newCollection = FeatureCollections
                    .newCollection();
            for( int i = 0; i < listFiles.length; i++ ) {
                File noteFile = listFiles[i];

                FileInputStream fStream = new FileInputStream(noteFile);
                Properties p = new Properties();
                p.load(fStream);

                String time = p.getProperty("utctime");
                String lat = p.getProperty("lat");
                String lon = p.getProperty("lon");
                String text = p.getProperty("text");
                if (time == null || lat == null || lon == null || text == null) {
                    continue;
                }

                Coordinate c = new Coordinate(Double.parseDouble(lon), Double.parseDouble(lat));
                Point point = gF.createPoint(c);

                Geometry reprojectPoint = JTS.transform(point, transform);

                SimpleFeatureBuilder builder = new SimpleFeatureBuilder(featureType);
                Object[] values = new Object[]{reprojectPoint, text, time};
                builder.addAll(values);
                SimpleFeature feature = builder.buildFeature(featureType.getTypeName() + "." + i);
                newCollection.add(feature);
                pm.worked(1);
            }
            pm.done();

            ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();
            Map<String, URL> map = Collections.singletonMap("url", outputShapeFile.toURI().toURL());
            ShapefileDataStore dStore = (ShapefileDataStore) factory.createNewDataStore(map);
            dStore.createSchema(featureType);
            dStore.forceSchemaCRS(mapCrs);

            FeatureUtilities.writeToShapefile(dStore, newCollection);

            FeatureUtilities.addServiceToCatalogAndMap(outputShapeFile.getAbsolutePath(), true,
                    true, new NullProgressMonitor());

        } catch (Exception e1) {
            GpsActivator.log(e1.getLocalizedMessage(), e1);
            e1.printStackTrace();
        }

    }

    private void gpsLogToShapefiles( File geopapFolderFile, File outputFolderFile,
            IProgressMonitor pm ) {
        /*
         * first every log to single line of a shapefile
         */
        File folder = new File(geopapFolderFile, "gpslogs");
        if (!folder.exists()) {
            // ignoring non existing things
            return;
        }
        File outputLinesShapeFile = new File(outputFolderFile, "gpslines.shp");

        File[] listLogFiles = folder.listFiles(new FileFilter(){
            public boolean accept( File pathname ) {
                String name = pathname.getName();
                if (name.toUpperCase().startsWith("GPSLOG")) {
                    return true;
                }
                return false;
            }
        });
        Arrays.sort(listLogFiles);

        /*
         * first read in all logs and insert them into the 
         * database
         */
        Session session = null;
        Transaction transaction = null;
        LinkedHashMap<String, List<GpsPoint>> logsMap = new LinkedHashMap<String, List<GpsPoint>>();
        try {
            session = DatabasePlugin.getDefault().getActiveDatabaseConnection().openSession();
            transaction = session.beginTransaction();
            for( File file : listLogFiles ) {

                ArrayList<GpsPoint> pointList = new ArrayList<GpsPoint>();
                logsMap.put(file.getName(), pointList);

                BufferedReader bR = new BufferedReader(new FileReader(file));
                String line = null;
                while( (line = bR.readLine()) != null ) {
                    String[] lineSplit = line.split(",");
                    GpsPoint gPoint = new GpsPoint();
                    gPoint.lon = Double.parseDouble(lineSplit[0]);
                    gPoint.lat = Double.parseDouble(lineSplit[1]);
                    gPoint.altim = Double.parseDouble(lineSplit[2]);
                    gPoint.utctime = lineSplit[3];
                    pointList.add(gPoint);

                    GpsLogTable gpsLog = new GpsLogTable();
                    DateTime utcTime = dateTimeFormatterYYYYMMDDHHMM.parseDateTime(gPoint.utctime);
                    gpsLog.setUtcTime(utcTime);
                    gpsLog.setEast(gPoint.lon);
                    gpsLog.setNorth(gPoint.lat);
                    gpsLog.setAltimetry(gPoint.altim);
                    gpsLog.setNumberOfTrackedSatellites(-1);
                    gpsLog.setHorizontalDilutionOfPosition(-1);

                    session.save(gpsLog);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            String message = "An error occurred while reading the gps logs.";
            ExceptionDetailsDialog.openError(null, message, IStatus.ERROR, GpsActivator.PLUGIN_ID,
                    e);
            return;
        } finally {
            transaction.commit();
            session.close();
        }

        /*
         * create the lines shapefile
         */
        SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
        b.setName("geopaparazzinotes");
        b.setCRS(mapCrs);
        b.add("the_geom", MultiLineString.class);
        b.add("STARTDATE", String.class);
        b.add("ENDDATE", String.class);
        SimpleFeatureType featureType = b.buildFeatureType();

        try {
            MathTransform transform = CRS.findMathTransform(DefaultGeographicCRS.WGS84, mapCrs);
            pm.beginTask("Import gps to lines...", listLogFiles.length);
            FeatureCollection<SimpleFeatureType, SimpleFeature> newCollection = FeatureCollections
                    .newCollection();
            Set<String> keySet = logsMap.keySet();
            for( String name : keySet ) {
                List<GpsPoint> gpsPointList = logsMap.get(name);

                List<Coordinate> coordList = new ArrayList<Coordinate>();
                String startDate = gpsPointList.get(0).utctime;
                String endDate = gpsPointList.get(gpsPointList.size() - 1).utctime;
                for( GpsPoint gpsPoint : gpsPointList ) {
                    Coordinate c = new Coordinate(gpsPoint.lon, gpsPoint.lat);
                    coordList.add(c);
                }
                Coordinate[] coordArray = (Coordinate[]) coordList.toArray(new Coordinate[coordList
                        .size()]);
                if (coordArray.length < 2) {
                    continue;
                }
                LineString lineString = gF.createLineString(coordArray);
                LineString reprojectLineString = (LineString) JTS.transform(lineString, transform);
                MultiLineString multiLineString = gF
                        .createMultiLineString(new LineString[]{reprojectLineString});

                SimpleFeatureBuilder builder = new SimpleFeatureBuilder(featureType);
                Object[] values = new Object[]{multiLineString, startDate, endDate};
                builder.addAll(values);
                SimpleFeature feature = builder
                        .buildFeature(featureType.getTypeName() + "." + name);

                newCollection.add(feature);
                pm.worked(1);
            }
            pm.done();

            ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();
            Map<String, URL> map = Collections.singletonMap("url", outputLinesShapeFile.toURI()
                    .toURL());
            ShapefileDataStore dStore = (ShapefileDataStore) factory.createNewDataStore(map);
            dStore.createSchema(featureType);
            dStore.forceSchemaCRS(mapCrs);

            FeatureUtilities.writeToShapefile(dStore, newCollection);

            FeatureUtilities.addServiceToCatalogAndMap(outputLinesShapeFile.getAbsolutePath(),
                    true, true, new NullProgressMonitor());

        } catch (Exception e1) {
            GpsActivator.log(e1.getLocalizedMessage(), e1);
            e1.printStackTrace();
        }
        /*
         * create the points shapefile
         */

        File outputPointsShapeFile = new File(outputFolderFile, "gpspoints.shp");

        b = new SimpleFeatureTypeBuilder();
        b.setName("geopaparazzinotes");
        b.setCRS(mapCrs);
        b.add("the_geom", Point.class);
        b.add("ALTIMETRY", String.class);
        b.add("DATE", String.class);
        featureType = b.buildFeatureType();

        try {
            MathTransform transform = CRS.findMathTransform(DefaultGeographicCRS.WGS84, mapCrs);

            pm.beginTask("Import gps to points...", listLogFiles.length);
            FeatureCollection<SimpleFeatureType, SimpleFeature> newCollection = FeatureCollections
                    .newCollection();
            Set<String> keySet = logsMap.keySet();
            for( String name : keySet ) {
                List<GpsPoint> gpsPointList = logsMap.get(name);
                for( GpsPoint gpsPoint : gpsPointList ) {
                    Coordinate c = new Coordinate(gpsPoint.lon, gpsPoint.lat);
                    Point point = gF.createPoint(c);

                    Point reprojectPoint = (Point) JTS.transform(point, transform);
                    Object[] values = new Object[]{reprojectPoint, String.valueOf(gpsPoint.altim),
                            gpsPoint.utctime};

                    SimpleFeatureBuilder builder = new SimpleFeatureBuilder(featureType);
                    builder.addAll(values);
                    SimpleFeature feature = builder.buildFeature(featureType.getTypeName() + "."
                            + name);
                    newCollection.add(feature);
                }
                pm.worked(1);
            }
            pm.done();

            ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();
            Map<String, URL> map = Collections.singletonMap("url", outputPointsShapeFile.toURI()
                    .toURL());
            ShapefileDataStore dStore = (ShapefileDataStore) factory.createNewDataStore(map);
            dStore.createSchema(featureType);
            dStore.forceSchemaCRS(mapCrs);

            FeatureUtilities.writeToShapefile(dStore, newCollection);

            FeatureUtilities.addServiceToCatalogAndMap(outputPointsShapeFile.getAbsolutePath(),
                    true, true, new NullProgressMonitor());

        } catch (Exception e1) {
            GpsActivator.log(e1.getLocalizedMessage(), e1);
            e1.printStackTrace();
        }
    }

    private void picturesToGeonotes( File geopapFolderFile, IProgressMonitor pm ) {
        File folder = new File(geopapFolderFile, "pictures");
        if (!folder.exists()) {
            // ignoring non existing things
            return;
        }

        File[] listFiles = folder.listFiles();
        List<String> nonTakenFilesList = new ArrayList<String>();

        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMddHHmmss"); //$NON-NLS-1$
        pm.beginTask("Importing pictures...", listFiles.length);
        for( File file : listFiles ) {
            String name = file.getName();
            if (name.endsWith("jpg") || file.getName().endsWith("JPG")
                    || file.getName().endsWith("png") || file.getName().endsWith("PNG")) {

                try {
                    String[] nameSplit = name.split("[_\\|.]"); //$NON-NLS-1$
                    String dateString = nameSplit[1];
                    String timeString = nameSplit[2];
                    DateTime dateTime = formatter.parseDateTime(dateString + timeString);

                    Properties locationProperties = new Properties();
                    String picturePath = file.getAbsolutePath();
                    int lastDot = picturePath.lastIndexOf("."); //$NON-NLS-1$
                    String nameNoExt = picturePath.substring(0, lastDot);
                    String infoPath = nameNoExt + ".properties"; //$NON-NLS-1$
                    File infoFile = new File(infoPath);
                    if (!infoFile.exists()) {
                        nonTakenFilesList.add(picturePath);
                        continue;
                    }
                    locationProperties.load(new FileInputStream(infoFile));
                    String azimuthString = locationProperties.getProperty("azimuth"); //$NON-NLS-1$
                    String latString = locationProperties.getProperty("latitude"); //$NON-NLS-1$
                    String lonString = locationProperties.getProperty("longitude"); //$NON-NLS-1$

                    Double azimuth = null;
                    if (azimuthString != null)
                        azimuth = Double.parseDouble(azimuthString);
                    double lat = Double.parseDouble(latString);
                    double lon = Double.parseDouble(lonString);

                    GeonotesHandler geonotesHandler = new GeonotesHandler(lon, lat, name,
                            "Imported from Geopaparazzi", PHOTO, dateTime,
                            DefaultGeographicCRS.WGS84.toWKT(), azimuth, null, null, null);
                    geonotesHandler.addMedia(file, file.getName());

                    FieldbookView fieldBookView = GeonotesPlugin.getDefault().getFieldbookView();
                    if (fieldBookView != null) {
                        geonotesHandler.addObserver(fieldBookView);
                    }
                    geonotesHandler.notifyObservers(NOTIFICATION.NOTEADDED);
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }

            }
            pm.worked(1);
        }
        pm.done();

        if (nonTakenFilesList.size() > 0) {
            final StringBuilder sB = new StringBuilder();
            sB.append("For the following images no *.info file could be found:\n");
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
                    MessageDialog.openInformation(shell, "Info",
                            "All photos were successfully imported.");
                }
            });
        }

    }
    private class GpsPoint {
        public double lat;
        public double lon;
        public double altim;
        public String utctime;
    }

}
