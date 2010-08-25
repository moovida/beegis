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
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FilenameUtils;

import eu.hydrologis.jgrass.featureeditor.xml.annotated.Form;

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

    /**
     * Parse a form xml.
     * 
     * @param xml the xml containing the form definition. 
     * @return the {@link Form}.
     * @throws Exception
     */
    public static Form parseXML( String xml ) throws Exception {
        JAXBContext jc = JAXBContext.newInstance("eu.hydrologis.jgrass.featureeditor.xml.annotated"); //$NON-NLS-1$
        Unmarshaller um = jc.createUnmarshaller();
        StringReader sr = new StringReader(xml);
        return (Form) um.unmarshal(sr);
    }

    /**
     * Write the {@link Form} to xml file.
     * 
     * @param form the {@link Form} object.
     * @param file the file to which to dump to. 
     * @throws Exception
     */
    public static void writeXML( Form form, File file ) throws Exception {
        JAXBContext jc = JAXBContext.newInstance(Form.class);
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(form, file);
    }
}
