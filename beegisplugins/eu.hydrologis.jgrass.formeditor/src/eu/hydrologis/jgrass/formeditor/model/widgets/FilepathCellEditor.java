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

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.CellEditor.LayoutData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * A cell editor that manages file path and the button to browse for it.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class FilepathCellEditor extends CellEditor {

    /**
     * The text control; initially <code>null</code>.
     */
    protected Text text;

    private ModifyListener modifyListener;

    /**
     * State information for updating action enablement
     */
    private boolean isSelection = false;

    private boolean isDeleteable = false;

    private boolean isSelectable = false;

    private boolean doFolder;

    private String[] extentions;

    /**
     * Default TextCellEditor style
     * specify no borders on text widget as cell outline in table already
     * provides the look of a border.
     */
    private static final int defaultStyle = SWT.SINGLE;

    private FocusAdapter textFocusAdapter;

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

    /**
     * Checks to see if the "deletable" state (can delete/
     * nothing to delete) has changed and if so fire an
     * enablement changed notification.
     */
    private void checkDeleteable() {
        boolean oldIsDeleteable = isDeleteable;
        isDeleteable = isDeleteEnabled();
        if (oldIsDeleteable != isDeleteable) {
            fireEnablementChanged(DELETE);
        }
    }

    /**
     * Checks to see if the "selectable" state (can select)
     * has changed and if so fire an enablement changed notification.
     */
    private void checkSelectable() {
        boolean oldIsSelectable = isSelectable;
        isSelectable = isSelectAllEnabled();
        if (oldIsSelectable != isSelectable) {
            fireEnablementChanged(SELECT_ALL);
        }
    }

    /**
     * Checks to see if the selection state (selection /
     * no selection) has changed and if so fire an
     * enablement changed notification.
     */
    private void checkSelection() {
        boolean oldIsSelection = isSelection;
        isSelection = text.getSelectionCount() > 0;
        if (oldIsSelection != isSelection) {
            fireEnablementChanged(COPY);
            fireEnablementChanged(CUT);
        }
    }

    /* (non-Javadoc)
     * Method declared on CellEditor.
     */
    protected Control createControl( Composite parent ) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        composite.setBackground(parent.getBackground());
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        text = new Text(composite, SWT.LEFT);
        text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        text.addSelectionListener(new SelectionAdapter(){
            public void widgetDefaultSelected( SelectionEvent e ) {
                handleDefaultSelection(e);
            }
        });
        text.addKeyListener(new KeyAdapter(){
            // hook key pressed - see PR 14201
            public void keyPressed( KeyEvent e ) {
                keyReleaseOccured(e);

                // as a result of processing the above call, clients may have
                // disposed this cell editor
                if ((getControl() == null) || getControl().isDisposed()) {
                    return;
                }
                checkSelection(); // see explanation below
                checkDeleteable();
                checkSelectable();
            }
        });
        text.addTraverseListener(new TraverseListener(){
            public void keyTraversed( TraverseEvent e ) {
                if (e.detail == SWT.TRAVERSE_ESCAPE || e.detail == SWT.TRAVERSE_RETURN) {
                    e.doit = false;
                }
            }
        });
        // We really want a selection listener but it is not supported so we
        // use a key listener and a mouse listener to know when selection changes
        // may have occurred
        text.addMouseListener(new MouseAdapter(){
            public void mouseUp( MouseEvent e ) {
                checkSelection();
                checkDeleteable();
                checkSelectable();
            }
        });
        textFocusAdapter = new FocusAdapter(){
            public void focusLost( FocusEvent e ) {
                FilepathCellEditor.this.focusLost();
            }
        };
        text.addFocusListener(textFocusAdapter);
        text.setFont(parent.getFont());
        text.setBackground(parent.getBackground());
        text.setText("");//$NON-NLS-1$
        text.addModifyListener(getModifyListener());

        Button browseButton = new Button(composite, SWT.PUSH);
        browseButton.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
        browseButton.setText("..."); //$NON-NLS-1$
        browseButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter(){
            public void widgetSelected( org.eclipse.swt.events.SelectionEvent e ) {
                if (doFolder) {
                    DirectoryDialog folderDialog = new DirectoryDialog(text.getShell(), SWT.OPEN);
                    String path = folderDialog.open();
                    if (path != null && path.length() >= 1) {
                        text.removeModifyListener(getModifyListener());
                        text.setText(path);
                        text.addModifyListener(getModifyListener());
                    }
                    editOccured(null);
                } else {
                    FileDialog fileDialog = new FileDialog(text.getShell(), SWT.OPEN);
                    fileDialog.setFilterExtensions(extentions);
                    String path = fileDialog.open();
                    if (path != null && path.length() >= 1) {
                        text.removeModifyListener(getModifyListener());
                        text.setText(path);
                        text.addModifyListener(getModifyListener());
                    }
                    editOccured(null);
                }
            }
        });
        
        browseButton.addMouseTrackListener(new MouseTrackAdapter(){
            @Override
            public void mouseEnter( MouseEvent e ) {
                super.mouseEnter(e);
                text.removeModifyListener(getModifyListener());
                text.removeFocusListener(textFocusAdapter);
            }
            
            @Override
            public void mouseExit( MouseEvent e ) {
                super.mouseExit(e);
                text.addModifyListener(getModifyListener());
                text.addFocusListener(textFocusAdapter);
            }
        });

        return composite;
    }

    /**
     * The <code>TextCellEditor</code> implementation of
     * this <code>CellEditor</code> framework method returns
     * the text string.
     *
     * @return the text string
     */
    protected Object doGetValue() {
        return text.getText();
    }

    /* (non-Javadoc)
     * Method declared on CellEditor.
     */
    protected void doSetFocus() {
        if (text != null) {
            text.selectAll();
            text.setFocus();
            checkSelection();
            checkDeleteable();
            checkSelectable();
        }
    }

    /**
     * The <code>TextCellEditor</code> implementation of
     * this <code>CellEditor</code> framework method accepts
     * a text string (type <code>String</code>).
     *
     * @param value a text string (type <code>String</code>)
     */
    protected void doSetValue( Object value ) {
        Assert.isTrue(text != null && (value instanceof String));
        text.removeModifyListener(getModifyListener());
        text.setText((String) value);
        text.addModifyListener(getModifyListener());
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
        String value = text.getText();
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
     * Return the modify listener.
     */
    private ModifyListener getModifyListener() {
        if (modifyListener == null) {
            modifyListener = new ModifyListener(){
                public void modifyText( ModifyEvent e ) {
                    editOccured(e);
                }
            };
        }
        return modifyListener;
    }

    /**
     * Handles a default selection event from the text control by applying the editor
     * value and deactivating this cell editor.
     * 
     * @param event the selection event
     * 
     * @since 3.0
     */
    protected void handleDefaultSelection( SelectionEvent event ) {
        // same with enter-key handling code in keyReleaseOccured(e);
        fireApplyEditorValue();
        deactivate();
    }

    /**
     * The <code>TextCellEditor</code>  implementation of this 
     * <code>CellEditor</code> method returns <code>true</code> if 
     * the current selection is not empty.
     */
    public boolean isCopyEnabled() {
        if (text == null || text.isDisposed()) {
            return false;
        }
        return text.getSelectionCount() > 0;
    }

    /**
     * The <code>TextCellEditor</code>  implementation of this 
     * <code>CellEditor</code> method returns <code>true</code> if 
     * the current selection is not empty.
     */
    public boolean isCutEnabled() {
        if (text == null || text.isDisposed()) {
            return false;
        }
        return text.getSelectionCount() > 0;
    }

    /**
     * The <code>TextCellEditor</code>  implementation of this 
     * <code>CellEditor</code> method returns <code>true</code>
     * if there is a selection or if the caret is not positioned 
     * at the end of the text.
     */
    public boolean isDeleteEnabled() {
        if (text == null || text.isDisposed()) {
            return false;
        }
        return text.getSelectionCount() > 0 || text.getCaretPosition() < text.getCharCount();
    }

    /**
     * The <code>TextCellEditor</code>  implementation of this 
     * <code>CellEditor</code> method always returns <code>true</code>.
     */
    public boolean isPasteEnabled() {
        if (text == null || text.isDisposed()) {
            return false;
        }
        return true;
    }

    /**
     * Check if save all is enabled
     * @return true if it is 
     */
    public boolean isSaveAllEnabled() {
        if (text == null || text.isDisposed()) {
            return false;
        }
        return true;
    }

    /**
     * Returns <code>true</code> if this cell editor is
     * able to perform the select all action.
     * <p>
     * This default implementation always returns 
     * <code>false</code>.
     * </p>
     * <p>
     * Subclasses may override
     * </p>
     * @return <code>true</code> if select all is possible,
     *  <code>false</code> otherwise
     */
    public boolean isSelectAllEnabled() {
        if (text == null || text.isDisposed()) {
            return false;
        }
        return text.getCharCount() > 0;
    }

    /**
     * Processes a key release event that occurred in this cell editor.
     * <p>
     * The <code>TextCellEditor</code> implementation of this framework method 
     * ignores when the RETURN key is pressed since this is handled in 
     * <code>handleDefaultSelection</code>.
     * An exception is made for Ctrl+Enter for multi-line texts, since
     * a default selection event is not sent in this case. 
     * </p>
     *
     * @param keyEvent the key event
     */
    protected void keyReleaseOccured( KeyEvent keyEvent ) {
        if (keyEvent.character == '\r') { // Return key
            // Enter is handled in handleDefaultSelection.
            // Do not apply the editor value in response to an Enter key event
            // since this can be received from the IME when the intent is -not-
            // to apply the value.
            // See bug 39074 [CellEditors] [DBCS] canna input mode fires bogus event from Text
            // Control
            //
            // An exception is made for Ctrl+Enter for multi-line texts, since
            // a default selection event is not sent in this case.
            if (text != null && !text.isDisposed() && (text.getStyle() & SWT.MULTI) != 0) {
                if ((keyEvent.stateMask & SWT.CTRL) != 0) {
                    super.keyReleaseOccured(keyEvent);
                }
            }
            return;
        }
        super.keyReleaseOccured(keyEvent);
    }

    /**
     * The <code>TextCellEditor</code> implementation of this
     * <code>CellEditor</code> method copies the
     * current selection to the clipboard. 
     */
    public void performCopy() {
        text.copy();
    }

    /**
     * The <code>TextCellEditor</code> implementation of this
     * <code>CellEditor</code> method cuts the
     * current selection to the clipboard. 
     */
    public void performCut() {
        text.cut();
        checkSelection();
        checkDeleteable();
        checkSelectable();
    }

    /**
     * The <code>TextCellEditor</code> implementation of this
     * <code>CellEditor</code> method deletes the
     * current selection or, if there is no selection,
     * the character next character from the current position. 
     */
    public void performDelete() {
        if (text.getSelectionCount() > 0) {
            // remove the contents of the current selection
            text.insert(""); //$NON-NLS-1$
        } else {
            // remove the next character
            int pos = text.getCaretPosition();
            if (pos < text.getCharCount()) {
                text.setSelection(pos, pos + 1);
                text.insert(""); //$NON-NLS-1$
            }
        }
        checkSelection();
        checkDeleteable();
        checkSelectable();
    }

    /**
     * The <code>TextCellEditor</code> implementation of this
     * <code>CellEditor</code> method pastes the
     * the clipboard contents over the current selection. 
     */
    public void performPaste() {
        text.paste();
        checkSelection();
        checkDeleteable();
        checkSelectable();
    }

    /**
     * The <code>TextCellEditor</code> implementation of this
     * <code>CellEditor</code> method selects all of the
     * current text. 
     */
    public void performSelectAll() {
        text.selectAll();
        checkSelection();
        checkDeleteable();
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
        return getClass() != FilepathCellEditor.class;
    }
}
