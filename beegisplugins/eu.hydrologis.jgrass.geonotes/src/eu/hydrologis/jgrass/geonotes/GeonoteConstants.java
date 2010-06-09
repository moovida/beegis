package eu.hydrologis.jgrass.geonotes;

import eu.hydrologis.jgrass.beegisutils.database.annotatedclasses.GeonotesDrawareaTable;
import eu.hydrologis.jgrass.beegisutils.database.annotatedclasses.GeonotesMediaboxBlobsTable;
import eu.hydrologis.jgrass.beegisutils.database.annotatedclasses.GeonotesTable;
import eu.hydrologis.jgrass.beegisutils.database.annotatedclasses.GeonotesTextareaTable;

public class GeonoteConstants {

    /*
     * ================= default values ======================
     */
    public static final int DEFAULT_GEONOTE_WIDTH = 350;
    public static final int DEFAULT_GEONOTE_HEIGHT = 350;
    public static final String DEFAULT_GEONOTE_TITLE = "new geonote"; //$NON-NLS-1$
    public static final int DEFAULT_GEONOTE_TYPE = 0;

    /**
     * A generic name used for geonotes. 
     * 
     * <p>For example this is used as main folder for a serialized geonote.</p>
     */
    public static final String GEONOTES = "geonotes"; //$NON-NLS-1$

    /**
     * Name for the file of a serialized geonote properties.
     */
    public static final String GEONOTE_BIN_PROPERTIES = "properties.bin"; //$NON-NLS-1$

    /**
     * Name for the file of a serialized geonote properties.
     */
    public static final String GEONOTE_BIN_DRAWINGS = "drawings.bin"; //$NON-NLS-1$

    /**
     * Name for the file of a geonote text.
     */
    public static final String GEONOTE_BIN_TEXT = "text.txt"; //$NON-NLS-1$

    /**
     * Extention used for the drawings done on an image media type.
     */
    public static final String DRAWING_EXTENTION = ".drawing"; //$NON-NLS-1$

    /**
     * The folder containing medias when exporting and importing a geonote. 
     */
    public static final String MEDIA_FOLDER = "media"; //$NON-NLS-1$

    /**
     * Width in pixels of the pin drawn on the geonotes layer. 
     */
    public static final int PINIMAGE_WIDTH = 30;

    /**
     * Height in pixels of the pin drawn on the geonotes layer. 
     */
    public static final int PINIMAGE_HEIGHT = 21;

    /**
     * Key defining that in this status the note is created from scratch and 
     * put in the database with inserts.
     */
    public static final String STATUSNEW = "NEW"; //$NON-NLS-1$

    /**
     * Key defining that in this status the note is already in the database and 
     * the data is updated.
     */
    public static final String STATUSOLD = "OLD"; //$NON-NLS-1$

    /**
     * Key for the position in which the geonote was taken.
     */
    public static final String GEONOTEPOSITION = "POSTITPOSITION"; //$NON-NLS-1$

    /**
     * Key for the CRS WKT with which the geonote was taken.
     */
    public static final String GEONOTEPOSITIONCRS = "POSTITPOSITIONCRS"; //$NON-NLS-1$

    /**
     * Key for the date in which the geonote was taken.
     */
    public static final String GEONOTESDATE = "POSTITDATE"; //$NON-NLS-1$

    /**
     * Key for the general geonote color.
     */
    public static final String GEONOTECOLOR = "POSTITCOLOR"; //$NON-NLS-1$

    /**
     * Key for the geonote start width and height.
     */
    public static final String GEONOTEWIDTHHEIGHT = "POSTITWIDTHHEIGHT"; //$NON-NLS-1$

    /**
     * Key for the geonote start title.
     */
    public static final String GEONOTETITLE = "POSTITTITLE"; //$NON-NLS-1$

    /**
     * Key for the geonote title tooltip.
     */
    public static final String GEONOTETITLETOOLTIP = "GEONOTETITLETOOLTIP"; //$NON-NLS-1$

    /**
     * Key defining the geonote type.
     */
    public static final String GEONOTETYPE = "GEONOTETYPE"; //$NON-NLS-1$
    public static final int NORMAL = 0;
    public static final int GPS = 1;
    public static final int PHOTO = 2;

    /**
     * Key defining the geonote status.
     */
    public static final String GEONOTESTATUS = "GEONOTESTATUS"; //$NON-NLS-1$

    /**
     * Array of RGB values for the default background colors for the quick color bar.
     */
    public static final int[][] DEFAULTBACKGROUNDCOLORS = new int[][]{{255, 250, 90},
            {254, 248, 194}, {245, 205, 101}, {221, 249, 153}, {210, 243, 253}, {236, 213, 251}};

    public static enum NOTIFICATION {
        STYLECHANGED, SIZECHANGED, TITLECHANGED, NOTEADDED, NOTESAVED, NOTEREMOVED
    }
    
    /*
     * ==========================================================================================
     * 
     * mapped classes needed in queries where criterias can't be used and the class is a string, 
     * i.e. it doesn't change when refactoring occurs.
     * 
     * ==========================================================================================
     */
    
    public static final String MEDIABOXBLOBSTABLE_CLASSNAME = GeonotesMediaboxBlobsTable.class.getSimpleName();
    public static final String DRAWAREATABLE_NAME = GeonotesDrawareaTable.class.getSimpleName();
    public static final String TEXTAREATABLE_NAME = GeonotesTextareaTable.class.getSimpleName();
    public static final String GEONOTESTABLE_NAME = GeonotesTable.class.getSimpleName();

}
