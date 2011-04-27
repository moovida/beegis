package eu.hydrologis.jgrass.formeditor.utils;

import org.eclipse.draw2d.geometry.Dimension;

@SuppressWarnings("nls")
public class Constants {
    public static final int LOCATION_PIXEL_SNAP = 30;
    public static final int DIMENSION_PIXEL_SNAP = 30;

    public static final int INSETS = 5;
    public static final int FIELDNAME_OFFSET_X = 30;
    public static final int FIELDNAME_OFFSET_Y = 5;

    /** 
     * ID for the widget type.  
     */
    public static final String ID_TYPE_PROP = "WidgetType";

    /** 
     * ID for the tab number property value.  
     */
    public static final String ID_TAB_PROP = "WidgetTab";

    /** 
     * ID for the field name property value.  
     */
    public static final String ID_FIELDNAME_PROP = "WidgetFieldname";

    /** 
     * ID for the X property value (used for by the corresponding property descriptor).  
     */
    public static final String ID_XPOS_PROP = "WidgetXpos";

    /** 
     * ID for the Y property value (used for by the corresponding property descriptor).  
     */
    public static final String ID_YPOS_PROP = "WidgetYpos";

    /** 
     * ID for the Height property value. 
     */
    public static final String ID_HEIGHT_PROP = "WidgetHeight";

    /** 
     * ID for the Width property value (used for by the corresponding property descriptor). 
     */
    public static final String ID_WIDTH_PROP = "WidgetWidth";

    /** 
     * Property ID to use for text. 
     */
    public static final String ID_TEXT_PROP = "WidgetText";

    /** 
     * Property ID to use for text type. 
     */
    public static final String ID_TEXT_TYPE_PROP = "WidgetTextType";

    /** 
     * Property ID to use for orientation type. 
     */
    public static final String ID_ORIENTATION_TYPE_PROP = "WidgetOrientationType";

    /** 
     * Property ID to use for name. 
     */
    public static final String ID_NAME_PROP = "WidgetName";

    /** 
     * Property ID to use for the default value. 
     */
    public static final String ID_DEFAULT_PROP = "WidgetDefault";

    /** 
     * Property ID to use for a list of values. 
     */
    public static final String ID_LIST_PROP = "WidgetList";

    /** 
     * Property ID to use for the selection of a value. May be "true" or simply an item name. 
     */
    public static final String ID_SELECTION_PROP = "WidgetSelection";

    /** 
     * Property ID to use for the items, as for example the items of a combo or radio button. 
     */
    public static final String ID_ITEMS_PROP = "WidgetItems";

    /** 
     * Property ID to use when the location of this shape is modified. 
     */
    public static final String ID_LOCATION_PROP = "WidgetLocation";

    /** 
     * Property ID to use then the size of this shape is modified. 
     */
    public static final String ID_SIZE_PROP = "WidgetSize";

    /** 
     * Property ID to use when the list of outgoing connections is modified. 
     */
    public static final String ID_SOURCE_CONNECTIONS_PROP = "WidgetSourceConn";

    /** 
     * Property ID to use when the list of incoming connections is modified. 
     */
    public static final String ID_TARGET_CONNECTIONS_PROP = "WidgetTargetConn";

    public static final String WIDGET_TAB = "tab";
    public static final String WIDGET_TYPE = "widget type";
    public static final String WIDGET_FIELDNAME = "fieldname";
    public static final String WIDGET_NAME = "name";
    public static final String WIDGET_TEXT = "text";
    public static final String WIDGET_TEXT_TYPE = "text type";
    public static final String WIDGET_LAYOUT_X = "layout X";
    public static final String WIDGET_LAYOUT_Y = "layout Y";
    public static final String WIDGET_LAYOUT_W = "layout width";
    public static final String WIDGET_LAYOUT_H = "layout height";
    public static final String WIDGET_DEFAULT = "default value";
    public static final String WIDGET_ORIENTATION = "orientation";
    public static final String WIDGET_CHECK = "default selection";
    public static final String WIDGET_ITEMS = "items (semicolon separated)";
    public static final String WIDGET_COMBOITEMS = "items (semicolon separated or file path)";

    public static final Dimension DEFAULT_DIMENSION = new Dimension(300, 30);
    public static final String[] TEXT_TYPES = {"string", "integer", "double"};
    public static final String[] ORIENTATION_TYPES = {"", "vertical", "horizontal"};
    public static final String[] CHECKBOX_TYPES = {"true", "false"};
    public static final String ITEMS_SEPARATOR = ";";
}
