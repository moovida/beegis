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
package eu.hydrologis.jgrass.beegisutils.utils;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import eu.hydrologis.jgrass.beegisutils.BeegisUtilsPlugin;

/**
 * A singleton cache for images.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@SuppressWarnings("nls")
public class ImageCache {
    
    public static final String STROKE_WIDTH_1 = "/icons/strokewidth_1.png";
    public static final String STROKE_WIDTH_2 = "/icons/strokewidth_2.png";
    public static final String STROKE_WIDTH_3 = "/icons/strokewidth_3.png";
    public static final String STROKE_WIDTH_4 = "/icons/strokewidth_4.png";
    public static final String STROKE_WIDTH_5 = "/icons/strokewidth_5.png";
    public static final String TRASH = "icons/trash.gif";
    public static final String CLOSE = "icons/close.gif";
    public static final String ZOOM_ALL = "icons/zoom_all.gif";
    public static final String ZOOM_IN = "icons/zoom_in.gif";
    public static final String ZOOM_OUT = "icons/zoom_out.gif";
    public static final String ROTATE = "icons/rotate.gif";
    
    private static ImageCache imageCache;

    private HashMap<String, Image> imageMap = new HashMap<String, Image>();

    private ImageCache() {
    }

    public static ImageCache getInstance() {
        if (imageCache == null) {
            imageCache = new ImageCache();
        }
        return imageCache;
    }

    /**
     * Get an image for a certain key.
     * 
     * <p><b>The only keys to be used are the static strings in this class!!</b></p>
     * 
     * @param key a file key, as for example {@link ImageCache#DATABASE_VIEW}.
     * @return the image.
     */
    public Image getImage( String key ) {
        Image image = imageMap.get(key);
        if (image == null) {
            image = createImage(key);
            imageMap.put(key, image);
        }
        return image;
    }

    private Image createImage( String key ) {
        ImageDescriptor id = AbstractUIPlugin.imageDescriptorFromPlugin(BeegisUtilsPlugin.PLUGIN_ID, key);
        Image image = id.createImage();
        return image;
    }

    /**
     * Disposes the images and clears the internal map.
     */
    public void dispose() {
        Set<Entry<String, Image>> entrySet = imageMap.entrySet();
        for( Entry<String, Image> entry : entrySet ) {
            entry.getValue().dispose();
        }
        imageMap.clear();
    }

}
