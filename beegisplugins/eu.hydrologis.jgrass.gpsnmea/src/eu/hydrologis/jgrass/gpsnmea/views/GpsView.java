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
package eu.hydrologis.jgrass.gpsnmea.views;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;

import eu.hydrologis.jgrass.gpsnmea.GpsActivator;
import eu.hydrologis.jgrass.gpsnmea.gps.AbstractGps;
import eu.hydrologis.jgrass.gpsnmea.gps.GpsPoint;
import eu.hydrologis.jgrass.gpsnmea.gps.IGpsObserver;
import eu.hydrologis.jgrass.gpsnmea.gps.NmeaGpsPoint;

/**
 * The view holding gps informations.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class GpsView extends ViewPart implements IGpsObserver {

    public static final String ID = "eu.hydrologis.jgrass.gps.views.GpsView"; //$NON-NLS-1$

    private final String[][] gpsData = new String[][]{{"", ""}}; //$NON-NLS-1$ //$NON-NLS-2$

    private TableViewer v;

    public GpsView() {
        new Thread(){
            public void run() {
                if (GpsActivator.getDefault() == null)
                    return;
                while( !GpsActivator.getDefault().isGpsLogging() ) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                GpsActivator.getDefault().addObserverToGps(GpsView.this);
            }
        }.start();
    }

    public void createPartControl( Composite parent ) {
        v = new TableViewer(parent);
        v.setLabelProvider(new GpsLabelProvider());
        v.setContentProvider(new ArrayContentProvider());

        Table table = v.getTable();
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, false, false);
        // gridData.widthHint = SWT.DEFAULT;
        // gridData.heightHint = SWT.DEFAULT;
        table.setLayoutData(gridData);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        TableLayout layout = new TableLayout();
        layout.addColumnData(new ColumnWeightData(50, true));
        layout.addColumnData(new ColumnWeightData(50, true));
        table.setLayout(layout);
        TableColumn from = new TableColumn(table, SWT.LEFT);
        // from.setWidth(300);
        TableColumn to = new TableColumn(table, SWT.LEFT);
        // to.setWidth(300);

        v.setInput(gpsData);
    }

    public void setFocus() {
    }

    public void updateGpsPoint( AbstractGps gpsEngine, final GpsPoint gpsPoint ) {
        if (gpsPoint != null) {
            Display.getDefault().asyncExec(new Runnable(){
                public void run() {
                    IContentProvider contentProvider = v.getContentProvider();
                    if (contentProvider == null) {
                        return;
                    }
                    v.setInput(gpsPoint.toTableArray());
                }
            });
        }
    }

    public void dispose() {
        GpsActivator.getDefault().removeObserverFromGps(this);
        super.dispose();
    }

    private class GpsLabelProvider implements ITableLabelProvider {

        public Image getColumnImage( Object element, int columnIndex ) {
            return null;
        }

        public String getColumnText( Object element, int columnIndex ) {
            String[] e = (String[]) element;
            return e[columnIndex];
        }

        public void addListener( ILabelProviderListener listener ) {
        }

        public void dispose() {
        }

        public boolean isLabelProperty( Object element, String property ) {
            return false;
        }

        public void removeListener( ILabelProviderListener listener ) {
        }
    }
}
