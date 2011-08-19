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
package eu.hydrologis.jgrass.gpsnmea.animation;

import java.awt.Color;
import java.awt.Rectangle;

import net.refractions.udig.project.ui.IAnimation;
import net.refractions.udig.project.ui.commands.AbstractDrawCommand;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * The animation that shows the current gps location in the map
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class GpsPositionAnimation extends AbstractDrawCommand implements IAnimation {

    private int frame = 10;
    private int counter = 0;
    private final int y;
    private final int x;
    public GpsPositionAnimation( int x, int y ) {
        this.x = x;
        this.y = y;
        frame = 0;
    }
    public short getFrameInterval() {
        return 100;
    }

    public void nextFrame() {
        if (counter >= 6) {
            frame--;
        } else {
            frame++;
        }
        counter++;
    }

    public boolean hasNext() {
        return counter < 12;
    }

    public void run( IProgressMonitor monitor ) throws Exception {
        graphics.setColor(Color.red);
        int rad = (frame * 2);

        graphics.setLineWidth(1);
        graphics.drawLine(x, y - rad, x, y + rad);
        graphics.drawLine(x - rad, y, x + rad, y);

        graphics.drawRect(x - rad / 2, y - rad / 2, rad, rad);
    }

    public Rectangle getValidArea() {
        int rad = (frame * 2);
        return new Rectangle(x - rad, y - rad, rad * 2, rad * 2);
    }

}
