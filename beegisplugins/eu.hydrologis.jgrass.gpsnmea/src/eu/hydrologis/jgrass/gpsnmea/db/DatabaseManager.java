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
package eu.hydrologis.jgrass.gpsnmea.db;

import static org.hibernate.criterion.Order.asc;
import static org.hibernate.criterion.Restrictions.between;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;
import org.joda.time.DateTime;

import eu.hydrologis.jgrass.beegisutils.BeegisUtilsPlugin;
import eu.hydrologis.jgrass.beegisutils.database.annotatedclasses.GpsLogTable;
import eu.hydrologis.jgrass.embeddeddb.EmbeddedDbPlugin;
import eu.hydrologis.jgrass.gpsnmea.gps.GpsPoint;
import eu.hydrologis.jgrass.gpsnmea.gps.NmeaGpsPoint;

/**
 * Class for the extraction of various data types from the database.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class DatabaseManager {
    private static DateTime defaultFrom = null;
    private static DateTime defaultTo = null;
    static {
        defaultFrom = BeegisUtilsPlugin.dateTimeFormatterYYYYMMDDHHMM
                .parseDateTime("1970-01-01 00:00"); //$NON-NLS-1$
        defaultTo = BeegisUtilsPlugin.dateTimeFormatterYYYYMMDDHHMM
                .parseDateTime("3000-01-01 00:00"); //$NON-NLS-1$
    }

    /**
     * Insert a {@link NmeaGpsPoint gps point} in the database.
     * 
     * @param gpsPoint the point to insert.
     * @throws Exception
     */
    public synchronized void insertGpsPoint( GpsPoint gpsPoint ) throws Exception {
        if (gpsPoint.utcDateTime == null || gpsPoint.longitude == 0.0 || gpsPoint.latitude == 0.0) {
            return;
        }

        SessionFactory sessionFactory = EmbeddedDbPlugin.getDefault().getSessionFactory();
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();

        GpsLogTable gpsLog = new GpsLogTable();
        gpsLog.setUtcTime(gpsPoint.utcDateTime);
        gpsLog.setEast(gpsPoint.longitude);
        gpsLog.setNorth(gpsPoint.latitude);
        gpsLog.setAltimetry(gpsPoint.altitude);
        gpsLog.setNumberOfTrackedSatellites((int) gpsPoint.sat);
        gpsLog.setHorizontalDilutionOfPosition(gpsPoint.hdop);

        session.save(gpsLog);
        transaction.commit();
        session.close();
    }

    /**
     * Retrieves the list of {@link GpsPoint points} between two given dates.
     * 
     * @param from start {@link DateTime}. Can be null, in which case 
     *                          1970-01-01 00:00 will be used.
     * @param to end {@link DateTime}. Can be null, in which case 
     *                          3000-01-01 00:00 will be used.
     * @return the list of points.
     * @throws Exception
     */
    public List<GpsPoint> getGpsPointBetweenTimeStamp( DateTime from, DateTime to )
            throws Exception {

        if (from == null) {
            from = defaultFrom;
        }
        if (to == null) {
            to = defaultTo;
        }

        SessionFactory hibernateSessionFactory = EmbeddedDbPlugin.getDefault().getSessionFactory();
        Session session = hibernateSessionFactory.openSession();
        List<GpsPoint> pointsList = new ArrayList<GpsPoint>();
        try {
            Criteria criteria = session.createCriteria(GpsLogTable.class);
            String utcTimeStr = "utcTime";
            criteria.add(between(utcTimeStr, from, to));
            criteria.addOrder(asc(utcTimeStr));

            List<GpsLogTable> resultsList = criteria.list();
            for( GpsLogTable gpsLog : resultsList ) {
                NmeaGpsPoint p = new NmeaGpsPoint();
                p.utcDateTime = gpsLog.getUtcTime();
                p.longitude = gpsLog.getEast();
                p.latitude = gpsLog.getNorth();
                p.altitude = gpsLog.getAltimetry();
                pointsList.add(p);
            }
        } finally {
            session.close();
        }
        return pointsList;
    }
}
