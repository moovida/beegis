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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import eu.hydrologis.jgrass.beegisutils.jgrassported.DressedStroke;

/**
 * The persistent class representing the mediabox blobs table.
 * 
 * <p>
 * This is kept as a separate table because hibernate would not lazy load the
 * blobs if the mediabox is queried.
 * </p>
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@Entity
@Table(name = "mediaboxblob")
public class GeonotesMediaboxBlobsTable {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToOne
    @JoinColumn(name = "mediaboxid", referencedColumnName = "id", nullable = false)
    private GeonotesMediaboxTable mediaboxId;

    @Lob
    @Column(name = "data", nullable = false)
    private byte[] media;

    @Lob
    @Column(name = "drawing", nullable = true)
    private ArrayList<DressedStroke> drawings;

    public Long getId() {
        return id;
    }

    public void setId( Long id ) {
        this.id = id;
    }

    public void setMediaboxId( GeonotesMediaboxTable mediaboxId ) {
        this.mediaboxId = mediaboxId;
    }

    public GeonotesMediaboxTable getMediaboxId() {
        return mediaboxId;
    }

    public byte[] getMedia() {
        return media;
    }

    public void setMedia( byte[] media ) {
        this.media = media;
    }

    public ArrayList<DressedStroke> getDrawings() {
        return drawings;
    }

    public void setDrawings( ArrayList<DressedStroke> drawings ) {
        this.drawings = drawings;
    }

    /**
     * Extracts the media and dumps it to file. 
     * 
     * @param file the file to which to dump to.
     * @throws IOException
     */
    public void getFromMediaboxToFile( File file ) throws IOException {
        FileOutputStream fos = null;
        byte[] mediaData = getMedia();
        try {
            fos = new FileOutputStream(file);
            fos.write(mediaData);
        } finally {
            if (fos != null)
                fos.close();
        }
    }

    /**
     * Inserts a new media into the mediabox taking the data from a file.
     * 
     * @param file the file containing the media to add.
     * @throws IOException
     */
    public void saveFileToMediabox( File file ) throws IOException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            int byteNum = fis.available();
            byte[] bytes = new byte[byteNum];
            int read = fis.read(bytes);
            if (read == 1) {
                return;
            }
            setMedia(bytes);
        } finally {
            if (null != fis)
                fis.close();
        }
    }

}
