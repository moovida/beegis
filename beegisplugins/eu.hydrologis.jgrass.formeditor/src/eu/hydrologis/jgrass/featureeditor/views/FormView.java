package eu.hydrologis.jgrass.featureeditor.views;

import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.ui.ApplicationGIS;
import net.refractions.udig.project.ui.IUDIGView;
import net.refractions.udig.project.ui.internal.tool.impl.ToolContextImpl;
import net.refractions.udig.project.ui.tool.IToolContext;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.opengis.feature.simple.SimpleFeature;

public class FormView extends ViewPart implements IUDIGView {

    private FormPropertiesPanel panel = null;

    public FormView() {

        IMap activeMap = ApplicationGIS.getActiveMap();
        ILayer selectedLayer = activeMap.getEditManager().getSelectedLayer();

        // ToolContextImpl
        //
        // panel = new FormPropertiesPanel();

    }

    public void createPartControl( Composite parent ) {
        panel.createControl(parent);
    }

    @Override
    public void init( IViewSite site ) throws PartInitException {
        super.init(site);
    }
    public void setFocus() {
        panel.setFocus();
    }

    private IToolContext context;
    public void setContext( IToolContext newContext ) {
        context = newContext;
    }

    public IToolContext getContext() {
        return context;
    }

    public void editFeatureChanged( SimpleFeature feature ) {
        panel.setEditFeature(feature, context);
    }
}
