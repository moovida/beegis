package eu.hydrologis.jgrass.gpsnmea;

import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

public class StartupHelper implements IStartup {

    public void earlyStartup() {
        // used only to activate the button disabling
        final IWorkbench workbench = PlatformUI.getWorkbench();
        workbench.getDisplay().asyncExec(new Runnable(){
            public void run() {
                GpsActivator.getDefault().getToggleLoggingAction();
            }
        });

    }

}
