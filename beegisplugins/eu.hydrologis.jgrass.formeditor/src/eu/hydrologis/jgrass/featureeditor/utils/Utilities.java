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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FilenameUtils;

import eu.hydrologis.jgrass.featureeditor.xml.annotated.AForm;

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
     * Reads a {@link AForm} from a form file.
     * 
     * @param formFile the file containing the form xml.
     * @return the {@link AForm} object.
     * @throws Exception
     */
    public static AForm readForm( File formFile ) throws Exception {
        BufferedReader br = null;
        StringBuilder xml = new StringBuilder();
        try {
            br = new BufferedReader(new FileReader(formFile));
            String line = null;
            while( (line = br.readLine()) != null ) {
                xml.append(line).append("\n"); //$NON-NLS-1$
            }
        } finally {
            br.close();
        }

        String xmlString = xml.toString();
        AForm form = parseXML(xmlString);
        return form;
    }

    /**
     * Parse a form xml.
     * 
     * @param xml the xml containing the form definition. 
     * @return the {@link AForm}.
     * @throws Exception
     */
    public static AForm parseXML( String xml ) throws Exception {
        JAXBContext jc = JAXBContext.newInstance("eu.hydrologis.jgrass.featureeditor.xml.annotated"); //$NON-NLS-1$
        Unmarshaller um = jc.createUnmarshaller();
        StringReader sr = new StringReader(xml);
        return (AForm) um.unmarshal(sr);
    }

    /**
     * Write the {@link AForm} to xml file.
     * 
     * @param form the {@link AForm} object.
     * @param file the file to which to dump to. 
     * @throws Exception
     */
    public static void writeXML( AForm form, File file ) throws Exception {
        JAXBContext jc = JAXBContext.newInstance(AForm.class);
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(form, file);
    }

}
