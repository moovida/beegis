package eu.hydrologis.jgrass.gpsnmea.export;

import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.ui.ApplicationGIS;
import net.refractions.udig.ui.PlatformGIS;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
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

import eu.hydrologis.jgrass.beegisutils.BeegisUtilsPlugin;
import eu.hydrologis.jgrass.beegisutils.jgrassported.GeometryUtilities;
import eu.hydrologis.jgrass.beegisutils.jgrassported.GeometryUtilities.GEOMETRYTYPE;
import eu.hydrologis.jgrass.gpsnmea.gps.GpsPoint;

public class ExportGpxWizard extends Wizard implements IExportWizard {

    private static final DefaultGeographicCRS GPXCRS = DefaultGeographicCRS.WGS84;
    private ExportGpxWizardPage mainPage;
    private static GeometryFactory gF = new GeometryFactory();
    private CoordinateReferenceSystem mapCrs;

    public ExportGpxWizard() {
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

                            try {
                                String filePath = mainPage.getFilePath();

                                IMap activeMap = ApplicationGIS.getActiveMap();
                                mapCrs = activeMap.getViewportModel().getCRS();

                                ILayer selectedLayer = activeMap.getEditManager().getSelectedLayer();
                                if (selectedLayer == null
                                        || !selectedLayer.getGeoResource().canResolve(SimpleFeatureSource.class)) {
                                    Display.getDefault().syncExec(new Runnable(){
                                        public void run() {
                                            MessageDialog.openWarning(getShell(), "WARNING",
                                                    "Select a feature layer to export to GPX before running the wizard.");
                                        }
                                    });
                                    return;
                                }

                                SimpleFeatureStore simpleFeatureStore = selectedLayer.getGeoResource().resolve(
                                        SimpleFeatureStore.class, pm);
                                SimpleFeatureCollection featureCollection = simpleFeatureStore.getFeatures();
                                SimpleFeatureType schema = featureCollection.getSchema();

                                GEOMETRYTYPE geometryType = GeometryUtilities.getGeometryType(schema.getGeometryDescriptor()
                                        .getType());
                                switch( geometryType ) {
                                case POINT:
                                case MULTIPOINT:
                                    exportToGpxPoint(featureCollection, filePath, pm);
                                    break;

                                default:
                                    break;
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
        mainPage = new ExportGpxWizardPage("Export selected feature layer to GPX", selection); // NON-NLS-1
    }

    public void addPages() {
        super.addPages();
        addPage(mainPage);
    }

    private void exportToGpxPoint( SimpleFeatureCollection featureCollection, String filePath, IProgressMonitor pm )
            throws Exception {
        pm.beginTask("Exporting to gpx...", IProgressMonitor.UNKNOWN);
        try {
            ReferencedEnvelope bounds = featureCollection.getBounds();
            bounds = bounds.transform(GPXCRS, true);

            MathTransform transform = CRS.findMathTransform(mapCrs, GPXCRS);

            ObjectFactory factory = new ObjectFactory();
            TrksegType trksegType = factory.createTrksegType();
            List<WptType> trkptList = trksegType.getTrkpt();

            SimpleFeatureIterator featureIterator = featureCollection.features();
            while( featureIterator.hasNext() ) {
                SimpleFeature feature = featureIterator.next();
                Geometry geom = (Geometry) feature.getDefaultGeometry();
                int numGeometries = geom.getNumGeometries();
                for( int i = 0; i < numGeometries; i++ ) {
                    Geometry geometryN = geom.getGeometryN(i);
                    geometryN = JTS.transform(geometryN, transform);

                    Coordinate coordinate = geometryN.getCoordinate();

                    GregorianCalendar gCalendar = (GregorianCalendar) GregorianCalendar.getInstance();
                    XMLGregorianCalendar xmlCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gCalendar);
                    WptType wptType = factory.createWptType();
                    if (!Double.isNaN(coordinate.z)) {
                        wptType.setEle(BigDecimal.valueOf(coordinate.z));
                    }
                    wptType.setLat(BigDecimal.valueOf(coordinate.y));
                    wptType.setLon(BigDecimal.valueOf(coordinate.x));
                    // wptType.setHdop(BigDecimal.valueOf(hdop));
                    // wptType.setSat(BigInteger.valueOf((long) sat));
                    wptType.setTime(xmlCalendar);
                    trkptList.add(wptType);
                }
            }
            featureIterator.close();

            GpxType gpxType = factory.createGpxType();
            gpxType.setCreator("BeeGIS");
            gpxType.setVersion("1.1");

            MetadataType metaType = factory.createMetadataType();
            BoundsType boundsType = factory.createBoundsType();
            boundsType.setMaxlat(new BigDecimal(bounds.getMaxY()));
            boundsType.setMinlat(new BigDecimal(bounds.getMinY()));
            boundsType.setMaxlon(new BigDecimal(bounds.getMaxX()));
            boundsType.setMinlon(new BigDecimal(bounds.getMinX()));
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
        } finally {
            pm.done();
        }
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
