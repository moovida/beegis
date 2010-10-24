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
package eu.hydrologis.jgrass.gpsbabel.importwizard;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.refractions.udig.project.IMap;
import net.refractions.udig.project.ui.ApplicationGIS;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import eu.hydrologis.jgrass.beegisutils.jgrassported.FeatureUtilities;
import eu.hydrologis.jgrass.gpsbabel.GpsBabelPlugin;

/**
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class GpsBabelImportWizard extends Wizard implements INewWizard {

    GpsBabelImportWizardPage mainPage;

    private final Map<String, String> params = new HashMap<String, String>();

    public GpsBabelImportWizard() {
        super();
    }

    public void init( IWorkbench workbench, IStructuredSelection selection ) {
        setWindowTitle("File Import Wizard");
        setDefaultPageImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
                GpsBabelPlugin.PLUGIN_ID, "icons/GPSBabel80x80.gif"));
        setNeedsProgressMonitor(true);
        mainPage = new GpsBabelImportWizardPage("Import Gps Babel Files", params); // NON-NLS-1
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.wizard.IWizard#addPages()
     */
    public void addPages() {
        super.addPages();
        addPage(mainPage);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    public boolean performFinish() {

        // on exit do the needed stuff
        boolean is3d = mainPage.getIs3dButton().getSelection();
        String epsg = mainPage.getEpsgText().getText();
        CoordinateReferenceSystem srcCrs = null;
        try {
            if (epsg.length() != 0) {
                srcCrs = CRS.decode(epsg);
            } else {
                srcCrs = null;
            }
        } catch (Exception e2) {
            try {
                // default to lat/long
                srcCrs = CRS.decode("EPSG:4326");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String importPath = mainPage.getFcwOpen().getString();
        if (importPath == null)
            return false;
        Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
        if (!(new File(importPath).exists())) {
            MessageDialog.openError(shell, "", "The input file doesn't exist: " + importPath);
            return false;
        }
        String importPathCsv = importPath + ".csv";

        int lastDotIndex = importPath.lastIndexOf('.');
        String extention = importPath.substring(lastDotIndex + 1);
        /*
         * convert the file to csv with gpsbabel
         */
        URL fileURL = null;
        // check the OS
        System.out.println("Operating system: " + Platform.getOS());
        if (Platform.getOS().equals(Platform.OS_WIN32)) {
            System.out.println(Platform.getOS() + " == " + Platform.OS_WIN32);
            fileURL = FileLocator.find(Platform.getBundle(GpsBabelPlugin.PLUGIN_ID), new Path(
                    "/libs/win/gpsbabel.exe"), null);
        } else if (Platform.getOS().equals(Platform.OS_LINUX)) {
            System.out.println(Platform.getOS() + " == " + Platform.OS_LINUX);
            fileURL = FileLocator.find(Platform.getBundle(GpsBabelPlugin.PLUGIN_ID), new Path(
                    "/libs/linux/usr/bin/gpsbabel"), null);
        } else if (Platform.getOS().equals(Platform.OS_MACOSX)) {
            System.out.println(Platform.getOS() + " == " + Platform.OS_MACOSX);
            fileURL = FileLocator.find(Platform.getBundle(GpsBabelPlugin.PLUGIN_ID), new Path(
                    "/libs/macosx/gpsbabel"), null);
        } else {
            MessageDialog.openError(shell, "",
                    "This operating system is not supported by gpsbabel.");
            return false;
        }
        try {
            fileURL = FileLocator.toFileURL(fileURL);
            String gpsbabelPath = fileURL.getPath();
            System.out.println("GpsBabel exe: " + gpsbabelPath);

            // String cmd = "-i " + extention + " -f " + importPath + " -o csv -F " +
            // importPathCsv;

            String[] cmd = new String[]{gpsbabelPath, "-i", extention, "-f", importPath, "-o",
                    "csv", "-F", importPathCsv};
            StringBuilder sB = new StringBuilder();
            for( String string : cmd ) {
                sB.append(string).append(" ");
            }
            System.out.println("Executing native command: " + sB.toString());
            ProcessBuilder pB = new ProcessBuilder(cmd);
            Process process = null;
            process = pB.start();
            process.waitFor();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (!mainPage.getOnlyCsvButton().getSelection()) {
            /*
             * create shpfile from csv
             */
            String types = "geom:MultiPoint,cat:i";
            String t = mainPage.getFieldsText().getText();
            if (t.length() > 0) {
                types = t;
            }
            types = types.replaceAll(":i", ":java.lang.Integer");
            types = types.replaceAll(":d", ":java.lang.Double");
            types = types.replaceAll(":s", ":java.lang.String");

            IMap activeMap = ApplicationGIS.getActiveMap();
            CoordinateReferenceSystem mapCrs = activeMap.getViewportModel().getCRS();

            File f = new File(importPath);
            File p = f.getParentFile();
            String outPath = null;
            if (p.isDirectory() && mainPage.getFcwSave().getString().length() > 0) {
                outPath = mainPage.getFcwSave().getString();
            } else {
                outPath = importPath;
            }
            try {
                FeatureUtilities.csvFileToShapeFile(srcCrs, mapCrs, importPathCsv, outPath, is3d,
                        false, null, types, true, new NullProgressMonitor());
            } catch (Exception e) {
                e.printStackTrace();
                MessageDialog.openError(shell, "", "An error occurred: " + e.getLocalizedMessage());
            }

            new File(importPathCsv).delete();
        }

        return true;
    }

}
