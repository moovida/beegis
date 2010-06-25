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
package eu.hydrologis.jgrass.beegisutils.actions;

import i18n.beegisutils.Messages;

import java.text.MessageFormat;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.hibernate.classic.Session;
import org.hibernate.criterion.Projections;

import eu.hydrologis.jgrass.beegisutils.database.annotatedclasses.AnnotationsTable;
import eu.hydrologis.jgrass.beegisutils.database.annotatedclasses.GeonotesTable;
import eu.hydrologis.jgrass.beegisutils.database.annotatedclasses.GpsLogTable;
import eu.hydrologis.jgrass.database.DatabasePlugin;
import eu.hydrologis.jgrass.database.interfaces.IDatabaseConnection;
import eu.hydrologis.jgrass.database.view.DatabaseView;

/**
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class StatsAction implements IViewActionDelegate {

    private IViewPart view;

    @Override
    public void init( IViewPart view ) {
        this.view = view;
    }

    @Override
    public void run( IAction action ) {
        if (view instanceof DatabaseView) {
            // DatabaseView dbView = (DatabaseView) view;

            try {
                IDatabaseConnection dbConn = DatabasePlugin.getDefault().getActiveDatabaseConnection();
                Session session = dbConn.openSession();
                Number geonotesCount = (Number) session.createCriteria(GeonotesTable.class).setProjection(Projections.rowCount())
                        .uniqueResult();
                Number logsCount = (Number) session.createCriteria(GpsLogTable.class).setProjection(Projections.rowCount())
                        .uniqueResult();
                Number annotCount = (Number) session.createCriteria(AnnotationsTable.class).setProjection(Projections.rowCount())
                        .uniqueResult();

                String msg = MessageFormat.format(Messages.StatsAction__db_summary_text, dbConn.getConnectionProperties()
                        .getTitle(), geonotesCount.intValue(), logsCount.intValue(), annotCount.intValue());

                MessageDialog.openInformation(view.getSite().getShell(), Messages.StatsAction__db_summary, msg);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void selectionChanged( IAction action, ISelection selection ) {
    }

}
