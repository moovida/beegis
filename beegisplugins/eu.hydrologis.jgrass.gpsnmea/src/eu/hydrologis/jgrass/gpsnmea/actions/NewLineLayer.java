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
package eu.hydrologis.jgrass.gpsnmea.actions;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import net.refractions.udig.project.IMap;
import net.refractions.udig.project.ui.ApplicationGIS;
import net.refractions.udig.ui.ExceptionDetailsDialog;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.LineString;

import eu.hydrologis.jgrass.beegisutils.jgrassported.FeatureUtilities;
import eu.hydrologis.jgrass.gpsnmea.GpsActivator;

/**
 * Action to create a new default type line layer.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public class NewLineLayer extends Action implements IWorkbenchWindowActionDelegate {
    public static final String ID = "new.layer.lines"; //$NON-NLS-1$

    private IWorkbenchWindow window;

    public NewLineLayer() {
        setId(ID);
    }

    public void dispose() {
    }

    public void init( IWorkbenchWindow window ) {
        this.window = window;
        // add action to the registry
        GpsActivator.getDefault().registerAction(this);
    }

    public void run( IAction action ) {
        FileDialog fileDialog = new FileDialog(window.getShell(), SWT.SAVE);
        fileDialog.setText("Enter a name for the new lines layer to be created.");
        fileDialog.setFileName("new_line_layer.shp");
        String selpath = fileDialog.open();
        if (selpath == null || selpath.length() < 1) {
            return;
        }
        if (!selpath.endsWith("shp")) {
            selpath = selpath + ".shp";
        }

        IMap activeMap = ApplicationGIS.getActiveMap();
        CoordinateReferenceSystem mapCrs = activeMap.getViewportModel().getCRS();

        // create the feature type
        SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
        b.setName("typename");
        b.setCRS(mapCrs);
        b.add("the_geom", LineString.class);
        b.add("name", String.class);
        b.add("description", String.class);
        SimpleFeatureType type = b.buildFeatureType();

        try {

            ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();

            File file = new File(selpath);
            Map<String, Serializable> create = new HashMap<String, Serializable>();
            create.put("url", file.toURI().toURL());
            ShapefileDataStore newDataStore = (ShapefileDataStore) factory
                    .createNewDataStore(create);

            newDataStore.createSchema(type);
            // if (crs != null)
            // newDataStore.forceSchemaCRS(crs);
            Transaction transaction = new DefaultTransaction();
            FeatureStore<SimpleFeatureType, SimpleFeature> featureStore = (FeatureStore<SimpleFeatureType, SimpleFeature>) newDataStore
                    .getFeatureSource();
            featureStore.setTransaction(transaction);
            try {
                featureStore.addFeatures(FeatureCollections.newCollection());
                transaction.commit();
            } catch (Exception problem) {
                problem.printStackTrace();
                transaction.rollback();
            } finally {
                transaction.close();
            }

            FeatureUtilities.addServiceToCatalogAndMap(selpath, true, true,
                    new NullProgressMonitor());

        } catch (Exception e1) {
            String message = "An error occurred while creating the default line layer.";
            ExceptionDetailsDialog.openError(null, message, IStatus.ERROR, GpsActivator.PLUGIN_ID,
                    e1);
            e1.printStackTrace();
        }
    }

    public void selectionChanged( IAction action, ISelection selection ) {
    }

}