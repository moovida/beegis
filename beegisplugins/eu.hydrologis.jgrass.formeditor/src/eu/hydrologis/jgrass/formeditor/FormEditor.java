/*******************************************************************************
 * Copyright (c) 2004, 2005 Elias Volanakis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Elias Volanakis - initial API and implementation
�*******************************************************************************/
package eu.hydrologis.jgrass.formeditor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.ui.ApplicationGIS;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.dnd.TemplateTransferDragSourceListener;
import org.eclipse.gef.dnd.TemplateTransferDropTargetListener;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.gef.requests.SimpleFactory;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.palette.PaletteViewerProvider;
import org.eclipse.gef.ui.parts.ContentOutlinePage;
import org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette;
import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
import org.eclipse.gef.ui.parts.TreeViewer;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.util.TransferDropTargetListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.geotools.data.FeatureSource;
import org.opengis.feature.type.AttributeDescriptor;

import com.vividsolutions.jts.geom.Geometry;

import eu.hydrologis.jgrass.formeditor.model.WidgetsDiagram;
import eu.hydrologis.jgrass.formeditor.palette.FormEditorPaletteFactory;
import eu.hydrologis.jgrass.formeditor.parts.WidgetEditPartFactory;
import eu.hydrologis.jgrass.formeditor.parts.WidgetsTreeEditPartFactory;
import eu.hydrologis.jgrass.formeditor.utils.FormContentLoadHelper;
import eu.hydrologis.jgrass.formeditor.utils.FormContentSaveHelper;

/**
 * A graphical editor with flyout palette that can edit .shapes files.
 * The binding between the .shapes file extension and this editor is done in plugin.xml
 * @author Elias Volanakis
 */
public class FormEditor extends GraphicalEditorWithFlyoutPalette {
    public static String ID = "eu.hydrologis.jgrass.formeditor.FormEditor"; //$NON-NLS-1$

    /** This is the root of the editor's model. */
    private WidgetsDiagram diagram;

    private static List<AttributeDescriptor> attributeDescriptors = new ArrayList<AttributeDescriptor>();
    private static String[] fieldNamesArrays = new String[0];

    /** Palette component, holding the tools and shapes. */
    private static PaletteRoot paletteModel;

    /** Create a new ShapesEditor instance. This is called by the Workspace. */
    public FormEditor() {
        setEditDomain(new DefaultEditDomain(this));
    }

    public static List<AttributeDescriptor> getAttributeDescriptors() {
        return attributeDescriptors;
    }

    public static String[] getFieldNamesArrays() {
        return fieldNamesArrays;
    }

    private static String lastTabNameInserted = "0";
    public static String getLastTabNameInserted() {
        return lastTabNameInserted;
    }
    public static void setLastTabNameInserted( String newlastTabNameInserted ) {
        lastTabNameInserted = newlastTabNameInserted;
    }

    /**
     * Configure the graphical viewer before it receives contents.
     * <p>This is the place to choose an appropriate RootEditPart and EditPartFactory
     * for your editor. The RootEditPart determines the behavior of the editor's "work-area".
     * For example, GEF includes zoomable and scrollable root edit parts. The EditPartFactory
     * maps model elements to edit parts (controllers).</p>
     * @see org.eclipse.gef.ui.parts.GraphicalEditor#configureGraphicalViewer()
     */
    protected void configureGraphicalViewer() {
        super.configureGraphicalViewer();

        GraphicalViewer viewer = getGraphicalViewer();
        viewer.setEditPartFactory(new WidgetEditPartFactory());
        viewer.setRootEditPart(new ScalableFreeformRootEditPart());
        viewer.setKeyHandler(new GraphicalViewerKeyHandler(viewer));

        // configure the context menu provider
        ContextMenuProvider cmProvider = new FormEditorContextMenuProvider(viewer, getActionRegistry());
        viewer.setContextMenu(cmProvider);
        getSite().registerContextMenu(cmProvider, viewer);
    }

    public void commandStackChanged( EventObject event ) {
        firePropertyChange(IEditorPart.PROP_DIRTY);
        super.commandStackChanged(event);
    }

    private void createOutputStream( OutputStream os ) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(os);
        oos.writeObject(getModel());
        oos.close();
    }

    protected PaletteViewerProvider createPaletteViewerProvider() {
        return new PaletteViewerProvider(getEditDomain()){
            protected void configurePaletteViewer( PaletteViewer viewer ) {
                super.configurePaletteViewer(viewer);
                // create a drag source listener for this palette viewer
                // together with an appropriate transfer drop target listener, this will enable
                // model element creation by dragging a CombinatedTemplateCreationEntries
                // from the palette into the editor
                // @see ShapesEditor#createTransferDropTargetListener()
                viewer.addDragSourceListener(new TemplateTransferDragSourceListener(viewer));
            }
        };
    }

    /**
     * Create a transfer drop target listener. When using a CombinedTemplateCreationEntry
     * tool in the palette, this will enable model element creation by dragging from the palette.
     * @see #createPaletteViewerProvider()
     */
    private TransferDropTargetListener createTransferDropTargetListener() {
        return new TemplateTransferDropTargetListener(getGraphicalViewer()){
            protected CreationFactory getFactory( Object template ) {
                return new SimpleFactory((Class< ? >) template);
            }
        };
    }

    public void doSave( IProgressMonitor monitor ) {
        try {
            File file = new File(((FileStoreEditorInput) getEditorInput()).getURI());
            FormContentSaveHelper saveHelper = new FormContentSaveHelper(file, diagram.getChildren());
            saveHelper.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doSaveAs() {
        // Show a SaveAs dialog
        Shell shell = getSite().getWorkbenchWindow().getShell();
        SaveAsDialog dialog = new SaveAsDialog(shell);
        // dialog.setOriginalFile(((IFileEditorInput) getEditorInput()).getFile());
        dialog.open();

        IPath path = dialog.getResult();
        if (path != null) {
            // try to save the editor's contents under a different file name
            final IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
            try {
                new ProgressMonitorDialog(shell).run(false, // don't fork
                        false, // not cancelable
                        new WorkspaceModifyOperation(){ // run this operation
                            public void execute( final IProgressMonitor monitor ) {
                                try {
                                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                                    createOutputStream(out);
                                    file.create(new ByteArrayInputStream(out.toByteArray()), // contents
                                            true, // keep saving, even if IFile is out of sync with
                                            // the Workspace
                                            monitor); // progress monitor
                                } catch (CoreException ce) {
                                    ce.printStackTrace();
                                } catch (IOException ioe) {
                                    ioe.printStackTrace();
                                }
                            }
                        });
                // set input to the new file
                setInput(new FileEditorInput(file));
                getCommandStack().markSaveLocation();
            } catch (InterruptedException ie) {
                // should not happen, since the monitor dialog is not cancelable
                ie.printStackTrace();
            } catch (InvocationTargetException ite) {
                ite.printStackTrace();
            }
        }
    }

    @SuppressWarnings("rawtypes")
    public Object getAdapter( Class type ) {
        if (type == IContentOutlinePage.class)
            return new ShapesOutlinePage(new TreeViewer());
        return super.getAdapter(type);
    }

    WidgetsDiagram getModel() {
        return diagram;
    }

    protected PaletteRoot getPaletteRoot() {
        if (paletteModel == null)
            paletteModel = FormEditorPaletteFactory.createPalette();
        return paletteModel;
    }

    /**
     * Set up the editor's inital content (after creation).
     * @see org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette#initializeGraphicalViewer()
     */
    protected void initializeGraphicalViewer() {
        super.initializeGraphicalViewer();
        GraphicalViewer viewer = getGraphicalViewer();
        viewer.setContents(getModel()); // set the contents of this editor

        // listen for dropped parts
        viewer.addDropTargetListener(createTransferDropTargetListener());
    }

    public boolean isSaveAsAllowed() {
        return true;
    }

    protected void setInput( IEditorInput input ) {
        super.setInput(input);
        try {
            IMap activeMap = ApplicationGIS.getActiveMap();
            if (activeMap != null) {
                ILayer selectedLayer = activeMap.getEditManager().getSelectedLayer();
                if (selectedLayer != null && selectedLayer.hasResource(FeatureSource.class)) {
                    attributeDescriptors = selectedLayer.getSchema().getAttributeDescriptors();
                    List<String> fieldNamesList = new ArrayList<String>();
                    for( int i = 0; i < attributeDescriptors.size(); i++ ) {
                        AttributeDescriptor attributeDescriptor = attributeDescriptors.get(i);
                        Class< ? > binding = attributeDescriptor.getType().getBinding();
                        if (Geometry.class.isAssignableFrom(binding)) {
                            continue;
                        }
                        String localName = attributeDescriptor.getLocalName();
                        fieldNamesList.add(localName);
                    }
                    fieldNamesArrays = (String[]) fieldNamesList.toArray(new String[fieldNamesList.size()]);
                }
            }

            loadFromProperties(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadFromProperties( IEditorInput input ) throws Exception {
        URI uri = ((FileStoreEditorInput) input).getURI();
        File file = null;
        if (uri != null) {
            file = new File(uri);
        } else {
            file = new File(System.getProperty("user.home"), "default.form");
        }

        diagram = new WidgetsDiagram();

        long length = file.length();
        if (length > 0) {
            FormContentLoadHelper loadHelper = new FormContentLoadHelper(file, diagram);
            loadHelper.load();
        }

    }

    /**
     * Creates an outline pagebook for this editor.
     */
    public class ShapesOutlinePage extends ContentOutlinePage {
        /**
         * Create a new outline page for the shapes editor.
         * @param viewer a viewer (TreeViewer instance) used for this outline page
         * @throws IllegalArgumentException if editor is null
         */
        public ShapesOutlinePage( EditPartViewer viewer ) {
            super(viewer);
        }

        public void createControl( Composite parent ) {
            // create outline viewer page
            EditPartViewer viewer = getViewer();
            viewer.createControl(parent);
            // configure outline viewer
            viewer.setEditDomain(getEditDomain());
            viewer.setEditPartFactory(new WidgetsTreeEditPartFactory());
            // configure & add context menu to viewer
            ContextMenuProvider cmProvider = new FormEditorContextMenuProvider(viewer, getActionRegistry());
            viewer.setContextMenu(cmProvider);
            getSite().registerContextMenu("org.eclipse.gef.examples.shapes.outline.contextmenu", cmProvider,
                    getSite().getSelectionProvider());
            // hook outline viewer
            getSelectionSynchronizer().addViewer(viewer);
            // initialize outline viewer with model
            viewer.setContents(getModel());
            // show outline viewer
        }

        public void dispose() {
            // unhook outline viewer
            getSelectionSynchronizer().removeViewer(getViewer());
            // dispose
            super.dispose();
        }

        public Control getControl() {
            return getViewer().getControl();
        }

        public void init( IPageSite pageSite ) {
            super.init(pageSite);
            ActionRegistry registry = getActionRegistry();
            IActionBars bars = pageSite.getActionBars();
            String id = ActionFactory.UNDO.getId();
            bars.setGlobalActionHandler(id, registry.getAction(id));
            id = ActionFactory.REDO.getId();
            bars.setGlobalActionHandler(id, registry.getAction(id));
            id = ActionFactory.DELETE.getId();
            bars.setGlobalActionHandler(id, registry.getAction(id));
        }
    }

}