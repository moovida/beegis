package eu.hydrologis.jgrass.formeditor.utils;

@SuppressWarnings("nls")
public class Constants {
    public static final int LOCATION_PIXEL_SNAP = 20;
    public static final int DIMENSION_PIXEL_SNAP = 20;
    
    public static final int INSETS = 5;
    public static final int FIELDNAME_OFFSET_X = 30;
    public static final int FIELDNAME_OFFSET_Y = 5;
    
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
}
