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
package eu.hydrologis.jgrass.formeditor.utils;

/**
 * Object representing the constraint's bound (cell of miglayout).
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class CellConstraint {
    private int x = 0;
    private int y = 0;
    private int width = 0;
    private int height = 0;

    private boolean hasCell = false;

    /**
     * Constructs the object basing of the constraint string.
     * 
     * @param cellConstraint the constraint string to use.
     */
    @SuppressWarnings("nls")
    public CellConstraint( String cellConstraint ) {
        String[] constraintsSplit = cellConstraint.split(",");
        for( int j = 0; j < constraintsSplit.length; j++ ) {
            String candidate = constraintsSplit[j].trim();
            if (candidate.startsWith("cell")) {
                String[] cellSplit = candidate.split("\\s+");
                x = Integer.parseInt(cellSplit[1]);
                y = Integer.parseInt(cellSplit[2]);
                width = Integer.parseInt(cellSplit[3]);
                height = Integer.parseInt(cellSplit[4]);
                hasCell = true;
            }
        }
    }

    /**
     * If true, the constraint has cell and bound definition.
     * 
     * @return <code>true</code> if the constraint has cell and bound definition.
     */
    public boolean hasCell() {
        return hasCell;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
