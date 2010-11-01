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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import eu.hydrologis.jgrass.formeditor.FormEditorPlugin;
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
public abstract class Widget extends ModelElement {

    
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
     * ID for the widget type.  
     */
    public static final String TYPE_PROP = "WidgetType";

    /** 
     * ID for the field name property value.  
     */
    public static final String FIELDNAME_PROP = "WidgetFieldname";

    /** 
     * ID for the X property value (used for by the corresponding property descriptor).  
     */
    public static final String XPOS_PROP = "WidgetXpos";

    /** 
     * ID for the Y property value (used for by the corresponding property descriptor).  
     */
    public static final String YPOS_PROP = "WidgetYpos";

    /** 
     * ID for the Height property value. 
     */
    public static final String HEIGHT_PROP = "WidgetHeight";

    /** 
     * ID for the Width property value (used for by the corresponding property descriptor). 
     */
    public static final String WIDTH_PROP = "WidgetWidth";

    /** 
     * Property ID to use for text. 
     */
    public static final String TEXT_PROP = "WidgetText";

    /** 
     * Property ID to use for the default value. 
     */
    public static final String DEFAULT_PROP = "WidgetDefault";

    /** 
     * Property ID to use for a list of values. 
     */
    public static final String LIST_PROP = "WidgetList";

    /** 
     * Property ID to use for the selection of a value. May be "true" or simply an item name. 
     */
    public static final String SELECTION_PROP = "WidgetSelection";

    /** 
     * Property ID to use when the location of this shape is modified. 
     */
    public static final String LOCATION_PROP = "WidgetLocation";

    /** 
     * Property ID to use then the size of this shape is modified. 
     */
    public static final String SIZE_PROP = "WidgetSize";

    /** 
     * Property ID to use when the list of outgoing connections is modified. 
     */
    public static final String SOURCE_CONNECTIONS_PROP = "WidgetSourceConn";

    /** 
     * Property ID to use when the list of incoming connections is modified. 
     */
    public static final String TARGET_CONNECTIONS_PROP = "WidgetTargetConn";

    /** Name of the widget. */
    protected String fieldname = "enter fieldname";
    /** Location of this widget. */
    protected Point location = new Point(0, 0);
    /** Size of this widget. */
    protected Dimension size = new Dimension(345, 47);
    /** List of outgoing Connections. */
    private List<Connection> sourceConnections = new ArrayList<Connection>();
    /** List of incoming Connections. */
    private List<Connection> targetConnections = new ArrayList<Connection>();

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
            firePropertyChange(SOURCE_CONNECTIONS_PROP, null, conn);
        } else if (conn.getTarget() == this) {
            targetConnections.add(conn);
            firePropertyChange(TARGET_CONNECTIONS_PROP, null, conn);
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
            firePropertyChange(SOURCE_CONNECTIONS_PROP, null, conn);
        } else if (conn.getTarget() == this) {
            targetConnections.remove(conn);
            firePropertyChange(TARGET_CONNECTIONS_PROP, null, conn);
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
        if (FIELDNAME_PROP.equals(propertyId)) {
            return getFieldname();
        }
        if (XPOS_PROP.equals(propertyId)) {
            return Integer.toString(location.x);
        }
        if (YPOS_PROP.equals(propertyId)) {
            return Integer.toString(location.y);
        }
        if (HEIGHT_PROP.equals(propertyId)) {
            return Integer.toString(size.height);
        }
        if (WIDTH_PROP.equals(propertyId)) {
            return Integer.toString(size.width);
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
        if (XPOS_PROP.equals(propertyId)) {
            int x = Integer.parseInt((String) value);
            setLocation(new Point(x, location.y));
        } else if (YPOS_PROP.equals(propertyId)) {
            int y = Integer.parseInt((String) value);
            setLocation(new Point(location.x, y));
        } else if (HEIGHT_PROP.equals(propertyId)) {
            int height = Integer.parseInt((String) value);
            setSize(new Dimension(size.width, height));
        } else if (WIDTH_PROP.equals(propertyId)) {
            int width = Integer.parseInt((String) value);
            setSize(new Dimension(width, size.height));
        } else if (FIELDNAME_PROP.equals(propertyId)) {
            setFieldname((String) value);
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
        firePropertyChange(LOCATION_PROP, null, location);
    }

    private void snapLocation( Point newLocation ) {
        int x = newLocation.x;
        int y = newLocation.y;
        x = x - (x % Constants.PIXEL_SNAP);
        y = y - (y % Constants.PIXEL_SNAP);

        newLocation.x = x;
        newLocation.y = y;
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
            firePropertyChange(SIZE_PROP, null, size);
        }
    }
    
    private void snapSize( Dimension newLocation ) {
        int x = newLocation.width;
        int y = newLocation.height;
        x = x - (x % Constants.PIXEL_SNAP);
        y = y - (y % Constants.PIXEL_SNAP);

        newLocation.width = x;
        newLocation.height = y;
    }

    public String getFieldname() {
        return fieldname;
    }

    public void setFieldname( String newName ) {
        if (newName == null) {
            throw new IllegalArgumentException();
        }
        fieldname = newName;
        firePropertyChange(FIELDNAME_PROP, null, fieldname);
    }

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

    public abstract String toDumpString();
}