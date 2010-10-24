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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.jpeg.exifRewrite.ExifRewriter;
import org.apache.sanselan.formats.tiff.TiffField;
import org.apache.sanselan.formats.tiff.TiffImageMetadata;
import org.apache.sanselan.formats.tiff.constants.TagInfo;
import org.apache.sanselan.formats.tiff.constants.TiffConstants;
import org.apache.sanselan.formats.tiff.write.TiffOutputSet;
import org.jgrasstools.gears.utils.time.UtcTimeUtilities;
import org.joda.time.DateTime;

/**
 * A class that handles exif operation.
 * 
 * <p>Adapted from various sanselan docs and snippets.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
@SuppressWarnings("nls")
public class ExifHandler {

    public static final String GPS_DESCRIPTION = "GPS Description";
    public static final String GPS_LATITUDE_DEGREES_NORTH = "GPS Latitude (Degrees North)";
    public static final String GPS_LONGITUDE_DEGREES_EAST = "GPS Longitude (Degrees East)";

    /**
     * Read exif tags into a {@link HashMap}.
     * 
     * <p>Current extracted tags are:
     * <ul>
     * <li>TiffConstants.TIFF_TAG_XRESOLUTION</li>
     * <li>TiffConstants.TIFF_TAG_DATE_TIME</li>
     * <li>TiffConstants.EXIF_TAG_DATE_TIME_ORIGINAL</li>
     * <li>TiffConstants.EXIF_TAG_CREATE_DATE</li>
     * <li>TiffConstants.EXIF_TAG_ISO</li>
     * <li>TiffConstants.EXIF_TAG_SHUTTER_SPEED_VALUE</li>
     * <li>TiffConstants.EXIF_TAG_APERTURE_VALUE</li>
     * <li>TiffConstants.EXIF_TAG_BRIGHTNESS_VALUE</li>
     * </ul>
     * 
     * @param jpegImageFile the image file to read the tags from.
     * @return the map of tag names and values.
     * @throws Exception
     */
    public static HashMap<String, String> readMetaData( File jpegImageFile ) throws Exception {
        HashMap<String, String> metadataMap = new HashMap<String, String>(30);

        IImageMetadata metadata = Sanselan.getMetadata(jpegImageFile);
        if (metadata instanceof JpegImageMetadata) {
            JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
            addTagToMap(jpegMetadata, TiffConstants.TIFF_TAG_XRESOLUTION, metadataMap);
            addTagToMap(jpegMetadata, TiffConstants.TIFF_TAG_DATE_TIME, metadataMap);
            addTagToMap(jpegMetadata, TiffConstants.EXIF_TAG_DATE_TIME_ORIGINAL, metadataMap);
            addTagToMap(jpegMetadata, TiffConstants.EXIF_TAG_CREATE_DATE, metadataMap);
            addTagToMap(jpegMetadata, TiffConstants.EXIF_TAG_ISO, metadataMap);
            addTagToMap(jpegMetadata, TiffConstants.EXIF_TAG_SHUTTER_SPEED_VALUE, metadataMap);
            addTagToMap(jpegMetadata, TiffConstants.EXIF_TAG_APERTURE_VALUE, metadataMap);
            addTagToMap(jpegMetadata, TiffConstants.EXIF_TAG_BRIGHTNESS_VALUE, metadataMap);

            TiffImageMetadata exifMetadata = jpegMetadata.getExif();
            if (exifMetadata != null) {
                try {
                    TiffImageMetadata.GPSInfo gpsInfo = exifMetadata.getGPS();
                    if (null != gpsInfo) {
                        double longitude = gpsInfo.getLongitudeAsDegreesEast();
                        double latitude = gpsInfo.getLatitudeAsDegreesNorth();
                        metadataMap.put(GPS_DESCRIPTION, gpsInfo.toString());
                        metadataMap.put(GPS_LONGITUDE_DEGREES_EAST, String.valueOf(longitude));
                        metadataMap.put(GPS_LATITUDE_DEGREES_NORTH, String.valueOf(latitude));
                    }
                } catch (ImageReadException e) {
                    e.printStackTrace();
                }
            }

            // ArrayList items = jpegMetadata.getItems();
            // for( int i = 0; i < items.size(); i++ ) {
            // Object item = items.get(i);
            // mdataList.add("    " + "item: " + item);
            // }
        }

        return metadataMap;
    }

    private static void addTagToMap( JpegImageMetadata jpegMetadata, TagInfo tagInfo, HashMap<String, String> metadataMap )
            throws Exception {
        TiffField field = jpegMetadata.findEXIFValue(tagInfo);
        if (field != null) {
            metadataMap.put(tagInfo.name, field.getValueDescription());
        }
    }

    /**
     * Extract the creation {@link DateTime} from the map of tags read by {@link #readMetaData(File)}.
     * 
     * @param tags2ValuesMap the map of tags read by {@link #readMetaData(File)}.
     * @return the datetime.
     */
    public static DateTime getCreationDatetimeUtc( HashMap<String, String> tags2ValuesMap ) {
        String creationDate = tags2ValuesMap.get(TiffConstants.EXIF_TAG_CREATE_DATE.name);
        creationDate = creationDate.replaceAll("'", "");
        creationDate = creationDate.replaceFirst(":", "-");
        creationDate = creationDate.replaceFirst(":", "-");

        DateTime dt = UtcTimeUtilities.fromStringWithSeconds(creationDate);
        return dt;
    }

    /**
     * Writes the supplied gps tags to the image file.
     * 
     * <p>The tags are written to a new image which then
     * replaces the old image and is then removed.
     * 
     * @param lat the latitude gps entry to add.
     * @param lon the longitude gps entry to add.
     * @param jpegImageFile the image file to update.
     * @throws Exception
     */
    public static void writeGPSTagsToImage( double lat, double lon, File jpegImageFile ) throws Exception {
        OutputStream os = null;
        try {
            TiffOutputSet outputSet = null;

            // note that metadata might be null if no metadata is found.
            IImageMetadata metadata = Sanselan.getMetadata(jpegImageFile);
            JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
            if (null != jpegMetadata) {
                TiffImageMetadata exif = jpegMetadata.getExif();
                if (null != exif) {
                    outputSet = exif.getOutputSet();
                }
            }

            if (null == outputSet)
                outputSet = new TiffOutputSet();

            outputSet.setGPSInDegrees(lon, lat);

            File destinationImageFile = new File(jpegImageFile.getAbsolutePath() + ".jpg"); //$NON-NLS-1$

            os = new FileOutputStream(destinationImageFile);
            os = new BufferedOutputStream(os);

            new ExifRewriter().updateExifMetadataLossless(jpegImageFile, os, outputSet);

            os.close();

            FileUtils.copyFile(destinationImageFile, jpegImageFile);
            FileUtils.forceDelete(destinationImageFile);
        } finally {
            if (os != null)
                os.close();
        }
    }

    public static void main( String[] args ) throws Exception {
        File imgFile = new File("/home/moovida/Desktop/rilievo_drava/DCIM/100CANON/IMG_3826.JPG");
        dump(imgFile);
        writeGPSTagsToImage(44.12, 11.26, imgFile);
        dump(imgFile);
        

    }

    private static void dump( File imgFile ) throws Exception {
        HashMap<String, String> readMetaData = ExifHandler.readMetaData(imgFile);
        DateTime creationDatetimeUtc = getCreationDatetimeUtc(readMetaData);
        String creationDate = readMetaData.get(TiffConstants.EXIF_TAG_CREATE_DATE.name);
        System.out.println(creationDate + " / " + creationDatetimeUtc);
        String lat = readMetaData.get(ExifHandler.GPS_LATITUDE_DEGREES_NORTH);
        String lon = readMetaData.get(ExifHandler.GPS_LONGITUDE_DEGREES_EAST);

        System.out.println("lat = " + lat + " / lon = " + lon);
    }

}
