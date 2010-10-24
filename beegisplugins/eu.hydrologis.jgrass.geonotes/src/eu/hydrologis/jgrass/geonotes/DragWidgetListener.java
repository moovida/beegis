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
package eu.hydrologis.jgrass.geonotes;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * Listener for widget dragging.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class DragWidgetListener implements Listener {
    private Point origin;
    private final Composite shell;

    public DragWidgetListener( Composite shell ) {
        this.shell = shell;
    }

    public void handleEvent( Event e ) {
        switch( e.type ) {
        case SWT.MouseDown:
            origin = new Point(e.x, e.y);
            break;
        case SWT.MouseUp:
            origin = null;
            break;
        case SWT.MouseMove:
            if (origin != null) {
                Point p = shell.getDisplay().map(shell, null, e.x, e.y);
                shell.setLocation(p.x - origin.x, p.y - origin.y);
            }
            break;
        }
    }
}
