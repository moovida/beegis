package eu.hydrologis.jgrass.database.view;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

public class CreateNewLocalDatabaseAction implements IViewActionDelegate {

    private IViewPart view;

    @Override
    public void init( IViewPart view ) {
        this.view = view;
    }

    @Override
    public void run( IAction action ) {
        if (view instanceof DatabaseView) {
            DatabaseView dbView = (DatabaseView) view;

            dbView.createNewLocalDatabaseDefinition();
        }
    }

    @Override
    public void selectionChanged( IAction action, ISelection selection ) {
        // TODO Auto-generated method stub

    }

}
