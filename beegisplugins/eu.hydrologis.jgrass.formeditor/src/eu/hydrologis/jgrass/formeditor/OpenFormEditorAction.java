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
package eu.hydrologis.jgrass.formeditor;

import java.io.File;

import net.refractions.udig.catalog.ID;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.ui.ApplicationGIS;

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
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.geotools.data.FeatureSource;

/**
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public class OpenFormEditorAction implements IWorkbenchWindowActionDelegate {

    private IWorkbenchWindow window;

    public void dispose() {
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
                                String baseName = FilenameUtils.getBaseName(f.getName());
                                File newF = new File(f.getParentFile(), baseName + ".form");
                                if (!newF.exists()) {
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
