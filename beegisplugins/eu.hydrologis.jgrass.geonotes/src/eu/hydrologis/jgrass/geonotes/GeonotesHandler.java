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

import static eu.hydrologis.jgrass.geonotes.GeonoteConstants.DEFAULTBACKGROUNDCOLORS;
import static eu.hydrologis.jgrass.geonotes.GeonoteConstants.DEFAULT_GEONOTE_HEIGHT;
import static eu.hydrologis.jgrass.geonotes.GeonoteConstants.DEFAULT_GEONOTE_TITLE;
import static eu.hydrologis.jgrass.geonotes.GeonoteConstants.DEFAULT_GEONOTE_TYPE;
import static eu.hydrologis.jgrass.geonotes.GeonoteConstants.DEFAULT_GEONOTE_WIDTH;
import static eu.hydrologis.jgrass.geonotes.GeonoteConstants.DRAWING_EXTENTION;
import static eu.hydrologis.jgrass.geonotes.GeonoteConstants.GEONOTE_BIN_DRAWINGS;
import static eu.hydrologis.jgrass.geonotes.GeonoteConstants.GEONOTE_BIN_PROPERTIES;
import static eu.hydrologis.jgrass.geonotes.GeonoteConstants.GEONOTE_BIN_TEXT;
import static eu.hydrologis.jgrass.geonotes.GeonoteConstants.MEDIA_FOLDER;
import static eu.hydrologis.jgrass.geonotes.GeonoteConstants.PINIMAGE_HEIGHT;
import static eu.hydrologis.jgrass.geonotes.GeonoteConstants.PINIMAGE_WIDTH;
import static org.hibernate.criterion.Order.asc;
import static org.hibernate.criterion.Restrictions.between;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.refractions.udig.mapgraphic.MapGraphic;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.ui.ApplicationGIS;
import net.refractions.udig.ui.ExceptionDetailsDialog;
import net.refractions.udig.ui.PlatformGIS;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import eu.hydrologis.jgrass.beegisutils.BeegisUtilsPlugin;
import eu.hydrologis.jgrass.beegisutils.database.annotatedclasses.GeonotesDrawareaTable;
import eu.hydrologis.jgrass.beegisutils.database.annotatedclasses.GeonotesMediaboxBlobsTable;
import eu.hydrologis.jgrass.beegisutils.database.annotatedclasses.GeonotesMediaboxTable;
import eu.hydrologis.jgrass.beegisutils.database.annotatedclasses.GeonotesTable;
import eu.hydrologis.jgrass.beegisutils.database.annotatedclasses.GeonotesTextareaTable;
import eu.hydrologis.jgrass.beegisutils.database.annotatedclasses.GpsLogTable;
import eu.hydrologis.jgrass.beegisutils.jgrassported.DressedStroke;
import eu.hydrologis.jgrass.database.DatabasePlugin;
import eu.hydrologis.jgrass.geonotes.GeonoteConstants.NOTIFICATION;

/**
 * Wrapper class for {@link GeonotesTable}.
 * 
 * <p>
 * The class handles all the needed issues around a Geonote, from listeners 
 * to the persistence part. 
 * </p>
 * <p>
 * Two constructors are available:
 * <ul>
 *  <li>one which will create a new default note 
 *      and save it to the database</li>
 *  <li>one that takes an id, which will extract the necessary 
 *      infos from the database as needed</li>
 * </ul>
 * This assures an id to be always defined and properly persisted to database.
 * </p>
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@SuppressWarnings({"unchecked", "nls"})
public class GeonotesHandler {

    private static final String GEONOTESMEDIA_NAME_FIELD = "mediaName";
    private static final String GEONOTESMEDIA_EXTERNAL_KEY_ID = "mediaboxId";
    private static final String GEONOTESTABLE_ID_FIELD = "id";
    private static final String GEONOTESTABLE_EXTERNAL_KEY_ID = "geonotesId";

    private List<GeonotesObserver> observers = new ArrayList<GeonotesObserver>();

    private Long id;
    private GeonotesTable geonoteTable;
    private GeonotesTextareaTable geonotesTextareaTable;
    private GeonotesDrawareaTable geonotesDrawareaTable;
    private List<GeonotesMediaboxTable> geonotesMediaboxTables;
    private Color color;

    private GeometryFactory gF = new GeometryFactory();

    private Session openSession() {
        try {
            return DatabasePlugin.getDefault().getActiveDatabaseConnection().openSession();
        } catch (Exception e) {
            String message = "An error occurred while connecting to the database";
            ExceptionDetailsDialog.openError(null, message, IStatus.ERROR, GeonotesPlugin.PLUGIN_ID, e);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Constructs a new Geonote, making it immediatly persistent to database.
     * 
     * @param east easting coordinate in {@link DefaultGeographicCRS#WGS84}.
     * @param north northing coordinate in {@link DefaultGeographicCRS#WGS84}.
     * @param title a short title for the geonote or <code>null</code>.
     * @param info an information text or <code>null</code>.
     * @param type an id for the note type. Can currently be one of:
     *                  <br><ul>
     *                          <li>{@link #NORMAL} </li>
     *                          <li>{@link #GPS} </li>
     *                          <li>{@link #PHOTO} </li>
     *                      </ul>
     * @param creationTime the {@link DateTime} of creation of the geonote.
     * @param azimut the azimut value if the geonote has directionality or <code>null</code>.
     * @param color a color for the note in format r:g:b:a integers or <code>null</code>.
     * @param width a width for the geonote or <code>null</code>.
     * @param height a height for the geonote or <code>null</code>.
     * @throws Exception
     */
    public GeonotesHandler( Double east, Double north, String title, String info, Integer type, DateTime creationTime,
            Double azimut, String color, Integer width, Integer height ) throws Exception {
        if (east == null) {
            throw new IllegalArgumentException("The east coordinate is a mandatory parameter to create a geonote.");
        }
        if (north == null) {
            throw new IllegalArgumentException("The west coordinate is a mandatory parameter to create a geonote.");
        }
        if (creationTime == null) {
            throw new IllegalArgumentException("The creationtime is a mandatory parameter to create a geonote.");
        }
        if (title == null) {
            title = DEFAULT_GEONOTE_TITLE;
        }
        if (info == null) {
            String dateString = creationTime.toString(BeegisUtilsPlugin.dateTimeFormatterYYYYMMDDHHMM);
            info = "Date:" + dateString + "\nN:" + north + "\nE:" + east;
        }
        if (type == null) {
            type = DEFAULT_GEONOTE_TYPE;
        }
        if (width == null) {
            width = DEFAULT_GEONOTE_WIDTH;
        }
        if (height == null) {
            height = DEFAULT_GEONOTE_HEIGHT;
        }
        if (color == null) {
            color = DEFAULTBACKGROUNDCOLORS[0][0] + ":" + DEFAULTBACKGROUNDCOLORS[0][1] + ":" + DEFAULTBACKGROUNDCOLORS[0][2]
                    + ":255";
        }

        Session session = openSession();
        try {
            Transaction transaction = session.beginTransaction();
            geonoteTable = new GeonotesTable();
            geonoteTable.setEast(east);
            geonoteTable.setNorth(north);
            geonoteTable.setType(type);
            geonoteTable.setTitle(title);
            geonoteTable.setInfo(info);
            geonoteTable.setCreationDateTime(creationTime);
            geonoteTable.setColor(color);
            geonoteTable.setWidth(width);
            geonoteTable.setHeight(height);
            if (azimut != null) {
                geonoteTable.setAzimut(azimut);
            }
            id = (Long) session.save(geonoteTable);
            geonoteTable.setTitle(geonoteTable.getTitle() + " - " + id);
            session.update(geonoteTable);
            transaction.commit();
        } finally {
            session.close();
        }
    }

    /**
     * Extracts a geonote from the database for a given id.
     * 
     * @param id the id of the geonote to search for.
     * @throws Exception
     */
    public GeonotesHandler( Long id ) throws Exception {
        Session session = openSession();
        try {
            Criteria criteria = session.createCriteria(GeonotesTable.class);
            criteria.add(Restrictions.eq(GEONOTESTABLE_ID_FIELD, id));
            Object result = criteria.uniqueResult();
            if (result != null) {
                geonoteTable = (GeonotesTable) result;
                this.id = id;
            } else {
                throw new IOException("Couldn't retrieve a geonote with id: " + id);
            }
        } finally {
            session.close();
        }
    }

    /**
     * Wraps a geonote.
     * 
     * @param geonotesTable the {@link GeonotesTable}.
     */
    public GeonotesHandler( GeonotesTable geonotesTable ) {
        this.geonoteTable = geonotesTable;
        this.id = geonotesTable.getId();
    }

    /**
     * Getter for the geonote's unique id.
     * 
     * @return the geonote's unique id.
     */
    public Long getId() {
        return id;
    }

    public Integer getType() {
        return geonoteTable.getType();
    }

    public Coordinate getPosition() {
        return new Coordinate(geonoteTable.getEast(), geonoteTable.getNorth());
    }

    public Double getAzimut() {
        return geonoteTable.getAzimut();
    }

    public DateTime getCreationDate() {
        return geonoteTable.getCreationDateTime();
    }

    public String getTitle() {
        return geonoteTable.getTitle();
    }

    public void setTitle( String title ) {
        geonoteTable.setTitle(title);
        notifyObservers(NOTIFICATION.TITLECHANGED);
    }

    public String getInfo() {
        return geonoteTable.getInfo();
    }

    public void setInfo( String info ) {
        geonoteTable.setInfo(info);
        notifyObservers(NOTIFICATION.TITLECHANGED);
    }

    public Integer getWidth() {
        return geonoteTable.getWidth();
    }

    public void setWidth( Integer width ) {
        geonoteTable.setWidth(width);
        notifyObservers(NOTIFICATION.SIZECHANGED);
    }

    public Integer getHeight() {
        return geonoteTable.getHeight();
    }

    public void setHeight( Integer height ) {
        geonoteTable.setHeight(height);
        notifyObservers(NOTIFICATION.SIZECHANGED);
    }

    public Color getColor( Display display ) {
        if (color == null) {
            String[] colorSplit = geonoteTable.getColor().split(":");
            int[] colorRGB = new int[]{Integer.parseInt(colorSplit[0]), Integer.parseInt(colorSplit[1]),
                    Integer.parseInt(colorSplit[2])};
            color = new Color(display, colorRGB[0], colorRGB[1], colorRGB[2]);
        }
        return color;
    }

    public String getColorString() {
        return geonoteTable.getColor();
    }

    public void setColor( Display display, String colorString ) {
        if (color != null) {
            color.dispose();
        }
        geonoteTable.setColor(colorString);
        String[] colorSplit = colorString.split(":");
        int[] colorRGB = new int[]{Integer.parseInt(colorSplit[0]), Integer.parseInt(colorSplit[1]),
                Integer.parseInt(colorSplit[2])};
        color = new Color(display, colorRGB[0], colorRGB[1], colorRGB[2]);

        notifyObservers(NOTIFICATION.STYLECHANGED);
    }
    
    public CoordinateReferenceSystem getCrs(){
        return geonoteTable.getGeonoteCrs();
    }

    /**
     * Extracts the {@link GeonotesTextareaTable} for this geonote from the database.
     * 
     * @return the {@link GeonotesTextareaTable} of the current geonote or null if none was found.
     */
    public GeonotesTextareaTable getGeonotesTextareaTable() {
        if (geonotesTextareaTable != null) {
            return geonotesTextareaTable;
        }
        Session session = openSession();
        try {
            Criteria criteria = session.createCriteria(GeonotesTextareaTable.class);
            criteria.add(Restrictions.eq(GEONOTESTABLE_EXTERNAL_KEY_ID, geonoteTable));
            List<GeonotesTextareaTable> resultsList = criteria.list();
            if (resultsList != null && resultsList.size() > 0) {
                geonotesTextareaTable = resultsList.get(0);
                return geonotesTextareaTable;
            }
            return null;
        } finally {
            session.close();
        }
    }

    /**
     * Extracts the {@link GeonotesDrawareaTable} for this geonote from the database.
     * 
     * @return the {@link GeonotesDrawareaTable} for this geonote.
     */
    public GeonotesDrawareaTable getGeonotesDrawareaTable() {
        if (geonotesDrawareaTable != null) {
            return geonotesDrawareaTable;
        }
        Session session = openSession();
        try {
            Criteria criteria = session.createCriteria(GeonotesDrawareaTable.class);
            criteria.add(Restrictions.eq(GEONOTESTABLE_EXTERNAL_KEY_ID, geonoteTable));
            List<GeonotesDrawareaTable> resultsList = criteria.list();
            if (resultsList != null && resultsList.size() > 0) {
                geonotesDrawareaTable = resultsList.get(0);
                return geonotesDrawareaTable;
            }
            return null;
        } finally {
            session.close();
        }
    }

    /**
     * Extracts the {@link GeonotesMediaboxTable}s for this geonote from the database.
     * 
     * @param mediaName the mediaName for which to retrieve the 
     *                  {@link GeonotesMediaboxTable} or null to get them all.
     * @return the {@link GeonotesMediaboxTable}s for this geonote.
     * @throws Exception
     */
    public List<GeonotesMediaboxTable> getGeonotesMediaboxTables( String mediaName ) throws Exception {
        Session session = openSession();
        try {
            Criteria criteria = session.createCriteria(GeonotesMediaboxTable.class);
            criteria.add(Restrictions.eq(GEONOTESTABLE_EXTERNAL_KEY_ID, geonoteTable));
            if (mediaName != null) {
                criteria.add(Restrictions.eq(GEONOTESMEDIA_NAME_FIELD, mediaName));
            }
            geonotesMediaboxTables = criteria.list();
            return geonotesMediaboxTables;
        } finally {
            session.close();
        }
    }

    /**
     * Extracts the {@link GeonotesMediaboxBlobsTable} for the supplied {@link GeonotesMediaboxTable}.
     * 
     * @return the {@link GeonotesMediaboxBlobsTable} for the supplied {@link GeonotesMediaboxTable}.
     */
    public GeonotesMediaboxBlobsTable getGeonotesMediaboxBlobsTable( GeonotesMediaboxTable geonotesMediaboxTable ) {
        Session session = openSession();
        try {
            Criteria criteria = session.createCriteria(GeonotesMediaboxBlobsTable.class);
            criteria.add(Restrictions.eq(GEONOTESMEDIA_EXTERNAL_KEY_ID, geonotesMediaboxTable));
            List<GeonotesMediaboxBlobsTable> resultsList = criteria.list();
            if (resultsList != null && resultsList.size() > 0) {
                GeonotesMediaboxBlobsTable geonotesMediaboxBlobsTable = resultsList.get(0);
                return geonotesMediaboxBlobsTable;
            }
            return null;
        } finally {
            session.close();
        }
    }

    /**
     * Persist everything needed and set to database.
     * 
     * <p>
     * Only the {@link GeonotesTable} needs to be persisted, since everything 
     * else is persisted as it comes in.
     * </p>
     */
    public void persistNote() {
        Session session = openSession();
        try {
            Transaction transaction = session.beginTransaction();
            session.saveOrUpdate(geonoteTable);
            transaction.commit();
        } finally {
            session.close();
        }
    }

    /**
     * Removes the current geonote and all related records from the database.
     * @throws Exception 
     */
    public void deleteNote() throws Exception {
        // delete mediaboxes
        List<GeonotesMediaboxTable> geonotesMediaboxTables = getGeonotesMediaboxTables(null);
        for( GeonotesMediaboxTable geonotesMediaboxTable : geonotesMediaboxTables ) {
            String mediaName = geonotesMediaboxTable.getMediaName();
            deleteMedia(mediaName);
        }

        Session session = openSession();
        try {
            // start batch delete
            Transaction transaction = session.beginTransaction();
            // remove drawarea
            Query query = session.createQuery("delete " + GeonoteConstants.DRAWAREATABLE_NAME
                    + " gda where gda.geonotesId = :geonotesId");
            query.setLong(GEONOTESTABLE_EXTERNAL_KEY_ID, id);
            query.executeUpdate();
            // remove properties
            query = session.createQuery("delete " + GeonoteConstants.TEXTAREATABLE_NAME
                    + " gta where gta.geonotesId = :geonotesId");
            query.setLong(GEONOTESTABLE_EXTERNAL_KEY_ID, id);
            query.executeUpdate();

            // remove the geonote itself
            query = session.createQuery("delete " + GeonoteConstants.GEONOTESTABLE_NAME + " g where g.id = :geonotesId");
            query.setLong(GEONOTESTABLE_EXTERNAL_KEY_ID, id);
            query.executeUpdate();

            transaction.commit();
        } finally {
            session.close();
        }
        notifyObservers(NOTIFICATION.NOTEREMOVED);
    }

    /**
     * Set the text of the {@link GeonotesTextareaTable} to a supplied value.
     * 
     * <p>If the textarea is not existing, it is created.</p>
     * 
     * @param text the text to save.
     */
    public void setTextarea( String text ) {
        if (text == null) {
            throw new IllegalArgumentException("text can't be null.");
        }

        GeonotesTextareaTable textareaTable = getGeonotesTextareaTable();
        if (textareaTable == null) {
            textareaTable = new GeonotesTextareaTable();
            textareaTable.setGeonotesId(geonoteTable);
        }
        textareaTable.setText(text);

        Session session = openSession();
        try {
            Transaction transaction = session.beginTransaction();
            session.saveOrUpdate(textareaTable);
            transaction.commit();
        } finally {
            session.close();
        }

    }

    /**
     * Set the drawings of the {@link GeonotesDrawareaTable} to a supplied value.
     * 
     * <p>If the drawarea is not existing, it is created.</p>
     * 
     * @param drawings the drawings to save.
     */
    public void setDrawarea( ArrayList<DressedStroke> drawings ) {
        if (drawings == null) {
            throw new IllegalArgumentException("drawings can't be null.");
        }

        GeonotesDrawareaTable drawareaTable = getGeonotesDrawareaTable();
        Session session = openSession();
        try {
            Transaction transaction = session.beginTransaction();
            if (drawareaTable == null) {
                drawareaTable = new GeonotesDrawareaTable();
                drawareaTable.setGeonotesId(geonoteTable);
            }
            drawareaTable.setDrawings(drawings);
            session.saveOrUpdate(drawareaTable);

            transaction.commit();
        } finally {
            session.close();
        }

    }

    /**
     * Add a media into the database.
     * 
     * @param mediaFile the file containing the media.
     * @param mediaName the name to use for lists.
     * @throws Exception
     */
    public void addMedia( File mediaFile, String mediaName ) throws Exception {

        if (mediaFile.isDirectory()) {
            // we do not store folders
            return;
        }

        Session session = openSession();
        Transaction transaction = session.beginTransaction();
        try {
            // create mediabox
            GeonotesMediaboxTable newMediaBox = new GeonotesMediaboxTable();
            newMediaBox.setGeonotesId(geonoteTable);
            newMediaBox.setMediaName(mediaName);

            // create media blobs
            GeonotesMediaboxBlobsTable newMediaBlobs = new GeonotesMediaboxBlobsTable();
            // add dummy drawings
            ArrayList<DressedStroke> dummyDrawings = new ArrayList<DressedStroke>();
            newMediaBlobs.setDrawings(dummyDrawings);
            newMediaBlobs.setMediaboxId(newMediaBox);
            newMediaBlobs.saveFileToMediabox(mediaFile);

            session.save(newMediaBox);
            session.save(newMediaBlobs);
            transaction.commit();
        } finally {
            session.close();
        }
    }

    /**
     * Move a media from one geonote to this.
     * 
     * @param mediaName the name to use for lists.
     * @param srcGeonoteTableId the geonote id from which to take the media file.
     * @throws Exception
     */
    public void moveMedia( String mediaName, long srcGeonoteTableId ) throws Exception {

        Session session = openSession();
        Transaction transaction = session.beginTransaction();
        try {
            Criteria criteria = session.createCriteria(GeonotesTable.class);
            criteria.add(Restrictions.eq(GEONOTESTABLE_ID_FIELD, srcGeonoteTableId));
            GeonotesTable srcGeonoteTable = (GeonotesTable) criteria.uniqueResult();
            if (srcGeonoteTable == null) {
                throw new IOException("Couldn't retrieve a geonote with id: " + id);
            }

            // extract media box
            criteria = session.createCriteria(GeonotesMediaboxTable.class);
            criteria.add(Restrictions.eq(GEONOTESTABLE_EXTERNAL_KEY_ID, srcGeonoteTable));
            criteria.add(Restrictions.eq(GEONOTESMEDIA_NAME_FIELD, mediaName));
            GeonotesMediaboxTable geonotesMediaboxTable = (GeonotesMediaboxTable) criteria.uniqueResult();

            // and simply change its parent
            geonotesMediaboxTable.setGeonotesId(geonoteTable);

            session.update(geonotesMediaboxTable);
            transaction.commit();
        } finally {
            session.close();
        }
    }

    /**
     * Delete a media from the database.
     * 
     * @param dndF the handler to the media.
     * @throws Exception
     */
    public void deleteMedia( DndFile dndF ) throws Exception {
        String mediaName = dndF.name;
        deleteMedia(mediaName);
    }

    /**
     * Delete a media from the database.
     * 
     * @param name the name of the media.
     * @throws Exception
     */
    public void deleteMedia( String mediaName ) throws Exception {
        Session session = openSession();
        try {
            Transaction transaction = session.beginTransaction();
            Criteria criteria = session.createCriteria(GeonotesMediaboxTable.class);
            criteria.add(Restrictions.eq(GEONOTESTABLE_EXTERNAL_KEY_ID, geonoteTable));
            criteria.add(Restrictions.eq(GEONOTESMEDIA_NAME_FIELD, mediaName));
            List<GeonotesMediaboxTable> resultList = criteria.list();
            GeonotesMediaboxTable mediabox = resultList.get(0);
            Long mediaboxId = mediabox.getId();
            // delete mediabox
            session.delete(mediabox);

            // delete blob
            Query query = session.createQuery("delete " + GeonoteConstants.MEDIABOXBLOBSTABLE_CLASSNAME
                    + " gb where gb.mediaboxId = :mediaboxId");
            query.setLong(GEONOTESMEDIA_EXTERNAL_KEY_ID, mediaboxId);
            query.executeUpdate();

            transaction.commit();
        } finally {
            session.close();
        }
    }

    /**
     * Set the drawings connected to a particular media.
     * 
     * @param drawings the drawings.
     * @param mediaName the name of the media to set the drawing of. This has 
     *              to refer to an existing media. 
     * @throws Exception
     */
    public void setMediaDrawings( ArrayList<DressedStroke> drawings, String mediaName ) throws Exception {
        if (drawings == null || mediaName == null) {
            throw new IllegalArgumentException("drawings/mediaName can't be null.");
        }

        List<GeonotesMediaboxTable> mediaboxTables = getGeonotesMediaboxTables(mediaName);
        if (mediaboxTables == null || mediaboxTables.size() == 0) {
            throw new IllegalArgumentException("Couldn't retrieve any mediabox for the name: " + mediaName);
        }

        GeonotesMediaboxBlobsTable mediaboxBlobsTable = getGeonotesMediaboxBlobsTable(mediaboxTables.get(0));
        mediaboxBlobsTable.setDrawings(drawings);

        Session session = openSession();
        try {
            Transaction transaction = session.beginTransaction();
            session.saveOrUpdate(mediaboxBlobsTable);
            transaction.commit();
        } finally {
            session.close();
        }
    }

    /**
     * The method to extract the files that are stored in a database.
     * 
     * @param folder the folder into which extract the media. 
     *                      If null, tmp is used
     * @param mediaName the name of the media to extract, as given by 
     *                      {@link GeonotesHandler#getMediaNames()}
     * @param drawingList the drawing objects, if there are any, are 
     *                      copied into this list. Can be null, in which case 
     *                      the drawings are not considered.
     * @return the {@link File} to the extracted media.
     */
    public File extractMediaToPath( File folder, String mediaName, List<DressedStroke> drawingList ) throws Exception {
        if (folder == null) {
            // extract to tmp
            String tmpDirPath = System.getProperty("java.io.tmpdir"); //$NON-NLS-1$
            folder = new File(tmpDirPath);
        }
        File file = new File(folder, mediaName);
        List<GeonotesMediaboxTable> mediaboxTables = getGeonotesMediaboxTables(mediaName);
        GeonotesMediaboxBlobsTable mediaboxBlobsTable = getGeonotesMediaboxBlobsTable(mediaboxTables.get(0));
        mediaboxBlobsTable.getFromMediaboxToFile(file);
        List<DressedStroke> drawings = mediaboxBlobsTable.getDrawings();
        drawingList.addAll(drawings);
        return file;
    }

    /**
     * Creates a boundary Envelope for the Geonote and its icon in the {@link MapGraphic}.
     * 
     * @param destinationCrs the {@linkplain CoordinateReferenceSystem crs} in which 
     *                      the envelope is needed.
     * @return the {@linkplain ReferencedEnvelope envelope} for the geonote.
     */
    public ReferencedEnvelope getBoundsAsReferenceEnvelope( CoordinateReferenceSystem destinationCrs ) {

        Coordinate position = new Coordinate(geonoteTable.getEast(), geonoteTable.getNorth());
        CoordinateReferenceSystem noteCrs = geonoteTable.getGeonoteCrs();

        Coordinate reprojectedCoordinate = position;

        if (!CRS.equalsIgnoreMetadata(noteCrs, destinationCrs)) {
            try {
                // transform coordinates before check
                MathTransform transform = CRS.findMathTransform(noteCrs, destinationCrs, true);
                // jts geometry
                com.vividsolutions.jts.geom.Point pt = gF.createPoint(new Coordinate(position.x, position.y));
                Geometry targetGeometry = JTS.transform(pt, transform);
                reprojectedCoordinate = targetGeometry.getCoordinate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // I need to reconstruct the pin size (16x16)
        Coordinate pixelSize = ApplicationGIS.getActiveMap().getViewportModel().getPixelSize();
        double pixelWidth = pixelSize.x;
        double pixelHeight = pixelSize.y;

        double deltaX = pixelWidth * (double) PINIMAGE_WIDTH;
        double deltaY = pixelHeight * (double) PINIMAGE_HEIGHT;
        Coordinate lowerLeft = new Coordinate(reprojectedCoordinate.x - deltaX, reprojectedCoordinate.y - deltaY);
        Coordinate upperRight = new Coordinate(reprojectedCoordinate.x + deltaX, reprojectedCoordinate.y + deltaY);
        ReferencedEnvelope refEnvelope = new ReferencedEnvelope(new Envelope(lowerLeft, upperRight), destinationCrs);

        return refEnvelope;
    }

    /**
     * Dump the contents of a geonote to disk.
     * 
     * @param folderPath the folder into which dump the geonotes data structure.
     * @param drawAreaImage 
     * @throws Exception
     */
    public void dumpNote( final String folderPath, final Image drawAreaImage ) throws Exception {

        IRunnableWithProgress operation = new IRunnableWithProgress(){

            public void run( IProgressMonitor pm ) throws InvocationTargetException, InterruptedException {

                try {
                    pm.beginTask("Dump geonote to disk.", 2);
                    File folderFile = new File(folderPath);
                    if (!folderFile.exists()) {
                        throw new InterruptedException("The supplied folder " + folderPath
                                + " doesn't exist. Can't dump the geonote into it.");
                    }

                    String title = geonoteTable.getTitle();
                    title = title.replace(' ', '_');
                    File mainDir = new File(folderFile.getAbsolutePath() + File.separator + title);
                    if (!mainDir.exists() && !mainDir.mkdir()) {
                        throw new InterruptedException("Coudn't create folder: " + mainDir.getAbsolutePath());
                    }

                    // dump info text
                    try {
                        File textFile = new File(mainDir.getAbsolutePath() + File.separator + "geonote_info.txt");
                        BufferedWriter bW = new BufferedWriter(new FileWriter(textFile));

                        StringBuilder msgBuilder = new StringBuilder();
                        msgBuilder.append("Geonote id: ");
                        msgBuilder.append(id);
                        msgBuilder.append("\n\n");
                        msgBuilder.append("Geonote title: ");
                        msgBuilder.append(title);
                        msgBuilder.append("\n\n");
                        msgBuilder.append("Geonote color: ");
                        String colorString = getColorString();
                        msgBuilder.append("r:g:b:a = ");
                        msgBuilder.append(colorString);
                        msgBuilder.append("\n\n");
                        msgBuilder.append("Geonote type: ");
                        msgBuilder.append(getType());
                        msgBuilder.append("\n\n");
                        msgBuilder.append("Creation date: ");
                        DateTime creationDateTime = geonoteTable.getCreationDateTime();
                        msgBuilder.append(creationDateTime.toString(BeegisUtilsPlugin.dateTimeFormatterYYYYMMDDHHMM));
                        msgBuilder.append("\n\n");
                        msgBuilder.append("Position (WGS 84 lat/long): ");
                        Coordinate position = new Coordinate(geonoteTable.getEast(), geonoteTable.getNorth());
                        msgBuilder.append(position.x);
                        msgBuilder.append(" / ");
                        msgBuilder.append(position.y);
                        msgBuilder.append("\n\n");
                        
                        IMap activeMap = ApplicationGIS.getActiveMap();
                        if(activeMap != null){
                            CoordinateReferenceSystem mapCrs = activeMap.getViewportModel().getCRS();
                            String crsName = mapCrs.getName().toString();
                            msgBuilder.append("Position ("+crsName+"): ");
                            MathTransform mathTransform = CRS.findMathTransform(DefaultGeographicCRS.WGS84, mapCrs, true);
                            Coordinate newPosition = JTS.transform(position, null, mathTransform);
                            msgBuilder.append(newPosition.x);
                            msgBuilder.append(" / ");
                            msgBuilder.append(newPosition.y);
                            msgBuilder.append("\n\n");
                        }
                        bW.write(msgBuilder.toString());
                        bW.close();
                        pm.worked(1);
                    } catch (Exception e) {
                        e.printStackTrace();
                        GeonotesPlugin.log("GeonotesPlugin problem: eu.hydrologis.jgrass.geonotes##run", e); //$NON-NLS-1$
                    }

                    // dump text
                    try {
                        File textFile = new File(mainDir.getAbsolutePath() + File.separator + "geonote_textbox.txt");
                        BufferedWriter bW = new BufferedWriter(new FileWriter(textFile));
                        GeonotesTextareaTable textareaTable = getGeonotesTextareaTable();
                        String text = "";
                        if (textareaTable != null) {
                            String tmptext = textareaTable.getText();
                            if (text != null) {
                                text = tmptext;
                            }
                        }
                        bW.write(text);
                        bW.close();
                        pm.worked(1);
                    } catch (Exception e) {
                        e.printStackTrace();
                        GeonotesPlugin.log("GeonotesPlugin problem: eu.hydrologis.jgrass.geonotes##run", e); //$NON-NLS-1$
                    }

                    try {
                        // dump image
                        File imageFile = new File(mainDir.getAbsolutePath() + File.separator + "geonote_paintbox.png");
                        if (drawAreaImage != null) {
                            OutputStream out = new FileOutputStream(imageFile);
                            ImageLoader imageLoader = new ImageLoader();
                            imageLoader.data = new ImageData[]{drawAreaImage.getImageData()};
                            imageLoader.save(out, SWT.IMAGE_PNG);
                            out.close();
                        }

                        pm.worked(1);
                    } catch (Exception e) {
                        e.printStackTrace();
                        GeonotesPlugin.log("GeonotesPlugin problem: eu.hydrologis.jgrass.geonotes##run", e); //$NON-NLS-1$
                    }
                    pm.done();

                    // dump media box
                    File mediaDir = new File(mainDir.getAbsolutePath() + File.separator + "geonote_mediabox");
                    if (!mediaDir.exists() && !mediaDir.mkdir()) {
                        throw new InterruptedException("Coudn't create folder: " + mediaDir.getAbsolutePath());
                    }

                    List<GeonotesMediaboxTable> mediaboxTables = getGeonotesMediaboxTables(null);
                    pm.beginTask("Dump media box to disk.", mediaboxTables.size());
                    for( GeonotesMediaboxTable mediaboxTable : mediaboxTables ) {
                        GeonotesMediaboxBlobsTable mediaboxBlobsTable = getGeonotesMediaboxBlobsTable(mediaboxTable);
                        String mediaName = mediaboxTable.getMediaName();

                        File file = new File(mediaDir, mediaName);
                        mediaboxBlobsTable.getFromMediaboxToFile(file);

                        pm.worked(1);
                    }
                    pm.done();
                } catch (Exception e) {
                    GeonotesPlugin.log("GeonotesPlugin problem: eu.hydrologis.jgrass.geonotes##run", e); //$NON-NLS-1$
                    e.printStackTrace();
                    throw new InterruptedException(e.getLocalizedMessage());
                }

            }

        };

        PlatformGIS.runInProgressDialog("Dump geonote", true, operation, true);

    }
    /**
     * Dump the contents of a geonote to disk in binary mode, usefull for sending or exporting.
     * 
     * <p>
     * When a geonote is dumped in binary mode, the following folder structure is created:
     * <pre>
     * geonotes(_nn)       (where _nn is added as index if the folder exists at dump time)
     *      |
     *      |- 0    (for first note)
     *      |  |-- geonote.bin (binary geonote serialized file see BinaryGeonote)
     *      |  `-- media
     *      |       |-- media1
     *      |       |-- media1.drawing (if it is an image and has drawing
     *      |       |-- media2
     *      |       |-- media3
     *      |       |-- ...
     *      |       `-- median
     *      |
     *      |- 1    (for second note)
     *      |  |-- geonote.bin 
     *      |  `-- media
     *      |       |-- media1
     *      |       |-- media1.drawing (if it is an image and has drawing
     *      |       `-- ...
     *      |...
     *      |
     *      `- n
     * </pre>
     * and the folder is then compressed.
     * </p>
     * 
     * @param folderPath the folder into which dump the geonotes data structure.
     */
    public void dumpBinaryNote( final String folderPath ) throws Exception {

        IRunnableWithProgress operation = new IRunnableWithProgress(){

            public void run( IProgressMonitor pm ) throws InvocationTargetException, InterruptedException {

                try {

                    File folderFile = new File(folderPath);
                    if (!folderFile.exists()) {
                        throw new InterruptedException("The supplied folder " + folderPath
                                + " doesn't exist. Can't dump the geonote into it.");
                    }

                    /*
                     * dump note to disk
                     */
                    GeonotesTextareaTable textareaTable = getGeonotesTextareaTable();
                    String text = "";
                    if (textareaTable != null) {
                        String tmp = textareaTable.getText();
                        if (tmp != null) {
                            text = tmp;
                        }
                    }
                    GeonotesDrawareaTable drawareaTable = getGeonotesDrawareaTable();
                    DressedStroke[] drawingArray = new DressedStroke[0];
                    if (drawareaTable != null) {
                        List<DressedStroke> drawing = drawareaTable.getDrawings();
                        drawingArray = (DressedStroke[]) drawing.toArray(new DressedStroke[drawing.size()]);
                    }

                    // the geonote
                    File dumpFile = new File(folderFile, GEONOTE_BIN_PROPERTIES);
                    FileOutputStream fos = new FileOutputStream(dumpFile);
                    ObjectOutputStream out = new ObjectOutputStream(fos);
                    out.writeObject(geonoteTable);
                    out.close();
                    fos.close();

                    // dressedstrokes
                    dumpFile = new File(folderFile, GEONOTE_BIN_DRAWINGS);
                    fos = new FileOutputStream(dumpFile);
                    out = new ObjectOutputStream(fos);
                    out.writeObject(drawingArray);
                    out.close();
                    fos.close();
                    pm.worked(1);

                    // text
                    dumpFile = new File(folderFile, GEONOTE_BIN_TEXT);
                    BufferedWriter bW = new BufferedWriter(new FileWriter(dumpFile));
                    bW.write(text);
                    bW.close();

                    /*
                     * dump media content to disk
                     */
                    List<GeonotesMediaboxTable> mediaboxTables = getGeonotesMediaboxTables(null);

                    if (mediaboxTables != null) {
                        pm.beginTask("Export geonote: " + getTitle(), mediaboxTables.size() + 1);
                        File mediaFolder = new File(folderFile, MEDIA_FOLDER);
                        if (mediaFolder.mkdir()) {
                            for( GeonotesMediaboxTable mediaBox : mediaboxTables ) {
                                List<DressedStroke> drawingList = new ArrayList<DressedStroke>();

                                // TODO change the following method
                                String mediaName = mediaBox.getMediaName();
                                extractMediaToPath(mediaFolder, mediaName, drawingList);

                                // dump drawings on media if they exist
                                if (drawingList.size() > 0) {
                                    DressedStroke[] drawingsArray = (DressedStroke[]) drawingList
                                            .toArray(new DressedStroke[drawingList.size()]);
                                    File drawingFile = new File(mediaFolder, mediaName + DRAWING_EXTENTION);
                                    FileOutputStream foStream = new FileOutputStream(drawingFile);
                                    ObjectOutputStream outStream = new ObjectOutputStream(foStream);
                                    outStream.writeObject(drawingsArray);
                                    outStream.close();
                                    foStream.close();
                                }
                            }
                            pm.worked(1);
                        }
                        pm.done();
                    }
                } catch (Exception e) {
                    GeonotesPlugin.log("GeonotesPlugin problem: eu.hydrologis.jgrass.geonotes##dumpBinaryNote", e); //$NON-NLS-1$
                    e.printStackTrace();
                    throw new InterruptedException(e.getLocalizedMessage());
                }

            }

        };

        PlatformGIS.runInProgressDialog("Export geonote", true, operation, false);

    }

    /**
     * @return all the {@link GeonotesTable}s wrapped in {@link GeonotesHandler}s.
     */
    public static List<GeonotesHandler> getGeonotesHandlers() {
        Session session = null;
        try {
            session = DatabasePlugin.getDefault().getActiveDatabaseConnection().openSession();
            List<GeonotesHandler> geonotesHandlers = new ArrayList<GeonotesHandler>();
            Criteria criteria = session.createCriteria(GeonotesTable.class);
            List<GeonotesTable> resultsList = criteria.list();
            for( int i = 0; i < resultsList.size(); i++ ) {
                geonotesHandlers.add(new GeonotesHandler(resultsList.get(i)));
            }
            return geonotesHandlers;
        } catch (Exception e) {
            String message = "An error occurred while connecting to the database";
            ExceptionDetailsDialog.openError(null, message, IStatus.ERROR, GeonotesPlugin.PLUGIN_ID, e);
            e.printStackTrace();
            return Collections.EMPTY_LIST;
        } finally {
            session.close();
        }
    }

    /**
     * @return all the {@link GeonotesTable}s.
     * @throws Exception 
     */
    public static List<GeonotesTable> getGeonotesTables() throws Exception {
        Session session = null;
        try {
            session = DatabasePlugin.getDefault().getActiveDatabaseConnection().openSession();
            Criteria criteria = session.createCriteria(GeonotesTable.class);
            List<GeonotesTable> resultsList = criteria.list();
            return resultsList;
        } finally {
            session.close();
        }
    }

    /**
     * Fetches a gps coordinate from the database nearest to a supplied time and date.
     * 
     * @param dateTime the time to search for.
     * @return the coordinate of the nearest time.
     * @throws Exception
     */
    public static Coordinate getGpsCoordinateForTimeStamp( DateTime dateTime, int minutesThreshold ) throws Exception {
        DateTime from = dateTime.minusMinutes(minutesThreshold);
        DateTime to = dateTime.plusMinutes(minutesThreshold);

        Session session = null;
        try {
            session = DatabasePlugin.getDefault().getActiveDatabaseConnection().openSession();
            Criteria criteria = session.createCriteria(GpsLogTable.class);
            String utcTimeStr = "utcTime";
            criteria.add(between(utcTimeStr, from, to));
            criteria.addOrder(asc(utcTimeStr));

            List<GpsLogTable> resultsList = criteria.list();
            for( int i = 0; i < resultsList.size() - 1; i++ ) {
                
                GpsLogTable gpsLog1 = resultsList.get(i);
                GpsLogTable gpsLog2 = resultsList.get(i + 1);

                DateTime utcTimeBefore = gpsLog1.getUtcTime();
                DateTime utcTimeAfter = gpsLog2.getUtcTime();

                Interval interval = new Interval(utcTimeBefore, utcTimeAfter);
                if (interval.contains(dateTime)) {
                    // take the nearest
                    Interval intervalBefore = new Interval(utcTimeBefore, dateTime);
                    Interval intervalAfter = new Interval(dateTime, utcTimeAfter);
                    long beforeMillis = intervalBefore.toDurationMillis();
                    long afterMillis = intervalAfter.toDurationMillis();
                    if (beforeMillis < afterMillis) {
                        Coordinate coord = new Coordinate(gpsLog1.getEast(), gpsLog1.getNorth());
                        return coord;
                    } else {
                        Coordinate coord = new Coordinate(gpsLog2.getEast(), gpsLog2.getNorth());
                        return coord;
                    }
                }

            }
        } finally {
            session.close();
        }
        return null;
    }

    public void addObserver( GeonotesObserver observer ) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver( GeonotesObserver observer ) {
        if (observers.contains(observer)) {
            observers.remove(observer);
        }
    }

    public void notifyObservers( Object arg ) {
        for( GeonotesObserver observer : observers ) {
            observer.updateFromGeonotes(this, arg);
        }
    }

    public boolean equals( Object obj ) {
        if (obj instanceof GeonotesHandler) {
            GeonotesHandler geonoteHandler = (GeonotesHandler) obj;
            return getId().equals(geonoteHandler.getId());
        }
        return false;
    }

    public int hashCode() {
        return getId().intValue();
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Geonote ID: ");
        stringBuilder.append(getId());
        stringBuilder.append("\n");
        stringBuilder.append("Title: ");
        stringBuilder.append(getTitle());
        stringBuilder.append("\n");
        stringBuilder.append("Easting: ");
        stringBuilder.append(geonoteTable.getEast());
        stringBuilder.append("\n");
        stringBuilder.append("Northing: ");
        stringBuilder.append(geonoteTable.getNorth());
        stringBuilder.append("\n");
        stringBuilder.append("Info: ");
        stringBuilder.append(getInfo());
        stringBuilder.append("\n");
        stringBuilder.append("type: ");
        stringBuilder.append(geonoteTable.getType());
        stringBuilder.append("\n");
        stringBuilder.append("Has color RGBA: ");
        stringBuilder.append(getColorString());
        String str = stringBuilder.toString();
        return str;
    }
}
