package eu.hydrologis.jgrass.database.view;

import java.util.List;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.part.ViewPart;

import eu.hydrologis.jgrass.database.DatabasePlugin;
import eu.hydrologis.jgrass.database.core.DatabaseConnectionProperties;
import eu.hydrologis.jgrass.database.utils.ImageCache;

public class DatabaseView extends ViewPart {

    private List<DatabaseConnectionProperties> availableDatabaseConnectionProperties;
    private Composite propertiesComposite;
    private StackLayout propertiesStackLayout;

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

        Group connectionsGroup = new Group(mainComposite, SWT.SHADOW_ETCHED_IN);
        connectionsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        connectionsGroup.setLayout(new GridLayout(2, true));
        connectionsGroup.setText("Connections");

        Composite connectionsListComposite = new Composite(connectionsGroup, SWT.NONE);
        connectionsListComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        connectionsListComposite.setLayout(new GridLayout(1, false));

        // connections panel
        final TableViewer connectionsViewer = createTableViewer(connectionsListComposite);
        connectionsViewer.setInput(availableDatabaseConnectionProperties);
        addFilterButton(connectionsListComposite, connectionsViewer);
        
        propertiesComposite = new Composite(mainComposite, SWT.NONE);
        propertiesStackLayout = new StackLayout();
        propertiesComposite.setLayout(propertiesStackLayout);
        propertiesComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    }

    private TableViewer createTableViewer( Composite connectionsListComposite ) {
        final TableViewer connectionsViewer = new TableViewer(connectionsListComposite);
        Table table = connectionsViewer.getTable();
        table.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));
        connectionsViewer.setContentProvider(new IStructuredContentProvider(){
            public Object[] getElements( Object inputElement ) {
                DatabaseConnectionProperties[] array = (DatabaseConnectionProperties[]) availableDatabaseConnectionProperties
                        .toArray(new DatabaseConnectionProperties[availableDatabaseConnectionProperties.size()]);
                return array;
            }
            public void dispose() {
                System.out.println("Disposing ...");
            }
            public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
                System.out.println("Input changed: old=" + oldInput + ", new=" + newInput);
            }
        });

        connectionsViewer.setLabelProvider(new LabelProvider(){
            public Image getImage( Object element ) {
                if (element instanceof DatabaseConnectionProperties) {
                    // DatabaseConnectionProperties connProp = (DatabaseConnectionProperties)
                    // element;
                    Image image = ImageCache.getInstance().getImage(ImageCache.LOCAL_DB);
                    return image;
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
                if (!(event.getSelection() instanceof TreeSelection)) {
                    return;
                }
                IStructuredSelection sel = (IStructuredSelection) event.getSelection();

                Object selectedItem = sel.getFirstElement();
                if (selectedItem == null) {
                    // unselected, show empty panel
                    return;
                }
                if (selectedItem instanceof DatabaseConnectionProperties) {
                    MonitoringPoint p = (MonitoringPoint) selectedItem;
                    Control propControl = p.getPropertiesWidget(propertiesComposite);
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

    private void addFilterButton( Composite connectionsListComposite, final TableViewer connectionsViewer ) {
        Button filterActive = new Button(connectionsListComposite, SWT.CHECK);
        filterActive.setText("Filter active");
        final ActiveFilter filter = new ActiveFilter();

        filterActive.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent event ) {
                if (((Button) event.widget).getSelection())
                    connectionsViewer.addFilter(filter);
                else
                    connectionsViewer.removeFilter(filter);
            }
        });
    }
    @Override
    public void setFocus() {
        // TODO Auto-generated method stub

    }

    private class ActiveFilter extends ViewerFilter {
        public boolean select( Viewer arg0, Object arg1, Object arg2 ) {
            return ((DatabaseConnectionProperties) arg2).isActive();
        }
    }

}
