package eu.hydrologis.jgrass.database.view;

import net.refractions.udig.ui.ExceptionDetailsDialog;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;

import eu.hydrologis.jgrass.database.DatabasePlugin;

public class OpenDatabaseViewAction implements IWorkbenchWindowActionDelegate {

    private IWorkbenchWindow window;

    @Override
    public void dispose() {
    }

    @Override
    public void init( IWorkbenchWindow window ) {
        this.window = window;
    }

    @Override
    public void run( IAction action ) {
        try {
            window.getActivePage().showView(DatabaseView.ID);
        } catch (PartInitException e) {
            e.printStackTrace();
            String message = "An error occurred while opening the database view.";
            ExceptionDetailsDialog.openError(null, message, IStatus.ERROR, DatabasePlugin.PLUGIN_ID, e);
        }

    }

    @Override
    public void selectionChanged( IAction action, ISelection selection ) {
    }

}
