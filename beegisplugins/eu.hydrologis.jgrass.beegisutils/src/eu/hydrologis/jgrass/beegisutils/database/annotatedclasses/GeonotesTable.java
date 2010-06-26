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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

/**
 * The persistent class representing the geonotes table.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@Entity
@Table(name = "geonotes")
public class GeonotesTable implements Serializable{

    @Transient
    private static final long serialVersionUID = 1L;

    /**
     * The unique id of the Geonote.
     */
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * A short title for the Geonote.
     */
    @Column(name = "title", nullable = false)
    private String title;

    /**
     * Various informations about the Geonote in human readable version. 
     */
    @Column(name = "info", nullable = false)
    private String info;

    /**
     * The type of the Geonote.
     */
    @Column(name = "type", nullable = false)
    private Integer type;

    /**
     * The creation time of the Geonote.
     */
    @Column(name = "timestamp", nullable = false)
    @Type(type = "org.joda.time.contrib.hibernate.PersistentDateTime")
    private DateTime creationDateTime;

    /**
     * The easting coordinate of the Geonote in the supplied {@link #crsWkt}.
     */
    @Column(name = "east", nullable = false)
    private Double east;

    /**
     * The northing coordinate of the Geonote in the supplied {@link #crsWkt}.
     */
    @Column(name = "north", nullable = false)
    private Double north;

    /**
     * The azimut of the Geonote if directionality is defined.
     */
    @Column(name = "azimut", nullable = true)
    private Double azimut;

    /**
     * The coordinate reference system in its WKT form.
     */
    @Lob
    @Column(name = "crswkt", nullable = false)
    private String crsWkt;

    /**
     * The color associated with the Geonote defined as r:g:b:a in integers. 
     */
    @Column(name = "color", nullable = false)
    private String color;

    /**
     * The width associated with the Geonote.
     */
    @Column(name = "width", nullable = false)
    private Integer width;

    /**
     * The height associated with the Geonote.
     */
    @Column(name = "height", nullable = false)
    private Integer height;

    /*
     * ============== getters and setters for persistend fields ==============
     */

    public Long getId() {
        return id;
    }

    public void setId( Long id ) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle( String title ) {
        this.title = title;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo( String info ) {
        this.info = info;
    }

    public Integer getType() {
        return type;
    }

    public void setType( Integer type ) {
        this.type = type;
    }

    public DateTime getCreationDateTime() {
        return creationDateTime;
    }

    public void setCreationDateTime( DateTime creationDateTime ) {
        this.creationDateTime = creationDateTime;
    }

    public Double getEast() {
        return east;
    }

    public void setEast( Double east ) {
        this.east = east;
    }

    public Double getNorth() {
        return north;
    }

    public void setNorth( Double north ) {
        this.north = north;
    }

    public void setAzimut( Double azimut ) {
        this.azimut = azimut;
    }

    public Double getAzimut() {
        return azimut;
    }

    public String getCrsWkt() {
        return crsWkt;
    }

    public void setCrsWkt( String crsWkt ) {
        this.crsWkt = crsWkt;
    }

    public String getColor() {
        return color;
    }

    public void setColor( String color ) {
        this.color = color;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth( Integer width ) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight( Integer height ) {
        this.height = height;
    }

}
