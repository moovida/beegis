package eu.hydrologis.jgrass.formeditor.model.widgets;

import java.util.Properties;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;

import eu.hydrologis.jgrass.formeditor.model.AWidget;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.*;

public class WidgetFactory {

    public static AWidget createWidget( Properties properties ) {
        String type = properties.getProperty(TYPE_PROP);
        String fieldName = properties.getProperty(FIELDNAME_PROP);
        String sizeString = properties.getProperty(SIZE_PROP);
        String[] sizeSplit = sizeString.split(",");
        int width = Integer.parseInt(sizeSplit[0].trim());
        int height = Integer.parseInt(sizeSplit[1].trim());

        String locationString = properties.getProperty(LOCATION_PROP);
        String[] locSplit = locationString.split(",");
        int x = Integer.parseInt(locSplit[0].trim());
        int y = Integer.parseInt(locSplit[1].trim());

        AWidget widget = null;
        if (type.equals(TextFieldWidget.TYPE)) {
            widget = new TextFieldWidget();
            addTextFieldAttributes((TextFieldWidget) widget, properties);
        } else if (type.equals(TextAreaWidget.TYPE)) {
            widget = new TextAreaWidget();
            addTextAreaAttributes((TextAreaWidget) widget, properties);
        } else if (type.equals(LabelWidget.TYPE)) {
            widget = new LabelWidget();
            addLabelAttributes((LabelWidget) widget, properties);
        } else if (type.equals(IntegerFieldWidget.TYPE)) {
            widget = new IntegerFieldWidget();
            addIntegerFieldAttributes((IntegerFieldWidget) widget, properties);
        } else if (type.equals(DoubleFieldWidget.TYPE)) {
            widget = new DoubleFieldWidget();
            addDoubleFieldAttributes((DoubleFieldWidget) widget, properties);
        } else if (type.equals(ComboBoxWidget.TYPE)) {
            widget = new ComboBoxWidget();
            addComboBoxAttributes((ComboBoxWidget) widget, properties);
        } else if (type.equals(CheckBoxWidget.TYPE)) {
            widget = new CheckBoxWidget();
            addCheckBoxAttributes((CheckBoxWidget) widget, properties);
        } else if (type.equals(RadioButtonWidget.TYPE)) {
            widget = new RadioButtonWidget();
            addRadioButtonAttributes((RadioButtonWidget) widget, properties);
        } else {
            throw new IllegalArgumentException();
        }

        // add common attributes
        widget.setFieldname(fieldName);
        Point point = new Point(x, y);
        widget.setLocation(point);
        Dimension dim = new Dimension(width, height);
        widget.setSize(dim);

        return widget;
    }

    private static void addRadioButtonAttributes( RadioButtonWidget widget, Properties properties ) {
        String listString = properties.getProperty(LIST_PROP);
        widget.setList(listString);
        String selctionString = properties.getProperty(SELECTION_PROP);
        widget.setSelected(selctionString);
    }

    private static void addCheckBoxAttributes( CheckBoxWidget widget, Properties properties ) {
        String selctionString = properties.getProperty(SELECTION_PROP);
        widget.setSelected(selctionString);
    }

    private static void addComboBoxAttributes( ComboBoxWidget widget, Properties properties ) {
        String listString = properties.getProperty(LIST_PROP);
        widget.setList(listString);
        String selctionString = properties.getProperty(SELECTION_PROP);
        widget.setSelected(selctionString);
    }

    private static void addTextFieldAttributes( TextFieldWidget widget, Properties properties ) {
        String defaultString = properties.getProperty(DEFAULT_PROP);
        widget.setDefaultValue(defaultString);
    }

    private static void addTextAreaAttributes( TextAreaWidget widget, Properties properties ) {
        String defaultString = properties.getProperty(DEFAULT_PROP);
        widget.setDefaultValue(defaultString);
    }

    private static void addLabelAttributes( LabelWidget widget, Properties properties ) {
        String textString = properties.getProperty(TEXT_PROP);
        widget.setTextValue(textString);
    }

    private static void addDoubleFieldAttributes( DoubleFieldWidget widget, Properties properties ) {
        String defaultString = properties.getProperty(DEFAULT_PROP);
        widget.setDefaultValue(defaultString);
    }

    private static void addIntegerFieldAttributes( IntegerFieldWidget widget, Properties properties ) {
        String defaultString = properties.getProperty(DEFAULT_PROP);
        widget.setDefaultValue(defaultString);
    }

}
