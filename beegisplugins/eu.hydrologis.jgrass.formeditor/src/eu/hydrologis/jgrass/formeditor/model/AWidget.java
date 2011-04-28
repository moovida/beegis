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
package eu.hydrologis.jgrass.formeditor.model;

import static eu.hydrologis.jgrass.formeditor.utils.Constants.DIMENSION_PIXEL_SNAP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.ID_HEIGHT_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.LOCATION_PIXEL_SNAP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.ID_LOCATION_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.ID_NAME_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.ID_SIZE_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.ID_SOURCE_CONNECTIONS_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.ID_TAB_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.ID_TARGET_CONNECTIONS_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.ID_TYPE_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.ID_WIDTH_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.ID_XPOS_PROP;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.ID_YPOS_PROP;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import eu.hydrologis.jgrass.formeditor.FormEditor;
import eu.hydrologis.jgrass.formeditor.utils.Constants;

/**
 * Abstract prototype of a widget representer.
 * 
 * It has a fixed size (width and height), a 
 * location (x and y position) and a list of incoming
 * and outgoing connections. Use subclasses to instantiate a specific widgets.
 * 
 * (taken from example of Elias Volanakis)
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public abstract class AWidget extends AModelElement {

    private static final long serialVersionUID = 1;
    /** 
     * A static array of property descriptors.
     * There is one IPropertyDescriptor entry per editable property.
     * @see #getPropertyDescriptors()
     * @see #getPropertyValue(Object)
     * @see #setPropertyValue(Object, Object)
     */
    protected IPropertyDescriptor[] descriptors;

    /**
     * A static number to propose the default widget name.
     */
    private static int widgetIndex = 0;

    /** 
     * The tab in which the widget will finish. 
     */
    protected String widgetTab;
    /** 
     * The name of the widget. 
     */
    protected String widgetName = "widget_" + widgetIndex++; //$NON-NLS-1$
    /** 
     * Location of this widget. 
     */
    protected Point location = new Point(0, 0);
    /** 
     * Size of this widget. 
     */
    protected Dimension size = new Dimension(345, 47);
    /** 
     * List of outgoing Connections. 
     */
    private List<Connection> sourceConnections = new ArrayList<Connection>();
    /** List of incoming Connections. */
    private List<Connection> targetConnections = new ArrayList<Connection>();
    private boolean isMarked;

    /**
     * Add an incoming or outgoing connection to this shape.
     * @param conn a non-null connection instance
     * @throws IllegalArgumentException if the connection is null or has not distinct endpoints
     */
    void addConnection( Connection conn ) {
        if (conn == null || conn.getSource() == conn.getTarget()) {
            throw new IllegalArgumentException();
        }
        if (conn.getSource() == this) {
            sourceConnections.add(conn);
            firePropertyChange(ID_SOURCE_CONNECTIONS_PROP, null, conn);
        } else if (conn.getTarget() == this) {
            targetConnections.add(conn);
            firePropertyChange(ID_TARGET_CONNECTIONS_PROP, null, conn);
        }
    }

    /**
     * Remove an incoming or outgoing connection from this shape.
     * @param conn a non-null connection instance
     * @throws IllegalArgumentException if the parameter is null
     */
    void removeConnection( Connection conn ) {
        if (conn == null) {
            throw new IllegalArgumentException();
        }
        if (conn.getSource() == this) {
            sourceConnections.remove(conn);
            firePropertyChange(ID_SOURCE_CONNECTIONS_PROP, null, conn);
        } else if (conn.getTarget() == this) {
            targetConnections.remove(conn);
            firePropertyChange(ID_TARGET_CONNECTIONS_PROP, null, conn);
        }
    }

    /**
     * Return a pictogram (small icon) describing this model element.
     * Children should override this method and return an appropriate Image.
     * @return a 16x16 Image or null
     */
    public abstract Image getIcon();

    /**
     * Returns an array of IPropertyDescriptors for this shape.
     * <p>The returned array is used to fill the property view, when the edit-part corresponding
     * to this model element is selected.</p>
     * @see #descriptors
     * @see #getPropertyValue(Object)
     * @see #setPropertyValue(Object, Object)
     */
    public IPropertyDescriptor[] getPropertyDescriptors() {
        return descriptors;
    }

    /**
     * Return the property value for the given propertyId, or null.
     * <p>The property view uses the IDs from the IPropertyDescriptors array 
     * to obtain the value of the corresponding properties.</p>
     * @see #descriptors
     * @see #getPropertyDescriptors()
     */
    public Object getPropertyValue( Object propertyId ) {
        if (ID_XPOS_PROP.equals(propertyId)) {
            return Integer.toString(location.x);
        } else if (ID_YPOS_PROP.equals(propertyId)) {
            return Integer.toString(location.y);
        } else if (ID_HEIGHT_PROP.equals(propertyId)) {
            return Integer.toString(size.height);
        } else if (ID_WIDTH_PROP.equals(propertyId)) {
            return Integer.toString(size.width);
        } else if (ID_NAME_PROP.equals(propertyId)) {
            return getName();
        } else if (ID_TAB_PROP.equals(propertyId)) {
            return getTab();
        } else if (ID_TYPE_PROP.equals(propertyId)) {
            return getWidgetType();
        }
        return super.getPropertyValue(propertyId);
    }

    /**
     * Set the property value for the given property id.
     * If no matching id is found, the call is forwarded to the superclass.
     * <p>The property view uses the IDs from the IPropertyDescriptors array to set the values
     * of the corresponding properties.</p>
     * @see #descriptors
     * @see #getPropertyDescriptors()
     */
    public void setPropertyValue( Object propertyId, Object value ) {
        if (ID_XPOS_PROP.equals(propertyId)) {
            int x = Integer.parseInt((String) value);
            setLocation(new Point(x, location.y));
        } else if (ID_YPOS_PROP.equals(propertyId)) {
            int y = Integer.parseInt((String) value);
            setLocation(new Point(location.x, y));
        } else if (ID_HEIGHT_PROP.equals(propertyId)) {
            int height = Integer.parseInt((String) value);
            setSize(new Dimension(size.width, height));
        } else if (ID_WIDTH_PROP.equals(propertyId)) {
            int width = Integer.parseInt((String) value);
            setSize(new Dimension(width, size.height));
        } else if (ID_NAME_PROP.equals(propertyId)) {
            setName((String) value);
        } else if (ID_TAB_PROP.equals(propertyId)) {
            setTab((String) value);
        } else {
            super.setPropertyValue(propertyId, value);
        }
    }

    /**
     * Return a List of outgoing Connections.
     */
    public List<Connection> getSourceConnections() {
        return new ArrayList<Connection>(sourceConnections);
    }

    /**
     * Return a List of incoming Connections.
     */
    public List<Connection> getTargetConnections() {
        return new ArrayList<Connection>(targetConnections);
    }

    /**
     * Get the bounds minus a couple of units at the lower and right edges. 
     * 
     * @return the bounds.
     */
    public Rectangle getBounds() {
//        Dimension newSize = size;// new Dimension(size.width - Constants.DIMENSION_PIXEL_SNAP/2,
//                                 // size.height - Constants.DIMENSION_PIXEL_SNAP/2);
//        Point newLocation = new Point(location.x, location.y + Constants.DIMENSION_PIXEL_SNAP / 4);

        Rectangle r = new Rectangle(location, size);
        return r;
    }

    public Rectangle snapToBounds( Rectangle oldRect ) {
        Point rLoc = oldRect.getLocation();
        Dimension rSize = oldRect.getSize();

        Point copy = rLoc.getCopy();
        snapLocation(copy);
        Dimension copy2 = rSize.getCopy();
        snapSize(copy2);

        Rectangle r = new Rectangle(copy, copy2);
        return r;
    }

    /**
     * Return the Location of this shape.
     * @return a non-null location instance
     */
    public Point getLocation() {
        return location.getCopy();
    }

    /**
     * Set the Location of this shape.
     * @param newLocation a non-null Point instance
     * @throws IllegalArgumentException if the parameter is null
     */
    public void setLocation( Point newLocation ) {
        if (newLocation == null) {
            throw new IllegalArgumentException();
        }
        snapLocation(newLocation);
        location.setLocation(newLocation);
        firePropertyChange(ID_LOCATION_PROP, null, location);
    }

    private void snapLocation( Point newLocation ) {
        int x = newLocation.x;
        int y = newLocation.y;
        x = x - (x % LOCATION_PIXEL_SNAP);
        y = y - (y % LOCATION_PIXEL_SNAP);

        newLocation.x = x > 0 ? x : 0;
        newLocation.y = y > 0 ? y : 0;
    }

    /**
     * Return the Size of this shape.
     * @return a non-null Dimension instance
     */
    public Dimension getSize() {
        return size.getCopy();
    }

    /**
     * Set the Size of this shape.
     * Will not modify the size if newSize is null.
     * @param newSize a non-null Dimension instance or null
     */
    public void setSize( Dimension newSize ) {
        if (newSize != null) {
            snapSize(newSize);
            size.setSize(newSize);
            firePropertyChange(ID_SIZE_PROP, null, size);
        }
    }

    private void snapSize( Dimension newLocation ) {
        int x = newLocation.width;
        int y = newLocation.height;
        x = x - (x % DIMENSION_PIXEL_SNAP);
        y = y - (y % DIMENSION_PIXEL_SNAP);

        newLocation.width = x > 0 ? x : DIMENSION_PIXEL_SNAP;
        newLocation.height = y > 0 ? y : DIMENSION_PIXEL_SNAP;
    }

    public String getName() {
        return widgetName;
    }

    public void setName( String newName ) {
        if (newName == null) {
            throw new IllegalArgumentException();
        }
        widgetName = newName;
        firePropertyChange(ID_NAME_PROP, null, widgetName);
    }

    public String getTab() {
        if (widgetTab == null) {
            widgetTab = FormEditor.getLastTabNameInserted();
        }
        return widgetTab;
    }

    public String getText() {
        return ""; //$NON-NLS-1$
    }

    public void setTab( String widgetTab ) {
        this.widgetTab = widgetTab;
        firePropertyChange(ID_TAB_PROP, null, widgetTab);
        FormEditor.setLastTabNameInserted(widgetTab);
    }

    /**
     * Returns the start and end row of the widget.
     * 
     * @return an array containing the start and end rows.
     */
    public int[] getRowBounds() {
        Point location = getLocation();
        int startRow = location.y / LOCATION_PIXEL_SNAP;
        Dimension size = getSize();
        int endRow = startRow + size.height / DIMENSION_PIXEL_SNAP - 1;
        return new int[]{startRow, endRow};
    }

    /**
     * Returns the start and end col of the widget.
     * 
     * @return an array containing the start and end cols.
     */
    public int[] getColBounds() {
        Point location = getLocation();
        int startCol = location.x / LOCATION_PIXEL_SNAP;
        Dimension size = getSize();
        int endCol = startCol + size.width / DIMENSION_PIXEL_SNAP - 1;
        return new int[]{startCol, endCol};
    }

    public void setMarked( boolean isMarked ) {
        this.isMarked = isMarked;
    }

    public boolean isMarked() {
        return isMarked;
    }

    /**
     * Get the widget type.
     * 
     * @return the string describing the widget type.
     */
    protected abstract String getWidgetType();

    @SuppressWarnings("nls")
    protected static synchronized void addIntegerPropertyValidator( PropertyDescriptor descriptor ) {
        descriptor.setValidator(new ICellEditorValidator(){
            public String isValid( Object value ) {
                int intValue = -1;
                try {
                    intValue = Integer.parseInt((String) value);
                } catch (NumberFormatException exc) {
                    return "Not an integer";
                }
                return (intValue >= 0) ? null : "Value must be >=  0";
            }
        });
    }

    @SuppressWarnings("nls")
    protected static synchronized void addDoublePropertyValidator( PropertyDescriptor descriptor ) {
        descriptor.setValidator(new ICellEditorValidator(){
            public String isValid( Object value ) {
                double doubleValue = -1;
                try {
                    doubleValue = Double.parseDouble((String) value);
                } catch (NumberFormatException exc) {
                    return "Not a number";
                }
                return String.valueOf(doubleValue);
            }
        });
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((widgetName == null) ? 0 : widgetName.hashCode());
        result = prime * result + ((widgetTab == null) ? 0 : widgetTab.hashCode());
        return result;
    }

    @Override
    public boolean equals( Object obj ) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AWidget other = (AWidget) obj;
        if (widgetName == null) {
            if (other.widgetName != null)
                return false;
        } else if (!widgetName.equals(other.widgetName))
            return false;
        if (widgetTab == null) {
            if (other.widgetTab != null)
                return false;
        } else if (!widgetTab.equals(other.widgetTab))
            return false;
        return true;
    }

}