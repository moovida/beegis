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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.refractions.udig.project.ui.ApplicationGIS;
import net.refractions.udig.ui.ExceptionDetailsDialog;
import net.refractions.udig.ui.PlatformGIS;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
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
import com.vividsolutions.jts.geom.Point;

import eu.hydrologis.jgrass.beegisutils.database.annotatedclasses.GeonotesTextareaTable;
import eu.hydrologis.jgrass.beegisutils.jgrassported.FeatureUtilities;
import eu.hydrologis.jgrass.geonotes.GeonotesHandler;
import eu.hydrologis.jgrass.geonotes.GeonotesPlugin;
import eu.hydrologis.jgrass.geonotes.GeonotesUI;
import eu.hydrologis.jgrass.geonotes.fieldbook.GeonotesListViewer;

/**
 * Action to export {@link GeonotesUI} to shapefile.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class ExportToShapefileAction extends Action {

    private static final String EXPORT_TO_SHAPEFILE = "Export to shapefile";
    private final GeonotesListViewer geonotesViewer;
    private String path;

    public ExportToShapefileAction( GeonotesListViewer geonotesViewer ) {
        super(EXPORT_TO_SHAPEFILE);
        this.geonotesViewer = geonotesViewer;
    }

    public void run() {
        final List<GeonotesHandler> currentGeonotesSelection = geonotesViewer
                .getCurrentGeonotesSelection();

        FileDialog fileDialog = new FileDialog(geonotesViewer.getTable().getShell(), SWT.SAVE);
        path = fileDialog.open();

        if (path != null && path.length() > 0) {
            if (!path.endsWith(".shp") || !path.endsWith(".SHP")) {
                path = path + ".shp";
            }
            IRunnableWithProgress operation = new IRunnableWithProgress(){
                public void run( IProgressMonitor pm ) throws InvocationTargetException,
                        InterruptedException {
                    notesToShapefile(currentGeonotesSelection, new File(path), pm);
                }
            };
            PlatformGIS.runInProgressDialog(EXPORT_TO_SHAPEFILE, true, operation, true);

        }

    }

    private void notesToShapefile( List<GeonotesHandler> geonotesList, File outputShapeFile,
            IProgressMonitor pm ) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
        GeometryFactory gF = new GeometryFactory();
        CoordinateReferenceSystem mapCrs = ApplicationGIS.getActiveMap().getViewportModel()
                .getCRS();

        SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
        b.setName("geonotes"); //$NON-NLS-1$
        b.setCRS(mapCrs);
        b.add("the_geom", Point.class); //$NON-NLS-1$

        b.add("title", String.class);
        b.add("text", String.class);
        b.add("timestamp", String.class);

        SimpleFeatureType featureType = b.buildFeatureType();

        try {
            pm.beginTask("Export geonotes...", geonotesList.size());
            FeatureCollection<SimpleFeatureType, SimpleFeature> newCollection = FeatureCollections
                    .newCollection();
            for( int i = 0; i < geonotesList.size(); i++ ) {

                GeonotesHandler geonotesHandler = geonotesList.get(i);
                Coordinate position = geonotesHandler.getPosition();
                String crsWkt = geonotesHandler.getCrsWkt();
                DateTime creationDate = geonotesHandler.getCreationDate();
                GeonotesTextareaTable geonotesTextareaTable = geonotesHandler
                        .getGeonotesTextareaTable();
                String text = "";
                if (geonotesTextareaTable != null) {
                    text = geonotesTextareaTable.getText();
                }
                String title = geonotesHandler.getTitle();

                CoordinateReferenceSystem noteCrs = CRS.parseWKT(crsWkt);
                MathTransform transform = CRS.findMathTransform(noteCrs, mapCrs);

                Point point = gF.createPoint(position);
                Geometry reprojectPoint = JTS.transform(point, transform);

                SimpleFeatureBuilder builder = new SimpleFeatureBuilder(featureType);
                Object[] values = new Object[]{reprojectPoint, title, text,
                        creationDate.toString(dateTimeFormatter)};
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
                    true, pm);

        } catch (Exception e1) {
            String message = "An error occurred while exporting the selected geonotes to shapefile.";
            ExceptionDetailsDialog.openError(null, message, IStatus.ERROR,
                    GeonotesPlugin.PLUGIN_ID, e1);
            e1.printStackTrace();
        }

    }
}
