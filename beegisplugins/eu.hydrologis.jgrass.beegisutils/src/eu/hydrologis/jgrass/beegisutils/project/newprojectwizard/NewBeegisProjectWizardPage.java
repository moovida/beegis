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
import java.net.MalformedURLException;
import java.net.URL;

import net.refractions.udig.project.ui.internal.ISharedImages;
import net.refractions.udig.project.ui.internal.Messages;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import eu.hydrologis.jgrass.beegisutils.BeegisUtilsPlugin;

/**
 * A wizard page to create a new project.
 * 
 * @author vitalus
 * 
 * @since 0.3
 */
public class NewBeegisProjectWizardPage extends WizardPage {

    private static final String DATABASE = "-database";
    DirectoryFieldEditor projectDirectoryEditor;
    DirectoryFieldEditor databaseDirectoryEditor;

    StringFieldEditor projectNameEditor;

    /**
     * Construct <code>NewProjectWizardPage</code>.
     */
    public NewBeegisProjectWizardPage() {
        super(Messages.NewProjectWizardPage_newProject, Messages.NewProjectWizardPage_newProject, BeegisUtilsPlugin.getDefault()
                .getImageDescriptor(ISharedImages.NEWPROJECT_WIZBAN));
        setDescription(Messages.NewProjectWizardPage_newProject_description);
    }

    /**
     * Set up this page for use.
     * 
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     * @param parent
     */
    public void createControl( Composite parent ) {

        Composite composite = new Composite(parent, SWT.NONE);

        // PROJECT NAME
        projectNameEditor = new StringFieldEditor("newproject.name", Messages.NewProjectWizardPage_label_projectName, composite){
            protected boolean doCheckState() {
                return validate();
            }
        };
        projectNameEditor.setPage(this);
        projectNameEditor.setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
        projectNameEditor.setValidateStrategy(StringFieldEditor.VALIDATE_ON_FOCUS_LOST);
        Text textControl = projectNameEditor.getTextControl(composite);
        GridData gd = new GridData(SWT.LEFT, SWT.NONE, false, false);
        gd.widthHint = 100;
        gd.horizontalSpan = 2;
        textControl.setLayoutData(gd);

        // PROJECT PARENT FOLDER
        projectDirectoryEditor = new DirectoryFieldEditor(
                "newproject.directory", Messages.NewProjectWizardPage_label_projectDir, composite){ //$NON-NLS-1$
            protected boolean doCheckState() {
                String projectPath = getProjectPath();
                String dpPath = projectPath + DATABASE;
                databaseDirectoryEditor.setStringValue(dpPath);
                return validate();
            }
        };
        projectDirectoryEditor.setPage(this);
        projectDirectoryEditor.setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
        projectDirectoryEditor.setValidateStrategy(StringFieldEditor.VALIDATE_ON_FOCUS_LOST);
        projectDirectoryEditor.fillIntoGrid(composite, 3);

        // PROJECT'S DATABASE FOLDER
        databaseDirectoryEditor = new DirectoryFieldEditor(
                "newproject.databasedirectory", i18n.beegisutils.Messages.NewProjectWizardPage_label_databaseDir, composite){ //$NON-NLS-1$
            protected boolean doCheckState() {
                return validate();
            }
        };
        databaseDirectoryEditor.setPage(this);
        databaseDirectoryEditor.setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
        databaseDirectoryEditor.setValidateStrategy(StringFieldEditor.VALIDATE_ON_FOCUS_LOST);
        databaseDirectoryEditor.fillIntoGrid(composite, 3);

        String defaultProjectName = Messages.NewProjectWizardPage_default_name;

        String projectPath = BeegisUtilsPlugin.getDefault().getLastFolderChosen();
        projectNameEditor.setStringValue(defaultProjectName);
        projectDirectoryEditor.setStringValue(projectPath);
        String dpPath = projectPath + DATABASE;
        databaseDirectoryEditor.setStringValue(dpPath);

        composite.pack();

        setControl(composite);
        setPageComplete(true);
    }

    /**
     * Returns specified project name.
     * 
     * @return
     */
    public String getProjectName() {
        return projectNameEditor.getStringValue();
    }

    /**
     * Returns specified project path.
     * 
     * @return
     */
    public String getProjectPath() {
        return projectDirectoryEditor.getStringValue();
    }

    /**
     * Returns specified database path.
     * 
     * @return
     */
    public String getDatabasePath() {
        return databaseDirectoryEditor.getStringValue();
    }

    /**
     * Validates the form with project name and path.
     * 
     * @return
     * 		<code>true</code> if valid
     */
    public boolean validate() {

        final String projectPath = getProjectPath();
        final String databasePath = getDatabasePath();
        final String projectName = getProjectName();

        if (projectPath == null || projectPath.length() == 0) {
            setErrorMessage(Messages.NewProjectWizardPage_err_project_dir_valid);
            setPageComplete(false);
            return false;
        }

        File f = new File(projectPath + File.separator + projectName + ".udig");
        if (f.exists()) {
            setErrorMessage(Messages.NewProjectWizardPage_err_project_exists);
            setPageComplete(false);
            return false;
        }

        File projectPathFolder = null;
        try {
            URL projectURL = new URL("file:///" + projectPath); //$NON-NLS-1$
            projectPathFolder = new File(projectURL.getFile());

            String absolutePath = projectPathFolder.getAbsolutePath();
            if (!projectPath.equals(absolutePath)) {
                setErrorMessage(Messages.NewProjectWizardPage_err_project_dir_absolute);
                setPageComplete(false);
                return false;
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
            setPageComplete(false);
            return false;
        }

        if (projectPathFolder.exists()) {
            String projectFileAbsolutePath = projectPathFolder.getAbsolutePath() + File.separatorChar + "project.uprj"; //$NON-NLS-1$;
            File projectFile = new File(projectFileAbsolutePath);
            if (projectFile.exists()) {
                setErrorMessage(Messages.NewProjectWizardPage_err_project_exists);
                setPageComplete(false);
                return false;
            }
        } else {
            setErrorMessage(Messages.NewProjectWizardPage_err_project_dir_valid);
            setPageComplete(false);
            return false;
        }

        if (projectName == null || projectName.length() == 0) {
            setErrorMessage(Messages.NewProjectWizardPage_err_project_name);
            setPageComplete(false);
            return false;
        }

        if (databasePath != null && databasePath.length() > 0) {
            // database folder may not exist, but it must be possible to create it
            File parent = new File(databasePath).getParentFile();
            if (!parent.canWrite()) {
                setErrorMessage(Messages.NewProjectWizardPage_err_project_name);
                setPageComplete(false);
                return false;
            }
        }

        BeegisUtilsPlugin.getDefault().setLastFolderChosen(projectPath);

        setPageComplete(true);
        return true;
    }

}
