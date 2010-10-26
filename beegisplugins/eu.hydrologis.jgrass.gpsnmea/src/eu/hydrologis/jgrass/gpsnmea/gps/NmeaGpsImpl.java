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

import eu.hydrologis.jgrass.gpsnmea.GpsActivator;
import eu.hydrologis.jgrass.gpsnmea.preferences.pages.PreferenceConstants;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observer;

import net.refractions.udig.project.IMap;
import net.refractions.udig.project.ui.ApplicationGIS;
import net.refractions.udig.ui.ExceptionDetailsDialog;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.preference.IPreferenceStore;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Implementation of a NMEA GPS reader.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class NmeaGpsImpl extends AbstractGps implements SerialPortEventListener, Runnable {

    private static final String MAXWAIT_DEF = "5000"; //$NON-NLS-1$
    private static final String BAUDRATE_DEF = "4800"; //$NON-NLS-1$
    private static final String DATABIT_DEF = "8"; //$NON-NLS-1$
    private static final String STOPBIT_DEF = "1"; //$NON-NLS-1$
    private static final String PARITYBIT_DEF = "0"; //$NON-NLS-1$
    private SerialPort port;
    private String currentGPGGAsentence = null;
    private String currentGPRMCsentence = null;
    private final String portString;
    private final String waitString;
    private final String baudRateString;
    private final String dataBitString;
    private final String stopBitString;
    private final String parityBitString;

    /**
     * the log of all the points taken from the gps in a session
     */
    private final IPreferenceStore prefs;
    private final GpsArtist gpsArtist;
    private CoordinateReferenceSystem mapCrs;
    private DummyNmea dummyNmea;
    private InputStream commPortInputStream;

    /**
     * Implementation of the Nmea Gps.
     * 
     * @param portString
     *            the serial port to which the gps is attached
     * @param waitString
     *            if null 5000 is used
     * @param baudRateString
     *            if null 4800 is used
     * @param dataBitString
     *            if null 8 is used
     * @param stopBitString
     *            if null 1 is used
     * @param parityBitString
     *            if null 0 is used
     * @param isTestmode
     *            test mode for the case no real gps is attached
     */
    public NmeaGpsImpl( String portString, String waitString, String baudRateString, String dataBitString, String stopBitString,
            String parityBitString, boolean isTestmode ) {
        this.portString = portString;

        this.waitString = (waitString == null ? MAXWAIT_DEF : waitString);
        this.baudRateString = (baudRateString == null ? BAUDRATE_DEF : baudRateString);
        this.dataBitString = (dataBitString == null ? DATABIT_DEF : dataBitString);
        this.stopBitString = (stopBitString == null ? STOPBIT_DEF : stopBitString);
        this.parityBitString = (parityBitString == null ? PARITYBIT_DEF : parityBitString);
        this.isTestmode = isTestmode;

        prefs = GpsActivator.getDefault().getPreferenceStore();

        observers = Collections.synchronizedList(new ArrayList<Observer>());

        gpsArtist = new GpsArtist();
    }

    public boolean startGps() {
        if (!isTestmode) {

            try {
                CommPortIdentifier portID = CommPortIdentifier.getPortIdentifier(portString);
                port = (SerialPort) portID.open("JGrass", Integer.parseInt(waitString)); //$NON-NLS-1$
                port.setSerialPortParams(Integer.parseInt(baudRateString), Integer.parseInt(dataBitString),
                        Integer.parseInt(stopBitString), Integer.parseInt(parityBitString));
                port.addEventListener(this);
                port.notifyOnDataAvailable(true);

                commPortInputStream = port.getInputStream();

                gpsIsConnected = true;
                return true;

            } catch (Exception e) {
                errorMessage(e);
                GpsActivator.log("GpsActivator problem: eu.hydrologis.jgrass.gpsnmea.gps#NmeaGpsImpl#startGps", e); //$NON-NLS-1$
                e.printStackTrace();
                gpsIsConnected = false;

                if (port != null) {
                    port.removeEventListener();
                }

                return false;
            }
        } else {
            dummyNmea = new DummyNmea();
            gpsIsConnected = true;
            new Thread(this).start();
            return true;
        }

    }

    public boolean stopGps() {
        if (isTestmode) {
            gpsIsConnected = false;
            dummyNmea = null;
            return true;
        }
        try {
            if (commPortInputStream != null)
                commPortInputStream.close();
            if (port != null) {
                port.removeEventListener();
                port.close();
            }
            gpsIsConnected = false;
            return true;
        } catch (Exception e) {
            GpsActivator.log("GpsActivator problem: eu.hydrologis.jgrass.gpsnmea.gps#NmeaGpsImpl#stopGps", e); //$NON-NLS-1$
            e.printStackTrace();
            gpsIsConnected = true;
            return false;
        }
    }

    public void doLogging( boolean doLog ) {
        if (doLog) {
            if (gpsIsConnected) {
                gpsIsLogging = true;
                new Thread(this).start();
            }
        } else {
            gpsIsLogging = false;
        }
    }

    public boolean isGpsConnected() {
        return gpsIsConnected;
    }

    public boolean isGpsLogging() {
        return gpsIsLogging;
    }

    public String getCurrentGpsData() {
        if (currentGPGGAsentence != null && currentGPRMCsentence != null) {
            return currentGPGGAsentence + "\n" + currentGPRMCsentence; //$NON-NLS-1$
        } else {
            return null;
        }
    }

    public synchronized NmeaGpsPoint getCurrentGpsPoint( CoordinateReferenceSystem destinationCrs ) throws Exception {

        NmeaGpsPoint currentGpsPoint = null;
        if (!isTestmode) {
            currentGpsPoint = new NmeaGpsPoint(currentGPGGAsentence, currentGPRMCsentence);
        } else {
            if (dummyNmea != null) {
                String[] nextNmeaSentences = dummyNmea.getNextNmeaSentences();
                currentGPGGAsentence = nextNmeaSentences[0];
                currentGPRMCsentence = nextNmeaSentences[1];
                currentGpsPoint = new NmeaGpsPoint(currentGPGGAsentence, currentGPRMCsentence);
            }
        }
        // filter out invalid points in 0,0
        if (currentGpsPoint.longitude == 0.0 && currentGpsPoint.latitude == 0.0) {
            return null;
        }
        if (currentGpsPoint.utcDateTime == null) {
            return null;
        }
        // log it to database
        GpsActivator.getDefault().getDatabaseManager().insertGpsPoint(currentGpsPoint);
        // reproject it
        if (destinationCrs != null) {
            NmeaGpsPoint clonedGpsPoint = new NmeaGpsPoint(currentGpsPoint);
            clonedGpsPoint.reproject(destinationCrs);
            return clonedGpsPoint;
        }

        return currentGpsPoint;
    }

    public void serialEvent( SerialPortEvent ev ) {
        switch( ev.getEventType() ) {
        case SerialPortEvent.BI:
            System.out.println("BI event"); //$NON-NLS-1$
        case SerialPortEvent.OE:
            System.out.println("OE event"); //$NON-NLS-1$
        case SerialPortEvent.FE:
            System.out.println("FE event"); //$NON-NLS-1$
        case SerialPortEvent.PE:
            System.out.println("PE event"); //$NON-NLS-1$
        case SerialPortEvent.CD:
            System.out.println("CD event"); //$NON-NLS-1$
        case SerialPortEvent.CTS:
            System.out.println("CTS event"); //$NON-NLS-1$
        case SerialPortEvent.DSR:
            System.out.println("DSR event"); //$NON-NLS-1$
        case SerialPortEvent.RI:
            System.out.println("RI event"); //$NON-NLS-1$
        case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
            System.out.println("OUTPUT_BUFFER_EMPTY event"); //$NON-NLS-1$
            break;
        case SerialPortEvent.DATA_AVAILABLE:
            //System.out.println("DATA_AVAILABLE event"); //$NON-NLS-1$

            if (gpsIsConnected) {
                try {
                    // byte[] buffer1 = new byte[1024];
                    // int data;
                    // try {
                    // int len1 = 0;
                    // while( (data = commPortInputStream.read()) > -1 ) {
                    // if (data == '\n') {
                    // break;
                    // }
                    // buffer1[len1++] = (byte) data;
                    // }
                    // String nmea = new String(buffer1, 0, len1);
                    // } catch (IOException e) {
                    // e.printStackTrace();
                    // System.exit(-1);
                    // }

                    byte[] buffer = new byte[1024];
                    int len = -1;
                    StringBuilder sb = new StringBuilder();
                    while( (len = commPortInputStream.read(buffer)) > -1 ) {
                        sb.append(new String(buffer, 0, len));
                    }
                    String currLine = sb.toString();
                    if (currLine.startsWith(NmeaGpsPoint.GPRMC)) {
                        currentGPRMCsentence = currLine;
                    } else if (currLine.startsWith(NmeaGpsPoint.GPGGA)) {
                        currentGPGGAsentence = currLine;
                    }
                } catch (IOException e) {
                    // if a line couldn't be read, ignore the event
                    break;
                } catch (Exception e) {
                    System.out.println("GPS DATAENEVENT READ EVENT NOT ENDED SUCCESSFULL: " + e.getLocalizedMessage());
                    break;
                }
            }

        }
    }
    public void addObserver( Observer o ) {
        if (!observers.contains(o)) {
            System.out.println("added");
            observers.add(o);
        }
    }

    public void deleteObserver( Observer o ) {
        if (observers.contains(o)) {
            System.out.println("removed");
            observers.remove(o);
        }
    }

    public synchronized void notifyObservers() {
        IMap activeMap = ApplicationGIS.getActiveMap();
        mapCrs = activeMap.getViewportModel().getCRS();
        GpsPoint returnGpsPoint = null;
        try {
            returnGpsPoint = getCurrentGpsPoint(mapCrs);
            if (returnGpsPoint == null)
                return;
        } catch (Exception e) {
            e.printStackTrace();
            String message = "An error occurred while retriving the GPS position.";
            ExceptionDetailsDialog.openError(null, message, IStatus.ERROR, GpsActivator.PLUGIN_ID, e);
            return;
        }

        synchronized (observers) {
            for( Observer observer : observers ) {
                observer.update(this, returnGpsPoint);
            }
        }
    }

    public void run() {
        while( gpsIsLogging && gpsIsConnected ) {
            final int milliSeconds = prefs.getInt(PreferenceConstants.INTERVAL_SECONDS) * 1000;
            IMap activeMap = ApplicationGIS.getActiveMap();
            mapCrs = activeMap.getViewportModel().getCRS();
            NmeaGpsPoint returnGpsPoint = null;
            try {
                returnGpsPoint = getCurrentGpsPoint(mapCrs);
                if (returnGpsPoint == null)
                    return;
            } catch (Exception e) {
                e.printStackTrace();
                String message = "An error occurred while retriving the GPS position.";
                ExceptionDetailsDialog.openError(null, message, IStatus.ERROR, GpsActivator.PLUGIN_ID, e);
                break;
            }

            System.out.println(returnGpsPoint.toString());

            gpsArtist.blink(returnGpsPoint);

            try {
                // notify all listeners
                notifyObservers();
                Thread.sleep(milliSeconds);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        gpsArtist.clear();
        gpsIsLogging = false;
    }

    private void errorMessage( Exception e ) {

        String msg = "An error occurred. If the problem persists, try to restart the application.";
        if (e instanceof NumberFormatException) {
            msg = "Some of the numerical parameters are for Gps connection were not inserted correctly. Please check your preferences and restart the Gps.";
        } else if (e instanceof NoSuchPortException) {
            msg = "The supplied serial port doesn't seem to exist. Probably the operating system lost the bloetooth connection to the device. Try to reconnect the device.";
        } else if (e instanceof PortInUseException) {
            msg = "The application was not able to attach the Gps to the operating system. This can sometimes be due to other applications using the port or not clean shutdown of resources. Try to connect again and restart the application if the problem persists.";
        } else if (e instanceof UnsupportedCommOperationException) {
            msg = "The application tried to access the serial port through an unsupported operation.";
        } else if (e instanceof IOException) {
            msg = "The connection to the Gps seems to be lost by the operating system. Try to reconnect the Gps and start again.";
        }
        ExceptionDetailsDialog.openError(null, msg, IStatus.ERROR, GpsActivator.PLUGIN_ID, e);
    }

    public static void main( String[] args ) throws Exception {
        String[][] test = checkGpsPorts();

        AbstractGps gps = new NmeaGpsImpl("COM23", null, null, null, null, null, false);
        if (!gps.startGps()) {
            return;
        }

        for( int i = 0; i < 20; i++ ) {
            System.out.println(gps.getCurrentGpsPoint(null).toString());
            Thread.sleep(2000);
        }

        gps.stopGps();

    }

}
