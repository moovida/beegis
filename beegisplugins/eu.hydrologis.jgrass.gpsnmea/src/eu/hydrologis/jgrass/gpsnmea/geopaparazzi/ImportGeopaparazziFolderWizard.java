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

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
import eu.hydrologis.jgrass.geonotes.GeonoteConstants.NOTIFICATION;
import eu.hydrologis.jgrass.geonotes.GeonotesHandler;
import eu.hydrologis.jgrass.geonotes.GeonotesPlugin;
import eu.hydrologis.jgrass.geonotes.fieldbook.FieldbookView;
import eu.hydrologis.jgrass.gpsnmea.GpsActivator;

/**
 * The wizard to import for geopaparazzi data.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@SuppressWarnings("nls")
public class ImportGeopaparazziFolderWizard extends Wizard implements IImportWizard {

    private static final String[] GEOPAPARAZZI_NOTES_DESCRIPTIONFIELDS = {"DESCRIPTION", "TIMESTAMP", "ALTIM"};
    private static final String GEOPAPARAZZI_NOTES_OUTPUTSHAPEFILENAME = "notes.shp";

    private ImportGeopaparazziFolderWizardPage mainPage;
    private static GeometryFactory gF = new GeometryFactory();
    private CoordinateReferenceSystem mapCrs;

    private boolean canFinish = true;

    private DateTimeFormatter dateTimeFormatterYYYYMMDDHHMM = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    public ImportGeopaparazziFolderWizard() {
        super();
    }

    @Override
    public boolean canFinish() {
        return canFinish;
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
                            File geopapDatabaseFile = new File(geopaparazziFolderPath, "geopaparazzi.db");

                            if (!geopapDatabaseFile.exists()) {
                                MessageDialog.openError(getShell(), "Missing database",
                                        "The geopaparazzi database file (geopaparazzi.db) is missing. Check the inserted path.");
                                return;
                            }

                            String outputFolderPath = mainPage.getOutputFolderPath();
                            File outputFolderFile = new File(outputFolderPath);

                            mapCrs = ApplicationGIS.getActiveMap().getViewportModel().getCRS();

                            Connection connection = null;
                            try {
                                // create a database connection
                                connection = DriverManager.getConnection("jdbc:sqlite:" + geopapDatabaseFile.getAbsolutePath());
                                if (geopapDatabaseFile.exists()) {
                                    /*
                                     * import notes as shapefile
                                     */
                                    notesToShapefile(connection, outputFolderFile, pm);

                                    /*
                                     * import gps logs as shapefiles, once as lines and once as points
                                     */
                                    gpsLogToShapefiles(connection, outputFolderFile, pm);
                                }
                                /*
                                 * import photos to geonotes if there are some
                                 */
                                mediaToGeonotes(geopapFolderFile, pm);

                            } catch (Exception e) {
                                String message = "An error occurred while importing from geopaparazzi.";
                                ExceptionDetailsDialog.openError(null, message, IStatus.ERROR, GpsActivator.PLUGIN_ID, e);
                            } finally {
                                try {
                                    if (connection != null)
                                        connection.close();
                                } catch (SQLException e) {
                                    // connection close failed.
                                    System.err.println(e);
                                }
                            }

                        }

                    });
                } catch (Exception e1) {
                    e1.printStackTrace();
                    String message = "An error occurred while extracting the data from the database.";
                    ExceptionDetailsDialog.openError(null, message, IStatus.ERROR, GpsActivator.PLUGIN_ID, e1);
                }
            }
        });

        return true;
    }

    public void init( IWorkbench workbench, IStructuredSelection selection ) {
        setWindowTitle("Geopaparazzi Import Wizard"); // NON-NLS-1
        setNeedsProgressMonitor(true);
        mainPage = new ImportGeopaparazziFolderWizardPage("Import GeoPaparazzi Data Folder", selection);
    }

    public void addPages() {
        super.addPages();
        addPage(mainPage);

        // make sure sqlite driver are there
        try {
            Class.forName("org.sqlite.JDBC");
            canFinish = true;
        } catch (Exception e) {
            String message = "An error occurred while loading the database drivers. Check your installation.";
            ExceptionDetailsDialog.openError(null, message, IStatus.ERROR, GpsActivator.PLUGIN_ID, e);
            canFinish = false;
        }
    }

    private void notesToShapefile( Connection connection, File outputFolderFile, IProgressMonitor pm ) throws Exception {
        File outputShapeFile = new File(outputFolderFile, GEOPAPARAZZI_NOTES_OUTPUTSHAPEFILENAME);

        SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
        b.setName("geopaparazzinotes"); //$NON-NLS-1$
        b.setCRS(mapCrs);
        b.add("the_geom", Point.class); //$NON-NLS-1$
        for( String fieldName : GEOPAPARAZZI_NOTES_DESCRIPTIONFIELDS ) {
            b.add(fieldName, String.class);
        }
        SimpleFeatureType featureType = b.buildFeatureType();
        MathTransform transform = CRS.findMathTransform(DefaultGeographicCRS.WGS84, mapCrs);
        pm.beginTask("Import notes...", IProgressMonitor.UNKNOWN);
        FeatureCollection<SimpleFeatureType, SimpleFeature> newCollection = FeatureCollections.newCollection();

        Session session = null;
        Transaction transaction = null;
        Statement statement = null;
        try {
            session = DatabasePlugin.getDefault().getActiveDatabaseConnection().openSession();
            transaction = session.beginTransaction();

            statement = connection.createStatement();
            statement.setQueryTimeout(30); // set timeout to 30 sec.

            ResultSet rs = statement.executeQuery("select lat, lon, altim, ts, text from notes");
            int i = 0;
            while( rs.next() ) {

                double lat = rs.getDouble("lat");
                double lon = rs.getDouble("lon");
                double altim = rs.getDouble("altim");
                String dateTimeString = rs.getString("ts");
                String text = rs.getString("text");

                if (lat == 0 || lon == 0) {
                    continue;
                }

                // save those points also in the database log
                GpsLogTable gpsLog = new GpsLogTable();
                DateTime utcTime = dateTimeFormatterYYYYMMDDHHMM.parseDateTime(dateTimeString);
                gpsLog.setUtcTime(utcTime);
                gpsLog.setEast(lon);
                gpsLog.setNorth(lat);
                gpsLog.setAltimetry(altim);
                gpsLog.setNumberOfTrackedSatellites(-1);
                gpsLog.setHorizontalDilutionOfPosition(-1);
                session.save(gpsLog);

                // and then create the features
                Coordinate c = new Coordinate(lon, lat);
                Point point = gF.createPoint(c);
                Geometry reprojectPoint = JTS.transform(point, transform);

                SimpleFeatureBuilder builder = new SimpleFeatureBuilder(featureType);
                Object[] values = new Object[]{reprojectPoint, text, dateTimeString, String.valueOf(altim)};
                builder.addAll(values);
                SimpleFeature feature = builder.buildFeature(featureType.getTypeName() + "." + i++);
                newCollection.add(feature);
                pm.worked(1);
            }
        } finally {
            pm.done();
            if (statement != null)
                statement.close();
            if (transaction != null)
                transaction.commit();
            if (session != null)
                session.close();
        }

        ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();
        Map<String, Serializable> params = new HashMap<String, Serializable>();
        params.put("url", outputShapeFile.toURI().toURL());
        params.put("create spatial index", Boolean.TRUE);
        ShapefileDataStore dStore = (ShapefileDataStore) factory.createNewDataStore(params);
        dStore.createSchema(featureType);
        dStore.forceSchemaCRS(mapCrs);

        FeatureUtilities.writeToShapefile(dStore, newCollection);

        FeatureUtilities.addServiceToCatalogAndMap(outputShapeFile.getAbsolutePath(), true, true, new NullProgressMonitor());

    }

    private void gpsLogToShapefiles( Connection connection, File outputFolderFile, IProgressMonitor pm ) throws Exception {
        File outputLinesShapeFile = new File(outputFolderFile, "gpslines.shp");

        Statement statement = connection.createStatement();
        statement.setQueryTimeout(30); // set timeout to 30 sec.

        List<GpsLog> logsList = new ArrayList<ImportGeopaparazziFolderWizard.GpsLog>();
        // first get the logs
        ResultSet rs = statement.executeQuery("select _id, startts, endts, text from gpslogs");
        while( rs.next() ) {
            long id = rs.getLong("_id");

            String startDateTimeString = rs.getString("startts");
            String endDateTimeString = rs.getString("endts");
            String text = rs.getString("text");

            GpsLog log = new GpsLog();
            log.id = id;
            log.startTime = startDateTimeString;
            log.endTime = endDateTimeString;
            log.text = text;
            logsList.add(log);
        }

        statement.close();

        /*
         * first read in all logs and insert them into the 
         * database
         */
        Session session = null;
        Transaction transaction = null;
        try {
            session = DatabasePlugin.getDefault().getActiveDatabaseConnection().openSession();
            transaction = session.beginTransaction();
            for( GpsLog log : logsList ) {
                long logId = log.id;
                String query = "select lat, lon, altim, ts from gpslog_data where logid = " + logId + " order by ts";

                Statement newStatement = connection.createStatement();
                newStatement.setQueryTimeout(30);
                ResultSet result = newStatement.executeQuery(query);

                while( result.next() ) {
                    double lat = result.getDouble("lat");
                    double lon = result.getDouble("lon");
                    double altim = result.getDouble("altim");
                    String dateTimeString = result.getString("ts");

                    GpsPoint gPoint = new GpsPoint();
                    gPoint.lon = lon;
                    gPoint.lat = lat;
                    gPoint.altim = altim;
                    gPoint.utctime = dateTimeString;
                    log.points.add(gPoint);

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

                newStatement.close();

            }
        } catch (Exception e) {
            e.printStackTrace();
            String message = "An error occurred while reading the gps logs.";
            ExceptionDetailsDialog.openError(null, message, IStatus.ERROR, GpsActivator.PLUGIN_ID, e);
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
        b.add("DESCR", String.class);
        SimpleFeatureType featureType = b.buildFeatureType();

        try {
            MathTransform transform = CRS.findMathTransform(DefaultGeographicCRS.WGS84, mapCrs);
            pm.beginTask("Import gps to lines...", logsList.size());
            FeatureCollection<SimpleFeatureType, SimpleFeature> newCollection = FeatureCollections.newCollection();
            int index = 0;
            for( GpsLog log : logsList ) {
                List<GpsPoint> points = log.points;

                List<Coordinate> coordList = new ArrayList<Coordinate>();
                String startDate = log.startTime;
                String endDate = log.endTime;
                for( GpsPoint gpsPoint : points ) {
                    Coordinate c = new Coordinate(gpsPoint.lon, gpsPoint.lat);
                    coordList.add(c);
                }
                Coordinate[] coordArray = (Coordinate[]) coordList.toArray(new Coordinate[coordList.size()]);
                if (coordArray.length < 2) {
                    continue;
                }
                LineString lineString = gF.createLineString(coordArray);
                LineString reprojectLineString = (LineString) JTS.transform(lineString, transform);
                MultiLineString multiLineString = gF.createMultiLineString(new LineString[]{reprojectLineString});

                SimpleFeatureBuilder builder = new SimpleFeatureBuilder(featureType);
                Object[] values = new Object[]{multiLineString, startDate, endDate, log.text};
                builder.addAll(values);
                SimpleFeature feature = builder.buildFeature(featureType.getTypeName() + "." + index++);

                newCollection.add(feature);
                pm.worked(1);
            }
            pm.done();

            ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();
            Map<String, Serializable> params = new HashMap<String, Serializable>();
            params.put("url", outputLinesShapeFile.toURI().toURL());
            params.put("create spatial index", Boolean.TRUE);
            ShapefileDataStore dStore = (ShapefileDataStore) factory.createNewDataStore(params);
            dStore.createSchema(featureType);
            dStore.forceSchemaCRS(mapCrs);

            FeatureUtilities.writeToShapefile(dStore, newCollection);

            FeatureUtilities.addServiceToCatalogAndMap(outputLinesShapeFile.getAbsolutePath(), true, true,
                    new NullProgressMonitor());

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

            pm.beginTask("Import gps to points...", logsList.size());
            FeatureCollection<SimpleFeatureType, SimpleFeature> newCollection = FeatureCollections.newCollection();
            int index = 0;
            for( GpsLog log : logsList ) {
                List<GpsPoint> gpsPointList = log.points;
                for( GpsPoint gpsPoint : gpsPointList ) {
                    Coordinate c = new Coordinate(gpsPoint.lon, gpsPoint.lat);
                    Point point = gF.createPoint(c);

                    Point reprojectPoint = (Point) JTS.transform(point, transform);
                    Object[] values = new Object[]{reprojectPoint, String.valueOf(gpsPoint.altim), gpsPoint.utctime};

                    SimpleFeatureBuilder builder = new SimpleFeatureBuilder(featureType);
                    builder.addAll(values);
                    SimpleFeature feature = builder.buildFeature(featureType.getTypeName() + "." + index++);
                    newCollection.add(feature);
                }
                pm.worked(1);
            }
            pm.done();

            ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();
            Map<String, Serializable> params = new HashMap<String, Serializable>();
            params.put("url", outputPointsShapeFile.toURI().toURL());
            params.put("create spatial index", Boolean.TRUE);
            ShapefileDataStore dStore = (ShapefileDataStore) factory.createNewDataStore(params);
            dStore.createSchema(featureType);
            dStore.forceSchemaCRS(mapCrs);

            FeatureUtilities.writeToShapefile(dStore, newCollection);

            FeatureUtilities.addServiceToCatalogAndMap(outputPointsShapeFile.getAbsolutePath(), true, true,
                    new NullProgressMonitor());

        } catch (Exception e1) {
            GpsActivator.log(e1.getLocalizedMessage(), e1);
            e1.printStackTrace();
        }
    }

    private void mediaToGeonotes( File geopapFolderFile, IProgressMonitor pm ) throws Exception {
        File folder = new File(geopapFolderFile, "media");
        if (!folder.exists()) {
            // try to see if it is an old version of geopaparazzi
            folder = new File(geopapFolderFile, "pictures");
            if (!folder.exists()) {
                // ignoring non existing things
                return;
            }
        }

        File[] listFiles = folder.listFiles();
        List<String> nonTakenFilesList = new ArrayList<String>();

        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMddHHmmss"); //$NON-NLS-1$
        pm.beginTask("Importing media...", listFiles.length);

        Session session = null;
        Transaction transaction = null;
        try {
            session = DatabasePlugin.getDefault().getActiveDatabaseConnection().openSession();
            transaction = session.beginTransaction();

            for( File file : listFiles ) {
                String name = file.getName();
                if (name.endsWith("jpg") || file.getName().endsWith("JPG") || file.getName().endsWith("png")
                        || file.getName().endsWith("PNG") || file.getName().endsWith("3gp")) {

                    String[] nameSplit = name.split("[_\\|.]"); //$NON-NLS-1$
                    String dateString = nameSplit[1];
                    String timeString = nameSplit[2];
                    DateTime dateTime = formatter.parseDateTime(dateString + timeString);

                    Properties locationProperties = new Properties();
                    String mediaPath = file.getAbsolutePath();
                    int lastDot = mediaPath.lastIndexOf("."); //$NON-NLS-1$
                    String nameNoExt = mediaPath.substring(0, lastDot);
                    String infoPath = nameNoExt + ".properties"; //$NON-NLS-1$
                    File infoFile = new File(infoPath);
                    if (!infoFile.exists()) {
                        nonTakenFilesList.add(mediaPath);
                        continue;
                    }
                    locationProperties.load(new FileInputStream(infoFile));
                    String azimuthString = locationProperties.getProperty("azimuth"); //$NON-NLS-1$
                    String latString = locationProperties.getProperty("latitude"); //$NON-NLS-1$
                    String lonString = locationProperties.getProperty("longitude"); //$NON-NLS-1$
                    String altimString = locationProperties.getProperty("altim"); //$NON-NLS-1$

                    Double azimuth = null;
                    if (azimuthString != null)
                        azimuth = Double.parseDouble(azimuthString);
                    double lat = 0.0;
                    double lon = 0.0;
                    if (latString.contains("/")) {
                        // this is an exif string
                        lat = exifFormat2degreeDecimal(latString);
                        lon = exifFormat2degreeDecimal(lonString);
                    } else {
                        lat = Double.parseDouble(latString);
                        lon = Double.parseDouble(lonString);
                    }
                    double altim = Double.parseDouble(altimString);

                    // save those points also in the database log
                    GpsLogTable gpsLog = new GpsLogTable();
                    gpsLog.setUtcTime(dateTime);
                    gpsLog.setEast(lon);
                    gpsLog.setNorth(lat);
                    gpsLog.setAltimetry(altim);
                    gpsLog.setNumberOfTrackedSatellites(-1);
                    gpsLog.setHorizontalDilutionOfPosition(-1);
                    session.save(gpsLog);

                    // and then create the photo geonote
                    GeonotesHandler geonotesHandler = new GeonotesHandler(lon, lat, name, "Imported from Geopaparazzi", PHOTO,
                            dateTime, azimuth, null, null, null);
                    geonotesHandler.addMedia(file, file.getName());

                    FieldbookView fieldBookView = GeonotesPlugin.getDefault().getFieldbookView();
                    if (fieldBookView != null) {
                        geonotesHandler.addObserver(fieldBookView);
                    }
                    geonotesHandler.notifyObservers(NOTIFICATION.NOTEADDED);

                }
                pm.worked(1);
            }
        } finally {
            pm.done();
            if (transaction != null)
                transaction.commit();
            if (session != null)
                session.close();
        }

        if (nonTakenFilesList.size() > 0) {
            final StringBuilder sB = new StringBuilder();
            sB.append("For the following images no *.properties file could be found:\n");
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
                    MessageDialog.openInformation(shell, "Info", "All photos were successfully imported.");
                }
            });
        }

    }
    private static class GpsPoint {
        public double lat;
        public double lon;
        public double altim;
        public String utctime;
    }

    private static class GpsLog {
        public long id;
        public String startTime;
        public String endTime;
        public String text;
        public List<GpsPoint> points = new ArrayList<GpsPoint>();

    }

    /**
     * Convert decimal degrees to exif format.
     * 
     * @param decimalDegree the angle in decimal format.
     * @return the exif format string.
     */
    @SuppressWarnings("nls")
    public static String degreeDecimal2ExifFormat( double decimalDegree ) {
        StringBuilder sb = new StringBuilder();
        sb.append((int) decimalDegree);
        sb.append("/1,");
        decimalDegree = (decimalDegree - (int) decimalDegree) * 60;
        sb.append((int) decimalDegree);
        sb.append("/1,");
        decimalDegree = (decimalDegree - (int) decimalDegree) * 60000;
        sb.append((int) decimalDegree);
        sb.append("/1000");
        return sb.toString();
    }

    /**
     * Convert exif format to decimal degree.
     * 
     * @param exifFormat the exif string of the gps position.
     * @return the decimal degree.
     */
    @SuppressWarnings("nls")
    public static double exifFormat2degreeDecimal( String exifFormat ) {
        // latitude=44/1,10/1,28110/1000
        String[] exifSplit = exifFormat.trim().split(",");

        String[] value = exifSplit[0].split("/");

        double tmp1 = Double.parseDouble(value[0]);
        double tmp2 = Double.parseDouble(value[1]);
        double degree = tmp1 / tmp2;

        value = exifSplit[1].split("/");
        tmp1 = Double.parseDouble(value[0]);
        tmp2 = Double.parseDouble(value[1]);
        double minutes = tmp1 / tmp2;

        value = exifSplit[2].split("/");
        tmp1 = Double.parseDouble(value[0]);
        tmp2 = Double.parseDouble(value[1]);
        double seconds = tmp1 / tmp2;

        double result = degree + (minutes / 60.0) + (seconds / 3600.0);
        return result;
    }

}
