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
package eu.hydrologis.jgrass.beegisutils.database.annotatedclasses;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

/**
 * The persistent class representing the gps logs table.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@Entity
@Table(name = "gpslog")
public class GpsLogTable {
    
    @Id
    @GeneratedValue
    @Column(name="id", nullable=false)
    private Long id;
    
    @Column(name="utctime", nullable=false)
    @Type(type="org.joda.time.contrib.hibernate.PersistentDateTime")
    private DateTime utcTime;

    @Column(name="east", nullable=false)
    private double east;

    @Column(name="north", nullable=false)
    private double north;
    
    @Column(name="altimetry", nullable=true)
    private double altimetry;
    
    @Column(name="hdop", nullable=true)
    private double horizontalDilutionOfPosition;
    
    @Column(name="satnum", nullable=true)
    private int numberOfTrackedSatellites;


    public void setId( Long id ) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setUtcTime( DateTime utcTime ) {
        this.utcTime = utcTime;
    }

    public DateTime getUtcTime() {
        return utcTime;
    }

    public double getEast() {
        return east;
    }

    public void setEast( double east ) {
        this.east = east;
    }

    public double getNorth() {
        return north;
    }

    public void setNorth( double north ) {
        this.north = north;
    }

    public double getAltimetry() {
        return altimetry;
    }

    public void setAltimetry( double altimetry ) {
        this.altimetry = altimetry;
    }

    public double getHorizontalDilutionOfPosition() {
        return horizontalDilutionOfPosition;
    }

    public void setHorizontalDilutionOfPosition( double horizontalDilutionOfPosition ) {
        this.horizontalDilutionOfPosition = horizontalDilutionOfPosition;
    }

    public int getNumberOfTrackedSatellites() {
        return numberOfTrackedSatellites;
    }

    public void setNumberOfTrackedSatellites( int numberOfTrackedSatellites ) {
        this.numberOfTrackedSatellites = numberOfTrackedSatellites;
    }
}



