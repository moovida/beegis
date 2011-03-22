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
package eu.hydrologis.jgrass.annotationlayer;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.refractions.udig.project.ui.ApplicationGIS;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.hibernate.Criteria;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;
import org.osgi.framework.BundleContext;

import eu.hydrologis.jgrass.beegisutils.database.BeegisTablesUpdater;
import eu.hydrologis.jgrass.beegisutils.database.annotatedclasses.AnnotationsTable;
import eu.hydrologis.jgrass.beegisutils.jgrassported.DressedWorldStroke;
import eu.hydrologis.jgrass.database.DatabasePlugin;
import eu.hydrologis.jgrass.database.core.DatabaseConnectionProperties;
import eu.hydrologis.jgrass.database.earlystartup.AnnotatedClassesCollector;
import eu.hydrologis.jgrass.database.interfaces.IDatabaseConnection;
import eu.hydrologis.jgrass.database.interfaces.IDatabaseEventListener;

/**
 * The activator class controls the plug-in life cycle
 */
public class AnnotationPlugin extends AbstractUIPlugin implements IDatabaseEventListener {

    private static final long ANNOTATIONSID = 1l;

    // The plug-in ID
    public static final String PLUGIN_ID = "eu.hydrologis.jgrass.annotationlayer"; //$NON-NLS-1$

    // The shared instance
    private static AnnotationPlugin plugin;

    private List<DressedWorldStroke> strokes = new ArrayList<DressedWorldStroke>();

    private int currentStrokeStyle = 6;

    private int currentStrokeWidth = 2;

    private double currentScale = -1.0;

    private int[] currentStrokeColor = new int[]{255, 0, 0, 255};

    public AnnotationPlugin() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    public void start( BundleContext context ) throws Exception {
        super.start(context);
        plugin = this;

        /*
         * check out the lines, if there are some
         */
        // make sure the classes were already mapped
        AnnotatedClassesCollector.getAnnotatedClassesList();
        BeegisTablesUpdater.checkSchema();
        resetStrokes();
        
        DatabasePlugin.getDefault().addDatabaseEventListener(plugin);
    }

    /**
     * Reads the strokes from the database from scratch.
     * 
     *  <p>Is needed for example on database change.
     * 
     * @throws Exception
     */
    public void resetStrokes() throws Exception {
        List<DressedWorldStroke> drawLines = getDrawLines();
        if (drawLines != null && drawLines.size() > 0) {
            setStrokes(drawLines);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    public void stop( BundleContext context ) throws Exception {
        plugin = null;
        // System.out.println("Lines before exit: " + strokes.size());
        // saveAnnotations();
        super.stop(context);
    }

    /**
     * save the annotations to the database
     * @throws Exception 
     */
    public void saveAnnotations() throws Exception {

        IDatabaseConnection databaseConnection = DatabasePlugin.getDefault().getActiveDatabaseConnection();
        if (databaseConnection == null) {
            throw new IOException("Database not connected");
        }
        Session session = databaseConnection.openSession();
        Transaction transaction = session.beginTransaction();

        AnnotationsTable annotations = new AnnotationsTable();
        annotations.setId(ANNOTATIONSID);
        annotations.setAnnotationDrawings(strokes);

        session.saveOrUpdate(annotations);
        transaction.commit();
        session.close();
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static AnnotationPlugin getDefault() {
        return plugin;
    }

    public void addStroke( DressedWorldStroke stroke ) {
        strokes.add(stroke);
    }

    public List<DressedWorldStroke> getStrokes() {
        return strokes;
    }

    public void setStrokes( List<DressedWorldStroke> strokes ) {
        this.strokes = strokes;
    }

    public static void log( String message2, Throwable t ) {
        if (getDefault() == null) {
            t.printStackTrace();
            return;
        }
        String message = message2;
        if (message == null)
            message = ""; //$NON-NLS-1$
        int status = t instanceof Exception || message != null ? IStatus.ERROR : IStatus.WARNING;
        getDefault().getLog().log(new Status(status, PLUGIN_ID, IStatus.OK, message, t));
    }

    public int getCurrentStrokeStyle() {
        return currentStrokeStyle;
    }

    public void setCurrentStrokeStyle( int currentStrokeStyle ) {
        this.currentStrokeStyle = currentStrokeStyle;
    }

    public int getCurrentStrokeWidth() {
        return currentStrokeWidth;
    }

    public void setCurrentStrokeWidth( int currentStrokeWidth ) {
        this.currentStrokeWidth = currentStrokeWidth;
    }

    public Color getCurrentStrokeColor() {
        Color c = new Color(currentStrokeColor[0], currentStrokeColor[1], currentStrokeColor[2], currentStrokeColor[3]);
        return c;
    }

    public int[] getCurrentStrokeColorInt() {
        return currentStrokeColor;
    }

    public void setCurrentStrokeColor( Color c ) {
        this.currentStrokeColor = new int[]{c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()};
    }

    public double getCurrentScale() {
        return currentScale;
    }

    public void setCurrentScale( double currentScale ) {
        this.currentScale = currentScale;
    }

    @SuppressWarnings("unchecked")
    private List<DressedWorldStroke> getDrawLines() throws Exception {
        List<DressedWorldStroke> lines = null;
        IDatabaseConnection databaseConnection = DatabasePlugin.getDefault().getActiveDatabaseConnection();
        if (databaseConnection == null) {
            throw new IOException("Database not connected");
        }
        Session session = databaseConnection.openSession();
        try {

            Criteria criteria = session.createCriteria(AnnotationsTable.class);
            List<AnnotationsTable> annotationsList = criteria.list();

            if (annotationsList.size() == 1) {
                AnnotationsTable annotations = annotationsList.get(0);
                lines = annotations.getAnnotationDrawings();
            } else {
                lines = new ArrayList<DressedWorldStroke>();
            }
        } finally {
            session.close();
        }
        return lines;
    }

    @Override
    public void onDatabaseOpened( DatabaseConnectionProperties connectionProperties ) {
        // need to reread the strokes
        try {
            resetStrokes();
            ApplicationGIS.getActiveMap().getRenderManager().refresh(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDatabaseClosed( DatabaseConnectionProperties connectionProperties ) {
        
    }

}
