package eu.hydrologis.jgrass.remotedb.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import eu.hydrologis.jgrass.remotedb.RemoteDbPlugin;


public class DbPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private ScopedPreferenceStore preferences;

    public DbPreferencePage() {
        super(GRID);
        preferences = (ScopedPreferenceStore) RemoteDbPlugin.getDefault().getPreferenceStore();
        setPreferenceStore(preferences);
        setDescription(""); //$NON-NLS-1$
    }

    /**
     * Creates the field editors. Field editors are abstractions of the common GUI blocks needed to
     * manipulate various types of preferences. Each field editor knows how to save and restore
     * itself.
     */

    public void createFieldEditors() {
        addField(new DatabaseTypeFieldEditor(DbParams.DATABASETYPE, getFieldEditorParent()));
        addField(new IntegerFieldEditor(DbParams.PORT, "tcp port", getFieldEditorParent())); //$NON-NLS-1$
        addField(new StringFieldEditor(DbParams.USER, "user", getFieldEditorParent())); //$NON-NLS-1$
        StringFieldEditor pasField = new StringFieldEditor(DbParams.PASSWD, "passwd", getFieldEditorParent()); //$NON-NLS-1$
        pasField.getTextControl(getFieldEditorParent()).setEchoChar('*');
        addField(pasField);
        addField(new StringFieldEditor(DbParams.NAME, "database name", getFieldEditorParent())); //$NON-NLS-1$
        addField(new StringFieldEditor(DbParams.HOST, "host", getFieldEditorParent())); //$NON-NLS-1$
    }

    public void init( IWorkbench workbench ) {
    }
}
