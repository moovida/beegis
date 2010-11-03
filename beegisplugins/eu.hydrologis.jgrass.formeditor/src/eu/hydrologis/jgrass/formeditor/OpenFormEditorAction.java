package eu.hydrologis.jgrass.formeditor;

import java.io.File;
import java.util.List;

import net.refractions.udig.catalog.ID;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.ui.ApplicationGIS;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.geotools.data.FeatureSource;
import org.opengis.feature.type.AttributeDescriptor;

import eu.hydrologis.jgrass.featureeditor.utils.Utilities;

public class OpenFormEditorAction implements IWorkbenchWindowActionDelegate {

    private IWorkbenchWindow window;

    public void dispose() {
        // TODO Auto-generated method stub

    }

    public void init( IWorkbenchWindow window ) {
        this.window = window;
    }

    public void run( IAction action ) {

        Display.getDefault().asyncExec(new Runnable(){

            public void run() {

                try {

                    IMap activeMap = ApplicationGIS.getActiveMap();
                    if (activeMap != null) {
                        ILayer selectedLayer = activeMap.getEditManager().getSelectedLayer();
                        if (selectedLayer.hasResource(FeatureSource.class)) {
                            IGeoResource geoResource = selectedLayer.getGeoResource();
                            ID id = geoResource.getID();
                            boolean isFile = id.isFile();
                            if (isFile) {
                                File f = id.toFile();

                                File newF = Utilities.getFormFile(f);
                                if (newF != null && !newF.exists()) {
                                    newF.createNewFile();
                                } else if (newF == null) {
                                    // ask use for file position
                                    FileDialog fileDialog = new FileDialog(window.getShell(), SWT.SAVE);
                                    fileDialog.setFilterExtensions(new String[]{"*.form"});
                                    String path = fileDialog.open();
                                    newF = new File(path);
                                    newF.createNewFile();
                                }

                                final IPath ipath = new Path(newF.getAbsolutePath());
                                IFileStore fileLocation = EFS.getLocalFileSystem().getStore(ipath);
                                FileStoreEditorInput fileStoreEditorInput = new FileStoreEditorInput(fileLocation);
                                IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                                page.openEditor(fileStoreEditorInput, FormEditor.ID);

                                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                                        .showView("org.eclipse.ui.views.PropertySheet");

                                return;
                            }

                        }

                    }

                    FileDialog fileDialog = new FileDialog(window.getShell(), SWT.OPEN);
                    fileDialog.setFilterExtensions(new String[]{"*.form"});
                    String path = fileDialog.open();

                    if (path == null || path.length() < 1) {
                        return;
                    }

                    final File f = new File(path);
                    if (!f.exists()) {
                        return;
                    }

                    final IPath ipath = new Path(f.getAbsolutePath());
                    IFileStore fileLocation = EFS.getLocalFileSystem().getStore(ipath);
                    FileStoreEditorInput fileStoreEditorInput = new FileStoreEditorInput(fileLocation);
                    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                    page.openEditor(fileStoreEditorInput, FormEditor.ID);

                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                            .showView("org.eclipse.ui.views.PropertySheet");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }
    public void selectionChanged( IAction action, ISelection selection ) {
        // TODO Auto-generated method stub

    }

}
