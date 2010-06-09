/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 * (C) Universita' di Urbino
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
package eu.hydrologis.jgrass.gpsnmea.preferences.pages;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import eu.hydrologis.jgrass.gpsnmea.GpsActivator;

/**
 * Class used to initialize default preference values.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

    public void initializeDefaultPreferences() {
        GpsActivator gpsActivator = GpsActivator.getDefault();
        if (gpsActivator != null) {
            IPreferenceStore store = gpsActivator.getPreferenceStore();
            if (store != null) {
                store.setDefault(PreferenceConstants.MAXWAIT, "5000");
                store.setDefault(PreferenceConstants.BAUDRATE, "4800");
                store.setDefault(PreferenceConstants.DATABIT, "8");
                store.setDefault(PreferenceConstants.STOPBIT, "1");
                store.setDefault(PreferenceConstants.PARITYBIT, "0");
                store.setDefault(PreferenceConstants.TESTMODE, false);
                store.setDefault(PreferenceConstants.DISTANCE_THRESHOLD, 3.0);
                store.setDefault(PreferenceConstants.INTERVAL_SECONDS, 5);
            }
        }
    }

}
