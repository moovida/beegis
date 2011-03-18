package eu.hydrologis.jgrass.featureeditor.utils;

import java.io.File;

import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.interceptor.FeatureInterceptor;
import net.refractions.udig.project.ui.ApplicationGIS;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.opengis.feature.Feature;

import eu.hydrologis.jgrass.featureeditor.views.FormView;
import eu.hydrologis.jgrass.formeditor.FormEditorPlugin;

public class OnFeatureCreateOpenFormViewInterceptor implements FeatureInterceptor {

    public OnFeatureCreateOpenFormViewInterceptor() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void run( Feature feature ) {
        IMap activeMap = ApplicationGIS.getActiveMap();
        if (activeMap != null) {
            ILayer selectedLayer = activeMap.getEditManager().getSelectedLayer();
            File formFile = FormEditorPlugin.getDefault().getFormFile(selectedLayer);
            if (formFile != null) {
                // open form view
                Display.getDefault().asyncExec(new Runnable(){
                    public void run() {
                        try {
                            IWorkbench workbench = PlatformUI.getWorkbench();
                            IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
                            IWorkbenchPage activePage = null;
                            if (activeWorkbenchWindow == null) {
                                IWorkbenchWindow[] workbenchWindows = workbench.getWorkbenchWindows();
                                if (workbenchWindows.length > 0) {
                                    activeWorkbenchWindow = workbenchWindows[0];
                                }
                            }
                            activePage = activeWorkbenchWindow.getActivePage();
                            activePage.showView(FormView.ID);
                        } catch (PartInitException e) {
                            e.printStackTrace();
                        }
                    }
                });

            }

        }
    }

}
