package eu.hydrologis.jgrass.featureeditor.utils;

import java.io.IOException;

import net.refractions.udig.catalog.ID;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.interceptor.FeatureInterceptor;
import net.refractions.udig.project.ui.ApplicationGIS;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.geotools.data.FeatureSource;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.feature.Feature;

public class OnFeatureCreateOpenFormViewInterceptor implements FeatureInterceptor {

    public OnFeatureCreateOpenFormViewInterceptor() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void run( Feature feature ) {
        // try {
        // IMap activeMap = ApplicationGIS.getActiveMap();
        // if (activeMap != null) {
        // ILayer selectedLayer = activeMap.getEditManager().getSelectedLayer();
        // if (selectedLayer != null) {
        // IGeoResource geoResource = selectedLayer.getGeoResource();
        // ID id = geoResource.getID();
        //
        // SimpleFeatureSource featureSource = (SimpleFeatureSource)
        // selectedLayer.getResource(FeatureSource.class,
        // new NullProgressMonitor());
        // if (featureSource == null) {
        // return;
        // }
        // SimpleFeatureCollection featureCollection =
        // featureSource.getFeatures(selectedLayer.getQuery(true));
        //
        //
        // }
        // }
        // } catch (IOException e) {
        // e.printStackTrace();
        // }

    }

}
