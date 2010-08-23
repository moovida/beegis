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
package eu.hydrologis.jgrass.geonotes.tools;

import net.refractions.udig.project.ui.tool.AbstractActionTool;
import net.refractions.udig.project.ui.tool.ActionTool;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import eu.hydrologis.jgrass.geonotes.fieldbook.FieldbookView;

/**
 * Action to open the fieldbook.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class OpenFieldBookActionTool extends AbstractActionTool implements ActionTool {

    public static final String ID = "eu.hydrologis.jgrass.geonotes.openfieldbookaction"; //$NON-NLS-1$

    public OpenFieldBookActionTool() {
    }

    public void run() {
        Display.getDefault().asyncExec(new Runnable(){
            public void run() {
                try {
                    IWorkbenchPage activePage = PlatformUI.getWorkbench()
                            .getActiveWorkbenchWindow().getActivePage();
                    activePage.showView(FieldbookView.ID);
                } catch (PartInitException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void dispose() {
    }

}
