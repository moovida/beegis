package eu.hydrologis.jgrass.featureeditor.views;

import net.miginfocom.swt.MigLayout;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.command.CompositeCommand;
import net.refractions.udig.project.ui.tool.IToolContext;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opengis.feature.IllegalAttributeException;
import org.opengis.feature.simple.SimpleFeature;

import eu.hydrologis.jgrass.featureeditor.xml.annotated.Form;
import eu.hydrologis.jgrass.featureeditor.xml.annotatedguis.FormGuiElement;
import eu.hydrologis.jgrass.featureeditor.xml.annotatedguis.FormGuiFactory;

/**
 * Properties panel adapting to *.form sidecar files.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public class FormPropertiesPanel implements KeyListener, ISelectionChangedListener {

    /** Used send commands to the edit blackboard */
    private IToolContext context;
    private final Form form;

    public FormPropertiesPanel( Form form ) {
        this.form = form;

    }

    public Control createControl( Composite parent ) {
        Composite mainComposite = new Composite(parent, SWT.NONE);
        mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        mainComposite.setLayout(new FillLayout());

        FormGuiElement formGui = FormGuiFactory.createFormGui(form);
        Control control = formGui.makeGui(mainComposite);

        return control;
    }

    public void keyPressed( KeyEvent e ) {
        // do nothing
    }

    public void keyReleased( KeyEvent e ) {
        setEnabled(true);
    }

    private void setEnabled( boolean enabled ) {
    }

    /** Listen to the viewer */
    public void selectionChanged( SelectionChangedEvent event ) {
        IStructuredSelection selection = (IStructuredSelection) event.getSelection();

        Integer value = (Integer) selection.getFirstElement();
        setEnabled(true);
    }

    private void applyChanges() {
        // try {
        // // editedFeature.setAttribute(NAME, name.getText());
        // // editedFeature.setAttribute(GMI_CNTRY, gmiCntry.getText());
        // //
        // // IStructuredSelection selection = (IStructuredSelection) colorMap.getSelection();
        // // Integer color = (Integer) selection.getFirstElement();
        // // editedFeature.setAttribute(COLOR_MAP, color.toString());
        //
        // } catch (IllegalAttributeException e1) {
        // // shouldn't happen.
        // }
        // CompositeCommand compComm = new CompositeCommand();
        // compComm.getCommands().add(context.getEditFactory().createSetEditFeatureCommand(editedFeature));
        // compComm.getCommands().add(context.getEditFactory().createWriteEditFeatureCommand());
        // context.sendASyncCommand(compComm);
        // setEnabled(false);
    }

    private void resetChanges() {
        // setEditFeature(oldFeature, context);
        // setEnabled(false);
    }

    public void setEditFeature( SimpleFeature newFeature, IToolContext newcontext ) {
        // this.context = newcontext;
        // oldFeature = newFeature;
        // if (oldFeature != null) {
        // try {
        // editedFeature = SimpleFeatureBuilder.copy(newFeature);
        // } catch (IllegalAttributeException e) {
        // // shouldn't happen
        // }
        // } else {
        // editedFeature = null;
        // }
        // if (oldFeature == null) {
        // gmiCntry.setText("");
        // colorMap.setSelection(new StructuredSelection());
        // name.setText("");
        // } else {
        // String nameText = (String) oldFeature.getAttribute(NAME);
        // if (nameText == null)
        // nameText = "";
        // name.setText(nameText);
        //
        // String gmiText = (String) oldFeature.getAttribute(GMI_CNTRY);
        // if (gmiText == null)
        // gmiText = "";
        // gmiCntry.setText(gmiText);
        //
        // String colorText = (String) oldFeature.getAttribute(COLOR_MAP);
        // if (colorText != null && !colorText.equals("")) {
        // StructuredSelection selection = new StructuredSelection(new Integer(colorText));
        // colorMap.setSelection(selection);
        // } else {
        // colorMap.setSelection(new StructuredSelection());
        // }
        // }
        setEnabled(false);
    }

    public void setFocus() {
        // name.setFocus();
    }

    public void updateOnLayer( ILayer selectedLayer ) {
        // TODO Auto-generated method stub

    }
}
