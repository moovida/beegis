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
package eu.hydrologis.jgrass.geonotes.preferences.pages;

import java.io.IOException;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import eu.hydrologis.jgrass.geonotes.GeonotesPlugin;

/**
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class GeonotesPreferencePage extends FieldEditorPreferencePage
        implements
            IWorkbenchPreferencePage {

    private ScopedPreferenceStore preferences = null;

    public GeonotesPreferencePage() {
        super(GRID);
        preferences = (ScopedPreferenceStore) GeonotesPlugin.getDefault().getPreferenceStore();
        setPreferenceStore(preferences);
        setDescription("Geonotes preferences page");
    }

    public boolean performOk() {
        try {
            preferences.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return super.performOk();
    }

    public void createFieldEditors() {
        addField(new StringFieldEditor(PreferenceConstants.SMTP_SERVER,
                PreferenceConstants.SMTP_SERVER, getFieldEditorParent()));
        addField(new StringFieldEditor(PreferenceConstants.SMTP_PORT,
                PreferenceConstants.SMTP_PORT, getFieldEditorParent()));
        addField(new StringFieldEditor(PreferenceConstants.SMTP_USERNAME,
                PreferenceConstants.SMTP_USERNAME, getFieldEditorParent()));
        StringFieldEditor pasField = new StringFieldEditor(PreferenceConstants.SMTP_PASSWORD,
                PreferenceConstants.SMTP_PASSWORD, getFieldEditorParent());
        pasField.getTextControl(getFieldEditorParent()).setEchoChar('*');
        addField(pasField);
        addField(new StringFieldEditor(PreferenceConstants.GEONOTES_DESTINATION_ADDRESS,
                PreferenceConstants.GEONOTES_DESTINATION_ADDRESS, getFieldEditorParent()));
        addField(new BooleanFieldEditor(PreferenceConstants.SMTP_AUTH,
                PreferenceConstants.SMTP_AUTH, getFieldEditorParent()));
    }

    public void init( IWorkbench workbench ) {
    }

}
