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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import eu.hydrologis.jgrass.beegisutils.jgrassported.DressedWorldStroke;

/**
 * The persistent class representing the annotations table.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@Entity
@Table(name = "annotations")
public class AnnotationsTable {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Lob
    @Column(name = "data", nullable = false)
    private ArrayList<DressedWorldStroke> annotationDrawings;

    public Long getId() {
        return id;
    }

    public void setId( Long id ) {
        this.id = id;
    }

    public List<DressedWorldStroke> getAnnotationDrawings() {
        return annotationDrawings;
    }

    public void setAnnotationDrawings( List<DressedWorldStroke> annotationDrawings ) {
        if (annotationDrawings instanceof ArrayList) {
            this.annotationDrawings = (ArrayList<DressedWorldStroke>) annotationDrawings;
        }
    }
}
