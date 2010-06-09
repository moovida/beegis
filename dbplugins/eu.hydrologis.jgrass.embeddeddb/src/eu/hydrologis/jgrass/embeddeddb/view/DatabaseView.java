package eu.hydrologis.jgrass.embeddeddb.view;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;

import net.refractions.udig.ui.ExceptionDetailsDialog;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import eu.hydrologis.jgrass.embeddeddb.EmbeddedDbPlugin;
import eu.hydrologis.jgrass.embeddeddb.preferences.DbParams;

public class DatabaseView extends ViewPart {

    private Browser browser;
    public DatabaseView() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void createPartControl( Composite parent ) {
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.verticalAlignment = GridData.FILL;
        browser = new Browser(parent, SWT.NONE);
        browser.setLayoutData(gridData);

        try {
            EmbeddedDbPlugin.getDefault().getSessionFactory();
        } catch (Exception e1) {
            String message = "An error occurred while connecting to the database.";
            ExceptionDetailsDialog.openError(null, message, IStatus.ERROR,
                    EmbeddedDbPlugin.PLUGIN_ID, e1);
            
            e1.printStackTrace();
        }
        
        ScopedPreferenceStore store = (ScopedPreferenceStore) EmbeddedDbPlugin.getDefault()
                .getPreferenceStore();
        int port = store.getInt(DbParams.PORT);
        String dbRoot = store.getString(DbParams.FOLDER);
        String user = store.getString(DbParams.USER);
        String passwd = store.getString(DbParams.PASSWD);

        try {
            // get the session id
            String base = "http://localhost:" + String.valueOf(port + 1);
            URL url = new URL(base);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String str;
            String loginDo = "";
            while( (str = in.readLine()) != null ) {
                if (str.matches(".*jsessionid.*")) {
                    str = str.replaceFirst("^.*jsessionid=", "");
                    str = str.replaceFirst("';$", "");
                    loginDo = "login.do?jsessionid=" + str;
                    break;
                }
            }
            in.close();

            StringBuilder sB = new StringBuilder();
            sB.append(base);
            if (loginDo.length() > 0) {
                sB.append("/");
                sB.append(loginDo);
                sB.append("&");
                sB.append("driver=org.h2.Driver&url=jdbc:h2:");
                sB.append(dbRoot);
                sB.append(File.separator);
                sB.append("database");
                sB.append("&");
                sB.append("user=");
                sB.append(user);
                sB.append("&");
                sB.append("password=");
                sB.append(passwd);
            }
            browser.setUrl(sB.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void setFocus() {
    }

}
