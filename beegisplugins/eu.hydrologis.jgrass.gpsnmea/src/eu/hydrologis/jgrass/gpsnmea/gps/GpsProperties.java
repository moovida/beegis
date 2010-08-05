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
package eu.hydrologis.jgrass.gpsnmea.gps;

import static eu.hydrologis.jgrass.gpsnmea.preferences.pages.PreferenceConstants.ACTIVECOLOR;
import static eu.hydrologis.jgrass.gpsnmea.preferences.pages.PreferenceConstants.CROSSCOLOR;
import static eu.hydrologis.jgrass.gpsnmea.preferences.pages.PreferenceConstants.CROSSWIDTH;
import static eu.hydrologis.jgrass.gpsnmea.preferences.pages.PreferenceConstants.DOCROSSHAIR;
import static eu.hydrologis.jgrass.gpsnmea.preferences.pages.PreferenceConstants.NONACTIVECOLOR;
import static eu.hydrologis.jgrass.gpsnmea.preferences.pages.PreferenceConstants.WIDTH;

import java.awt.Color;

import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import eu.hydrologis.jgrass.gpsnmea.GpsActivator;
import eu.hydrologis.jgrass.gpsnmea.preferences.pages.PreferenceInitializer;

/**
 * The GPS preferences at runtime. 
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class GpsProperties {

    public static int gpsSymbolWidth = 1;

    public static Color gpsActiveColor = Color.red;

    public static Color gpsNonActiveColor = Color.magenta;

    public static boolean doCrosshair = false;

    public static int crosshairWidth = 1;

    public static Color crosshairColor = Color.gray;

    public static double deltaX = 0d;

    public static double deltaY = 0d;

    static {
        /*
         * make sure to have defaults
         */
        new PreferenceInitializer().initializeDefaultPreferences();

        ScopedPreferenceStore preferences = (ScopedPreferenceStore) GpsActivator.getDefault().getPreferenceStore();

        gpsSymbolWidth = preferences.getInt(WIDTH);
        RGB activeRgb = PreferenceConverter.getColor(preferences, ACTIVECOLOR);
        gpsActiveColor = new Color(activeRgb.red, activeRgb.green, activeRgb.blue);
        RGB nonActiveRgb = PreferenceConverter.getColor(preferences, NONACTIVECOLOR);
        gpsNonActiveColor = new Color(nonActiveRgb.red, nonActiveRgb.green, nonActiveRgb.blue);

        doCrosshair = preferences.getBoolean(DOCROSSHAIR);
        crosshairWidth = preferences.getInt(CROSSWIDTH);
        RGB crossRgb = PreferenceConverter.getColor(preferences, CROSSCOLOR);
        crosshairColor = new Color(crossRgb.red, crossRgb.green, crossRgb.blue);
    }
}
