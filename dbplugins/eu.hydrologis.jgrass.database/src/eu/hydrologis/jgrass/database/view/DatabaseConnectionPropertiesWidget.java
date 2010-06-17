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

import i18n.Messages;

import java.lang.reflect.InvocationTargetException;

import net.refractions.udig.ui.PlatformGIS;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import eu.hydrologis.jgrass.database.DatabasePlugin;
import eu.hydrologis.jgrass.database.core.ConnectionManager;
import eu.hydrologis.jgrass.database.core.DatabaseConnectionProperties;

/**
 * A widget to present {@link DatabaseConnectionProperties} in guis. 
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class DatabaseConnectionPropertiesWidget {

    private Composite propertiesComposite = null;
    private Text pathText;
    private Text hostText;
    private final DatabaseView databaseView;
    private DatabaseConnectionProperties properties;
    private Text nameText;
    private Text databaseText;
    private Text userText;
    private Text passwordText;
    private Text portText;
    private Button activateButton;
    private Button disableButton;

    public DatabaseConnectionPropertiesWidget( DatabaseView databaseView ) {
        this.databaseView = databaseView;
    }

    /**
     * Creates the widget for the database properties.
     * 
     * <p>If the widget is already created, it return the existing one.</p>
     * 
     * @param properties the {@link DatabaseConnectionProperties}.
     * @param parent the parent composite into which to insert the panel.
     * @return the composite for the properties.
     */
    public Composite getComposite( final DatabaseConnectionProperties properties, Composite parent ) {
        this.properties = properties;
        if (propertiesComposite == null) {
            propertiesComposite = new Composite(parent, SWT.NONE);
            propertiesComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            propertiesComposite.setLayout(new GridLayout(2, true));

            // type
            Label typeLabel = new Label(propertiesComposite, SWT.NONE);
            GridData typeLabelGD = new GridData(SWT.FILL, SWT.CENTER, true, false);
            typeLabelGD.horizontalSpan = 2;
            typeLabel.setLayoutData(typeLabelGD);
            typeLabel.setText(Messages.databaseplugin__database_type + properties.getType());

            // name
            Label nameLabel = new Label(propertiesComposite, SWT.NONE);
            nameLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            nameLabel.setText(Messages.databaseplugin__connection_name);
            nameText = new Text(propertiesComposite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
            nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            String title = properties.getTitle();
            if (title == null) {
                title = ""; //$NON-NLS-1$
            }
            nameText.setText(title);
            nameText.addKeyListener(new KeyAdapter(){
                public void keyReleased( KeyEvent e ) {
                    properties.put(DatabaseConnectionProperties.TITLE, nameText.getText());
                    triggerViewerLayout();
                }
            });

            boolean isLocal = ConnectionManager.isLocal(properties);
            if (isLocal) {
                // path
                Label pathLabel = new Label(propertiesComposite, SWT.NONE);
                pathLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
                pathLabel.setText(Messages.databaseplugin__database_folder);
                pathText = new Text(propertiesComposite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
                pathText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
                String path = properties.getPath();
                if (path == null) {
                    path = ""; //$NON-NLS-1$
                }
                pathText.setText(path);
                pathText.addKeyListener(new KeyAdapter(){
                    public void keyReleased( KeyEvent e ) {
                        properties.put(DatabaseConnectionProperties.PATH, pathText.getText());
                    }
                });
            } else {
                // host
                Label hostLabel = new Label(propertiesComposite, SWT.NONE);
                hostLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
                hostLabel.setText(Messages.databaseplugin__database_host);
                hostText = new Text(propertiesComposite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
                hostText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
                String host = properties.getHost();
                if (host == null) {
                    host = ""; //$NON-NLS-1$
                }
                hostText.setText(host);
                hostText.addKeyListener(new KeyAdapter(){
                    public void keyReleased( KeyEvent e ) {
                        properties.put(DatabaseConnectionProperties.HOST, hostText.getText());
                    }
                });
            }

            // database
            Label databaseLabel = new Label(propertiesComposite, SWT.NONE);
            databaseLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            databaseLabel.setText(Messages.databaseplugin__database_name);
            databaseText = new Text(propertiesComposite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
            databaseText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            String databaseName = properties.getDatabaseName();
            if (databaseName == null) {
                databaseName = ""; //$NON-NLS-1$
            }
            databaseText.setText(databaseName);
            databaseText.addKeyListener(new KeyAdapter(){
                public void keyReleased( KeyEvent e ) {
                    properties.put(DatabaseConnectionProperties.DATABASE, databaseText.getText());
                }
            });

            // user
            Label userLabel = new Label(propertiesComposite, SWT.NONE);
            userLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            userLabel.setText(Messages.databaseplugin__database_user);
            userText = new Text(propertiesComposite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
            userText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            String user = properties.getUser();
            if (user == null) {
                user = ""; //$NON-NLS-1$
            }
            userText.setText(user);
            userText.addKeyListener(new KeyAdapter(){
                public void keyReleased( KeyEvent e ) {
                    properties.put(DatabaseConnectionProperties.USER, userText.getText());
                }
            });

            // password
            Label passwordLabel = new Label(propertiesComposite, SWT.NONE);
            passwordLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            passwordLabel.setText(Messages.databaseplugin__database_password);
            passwordText = new Text(propertiesComposite, SWT.SINGLE | SWT.LEAD | SWT.BORDER | SWT.PASSWORD);
            passwordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            String password = properties.getPassword();
            if (password == null) {
                password = ""; //$NON-NLS-1$
            }
            passwordText.setText(password);
            passwordText.addKeyListener(new KeyAdapter(){
                public void keyReleased( KeyEvent e ) {
                    properties.put(DatabaseConnectionProperties.PASS, passwordText.getText());
                }
            });

            // port
            Label portLabel = new Label(propertiesComposite, SWT.NONE);
            portLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            portLabel.setText(Messages.databaseplugin__database_port);
            portText = new Text(propertiesComposite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
            portText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            String port = properties.getPort();
            if (port == null) {
                port = ""; //$NON-NLS-1$
            }
            portText.setText(port);
            portText.addKeyListener(new KeyAdapter(){
                public void keyReleased( KeyEvent e ) {
                    properties.put(DatabaseConnectionProperties.PORT, portText.getText());
                }
            });

            activateButton = new Button(propertiesComposite, SWT.PUSH);
            GridData activateButtonGD = new GridData(SWT.FILL, SWT.FILL, true, false);
            activateButton.setLayoutData(activateButtonGD);
            activateButton.setText(Messages.databaseplugin__activate_connection);
            activateButton.setEnabled(!properties.isActive());
            activateButton.addSelectionListener(new SelectionAdapter(){
                public void widgetSelected( SelectionEvent e ) {
                    IRunnableWithProgress operation = new IRunnableWithProgress(){
                        public void run( IProgressMonitor pm ) throws InvocationTargetException, InterruptedException {
                            boolean troubles = false;
                            pm
                                    .beginTask(Messages.databaseplugin__connecting_db + properties.getTitle(),
                                            IProgressMonitor.UNKNOWN);
                            DatabaseConnectionProperties activeDatabaseConnectionProperties = DatabasePlugin.getDefault()
                                    .getActiveDatabaseConnectionProperties();
                            try {
                                DatabasePlugin.getDefault().activateDatabaseConnection(properties);
                            } catch (Exception e1) {
                                troubles = true;
                                // problem occurred while connecting, reconnect to the previous
                                try {
                                    DatabasePlugin.getDefault().activateDatabaseConnection(activeDatabaseConnectionProperties);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            pm.done();

                            if (troubles) {
                                MessageDialog.openWarning(portText.getShell(), Messages.databaseplugin__connection_error,
                                        Messages.databaseplugin__errmsg_connection);
                            }
                            triggerViewerLayout();

                        }
                    };
                    PlatformGIS.runInProgressDialog(Messages.databaseplugin__connecting_db, true, operation, true);
                }
            });

            disableButton = new Button(propertiesComposite, SWT.PUSH);
            GridData disableButtonGD = new GridData(SWT.FILL, SWT.FILL, true, false);
            disableButton.setLayoutData(disableButtonGD);
            disableButton.setText(Messages.databaseplugin__disconnect);
            disableButton.setEnabled(properties.isActive());
            disableButton.addSelectionListener(new SelectionAdapter(){
                public void widgetSelected( SelectionEvent e ) {
                    IRunnableWithProgress operation = new IRunnableWithProgress(){
                        public void run( IProgressMonitor pm ) throws InvocationTargetException, InterruptedException {
                            pm.beginTask(Messages.databaseplugin__disconnecting_db + properties.getTitle(),
                                    IProgressMonitor.UNKNOWN);
                            DatabasePlugin.getDefault().disconnectActiveDatabaseConnection();
                            pm.done();

                        }
                    };
                    PlatformGIS.runInProgressDialog(Messages.databaseplugin__disconnecting_db, true, operation, false);
                    triggerViewerLayout();
                }
            });

            if (isLocal) {
                // open db folder
                Button openFolderButton = new Button(propertiesComposite, SWT.PUSH);
                GridData openFolderButtonGD = new GridData(SWT.FILL, SWT.FILL, true, false);
                openFolderButtonGD.horizontalSpan = 2;
                openFolderButton.setLayoutData(openFolderButtonGD);
                openFolderButton.setText(Messages.databaseplugin__open_db_location);
                openFolderButton.addSelectionListener(new SelectionAdapter(){
                    public void widgetSelected( SelectionEvent e ) {
                        Program.launch(properties.getPath());
                    }
                });
            }

        }

        return propertiesComposite;
    }

    public void refresh() {
        // make sure the params are updated
        // name
        String title = properties.getTitle();
        if (title == null) {
            title = ""; //$NON-NLS-1$
        }
        nameText.setText(title);

        boolean isLocal = ConnectionManager.isLocal(properties);
        if (isLocal) {
            // path
            String path = properties.getPath();
            if (path == null) {
                path = ""; //$NON-NLS-1$
            }
            pathText.setText(path);
        } else {
            String host = properties.getHost();
            if (host == null) {
                host = ""; //$NON-NLS-1$
            }
            hostText.setText(host);
        }

        // database
        String databaseName = properties.getDatabaseName();
        if (databaseName == null) {
            databaseName = ""; //$NON-NLS-1$
        }
        databaseText.setText(databaseName);

        // user
        String user = properties.getUser();
        if (user == null) {
            user = ""; //$NON-NLS-1$
        }
        userText.setText(user);

        // password
        String password = properties.getPassword();
        if (password == null) {
            password = ""; //$NON-NLS-1$
        }
        passwordText.setText(password);

        // port
        String port = properties.getPort();
        if (port == null) {
            port = ""; //$NON-NLS-1$
        }
        portText.setText(port);

        // make connection active button
        activateButton.setEnabled(!properties.isActive());
        disableButton.setEnabled(properties.isActive());
    }

    private void triggerViewerLayout() {
        databaseView.relayout();
    }

}
