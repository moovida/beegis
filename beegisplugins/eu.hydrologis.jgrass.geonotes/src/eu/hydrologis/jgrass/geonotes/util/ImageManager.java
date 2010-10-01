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
package eu.hydrologis.jgrass.geonotes.util;

import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.ImageIcon;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import eu.hydrologis.jgrass.geonotes.GeonotesPlugin;

/**
 * Manager for centralized image handling.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public enum ImageManager {
    /**
     * Icon for the geonote.
     */
    ICON_PIN_30("/icons/redpin30.png"), //
    ICON_PIN_16("/icons/redpin16.png"), //

    /**
     * Icon for the selected geonote.
     */
    ICON_SELECTED_PIN("/icons/selectedredpin30.png"),

    /**
     * Icon for geonotes bound to a gps position.
     */
    ICON_GPS_PIN_16("/icons/gpsnote16.png"), //
    ICON_GPS_PIN_30("/icons/gpsnote30.png"), //

    /**
     * Icon for geonotes bound to a imported photo position.
     */
    ICON_PHOTO_PIN("/icons/photonote.png"),

    ICON_TEXT("icons/text.gif"), //
    ICON_DRAW("icons/draw.gif"), //
    ICON_MULTIMEDIA("icons/multimedia.gif"), //
    ICON_TRASH("icons/trash.gif"), //
    ICON_CLOSE("icons/close.gif"), //
    ICON_SAFECLOSE("icons/save.gif"), //
    ICON_CONFIG("icons/config.gif");

    private final String path;
    private static HashMap<String, org.eclipse.swt.graphics.Image> swtImageMap = new HashMap<String, org.eclipse.swt.graphics.Image>();
    private static HashMap<String, Image> awtImageMap = new HashMap<String, Image>();

    private ImageManager( String path ) {
        this.path = path;
    }

    public Image getAwtImage() {
        Image image = awtImageMap.get(path);
        if (image == null) {
            createImages();
            image = awtImageMap.get(path);
        }
        return image;
    }

    public org.eclipse.swt.graphics.Image getSwtImage() {
        org.eclipse.swt.graphics.Image image = swtImageMap.get(path);
        if (image == null) {
            createImages();
            image = swtImageMap.get(path);
        }
        return image;
    }

    private void createImages() {
        try {
            URL fileURL = FileLocator.find(Platform.getBundle(GeonotesPlugin.PLUGIN_ID), new Path(path), null);
            fileURL = FileLocator.toFileURL(fileURL);
            String fileUrlPath = fileURL.getPath();
            ImageIcon ii = new ImageIcon(fileUrlPath);
            Image awtImage = ii.getImage();

            ImageDescriptor iD = AbstractUIPlugin.imageDescriptorFromPlugin(GeonotesPlugin.PLUGIN_ID, path);
            org.eclipse.swt.graphics.Image swtImage = iD.createImage();

            awtImageMap.put(path, awtImage);
            swtImageMap.put(path, swtImage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void disposeSwt() {
        Collection<org.eclipse.swt.graphics.Image> values = swtImageMap.values();
        for( org.eclipse.swt.graphics.Image image : values ) {
            image.dispose();
        }
    }

}
