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
package eu.hydrologis.jgrass.gpsnmea.imports;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.refractions.udig.ui.ExceptionDetailsDialog;
import net.refractions.udig.ui.PlatformGIS;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.geotools.data.shapefile.dbf.DbaseFileHeader;
import org.geotools.data.shapefile.dbf.DbaseFileReader;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import eu.hydrologis.jgrass.beegisutils.database.annotatedclasses.GpsLogTable;
import eu.hydrologis.jgrass.database.DatabasePlugin;
import eu.hydrologis.jgrass.gpsnmea.GpsActivator;

/**
 * @author Andrea Antonello - www.hydrologis.com
 */
public class ArcpadImportWizard extends Wizard implements INewWizard {

    private ArcpadImportWizardPage mainPage;

    public static boolean canFinish = false;

    private final Map<String, String> params = new HashMap<String, String>();

    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    private DateTimeFormatter dateTimeFormatterYYYYMMDDHHMMSS = DateTimeFormat
            .forPattern("yyyy-MM-dd HH:mm:ss");

    public ArcpadImportWizard() {
        super();
    }

    public void init( IWorkbench workbench, IStructuredSelection selection ) {
        setWindowTitle("Arcpad file import");
        setDefaultPageImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
                GpsActivator.PLUGIN_ID, "icons/arcpad128.gif")); //$NON-NLS-1$
        setNeedsProgressMonitor(true);
        mainPage = new ArcpadImportWizardPage("Arcpad file import", params); //$NON-NLS-1$
    }

    public void addPages() {
        super.addPages();
        addPage(mainPage);
    }

    public boolean canFinish() {
        return super.canFinish() && canFinish;
    }

    public boolean performFinish() {

        final File arcpadFile = mainPage.getArcpadFile();

        /*
         * run with backgroundable progress monitoring
         */
        IRunnableWithProgress operation = new IRunnableWithProgress(){

            public void run( IProgressMonitor pm ) throws InvocationTargetException,
                    InterruptedException {
                try {
                    FileChannel in = new FileInputStream(arcpadFile).getChannel();
                    // TODO set udig charset
                    Charset chars = Charset.forName("UTF-8");

                    DbaseFileReader r = new DbaseFileReader(in, false, chars);
                    DbaseFileHeader header = r.getHeader();
                    int numFields = header.getNumFields();

                    int latIndex = -1;
                    int longIndex = -1;
                    int dateIndex = -1;
                    int timeIndex = -1;
                    int satsIndex = -1;
                    int hdopIndex = -1;
                    for( int i = 0; i < numFields; i++ ) {
                        String fieldName = header.getFieldName(i);
                        if (fieldName.toUpperCase().equals("LATITUDE")) {
                            latIndex = i;
                        }
                        if (fieldName.toUpperCase().equals("LONGITUDE")) {
                            longIndex = i;
                        }
                        if (fieldName.toUpperCase().equals("UTCDATE")) {
                            dateIndex = i;
                        }
                        if (fieldName.toUpperCase().equals("UTCTIME")) {
                            timeIndex = i;
                        }
                        if (fieldName.toUpperCase().equals("SATS_USED")) {
                            satsIndex = i;
                        }
                        if (fieldName.toUpperCase().equals("HDOP")) {
                            hdopIndex = i;
                        }
                    }

                    if (latIndex == -1 || longIndex == -1 || dateIndex == -1 || timeIndex == -1
                            || satsIndex == -1 || hdopIndex == -1) {
                        MessageDialog
                                .openError(
                                        getShell(),
                                        "Warning",
                                        "The dbf file to be imported has to contain the following fields to be imported into the database: LATITUDE, LONGITUDE, UTCDATE, UTCTIME, SATS_USED, HDOP.");
                        return;
                    }

                    SessionFactory sessionFactory = DatabasePlugin.getDefault().getActiveDatabaseConnection().getSessionFactory();
                    Session session = sessionFactory.openSession();
                    Transaction transaction = session.beginTransaction();
                    while( r.hasNext() ) {
                        Object[] fields = new Object[numFields];
                        r.readEntry(fields);

                        double lon = (Double) fields[longIndex];
                        double lat = (Double) fields[latIndex];

                        Date date = (Date) fields[dateIndex];
                        String timeString = (String) fields[timeIndex];
                        String dateString = dateFormatter.format(date);
                        String time = " " + timeString.substring(0, 2) + ":"
                                + timeString.substring(2, 4) + ":" + timeString.substring(4, 6);
                        DateTime dateTime = dateTimeFormatterYYYYMMDDHHMMSS
                                .parseDateTime(dateString + time);
                        double hdop = ((Number) fields[hdopIndex]).doubleValue();
                        int sats = ((Number) fields[satsIndex]).intValue();

                        GpsLogTable gpsLog = new GpsLogTable();
                        gpsLog.setUtcTime(dateTime);
                        gpsLog.setEast(lon);
                        gpsLog.setNorth(lat);
                        gpsLog.setAltimetry(-1.0);
                        gpsLog.setNumberOfTrackedSatellites(sats);
                        gpsLog.setHorizontalDilutionOfPosition(hdop);

                        session.save(gpsLog);
                    }
                    r.close();

                    transaction.commit();
                    session.close();

                } catch (Exception e) {
                    e.printStackTrace();
                    String message = "An error occurred during Arcpad gps log import.";
                    ExceptionDetailsDialog.openError(null, message, IStatus.ERROR,
                            GpsActivator.PLUGIN_ID, e);
                }
            }
        };

        PlatformGIS.runInProgressDialog("Importing Arcpad data", true, operation, true);

        return true;
    }
}
