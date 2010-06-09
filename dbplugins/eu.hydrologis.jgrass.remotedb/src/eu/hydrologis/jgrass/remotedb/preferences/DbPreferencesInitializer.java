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
package eu.hydrologis.jgrass.remotedb.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import eu.hydrologis.jgrass.remotedb.RemoteDbPlugin;

/**
 * @author Andrea Antonello - www.hydrologis.com
 */
public class DbPreferencesInitializer extends AbstractPreferenceInitializer {

    public void initializeDefaultPreferences() {

        int port = 9092;
        String user = "sa"; //$NON-NLS-1$
        String passwd = ""; //$NON-NLS-1$
        ScopedPreferenceStore preferences = (ScopedPreferenceStore) RemoteDbPlugin.getDefault()
                .getPreferenceStore();
        preferences.setDefault(DbParams.DATABASETYPE, RemoteDbPlugin.POSTGRESQL);
        preferences.setDefault(DbParams.PORT, port);
        preferences.setDefault(DbParams.USER, user);
        preferences.setDefault(DbParams.PASSWD, passwd);
        preferences.setDefault(DbParams.HOST, "");
        preferences.setDefault(DbParams.NAME, "");

    }
}
