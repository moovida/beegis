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

import net.miginfocom.swt.MigLayout;
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
import net.refractions.udig.project.internal.Map;
import net.refractions.udig.project.ui.ApplicationGIS;
import net.refractions.udig.project.ui.IUDIGView;
import net.refractions.udig.project.ui.internal.tool.impl.ToolContextImpl;
import net.refractions.udig.project.ui.tool.IToolContext;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.opengis.feature.simple.SimpleFeature;

import eu.hydrologis.jgrass.featureeditor.utils.ISelectionObserver;
import eu.hydrologis.jgrass.featureeditor.utils.MapLayerHandler;
import eu.hydrologis.jgrass.featureeditor.utils.Utilities;
import eu.hydrologis.jgrass.formeditor.FormEditorPlugin;

/**
 * The Form View to visualize and edit form enabled layers.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class FormView extends ViewPart implements ISelectionObserver {

    // private FormPropertiesPanel panel = null;
    private ILayer selectedLayer;
    private IMap selectedMap;
    private SimpleFeature selectedFeature;
    private File selectedLayerFormFile;

    private Control currentControl = null;
    private Composite parentComposite;

    private FormPropertiesPanel featurePropertiesPanel;
    private Composite parent;

    /**
     * Check which layer is currently selected and if the selection changed.
     * 
     * @return true if the selected layer changed.
     */
    // private boolean checkSelectedLayer() {
    // IMap tmpSelectedMap = ApplicationGIS.getActiveMap();
    // if (selectedMap == null || tmpSelectedMap.getID().equals(selectedMap.getID())) {
    // // map changed or init
    // if (selectedMap != null) {
    // selectedMap.removeMapListener(this);
    // }
    // selectedMap = tmpSelectedMap;
    // selectedMap.addMapListener(this);
    // }
    //
    // ILayer tmpSelectedLayer = selectedMap.getEditManager().getSelectedLayer();
    // boolean hasChanged = false;
    // if (selectedLayer == null || !tmpSelectedLayer.getID().equals(selectedLayer.getID())) {
    // /*
    // * layer changed
    // */
    // hasChanged = true;
    // ID id = tmpSelectedLayer.getGeoResource().getID();
    // if (id.isFile()) {
    // File file = id.toFile();
    // selectedLayerFormFile = Utilities.getFormFile(file);
    // }
    //
    // // reset listeners
    // if (selectedLayer != null) {
    // selectedLayer.removeListener(this);
    // }
    // selectedLayer = tmpSelectedLayer;
    // selectedLayer.addListener(this);
    // }
    //
    // // panel.updateOnLayer(selectedLayer);
    // return hasChanged;
    // }

    public void createPartControl( Composite parent ) {
        this.parent = parent;
        parentComposite = new Composite(parent, SWT.NONE);
        parentComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        parentComposite.setLayout(new GridLayout(1, true));

        Label startSelectionLabel = new Label(parentComposite, SWT.NONE);
        startSelectionLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        startSelectionLabel.setText("Select a feature to start.");
        currentControl = startSelectionLabel;
    }

    @Override
    public void init( IViewSite site ) throws PartInitException {
        super.init(site);

        FormEditorPlugin.getDefault().addSelectionListener(this);
    }

    @Override
    public void dispose() {
        FormEditorPlugin.getDefault().removeSelectionListener(this);
        super.dispose();
    }

    public void setFocus() {
        System.out.println("focus");
        // panel.setFocus();
    }

    @Override
    public void selectionChanged( Map selectedMap, final ILayer newLayer, final SimpleFeature selectedFeature ) {
        Display.getDefault().syncExec(new Runnable(){
            public void run() {
                updateGui(newLayer, selectedFeature);
            }
        });
    }

    private void updateGui( final ILayer newLayer, SimpleFeature newFeature ) {
        if (newFeature == null) {
            selectedLayer = newLayer;
            if (currentControl != null) {
                currentControl.dispose();
            }
            Label noFeatureSelectedLabel = new Label(parentComposite, SWT.NONE);
            noFeatureSelectedLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
            String name = "";
            if (selectedLayer != null) {
                name = selectedLayer.getName();
            }
            noFeatureSelectedLabel.setText("No feature selected on layer " + name);
            currentControl = noFeatureSelectedLabel;
            featurePropertiesPanel = null;
            parentComposite.layout();
            parentComposite.redraw();
            return;
        }
        if (newLayer != selectedLayer || featurePropertiesPanel == null) {
            selectedLayer = newLayer;
            ID id = selectedLayer.getGeoResource().getID();
            if (id.isFile()) {
                File file = id.toFile();
                selectedLayerFormFile = Utilities.getFormFile(file);
            }
            if (currentControl != null) {
                currentControl.dispose();
            }
            if (selectedLayerFormFile != null) {
                // layer changed, has form file, load new gui
                try {
                    featurePropertiesPanel = new FormPropertiesPanel(Utilities.readForm(selectedLayerFormFile));
                    currentControl = featurePropertiesPanel.createControl(parentComposite);
                } catch (Exception e) {
                    e.printStackTrace();
                    selectedLayerFormFile = null;
                }
            }
            if (selectedLayerFormFile == null) {
                // layer changed, has no form file
                Label noSupportLabel = new Label(parentComposite, SWT.NONE);
                noSupportLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
                String name = "";
                if (selectedLayer != null) {
                    name = selectedLayer.getName();
                }
                noSupportLabel.setText("The layer " + name + " has no valid form file associated.");
                currentControl = noSupportLabel;
                featurePropertiesPanel = null;
            }
            parentComposite.layout();
            parentComposite.redraw();
        } else {
            // layer the same, feature changed
            ToolContextImpl newcontext = new ToolContextImpl();
            newcontext.setMapInternal((Map) selectedMap);
            featurePropertiesPanel.setEditFeature(newFeature, newcontext);
        }
    }

}
