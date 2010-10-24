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
package eu.hydrologis.jgrass.beegisutils.utils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Utility to deal with joda time.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class UtcDatetimeFormatter {
    
    public static DateTimeFormatter ISO_DATE_TIME_FORMATTER = ISODateTimeFormat.dateTime()
            .withZone(DateTimeZone.UTC);

    public static DateTimeFormatter ISO_DATE_TIME_PARSER = ISODateTimeFormat.dateTimeParser()
            .withZone(DateTimeZone.UTC);
    
    public static String datetime2UtcIsoString(DateTime dateTime) {
        return ISO_DATE_TIME_FORMATTER.print(dateTime);
    }
    
    public static DateTime utcIsoString2datetime(String utcIsoString) {
        return ISO_DATE_TIME_PARSER.parseDateTime(utcIsoString);
    }
    
}
