package eu.hydrologis.jgrass.gpsnmea.export;

import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
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

import eu.hydrologis.jgrass.beegisutils.jgrassported.GeometryUtilities;
import eu.hydrologis.jgrass.beegisutils.jgrassported.GeometryUtilities.GEOMETRYTYPE;

public class ExportGpxWizard extends Wizard implements IExportWizard {

    private static final DefaultGeographicCRS GPXCRS = DefaultGeographicCRS.WGS84;
    private ExportGpxWizardPage mainPage;
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
                                    exportLineOrPolygonToGpx(featureCollection, filePath, pm);
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

    private void exportLineOrPolygonToGpx( SimpleFeatureCollection featureCollection, String filePath, IProgressMonitor pm )
            throws Exception {
        pm.beginTask("Exporting to gpx...", IProgressMonitor.UNKNOWN);
        try {
            ReferencedEnvelope bounds = featureCollection.getBounds();
            bounds = bounds.transform(GPXCRS, true);
            MathTransform transform = CRS.findMathTransform(mapCrs, GPXCRS);

            ObjectFactory factory = new ObjectFactory();
            GpxType gpxType = factory.createGpxType();
            gpxType.setCreator("BeeGIS");
            gpxType.setVersion("1.1");
            List<TrkType> trkList = gpxType.getTrk();

            SimpleFeatureIterator featureIterator = featureCollection.features();
            while( featureIterator.hasNext() ) {
                SimpleFeature feature = featureIterator.next();

                TrkType trkType = factory.createTrkType();
                trkType.setName(feature.getID());
                trkType.setType("Gps tracklog");
                trkList.add(trkType);
                List<TrksegType> trksegList = trkType.getTrkseg();

                Geometry geom = (Geometry) feature.getDefaultGeometry();
                int numGeometries = geom.getNumGeometries();
                for( int i = 0; i < numGeometries; i++ ) {
                    Geometry geometryN = geom.getGeometryN(i);
                    geometryN = JTS.transform(geometryN, transform);

                    Coordinate[] coordinates = geometryN.getCoordinates();

                    // create gpx track
                    TrksegType trksegType = factory.createTrksegType();
                    List<WptType> trkptList = trksegType.getTrkpt();
                    for( Coordinate coordinate : coordinates ) {
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
                    trksegList.add(trksegType);

                }
            }
            featureIterator.close();

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

            JAXBElement<GpxType> jaxbElement = factory.createGpx(gpxType);
            JAXBContext jaxbContext = JAXBContext.newInstance("schema.gpx_1_1");
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, new Boolean(true));
            marshaller.marshal(jaxbElement, new FileOutputStream(filePath));

        } finally {
            pm.done();
        }
    }

}
