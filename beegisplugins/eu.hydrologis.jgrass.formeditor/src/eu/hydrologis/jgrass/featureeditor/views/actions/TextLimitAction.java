package eu.hydrologis.jgrass.featureeditor.views.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

import eu.hydrologis.jgrass.formeditor.FormEditorPlugin;

public class TextLimitAction implements IViewActionDelegate {

    private IViewPart view;

    @Override
    public void run( IAction action ) {

        IInputValidator validator = new IInputValidator(){

            public String isValid( String newText ) {
                try {
                    int limit = Integer.parseInt(newText);
                    if (limit < 1) {
                        throw new RuntimeException();
                    }
                } catch (Exception e) {
                    return "The vaue has to be an integer number > 0.";
                }
                return null;
            }

        };

        int textLimit = FormEditorPlugin.getDefault().getTextLimit();
        InputDialog iDialog = new InputDialog(view.getSite().getShell(), //
                "Text length limit", //
                "Insert a text limit to apply to the textfields:", textLimit + "", validator);
        iDialog.setBlockOnOpen(true);
        int open = iDialog.open();
        if (open == InputDialog.OK) {
            String value = iDialog.getValue();
            FormEditorPlugin.getDefault().setTextLimit(Integer.parseInt(value));
        }

    }

    @Override
    public void selectionChanged( IAction action, ISelection selection ) {
    }

    @Override
    public void init( IViewPart view ) {
        this.view = view;
    }

}
