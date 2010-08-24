package eu.hydrologis.jgrass.featureeditor.views;

import net.miginfocom.layout.CC;
import net.miginfocom.swt.MigLayout;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.command.CompositeCommand;
import net.refractions.udig.project.ui.tool.IToolContext;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opengis.feature.IllegalAttributeException;
import org.opengis.feature.simple.SimpleFeature;

import eu.hydrologis.jgrass.featureeditor.xml.annotated.Form;

/**
 * Properties panel adapting to *.form sidecar files.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public class FormPropertiesPanel implements KeyListener, ISelectionChangedListener {

    private Button apply;
    private Button reset;
    private SimpleFeature editedFeature;
    private SimpleFeature oldFeature;

    /** Used send commands to the edit blackboard */
    private IToolContext context;
    private final Form form;

    public FormPropertiesPanel( Form form ) {
        this.form = form;

    }

    public void createControl( Composite parent ) {
        parent.setLayout(new MigLayout("", "[right]10[left, grow][min!][min!]", "30"));
        // SWT Widgets

        // Buttons
        apply = new Button(parent, SWT.PUSH);
        apply.setLayoutData("skip2");
        apply.setText("Apply");
        apply.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                applyChanges();
            }
        });
        apply.setEnabled(false);

        reset = new Button(parent, SWT.PUSH);
        reset.setText("Reset");
        reset.setEnabled(false);
        reset.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                resetChanges();
            }
        });
    }

    public void keyPressed( KeyEvent e ) {
        // do nothing
    }

    public void keyReleased( KeyEvent e ) {
        setEnabled(true);
    }

    private void setEnabled( boolean enabled ) {
        if (oldFeature == null && enabled) {
            return;
        }
        apply.setEnabled(enabled);
        reset.setEnabled(enabled);
    }

    /** Listen to the viewer */
    public void selectionChanged( SelectionChangedEvent event ) {
        IStructuredSelection selection = (IStructuredSelection) event.getSelection();

        Integer value = (Integer) selection.getFirstElement();
        setEnabled(true);
    }

    private void applyChanges() {
        try {
            // editedFeature.setAttribute(NAME, name.getText());
            // editedFeature.setAttribute(GMI_CNTRY, gmiCntry.getText());
            //
            // IStructuredSelection selection = (IStructuredSelection) colorMap.getSelection();
            // Integer color = (Integer) selection.getFirstElement();
            // editedFeature.setAttribute(COLOR_MAP, color.toString());

        } catch (IllegalAttributeException e1) {
            // shouldn't happen.
        }
        CompositeCommand compComm = new CompositeCommand();
        compComm.getCommands().add(context.getEditFactory().createSetEditFeatureCommand(editedFeature));
        compComm.getCommands().add(context.getEditFactory().createWriteEditFeatureCommand());
        context.sendASyncCommand(compComm);
        setEnabled(false);
    }

    private void resetChanges() {
        setEditFeature(oldFeature, context);
        setEnabled(false);
    }

    public void setEditFeature( SimpleFeature newFeature, IToolContext newcontext ) {
        this.context = newcontext;
        oldFeature = newFeature;
        if (oldFeature != null) {
            try {
                editedFeature = SimpleFeatureBuilder.copy(newFeature);
            } catch (IllegalAttributeException e) {
                // shouldn't happen
            }
        } else {
            editedFeature = null;
        }
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
