/*******************************************************************************
 * Copyright (c) 2004, 2005 Elias Volanakis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *����Elias Volanakis - initial API and implementation
 *******************************************************************************/
package eu.hydrologis.jgrass.formeditor.parts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.ReconnectRequest;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import eu.hydrologis.jgrass.formeditor.FormEditorPlugin;
import eu.hydrologis.jgrass.formeditor.model.Connection;
import eu.hydrologis.jgrass.formeditor.model.ModelElement;
import eu.hydrologis.jgrass.formeditor.model.Widget;
import eu.hydrologis.jgrass.formeditor.model.commands.ConnectionCreateCommand;
import eu.hydrologis.jgrass.formeditor.model.commands.ConnectionReconnectCommand;
import eu.hydrologis.jgrass.formeditor.model.commands.WidgetComponentEditPolicy;
import eu.hydrologis.jgrass.formeditor.model.widgets.CheckBoxWidget;
import eu.hydrologis.jgrass.formeditor.model.widgets.ComboBoxWidget;
import eu.hydrologis.jgrass.formeditor.model.widgets.DoubleFieldWidget;
import eu.hydrologis.jgrass.formeditor.model.widgets.IntegerFieldWidget;
import eu.hydrologis.jgrass.formeditor.model.widgets.LabelWidget;
import eu.hydrologis.jgrass.formeditor.model.widgets.RadioButtonWidget;
import eu.hydrologis.jgrass.formeditor.model.widgets.TextAreaWidget;
import eu.hydrologis.jgrass.formeditor.model.widgets.TextFieldWidget;
import eu.hydrologis.jgrass.formeditor.model.widgets.WidgetTextFigure;

/**
 * EditPart used for Shape instances (more specific for EllipticalShape and
 * RectangularShape instances).
 * <p>This edit part must implement the PropertyChangeListener interface, 
 * so it can be notified of property changes in the corresponding model element.
 * </p>
 * 
 * @author Elias Volanakis
 */
class WidgetEditPart extends AbstractGraphicalEditPart
        implements
            PropertyChangeListener,
            NodeEditPart {

    private HashMap<String, Image> imageCache = new HashMap<String, Image>();

    private ConnectionAnchor anchor;

    /**
     * Upon activation, attach to the model element as a property change listener.
     */
    public void activate() {
        if (!isActive()) {
            super.activate();
            ((ModelElement) getModel()).addPropertyChangeListener(this);
        }
    }

    protected void createEditPolicies() {
        // allow removal of the associated model element
        installEditPolicy(EditPolicy.COMPONENT_ROLE, new WidgetComponentEditPolicy());
        // allow the creation of connections and
        // and the reconnection of connections between Shape instances
        installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new GraphicalNodeEditPolicy(){
            /* (non-Javadoc)
             * @see org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#getConnectionCompleteCommand(org.eclipse.gef.requests.CreateConnectionRequest)
             */
            protected Command getConnectionCompleteCommand( CreateConnectionRequest request ) {
                ConnectionCreateCommand cmd = (ConnectionCreateCommand) request.getStartCommand();
                cmd.setTarget((Widget) getHost().getModel());
                return cmd;
            }
            /* (non-Javadoc)
             * @see org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#getConnectionCreateCommand(org.eclipse.gef.requests.CreateConnectionRequest)
             */
            protected Command getConnectionCreateCommand( CreateConnectionRequest request ) {
                Widget source = (Widget) getHost().getModel();
                int style = ((Integer) request.getNewObjectType()).intValue();
                ConnectionCreateCommand cmd = new ConnectionCreateCommand(source, style);
                request.setStartCommand(cmd);
                return cmd;
            }
            /* (non-Javadoc)
             * @see org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#getReconnectSourceCommand(org.eclipse.gef.requests.ReconnectRequest)
             */
            protected Command getReconnectSourceCommand( ReconnectRequest request ) {
                Connection conn = (Connection) request.getConnectionEditPart().getModel();
                Widget newSource = (Widget) getHost().getModel();
                ConnectionReconnectCommand cmd = new ConnectionReconnectCommand(conn);
                cmd.setNewSource(newSource);
                return cmd;
            }
            /* (non-Javadoc)
             * @see org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#getReconnectTargetCommand(org.eclipse.gef.requests.ReconnectRequest)
             */
            protected Command getReconnectTargetCommand( ReconnectRequest request ) {
                Connection conn = (Connection) request.getConnectionEditPart().getModel();
                Widget newTarget = (Widget) getHost().getModel();
                ConnectionReconnectCommand cmd = new ConnectionReconnectCommand(conn);
                cmd.setNewTarget(newTarget);
                return cmd;
            }
        });
    }

    /*(non-Javadoc)
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
     */
    protected IFigure createFigure() {
        IFigure f = createFigureForModel();
        f.setOpaque(true); // non-transparent figure
        f.setBackgroundColor(ColorConstants.green);
        return f;
    }

    /**
     * Return a IFigure depending on the instance of the current model element.
     * This allows this EditPart to be used for both sublasses of Shape. 
     */
    private IFigure createFigureForModel() {
        Object model = getModel();
        if (model instanceof TextFieldWidget) {
            Image textImage = imageCache.get(TextFieldWidget.TYPE);
            if (textImage == null)
                textImage = createImage("icons/textfield.png");
            return new WidgetTextFigure((Widget) model, textImage);
        } else if (model instanceof TextAreaWidget) {
            Image textAreaImage = imageCache.get(TextAreaWidget.TYPE);
            if (textAreaImage == null)
                textAreaImage = createImage("icons/textarea.png");
            return new WidgetTextFigure((Widget) model, textAreaImage);
        } else if (model instanceof LabelWidget) {
            Image labelImage = imageCache.get(LabelWidget.TYPE);
            if (labelImage == null)
                labelImage = createImage("icons/label.png");
            return new WidgetTextFigure((Widget) model, labelImage);
        } else if (model instanceof IntegerFieldWidget) {
            Image integerImage = imageCache.get(IntegerFieldWidget.TYPE);
            if (integerImage == null)
                integerImage = createImage("icons/textfield_i.png");
            return new WidgetTextFigure((Widget) model, integerImage);
        } else if (model instanceof DoubleFieldWidget) {
            Image doubleImage = imageCache.get(DoubleFieldWidget.TYPE);
            if (doubleImage == null)
                doubleImage = createImage("icons/textfield_d.png");
            return new WidgetTextFigure((Widget) model, doubleImage);
        } else if (model instanceof ComboBoxWidget) {
            Image comboImage = imageCache.get("combo");
            if (comboImage == null)
                comboImage = createImage("icons/combobox.png");
            return new WidgetTextFigure((Widget) model, comboImage);
        } else if (model instanceof CheckBoxWidget) {
            Image checkImage = imageCache.get("check");
            if (checkImage == null)
                checkImage = createImage("icons/checkbox.png");
            return new WidgetTextFigure((Widget) model, checkImage);
        } else if (model instanceof RadioButtonWidget) {
            Image radioImage = imageCache.get("radio");
            if (radioImage == null)
                radioImage = createImage("icons/radiobutton.png");
            return new WidgetTextFigure((Widget) model, radioImage);
        } else {
            // if Shapes gets extended the conditions above must be updated
            throw new IllegalArgumentException();
        }
    }

    private static Image createImage( String name ) {
        ImageDescriptor imageD = AbstractUIPlugin.imageDescriptorFromPlugin(
                FormEditorPlugin.PLUGIN_ID, name);
        Image image = imageD.createImage();
        return image;
    }

    /**
     * Upon deactivation, detach from the model element as a property change listener.
     */
    public void deactivate() {
        Collection<Image> values = imageCache.values();
        for( Image image : values ) {
            image.dispose();
        }
        imageCache.clear();
        if (isActive()) {
            super.deactivate();
            ((ModelElement) getModel()).removePropertyChangeListener(this);
        }
    }

    private Widget getCastedModel() {
        return (Widget) getModel();
    }

    protected ConnectionAnchor getConnectionAnchor() {
        if (anchor == null) {
            if (getModel() instanceof TextFieldWidget)
                anchor = new ChopboxAnchor(getFigure());
            else if (getModel() instanceof TextAreaWidget)
                anchor = new ChopboxAnchor(getFigure());
            else if (getModel() instanceof LabelWidget)
                anchor = new ChopboxAnchor(getFigure());
            else if (getModel() instanceof IntegerFieldWidget)
                anchor = new ChopboxAnchor(getFigure());
            else if (getModel() instanceof DoubleFieldWidget)
                anchor = new ChopboxAnchor(getFigure());
            else if (getModel() instanceof ComboBoxWidget)
                anchor = new ChopboxAnchor(getFigure());
            else if (getModel() instanceof CheckBoxWidget)
                anchor = new ChopboxAnchor(getFigure());
            else if (getModel() instanceof RadioButtonWidget)
                anchor = new ChopboxAnchor(getFigure());
            else
                // if Shapes gets extended the conditions above must be updated
                throw new IllegalArgumentException("unexpected model");
        }
        return anchor;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#getModelSourceConnections()
     */
    protected List<Connection> getModelSourceConnections() {
        return getCastedModel().getSourceConnections();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#getModelTargetConnections()
     */
    protected List<Connection> getModelTargetConnections() {
        return getCastedModel().getTargetConnections();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.gef.NodeEditPart#getSourceConnectionAnchor(org.eclipse.gef.ConnectionEditPart)
     */
    public ConnectionAnchor getSourceConnectionAnchor( ConnectionEditPart connection ) {
        return getConnectionAnchor();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.gef.NodeEditPart#getSourceConnectionAnchor(org.eclipse.gef.Request)
     */
    public ConnectionAnchor getSourceConnectionAnchor( Request request ) {
        return getConnectionAnchor();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.gef.NodeEditPart#getTargetConnectionAnchor(org.eclipse.gef.ConnectionEditPart)
     */
    public ConnectionAnchor getTargetConnectionAnchor( ConnectionEditPart connection ) {
        return getConnectionAnchor();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.gef.NodeEditPart#getTargetConnectionAnchor(org.eclipse.gef.Request)
     */
    public ConnectionAnchor getTargetConnectionAnchor( Request request ) {
        return getConnectionAnchor();
    }

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange( PropertyChangeEvent evt ) {
        String prop = evt.getPropertyName();
        if (Widget.SIZE_PROP.equals(prop) || Widget.LOCATION_PROP.equals(prop)
                || Widget.FIELDNAME_PROP.equals(prop)) {
            refreshVisuals();
        } else if (Widget.SOURCE_CONNECTIONS_PROP.equals(prop)) {
            refreshSourceConnections();
        } else if (Widget.TARGET_CONNECTIONS_PROP.equals(prop)) {
            refreshTargetConnections();
        }
    }

    protected void refreshVisuals() {
        // notify parent container of changed position & location
        // if this line is removed, the XYLayoutManager used by the parent container
        // (the Figure of the ShapesDiagramEditPart), will not know the bounds of this figure
        // and will not draw it correctly.
        Rectangle bounds = new Rectangle(getCastedModel().getLocation(), getCastedModel().getSize());
        ((GraphicalEditPart) getParent()).setLayoutConstraint(this, getFigure(), bounds);
    }
}