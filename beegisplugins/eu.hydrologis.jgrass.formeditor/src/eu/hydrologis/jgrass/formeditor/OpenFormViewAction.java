package eu.hydrologis.jgrass.formeditor;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;

import eu.hydrologis.jgrass.featureeditor.views.FormView;

public class OpenFormViewAction implements IWorkbenchWindowActionDelegate {

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
                    window.getActivePage().showView(FormView.ID);
                } catch (PartInitException e) {
                    e.printStackTrace();
                }
            }
        });

    }
    public void selectionChanged( IAction action, ISelection selection ) {
    }

}
