package eu.hydrologis.jgrass.featureeditor.utils;

import net.refractions.udig.project.EditManagerEvent;
import net.refractions.udig.project.IEditManagerListener;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.ui.ApplicationGIS;

public class MapLayerHandler {

    private static MapLayerHandler handler;

    private IMap selectedMap;
    private ILayer selectedLayer;

    private MapLayerHandler() {
        IMap activeMap = ApplicationGIS.getActiveMap();
        activeMap.getEditManager().addListener(editManagerListener);
    }

    public static MapLayerHandler getInstance() {
        if (handler == null) {
            handler = new MapLayerHandler();
        }
        return handler;
    }

    private IEditManagerListener editManagerListener = new IEditManagerListener(){

        public void changed( EditManagerEvent event ) {
            IMap newMap = event.getSource().getMap();
            if (newMap != selectedMap) {
                System.out.println("map changed");
                event.getSource().removeListener(this);

                newMap.getEditManager().addListener(this);
                selectedMap = newMap;
                return;
            }
            if (event.getType() == EditManagerEvent.SELECTED_LAYER) {
                // ILayer oldLayer = (ILayer) event.getOldValue();
                selectedLayer = (ILayer) event.getNewValue();
                System.out.println("layer changed");
                return;
            }
            System.out.println("other");
        }
    };

}
