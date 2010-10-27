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

import java.io.IOException;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import eu.hydrologis.jgrass.gpsnmea.GpsActivator;

/**
 * Preferences page for gps advances settings.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class AdvancedGpsPreferencePage extends FieldEditorPreferencePage
        implements IWorkbenchPreferencePage {

    private ScopedPreferenceStore preferences;

    public AdvancedGpsPreferencePage() {
        super(GRID);
        preferences = (ScopedPreferenceStore) GpsActivator.getDefault()
                .getPreferenceStore();
        setPreferenceStore(preferences);
        setDescription("Advanced GPS Connection Settings");
    }

    protected void createFieldEditors() {
        addField(new StringFieldEditor(PreferenceConstants.MAXWAIT,
                PreferenceConstants.MAXWAIT + ":", getFieldEditorParent()));
        addField(new StringFieldEditor(PreferenceConstants.BAUDRATE,
                PreferenceConstants.BAUDRATE + ":", getFieldEditorParent()));
        addField(new StringFieldEditor(PreferenceConstants.DATABIT,
                PreferenceConstants.DATABIT + ":", getFieldEditorParent()));
        addField(new StringFieldEditor(PreferenceConstants.STOPBIT,
                PreferenceConstants.STOPBIT + ":", getFieldEditorParent()));
        addField(new StringFieldEditor(PreferenceConstants.PARITYBIT,
                PreferenceConstants.PARITYBIT + ":", getFieldEditorParent()));
    }

    public boolean performOk() {
        try {
            preferences.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return super.performOk();
    }

    public void init(IWorkbench workbench) {
    }

}
