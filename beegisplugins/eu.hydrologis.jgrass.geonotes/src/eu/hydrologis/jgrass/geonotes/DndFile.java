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
package eu.hydrologis.jgrass.geonotes;

import java.io.File;
import java.util.HashMap;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The file representation for the media box
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class DndFile implements MimeTypes {

    public File file = null;
    public String name = null;
    public String iconPath = null;
    private final GeonotesHandler geonotesHandler;

    public DndFile( GeonotesHandler geonotesHandler ) {
        this.geonotesHandler = geonotesHandler;
    }

    public GeonotesHandler getGeonotesHandler() {
        return geonotesHandler;
    }

    public boolean equals( Object obj ) {
        if (obj instanceof DndFile) {
            DndFile otherDndFile = (DndFile) obj;

            if (!otherDndFile.getGeonotesHandler().equals(geonotesHandler)) {
                return false;
            }
            if (!otherDndFile.name.equals(name)) {
                return false;
            }
            if (file != null && otherDndFile.file != null) {
                if (!otherDndFile.file.equals(file)) {
                    return false;
                }
            }
            if (!otherDndFile.iconPath.equals(iconPath)) {
                return false;
            }
            return true;
        }
        return false;
    }

    private static HashMap<String, String> iconsMap = null;

    public synchronized static String getIconForExtention( String ext ) {
        if (iconsMap == null) {
            iconsMap = new HashMap<String, String>();

            iconsMap.put(T_GIF, IMAGEICON);
            iconsMap.put(T_PNG, IMAGEICON);
            iconsMap.put(T_JPG, IMAGEICON);
            iconsMap.put(T_TIF, IMAGEICON);
            iconsMap.put(T_ZIP, ARCHIVEICON);
            iconsMap.put(T_MP3, AUDIOICON);
            iconsMap.put(T_WAV, AUDIOICON);
            iconsMap.put(T_ODOC, OFFICETEXTICON);
            iconsMap.put(T_OPPT, OFFICEPRESICON);
            iconsMap.put(T_OXLS, OFFICESPREADICON);
            iconsMap.put(T_DOC, OFFICETEXTICON);
            iconsMap.put(T_PPT, OFFICEPRESICON);
            iconsMap.put(T_XLS, OFFICESPREADICON);
            iconsMap.put(T_TXT, TEXTICON);
            iconsMap.put(T_AVI, VIDEOICON);
            iconsMap.put(T_MPG, VIDEOICON);
            iconsMap.put(T_PDF, PDFICON);
            iconsMap.put(T_FOLDER, FOLDERICON);
        }

        String path = iconsMap.get(ext);
        if (path == null) {
            path = UNKNOWNICON;
        }

        return path;
    }

    public ImageDescriptor getImage() {
        if (iconPath == null) {
            return null;
        }
        ImageDescriptor imgDescr = AbstractUIPlugin.imageDescriptorFromPlugin(
                GeonotesPlugin.PLUGIN_ID, this.iconPath);
        return imgDescr;
    }
}
