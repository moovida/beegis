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
package eu.hydrologis.jgrass.formeditor.model.widgets;

import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * A cell editor that manages file path and the button to browse for it.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class FilepathCellEditor extends DialogCellEditor {

    private Composite composite;
    private Text pathText;
    private final boolean doFolder;
    private final String[] extentions;

    /**
     * Creates a new file cell editor parented under the given control.
     *
     * @param parent the parent control.
     * @param doFolder open a folder browser instead of file browser.
     * @param extentions the extentions that the dialog should consider.
     */
    public FilepathCellEditor( Composite parent, boolean doFolder, String[] extentions ) {
        super(parent, SWT.NONE);
        this.doFolder = doFolder;
        this.extentions = extentions;
    }

    protected Control createContents( Composite cell ) {
        // composite = new Composite(cell, getStyle());
        // composite.setLayout(new GridLayout(2, false));

        pathText = new Text(cell, SWT.SINGLE | SWT.LEFT);
        pathText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        pathText.setText(""); //$NON-NLS-1$
        pathText.setEditable(true);
        pathText.setFont(cell.getFont());
        pathText.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
        // pathText.setBackground(cell.getBackground());
        // Button browseButton = new Button(composite, SWT.PUSH);
        // browseButton.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
        //            browseButton.setText("..."); //$NON-NLS-1$

        return pathText;
    }

    protected Object openDialogBox( Control cellEditorWindow ) {
        Shell shell = cellEditorWindow.getShell();
        if (doFolder) {
            DirectoryDialog folderDialog = new DirectoryDialog(shell, SWT.OPEN);
            String path = folderDialog.open();
            if (path != null && path.length() >= 1) {
                return path;
            }
        } else {
            FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
            fileDialog.setFilterExtensions(extentions);
            String path = fileDialog.open();
            if (path != null && path.length() >= 1) {
                return path;
            }
        }
        return null;
    }

    protected void updateContents( Object value ) {
        if (value == null) {
            return;
        }
        String text = "";//$NON-NLS-1$
        if (value != null) {
            text = value.toString();
        }
        pathText.setText(text);
    }
}
