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
package eu.hydrologis.jgrass.gpsnmea.preferences.runtime;

import java.awt.Color;

/**
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class Preferences {
    public static int intervalSeconds = 3;
    public static float minDistance = 1;

    public static int pointSize = 6;
    public static Color outlineColor = Color.red;
    public static Color fillColor = new Color((float) outlineColor.getRed() / 255f,
            (float) outlineColor.getGreen() / 255f, (float) outlineColor.getBlue() / 255f,
            128f / 255f);
    public static boolean testWithDummyData = true;

    public static String[][] dummyCoords = new String[][]{
            {"01121.12", "4596.12", "1.0", "4", "1.5"}, {"01115.12", "4591.12", "1.0", "4", "1.5"},
            {"01109.12", "4588.12", "1.0", "4", "1.5"}, {"01109.12", "4621.12", "1.0", "4", "1.5"},
            {"01115.12", "4612.12", "1.0", "4", "1.5"}, {"01119.12", "4607.12", "1.0", "4", "1.5"},
            {"01079.12", "4609.12", "1.0", "4", "1.5"}, {"01095.12", "4624.12", "1.0", "4", "1.5"},
            {"01086.12", "4597.12", "1.0", "4", "1.5"}};
    public static int dummyIndex = 0;
}