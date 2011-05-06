package eu.hydrologis.jgrass.annotationlayer.export;

import java.awt.Color;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

import eu.hydrologis.jgrass.annotationlayer.AnnotationPlugin;
import eu.hydrologis.jgrass.beegisutils.jgrassported.DressedWorldStroke;
import eu.hydrologis.jgrass.beegisutils.jgrassported.FeatureUtilities;

public class ExportAnnotationsWizard extends Wizard implements IExportWizard {

    private ExportAnnotationsWizardPage mainPage;
    private static GeometryFactory gF = new GeometryFactory();
    private CoordinateReferenceSystem mapCrs;

    public ExportAnnotationsWizard() {
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

                                mapCrs = ApplicationGIS.getActiveMap().getViewportModel().getCRS();

                                AnnotationPlugin.getDefault().resetStrokes();
                                List<DressedWorldStroke> strokes = AnnotationPlugin.getDefault().getStrokes();

                                exportToShpLine(strokes, filePath, pm);
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
        mainPage = new ExportAnnotationsWizardPage("Export Annotations to Shapefile", selection); // NON-NLS-1
    }

    public void addPages() {
        super.addPages();
        addPage(mainPage);
    }

    private void exportToShpLine( List<DressedWorldStroke> strokes, String filePath, IProgressMonitor pm ) {
        if (!filePath.endsWith("shp")) {
            filePath = filePath + ".shp";
        }

        SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
        b.setName("annotations");
        b.setCRS(mapCrs);
        b.add("the_geom", LineString.class);
        b.add("width", Double.class);
        b.add("alpha", Double.class);
        b.add("color", String.class);
        SimpleFeatureType featureType = b.buildFeatureType();

        try {
            pm.beginTask("Dump lines...", IProgressMonitor.UNKNOWN);
            FeatureCollection<SimpleFeatureType, SimpleFeature> newCollection = FeatureCollections.newCollection();

            for( int i = 0; i < strokes.size(); i++ ) {
                DressedWorldStroke dressedWorldStroke = strokes.get(i);
                Double[] nodes = dressedWorldStroke.nodes;
                double width = dressedWorldStroke.strokeWidth[0];
                int[] rgb = dressedWorldStroke.rgb;
                double alpha = rgb[3] / 255d;
                Color c = new Color(rgb[0], rgb[1], rgb[2]);
                String hexString = "#" + Integer.toHexString(c.getRGB() & 0x00ffffff);

                Coordinate[] coords = new Coordinate[nodes.length / 2];
                int index = 0;
                for( int j = 0; j < nodes.length; j = j + 2 ) {
                    Double first = nodes[j];
                    Double sec = nodes[j + 1];
                    coords[index] = new Coordinate(first, sec);
                    index++;
                }
                if (coords.length < 2) {
                    continue;
                }
                LineString lineString = gF.createLineString(coords);
                CoordinateReferenceSystem lineCrs = CRS.parseWKT(dressedWorldStroke.crsWKT);
                MathTransform transform = CRS.findMathTransform(lineCrs, mapCrs, true);
                lineString = (LineString) JTS.transform(lineString, transform);

                SimpleFeatureBuilder builder = new SimpleFeatureBuilder(featureType);
                Object[] values = new Object[]{lineString, width, alpha, hexString};
                builder.addAll(values);
                SimpleFeature feature = builder.buildFeature(featureType.getTypeName() + "." + i);
                newCollection.add(feature);
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
            e1.printStackTrace();
        }
    }

}
