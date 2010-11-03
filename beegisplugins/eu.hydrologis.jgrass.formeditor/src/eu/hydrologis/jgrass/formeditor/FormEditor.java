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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import net.refractions.udig.catalog.ID;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.internal.SetDefaultStyleProcessor;
import net.refractions.udig.project.ui.ApplicationGIS;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
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
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.geotools.data.FeatureSource;
import org.opengis.feature.type.AttributeDescriptor;

import eu.hydrologis.jgrass.formeditor.model.AWidget;
import eu.hydrologis.jgrass.formeditor.model.WidgetsDiagram;
import eu.hydrologis.jgrass.formeditor.model.widgets.TextFieldWidget;
import eu.hydrologis.jgrass.formeditor.model.widgets.WidgetFactory;
import eu.hydrologis.jgrass.formeditor.palette.FormEditorPaletteFactory;
import eu.hydrologis.jgrass.formeditor.parts.WidgetEditPartFactory;
import eu.hydrologis.jgrass.formeditor.parts.WidgetsTreeEditPartFactory;

/**
 * A graphical editor with flyout palette that can edit .shapes files.
 * The binding between the .shapes file extension and this editor is done in plugin.xml
 * @author Elias Volanakis
 */
public class FormEditor extends GraphicalEditorWithFlyoutPalette {
    public static String ID = "eu.hydrologis.jgrass.formeditor.FormEditor";

    /** This is the root of the editor's model. */
    private WidgetsDiagram diagram;

    /**
     * The formEditor is a singleton, this var is set when it is opened and made accessible.
     * 
     * <p>This is a hack, should be designed better.
     */
    private static List<AttributeDescriptor> attributeDescriptors = new ArrayList<AttributeDescriptor>();
    private static  String[] fieldNamesArrays;

    /** Palette component, holding the tools and shapes. */
    private static PaletteRoot PALETTE_MODEL;


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

    /* (non-Javadoc)
     * @see org.eclipse.gef.ui.parts.GraphicalEditor#commandStackChanged(java.util.EventObject)
     */
    public void commandStackChanged( EventObject event ) {
        firePropertyChange(IEditorPart.PROP_DIRTY);
        super.commandStackChanged(event);
    }

    private void createOutputStream( OutputStream os ) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(os);
        oos.writeObject(getModel());
        oos.close();
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette#createPaletteViewerProvider()
     */
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
                return new SimpleFactory((Class) template);
            }
        };
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.ISaveablePart#doSave(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void doSave( IProgressMonitor monitor ) {
        try {
            saveToProperties();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // saveSerializing();
    }

    private void saveToProperties() throws IOException {
        File file = new File(((FileStoreEditorInput) getEditorInput()).getURI());
        BufferedWriter bW = new BufferedWriter(new FileWriter(file));
        try {
            if (diagram == null) {
                throw new IllegalArgumentException();
            }

            List<AWidget> widgets = diagram.getChildren();

            for( AWidget widget : widgets ) {
                String dumpString = widget.toDumpString();
                bW.write(dumpString);
            }
            
        } finally {
            bW.close();
        }
    }

    private void saveSerializing() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            createOutputStream(out);
            File file = new File(((FileStoreEditorInput) getEditorInput()).getURI());
            FileOutputStream fOs = new FileOutputStream(file);
            fOs.write(out.toByteArray());
            fOs.close();

            // file.setContents(new ByteArrayInputStream(out.toByteArray()), true, // keep saving,
            // even
            // // if IFile is out
            // // of sync with the
            // // Workspace
            // false, // dont keep history
            // monitor); // progress monitor
            getCommandStack().markSaveLocation();
            // } catch (CoreException ce) {
            // ce.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.ISaveablePart#doSaveAs()
     */
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

    public Object getAdapter( Class type ) {
        if (type == IContentOutlinePage.class)
            return new ShapesOutlinePage(new TreeViewer());
        return super.getAdapter(type);
    }

    WidgetsDiagram getModel() {
        return diagram;
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette#getPaletteRoot()
     */
    protected PaletteRoot getPaletteRoot() {
        if (PALETTE_MODEL == null)
            PALETTE_MODEL = FormEditorPaletteFactory.createPalette();
        return PALETTE_MODEL;
    }

    private void handleLoadException( Exception e ) {
        System.err.println("** Load failed. Using default model. **");
        e.printStackTrace();
        diagram = new WidgetsDiagram();
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

    /* (non-Javadoc)
     * @see org.eclipse.ui.ISaveablePart#isSaveAsAllowed()
     */
    public boolean isSaveAsAllowed() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.EditorPart#setInput(org.eclipse.ui.IEditorInput)
     */
    protected void setInput( IEditorInput input ) {
        super.setInput(input);
        try {
            IMap activeMap = ApplicationGIS.getActiveMap();
            if (activeMap != null) {
                ILayer selectedLayer = activeMap.getEditManager().getSelectedLayer();
                if (selectedLayer.hasResource(FeatureSource.class)) {
                    attributeDescriptors = selectedLayer.getSchema().getAttributeDescriptors();

                    fieldNamesArrays = new String[attributeDescriptors.size()];
                    for( int i = 0; i < attributeDescriptors.size(); i++ ) {
                        AttributeDescriptor attributeDescriptor = attributeDescriptors.get(i);
                        String localName = attributeDescriptor.getLocalName();
                        fieldNamesArrays[i] = localName;
                    }
                }
            }

            loadFromProperties(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadFromProperties( IEditorInput input ) throws IOException {
        File file = new File(((FileStoreEditorInput) input).getURI());

        HashMap<String, Properties> name2PropertiesMap = new HashMap<String, Properties>();

        BufferedReader bR = new BufferedReader(new FileReader(file));
        String line = null;
        while( (line = bR.readLine()) != null ) {
            if (!line.contains("=") || line.startsWith("#")) {
                continue;
            }

            String[] split = line.split("\\.");
            String fieldName = split[0];

            Properties properties = name2PropertiesMap.get(fieldName);
            if (properties == null) {
                properties = new Properties();
                name2PropertiesMap.put(fieldName, properties);
            }

            line = line.replaceFirst(fieldName + "\\.", "");
            String[] propSplit = line.split("=");
            String name = propSplit[0].trim();
            String value = "";
            if (propSplit.length > 1) {
                value = propSplit[1].trim();
            }
            properties.put(name, value);
        }

        diagram = new WidgetsDiagram();

        Set<Entry<String, Properties>> entrySet = name2PropertiesMap.entrySet();
        for( Entry<String, Properties> entry : entrySet ) {
            Properties properties = entry.getValue();

            AWidget widget = WidgetFactory.createWidget(properties);

            diagram.addChild(widget);
        }

    }

    private void loadSerialized( IEditorInput input ) {
        try {
            File file = new File(((FileStoreEditorInput) input).getURI());
            if (file != null) {
                FileInputStream fIs = new FileInputStream(file);
                ObjectInputStream in = new ObjectInputStream(fIs);
                diagram = (WidgetsDiagram) in.readObject();
                in.close();
                setPartName(file.getName());
            } else {
                setPartName("test");
                diagram = new WidgetsDiagram();
            }
        } catch (IOException e) {
            handleLoadException(e);
        } catch (ClassNotFoundException e) {
            handleLoadException(e);
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

        /* (non-Javadoc)
         * @see org.eclipse.ui.part.IPage#createControl(org.eclipse.swt.widgets.Composite)
         */
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

        /* (non-Javadoc)
         * @see org.eclipse.ui.part.IPage#dispose()
         */
        public void dispose() {
            // unhook outline viewer
            getSelectionSynchronizer().removeViewer(getViewer());
            // dispose
            super.dispose();
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.part.IPage#getControl()
         */
        public Control getControl() {
            return getViewer().getControl();
        }

        /**
         * @see org.eclipse.ui.part.IPageBookViewPage#init(org.eclipse.ui.part.IPageSite)
         */
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