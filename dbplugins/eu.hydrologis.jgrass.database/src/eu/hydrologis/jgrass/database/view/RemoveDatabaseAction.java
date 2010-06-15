package eu.hydrologis.jgrass.database.view;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

public class RemoveDatabaseAction implements IViewActionDelegate {

    private IViewPart view;

    @Override
    public void init( IViewPart view ) {
        this.view = view;
    }

    @Override
    public void run( IAction action ) {
        if (view instanceof DatabaseView) {
            DatabaseView dbView = (DatabaseView) view;

            boolean doRemove = MessageDialog
                    .openConfirm(dbView.getViewSite().getShell(), "Remove database warning",
                            "This will remove the database definition from the database view and can't be undone. Are you sure you want to contine?");

            if (doRemove) {
                dbView.removeCurrentSelectedDatabaseDefinition();
            }
        }

    }

    @Override
    public void selectionChanged( IAction action, ISelection selection ) {

        System.out.println();

    }

}
