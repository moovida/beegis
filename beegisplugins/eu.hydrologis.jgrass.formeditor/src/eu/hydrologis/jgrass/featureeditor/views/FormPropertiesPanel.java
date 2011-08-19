/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.hydrologis.jgrass.featureeditor.views;

import net.refractions.udig.project.command.CompositeCommand;
import net.refractions.udig.project.command.factory.EditCommandFactory;
import net.refractions.udig.project.ui.tool.IToolContext;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
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
        formGui.makeGui(mainComposite);

        return mainComposite;
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
        
        setEditFeature(editedFeature, context);
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
