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
package eu.hydrologis.jgrass.embeddeddb.preferences;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import net.refractions.udig.ui.ExceptionDetailsDialog;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import eu.hydrologis.jgrass.embeddeddb.EmbeddedDbPlugin;

/**
 * Backup and restore engine for embedded db.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public class BackupRestoreFieldEditor extends FieldEditor {

    private int columns = 3;

    public BackupRestoreFieldEditor( String preferenceName, Composite parent ) {
        init(preferenceName, ""); //$NON-NLS-1$
        createControl(parent);
    }

    @Override
    protected void adjustForNumColumns( int numColumns ) {
        this.columns = numColumns;
    }

    @Override
    protected void doFillIntoGrid( Composite parent, int numColumns ) {

        Group typeGroup = new Group(parent, SWT.NONE);
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
        gridData.horizontalSpan = columns;
        typeGroup.setLayoutData(gridData);
        typeGroup.setLayout(new GridLayout(2, true));
        typeGroup.setText("Backup the database to a compressed file or restore it");

        final Button backupButton = new Button(typeGroup, SWT.PUSH);
        backupButton
                .setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
        backupButton.setText("Backup to file");
        backupButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter(){
            public void widgetSelected( org.eclipse.swt.events.SelectionEvent e ) {
                FileDialog fileDialog = new FileDialog(backupButton.getShell(), SWT.SAVE);
                final String path = fileDialog.open();

                if (path == null || path.length() < 1) {
                    return;
                } else {
                    try {
                        IWorkbench wb = PlatformUI.getWorkbench();
                        IProgressService ps = wb.getProgressService();
                        ps.busyCursorWhile(new IRunnableWithProgress(){
                            public void run( IProgressMonitor pm ) {
                                pm.beginTask("Backing up database to " + path,
                                        IProgressMonitor.UNKNOWN);
                                try {
                                    EmbeddedDbPlugin.getDefault().backUpTo(path);
                                } catch (Exception e) {
                                    String message = "An error occurred while backing up database.";
                                    ExceptionDetailsDialog.openError(null, message, IStatus.ERROR,
                                            EmbeddedDbPlugin.PLUGIN_ID, e);
                                }
                                pm.done();
                            }
                        });
                    } catch (InvocationTargetException e1) {
                        e1.printStackTrace();
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }

                }
            }
        });

        final Button restoreButton = new Button(typeGroup, SWT.PUSH);
        restoreButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
                | GridData.GRAB_HORIZONTAL));
        restoreButton.setText("Restore from file");
        restoreButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter(){
            public void widgetSelected( org.eclipse.swt.events.SelectionEvent e ) {

                MessageBox msgBox = new MessageBox(restoreButton.getShell(), SWT.YES | SWT.NO
                        | SWT.ICON_WARNING);
                msgBox
                        .setMessage("To be able to restore the database from file, the destination database has to be empty, else the restore will not work.\nAre you sure you want to continue?");
                int answ = msgBox.open();
                if (answ == SWT.NO) {
                    System.out.println("ret");
                    return;
                }

                FileDialog fileDialog = new FileDialog(restoreButton.getShell(), SWT.OPEN);
                final String path = fileDialog.open();
                if (path == null || path.length() < 1) {
                    return;
                } else {
                    try {
                        IWorkbench wb = PlatformUI.getWorkbench();
                        IProgressService ps = wb.getProgressService();
                        ps.busyCursorWhile(new IRunnableWithProgress(){
                            public void run( IProgressMonitor pm ) {
                                pm.beginTask("Restore database from " + path,
                                        IProgressMonitor.UNKNOWN);
                                try {
                                    EmbeddedDbPlugin.getDefault().restoreFrom(path);
                                } catch (Exception e) {
                                    String message = "An error occurred while restoring database.";
                                    ExceptionDetailsDialog.openError(null, message, IStatus.ERROR,
                                            EmbeddedDbPlugin.PLUGIN_ID, e);
                                }
                                pm.done();
                            }
                        });
                    } catch (InvocationTargetException e1) {
                        e1.printStackTrace();
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

    }
    protected void doLoad() {
    }

    protected void doLoadDefault() {
    }

    protected void doStore() {
    }

    public int getNumberOfControls() {
        return 1;
    }

}
