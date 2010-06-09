package eu.hydrologis.jgrass.remotedb.preferences;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import eu.hydrologis.jgrass.remotedb.RemoteDbPlugin;

public class DatabaseTypeFieldEditor extends FieldEditor {

    private String[] dbItems = new String[]{RemoteDbPlugin.POSTGRESQL};
    private int columns = 3;
    private String chosenDb = RemoteDbPlugin.POSTGRESQL;
    private Combo typeCombo;

    public DatabaseTypeFieldEditor( String preferenceName, Composite parent ) {
        init(preferenceName, ""); //$NON-NLS-1$
        createControl(parent);
    }

    @Override
    protected void adjustForNumColumns( int numColumns ) {
        this.columns = numColumns;
    }

    @Override
    protected void doFillIntoGrid( Composite parent, int numColumns ) {

        Group typeGroup = new Group(parent, SWT.NONE);
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
        gridData.horizontalSpan = columns;
        typeGroup.setLayoutData(gridData);
        typeGroup.setLayout(new GridLayout(2, true));
        typeGroup.setText("Choose the database type (requires restart of JGrass to get active)");

        typeCombo = new Combo(typeGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
        GridData gridData2 = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
        gridData2.horizontalSpan = 2;
        typeCombo.setLayoutData(gridData2);
        typeCombo.setItems(dbItems);
        int index = 0;
        for( int i = 0; i < dbItems.length; i++ ) {
            if (dbItems[i].equals(chosenDb)) {
                index = i;
            }
        }
        typeCombo.select(index);
        typeCombo.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                int selected = typeCombo.getSelectionIndex();
                if (selected != -1) {
                    getPreferenceStore().setValue(DbParams.DATABASETYPE,
                            typeCombo.getItem(selected));
                }
            }
        });

    }

    protected void doLoad() {
        int index = 0;
        chosenDb = getPreferenceStore().getString(DbParams.DATABASETYPE);
        for( int i = 0; i < dbItems.length; i++ ) {
            if (dbItems[i].equals(chosenDb)) {
                index = i;
            }
        }
        typeCombo.select(index);
    }

    protected void doLoadDefault() {
        chosenDb = RemoteDbPlugin.POSTGRESQL;
        int index = 0;
        chosenDb = getPreferenceStore().getString(DbParams.DATABASETYPE);
        for( int i = 0; i < dbItems.length; i++ ) {
            if (dbItems[i].equals(chosenDb)) {
                index = i;
            }
        }
        typeCombo.select(index);
    }

    protected void doStore() {
        int selected = typeCombo.getSelectionIndex();
        if (selected != -1) {
            getPreferenceStore().setValue(DbParams.DATABASETYPE, typeCombo.getItem(selected));
        }
    }

    public int getNumberOfControls() {
        return 1;
    }

}
