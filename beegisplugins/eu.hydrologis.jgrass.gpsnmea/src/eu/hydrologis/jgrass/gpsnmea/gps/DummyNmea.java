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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IStatus;

import net.refractions.udig.ui.ExceptionDetailsDialog;

import eu.hydrologis.jgrass.gpsnmea.GpsActivator;

/**
 * A dummy nmea data provider. 
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class DummyNmea {

    private BufferedReader nmeaBuffer;

    private List<String[]> nmea = new ArrayList<String[]>();

    private Iterator<String[]> iterator;

    private String[] current;

    public DummyNmea() {
        try {
            InputStream nmeaStream = this.getClass().getResourceAsStream("dummy.nmea"); //$NON-NLS-1$
            nmeaBuffer = new BufferedReader(new InputStreamReader(nmeaStream));

            List<String> rawGpgga = new ArrayList<String>();
            List<String> rawGprmc = new ArrayList<String>();
            String line = null;
            while( (line = nmeaBuffer.readLine()) != null ) {
                if (line.startsWith(NmeaGpsPoint.GPGGA)) {
                    rawGpgga.add(line);
                } else if (line.startsWith(NmeaGpsPoint.GPRMC)) {
                    rawGprmc.add(line);
                }
            }
            nmeaBuffer.close();
            int index = Math.min(rawGpgga.size(), rawGprmc.size());
            for( int i = 0; i < index; i++ ) {
                nmea.add(new String[]{rawGpgga.get(i), rawGprmc.get(i)});
            }
        } catch (IOException e) {
            String message = "An error occurred while reading the dummy gps data.";
            ExceptionDetailsDialog.openError(null, message, IStatus.ERROR, GpsActivator.PLUGIN_ID,
                    e);
            e.printStackTrace();
        }
    }

    /**
     * @return the couple of nmea sentences that are of interest to us. [ GPGGA ,
     *         GPRMC ]
     */
    public String[] getNextNmeaSentences() {
        if (iterator == null || !iterator.hasNext()) {
            iterator = nmea.iterator();
        }

        if (iterator.hasNext()) {
            current = iterator.next();
            return current;
        }
        return null;
    }

    /**
     * @return the couple of nmea sentences that are of interest to us. [ GPGGA ,
     *         GPRMC ]
     */
    public String[] getCurrentNmeaSentences() {
        if (iterator == null || !iterator.hasNext()) {
            iterator = nmea.iterator();
        }

        if (current != null)
            return current;
        return null;
    }

}
