package eu.hydrologis.jgrass.annotationlayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.mapgraphic.internal.MapGraphicService;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.render.IViewportModel;
import net.refractions.udig.project.ui.ApplicationGIS;
import net.refractions.udig.project.ui.internal.commands.draw.DrawPathCommand;
import net.refractions.udig.project.ui.render.displayAdapter.MapMouseEvent;
import net.refractions.udig.project.ui.tool.AbstractModalTool;
import net.refractions.udig.project.ui.tool.ModalTool;
import net.refractions.udig.ui.ExceptionDetailsDialog;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.geotools.geometry.jts.ReferencedEnvelope;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

import eu.hydrologis.jgrass.annotationlayer.mapgraphic.AnnotationLayerMapGraphic;
import eu.hydrologis.jgrass.beegisutils.jgrassported.DressedWorldStroke;

public class AnnotationTool extends AbstractModalTool implements ModalTool {

    /**
     * ID of the current tool.
     */
    public static final String ID = "eu.hydrologis.jgrass.annotationlayer.annotationtool"; //$NON-NLS-1$

    public static final String CATEGORY_ID = "eu.hydrologis.jgrass.annotations.category"; //$NON-NLS-1$

    private DrawPathCommand draw = new DrawPathCommand();

    private int lastX = 0, lastY = 0;

    private ILayer annotationLayer;

    private Display display;

    private Path p;

    private ReferencedEnvelope currentDrawBounds = null;

    private List<Double> currentDraw = null;

    private IViewPart annPropView;

    private IMap activeMap;

    private IViewportModel viewportModel;

    /**
     * Creates an LayerPointInfo Tool.
     */
    public AnnotationTool() {
        super(MOUSE | MOTION);

        display = Display.getDefault();

    }

    public void mousePressed( MapMouseEvent e ) {
        activeMap = ApplicationGIS.getActiveMap();
        viewportModel = activeMap.getViewportModel();

        lastX = e.x;
        lastY = e.y;
        p = new Path(display);
        p.moveTo(lastX, lastY);
        draw.setValid(true); // make sure context.getViewportPane().repaint()

        draw.setPaint(AnnotationPlugin.getDefault().getCurrentStrokeColor());
        draw.setStroke(AnnotationPlugin.getDefault().getCurrentStrokeStyle(), AnnotationPlugin
                .getDefault().getCurrentStrokeWidth());
        // knows about us
        context.sendASyncCommand(draw); // should of isValided us

        currentDraw = new ArrayList<Double>();
        Coordinate pixelToWorld = viewportModel.pixelToWorld(lastX, lastY);
        currentDraw.add(pixelToWorld.x);
        currentDraw.add(pixelToWorld.y);
        if (currentDrawBounds == null) {
            Envelope env = new Envelope(pixelToWorld, pixelToWorld);
            currentDrawBounds = new ReferencedEnvelope(env, viewportModel.getCRS());
        } else {
            currentDrawBounds.expandToInclude(pixelToWorld);
        }
    }

    public void mouseDragged( MapMouseEvent e ) {

        if (!p.isDisposed()) {

            p.lineTo(e.x, e.y);
            draw.setPath(p);

            getContext().getViewportPane().repaint();

            lastX = e.x;
            lastY = e.y;
            Coordinate pixelToWorld = viewportModel.pixelToWorld(lastX, lastY);
            currentDraw.add(pixelToWorld.x);
            currentDraw.add(pixelToWorld.y);

            if (currentDrawBounds != null) {
                currentDrawBounds.expandToInclude(pixelToWorld);
            }
        }
    }

    /**
     * What's this then?
     * <p>
     * See class description for intended workflow.
     * </p>
     * 
     * @see net.refractions.udig.project.ui.tool.AbstractTool#mouseReleased(MapMouseEvent)
     */
    public void mouseReleased( final MapMouseEvent e ) {
        String crsWKT = viewportModel.getCRS().toWKT();
        DressedWorldStroke d = new DressedWorldStroke();
        d.crsWKT = crsWKT;
        d.nodes = currentDraw.toArray(new Double[currentDraw.size()]);
        d.bounds = currentDrawBounds;
        d.lineStyle[0] = 6;
        d.strokeWidth[0] = AnnotationPlugin.getDefault().getCurrentStrokeWidth();
        d.rgb = AnnotationPlugin.getDefault().getCurrentStrokeColorInt();
        double scale = viewportModel.getScaleDenominator();
        d.scale = scale;
        AnnotationPlugin.getDefault().addStroke(d);

        draw.setValid(false);
        annotationLayer.refresh(d.bounds);
    }

    /**
     * @see net.refractions.udig.project.ui.tool.Tool#dispose()
     */
    public void dispose() {
        super.dispose();
    }

    public void setActive( boolean active ) {
        IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getActivePage();
        if (active) {
            try {
                /*
                 * load the right mapgraphic layer
                 */
                List<IResolve> mapgraphics = CatalogPlugin.getDefault().getLocalCatalog().find(
                        MapGraphicService.SERVICE_URL, null);
                List<IResolve> members = mapgraphics.get(0).members(null);
                for( IResolve resolve : members ) {
                    if (resolve.canResolve(AnnotationLayerMapGraphic.class)) {
                        IMap activeMap = ApplicationGIS.getActiveMap();
                        List<ILayer> layers = activeMap.getMapLayers();
                        boolean isAlreadyLoaded = false;
                        for( ILayer layer : layers ) {
                            if (layer.hasResource(AnnotationLayerMapGraphic.class)) {
                                isAlreadyLoaded = true;
                                annotationLayer = layer;
                            }
                        }

                        if (!isAlreadyLoaded) {
                            List< ? extends ILayer> addedLayersToMap = ApplicationGIS
                                    .addLayersToMap(activeMap, Collections.singletonList(resolve
                                            .resolve(IGeoResource.class, null)), layers.size());
                            for( ILayer l : addedLayersToMap ) {
                                IGeoResource geoResource = l.getGeoResource();
                                if (geoResource.canResolve(AnnotationLayerMapGraphic.class)) {
                                    annotationLayer = l;
                                }
                            }
                        }
                        annotationLayer.refresh(null);
                    }
                }
                annPropView = activePage.showView(AnnotationsPropertiesView.ID);
            } catch (Exception e) {
                e.printStackTrace();
                String message = "An error occurred while loading the annotation graphics.";
                ExceptionDetailsDialog.openError(null, message, IStatus.ERROR,
                        AnnotationPlugin.PLUGIN_ID, e);
            }
        } else {
            new Thread(){
                public void run() {
                    try {
                        AnnotationPlugin.getDefault().saveAnnotations();
                    } catch (Exception e) {
                        String message = "An error occurred while saving the annotations.";
                        ExceptionDetailsDialog.openError(null, message, IStatus.ERROR,
                                AnnotationPlugin.PLUGIN_ID, e);
                    }
                };
            }.start();
            if (annPropView != null)
                activePage.hideView(annPropView);
        }

        super.setActive(active);
    }
}
