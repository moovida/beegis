package eu.hydrologis.jgrass.featureeditor.utils;

import org.opengis.feature.simple.SimpleFeature;

import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.internal.Map;

public interface ISelectionObserver {
    
    public void selectionChanged(Map selectedMap, ILayer selectedLayer, SimpleFeature selectedFeature);
}
