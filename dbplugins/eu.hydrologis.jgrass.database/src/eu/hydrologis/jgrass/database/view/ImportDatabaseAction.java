package eu.hydrologis.jgrass.database.view;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.refractions.udig.ui.ExceptionDetailsDialog;
import net.refractions.udig.ui.PlatformGIS;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.jgrasstools.gears.utils.CompressionUtilities;

import eu.hydrologis.jgrass.database.DatabasePlugin;
import eu.hydrologis.jgrass.database.core.ConnectionManager;
import eu.hydrologis.jgrass.database.core.DatabaseConnectionProperties;

public class ImportDatabaseAction implements IViewActionDelegate {

    private IViewPart view;

    @Override
    public void init( IViewPart view ) {
        this.view = view;
    }

    @Override
    public void run( IAction action ) {
        if (view instanceof DatabaseView) {
            final DatabaseView dbView = (DatabaseView) view;

            FileDialog fileDialog = new FileDialog(view.getSite().getShell(), SWT.OPEN);
            fileDialog.setText("Select zip file of the database to import.");
            final String dbImportPath = fileDialog.open();

            DirectoryDialog folderDialog = new DirectoryDialog(view.getSite().getShell(), SWT.OPEN);
            folderDialog.setText("Select folder where to unzip the database to import it.");
            final String dbParentFolderPath = folderDialog.open();

            if (dbParentFolderPath != null && new File(dbParentFolderPath).isDirectory()) {
                IRunnableWithProgress operation = new IRunnableWithProgress(){
                    public void run( IProgressMonitor pm ) throws InvocationTargetException, InterruptedException {
                        try {
                            String internalFolderName = null;
                            ZipFile zf = new ZipFile(dbImportPath);
                            Enumeration< ? extends ZipEntry> e = zf.entries();
                            if (e.hasMoreElements()) {
                                ZipEntry ze = (ZipEntry) e.nextElement();
                                internalFolderName = ze.getName();
                                int sep = internalFolderName.indexOf(File.separatorChar);
                                internalFolderName = internalFolderName.substring(0, sep);
                            }
                            zf.close();

                            CompressionUtilities.unzipFolder(dbImportPath, dbParentFolderPath);
                            String dbName = internalFolderName; // new File(dbImportPath).getName();
                            File databaseFile = new File(dbParentFolderPath, dbName);
                            if (databaseFile.exists()) {
                                DatabaseConnectionProperties connectionProperties = ConnectionManager
                                        .createPropertiesBasedOnFolder(databaseFile);
                                String name = new File(dbImportPath).getName().replaceFirst("\\.zip$", "");
                                connectionProperties.put(DatabaseConnectionProperties.TITLE, name);
                                List<DatabaseConnectionProperties> databaseConnectionProperties = DatabasePlugin.getDefault()
                                        .getAvailableDatabaseConnectionProperties();
                                databaseConnectionProperties.add(connectionProperties);
                                dbView.relayout();
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                            String message = "An error occurred during the import of the database.";
                            ExceptionDetailsDialog.openError(null, message, IStatus.ERROR, DatabasePlugin.PLUGIN_ID, e);
                        }
                    }
                };
                PlatformGIS.runInProgressDialog("Export database", true, operation, true);
            }
        }
    }

    @Override
    public void selectionChanged( IAction action, ISelection selection ) {
    }

}
