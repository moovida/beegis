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
package eu.hydrologis.jgrass.gpsnmea;

import java.util.HashMap;
import java.util.Observer;

import net.refractions.udig.project.IMap;
import net.refractions.udig.project.ui.ApplicationGIS;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.osgi.framework.BundleContext;

import eu.hydrologis.jgrass.gpsnmea.actions.AutomaticAddPoint;
import eu.hydrologis.jgrass.gpsnmea.actions.GeonoteAdd;
import eu.hydrologis.jgrass.gpsnmea.actions.ManualAddPoint;
import eu.hydrologis.jgrass.gpsnmea.actions.ToggleLoggingGps;
import eu.hydrologis.jgrass.gpsnmea.actions.ZoomToGps;
import eu.hydrologis.jgrass.gpsnmea.db.DatabaseManager;
import eu.hydrologis.jgrass.gpsnmea.gps.AbstractGps;
import eu.hydrologis.jgrass.gpsnmea.gps.GpsPoint;
import eu.hydrologis.jgrass.gpsnmea.gps.NmeaGpsImpl;
import eu.hydrologis.jgrass.gpsnmea.preferences.pages.PreferenceConstants;
import eu.hydrologis.jgrass.gpsnmea.views.GpsView;

/**
 * The activator class controls the plug-in life cycle
 */
public class GpsActivator extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "eu.hydrologis.jgrass.gpsnmea"; //$NON-NLS-1$

    // The shared instance
    private static GpsActivator plugin;

    private AbstractGps gpsImpl;

    private IViewPart activeGpsView;

    /**
     * True when the gps is creating a feature layer in automatic mode.
     */
    private boolean isInAutomaticMode = false;

    /**
     * The constructor
     */
    public GpsActivator() {
        plugin = this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    public void start( BundleContext context ) throws Exception {
        super.start(context);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    public void stop( BundleContext context ) throws Exception {
        plugin = null;
        getPreferenceStore().setValue(PreferenceConstants.GPS_IS_ON, false);

        super.stop(context);
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static GpsActivator getDefault() {
        return plugin;
    }

    /**
     * Delegate for starting the Gps connection
     * 
     * @return true if gps connected
     */
    public boolean startGps() {
        if (gpsImpl != null && gpsImpl.isGpsConnected()) {
            // if connected already, stop it and restart, the params could have
            // changed
            gpsImpl.doLogging(false);
            gpsImpl.stopGps();
        }
        IPreferenceStore prefs = GpsActivator.getDefault().getPreferenceStore();
        final String portString = prefs.getString(PreferenceConstants.PORTUSED);
        final String waitString = prefs.getString(PreferenceConstants.MAXWAIT);
        final String baudRateString = prefs.getString(PreferenceConstants.BAUDRATE);
        final String dataBitString = prefs.getString(PreferenceConstants.DATABIT);
        final String stopBitString = prefs.getString(PreferenceConstants.STOPBIT);
        final String parityBitString = prefs.getString(PreferenceConstants.PARITYBIT);
        final boolean testmodeString = prefs.getBoolean(PreferenceConstants.TESTMODE);
        gpsImpl = new NmeaGpsImpl(portString, waitString, baudRateString, dataBitString, stopBitString, parityBitString,
                testmodeString);
        if (!gpsImpl.startGps()) {
            return false;
        }
        return true;
    }

    /**
     * Delegate for stopping the Gps connection
     * 
     * @return true if gps disconnected properly
     */
    public boolean stopGps() {
        if (gpsImpl != null) {
            return gpsImpl.stopGps();
        }
        return false;
    }

    /**
     * Delegate for starting Gps logging
     */
    public void startGpsLogging() {
        if (gpsImpl != null && gpsImpl.isGpsConnected()) {
            gpsImpl.doLogging(true);
            try {
                // try to open the Gps view
                IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                activeGpsView = activePage.showView(GpsView.ID);
            } catch (Exception e) {
            }
        }
    }

    /**
     * Delegate for stopping Gps logging
     */
    public void stopGpsLogging() {
        if (gpsImpl != null && gpsImpl.isGpsConnected()) {
            gpsImpl.doLogging(false);
            try {
                // try to hide the gps view
                IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                if (activeGpsView != null)
                    activePage.hideView(activeGpsView);
            } catch (Exception e) {
            }
        }
    }

    /**
     * Checks if the gps is connected to the operating system.
     * 
     * @return true if the gps is connected properly.
     */
    public boolean isGpsConnected() {
        if (gpsImpl != null)
            return gpsImpl.isGpsConnected();
        return false;
    }

    /**
     * Checks if the gps is logging, i.e. the user activated it in the 
     * gui and sees it blink. 
     * 
     * @return true if the gps is logging.
     */
    public boolean isGpsLogging() {
        if (gpsImpl != null)
            return gpsImpl.isGpsLogging();
        return false;
    }

    /**
     * Flag that tells if the automatic feature layer creation is on.
     * 
     * @return true if the automatic mode is on.
     */
    public boolean isInAutomaticMode() {
        return isInAutomaticMode;
    }

    /**
     * Flag to set if the automatic feature layer creation is on.
     */
    public void setInAutomaticMode( boolean isInAutomaticMode ) {
        this.isInAutomaticMode = isInAutomaticMode;
    }

    /**
     * Retrieves the next available Gps point.
     * 
     * @return the {@link GpsPoint}. This can be null in case 
     *                  it was decided that it is invalid.
     * @throws Exception
     */
    public GpsPoint getNextGpsPoint() throws Exception {
        IMap activeMap = ApplicationGIS.getActiveMap();
        CoordinateReferenceSystem mapCrs = activeMap.getViewportModel().getCRS();
        GpsPoint returnGpsPoint = gpsImpl.getCurrentGpsPoint(mapCrs);
        return returnGpsPoint;
    }

    public void addObserverToGps( Observer observer ) {
        if (gpsImpl != null) {
            gpsImpl.addObserver(observer);
        }
    }

    public void removeObserverFromGps( Observer observer ) {
        if (gpsImpl != null) {
            gpsImpl.deleteObserver(observer);
        }
    }

    /**
     * Logs the Throwable in the plugin's log.
     * <p>
     * This will be a user visable ERROR iff:
     * <ul>
     * <li>t is an Exception we are assuming it is human readable or if a message is provided
     */
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

    /*
     * actions
     */
    private HashMap<String, IAction> actionMap = new HashMap<String, IAction>();

    private DatabaseManager databaseManager;

    public void registerAction( IAction action ) {
        String id = action.getId();
        actionMap.put(id, action);
    }

    public IAction getAction( String id ) {
        IAction action = actionMap.get(id);
        return action;
    }

    public AutomaticAddPoint getAutomaticAddPointAction() {
        AutomaticAddPoint automaticAction = (AutomaticAddPoint) GpsActivator.getDefault().getAction(AutomaticAddPoint.ID);
        return automaticAction;

    }
    public GeonoteAdd getGeonoteAddAction() {
        GeonoteAdd geonoteAddAction = (GeonoteAdd) GpsActivator.getDefault().getAction(GeonoteAdd.ID);
        return geonoteAddAction;
    }

    public ManualAddPoint getManualAddPointAction() {
        ManualAddPoint manualAddAction = (ManualAddPoint) GpsActivator.getDefault().getAction(ManualAddPoint.ID);
        return manualAddAction;
    }

    public ZoomToGps getZoomToGpsAction() {
        ZoomToGps zoomAction = (ZoomToGps) GpsActivator.getDefault().getAction(ZoomToGps.ID);
        return zoomAction;
    }

    public ToggleLoggingGps getToggleLoggingAction() {
        ToggleLoggingGps toggleAction = (ToggleLoggingGps) GpsActivator.getDefault().getAction(ToggleLoggingGps.ID);
        return toggleAction;
    }

    public DatabaseManager getDatabaseManager() {
        if (databaseManager == null) {
            databaseManager = new DatabaseManager();
        }
        return databaseManager;
    }
}
