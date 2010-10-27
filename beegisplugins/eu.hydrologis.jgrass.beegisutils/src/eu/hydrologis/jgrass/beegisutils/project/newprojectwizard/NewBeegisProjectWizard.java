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
package eu.hydrologis.jgrass.beegisutils.project.newprojectwizard;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import net.refractions.udig.project.internal.Project;
import net.refractions.udig.project.internal.ProjectPlugin;
import net.refractions.udig.project.ui.internal.ProjectUIPlugin;
import net.refractions.udig.ui.ExceptionDetailsDialog;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;

import eu.hydrologis.jgrass.database.DatabasePlugin;
import eu.hydrologis.jgrass.database.core.DatabaseConnectionProperties;
import eu.hydrologis.jgrass.database.view.DatabaseView;

/**
 * Wizard to create a new udig project with an associated database.
 * 
 * <p>Extended from the new project wizard of udig.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class NewBeegisProjectWizard extends Wizard implements INewWizard {

    /** 
     * Wizard page used to fill parameters of new project 
     */
    protected NewBeegisProjectWizardPage page;
    private IWorkbench workbench;

    public void setPage( NewBeegisProjectWizardPage page ) {
        this.page = page;
        setWindowTitle(page.getTitle());
    }

    public void addPages() {
        page = new NewBeegisProjectWizardPage();
        addPage(page);
        setWindowTitle(page.getTitle());
        setHelpAvailable(true);
    }

    public boolean performFinish() {
        if (!page.validate()) {
            return false;
        }

        String projectPath = page.getProjectPath();
        String databasePath = page.getDatabasePath();
        final String projectName = page.getProjectName();
        projectPath = projectPath.replaceAll("\\\\", "/"); //$NON-NLS-1$//$NON-NLS-2$
        databasePath = databasePath.replaceAll("\\\\", "/"); //$NON-NLS-1$//$NON-NLS-2$

        while( projectPath.endsWith("/") ) { //$NON-NLS-1$
            projectPath = projectPath.substring(0, projectPath.length() - 2);
        }
        Project project = ProjectPlugin.getPlugin().getProjectRegistry()
                .getProject(projectPath + File.separator + projectName + ".udig"); //$NON-NLS-1$ //$NON-NLS-2$
        project.setName(projectName);
        Resource projectResource = project.eResource();
        try {
            projectResource.save(Collections.EMPTY_MAP);
        } catch (IOException e) {
            ProjectUIPlugin.log("Error during saving the project file of an new created project", e); //$NON-NLS-1$
        }
        
        /*
         * create also the database entry
         */
        
        File dbFolder = new File (databasePath);
        if (!dbFolder.exists()) {
            boolean mkdirs = dbFolder.mkdirs();
            if (!mkdirs) {
                // error in creating database
            }
        }
        
        try {
            DatabaseView dbView = (DatabaseView) workbench.getActiveWorkbenchWindow().getActivePage().showView(DatabaseView.ID);
            String projectNameNoSpace = projectName.replaceAll("\\s+", "_");
            DatabaseConnectionProperties newLocalDatabaseDefinition = dbView.createNewLocalDatabaseDefinition(databasePath, projectNameNoSpace);
            DatabasePlugin.getDefault().activateDatabaseConnection(newLocalDatabaseDefinition);
            dbView.relayout();
        } catch (Exception e) {
            e.printStackTrace();
            String message = "An error occurred while creating the database for project " + projectName;
            ExceptionDetailsDialog.openError(null, message, IStatus.ERROR, DatabasePlugin.PLUGIN_ID, e);
        }

        return true;
    }

    /**
     * We can finish if the user has entered a file.
     * 
     * @return true if we can finish
     */
    public boolean canFinish() {
        return page.isPageComplete();
    }

    public void init( IWorkbench workbench, IStructuredSelection selection ) {
        this.workbench = workbench;
    }

}
