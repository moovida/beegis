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
package eu.hydrologis.jgrass.featureeditor;

import org.eclipse.ui.IStartup;

import eu.hydrologis.jgrass.formeditor.FormEditorPlugin;

/**
 * Class to register the listener on the plugin activator.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public class FormEditorPluginEarlyStartup implements IStartup {

    public void earlyStartup() {
         FormEditorPlugin.getDefault().registerPartListener();
    }

}
