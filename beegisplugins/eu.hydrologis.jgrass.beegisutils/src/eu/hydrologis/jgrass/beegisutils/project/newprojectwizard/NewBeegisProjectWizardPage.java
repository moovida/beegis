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

import net.refractions.udig.project.ui.internal.Messages;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import eu.hydrologis.jgrass.beegisutils.BeegisUtilsPlugin;

/**
 * A wizard page to create a new BeeGIS project.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class NewBeegisProjectWizardPage extends WizardPage {
    public static final String ID = "NewBeegisProjectWizardPage"; //$NON-NLS-1$

    private static final String BASE_PREFIX = "Overall project folder: ";
    private static final String UDIGPROJECTNAME_SUFFIX = ".udig";
    private static final String UDIGPROJECTNAME_PREFIX = "Folder of the uDig/BeeGIS project: ";
    private static final String DATABASE_SUFFIX = "-database";
    private static final String DATABASE_PREFIX = "Folder of the project's database: ";

    private String overallProjectPath = null;
    private String udigProjectPath = null;
    private String projectDatabasePath = null;

    DirectoryFieldEditor projectDirectoryEditor;

    StringFieldEditor projectNameEditor;

    private Label overallProjectFolderlabel;

    private Label udigProjectFolderlabel;

    private Label projectDatabaseFolderlabel;

    /**
     * Construct <code>NewProjectWizardPage</code>.
     */
    public NewBeegisProjectWizardPage() {
        super(ID);
        setTitle(Messages.NewProjectWizardPage_newProject);
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
        projectNameEditor = new StringFieldEditor("newproject.name", "Name of the new project", composite){
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
        projectDirectoryEditor = new DirectoryFieldEditor("newproject.directory", "Base folder for the new project", composite){ //$NON-NLS-1$
            protected boolean doCheckState() {
                updatePathsAndLabels();
                return validate();
            }
        };
        projectDirectoryEditor.setPage(this);
        projectDirectoryEditor.setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
        projectDirectoryEditor.setValidateStrategy(StringFieldEditor.VALIDATE_ON_FOCUS_LOST);
        projectDirectoryEditor.fillIntoGrid(composite, 3);

        Group resultGroup = new Group(composite, SWT.NONE);
        GridData resultGroupGD = new GridData(SWT.FILL, SWT.FILL, true, false);
        resultGroupGD.horizontalSpan = 3;
        resultGroup.setLayoutData(resultGroupGD);
        resultGroup.setLayout(new GridLayout(1, false));
        resultGroup.setText("Resulting folders summary");

        overallProjectFolderlabel = new Label(resultGroup, SWT.NONE);
        overallProjectFolderlabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        udigProjectFolderlabel = new Label(resultGroup, SWT.NONE);
        udigProjectFolderlabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        projectDatabaseFolderlabel = new Label(resultGroup, SWT.NONE);
        projectDatabaseFolderlabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        String defaultProjectName = Messages.NewProjectWizardPage_default_name;
        String projectPath = BeegisUtilsPlugin.getDefault().getLastFolderChosen();
        projectNameEditor.setStringValue(defaultProjectName);
        projectDirectoryEditor.setStringValue(projectPath);

        updatePathsAndLabels();

        composite.pack();

        setControl(composite);
        setPageComplete(true);
    }

    private void updatePathsAndLabels() {
        String baseFolder = projectDirectoryEditor.getStringValue();
        String projectName = projectNameEditor.getStringValue();

        overallProjectPath = baseFolder + File.separator + projectName;
        udigProjectPath = overallProjectPath + File.separator + projectName + UDIGPROJECTNAME_SUFFIX;
        projectDatabasePath = overallProjectPath + File.separator + projectName + DATABASE_SUFFIX;

        overallProjectFolderlabel.setText(BASE_PREFIX + overallProjectPath);
        udigProjectFolderlabel.setText(UDIGPROJECTNAME_PREFIX + udigProjectPath);
        projectDatabaseFolderlabel.setText(DATABASE_PREFIX + projectDatabasePath);
    }

    public String getProjectName() {
        return projectNameEditor.getStringValue();
    }

    /**
     * Returns specified project path.
     * 
     * @return
     */
    public String getProjectPath() {
        return udigProjectPath;
    }

    /**
     * Returns specified database path.
     * 
     * @return
     */
    public String getDatabasePath() {
        return projectDatabasePath;
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

        if (projectPath == null || projectPath.length() == 0) {
            setErrorMessage(Messages.NewProjectWizardPage_err_project_dir_valid);
            setPageComplete(false);
            return false;
        }

        File f = new File(projectPath);
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

        File overallFolder = new File(overallProjectPath);
        File parentFile = overallFolder.getParentFile();
        if (parentFile.exists()) {
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

        if (databasePath != null && databasePath.length() > 0) {
            // database folder may not exist, but it must be possible to create it
            File parent = new File(databasePath).getParentFile();
            if (!parent.getParentFile().canWrite()) {
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
