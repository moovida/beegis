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
package eu.hydrologis.jgrass.gpsnmea.export;

import java.io.File;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Text;

import eu.hydrologis.jgrass.beegisutils.BeegisUtilsPlugin;

public class ExportGpxWizardPage extends WizardPage implements ModifyListener {

    private Text fileText;
    private String filePath = null;

    public ExportGpxWizardPage( String pageName, IStructuredSelection selection ) {
        super(pageName);
        setTitle(pageName);
        setDescription("Export the selected layer to gpx."); // NON-NLS-1
        // ImageDescriptor imageDescriptorFromPlugin =
        // AbstractUIPlugin.imageDescriptorFromPlugin(AnnotationPlugin.PLUGIN_ID,
        // "icons/highlight.gif");
        // setImageDescriptor(imageDescriptorFromPlugin);
    }

    public void createControl( Composite parent ) {
        final Composite fileSelectionArea = new Composite(parent, SWT.NONE);
        GridData fileSelectionData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
        fileSelectionArea.setLayoutData(fileSelectionData);

        GridLayout fileSelectionLayout = new GridLayout();
        fileSelectionLayout.numColumns = 2;
        fileSelectionLayout.makeColumnsEqualWidth = false;
        fileSelectionLayout.marginWidth = 0;
        fileSelectionLayout.marginHeight = 0;
        fileSelectionArea.setLayout(fileSelectionLayout);

        // folder chooser
        fileText = new Text(fileSelectionArea, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        fileText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        fileText.setText("Insert here the file to save...");
        fileText.addModifyListener(this);
        Button fileButton = new Button(fileSelectionArea, SWT.PUSH);
        fileButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        fileButton.setText("...");
        fileButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter(){
            public void widgetSelected( org.eclipse.swt.events.SelectionEvent e ) {
                FileDialog fileDialog = new FileDialog(fileSelectionArea.getShell(), SWT.SAVE);
                String lastFolderChosen = BeegisUtilsPlugin.getDefault().getLastFolderChosen();
                fileDialog.setFilterPath(lastFolderChosen);
                fileDialog.setFilterExtensions(new String[]{"*.gpx;*.GPX"});
                fileDialog.setText("Save file");
                filePath = fileDialog.open();
                if (filePath == null || filePath.length() < 1) {
                    fileText.setText("");
                } else {
                    fileText.setText(filePath);
                    String filterPath = fileDialog.getFilterPath();
                    BeegisUtilsPlugin.getDefault().setLastFolderChosen(filterPath);
                }
            }
        });

        setPageComplete(false);

        setControl(fileSelectionArea);
    }

    public String getFilePath() {
        return filePath;
    }

    public void modifyText( ModifyEvent e ) {
        filePath = fileText.getText();
        // to be ok, the parent folder has to exist
        File f = new File(filePath);
        if (filePath.length() > 0 && f.getParentFile().exists() && f.getName().length() > 0) {
            setPageComplete(true);
        } else {
            setPageComplete(false);
        }
    }
}
