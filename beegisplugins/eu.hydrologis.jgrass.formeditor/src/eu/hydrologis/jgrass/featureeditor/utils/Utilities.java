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

import java.io.File;

import org.apache.commons.io.FilenameUtils;

/**
 * Utilities for forms.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class Utilities {

    public static final String FORM = ".form"; //$NON-NLS-1$

    /**
     * Extracts the form file from a resource file (ex shapefile).
     * 
     * @param resourceFile the resource that has a form sidecar file.
     * @return the form file or null, if it doesn't exist.
     */
    public static File getFormFile( File resourceFile ) {

        String baseName = FilenameUtils.getBaseName(resourceFile.getName());
        File formFile = new File(resourceFile.getParentFile(), baseName + FORM);

        if (formFile.exists()) {
            return formFile;
        } else {
            return null;
        }
    }
}
