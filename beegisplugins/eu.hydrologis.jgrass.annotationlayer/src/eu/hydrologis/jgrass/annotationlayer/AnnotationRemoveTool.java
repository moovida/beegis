package eu.hydrologis.jgrass.annotationlayer;

import java.awt.Color;
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
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

import eu.hydrologis.jgrass.annotationlayer.mapgraphic.AnnotationLayerMapGraphic;
import eu.hydrologis.jgrass.beegisutils.jgrassported.DressedWorldStroke;

public class AnnotationRemoveTool extends AbstractModalTool implements ModalTool {

    /**
     * ID of the current tool.
     */
    public static final String ID = "eu.hydrologis.jgrass.annotationlayer.annotationremovetool"; //$NON-NLS-1$

    public static final String CATEGORY_ID = "eu.hydrologis.jgrass.annotations.category"; //$NON-NLS-1$

    private DrawPathCommand draw = new DrawPathCommand();

    private int startX = 0, startY = 0;

    private ILayer annotationLayer;

    private Display display;

    private Path p;

    private IViewPart annPropView;

    private IMap activeMap;

    private IViewportModel viewportModel;

    private GeometryFactory gF = new GeometryFactory();

    /**
     * Creates an LayerPointInfo Tool.
     */
    public AnnotationRemoveTool() {
        super(MOUSE | MOTION);

        display = Display.getDefault();

    }

    public void mousePressed( MapMouseEvent e ) {
        activeMap = ApplicationGIS.getActiveMap();
        viewportModel = activeMap.getViewportModel();

        startX = e.x;
        startY = e.y;
        draw.setValid(true); // make sure context.getViewportPane().repaint()

        draw.setPaint(Color.red);
        draw.setStroke(6, 2);
        // knows about us
        context.sendASyncCommand(draw); // should of isValided us

    }

    public void mouseDragged( MapMouseEvent e ) {
        p = new Path(display);
        p.moveTo(startX, startY);
        p.lineTo(e.x, e.y);
        draw.setPath(p);
        getContext().getViewportPane().repaint();
    }

    public void mouseReleased( final MapMouseEvent e ) {
        List<DressedWorldStroke> strokes = AnnotationPlugin.getDefault().getStrokes();
        List<DressedWorldStroke> removeStrokes = new ArrayList<DressedWorldStroke>();

        float[] removeLinePoints = p.getPathData().points;
        Coordinate startPoint = getContext().pixelToWorld((int) removeLinePoints[0],
                (int) removeLinePoints[1]);
        Coordinate endPoint = getContext().pixelToWorld((int) removeLinePoints[2],
                (int) removeLinePoints[3]);
        LineString removeLine = gF.createLineString(new Coordinate[]{startPoint, endPoint});
        String crsWKT = viewportModel.getCRS().toWKT();

        for( DressedWorldStroke dressedWorldStroke : strokes ) {
            Double[] nodes = dressedWorldStroke.nodes;
            Coordinate[] coords = new Coordinate[nodes.length / 2];
            int index = 0;
            for( int i = 0; i < nodes.length; i = i + 2 ) {
                Double first = nodes[i];
                Double sec = nodes[i + 1];
                coords[index] = new Coordinate(first, sec);
                index++;
            }
            if (coords.length < 2) {
                removeStrokes.add(dressedWorldStroke);
                continue;
            }
            LineString tmpLineString = gF.createLineString(coords);
            String strokeCRSWKT = dressedWorldStroke.crsWKT;
            if (!strokeCRSWKT.equals(crsWKT)) {
                try {
                    CoordinateReferenceSystem mapCrs = CRS.parseWKT(crsWKT);
                    CoordinateReferenceSystem strokeCrs = CRS.parseWKT(strokeCRSWKT);
                    MathTransform transform = CRS.findMathTransform(mapCrs, strokeCrs, true);
                    removeLine = (LineString) JTS.transform(removeLine, transform);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

            if (tmpLineString.intersects(removeLine)) {
                removeStrokes.add(dressedWorldStroke);
            }
        }

        strokes.removeAll(removeStrokes);

        draw.setValid(false);
        annotationLayer.refresh(null);
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
        try {
            if (active) {
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
            } else {
                AnnotationPlugin.getDefault().saveAnnotations();
                if (annPropView != null)
                    activePage.hideView(annPropView);
            }
        } catch (final Exception e) {
            String message = "An error occurred.    ";
            ExceptionDetailsDialog.openError(null, message, IStatus.ERROR,
                    AnnotationPlugin.PLUGIN_ID, e);

            e.printStackTrace();
        }

        super.setActive(active);
    }

}
