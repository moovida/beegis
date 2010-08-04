/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Library General Public License
 * along with this library; if not, write to the Free Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package eu.hydrologis.jgrass.gpsnmea.animation;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;

import net.refractions.udig.project.ui.commands.AbstractDrawCommand;
import net.refractions.udig.project.ui.commands.IDrawCommand;

import org.eclipse.core.runtime.IProgressMonitor;

import eu.hydrologis.jgrass.gpsnmea.gps.GpsProperties;

/**
 * The draw command for visualizing the gps position on the screen.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class GpsPositionDrawCommand extends AbstractDrawCommand implements IDrawCommand {

    private Rectangle validArea;
    private final Point point;
    private int frame = 10;
    private final double angle;
    private final boolean active;
    private final Point ll;
    private final Point ur;

    /**
     * @param p the point to draw.
     * @param ur the upper right corner of the viewport.
     * @param ll the lower left corner of the viewport.
     * @param angle the direction for vector arrow if needed.
     * @param active flag to be able to draw in two modes (normal and active).
     */
    public GpsPositionDrawCommand( Point p, Point ll, Point ur, double angle, boolean active ) {
        this.point = p;
        this.ll = ll;
        this.ur = ur;
        this.angle = angle;
        this.active = active;
    }

    public void run( IProgressMonitor monitor ) throws Exception {
        validArea = new Rectangle(point.x, point.y + frame, frame, frame);

        /*
         * crosshair
         */
        if (GpsProperties.doCrosshair) {
            graphics.setColor(GpsProperties.crosshairColor);
            graphics.setLineWidth(GpsProperties.crosshairWidth);
            graphics.drawLine(point.x, ur.y, point.x, ll.y);
            graphics.drawLine(ll.x, point.y, ur.x, point.y);
        }

        /*
         * gps symbol
         */
        if (active) {
            graphics.setColor(GpsProperties.gpsActiveColor);
            // graphics.setLineWidth(GpsProperties.gpsSymbolWidth);
            // graphics.drawLine(point.x, point.y - frame, point.x, point.y + frame);
            // graphics.drawLine(point.x - frame, point.y, point.x + frame, point.y);
            // graphics.drawRect(point.x - frame / 2, point.y - frame / 2, frame, frame);
            // GeneralPath p = new GeneralPath();
            // p.moveTo(point.x, point.y - 2 * frame);
            // p.lineTo(point.x, point.y + 2 * frame);
            // p.lineTo(point.x - 2, point.y + 2 * frame - 4);
            // p.lineTo(point.x + 2, point.y + 2 * frame - 4);
            // p.lineTo(point.x, point.y + 2 * frame);
            // AffineTransform rotTransform = AffineTransform.getRotateInstance(angle * Math.PI
            // / 180.0 + Math.PI, point.x, point.y);
            // p.transform(rotTransform);
            // graphics.setColor(Color.blue);
            // graphics.draw(p);
        } else {
            graphics.setColor(GpsProperties.gpsActiveColor);
        }
        graphics.setLineWidth(GpsProperties.gpsSymbolWidth);
        graphics.drawLine(point.x, point.y - frame, point.x, point.y + frame);
        graphics.drawLine(point.x - frame, point.y, point.x + frame, point.y);
        graphics.drawRect(point.x - frame / 2, point.y - frame / 2, frame, frame);

        // DON'T DRAW ARROW FOR NOW
        // GeneralPath p = new GeneralPath();
        // p.moveTo(point.x, point.y - 2 * frame);
        // p.lineTo(point.x, point.y + 2 * frame);
        // p.lineTo(point.x - 2, point.y + 2 * frame - 4);
        // p.lineTo(point.x + 2, point.y + 2 * frame - 4);
        // p.lineTo(point.x, point.y + 2 * frame);
        // AffineTransform rotTransform = AffineTransform.getRotateInstance(angle * Math.PI / 180.0
        // + Math.PI, point.x, point.y);
        // p.transform(rotTransform);
        // graphics.setColor(Color.blue);
        // graphics.draw(p);
    }

    public Rectangle getValidArea() {
        return validArea;
    }

    public void setValid( boolean valid ) {
        super.setValid(valid);
    }
}
