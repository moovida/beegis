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
package eu.hydrologis.jgrass.database.view;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.ui.ApplicationGIS;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.part.ViewPart;

import eu.hydrologis.jgrass.database.DatabasePlugin;
import eu.hydrologis.jgrass.database.core.ConnectionManager;
import eu.hydrologis.jgrass.database.core.DatabaseConnectionProperties;
import eu.hydrologis.jgrass.database.core.h2.H2DatabaseConnection;
import eu.hydrologis.jgrass.database.core.postgres.PostgresDatabaseConnection;
import eu.hydrologis.jgrass.database.utils.ImageCache;

/**
 * The database view.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class DatabaseView extends ViewPart {
    
    public static final String ID = "eu.hydrologis.jgrass.database.catalogview";

    private HashMap<DatabaseConnectionProperties, DatabaseConnectionPropertiesWidget> widgetMap = new HashMap<DatabaseConnectionProperties, DatabaseConnectionPropertiesWidget>();

    private List<DatabaseConnectionProperties> availableDatabaseConnectionProperties;
    private Composite propertiesComposite;
    private StackLayout propertiesStackLayout;

    private DatabaseConnectionProperties currentSelectedConnectionProperties;

    private TableViewer connectionsViewer;

    public DatabaseView() {
        try {
            DatabasePlugin.getDefault().getActiveDatabaseConnection();
            availableDatabaseConnectionProperties = DatabasePlugin.getDefault().getAvailableDatabaseConnectionProperties();
        } catch (Exception e) {
            // exception handled at connection time
            e.printStackTrace();
        }
    }

    @Override
    public void createPartControl( Composite parent ) {
        Composite mainComposite = new Composite(parent, SWT.None);
        mainComposite.setLayout(new GridLayout(1, false));
        mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));

        Group connectionsGroup = new Group(mainComposite, SWT.BORDER | SWT.SHADOW_ETCHED_IN);
        connectionsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        connectionsGroup.setLayout(new GridLayout(3, true));
        connectionsGroup.setText("Connections");

        Composite connectionsListComposite = new Composite(connectionsGroup, SWT.NONE);
        connectionsListComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        connectionsListComposite.setLayout(new GridLayout(2, false));

        connectionsViewer = createTableViewer(connectionsListComposite);
        connectionsViewer.setInput(availableDatabaseConnectionProperties);
        addFilterButtons(connectionsListComposite, connectionsViewer);

        ScrolledComposite scrolledComposite = new ScrolledComposite(connectionsGroup, SWT.BORDER | SWT.V_SCROLL);
        scrolledComposite.setLayout(new GridLayout(1, false));

        propertiesComposite = new Composite(scrolledComposite, SWT.NONE);
        propertiesStackLayout = new StackLayout();
        propertiesComposite.setLayout(propertiesStackLayout);
        GridData propertiesCompositeGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        // propertiesCompositeGD.horizontalSpan = 2;
        propertiesComposite.setLayoutData(propertiesCompositeGD);
        Label l = new Label(propertiesComposite, SWT.SHADOW_ETCHED_IN);
        l.setText("No item selected");
        propertiesStackLayout.topControl = l;

        scrolledComposite.setContent(propertiesComposite);
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);
        // scrolledComposite.setMinWidth(400);
        scrolledComposite.setMinHeight(300);
        GridData scrolledCompositeGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        scrolledCompositeGD.horizontalSpan = 2;
        scrolledComposite.setLayoutData(scrolledCompositeGD);
    }

    private TableViewer createTableViewer( Composite connectionsListComposite ) {
        final TableViewer connectionsViewer = new TableViewer(connectionsListComposite);
        Table table = connectionsViewer.getTable();
        GridData tableGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        tableGD.horizontalSpan = 2;
        table.setLayoutData(tableGD);
        connectionsViewer.setContentProvider(new IStructuredContentProvider(){
            public Object[] getElements( Object inputElement ) {
                DatabaseConnectionProperties[] array = (DatabaseConnectionProperties[]) availableDatabaseConnectionProperties
                        .toArray(new DatabaseConnectionProperties[availableDatabaseConnectionProperties.size()]);
                return array;
            }
            public void dispose() {
            }
            public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
            }
        });

        connectionsViewer.setLabelProvider(new LabelProvider(){
            public Image getImage( Object element ) {
                if (element instanceof DatabaseConnectionProperties) {
                    DatabaseConnectionProperties connProp = (DatabaseConnectionProperties) element;

                    Image image = null;
                    if (ConnectionManager.isLocal(connProp)) {
                        if (connProp.isActive()) {
                            image = ImageCache.getInstance().getImage(ImageCache.LOCAL_DB_ACTIVE);
                            return image;
                        } else {
                            image = ImageCache.getInstance().getImage(ImageCache.LOCAL_DB);
                            return image;
                        }
                    } else {
                        if (connProp.isActive()) {
                            image = ImageCache.getInstance().getImage(ImageCache.REMOTE_DB_ACTIVE);
                            return image;
                        } else {
                            image = ImageCache.getInstance().getImage(ImageCache.REMOTE_DB);
                            return image;
                        }
                    }

                }
                return null;
            }

            public String getText( Object element ) {
                if (element instanceof DatabaseConnectionProperties) {
                    DatabaseConnectionProperties connProp = (DatabaseConnectionProperties) element;
                    return connProp.getTitle();
                }
                return "";
            }
        });

        connectionsViewer.addSelectionChangedListener(new ISelectionChangedListener(){

            public void selectionChanged( SelectionChangedEvent event ) {
                if (!(event.getSelection() instanceof IStructuredSelection)) {
                    return;
                }
                IStructuredSelection sel = (IStructuredSelection) event.getSelection();

                Object selectedItem = sel.getFirstElement();
                if (selectedItem == null) {
                    // unselected, show empty panel
                    return;
                }
                if (selectedItem instanceof DatabaseConnectionProperties) {
                    currentSelectedConnectionProperties = (DatabaseConnectionProperties) selectedItem;
                    DatabaseConnectionPropertiesWidget widget = widgetMap.get(currentSelectedConnectionProperties);
                    if (widget == null) {
                        widget = new DatabaseConnectionPropertiesWidget(DatabaseView.this);
                        widgetMap.put(currentSelectedConnectionProperties, widget);
                    }
                    Control propControl = widget.getComposite(currentSelectedConnectionProperties, propertiesComposite);
                    propertiesStackLayout.topControl = propControl;
                } else {
                    Label l = new Label(propertiesComposite, SWT.SHADOW_ETCHED_IN);
                    l.setText("No item selected");
                    propertiesStackLayout.topControl = l;
                }
                propertiesComposite.layout(true);
            }
        });
        return connectionsViewer;
    }

    private void addFilterButtons( Composite connectionsListComposite, final TableViewer connectionsViewer ) {
        Button filterActive = new Button(connectionsListComposite, SWT.CHECK);
        filterActive.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        filterActive.setText("Filter active");
        final ActiveFilter activeFilter = new ActiveFilter();

        filterActive.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent event ) {
                if (((Button) event.widget).getSelection())
                    connectionsViewer.addFilter(activeFilter);
                else
                    connectionsViewer.removeFilter(activeFilter);
            }
        });
        Button filterProjectmatch = new Button(connectionsListComposite, SWT.CHECK);
        filterProjectmatch.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        filterProjectmatch.setText("Filter project");
        final ProjectMatchFilter projectFilter = new ProjectMatchFilter();

        filterProjectmatch.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent event ) {
                if (((Button) event.widget).getSelection())
                    connectionsViewer.addFilter(projectFilter);
                else
                    connectionsViewer.removeFilter(projectFilter);
            }
        });
    }
    @Override
    public void setFocus() {
        // TODO Auto-generated method stub

    }

    private static class ActiveFilter extends ViewerFilter {
        public boolean select( Viewer arg0, Object arg1, Object arg2 ) {
            return ((DatabaseConnectionProperties) arg2).isActive();
        }
    }

    private static class ProjectMatchFilter extends ViewerFilter {
        public boolean select( Viewer arg0, Object arg1, Object arg2 ) {
            String name = ((DatabaseConnectionProperties) arg2).getTitle();
            String projectName = ApplicationGIS.getActiveProject().getName();
            if (name.matches("(?i).*" + projectName + ".*")) {
                return true;
            }
            return false;
        }
    }

    public void createNewLocalDatabaseDefinition() {
        DatabaseConnectionProperties props = new DatabaseConnectionProperties();
        props.put("TYPE", H2DatabaseConnection.TYPE);
        props.put("ISACTIVE", "false");
        props.put("TITLE", "New Local Database");
        props.put("DRIVER", H2DatabaseConnection.DRIVER);
        props.put("DATABASE", "database");
        props.put("PORT", "9092");
        props.put("USER", "sa");
        props.put("PASS", "");
        props.put("PATH", "");

        availableDatabaseConnectionProperties.add(props);
        relayout();
    }

    public void createNewRemoteDatabaseDefinition() {
        DatabaseConnectionProperties props = new DatabaseConnectionProperties();
        props.put("TYPE", PostgresDatabaseConnection.TYPE);
        props.put("ISACTIVE", "false");
        props.put("TITLE", "New Remote Database");
        props.put("DRIVER", PostgresDatabaseConnection.DRIVER);
        props.put("DATABASE", "databasename");
        props.put("PORT", "5432");
        props.put("USER", "");
        props.put("PASS", "");
        props.put("PATH", "");
        
        availableDatabaseConnectionProperties.add(props);
        relayout();
    }
    
    public DatabaseConnectionProperties getCurrentSelectedConnectionProperties() {
        return currentSelectedConnectionProperties;
    }

    public void removeCurrentSelectedDatabaseDefinition() {
        if (currentSelectedConnectionProperties != null) {
            if (currentSelectedConnectionProperties.isActive()) {
                DatabasePlugin.getDefault().disconnectActiveDatabaseConnection();
            }
            availableDatabaseConnectionProperties.remove(currentSelectedConnectionProperties);
            relayout();
        }
    }

    public void relayout() {

        Display.getDefault().asyncExec(new Runnable(){
            public void run() {
                // refresh widgets
                Collection<DatabaseConnectionPropertiesWidget> widgets = widgetMap.values();
                for( DatabaseConnectionPropertiesWidget widget : widgets ) {
                    widget.refresh();
                }
                // connectionsViewer.getTable().removeAll();
                // connectionsViewer.setInput(null);
                connectionsViewer.setInput(availableDatabaseConnectionProperties);

                List<ILayer> mapLayers = ApplicationGIS.getActiveMap().getMapLayers();
                for( ILayer iLayer : mapLayers ) {
                    iLayer.refresh(null);
                }
            }
        });
    }

}
