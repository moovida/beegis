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

import i18n.geonotes.Messages;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import net.refractions.udig.project.ui.ApplicationGIS;
import net.refractions.udig.ui.ExceptionDetailsDialog;
import net.refractions.udig.ui.PlatformGIS;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.geotools.data.simple.SimpleFeatureCollection;
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
 * Action to export {@link GeonotesUI} to feature layer.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class ExportToFeatureLayerAction extends Action {

    private static final String EXPORT_TO_SHAPEFILE = Messages.getString("ExportToFeatureLayerAction__export_to_featurelayer"); //$NON-NLS-1$
    private final GeonotesListViewer geonotesViewer;
    private SimpleFeatureCollection newCollection;

    public ExportToFeatureLayerAction( GeonotesListViewer geonotesViewer ) {
        super(EXPORT_TO_SHAPEFILE);
        this.geonotesViewer = geonotesViewer;
    }

    public void run() {

        IRunnableWithProgress operation = new IRunnableWithProgress(){

            public void run( IProgressMonitor pm ) throws InvocationTargetException, InterruptedException {
                List<GeonotesHandler> currentGeonotesSelection = geonotesViewer.getCurrentGeonotesSelection();
                if (currentGeonotesSelection == null || currentGeonotesSelection.size() == 0) {
                    currentGeonotesSelection = (List<GeonotesHandler>) geonotesViewer.getInput();
                }

                DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm"); //$NON-NLS-1$
                GeometryFactory gF = new GeometryFactory();
                CoordinateReferenceSystem mapCrs = ApplicationGIS.getActiveMap().getViewportModel().getCRS();

                SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
                b.setName("geonotes"); //$NON-NLS-1$
                b.setCRS(mapCrs);
                b.add("the_geom", Point.class); //$NON-NLS-1$

                b.add("title", String.class); //$NON-NLS-1$
                b.add("text", String.class); //$NON-NLS-1$
                b.add("timestamp", String.class); //$NON-NLS-1$

                SimpleFeatureType featureType = b.buildFeatureType();

                try {
                    pm
                            .beginTask(
                                    Messages.getString("ExportToFeatureLayerAction__exporting_geonotes"), currentGeonotesSelection.size()); //$NON-NLS-1$
                    newCollection = FeatureCollections.newCollection();
                    for( int i = 0; i < currentGeonotesSelection.size(); i++ ) {

                        GeonotesHandler geonotesHandler = currentGeonotesSelection.get(i);
                        Coordinate position = geonotesHandler.getPosition();
                        DateTime creationDate = geonotesHandler.getCreationDate();
                        GeonotesTextareaTable geonotesTextareaTable = geonotesHandler.getGeonotesTextareaTable();
                        String text = ""; //$NON-NLS-1$
                        if (geonotesTextareaTable != null) {
                            text = geonotesTextareaTable.getText();
                        }
                        String title = geonotesHandler.getTitle();

                        CoordinateReferenceSystem noteCrs = geonotesHandler.getCrs();
                        MathTransform transform = CRS.findMathTransform(noteCrs, mapCrs);

                        Point point = gF.createPoint(position);
                        Geometry reprojectPoint = JTS.transform(point, transform);

                        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(featureType);
                        Object[] values = new Object[]{reprojectPoint, title, text, creationDate.toString(dateTimeFormatter)};
                        builder.addAll(values);
                        SimpleFeature feature = builder.buildFeature(featureType.getTypeName() + "." + i); //$NON-NLS-1$
                        newCollection.add(feature);
                        pm.worked(1);
                    }
                    pm.done();

                } catch (Exception e1) {
                    String message = Messages.getString("ExportToFeatureLayerAction__error_while_exporting"); //$NON-NLS-1$
                    ExceptionDetailsDialog.openError(null, message, IStatus.ERROR, GeonotesPlugin.PLUGIN_ID, e1);
                    e1.printStackTrace();
                }

            }
        };
        PlatformGIS.runInProgressDialog(EXPORT_TO_SHAPEFILE, true, operation, false);
        try {
            FeatureUtilities.featureCollectionToTempLayer(newCollection);
        } catch (Exception e) {
            MessageDialog.openError(geonotesViewer.getTable().getShell(),
                    Messages.getString("ExportToFeatureLayerAction__error"), //$NON-NLS-1$
                    Messages.getString("ExportToFeatureLayerAction__error_while_creating_layer")); //$NON-NLS-1$
        }
    }

}
