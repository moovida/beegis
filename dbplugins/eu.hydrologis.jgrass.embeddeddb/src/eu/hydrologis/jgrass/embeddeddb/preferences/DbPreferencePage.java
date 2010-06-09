package eu.hydrologis.jgrass.embeddeddb.preferences;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import eu.hydrologis.jgrass.embeddeddb.EmbeddedDbPlugin;
import eu.hydrologis.jgrass.embeddeddb.messages.Messages;

public class DbPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private ScopedPreferenceStore preferences;

    public DbPreferencePage() {
        super(GRID);
        preferences = (ScopedPreferenceStore) EmbeddedDbPlugin.getDefault().getPreferenceStore();
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
        addField(new IntegerFieldEditor(DbParams.PORT, Messages
                .getString("DbPreferencePage.db.tcpport"), getFieldEditorParent())); //$NON-NLS-1$
        addField(new StringFieldEditor(DbParams.USER, Messages
                .getString("DbPreferencePage.db.user"), getFieldEditorParent())); //$NON-NLS-1$
        StringFieldEditor pasField = new StringFieldEditor(DbParams.PASSWD, Messages
                .getString("DbPreferencePage.db.passwd"), getFieldEditorParent()); //$NON-NLS-1$
        pasField.getTextControl(getFieldEditorParent()).setEchoChar('*');
        addField(pasField);
        addField(new DirectoryFieldEditor(DbParams.FOLDER, Messages
                .getString("DbPreferencePage.db.dbpath"), //$NON-NLS-1$
                getFieldEditorParent()));
        addField(new BackupRestoreFieldEditor(DbParams.DATABASETYPE, getFieldEditorParent()));
    }

    public void init( IWorkbench workbench ) {
    }
}
