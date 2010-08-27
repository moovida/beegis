package eu.hydrologis.jgrass.featureeditor.views;

import net.refractions.udig.project.command.CompositeCommand;
import net.refractions.udig.project.command.factory.EditCommandFactory;
import net.refractions.udig.project.ui.tool.IToolContext;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opengis.feature.simple.SimpleFeature;

import eu.hydrologis.jgrass.featureeditor.xml.annotated.AForm;
import eu.hydrologis.jgrass.featureeditor.xml.annotatedguis.AFormGui;

/**
 * Properties panel adapting to *.form sidecar files.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public class FormPropertiesPanel {

    /** Used send commands to the edit blackboard */
    private IToolContext context;
    private final AForm form;
    private AFormGui formGui;
    private SimpleFeature editedFeature;
    private SimpleFeature oldFeature;

    public FormPropertiesPanel( AForm form ) {
        this.form = form;

    }

    public Control createControl( Composite parent ) {
        Composite mainComposite = new Composite(parent, SWT.NONE);
        mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        mainComposite.setLayout(new FillLayout());

        formGui = new AFormGui(form);
        Control control = formGui.makeGui(mainComposite);

        return control;
    }

    public void applyChanges() {
        // Display.getDefault().syncExec(new Runnable(){
        // public void run() {
        CompositeCommand compComm = new CompositeCommand();
        compComm.getCommands().add(EditCommandFactory.getInstance().createSetEditFeatureCommand(editedFeature));
        compComm.getCommands().add(EditCommandFactory.getInstance().createWriteEditFeatureCommand());
        // compComm.getCommands().add(EditCommandFactory.getInstance().createCommitCommand());
        context.getMap().sendCommandSync(compComm);
        // }
        // });
    }

    public void resetChanges() {
        setEditFeature(oldFeature, context);
    }

    public void setEditFeature( SimpleFeature featureToEdit, IToolContext newcontext ) {
        context = newcontext;

        oldFeature = featureToEdit;
        editedFeature = SimpleFeatureBuilder.copy(featureToEdit);
        formGui.setFeature(editedFeature);
    }

    public void setFocus() {
        // name.setFocus();
    }

}
