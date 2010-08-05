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
import static eu.hydrologis.jgrass.gpsnmea.preferences.pages.PreferenceConstants.*;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import eu.hydrologis.jgrass.gpsnmea.GpsActivator;
import eu.hydrologis.jgrass.gpsnmea.gps.GpsProperties;

/**
 * Preferences page for gps advances settings.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class GpsCorrectionPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private ScopedPreferenceStore preferences;
    private StringFieldEditor deltaXEditor;
    private StringFieldEditor deltaYEditor;

    public GpsCorrectionPreferencePage() {
        super(GRID);
        preferences = (ScopedPreferenceStore) GpsActivator.getDefault().getPreferenceStore();
        setPreferenceStore(preferences);
        setDescription("GPS Corrections Page (the dirty tweak page)");

        checkInitValues();
    }

    private void checkInitValues() {
        /*
         * check values
         */
        String dx = preferences.getString(DELTAX);
        String dy = preferences.getString(DELTAY);
        if (dx == null || dx.length() == 0) {
            preferences.setDefault(DELTAX, "0.0");
        }
        if (dy == null || dy.length() == 0) {
            preferences.setDefault(DELTAY, "0.0");
        }
    }

    protected void createFieldEditors() {

        deltaXEditor = new StringFieldEditor(DELTAX, DELTAX, getFieldEditorParent());
        addField(deltaXEditor);
        deltaYEditor = new StringFieldEditor(DELTAY, DELTAY, getFieldEditorParent());
        addField(deltaYEditor);
    }

    @Override
    protected void checkState() {
        super.checkState();
        if (!isValid()) {
            return;
        }

        String deltaXStr = deltaXEditor.getStringValue();
        try {
            Double.parseDouble(deltaXStr);
        } catch (NumberFormatException e) {
            setErrorMessage("The delta X has to be a decimal value.");
            setValid(false);
            return;
        }
        String deltaYStr = deltaYEditor.getStringValue();
        try {
            Double.parseDouble(deltaYStr);
        } catch (NumberFormatException e) {
            setErrorMessage("The delta Y has to be a decimal value.");
            setValid(false);
            return;
        }
        setErrorMessage(null);
        setValid(true);
    }

    @Override
    public void propertyChange( PropertyChangeEvent event ) {
        super.propertyChange(event);

        checkState();
    }

    public boolean performOk() {
        String deltaXStr = deltaXEditor.getStringValue();
        GpsProperties.deltaX = Double.parseDouble(deltaXStr);
        String deltaYStr = deltaYEditor.getStringValue();
        GpsProperties.deltaY = Double.parseDouble(deltaYStr);
        try {
            preferences.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return super.performOk();
    }

    public void init( IWorkbench workbench ) {
        checkInitValues();
    }

}
