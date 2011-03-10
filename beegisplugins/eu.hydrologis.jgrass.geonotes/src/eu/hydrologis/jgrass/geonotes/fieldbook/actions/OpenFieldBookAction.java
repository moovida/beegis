package eu.hydrologis.jgrass.geonotes.fieldbook.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import eu.hydrologis.jgrass.geonotes.fieldbook.FieldbookView;

public class OpenFieldBookAction implements IWorkbenchWindowActionDelegate {

    public void dispose() {
    }

    public void init( IWorkbenchWindow window ) {
    }

    public void run( IAction action ) {
        try {
            IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            activePage.showView(FieldbookView.ID);
        } catch (PartInitException e) {
            e.printStackTrace();
        }
    }

    public void selectionChanged( IAction action, ISelection selection ) {
    }

}
