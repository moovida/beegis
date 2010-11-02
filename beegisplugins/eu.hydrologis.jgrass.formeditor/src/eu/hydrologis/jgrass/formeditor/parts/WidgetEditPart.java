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
import org.eclipse.swt.graphics.Image;

import eu.hydrologis.jgrass.formeditor.model.Connection;
import eu.hydrologis.jgrass.formeditor.model.AModelElement;
import eu.hydrologis.jgrass.formeditor.model.AWidget;
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
import eu.hydrologis.jgrass.formeditor.utils.ImageCache;
import static eu.hydrologis.jgrass.formeditor.utils.Constants.*;

/**
 * EditPart used for Shape instances (more specific for EllipticalShape and
 * RectangularShape instances).
 * <p>This edit part must implement the PropertyChangeListener interface, 
 * so it can be notified of property changes in the corresponding model element.
 * </p>
 * 
 * @author Elias Volanakis
 */
class WidgetEditPart extends AbstractGraphicalEditPart implements PropertyChangeListener, NodeEditPart {

    private ConnectionAnchor anchor;

    /**
     * Upon activation, attach to the model element as a property change listener.
     */
    public void activate() {
        if (!isActive()) {
            super.activate();
            ((AModelElement) getModel()).addPropertyChangeListener(this);
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
                cmd.setTarget((AWidget) getHost().getModel());
                return cmd;
            }
            /* (non-Javadoc)
             * @see org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#getConnectionCreateCommand(org.eclipse.gef.requests.CreateConnectionRequest)
             */
            protected Command getConnectionCreateCommand( CreateConnectionRequest request ) {
                AWidget source = (AWidget) getHost().getModel();
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
                AWidget newSource = (AWidget) getHost().getModel();
                ConnectionReconnectCommand cmd = new ConnectionReconnectCommand(conn);
                cmd.setNewSource(newSource);
                return cmd;
            }
            /* (non-Javadoc)
             * @see org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#getReconnectTargetCommand(org.eclipse.gef.requests.ReconnectRequest)
             */
            protected Command getReconnectTargetCommand( ReconnectRequest request ) {
                Connection conn = (Connection) request.getConnectionEditPart().getModel();
                AWidget newTarget = (AWidget) getHost().getModel();
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
        f.setBackgroundColor(ColorConstants.lightGray);
        return f;
    }

    /**
     * Return a IFigure depending on the instance of the current model element.
     * This allows this EditPart to be used for both sublasses of Shape. 
     */
    private IFigure createFigureForModel() {
        Object model = getModel();
        if (model instanceof TextFieldWidget) {
            Image textImage = ImageCache.getInstance().getImage(ImageCache.TEXT_ICON_24);
            return new WidgetTextFigure((AWidget) model, textImage);
        } else if (model instanceof TextAreaWidget) {
            Image textAreaImage = ImageCache.getInstance().getImage(ImageCache.TEXTAREA_ICON_24);
            return new WidgetTextFigure((AWidget) model, textAreaImage);
        } else if (model instanceof LabelWidget) {
            Image labelImage = ImageCache.getInstance().getImage(ImageCache.LABEL_ICON_24);
            return new WidgetTextFigure((AWidget) model, labelImage);
        } else if (model instanceof IntegerFieldWidget) {
            Image integerImage = ImageCache.getInstance().getImage(ImageCache.TEXT_INTEGER_ICON_24);
            return new WidgetTextFigure((AWidget) model, integerImage);
        } else if (model instanceof DoubleFieldWidget) {
            Image doubleImage = ImageCache.getInstance().getImage(ImageCache.TEXT_DOUBLE_ICON_24);
            return new WidgetTextFigure((AWidget) model, doubleImage);
        } else if (model instanceof ComboBoxWidget) {
            Image comboImage = ImageCache.getInstance().getImage(ImageCache.COMBO_ICON_24);
            return new WidgetTextFigure((AWidget) model, comboImage);
        } else if (model instanceof CheckBoxWidget) {
            Image checkImage = ImageCache.getInstance().getImage(ImageCache.CHECK_ICON_24);
            return new WidgetTextFigure((AWidget) model, checkImage);
        } else if (model instanceof RadioButtonWidget) {
            Image radioImage = ImageCache.getInstance().getImage(ImageCache.RADIO_ICON_24);
            return new WidgetTextFigure((AWidget) model, radioImage);
        } else {
            // if Shapes gets extended the conditions above must be updated
            throw new IllegalArgumentException();
        }
    }

    /**
     * Upon deactivation, detach from the model element as a property change listener.
     */
    public void deactivate() {
        if (isActive()) {
            super.deactivate();
            ((AModelElement) getModel()).removePropertyChangeListener(this);
        }
    }

    private AWidget getCastedModel() {
        return (AWidget) getModel();
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
        if (SIZE_PROP.equals(prop) || LOCATION_PROP.equals(prop) || FIELDNAME_PROP.equals(prop)) {
            refreshVisuals();
        } else if (SOURCE_CONNECTIONS_PROP.equals(prop)) {
            refreshSourceConnections();
        } else if (TARGET_CONNECTIONS_PROP.equals(prop)) {
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