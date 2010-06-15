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

import java.lang.reflect.InvocationTargetException;

import net.refractions.udig.ui.PlatformGIS;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
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
import org.eclipse.ui.PlatformUI;

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
        if (propertiesComposite == null) {
            propertiesComposite = new Composite(parent, SWT.NONE);
            propertiesComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            propertiesComposite.setLayout(new GridLayout(2, true));

            // type
            Label typeLabel = new Label(propertiesComposite, SWT.NONE);
            GridData typeLabelGD = new GridData(SWT.FILL, SWT.CENTER, true, false);
            typeLabelGD.horizontalSpan = 2;
            typeLabel.setLayoutData(typeLabelGD);
            typeLabel.setText("Database type: " + properties.getType());

            // name
            Label nameLabel = new Label(propertiesComposite, SWT.NONE);
            nameLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            nameLabel.setText("Connection name");
            final Text nameText = new Text(propertiesComposite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
            nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            String title = properties.getTitle();
            if (title == null) {
                title = "";
            }
            nameText.setText(title);
            nameText.addKeyListener(new KeyAdapter(){
                public void keyReleased( KeyEvent e ) {
                    properties.put(DatabaseConnectionProperties.TITLE, nameText.getText());
                }
            });

            boolean isLocal = ConnectionManager.isLocal(properties);
            if (isLocal) {
                // path
                Label pathLabel = new Label(propertiesComposite, SWT.NONE);
                pathLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
                pathLabel.setText("Database folder");
                pathText = new Text(propertiesComposite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
                pathText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
                String path = properties.getPath();
                if (path == null) {
                    path = "";
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
                hostLabel.setText("Host");
                hostText = new Text(propertiesComposite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
                hostText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
                String host = properties.getHost();
                if (host == null) {
                    host = "";
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
            databaseLabel.setText("Database name");
            final Text databaseText = new Text(propertiesComposite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
            databaseText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            String databaseName = properties.getDatabaseName();
            if (databaseName == null) {
                databaseName = "";
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
            userLabel.setText("User");
            final Text userText = new Text(propertiesComposite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
            userText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            String user = properties.getUser();
            if (user == null) {
                user = "";
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
            passwordLabel.setText("Password");
            final Text passwordText = new Text(propertiesComposite, SWT.SINGLE | SWT.LEAD | SWT.BORDER | SWT.PASSWORD);
            passwordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            String password = properties.getPassword();
            if (password == null) {
                password = "";
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
            portLabel.setText("Port");
            final Text portText = new Text(propertiesComposite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
            portText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            String port = properties.getPort();
            if (port == null) {
                port = "";
            }
            portText.setText(port);
            portText.addKeyListener(new KeyAdapter(){
                public void keyReleased( KeyEvent e ) {
                    properties.put(DatabaseConnectionProperties.PORT, portText.getText());
                }
            });

            // make connection active button
            final Button activateButton = new Button(propertiesComposite, SWT.PUSH);
            GridData activateButtonGD = new GridData(SWT.FILL, SWT.FILL, true, false);
            activateButtonGD.horizontalSpan = 2;
            activateButton.setLayoutData(activateButtonGD);
            activateButton.setText("Activate this connection");
            if (properties.isActive()) {
                activateButton.setEnabled(false);
            }else{
                activateButton.setEnabled(true);
            }
            activateButton.addSelectionListener(new SelectionAdapter(){
                public void widgetSelected( SelectionEvent e ) {
                    IRunnableWithProgress operation = new IRunnableWithProgress(){
                        public void run( IProgressMonitor pm ) throws InvocationTargetException, InterruptedException {
                            boolean troubles = false;
                            pm.beginTask("Activate database: " + properties.getTitle(), IProgressMonitor.UNKNOWN);
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
                                MessageDialog.openWarning(portText.getShell(), "Connection error",
                                        "An error occurred while connecting to the new database."
                                                + "\nPlease check the database parameters entered.");
                            }

                        }
                    };
                    PlatformGIS.runInProgressDialog("Activate database...", true, operation, true);
                }
            });

            if (isLocal) {
                // open db folder
                Button openFolderButton = new Button(propertiesComposite, SWT.PUSH);
                GridData openFolderButtonGD = new GridData(SWT.FILL, SWT.FILL, true, false);
                openFolderButtonGD.horizontalSpan = 2;
                openFolderButton.setLayoutData(openFolderButtonGD);
                openFolderButton.setText("Open database location");
                openFolderButton.addSelectionListener(new SelectionAdapter(){
                    public void widgetSelected( SelectionEvent e ) {
                        Program.launch(properties.getPath());
                    }
                });
            }

            propertiesComposite.addFocusListener(new FocusAdapter(){
                public void focusGained( FocusEvent e ) {
                    super.focusGained(e);
                    
                    // make sure the params are updated
                    // name
                    String title = properties.getTitle();
                    if (title == null) {
                        title = "";
                    }
                    nameText.setText(title);

                    boolean isLocal = ConnectionManager.isLocal(properties);
                    if (isLocal) {
                        // path
                        String path = properties.getPath();
                        if (path == null) {
                            path = "";
                        }
                        pathText.setText(path);
                    } else {
                        String host = properties.getHost();
                        if (host == null) {
                            host = "";
                        }
                        hostText.setText(host);
                    }

                    // database
                    String databaseName = properties.getDatabaseName();
                    if (databaseName == null) {
                        databaseName = "";
                    }
                    databaseText.setText(databaseName);

                    // user
                    String user = properties.getUser();
                    if (user == null) {
                        user = "";
                    }
                    userText.setText(user);

                    // password
                    String password = properties.getPassword();
                    if (password == null) {
                        password = "";
                    }
                    passwordText.setText(password);

                    // port
                    String port = properties.getPort();
                    if (port == null) {
                        port = "";
                    }
                    portText.setText(port);

                    // make connection active button
                    if (properties.isActive()) {
                        activateButton.setEnabled(false);
                    }else{
                        activateButton.setEnabled(true);
                    }
                }
            });
            
            
        }

        return propertiesComposite;
    }

}
