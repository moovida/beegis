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
package eu.hydrologis.jgrass.formeditor.utils;

import java.util.HashMap;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;

/**
 * A singleton cache for colors.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class ColorCache {

    private static ColorCache imageCache;

    private HashMap<String, Color> colorsMap = new HashMap<String, Color>();

    private int colorIndex = 0;

    private Color[] colorsArray = { //
    ColorConstants.black, //
            ColorConstants.red, //
            ColorConstants.blue, //
            ColorConstants.green, //
            ColorConstants.yellow, //
            ColorConstants.orange, //
            ColorConstants.cyan, //
            ColorConstants.darkBlue, //
            ColorConstants.darkGray, //
            ColorConstants.darkGreen, //
            ColorConstants.lightBlue, //
            ColorConstants.lightGreen, //
            ColorConstants.lightGray //
    };

    private ColorCache() {
    }

    public static ColorCache getInstance() {
        if (imageCache == null) {
            imageCache = new ColorCache();
        }
        return imageCache;
    }

    /**
     * Get an image for a certain key.
     * 
     * @param key any string, that will then be associated with a color.
     * @return the {@link Color}.
     */
    public Color getColor( String key ) {
        Color color = colorsMap.get(key);
        if (color == null) {
            color = getNextColor();
            colorsMap.put(key, color);
        }
        return color;
    }

    private Color getNextColor() {
        if (colorIndex < colorsArray.length) {
            return colorsArray[colorIndex++];
        } else {
            colorIndex = 0;
            return colorsArray[colorIndex++];
        }
    }

    public void reset() {
        colorIndex = 0;
    }

}
