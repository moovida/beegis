package eu.hydrologis.jgrass.database.view;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import net.refractions.udig.ui.ExceptionDetailsDialog;
import net.refractions.udig.ui.PlatformGIS;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.jgrasstools.gears.utils.CompressionUtilities;

import eu.hydrologis.jgrass.database.DatabasePlugin;
import eu.hydrologis.jgrass.database.core.ConnectionManager;
import eu.hydrologis.jgrass.database.core.DatabaseConnectionProperties;

public class ExportDatabaseAction implements IViewActionDelegate {

    private IViewPart view;

    @Override
    public void init( IViewPart view ) {
        this.view = view;
    }

    @Override
    public void run( IAction action ) {
        if (view instanceof DatabaseView) {
            DatabaseView dbView = (DatabaseView) view;

            DatabaseConnectionProperties properties = dbView.getCurrentSelectedConnectionProperties();
            if (ConnectionManager.isLocal(properties)) {
                FileDialog fileDialog = new FileDialog(view.getSite().getShell(), SWT.SAVE);
                fileDialog.setText("Select zip file to which to export the database.");
                fileDialog.setFileName(properties.getTitle() + ".zip");
                fileDialog.setOverwrite(true);
                final String newDbPath = fileDialog.open();

                if (newDbPath != null && new File(newDbPath).getParentFile().isDirectory()) {
                    final String dbPath = properties.getPath();
                    IRunnableWithProgress operation = new IRunnableWithProgress(){
                        public void run( IProgressMonitor pm ) throws InvocationTargetException, InterruptedException {
                            try {
                                CompressionUtilities.zipFolder(dbPath, newDbPath, true);
                            } catch (IOException e) {
                                e.printStackTrace();
                                String message = "An error occurred during the export of the database.";
                                ExceptionDetailsDialog.openError(null, message, IStatus.ERROR, DatabasePlugin.PLUGIN_ID, e);
                            }
                        }
                    };
                    PlatformGIS.runInProgressDialog("Export database", true, operation, true);
                }
            }
        }
    }

    @Override
    public void selectionChanged( IAction action, ISelection selection ) {
    }

}
