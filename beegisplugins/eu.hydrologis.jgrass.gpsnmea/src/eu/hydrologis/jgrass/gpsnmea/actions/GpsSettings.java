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
package eu.hydrologis.jgrass.gpsnmea.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import eu.hydrologis.jgrass.gpsnmea.GpsActivator;

/**
 * Action to open the gps settings dialog.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class GpsSettings extends Action implements IWorkbenchWindowActionDelegate {

    public static final String ID = "open.settings.id"; //$NON-NLS-1$
    private IWorkbenchWindow window;

    public GpsSettings() {
        setId(ID);
    }

    public void dispose() {
    }

    public void init( IWorkbenchWindow window ) {
        this.window = window;
        // add action to the registry
        GpsActivator.getDefault().registerAction(this);

    }

    public void run( IAction action ) {
        Shell shell = new Shell(window.getShell(), SWT.SHELL_TRIM | SWT.PRIMARY_MODAL);
        shell.setLayout(new GridLayout(1, false));
        shell.setSize(500, 500);
        Point cursorLocation = window.getShell().getDisplay().getCursorLocation();
        shell.setLocation(cursorLocation);

        new GpsSettingsComposite(shell);

        shell.open();

    }

    public void selectionChanged( IAction action, ISelection selection ) {
    }

}
