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

import java.text.MessageFormat;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import eu.hydrologis.jgrass.beegisutils.BeegisUtilsPlugin;

/**
 * A cell editor that manages file path and the button to browse for it.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class FormComboCellEditor extends CellEditor {

    /**
     * The text control; initially <code>null</code>.
     */
    protected Text manualText;

    private Button mainButton;

    private Button manualButton;

    private Button fileButton;

    private Text pathText;

    private Button browseButton;

    private Text separatorText;

    private Button linkButton;

    private String valueString;
    private Text attributeValueText;
    private Text guiNameText;

    /**
     * Creates a new file cell editor parented under the given control.
     *
     * @param parent the parent control.
     */
    public FormComboCellEditor( Composite parent ) {
        super(parent, SWT.NONE);
    }

    /* (non-Javadoc)
     * Method declared on CellEditor.
     */
    protected Control createControl( Composite parent ) {

        mainButton = new Button(parent, SWT.PUSH);
        mainButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        mainButton.setText("...");
        mainButton.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                Display.getDefault().syncExec(new Runnable(){
                    public void run() {
                        handleEvent();
                    }
                });
            }
        });

        return mainButton;
    }

    private void handleEvent() {

        Shell shell = new Shell(Display.getDefault(), SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL);
        Dialog dialog = new Dialog(shell){

            @Override
            protected void configureShell( Shell shell ) {
                super.configureShell(shell);
                shell.setText("Insert data");
            }

            @Override
            protected Point getInitialSize() {
                return new Point(320, 300);
            }

            @Override
            protected Control createDialogArea( Composite parent ) {

                Composite mainComposite = new Composite(parent, SWT.NONE);
                mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
                mainComposite.setLayout(new GridLayout(2, false));

                manualButton = new Button(mainComposite, SWT.RADIO);
                manualButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
                manualButton.setText("Manual");
                manualButton.addSelectionListener(new SelectionAdapter(){
                    public void widgetSelected( SelectionEvent e ) {
                        boolean selection = manualButton.getSelection();
                        handleEnablements(selection);
                    }

                });

                manualText = new Text(mainComposite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
                manualText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
                manualText.setText("");

                fileButton = new Button(mainComposite, SWT.RADIO);
                GridData fileButtonGD = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
                fileButtonGD.horizontalSpan = 2;
                fileButton.setLayoutData(fileButtonGD);
                fileButton.setText("File");
                fileButton.addSelectionListener(new SelectionAdapter(){
                    public void widgetSelected( SelectionEvent e ) {
                        boolean selection = fileButton.getSelection();
                        handleEnablements(!selection);
                    }
                });

                Group fileGroup = new Group(mainComposite, SWT.NONE);
                GridData fileGroupGD = new GridData(SWT.FILL, SWT.FILL, true, false);
                fileGroupGD.horizontalSpan = 2;
                fileGroup.setLayoutData(fileGroupGD);
                fileGroup.setLayout(new GridLayout(3, false));
                // fileGroup.setText();

                Label pathLabel = new Label(fileGroup, SWT.NONE);
                pathLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
                pathLabel.setText("Path");

                pathText = new Text(fileGroup, SWT.SINGLE | SWT.LEAD | SWT.BORDER | SWT.READ_ONLY);
                pathText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
                pathText.setText("");

                browseButton = new Button(fileGroup, SWT.PUSH);
                browseButton.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
                browseButton.setText("..."); //$NON-NLS-1$
                browseButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter(){
                    public void widgetSelected( org.eclipse.swt.events.SelectionEvent e ) {
                        String lastFolderChosen = BeegisUtilsPlugin.getDefault().getLastFolderChosen();
                        FileDialog fileDialog = new FileDialog(pathText.getShell(), SWT.OPEN);
                        // fileDialog.setFilterExtensions(extentions);
                        fileDialog.setFilterPath(lastFolderChosen);
                        String path = fileDialog.open();
                        if (path != null && path.length() >= 1) {
                            BeegisUtilsPlugin.getDefault().setLastFolderChosen(path);
                            pathText.setText(path);
                        }
                    }
                });

                Label separatorBabel = new Label(fileGroup, SWT.NONE);
                separatorBabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
                separatorBabel.setText("Separator");

                separatorText = new Text(fileGroup, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
                GridData separatorTextGD = new GridData(SWT.FILL, SWT.CENTER, true, false);
                separatorTextGD.horizontalSpan = 2;
                separatorText.setLayoutData(separatorTextGD);
                separatorText.setText(",");

                linkButton = new Button(fileGroup, SWT.CHECK);
                GridData linkButtonGD = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
                linkButtonGD.horizontalSpan = 3;
                linkButton.setLayoutData(linkButtonGD);
                linkButton.setText("link file");

                Label attributeValueLabel = new Label(fileGroup, SWT.NONE);
                attributeValueLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
                attributeValueLabel.setText("Attribute value column");

                attributeValueText = new Text(fileGroup, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
                GridData attributeValueGD = new GridData(SWT.FILL, SWT.CENTER, true, false);
                attributeValueGD.horizontalSpan = 2;
                attributeValueText.setLayoutData(attributeValueGD);
                attributeValueText.setText("1");

                Label guiNameLabel = new Label(fileGroup, SWT.NONE);
                guiNameLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
                guiNameLabel.setText("Interface name column");

                guiNameText = new Text(fileGroup, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
                GridData guiNameTextGD = new GridData(SWT.FILL, SWT.CENTER, true, false);
                guiNameTextGD.horizontalSpan = 2;
                guiNameText.setLayoutData(guiNameTextGD);
                guiNameText.setText("2");

                setValuesFromValueString();

                return mainComposite;
            }

            @Override
            protected void buttonPressed( int buttonId ) {
                if (buttonId == OK) {
                    if (manualButton.getSelection()) {
                        valueString = manualText.getText();
                    } else {
                        setValueStringFromFileType();
                    }
                    editOccured(null);
                }
                super.buttonPressed(buttonId);
            }

        };
        dialog.setBlockOnOpen(true);
        dialog.open();

        // Point location = mainButton.getLocation();
        //
        // Point size = dialog.getSize();
        // dialog.setLocation(location.x - size.x, location.y + size.y);
    }

    private void handleEnablements( boolean selection ) {
        manualText.setEnabled(selection);
        pathText.setEnabled(!selection);
        browseButton.setEnabled(!selection);
        separatorText.setEnabled(!selection);
        linkButton.setEnabled(!selection);
        attributeValueText.setEnabled(!selection);
        guiNameText.setEnabled(!selection);
    }

    private void setValueStringFromFileType() {
        String path = pathText.getText();
        String attributeValueString = attributeValueText.getText();
        String guiNameString = guiNameText.getText();
        try {
            Integer.parseInt(attributeValueString);
            Integer.parseInt(guiNameString);
        } catch (Exception e) {
            attributeValueString = "0";
            guiNameString = "1";
        }
        String sep = separatorText.getText();
        String link = linkButton.getSelection() ? "y" : "n";

        valueString = "file:" + path + ";" + guiNameString + ";" + attributeValueString + ";" + sep + ";" + link;
    }

    /**
     * Sets the gui values from the data string.
     * 
     * <p>
     * The valuestring is of type: <b>file:/path/to/file;guiNameColumn;attributeValueColumn;separator;link</b>
     * </p>
     *  
     */
    private void setValuesFromValueString() {
        boolean link = false;
        if (valueString != null && valueString.length() > 0) {
            valueString = valueString.trim();
            if (valueString.startsWith("file")) {
                String[] split = valueString.split(";");
                String pathString = split[0];
                String guiNameString = split[1];
                String attributeValueString = split[2];
                String sepString = split[3];
                link = split[4].equals("y") ? true : false;

                pathText.setText(pathString.replaceFirst("file:", ""));
                attributeValueText.setText(attributeValueString);
                guiNameText.setText(guiNameString);
                separatorText.setText(sepString);
                linkButton.setSelection(link);

                manualButton.setSelection(false);
                fileButton.setSelection(true);
                handleEnablements(false);
            } else {
                manualText.setText(valueString);
                manualButton.setSelection(true);
                fileButton.setSelection(false);
                handleEnablements(true);
            }
        }
    }

    /**
     * The <code>TextCellEditor</code> implementation of
     * this <code>CellEditor</code> framework method returns
     * the text string.
     *
     * @return the text string
     */
    protected Object doGetValue() {
        return valueString;
    }

    /* (non-Javadoc)
     * Method declared on CellEditor.
     */
    protected void doSetFocus() {
        // if (manualText != null) {
        // manualText.selectAll();
        // manualText.setFocus();
        // checkSelection();
        // checkDeleteable();
        // checkSelectable();
        // }
    }

    /**
     * The <code>TextCellEditor</code> implementation of
     * this <code>CellEditor</code> framework method accepts
     * a text string (type <code>String</code>).
     *
     * @param value a text string (type <code>String</code>)
     */
    protected void doSetValue( Object value ) {
        if (value instanceof String) {
            valueString = (String) value;
        }
    }

    /**
     * Processes a modify event that occurred in this text cell editor.
     * This framework method performs validation and sets the error message
     * accordingly, and then reports a change via <code>fireEditorValueChanged</code>.
     * Subclasses should call this method at appropriate times. Subclasses
     * may extend or reimplement.
     *
     * @param e the SWT modify event
     */
    protected void editOccured( ModifyEvent e ) {
        String value = valueString;
        if (value == null) {
            value = "";//$NON-NLS-1$
        }
        Object typedValue = value;
        boolean oldValidState = isValueValid();
        boolean newValidState = isCorrect(typedValue);
        if (!newValidState) {
            // try to insert the current value into the error message.
            setErrorMessage(MessageFormat.format(getErrorMessage(), new Object[]{value}));
        }
        valueChanged(oldValidState, newValidState);
    }

    /**
     * Since a text editor field is scrollable we don't
     * set a minimumSize.
     */
    public LayoutData getLayoutData() {
        LayoutData data = new LayoutData();
        data.minimumWidth = 0;
        return data;
    }

    /**
     * This implementation of
     * {@link CellEditor#dependsOnExternalFocusListener()} returns false if the
     * current instance's class is TextCellEditor, and true otherwise.
     * Subclasses that hook their own focus listener should override this method
     * and return false. See also bug 58777.
     * 
     * @since 3.4
     */
    protected boolean dependsOnExternalFocusListener() {
        return getClass() != FormComboCellEditor.class;
    }
}
