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
package eu.hydrologis.jgrass.geonotes.fieldbook.actions;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import net.refractions.udig.ui.ExceptionDetailsDialog;
import net.refractions.udig.ui.PlatformGIS;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import eu.hydrologis.jgrass.beegisutils.jgrassported.CompressionUtilities;
import eu.hydrologis.jgrass.beegisutils.jgrassported.FileUtilities;
import eu.hydrologis.jgrass.geonotes.GeonoteConstants;
import eu.hydrologis.jgrass.geonotes.GeonotesHandler;
import eu.hydrologis.jgrass.geonotes.GeonotesUI;
import eu.hydrologis.jgrass.geonotes.GeonotesPlugin;
import eu.hydrologis.jgrass.geonotes.fieldbook.GeonotesListViewer;
import eu.hydrologis.jgrass.geonotes.preferences.pages.PreferenceConstants;

/**
 * Action to send {@link GeonotesUI}s via mail.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public class SendNotesAction extends Action {

    private final GeonotesListViewer geonotesViewer;

    public SendNotesAction( GeonotesListViewer geonotesViewer ) {
        super("Send geonotes");
        this.geonotesViewer = geonotesViewer;
    }

    public void run() {
        List<GeonotesHandler> currentGeonotesSelection = geonotesViewer.getCurrentGeonotesSelection();

        String tempdir = System.getProperty("java.io.tmpdir");
        File tmpDirFile = new File(tempdir);
        if (tmpDirFile.exists()) {
            try {

                String ffName = GeonoteConstants.GEONOTES;
                File geonotesDirFile = new File(tmpDirFile, ffName);
                int i = 1;
                while( geonotesDirFile.exists() ) {
                    ffName = "geonotes_" + i;
                    geonotesDirFile = new File(geonotesDirFile.getParentFile(), ffName);
                    i++;
                }
                if (!geonotesDirFile.mkdir())
                    throw new IOException("Cannot create folder: "
                            + geonotesDirFile.getAbsolutePath());

                int index = 0;
                for( GeonotesHandler geoNote : currentGeonotesSelection ) {
                    File dir = new File(geonotesDirFile.getAbsolutePath() + File.separator
                            + String.valueOf(index));
                    index++;
                    if (dir.mkdir())
                        geoNote.dumpBinaryNote(dir.getAbsolutePath());
                }

                // zip the folder
                final File zipFile = new File(geonotesDirFile.getParentFile(), ffName + ".zip");
                CompressionUtilities.zipFolder(geonotesDirFile.getAbsolutePath(), zipFile
                        .getAbsolutePath(), false);

                final File geonotesDirFile2 = geonotesDirFile;
                IRunnableWithProgress operation = new IRunnableWithProgress(){

                    public void run( IProgressMonitor monitor ) throws InvocationTargetException,
                            InterruptedException {
                        monitor.beginTask("Sending note via email...", IProgressMonitor.UNKNOWN);
                        try {
                            sendmail(zipFile);
                        } catch (Exception e) {
                            e.printStackTrace();
                            String message = "An error occurred while sending the geonote";
                            ErrorDialog.openError(geonotesViewer.getTable().getShell(), "Error",
                                    message, new Status(IStatus.ERROR, GeonotesPlugin.PLUGIN_ID,
                                            message, e));
                        } finally {
                            monitor.done();
                        }

                        if (!FileUtilities.deleteFileOrDir(zipFile)) {
                            FileUtilities.deleteFileOrDirOnExit(zipFile);
                        }
                        if (!FileUtilities.deleteFileOrDir(geonotesDirFile2)) {
                            FileUtilities.deleteFileOrDirOnExit(geonotesDirFile2);
                        }

                    }
                };
                PlatformGIS.runInProgressDialog("Set some activity title", true, operation, true);

            } catch (Exception e1) {
                String message = "An error occurred while sending the geonotes.";
                ExceptionDetailsDialog.openError(null, message, IStatus.ERROR,
                        GeonotesPlugin.PLUGIN_ID, e1);

                e1.printStackTrace();
            }
        } else {
            MessageDialog.openError(geonotesViewer.getTable().getShell(), "Error",
                    "The temporary folder, which is needed to dump notes could not be accessed: "
                            + tempdir);
        }

    }

    private void sendmail( File zipFile ) throws Exception {

        ScopedPreferenceStore preferences = (ScopedPreferenceStore) GeonotesPlugin.getDefault()
                .getPreferenceStore();
        String to = preferences.getString(PreferenceConstants.GEONOTES_DESTINATION_ADDRESS);
        String host = preferences.getString(PreferenceConstants.SMTP_SERVER);
        String port = preferences.getString(PreferenceConstants.SMTP_PORT);
        final String user = preferences.getString(PreferenceConstants.SMTP_USERNAME);
        final String passwd = preferences.getString(PreferenceConstants.SMTP_PASSWORD);
        boolean auth = preferences.getBoolean(PreferenceConstants.SMTP_AUTH);
        String from = "beegis@beegis.org";

        boolean debug = true;

        // String SMTP_PORT = "465";
        String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
        String msgText1 = "Find attached the Geonote archive sent to you via BeeGIS.\n\n"
                + "To import it into BeeGIS again, just open the fieldbook, and drag the attached archive as it is into the geonotes list viewer.";
        String subject = "Geonote archive sent from BeeGIS";

        // create some properties and get the default Session
        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        if (auth) {
            props.put("mail.smtp.auth", auth);
            props.put("mail.debug", String.valueOf(debug));
            props.put("mail.smtp.socketFactory.port", port);
            props.put("mail.smtp.socketFactory.class", SSL_FACTORY);
            props.put("mail.smtp.socketFactory.fallback", "false");
        }
        Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator(){
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, passwd);
            }
        });

        session.setDebug(debug);

        // create a message
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from));
        InternetAddress[] address = {new InternetAddress(to)};
        msg.setRecipients(Message.RecipientType.TO, address);
        msg.setSubject(subject);

        // create and fill the first message part
        MimeBodyPart mbp1 = new MimeBodyPart();
        mbp1.setText(msgText1);

        // create the second message part
        MimeBodyPart mbp2 = new MimeBodyPart();

        // attach the file to the message
        mbp2.attachFile(zipFile);

        /*
         * Use the following approach instead of the above line if
         * you want to control the MIME type of the attached file.
         * Normally you should never need to do this.
         *
        FileDataSource fds = new FileDataSource(filename) {
        public String getContentType() {
            return "application/octet-stream";
        }
        };
        mbp2.setDataHandler(new DataHandler(fds));
        mbp2.setFileName(fds.getName());
         */

        // create the Multipart and add its parts to it
        Multipart mp = new MimeMultipart();
        mp.addBodyPart(mbp1);
        mp.addBodyPart(mbp2);

        // add the Multipart to the message
        msg.setContent(mp);

        // set the Date: header
        msg.setSentDate(new Date());

        /*
         * If you want to control the Content-Transfer-Encoding
         * of the attached file, do the following.  Normally you
         * should never need to do this.
         *
        msg.saveChanges();
        mbp2.setHeader("Content-Transfer-Encoding", "base64");
         */

        // send the message
        Transport.send(msg);

    }
}
