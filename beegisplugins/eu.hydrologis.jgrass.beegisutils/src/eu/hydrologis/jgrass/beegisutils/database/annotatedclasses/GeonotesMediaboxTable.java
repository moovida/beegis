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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * The persistent class representing the mediabox table.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@Entity
@Table(name = "mediabox")
public class GeonotesMediaboxTable {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "geonotesid", referencedColumnName = "id", nullable = false)
    private GeonotesTable geonotesId;

    @Column(name = "medianame", nullable = false)
    private String mediaName;

    public Long getId() {
        return id;
    }

    public void setId( Long id ) {
        this.id = id;
    }

    public GeonotesTable getGeonotesId() {
        return geonotesId;
    }

    public void setGeonotesId( GeonotesTable geonotesId ) {
        this.geonotesId = geonotesId;
    }

    public String getMediaName() {
        return mediaName;
    }

    public void setMediaName( String mediaName ) {
        this.mediaName = mediaName;
    }
}
