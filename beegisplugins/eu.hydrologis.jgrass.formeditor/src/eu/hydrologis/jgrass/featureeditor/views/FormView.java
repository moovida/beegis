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
package eu.hydrologis.jgrass.featureeditor.views;

import java.io.File;

import net.refractions.udig.catalog.ID;
import net.refractions.udig.project.EditManagerEvent;
import net.refractions.udig.project.IEditManagerListener;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.ILayerListener;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.IMapListener;
import net.refractions.udig.project.LayerEvent;
import net.refractions.udig.project.LayerEvent.EventType;
import net.refractions.udig.project.MapEvent;
import net.refractions.udig.project.ui.ApplicationGIS;
import net.refractions.udig.project.ui.IUDIGView;
import net.refractions.udig.project.ui.tool.IToolContext;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.opengis.feature.simple.SimpleFeature;

import eu.hydrologis.jgrass.featureeditor.utils.MapLayerHandler;
import eu.hydrologis.jgrass.featureeditor.utils.Utilities;

/**
 * The Form View to visualize and edit form enabled layers.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class FormView extends ViewPart implements IUDIGView {

    // private FormPropertiesPanel panel = null;
    private ILayer selectedLayer;
    private IMap selectedMap;
    private File selectedLayerFormFile;

    private Control currentControl = null;
    private Composite parentComposite;

    /**
     * Check which layer is currently selected and if the selection changed.
     * 
     * @return true if the selected layer changed.
     */
//    private boolean checkSelectedLayer() {
//        IMap tmpSelectedMap = ApplicationGIS.getActiveMap();
//        if (selectedMap == null || tmpSelectedMap.getID().equals(selectedMap.getID())) {
//            // map changed or init
//            if (selectedMap != null) {
//                selectedMap.removeMapListener(this);
//            }
//            selectedMap = tmpSelectedMap;
//            selectedMap.addMapListener(this);
//        }
//
//        ILayer tmpSelectedLayer = selectedMap.getEditManager().getSelectedLayer();
//        boolean hasChanged = false;
//        if (selectedLayer == null || !tmpSelectedLayer.getID().equals(selectedLayer.getID())) {
//            /*
//             * layer changed
//             */
//            hasChanged = true;
//            ID id = tmpSelectedLayer.getGeoResource().getID();
//            if (id.isFile()) {
//                File file = id.toFile();
//                selectedLayerFormFile = Utilities.getFormFile(file);
//            }
//
//            // reset listeners
//            if (selectedLayer != null) {
//                selectedLayer.removeListener(this);
//            }
//            selectedLayer = tmpSelectedLayer;
//            selectedLayer.addListener(this);
//        }
//
//        // panel.updateOnLayer(selectedLayer);
//        return hasChanged;
//    }

    public void createPartControl( Composite parent ) {
        this.parentComposite = parent;
        updateGui();
    }

    private void updateGui() {
        Display.getDefault().syncExec(new Runnable(){
            public void run() {
                boolean hasChanged =false;// checkSelectedLayer();
                if (hasChanged) {
                    if (currentControl != null) {
                        currentControl.dispose();
                    }
                    if (selectedLayerFormFile == null) {
                        setNoFormPanel(parentComposite);
                    } else {
                        setFormPanel(parentComposite);
                    }
                } else {
                    // just update the gui content

                }
                parentComposite.layout();
            }
        });
    }

    private void setFormPanel( Composite parent ) {
        Label dummyLabel = new Label(parent, SWT.NONE);
        dummyLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        String name = "";
        if (selectedLayer != null) {
            name = selectedLayer.getName();
        }
        dummyLabel.setText("This layer " + name + " has a dummy form file associated.");
        currentControl = dummyLabel;
    }

    private void setNoFormPanel( Composite parent ) {
        Label noSupportLabel = new Label(parent, SWT.NONE);
        noSupportLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        String name = "";
        if (selectedLayer != null) {
            name = selectedLayer.getName();
        }
        noSupportLabel.setText("This layer " + name + " has no form file associated.");
        currentControl = noSupportLabel;
    }

    @Override
    public void init( IViewSite site ) throws PartInitException {
        super.init(site);
        MapLayerHandler.getInstance();
    }
    public void setFocus() {
        System.out.println("focus");
        // panel.setFocus();
    }

    private IToolContext context;

    public void setContext( IToolContext newContext ) {
        context = newContext;
    }

    public IToolContext getContext() {
        return context;
    }

    public void editFeatureChanged( SimpleFeature feature ) {
        // panel.setEditFeature(feature, context);
        System.out.println("editfeature");
    }

    /* 
     * Map events.
     * 
     * @see net.refractions.udig.project.IMapListener#changed(net.refractions.udig.project.MapEvent)
     */
    public void changed( MapEvent event ) {
        updateGui();
        System.out.println("mapevent");
    }

    /* 
     * Layer events.
     * 
     * @see net.refractions.udig.project.ILayerListener#refresh(net.refractions.udig.project.LayerEvent)
     */
    public void refresh( LayerEvent event ) {
        EventType type = event.getType();
        if (type == EventType.FILTER) {
            System.out.println("feature selection has changed " + System.currentTimeMillis());
        }

    }
    
    


}
