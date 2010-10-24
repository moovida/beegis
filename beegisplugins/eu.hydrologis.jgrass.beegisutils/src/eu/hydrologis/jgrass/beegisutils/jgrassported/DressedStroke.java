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
package eu.hydrologis.jgrass.beegisutils.jgrassported;

import static java.lang.Math.abs;

import java.io.Serializable;

import org.eclipse.swt.graphics.Rectangle;

/**
 * A styled stroke.
 * 
 * <p>
 * This class represents a dressed stroke, i.e. coordinates in pixel
 * position of it's nodes, stroke width, color and whatever will be.
 * </p>
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class DressedStroke implements Serializable {
    private static final long serialVersionUID = 1L;

    private int[] nodes = {0, 0};
    private int[] scaledNodes = {0, 0};
    private int[] strokeWidth = {1};
    private int strokeAlpha = 100;
    private int[] rgb = {0, 0, 0};
    private int[] lineStyle = {1};
    private double scaleFactor = 1.0;

    private Rectangle scaledBounds;

    private Rectangle bounds;

    /**
     * Creates a {@link DressedStroke} based on the given nodes coords.
     * 
     * @param nodes the nodes coordinates in format x0, y0, x1, y1,...
     */
    public DressedStroke( int[] nodes ) {
        this.nodes = nodes;
        calculateBounds();
        scaleNodes();
        calculateScaledBounds();
    }

    public DressedStroke() {
    }

    /**
     * Applies a scale factor to the stroke.
     * 
     * <p>This updates the scaledNodes available via {@link #getScaledNodes()}.
     * 
     * @param scaleFactor the scale factor to apply.
     */
    public void applyScaleFactor( double scaleFactor ) {
        if (abs(this.scaleFactor - scaleFactor) > 0.0001) {
            this.scaleFactor = scaleFactor;
            scaleNodes();
            calculateScaledBounds();
        }
    }

    private void scaleNodes() {
        scaledNodes = new int[nodes.length];
        for( int i = 0; i < scaledNodes.length; i++ ) {
            scaledNodes[i] = (int) Math.round((double) nodes[i] * scaleFactor);
        }
    }

    private void calculateScaledBounds() {
        scaledBounds = new Rectangle(0, 0, 1, 1);
        for( int i = 0; i < scaledNodes.length; i = i + 2 ) {
            int x = scaledNodes[i];
            int y = scaledNodes[i + 1];
            scaledBounds.add(new Rectangle(x, y, 1, 1));
        }
    }

    private void calculateBounds() {
        bounds = new Rectangle(0, 0, 1, 1);
        for( int i = 0; i < nodes.length; i = i + 2 ) {
            int x = nodes[i];
            int y = nodes[i + 1];
            bounds.add(new Rectangle(x, y, 1, 1));
        }
    }

    /**
     * Getter for the nodes.
     * 
     * @return the array of scaled nodes in format x0, y0, x1, y1,...
     */
    public int[] getScaledNodes() {
        return scaledNodes;
    }

    /**
     * Getter for the stroke bounds.
     * 
     * @return the stroke's bounding box.
     */
    public Rectangle getBounds() {
        return bounds;
    }

    /**
     * Getter for the scaled stroke bounds.
     * 
     * @return the scaled stroke's bounding box.
     */
    public Rectangle getScaledBounds() {
        return scaledBounds;
    }

    /**
     * Setter for the nodes.
     * 
     * @param nodes the array of nodes.
     */
    public void setNodes( int[] nodes ) {
        this.nodes = nodes;
        calculateBounds();
    }

    /**
     * Getter for the nodes in the original scale.
     * 
     * @return the array of nodes in format x0, y0, x1, y1,... 
     */
    public int[] getNodes() {
        return nodes;
    }

    /**
     * Setter for the stroke width.
     * 
     * @param strokeWidth the stroke width.
     */
    public void setStrokeWidth( int[] strokeWidth ) {
        this.strokeWidth = strokeWidth;
    }

    /**
     * Getter for the stroke width.
     * 
     * @return the stroke width.
     */
    public int getStrokeWidth() {
        return strokeWidth[0];
    }

    /**
     * Getter for the scaled stroke width.
     * 
     * @return the scaled stroke width.
     */
    public int getScaledStrokeWidth() {
        return (int) ((double)strokeWidth[0] * scaleFactor);
    }

    /**
     * Getter for the stroke's alpha. 
     * 
     * @return the stroke's alpha.
     */
    public int getStrokeAlpha() {
        return strokeAlpha;
    }

    /**
     * Setter for the stroke's alpha.
     * 
     * @param strokeAlpha the stroke's alpha.
     */
    public void setStrokeAlpha( int strokeAlpha ) {
        this.strokeAlpha = strokeAlpha;
    }

    /**
     * Getter for the stroke's color.
     * 
     * @return the stroke's color.
     */
    public int[] getRgb() {
        return rgb;
    }

    /**
     * Setter for the stroke's color.
     * 
     * @param rgb the stroke's color.
     */
    public void setRgb( int[] rgb ) {
        this.rgb = rgb;
    }

    /**
     * Getter for the line style.
     * 
     * @return the line style.
     */
    public int[] getLineStyle() {
        return lineStyle;
    }

    /**
     * Setter for the line style.
     * 
     * @param lineStyle the line style.
     */
    public void setLineStyle( int[] lineStyle ) {
        this.lineStyle = lineStyle;
    }

}
