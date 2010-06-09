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

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import com.vividsolutions.jts.geom.Coordinate;

import eu.hydrologis.jgrass.beegisutils.BeegisUtilsPlugin;

/**
 * A base class for gps point.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class GpsPoint {
    public static final String GPGGA = "$GPGGA";
    public static final String GPRMC = "$GPRMC";
    public final static String strLatitude = "Northing";
    public final static String strLongitude = "Easting";
    public final static String strSpeed = "Speed";
    public final static String strAltitude = "Altitude above msl";
    public final static String strQuality = "quality";
    public final static String strSat = "Number of Satellites";
    public final static String strHdop = "HDOP";
    public final static String strMsl = "Diff. ellipsoid and msl";
    public final static String strUtctime = "UTC time";
    public final static String strMag_var = "Magnetic variation";
    public final static String strAngle = "angle";
    public final static String strValidity1 = "THIS POINT IS NOT VALID"; // "Validity";
    public final static String strValidity2 = "";
    public double latitude = -1.0;
    public double longitude = -1.0;
    public double speed = -1.0;
    public double altitude = -1.0;
    public double quality = -1.0;
    public double sat = -1.0;
    public double hdop = -1.0;
    public double ellipsoidVsMsl = -1.0;
    public DateTime utcDateTime = null;
    public double mag_var = -1.0;
    public double angle = -1.0;
    public String altitudeUnit = "M";
    public String ellipsoidVsMslUnit = "M";
    public boolean isValid = false;

    /**
     * @return a table array representation useful for tableviewers.
     */
    public String[][] toTableArray() {
        List<String[]> arrayList = new ArrayList<String[]>();
        if (!isValid) {
            arrayList.add(new String[]{strValidity1, strValidity2});
        }
        arrayList.add(new String[]{strLatitude, String.valueOf(latitude)});
        arrayList.add(new String[]{strLongitude, String.valueOf(longitude)});
        arrayList.add(new String[]{strSpeed, String.valueOf(speed)});
        arrayList.add(new String[]{strAltitude, String.valueOf(altitude) + altitudeUnit});
        arrayList.add(new String[]{strQuality, String.valueOf(quality)});
        arrayList.add(new String[]{strSat, String.valueOf(sat)});
        arrayList.add(new String[]{strHdop, String.valueOf(hdop)});
        arrayList.add(new String[]{strMsl, String.valueOf(ellipsoidVsMsl) + ellipsoidVsMslUnit});
        arrayList.add(new String[]{strUtctime,
                utcDateTime.toString(BeegisUtilsPlugin.dateTimeFormatterYYYYMMDDHHMMSS)});
        arrayList.add(new String[]{strMag_var, String.valueOf(mag_var)});
        arrayList.add(new String[]{strAngle, String.valueOf(angle)});
        return (String[][]) arrayList.toArray(new String[arrayList.size()][2]);
    }

    /**
     * Measures if this point is al least at a given distance from another one.
     * 
     * @param point the other point.
     * @param distance the distance to use as threshold.
     * @return 
     */
    public boolean isAtLeastAtDistanceOf( GpsPoint point, double distance ) {
        Coordinate thisPoint = new Coordinate(longitude, latitude);
        Coordinate otherPoint = new Coordinate(point.longitude, point.latitude);

        double realDistance = thisPoint.distance(otherPoint);
        return realDistance >= distance;
    }

}