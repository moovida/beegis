/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 * (C) Universita' di Urbino
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
 package eu.hydrologis.jgrass.gpsnmea.preferences.pages;

/**
 * Constant definitions for GPS preferences
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class PreferenceConstants {

    /*
     * GPS preferences
     */
    public static final String PORT = "Ports available found to which a GPS could be connected.";
    public static final String MAXWAIT = "Max wait time to get ownership of port";
    public static final String BAUDRATE = "Baudrate";
    public static final String DATABIT = "Data bit";
    public static final String STOPBIT = "Stop bit";
    public static final String PARITYBIT = "Parity bit"; // none = 0
    public static final String TESTMODE = "Test mode with dummy data (if no gps available)";
    
    
    public static final String GPS_IS_ON = "gps is on";
    public static final String PORTUSED = "Gps port in use";
    public static final String INTERVAL_SECONDS = "Interval in seconds for Gps acquisition.";
    public static final String DISTANCE_THRESHOLD = "Distance threshold for Gps acquisition.";

}
