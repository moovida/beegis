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

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.ScaleFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Scale;
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
public class GpsPropertiesPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private ScopedPreferenceStore preferences;
    private IntegerFieldEditor widthEditor;
    private ColorFieldEditor activeColorEditor;
    private ColorFieldEditor nonActiveColorEditor;
    private BooleanFieldEditor doCrossEditor;
    private IntegerFieldEditor crossWidthEditor;
    private ColorFieldEditor crossColorEditor;

    public GpsPropertiesPreferencePage() {
        super(GRID);
        preferences = (ScopedPreferenceStore) GpsActivator.getDefault().getPreferenceStore();
        setPreferenceStore(preferences);
        setDescription("GPS Properties Page");
    }

    protected void createFieldEditors() {

        widthEditor = new IntegerFieldEditor(WIDTH, WIDTH, getFieldEditorParent());
        widthEditor.setValidRange(1, 5);
        widthEditor.setValidRange(1, 5);
        addField(widthEditor);

        // symbol colors
        Color red = Display.getDefault().getSystemColor(SWT.COLOR_RED);
        RGB activeRgb = red.getRGB();
        Color magenta = Display.getDefault().getSystemColor(SWT.COLOR_MAGENTA);
        RGB nonActiveRgb = magenta.getRGB();

        activeColorEditor = new ColorFieldEditor(ACTIVECOLOR, ACTIVECOLOR, getFieldEditorParent());
        activeColorEditor.getColorSelector().setColorValue(activeRgb);
        addField(activeColorEditor);

        nonActiveColorEditor = new ColorFieldEditor(NONACTIVECOLOR, NONACTIVECOLOR, getFieldEditorParent());
        nonActiveColorEditor.getColorSelector().setColorValue(nonActiveRgb);
        addField(nonActiveColorEditor);

        doCrossEditor = new BooleanFieldEditor(DOCROSSHAIR, DOCROSSHAIR, getFieldEditorParent());
        addField(doCrossEditor);

        crossWidthEditor = new IntegerFieldEditor(CROSSWIDTH, CROSSWIDTH, getFieldEditorParent());
        crossWidthEditor.setValidRange(1, 5);
        addField(crossWidthEditor);

        // cross color
        Color gray = Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY);
        RGB crossRgb = gray.getRGB();
        crossColorEditor = new ColorFieldEditor(CROSSCOLOR, CROSSCOLOR, getFieldEditorParent());
        crossColorEditor.getColorSelector().setColorValue(crossRgb);
        addField(crossColorEditor);

    }

    public boolean performOk() {
        try {
            // set also runtime preferences
            GpsProperties.gpsSymbolWidth = widthEditor.getIntValue();
            RGB rgb = activeColorEditor.getColorSelector().getColorValue();
            GpsProperties.gpsActiveColor = new java.awt.Color(rgb.red, rgb.green, rgb.blue);
            rgb = nonActiveColorEditor.getColorSelector().getColorValue();
            GpsProperties.gpsNonActiveColor = new java.awt.Color(rgb.red, rgb.green, rgb.blue);

            GpsProperties.doCrosshair = doCrossEditor.getBooleanValue();
            GpsProperties.crosshairWidth = crossWidthEditor.getIntValue();
            rgb = crossColorEditor.getColorSelector().getColorValue();
            GpsProperties.crosshairColor = new java.awt.Color(rgb.red, rgb.green, rgb.blue);

            preferences.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return super.performOk();
    }

    public void init( IWorkbench workbench ) {
    }

}
