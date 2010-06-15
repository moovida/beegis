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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import eu.hydrologis.jgrass.database.core.ConnectionManager;
import eu.hydrologis.jgrass.database.core.DatabaseConnectionProperties;

/**
 * A widget to present {@link DatabaseConnectionProperties} in guis. 
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class DatabaseConnectionPropertiesWidget {

    private Composite widget = null;

    /**
     * Creates the widget for the database properties.
     * 
     * <p>If the widget is already created, it return the existing one.</p>
     * 
     * @param properties the {@link DatabaseConnectionProperties}.
     * @param parent the parent composite into which to insert the panel.
     * @return the composite for the properties.
     */
    public Composite getComposite( DatabaseConnectionProperties properties, Composite parent ) {
        if (widget == null) {
            Composite propertiesComposite = new Composite(parent, SWT.NONE);
            propertiesComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            propertiesComposite.setLayout(new GridLayout(1, false));

            boolean isLocal = ConnectionManager.isLocal(properties);
            if (isLocal) {

            }

        }

        return widget;
    }

}
