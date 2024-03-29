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
package eu.hydrologis.jgrass.geonotes;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.mapgraphic.internal.MapGraphicService;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.ui.ApplicationGIS;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import eu.hydrologis.jgrass.geonotes.fieldbook.FieldbookView;
import eu.hydrologis.jgrass.geonotes.mapgraphic.GeonotesMapGraphic;
import eu.hydrologis.jgrass.geonotes.util.ImageManager;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class GeonotesPlugin extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "eu.hydrologis.jgrass.geonotes"; //$NON-NLS-1$

    // The shared instance
    private static GeonotesPlugin plugin;
    private ILayer geonotesLayer;

    /**
     * The constructor
     */
    public GeonotesPlugin() {
    }

    public void start( BundleContext context ) throws Exception {
        super.start(context);
        plugin = this;
    }

    public void stop( BundleContext context ) throws Exception {
        ImageManager.disposeSwt();
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static GeonotesPlugin getDefault() {
        return plugin;
    }

    /**
     * Logs the Throwable in the plugin's log.
     * <p>
     * This will be a user visable ERROR iff:
     * <ul>
     * <li>t is an Exception we are assuming it is human readable or if a message is provided
     */
    public static void log( String message2, Throwable t ) {
        if (getDefault() == null) {
            t.printStackTrace();
            return;
        }
        String message = message2;
        if (message == null)
            message = ""; //$NON-NLS-1$
        int status = t instanceof Exception || message != null ? IStatus.ERROR : IStatus.WARNING;
        getDefault().getLog().log(new Status(status, PLUGIN_ID, IStatus.OK, message, t));
    }

    /**
     * Getter for the geonotes layer.
     * 
     * @return the geonotes layer.
     */
    public ILayer getGeonotesLayer() {

        /*
         * load the right mapgraphic layer
         */
        try {
            List<IResolve> mapgraphics = CatalogPlugin.getDefault().getLocalCatalog().find(
                    MapGraphicService.SERVICE_URL, null);
            List<IResolve> members = mapgraphics.get(0).members(null);
            for( IResolve resolve : members ) {
                if (resolve.canResolve(GeonotesMapGraphic.class)) {
                    IMap activeMap = ApplicationGIS.getActiveMap();
                    List<ILayer> layers = activeMap.getMapLayers();
                    boolean isAlreadyLoaded = false;
                    for( ILayer layer : layers ) {
                        if (layer.hasResource(GeonotesMapGraphic.class)) {
                            isAlreadyLoaded = true;
                            geonotesLayer = layer;
                        }
                    }

                    if (!isAlreadyLoaded) {
                        List< ? extends ILayer> addedLayersToMap = ApplicationGIS.addLayersToMap(
                                activeMap, Collections.singletonList(resolve.resolve(
                                        IGeoResource.class, null)), layers.size());
                        for( ILayer l : addedLayersToMap ) {
                            IGeoResource geoResource = l.getGeoResource();
                            if (geoResource.canResolve(GeonotesMapGraphic.class)) {
                                geonotesLayer = l;
                            }
                        }
                    }
                    break;
                }
            }
            return geonotesLayer;
        } catch (IOException e1) {
            GeonotesPlugin
                    .log(
                            "GeonotesPlugin problem: eu.hydrologis.jgrass.geonotes.tools#GeoNoteTool#setActive", e1); //$NON-NLS-1$
            e1.printStackTrace();
            return null;
        }

    }

    public FieldbookView getFieldbookView() {
        try {
            IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                    .getActivePage();
            return (FieldbookView) activePage.findView(FieldbookView.ID);
        } catch (Exception e) {
            return null;
        }
    }

}
