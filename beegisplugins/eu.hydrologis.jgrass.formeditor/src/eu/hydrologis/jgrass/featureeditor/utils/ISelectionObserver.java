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
package eu.hydrologis.jgrass.featureeditor.utils;

import org.opengis.feature.simple.SimpleFeature;

import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.internal.Map;

/**
 * A selection observer that listens to feature changes considering maps and layers.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public interface ISelectionObserver {
    
    /**
     * Triggered by a feature selection change. 
     * 
     * @param selectedMap the selected {@link Map}.
     * @param selectedLayer the selected {@link ILayer layer}.
     * @param selectedFeature the selected {@link SimpleFeature feature}.
     */
    public void selectionChanged(Map selectedMap, ILayer selectedLayer, SimpleFeature selectedFeature);
}
