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
    INSTANCE;

    /**
     * Icon for the geonote.
     */
    public static final String ICON_PIN_30 = "/icons/redpin30.png";
    public static final String ICON_PIN_16 = "/icons/redpin16.png";

    /**
     * Icon for the selected geonote.
     */
    public static final String ICON_SELECTED_PIN = "/icons/selectedredpin30.png";

    /**
     * Icon for geonotes bound to a gps position.
     */
    public static final String ICON_GPS_PIN_16 = "/icons/gpsnote16.png";
    public static final String ICON_GPS_PIN_30 = "/icons/gpsnote30.png";

    /**
     * Icon for geonotes bound to a imported photo position.
     */
    public static final String ICON_PHOTO_PIN = "/icons/photonote.png";

    public static final String ICON_TEXT = "icons/text.gif";
    public static final String ICON_DRAW = "icons/draw.gif";
    public static final String ICON_MULTIMEDIA = "icons/multimedia.gif";
    public static final String ICON_TRASH = "icons/trash.gif";
    public static final String ICON_CLOSE = "icons/close.gif";
    public static final String ICON_SAFECLOSE = "icons/save.gif";

    private static Image pinImage30;
    private static org.eclipse.swt.graphics.Image pinImageSWT16;
    private static org.eclipse.swt.graphics.Image pinImageSWT30;

    private static Image selectedPinImage30;
    private static org.eclipse.swt.graphics.Image selPinImageSWT30;
    
    private static Image gpsPinImage16;
    private static Image gpsPinImage30;
    private static org.eclipse.swt.graphics.Image gpsPinImageSWT16;

    private static Image photoPinImage30;
    private static org.eclipse.swt.graphics.Image photoPinImageSWT30;
    
    private static org.eclipse.swt.graphics.Image textImage;
    private static org.eclipse.swt.graphics.Image drawImage;
    private static org.eclipse.swt.graphics.Image multimediaImage;
    private static org.eclipse.swt.graphics.Image trashImage;
    private static org.eclipse.swt.graphics.Image closeImage;
    private static org.eclipse.swt.graphics.Image safecloseImage;
    private static org.eclipse.swt.graphics.Image gpsPinImageSWT30;

    static {
        try {
            URL fileURL = FileLocator.find(Platform.getBundle(GeonotesPlugin.PLUGIN_ID), new Path(
                    ICON_PIN_30), null);
            fileURL = FileLocator.toFileURL(fileURL);
            String fileUrlPath = fileURL.getPath();
            ImageIcon ii = new ImageIcon(fileUrlPath);
            pinImage30 = ii.getImage();
            
            ImageDescriptor pinD = AbstractUIPlugin.imageDescriptorFromPlugin(
                    GeonotesPlugin.PLUGIN_ID, ICON_PIN_30);
            pinImageSWT30 = pinD.createImage();
            
            pinD = AbstractUIPlugin.imageDescriptorFromPlugin(
                    GeonotesPlugin.PLUGIN_ID, ICON_PIN_16);
            pinImageSWT16 = pinD.createImage();

            fileURL = FileLocator.find(Platform.getBundle(GeonotesPlugin.PLUGIN_ID), new Path(
                    ICON_SELECTED_PIN), null);
            fileURL = FileLocator.toFileURL(fileURL);
            fileUrlPath = fileURL.getPath();
            ii = new ImageIcon(fileUrlPath);
            selectedPinImage30 = ii.getImage();

            ImageDescriptor selPinD = AbstractUIPlugin.imageDescriptorFromPlugin(
                    GeonotesPlugin.PLUGIN_ID, ICON_SELECTED_PIN);
            selPinImageSWT30 = selPinD.createImage();

            fileURL = FileLocator.find(Platform.getBundle(GeonotesPlugin.PLUGIN_ID), new Path(
                    ICON_GPS_PIN_16), null);
            fileURL = FileLocator.toFileURL(fileURL);
            fileUrlPath = fileURL.getPath();
            ii = new ImageIcon(fileUrlPath);
            gpsPinImage16 = ii.getImage();

            fileURL = FileLocator.find(Platform.getBundle(GeonotesPlugin.PLUGIN_ID), new Path(
                    ICON_GPS_PIN_30), null);
            fileURL = FileLocator.toFileURL(fileURL);
            fileUrlPath = fileURL.getPath();
            ii = new ImageIcon(fileUrlPath);
            gpsPinImage30 = ii.getImage();

            ImageDescriptor gpsPinD = AbstractUIPlugin.imageDescriptorFromPlugin(
                    GeonotesPlugin.PLUGIN_ID, ICON_GPS_PIN_16);
            gpsPinImageSWT16 = gpsPinD.createImage();

            ImageDescriptor gpsPin30D = AbstractUIPlugin.imageDescriptorFromPlugin(
                    GeonotesPlugin.PLUGIN_ID, ICON_GPS_PIN_30);
            gpsPinImageSWT30 = gpsPin30D.createImage();

            fileURL = FileLocator.find(Platform.getBundle(GeonotesPlugin.PLUGIN_ID), new Path(
                    ICON_PHOTO_PIN), null);
            fileURL = FileLocator.toFileURL(fileURL);
            fileUrlPath = fileURL.getPath();
            ii = new ImageIcon(fileUrlPath);
            photoPinImage30 = ii.getImage();

            ImageDescriptor photoPinD = AbstractUIPlugin.imageDescriptorFromPlugin(
                    GeonotesPlugin.PLUGIN_ID, ICON_PHOTO_PIN);
            photoPinImageSWT30 = photoPinD.createImage();

            ImageDescriptor textD = AbstractUIPlugin.imageDescriptorFromPlugin(
                    GeonotesPlugin.PLUGIN_ID, ICON_TEXT);
            textImage = textD.createImage();
            ImageDescriptor drawD = AbstractUIPlugin.imageDescriptorFromPlugin(
                    GeonotesPlugin.PLUGIN_ID, ICON_DRAW);
            drawImage = drawD.createImage();
            ImageDescriptor mmediaID = AbstractUIPlugin.imageDescriptorFromPlugin(
                    GeonotesPlugin.PLUGIN_ID, ICON_MULTIMEDIA);
            multimediaImage = mmediaID.createImage();
            ImageDescriptor trashD = AbstractUIPlugin.imageDescriptorFromPlugin(
                    GeonotesPlugin.PLUGIN_ID, ICON_TRASH); 
            trashImage = trashD.createImage();
            ImageDescriptor closeD = AbstractUIPlugin.imageDescriptorFromPlugin(
                    GeonotesPlugin.PLUGIN_ID, ICON_CLOSE); //$NON-NLS-1$
            closeImage = closeD.createImage();
            ImageDescriptor safecloseD = AbstractUIPlugin.imageDescriptorFromPlugin(
                    GeonotesPlugin.PLUGIN_ID, ICON_SAFECLOSE); //$NON-NLS-1$
            safecloseImage = safecloseD.createImage();
        } catch (IOException e) {
            GeonotesPlugin
                    .log(
                            "GeonotesPlugin problem: eu.hydrologis.jgrass.geonotes.util#ImageManager#staticimageproducer", e); //$NON-NLS-1$
            e.printStackTrace();
        }
    }

    /**
     * Getter for the normal geonote symbol.
     * 
     * @return the normal geonote symbol.
     */
    public Image getPinImage30() {
        return pinImage30;
    }

    /**
     * Getter for the normal geonote symbol SWT.
     * 
     * @return the normal geonote symbol SWT.
     */
    public org.eclipse.swt.graphics.Image getPinImageSWT30() {
        return pinImageSWT30;
    }

    /**
     * Getter for the normal geonote symbol SWT.
     * 
     * @return the normal geonote symbol SWT.
     */
    public org.eclipse.swt.graphics.Image getPinImageSWT16() {
        return pinImageSWT16;
    }

    /**
     * Getter for the selected geonote symbol.
     * 
     * @return the selected geonote symbol.
     */
    public Image getSelectedPinImage30() {
        return selectedPinImage30;
    }

    /**
     * Getter for the selected geonote symbol SWT.
     * 
     * @return the selected geonote symbol SWT.
     */
    public org.eclipse.swt.graphics.Image getSelectedPinImageSWT30() {
        return selPinImageSWT30;
    }

    /**
     * Getter for the gps geonote symbol.
     * 
     * @return the gps geonote symbol.
     */
    public Image getGpsPinImage16() {
        return gpsPinImage16;
    }

    /**
     * Getter for the gps geonote symbol.
     * 
     * @return the gps geonote symbol.
     */
    public Image getGpsPinImage30() {
        return gpsPinImage30;
    }

    /**
     * Getter for the gps geonote symbol SWT.
     * 
     * @return the gps geonote symbol SWT.
     */
    public org.eclipse.swt.graphics.Image getGpsPinImageSWT16() {
        return gpsPinImageSWT16;
    }

    /**
     * Getter for the gps geonote symbol SWT.
     * 
     * @return the gps geonote symbol SWT.
     */
    public org.eclipse.swt.graphics.Image getGpsPinImageSWT30() {
        return gpsPinImageSWT30;
    }

    /**
     * Getter for the photo geonote symbol.
     * 
     * @return the photo geonote symbol.
     */
    public Image getPhotoPinImage30() {
        return photoPinImage30;
    }

    /**
     * Getter for the photo geonote symbol SWT.
     * 
     * @return the photo geonote symbol SWT.
     */
    public org.eclipse.swt.graphics.Image getPhotoPinImageSWT30() {
        return photoPinImageSWT30;
    }

    /**
     * Getter for the text icon.
     * 
     * @return the text icon.
     */
    public org.eclipse.swt.graphics.Image getTextImage() {
        return textImage;
    }

    /**
     * Getter for the draw icon.
     * 
     * @return the draw icon.
     */
    public org.eclipse.swt.graphics.Image getDrawImage() {
        return drawImage;
    }

    /**
     * Getter for the multimedia icon.
     * 
     * @return the multimedia icon.
     */
    public org.eclipse.swt.graphics.Image getMultimediaImage() {
        return multimediaImage;
    }

    /**
     * Getter for the trash icon.
     * 
     * @return the trash icon.
     */
    public org.eclipse.swt.graphics.Image getTrashImage() {
        return trashImage;
    }

    /**
     * Getter for the close icon.
     * 
     * @return the close icon.
     */
    public org.eclipse.swt.graphics.Image getCloseImage() {
        return closeImage;
    }

    /**
     * Getter for the safe close icon.
     * 
     * @return the safe close icon.
     */
    public org.eclipse.swt.graphics.Image getSafeCloseImage() {
        return safecloseImage;
    }

}
