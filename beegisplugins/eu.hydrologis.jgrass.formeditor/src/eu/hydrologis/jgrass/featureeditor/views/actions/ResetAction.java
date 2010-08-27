package eu.hydrologis.jgrass.featureeditor.views.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

import eu.hydrologis.jgrass.featureeditor.views.FormView;

public class ResetAction implements IViewActionDelegate {

    private IViewPart view;

    @Override
    public void run( IAction action ) {
        FormView formView = (FormView) view;
        formView.reset();
    }

    @Override
    public void selectionChanged( IAction action, ISelection selection ) {
    }

    @Override
    public void init( IViewPart view ) {
        this.view = view;
    }

}
