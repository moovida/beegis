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

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import eu.hydrologis.jgrass.gpsnmea.GpsActivator;

/**
 * The Gps preference page.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class GpsPreferencePage extends FieldEditorPreferencePage
        implements
            IWorkbenchPreferencePage {

    private ScopedPreferenceStore preferences = null;

    public GpsPreferencePage() {
        super(GRID);
        preferences = (ScopedPreferenceStore) GpsActivator.getDefault().getPreferenceStore();
        setPreferenceStore(preferences);
        setDescription("GPS settings page");
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

        addField(new SerialPortsFieldEditor(PreferenceConstants.PORTUSED, getFieldEditorParent()));
        addField(new StartStopGpsFieldEditor(PreferenceConstants.GPS_IS_ON, getFieldEditorParent()));
        addField(new IntegerFieldEditor(PreferenceConstants.INTERVAL_SECONDS,
                PreferenceConstants.INTERVAL_SECONDS, getFieldEditorParent()));
        addField(new DoubleFieldEditor(PreferenceConstants.DISTANCE_THRESHOLD,
                PreferenceConstants.DISTANCE_THRESHOLD, getFieldEditorParent()));
        addField(new BooleanFieldEditor(PreferenceConstants.TESTMODE,
                "Use a dummy dataset for testing", 1, getFieldEditorParent()));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init( IWorkbench workbench ) {
    }

    static class DoubleFieldEditor extends FieldEditor {

        Label label;
        Text text;
        public DoubleFieldEditor( String name, String labelText, Composite parent ) {
            super(name, labelText, parent);
        }

        protected void adjustForNumColumns( int numColumns ) {

        }

        @Override
        protected void doFillIntoGrid( Composite comp, int numColumns ) {
            label = new Label(comp, SWT.NONE);
            label.setText(super.getLabelText());
            label.setLayoutData(new GridData(SWT.FILL, SWT.NONE, false, false));
            text = new Text(comp, SWT.SINGLE | SWT.BORDER);
            text.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
        }

        @Override
        protected void doLoad() {
            text.setText(String.valueOf(getPreferenceStore().getDouble(super.getPreferenceName())));
        }

        @Override
        protected void doLoadDefault() {
            text.setText(String.valueOf(getPreferenceStore().getDefaultDouble(
                    super.getPreferenceName())));
        }

        @Override
        protected void doStore() {
            try {
                getPreferenceStore().setValue(super.getPreferenceName(),
                        Double.valueOf(text.getText()));
            } catch (Exception e) {
                // you've been warned
            }
        }

        @Override
        public int getNumberOfControls() {
            return 2;
        }

    }

}