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
package eu.hydrologis.jgrass.gpsnmea.imports;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;

import net.refractions.udig.ui.ExceptionDetailsDialog;
import net.refractions.udig.ui.PlatformGIS;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;

import schema.gpx_1_1.GpxType;
import schema.gpx_1_1.TrkType;
import schema.gpx_1_1.TrksegType;
import schema.gpx_1_1.WptType;
import eu.hydrologis.jgrass.beegisutils.jgrassported.FeatureUtilities;
import eu.hydrologis.jgrass.gpsnmea.GpsActivator;

/**
 * @author Andrea Antonello - www.hydrologis.com
 */
public class GpxImportWizard extends Wizard implements INewWizard {

    private GpxImportWizardPage mainPage;

    public static boolean canFinish = false;

    private final Map<String, String> params = new HashMap<String, String>();

    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    private DateTimeFormatter dateTimeFormatterYYYYMMDDHHMMSS = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    public GpxImportWizard() {
        super();
    }

    public void init( IWorkbench workbench, IStructuredSelection selection ) {
        setWindowTitle("GPX file import");
        // setDefaultPageImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
        //                GpsActivator.PLUGIN_ID, "icons/arcpad128.gif")); //$NON-NLS-1$
        setNeedsProgressMonitor(true);
        mainPage = new GpxImportWizardPage("Arcpad file import", params); //$NON-NLS-1$
    }

    public void addPages() {
        super.addPages();
        addPage(mainPage);
    }

    public boolean canFinish() {
        return super.canFinish() && canFinish;
    }

    public boolean performFinish() {

        final File gpxFile = mainPage.getGpxFile();

        /*
         * run with backgroundable progress monitoring
         */
        IRunnableWithProgress operation = new IRunnableWithProgress(){

            @SuppressWarnings("unchecked")
            public void run( IProgressMonitor pm ) throws InvocationTargetException, InterruptedException {
                try {
                    JAXBContext jaxbContext = JAXBContext.newInstance("schema.gpx_1_1");
                    Unmarshaller unMarshaller = jaxbContext.createUnmarshaller();
                    JAXBElement<GpxType> jaxbElement = (JAXBElement<GpxType>) unMarshaller.unmarshal(gpxFile);
                    GpxType gpxType = jaxbElement.getValue();

                    SimpleFeatureCollection linesCollection = FeatureCollections.newCollection();
                    GeometryFactory gf = new GeometryFactory();
                    SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
                    b.setName("gpx_import_lines");
                    b.setCRS(DefaultGeographicCRS.WGS84);
                    b.add("the_geom", LineString.class);
                    b.add("starttime", String.class);
                    b.add("endtime", String.class);
                    b.add("startelev", Double.class);
                    b.add("endelev", Double.class);
                    SimpleFeatureType type = b.buildFeatureType();

                    // get tracks
                    int fid = 0;
                    List<TrkType> trkList = gpxType.getTrk();
                    for( TrkType trkType : trkList ) {
                        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(type);
                        Object[] values = new Object[]{null, null, null, null, null};
                        List<LineString> lineStrings = new ArrayList<LineString>();
                        // get each track segment
                        List<TrksegType> trksegList = trkType.getTrkseg();
                        for( TrksegType trksegType : trksegList ) {
                            List<Coordinate> coords = new ArrayList<Coordinate>();
                            String startDate = null;
                            String endDate = null;
                            Double startElev = null;
                            Double endElev = null;

                            // get the segment's trackpoint
                            List<WptType> trkptList = trksegType.getTrkpt();
                            for( int i = 0; i < trkptList.size(); i++ ) {
                                WptType wptType = trkptList.get(i);
                                BigDecimal lat = wptType.getLat();
                                BigDecimal lon = wptType.getLon();
                                BigDecimal ele = wptType.getEle();
                                XMLGregorianCalendar time = wptType.getTime();
                                String timeStr = "";
                                if (time != null) {
                                    DateTime dt = new DateTime(time.toGregorianCalendar().getTime());
                                    timeStr = dt.toString(dateTimeFormatterYYYYMMDDHHMMSS);
                                }

                                if (i == 0) {
                                    startDate = timeStr;
                                    startElev = ele.doubleValue();
                                }
                                if (i == trkptList.size() - 1) {
                                    endDate = timeStr;
                                    endElev = ele.doubleValue();
                                }
                                coords.add(new Coordinate(lon.doubleValue(), lat.doubleValue()));
                            }

                            LineString lineString = gf.createLineString(coords.toArray(new Coordinate[0]));
                            lineStrings.add(lineString);
                            values[0] = lineString;
                            values[1] = startDate;
                            values[2] = endDate;
                            values[3] = startElev;
                            values[4] = endElev;

                            builder.addAll(values);
                            SimpleFeature feature = builder.buildFeature(type.getTypeName() + "." + fid);
                            fid++;
                            linesCollection.add(feature);
                        }
                    }

                    if (linesCollection.size() > 0) {
                        FeatureUtilities.featureCollectionToTempLayer(linesCollection);
                    }

                    fid = 0;
                    SimpleFeatureCollection pointsCollection = FeatureCollections.newCollection();
                    b = new SimpleFeatureTypeBuilder();
                    b.setName("gpx_import_poits");
                    b.setCRS(DefaultGeographicCRS.WGS84);
                    b.add("the_geom", Point.class);
                    b.add("elev", Double.class);
                    b.add("time", String.class);
                    type = b.buildFeatureType();
                    // get waypoints
                    List<WptType> wpt = gpxType.getWpt();
                    for( int i = 0; i < wpt.size(); i++ ) {
                        WptType wptType = wpt.get(i);
                        BigDecimal lat = wptType.getLat();
                        BigDecimal lon = wptType.getLon();
                        BigDecimal ele = wptType.getEle();
                        XMLGregorianCalendar time = wptType.getTime();
                        String timeStr = "";
                        if (time != null) {
                            DateTime dt = new DateTime(time.toGregorianCalendar().getTime());
                            timeStr = dt.toString(dateTimeFormatterYYYYMMDDHHMMSS);
                        }
                        Point point = gf.createPoint(new Coordinate(lon.doubleValue(), lat.doubleValue()));
                        Object[] values = new Object[]{point, ele.doubleValue(), timeStr};
                        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(type);
                        builder.addAll(values);
                        SimpleFeature feature = builder.buildFeature(type.getTypeName() + "." + fid);
                        fid++;
                        pointsCollection.add(feature);
                    }
                    if (pointsCollection.size() > 0) {
                        FeatureUtilities.featureCollectionToTempLayer(pointsCollection);
                    }

                    System.out.println();
                } catch (Exception e) {
                    e.printStackTrace();
                    String message = "An error occurred during the gpx file import.";
                    ExceptionDetailsDialog.openError(null, message, IStatus.ERROR, GpsActivator.PLUGIN_ID, e);
                }
            }
        };

        PlatformGIS.runInProgressDialog("Importing GPX data", true, operation, true);

        return true;
    }
}
