package eu.hydrologis.jgrass.gpsnmea.export;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import net.refractions.udig.project.IMap;
import net.refractions.udig.project.ui.ApplicationGIS;
import net.refractions.udig.ui.PlatformGIS;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
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
import org.joda.time.DateTime;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import schema.gpx_1_1.BoundsType;
import schema.gpx_1_1.GpxType;
import schema.gpx_1_1.MetadataType;
import schema.gpx_1_1.ObjectFactory;
import schema.gpx_1_1.TrkType;
import schema.gpx_1_1.TrksegType;
import schema.gpx_1_1.WptType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;

import eu.hydrologis.jgrass.beegisutils.BeegisUtilsPlugin;
import eu.hydrologis.jgrass.beegisutils.jgrassported.FeatureUtilities;
import eu.hydrologis.jgrass.gpsnmea.GpsActivator;
import eu.hydrologis.jgrass.gpsnmea.db.DatabaseManager;
import eu.hydrologis.jgrass.gpsnmea.gps.GpsPoint;

public class ExportGpsLogWizard extends Wizard implements IExportWizard {

    private ExportGpsLogWizardPage mainPage;
    private static GeometryFactory gF = new GeometryFactory();
    private CoordinateReferenceSystem mapCrs;

    public ExportGpsLogWizard() {
        super();
    }

    public boolean performFinish() {
        PlatformGIS.asyncInDisplayThread(new Runnable(){
            public void run() {
                try {
                    IWorkbench wb = PlatformUI.getWorkbench();
                    IProgressService ps = wb.getProgressService();
                    ps.busyCursorWhile(new IRunnableWithProgress(){

                        public void run( IProgressMonitor pm ) {

                            String startDateStr = mainPage.getStartDateStr();
                            String endDateStr = mainPage.getEndDateStr();
                            String splitIntervalStr = mainPage.getSplitIntervalStr();
                            int splitIntervalMinutes = Integer.parseInt(splitIntervalStr);
                            String filePath = mainPage.getFilePath();
                            boolean isLine = mainPage.isLine();
                            boolean isShp = mainPage.isShp();

                            mapCrs = ApplicationGIS.getActiveMap().getViewportModel().getCRS();
                            DatabaseManager dbMan = GpsActivator.getDefault().getDatabaseManager();
                            DateTime from = null;
                            DateTime to = null;
                            try {
                                from = BeegisUtilsPlugin.dateTimeFormatterYYYYMMDDHHMM.parseDateTime(startDateStr);
                            } catch (Exception e) {
                                // not handled, since the default is used
                            }
                            try {
                                to = BeegisUtilsPlugin.dateTimeFormatterYYYYMMDDHHMM.parseDateTime(endDateStr);
                            } catch (Exception e) {
                                // not handled, since the default is used
                            }

                            try {
                                pm.beginTask("Extract points from database...", IProgressMonitor.UNKNOWN);
                                List<GpsPoint> gpsPointBetweenTimeStamp = dbMan.getGpsPointBetweenTimeStamp(from, to);
                                pm.done();

                                if (isShp) {
                                    if (isLine) {
                                        exportToShpLine(gpsPointBetweenTimeStamp, splitIntervalMinutes, filePath, pm);
                                    } else {
                                        exportToShpPoint(gpsPointBetweenTimeStamp, filePath, pm);
                                    }
                                } else {
                                    if (isLine) {
                                        exportToGpxLine(gpsPointBetweenTimeStamp, splitIntervalMinutes, filePath, pm);
                                    } else if (!isLine && !isShp) {
                                        exportToGpxPoint(gpsPointBetweenTimeStamp, filePath, pm);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    });
                } catch (InvocationTargetException e1) {
                    e1.printStackTrace();
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }, true);

        return true;
    }
    public void init( IWorkbench workbench, IStructuredSelection selection ) {
        setWindowTitle("Gps log export wizard"); // NON-NLS-1
        setNeedsProgressMonitor(true);
        mainPage = new ExportGpsLogWizardPage("Export Gps Log to shapefile", selection); // NON-NLS-1
    }

    public void addPages() {
        super.addPages();
        addPage(mainPage);
    }

    private void exportToShpPoint( List<GpsPoint> gpsPointBetweenTimeStamp, String filePath, IProgressMonitor pm ) {
        if (!filePath.endsWith("shp")) {
            filePath = filePath + ".shp";
        }

        SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
        b.setName("gpspoints");
        b.setCRS(mapCrs);
        b.add("the_geom", Point.class);
        b.add("TIMESTAMP", String.class);
        SimpleFeatureType featureType = b.buildFeatureType();

        try {
            MathTransform transform = CRS.findMathTransform(DefaultGeographicCRS.WGS84, mapCrs);
            pm.beginTask("Dump points...", gpsPointBetweenTimeStamp.size());
            FeatureCollection<SimpleFeatureType, SimpleFeature> newCollection = FeatureCollections.newCollection();

            for( int i = 0; i < gpsPointBetweenTimeStamp.size(); i++ ) {
                GpsPoint gpsPoint = gpsPointBetweenTimeStamp.get(i);

                Coordinate c = new Coordinate(gpsPoint.longitude, gpsPoint.latitude);
                String t = gpsPoint.utcDateTime.toString(BeegisUtilsPlugin.dateTimeFormatterYYYYMMDDHHMMSS);
                Point point = gF.createPoint(c);
                Geometry reprojectPoint = JTS.transform(point, transform);

                SimpleFeatureBuilder builder = new SimpleFeatureBuilder(featureType);
                Object[] values = new Object[]{reprojectPoint, t};
                builder.addAll(values);
                // build the feature with provided ID
                SimpleFeature feature = builder.buildFeature(featureType.getTypeName() + "." + i);
                newCollection.add(feature);
                pm.worked(1);
            }
            pm.done();

            ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();
            File f = new File(filePath);
            Map<String, URL> map = Collections.singletonMap("url", f.toURI().toURL());
            ShapefileDataStore dStore = (ShapefileDataStore) factory.createNewDataStore(map);
            dStore.createSchema(featureType);
            IMap activeMap = ApplicationGIS.getActiveMap();
            CoordinateReferenceSystem mapCrs = activeMap.getViewportModel().getCRS();
            dStore.forceSchemaCRS(mapCrs);

            FeatureUtilities.writeToShapefile(dStore, newCollection);

            FeatureUtilities.addServiceToCatalogAndMap(filePath, true, true, new NullProgressMonitor());

        } catch (Exception e1) {
            GpsActivator.log(e1.getLocalizedMessage(), e1);
            e1.printStackTrace();
        }
    }

    private void exportToShpLine( List<GpsPoint> gpsPointBetweenTimeStamp, int splitIntervalMinutes, String filePath,
            IProgressMonitor pm ) {
        if (!filePath.endsWith("shp")) {
            filePath = filePath + ".shp";
        }

        SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
        b.setName("gpslines");
        b.setCRS(mapCrs);
        b.add("the_geom", MultiLineString.class);
        b.add("TIMESTAMP", String.class);
        SimpleFeatureType featureType = b.buildFeatureType();

        try {
            MathTransform transform = CRS.findMathTransform(DefaultGeographicCRS.WGS84, mapCrs);
            pm.beginTask("Dump lines...", IProgressMonitor.UNKNOWN);
            FeatureCollection<SimpleFeatureType, SimpleFeature> newCollection = FeatureCollections.newCollection();

            List<Coordinate> coordList = new ArrayList<Coordinate>();
            String firstTs = null;
            String lastTs = null;
            int lineIndex = 0;
            for( int i = 0; i < gpsPointBetweenTimeStamp.size() - 1; i++ ) {
                GpsPoint p1 = gpsPointBetweenTimeStamp.get(i);
                DateTime t1 = p1.utcDateTime;
                double lon1 = p1.longitude;
                double lat1 = p1.latitude;

                coordList.add(new Coordinate(lon1, lat1));
                if (firstTs == null) {
                    firstTs = p1.utcDateTime.toString(BeegisUtilsPlugin.dateTimeFormatterYYYYMMDDHHMMSS);
                }
                lastTs = p1.utcDateTime.toString(BeegisUtilsPlugin.dateTimeFormatterYYYYMMDDHHMMSS);

                GpsPoint p2 = gpsPointBetweenTimeStamp.get(i + 1);
                DateTime t2 = p2.utcDateTime;

                long dt = (t2.getMillis() - t1.getMillis()) / 1000l / 60l;
                if (dt < 0 || dt > splitIntervalMinutes || i == gpsPointBetweenTimeStamp.size() - 2) {
                    // dump line and start new
                    if (coordList.size() == 0) {
                        continue;
                    }
                    if (coordList.size() == 1) {
                        coordList.add((Coordinate) coordList.get(0).clone());
                    }
                    LineString lineString = gF
                            .createLineString((Coordinate[]) coordList.toArray(new Coordinate[coordList.size()]));
                    MultiLineString mlineString = gF.createMultiLineString(new LineString[]{lineString});
                    Geometry reprojectLineString = JTS.transform(mlineString, transform);
                    String t = firstTs + "/" + lastTs;

                    SimpleFeatureBuilder builder = new SimpleFeatureBuilder(featureType);
                    Object[] values = new Object[]{reprojectLineString, t};
                    builder.addAll(values);
                    // build the feature with provided ID
                    SimpleFeature feature = builder.buildFeature(featureType.getTypeName() + "." + lineIndex++);
                    newCollection.add(feature);

                    // reset
                    coordList.clear();
                    firstTs = null;
                }
            }

            ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();
            File f = new File(filePath);
            Map<String, URL> map = Collections.singletonMap("url", f.toURI().toURL());
            ShapefileDataStore dStore = (ShapefileDataStore) factory.createNewDataStore(map);
            dStore.createSchema(featureType);
            IMap activeMap = ApplicationGIS.getActiveMap();
            CoordinateReferenceSystem mapCrs = activeMap.getViewportModel().getCRS();
            dStore.forceSchemaCRS(mapCrs);

            FeatureUtilities.writeToShapefile(dStore, newCollection);
            pm.done();
            FeatureUtilities.addServiceToCatalogAndMap(filePath, true, true, new NullProgressMonitor());

        } catch (Exception e1) {
            GpsActivator.log(e1.getLocalizedMessage(), e1);
            e1.printStackTrace();
        }
    }

    private void exportToGpxPoint( List<GpsPoint> gpsPointBetweenTimeStamp, String filePath, IProgressMonitor pm )
            throws Exception {

        ObjectFactory factory = new ObjectFactory();

        TrksegType trksegType = factory.createTrksegType();
        List<WptType> trkptList = trksegType.getTrkpt();

        double minLat = Double.POSITIVE_INFINITY;
        double minLon = Double.POSITIVE_INFINITY;
        double maxLat = Double.NEGATIVE_INFINITY;
        double maxLon = Double.NEGATIVE_INFINITY;
        DateTime first = null;
        DateTime last = null;
        for( GpsPoint gpsPoint : gpsPointBetweenTimeStamp ) {
            double altitude = gpsPoint.altitude;
            double lat = gpsPoint.latitude;
            double lon = gpsPoint.longitude;
            double sat = gpsPoint.sat;
            double hdop = gpsPoint.hdop;

            if (lat < minLat)
                minLat = lat;
            if (lat > maxLat)
                maxLat = lat;
            if (lon < minLon)
                minLon = lon;
            if (lon > maxLon)
                maxLon = lon;

            DateTime utc = gpsPoint.utcDateTime;
            if (first == null || utc.isBefore(first)) {
                first = utc;
            }
            if (last == null || utc.isAfter(last)) {
                last = utc;
            }

            GregorianCalendar gCalendar = (GregorianCalendar) GregorianCalendar.getInstance();
            gCalendar.setTimeInMillis(utc.getMillis());
            XMLGregorianCalendar xmlCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gCalendar);

            WptType wptType = factory.createWptType();
            wptType.setEle(BigDecimal.valueOf(altitude));
            wptType.setLat(BigDecimal.valueOf(lat));
            wptType.setLon(BigDecimal.valueOf(lon));
            wptType.setHdop(BigDecimal.valueOf(hdop));
            wptType.setSat(BigInteger.valueOf((long) sat));
            wptType.setTime(xmlCalendar);
            trkptList.add(wptType);
        }

        GpxType gpxType = factory.createGpxType();
        gpxType.setCreator("BeeGIS");
        gpxType.setVersion("1.1");

        MetadataType metaType = factory.createMetadataType();
        BoundsType boundsType = factory.createBoundsType();
        boundsType.setMaxlat(new BigDecimal(maxLat));
        boundsType.setMinlat(new BigDecimal(minLat));
        boundsType.setMaxlon(new BigDecimal(maxLon));
        boundsType.setMinlon(new BigDecimal(minLon));
        metaType.setBounds(boundsType);

        GregorianCalendar gCalendar = (GregorianCalendar) GregorianCalendar.getInstance();
        XMLGregorianCalendar xmlCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gCalendar);
        metaType.setTime(xmlCalendar);

        gpxType.setMetadata(metaType);

        List<WptType> wptList = gpxType.getWpt();
        wptList.addAll(trkptList);

        JAXBElement<GpxType> jaxbElement = factory.createGpx(gpxType);
        JAXBContext jaxbContext = JAXBContext.newInstance("schema.gpx_1_1");
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, new Boolean(true));
        marshaller.marshal(jaxbElement, new FileOutputStream(filePath));

    }

    private void exportToGpxLine( List<GpsPoint> gpsPointBetweenTimeStamp, int splitIntervalMinutes, String filePath,
            IProgressMonitor pm ) throws Exception {

        ObjectFactory factory = new ObjectFactory();

        List<TrksegType> segments = new ArrayList<TrksegType>();

        double minLat = Double.POSITIVE_INFINITY;
        double minLon = Double.POSITIVE_INFINITY;
        double maxLat = Double.NEGATIVE_INFINITY;
        double maxLon = Double.NEGATIVE_INFINITY;
        DateTime first = null;
        DateTime last = null;
        List<GpsPoint> pointsList = new ArrayList<GpsPoint>();
        for( int i = 0; i < gpsPointBetweenTimeStamp.size() - 1; i++ ) {
            GpsPoint p1 = gpsPointBetweenTimeStamp.get(i);
            DateTime t1 = p1.utcDateTime;

            pointsList.add(p1);

            GpsPoint p2 = gpsPointBetweenTimeStamp.get(i + 1);
            DateTime t2 = p2.utcDateTime;

            long dt = (t2.getMillis() - t1.getMillis()) / 1000l / 60l;
            if (dt < 0 || dt > splitIntervalMinutes || i == gpsPointBetweenTimeStamp.size() - 2) {
                // dump line and start new
                if (pointsList.size() < 2) {
                    continue;
                }

                // create gpx track
                TrksegType trksegType = factory.createTrksegType();
                List<WptType> trkptList = trksegType.getTrkpt();

                for( GpsPoint gpsPoint : pointsList ) {
                    double altitude = gpsPoint.altitude;
                    double lat = gpsPoint.latitude;
                    double lon = gpsPoint.longitude;
                    double sat = gpsPoint.sat;
                    double hdop = gpsPoint.hdop;

                    if (lat < minLat)
                        minLat = lat;
                    if (lat > maxLat)
                        maxLat = lat;
                    if (lon < minLon)
                        minLon = lon;
                    if (lon > maxLon)
                        maxLon = lon;

                    DateTime utc = gpsPoint.utcDateTime;
                    if (first == null || utc.isBefore(first)) {
                        first = utc;
                    }
                    if (last == null || utc.isAfter(last)) {
                        last = utc;
                    }

                    GregorianCalendar gCalendar = (GregorianCalendar) GregorianCalendar.getInstance();
                    gCalendar.setTimeInMillis(utc.getMillis());
                    XMLGregorianCalendar xmlCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gCalendar);

                    WptType wptType = factory.createWptType();
                    wptType.setEle(BigDecimal.valueOf(altitude));
                    wptType.setLat(BigDecimal.valueOf(lat));
                    wptType.setLon(BigDecimal.valueOf(lon));
                    wptType.setHdop(BigDecimal.valueOf(hdop));
                    wptType.setSat(BigInteger.valueOf((long) sat));
                    wptType.setTime(xmlCalendar);
                    trkptList.add(wptType);
                }

                segments.add(trksegType);

                // reset
                pointsList.clear();
            }
        }
        GpxType gpxType = factory.createGpxType();
        gpxType.setCreator("BeeGIS");
        gpxType.setVersion("1.1");

        MetadataType metaType = factory.createMetadataType();
        BoundsType boundsType = factory.createBoundsType();
        boundsType.setMaxlat(new BigDecimal(maxLat));
        boundsType.setMinlat(new BigDecimal(minLat));
        boundsType.setMaxlon(new BigDecimal(maxLon));
        boundsType.setMinlon(new BigDecimal(minLon));
        metaType.setBounds(boundsType);

        GregorianCalendar gCalendar = (GregorianCalendar) GregorianCalendar.getInstance();
        XMLGregorianCalendar xmlCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gCalendar);
        metaType.setTime(xmlCalendar);

        gpxType.setMetadata(metaType);

        TrkType trkType = factory.createTrkType();
        trkType.setName(first.toString(BeegisUtilsPlugin.dateTimeFormatterYYYYMMDDHHMM) + " - "
                + last.toString(BeegisUtilsPlugin.dateTimeFormatterYYYYMMDDHHMM));
        trkType.setType("Gps tracklog");

        List<TrkType> trkList = gpxType.getTrk();
        trkList.add(trkType);

        List<TrksegType> trksegList = trkType.getTrkseg();
        trksegList.addAll(segments);

        JAXBElement<GpxType> jaxbElement = factory.createGpx(gpxType);
        JAXBContext jaxbContext = JAXBContext.newInstance("schema.gpx_1_1");
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, new Boolean(true));
        marshaller.marshal(jaxbElement, new FileOutputStream(filePath));
    }
}
