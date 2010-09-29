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
package eu.hydrologis.jgrass.beegisutils.jgrassported;

import java.io.IOException;
import java.util.List;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.mapgraphic.internal.MapGraphicService;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.ui.ApplicationGIS;
import net.refractions.udig.ui.ExceptionDetailsDialog;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;

import eu.hydrologis.jgrass.beegisutils.BeegisUtilsPlugin;

/**
 * Some jgrass catalog related helper methods
 * 
 * @author Andrea Antonello - www.hydrologis.com
 */
public class JGrassCatalogUtilities {

    /**
     * Returns a mapgraphics layer by its class.
     * 
     * @param theClass the class of the mapgraphics.
     * @return the layer or null, if none was found (not visible or not existing).
     */
    public static ILayer getMapgraphicLayerByClass( Class< ? > theClass ) {

        try {
            List<IResolve> mapgraphics = CatalogPlugin.getDefault().getLocalCatalog().find(
                    MapGraphicService.SERVICE_URL, null);
            List<IResolve> members = mapgraphics.get(0).members(null);
            for( IResolve resolve : members ) {
                if (resolve.canResolve(theClass)) {
                    IGeoResource resolve2 = resolve.resolve(IGeoResource.class, null);
                    String resName = resolve2.getInfo(new NullProgressMonitor()).getName();

                    List<ILayer> mapLayers = ApplicationGIS.getActiveMap().getMapLayers();
                    for( ILayer layer : mapLayers ) {
                        if (layer.getName().trim().equals(resName.trim())) {
                            return layer;
                        }
                    }
                }
            }
        } catch (IOException e) {
            String message = "An error occurred while retrieving the mapgraphic";
            ExceptionDetailsDialog.openError(null, message, IStatus.ERROR,
                    BeegisUtilsPlugin.PLUGIN_ID, e);
            e.printStackTrace();
            return null;
        }
        return null;

    }

}
