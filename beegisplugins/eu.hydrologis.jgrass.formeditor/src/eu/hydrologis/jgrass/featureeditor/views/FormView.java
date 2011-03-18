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
import java.io.IOException;

import net.refractions.udig.catalog.ID;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.internal.Map;
import net.refractions.udig.project.ui.internal.ApplicationGISInternal;
import net.refractions.udig.project.ui.internal.tool.impl.ToolContextImpl;

import org.eclipse.core.runtime.NullProgressMonitor;
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
import org.geotools.data.FeatureSource;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.feature.simple.SimpleFeature;

import eu.hydrologis.jgrass.featureeditor.utils.ISelectionObserver;
import eu.hydrologis.jgrass.featureeditor.utils.Utilities;
import eu.hydrologis.jgrass.formeditor.FormEditorPlugin;

/**
 * The Form View to visualize and edit form enabled layers.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class FormView extends ViewPart implements ISelectionObserver {

    public static final String ID = "eu.hydrologis.jgrass.featureeditor.views.FormView"; //$NON-NLS-1$

    private ILayer selectedLayer;
    private IMap selectedMap;
    private File selectedLayerFormFile;

    private Control currentControl = null;
    private Composite parentComposite;

    private FormPropertiesPanel featurePropertiesPanel;

    public void createPartControl( Composite parent ) {
        parentComposite = new Composite(parent, SWT.NONE);
        parentComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        parentComposite.setLayout(new GridLayout(1, true));

        Label startSelectionLabel = new Label(parentComposite, SWT.NONE);
        startSelectionLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        startSelectionLabel.setText("Select a feature to start.");
        currentControl = startSelectionLabel;

        // if one selected, load it already
        try {
            Map activeMap = ApplicationGISInternal.getActiveMap();
            if (activeMap != null) {
                ILayer selectedLayer = activeMap.getEditManager().getSelectedLayer();
                if (selectedLayer != null) {
                    SimpleFeatureSource featureSource = (SimpleFeatureSource) selectedLayer.getResource(FeatureSource.class,
                            new NullProgressMonitor());
                    if (featureSource == null) {
                        return;
                    }
                    SimpleFeatureCollection featureCollection = featureSource.getFeatures(selectedLayer.getQuery(true));
                    SimpleFeatureIterator features = featureCollection.features();
                    if (features.hasNext()) {
                        selectionChanged(activeMap, selectedLayer, features.next());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void init( IViewSite site ) throws PartInitException {
        super.init(site);

        FormEditorPlugin.getDefault().registerPartListener();
        FormEditorPlugin.getDefault().addSelectionListener(this);
    }

    @Override
    public void dispose() {
        FormEditorPlugin.getDefault().removeSelectionListener(this);
        super.dispose();
    }

    public void setFocus() {
        // panel.setFocus();
    }

    @Override
    public void selectionChanged( final Map selectedMap, final ILayer newLayer, final SimpleFeature selectedFeature ) {
        Display.getDefault().syncExec(new Runnable(){
            public void run() {
                updateGui(selectedMap, newLayer, selectedFeature);
            }
        });
    }

    private void updateGui( Map newMap, final ILayer newLayer, SimpleFeature newFeature ) {
        selectedMap = newMap;

        if (newFeature != null && featurePropertiesPanel != null && newLayer.equals(selectedLayer)) {
            // layer the same, feature changed
            ToolContextImpl newcontext = new ToolContextImpl();
            newcontext.setMapInternal((Map) selectedMap);
            featurePropertiesPanel.setEditFeature(newFeature, newcontext);
            return;
        }

        // every other case has the current control to be disposed
        if (currentControl != null) {
            currentControl.dispose();
        }

        if (newFeature == null) {
            selectedLayer = newLayer;
            Label noFeatureSelectedLabel = new Label(parentComposite, SWT.NONE);
            noFeatureSelectedLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
            String name = "";
            if (selectedLayer != null) {
                name = selectedLayer.getName();
            }
            noFeatureSelectedLabel.setText("No feature selected on layer " + name);
            currentControl = noFeatureSelectedLabel;
            featurePropertiesPanel = null;
        } else if (newLayer != selectedLayer || featurePropertiesPanel == null) {
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
                    ToolContextImpl newcontext = new ToolContextImpl();
                    newcontext.setMapInternal((Map) selectedMap);
                    featurePropertiesPanel.setEditFeature(newFeature, newcontext);
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
        } else {
            throw new IllegalArgumentException();
        }

        parentComposite.layout();
        parentComposite.redraw();
    }

    public void apply() {
        if (featurePropertiesPanel != null) {
            featurePropertiesPanel.applyChanges();
        }
    }

    public void reset() {
        if (featurePropertiesPanel != null) {
            featurePropertiesPanel.resetChanges();
        }
    }

}
