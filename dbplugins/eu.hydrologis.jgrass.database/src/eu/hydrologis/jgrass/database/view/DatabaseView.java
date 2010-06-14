package eu.hydrologis.jgrass.database.view;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.part.ViewPart;

import eu.hydrologis.jgrass.database.DatabasePlugin;
import eu.hydrologis.jgrass.database.core.DatabaseConnectionProperties;
import eu.hydrologis.jgrass.database.utils.ImageCache;

public class DatabaseView extends ViewPart {

    private List<DatabaseConnectionProperties> availableDatabaseConnectionProperties;

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

        TableViewer connectionsViewer = new TableViewer(connectionsGroup);
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
                // IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                // StringBuffer sb = new StringBuffer("Selection - ");
                // sb.append("tatal " + selection.size() + " items selected: ");
                // for( Iterator iterator = selection.iterator(); iterator.hasNext(); ) {
                // sb.append(iterator.next() + ", ");
                // }
                // System.out.println(sb);
            }
        });

        connectionsViewer.setInput(availableDatabaseConnectionProperties);
    }
    @Override
    public void setFocus() {
        // TODO Auto-generated method stub

    }

}
