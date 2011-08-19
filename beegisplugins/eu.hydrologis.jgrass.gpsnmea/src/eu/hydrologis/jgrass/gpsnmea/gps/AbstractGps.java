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
package eu.hydrologis.jgrass.gpsnmea.gps;

import gnu.io.CommPortIdentifier;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * The abstraction for Gps
 * 
 * @author Andrea Antonello - www.hydrologis.com
 */
public abstract class AbstractGps {

    /**
     * the list of {@link IGpsObserver} that listen to this Gps Implementation
     */
    protected List<IGpsObserver> observers = null;

    /**
     * defines the state of the Gps, true = connected
     */
    protected boolean gpsIsConnected = false;

    /**
     * defines the state of the Gps, true = logging
     */
    protected boolean gpsIsLogging = false;

    /**
     * defines the state of the Gps, true = gps implementation returns dummy
     * testdata in
     * {@link AbstractGps#getCurrentGpsPoint(CoordinateReferenceSystem, CoordinateReferenceSystem)},
     * while the {@link AbstractGps#getCurrentGpsData()} method will not be
     * usable.
     */
    protected boolean isTestmode = false;

    /**
     * start the gps for data acquisition
     */
    public abstract boolean startGps();

    /**
     * stop the Gps and connection related stuff
     */
    public abstract boolean stopGps();

    /**
     * Toggle logging of gps data.
     * 
     * @param doLog
     *                true to start logging, false to stop
     */
    public abstract void doLogging( boolean doLog );

    /**
     * @return the state of the gps, true = connected
     */
    public abstract boolean isGpsConnected();

    /**
     * @return the state of the gps, true = logging
     */
    public abstract boolean isGpsLogging();

    /**
     * This assumes that the gps internal system is always {@link DefaultGeographicCRS#WGS84}.
     * 
     * @param destinationCrs
     *                the crs to convert to, if null, no conversion is done
     * @return the gpsPoint object
     * @throws Exception 
     */
    public abstract GpsPoint getCurrentGpsPoint( CoordinateReferenceSystem destinationCrs ) throws Exception;

    /**
     * @return a String representing the current representative Gps data
     *         available
     */
    public abstract String getCurrentGpsData();

    /**
     * checks for available serial ports
     * 
     * @return the names of the available ports
     */
    public static synchronized String[][] checkGpsPorts() {
        try {
            Enumeration< ? > ports = CommPortIdentifier.getPortIdentifiers();
            if (ports == null) {
                return new String[][]{{""}, {""}};
            }

            ArrayList<String> portsList = new ArrayList<String>();

            while( ports.hasMoreElements() ) {

                CommPortIdentifier portIdentifier = ((CommPortIdentifier) ports.nextElement());
                // take only serial ports
                if (portIdentifier.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                    portsList.add(portIdentifier.getName());
                }

                System.out.println(portIdentifier.getName());
            }

            String[][] choiseStrings = new String[portsList.size()][2];
            for( int i = 0; i < portsList.size(); i++ ) {
                choiseStrings[i][0] = portsList.get(i);
                choiseStrings[i][1] = portsList.get(i);
            }

            return choiseStrings;
        } catch (Exception e) {
            e.printStackTrace();
            return new String[][]{{""}, {""}};
        }
    }

    public void addObserver( IGpsObserver observer ) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void deleteObserver( IGpsObserver observer ) {
        if (observers.contains(observer)) {
            observers.remove(observer);
        }
    }

}