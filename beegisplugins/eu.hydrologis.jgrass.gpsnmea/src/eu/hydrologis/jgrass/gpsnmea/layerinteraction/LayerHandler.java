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
package eu.hydrologis.jgrass.gpsnmea.layerinteraction;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.command.UndoableMapCommand;
import net.refractions.udig.project.command.factory.EditCommandFactory;
import net.refractions.udig.project.internal.commands.edit.AddFeatureCommand;
import net.refractions.udig.project.ui.IAnimation;
import net.refractions.udig.project.ui.commands.DrawCommandFactory;
import net.refractions.udig.tools.edit.animation.AddVertexAnimation;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import eu.hydrologis.jgrass.gpsnmea.GpsActivator;
import eu.hydrologis.jgrass.gpsnmea.gps.GpsPoint;

/**
 * 
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class LayerHandler {
    public static String POINT = ".*[Pp][Oo][iI][nN][tT].*"; //$NON-NLS-1$
    public static String MULTIPOINT = ".*[Mm][Uu][Ll][Tt][Ii][Pp][Oo][iI][nN][tT].*"; //$NON-NLS-1$
    public static String LINE = ".*[lL][iI][nN][eE].*"; //$NON-NLS-1$
    public static String MULTILINE = ".*[Mm][Uu][Ll][Tt][Ii][lL][iI][nN][eE].*"; //$NON-NLS-1$
    public static String POLYGON = ".*[pP][Oo][lL][yY][gG][oO][nN].*"; //$NON-NLS-1$
    public static String MULTIPOLYGON = ".*[Mm][Uu][Ll][Tt][Ii][pP][Oo][lL][yY][gG][oO][nN].*"; //$NON-NLS-1$

    private final GeometryFactory gFac = new GeometryFactory();
    private boolean continueFromLast = false;
    private static LayerHandler instanceLH = null;
    private final LinkedHashMap<ILayer, SimpleFeature> layerLastFeatureMap = new LinkedHashMap<ILayer, SimpleFeature>();

    private LayerHandler() {
    }

    public static LayerHandler getInstance() {
        if (instanceLH == null) {
            instanceLH = new LayerHandler();
        }
        return instanceLH;
    }

    /**
     * Initializes the feature on the selected layer to use for the Gps. 
     * 
     * <p>
     * It checks for compatibility, and also for the acquisition mode. The feature
     * is created or extracted and put into the layer-vs-feature stack to be
     * used when adding a new point.
     * </p>
     * 
     * @param layer
     * @return
     * @throws Exception
     */
    public boolean initLayer( ILayer layer ) throws Exception {

        if (!GpsActivator.getDefault().isGpsLogging()) {
            return false;
        }
        /*
         * first check if there is already a feature for this layer, in which
         * case it is assumed that we continue anyway from that feature.
         */
        SimpleFeature featureToUse = layerLastFeatureMap.get(layer);
        if (featureToUse == null) {
            /*
             * No feature available, therefore a new feature has to be created.
             * If the gps points have to added to the last feature in the layer,
             * the geometry of the last feature has to be extracted, else a new
             * feature has to be created and the attributes for that feature
             * have to be asked to the user.
             */
            FeatureSource<SimpleFeatureType, SimpleFeature> source = layer.getResource(
                    FeatureSource.class, new NullProgressMonitor());
            if (source == null) {
                throw new IllegalArgumentException(
                        "This layer is not supported, only feature layers are supported.");
            }
            // get the featurecollection from the layer, even if empty
            FeatureCollection<SimpleFeatureType, SimpleFeature> selectedFeatureCollection = source
                    .getFeatures();
            SimpleFeatureType schema = selectedFeatureCollection.getSchema();
            // find the geometry type
            String geometryType = getGeometryType(schema);
            if (geometryType == null) {
                throw new IllegalArgumentException(
                        "This layer is not supported, only feature layers are supported.");
            }

            /*
             * If geomType is null, we have an empty collection, in which case a
             * new feature has to be created. If the geometry is a point, a new
             * feature has to be created also, because in that case every new
             * gps point turns out to be a new created feature.
             */
            if (continueFromLast && !geometryType.matches(POINT)) {
                /*
                 * find last feature of the layer, which will be the one we will
                 * work on
                 */
                FeatureIterator<SimpleFeature> featureIterator = selectedFeatureCollection
                        .features();
                while( featureIterator.hasNext() ) {
                    featureToUse = featureIterator.next();
                }
                selectedFeatureCollection.close(featureIterator);
            }
            // the following can happen in both the cases that the collection is
            // empty or a new feature is requested
            if (featureToUse == null) {
                /*
                 * in this case the feature has to be created on the blueprint
                 * of the layer's features, asking the user for the new
                 * attributes to use. Note that getFeatureType here doesn't
                 * supply hwat you want.
                 */
                SimpleFeatureType featureType = selectedFeatureCollection.getSchema();
                /*
                 * try to create default features where possible
                 */
                Object attributes[] = DataUtilities.defaultValues(featureType);

                // create the feature
                SimpleFeatureBuilder builder = new SimpleFeatureBuilder(featureType);
                // add the values
                builder.addAll(attributes);
                // build the feature with provided ID
                featureToUse = builder.buildFeature(featureType.getTypeName() + ".0"); //$NON-NLS-1$
            }

            if (featureToUse == null) {
                return false;
            }

            layerLastFeatureMap.put(layer, featureToUse);
        }

        return true;
    }

    /**
     * Add a point to the geometry of the passed layer. Check on type is done
     * and necessary stuff to add the new coordinate to the feature.
     * 
     * @param layer
     * @param gpsPoint
     * @throws Exception
     */
    public void addGpsPointToLayer( ILayer layer, GpsPoint gpsPoint ) throws Exception {
        if (!GpsActivator.getDefault().isGpsLogging()) {
            return;
        }
        SimpleFeature feature = layerLastFeatureMap.get(layer);
        String geometryType = getGeometryType(feature.getFeatureType());
        // TODO askAttributes(feature);
        if (geometryType.matches(POINT)) {
            addPoint(layer, gpsPoint, feature, geometryType);
        } else if (geometryType.matches(LINE)) {
            addLine(layer, gpsPoint, feature, geometryType);
        } else if (geometryType.matches(POLYGON)) {
            addPolygon(layer, gpsPoint, feature, geometryType);
        }

    }

    /**
     * @param layer
     * @param gpsPoint
     * @param feature
     * @param geometryType
     * @throws IllegalAttributeException
     */
    private void addPoint( ILayer layer, GpsPoint gpsPoint, SimpleFeature feature,
            String geometryType ) throws Exception {
        // for points simply add the point to the layer
        Point gpsPointGeometry = gFac.createPoint(new Coordinate(gpsPoint.longitude,
                gpsPoint.latitude));
        if (geometryType.matches(MULTIPOINT)) {
            feature.setDefaultGeometry(gFac.createMultiPoint(new Point[]{gpsPointGeometry}));
        } else {
            feature.setDefaultGeometry(gpsPointGeometry);
        }
        UndoableMapCommand createAddFeatureCommand = EditCommandFactory.getInstance()
                .createAddFeatureCommand(feature, layer);
        layer.getMap().sendCommandASync(createAddFeatureCommand);
    }

    /**
     * @param layer
     * @param gpsPoint
     * @param feature
     * @param geometryType
     * @throws IllegalAttributeException
     */
    private void addLine( ILayer layer, GpsPoint gpsPoint, SimpleFeature feature,
            String geometryType ) throws Exception {
        /*
         * in the case of lines, the geometry has to be extended if we are
         * continuing from a current line, if instead we have a different
         * geometry, we start from scratch and the geometry has to be replaced.
         */
        IMap map = layer.getMap();
        if (continueFromLast) {
            Geometry geometry = addPointToLineFeature(gpsPoint, feature, geometryType);

            UndoableMapCommand[] cmds = new UndoableMapCommand[2];
            cmds[0] = EditCommandFactory.getInstance().createSetEditFeatureCommand(feature, layer);
            cmds[1] = EditCommandFactory.getInstance().createSetGeomteryCommand(feature, layer,
                    geometry);

            map.sendCommandASync(cmds[0]);
            map.sendCommandASync(cmds[1]);
        } else {
            /*
             * if not continued from the last, there are two possibilities: (1.)
             * the feature has to be created from scratch, or (2.) the feature
             * was created already from scratch, but we have to continue from
             * the new feature created. This can be tested by simply asking for
             * the geometry, which in the first case is null.
             */
            Coordinate gpsCoordinate = new Coordinate(gpsPoint.longitude, gpsPoint.latitude);
            Coordinate gpsCoordinateDelta = new Coordinate(gpsPoint.longitude, gpsPoint.latitude);
            Geometry geometry = (Geometry) feature.getDefaultGeometry();
            if (geometry == null) {
                // create a new geometry for the feature
                LineString lString = gFac.createLineString(new Coordinate[]{gpsCoordinate,
                        gpsCoordinateDelta});
                if (geometryType.matches(MULTILINE)) {
                    geometry = gFac.createMultiLineString(new LineString[]{lString});
                } else {
                    geometry = lString;
                }
                feature.setDefaultGeometry(geometry);
                // draw the add vertex position
                java.awt.Point p = map.getViewportModel().worldToPixel(gpsCoordinate);
                UndoableMapCommand createStartAnimationCommand = DrawCommandFactory.getInstance()
                        .createStartAnimationCommand(
                                map.getRenderManager().getMapDisplay(),
                                Collections.singletonList((IAnimation) new AddVertexAnimation(p.x,
                                        p.y)));
                map.sendCommandASync(createStartAnimationCommand);
                // add the feature to the layer
                UndoableMapCommand createAddFeatureCommand = EditCommandFactory.getInstance()
                        .createAddFeatureCommand(feature, layer);
                map.sendCommandSync(createAddFeatureCommand);
                // and put it inside the map
                SimpleFeature newFeatureAdded = ((AddFeatureCommand) createAddFeatureCommand)
                        .getNewFeature();
                layerLastFeatureMap.put(layer, newFeatureAdded);

            } else {
                // draw the add vertex position
                java.awt.Point p = map.getViewportModel().worldToPixel(gpsCoordinate);
                UndoableMapCommand createStartAnimationCommand = DrawCommandFactory.getInstance()
                        .createStartAnimationCommand(
                                map.getRenderManager().getMapDisplay(),
                                Collections.singletonList((IAnimation) new AddVertexAnimation(p.x,
                                        p.y)));
                map.sendCommandASync(createStartAnimationCommand);
                // add to the new created
                geometry = addPointToLineFeature(gpsPoint, feature, geometryType);
                UndoableMapCommand cmd = EditCommandFactory.getInstance()
                        .createSetEditFeatureCommand(feature, layer);
                map.sendCommandASync(cmd);
                cmd = EditCommandFactory.getInstance().createSetGeomteryCommand(feature, layer,
                        geometry);
                map.sendCommandASync(cmd);
            }
        }
    }

    /**
     * @param layer
     * @param gpsPoint
     * @param feature
     * @param geometryType
     * @throws IllegalAttributeException
     */
    private void addPolygon( ILayer layer, GpsPoint gpsPoint, SimpleFeature feature,
            String geometryType ) throws Exception {
        /*
         * in the case of lines, the geometry has to be extended if we are
         * continuing from a current line, if instead we have a different
         * geometry, we start from scratch and the geometry has to be replaced.
         */
        IMap map = layer.getMap();
        if (continueFromLast) {
            Geometry geometry = addPointToPolygonFeature(gpsPoint, feature, geometryType);

            UndoableMapCommand[] cmds = new UndoableMapCommand[2];
            cmds[0] = EditCommandFactory.getInstance().createSetEditFeatureCommand(feature, layer);
            cmds[1] = EditCommandFactory.getInstance().createSetGeomteryCommand(feature, layer,
                    geometry);

            map.sendCommandASync(cmds[0]);
            map.sendCommandASync(cmds[1]);
        } else {

            /*
             * if not continued from the last, there are two possibilities: (1.)
             * the feature has to be created from scratch, or (2.) the feature
             * was created already from scratch, but we have to continue from
             * the new feature created. This can be tested by simply asking for
             * the geometry, which in the first case is null.
             */
            Coordinate gpsCoordinate = new Coordinate(gpsPoint.longitude, gpsPoint.latitude);
            Geometry geometry = (Geometry) feature.getDefaultGeometry();
            if (geometry == null) {
                // create a new geometry for the feature
                Coordinate gpsCoordinateDelta = new Coordinate(gpsPoint.longitude,
                        gpsPoint.latitude);
                Coordinate gpsCoordinateDelta2 = new Coordinate(gpsPoint.longitude,
                        gpsPoint.latitude);
                LinearRing linearRing = gFac.createLinearRing(new Coordinate[]{gpsCoordinate,
                        gpsCoordinateDelta, gpsCoordinateDelta2, gpsCoordinate});
                Polygon polygon = gFac.createPolygon(linearRing, null);
                if (geometryType.matches(MULTIPOLYGON)) {
                    geometry = gFac.createMultiPolygon(new Polygon[]{polygon});
                } else {
                    geometry = polygon;
                }
                feature.setDefaultGeometry(geometry);
                // draw the add vertex position
                java.awt.Point p = map.getViewportModel().worldToPixel(gpsCoordinate);
                UndoableMapCommand createStartAnimationCommand = DrawCommandFactory.getInstance()
                        .createStartAnimationCommand(
                                map.getRenderManager().getMapDisplay(),
                                Collections.singletonList((IAnimation) new AddVertexAnimation(p.x,
                                        p.y)));
                map.sendCommandASync(createStartAnimationCommand);
                // add the feature to the layer
                UndoableMapCommand createAddFeatureCommand = EditCommandFactory.getInstance()
                        .createAddFeatureCommand(feature, layer);
                map.sendCommandSync(createAddFeatureCommand);
                // and put it inside the map
                SimpleFeature newFeatureAdded = ((AddFeatureCommand) createAddFeatureCommand)
                        .getNewFeature();
                layerLastFeatureMap.put(layer, newFeatureAdded);
            } else {
                // draw the add vertex position
                java.awt.Point p = map.getViewportModel().worldToPixel(gpsCoordinate);
                UndoableMapCommand createStartAnimationCommand = DrawCommandFactory.getInstance()
                        .createStartAnimationCommand(
                                map.getRenderManager().getMapDisplay(),
                                Collections.singletonList((IAnimation) new AddVertexAnimation(p.x,
                                        p.y)));
                map.sendCommandASync(createStartAnimationCommand);
                // add to the new created
                geometry = addPointToPolygonFeature(gpsPoint, feature, geometryType);
                UndoableMapCommand cmd = EditCommandFactory.getInstance()
                        .createSetEditFeatureCommand(feature, layer);
                map.sendCommandASync(cmd);
                cmd = EditCommandFactory.getInstance().createSetGeomteryCommand(feature, layer,
                        geometry);
                map.sendCommandASync(cmd);
            }

        }
    }

    /**
     * @param gpsPoint
     * @param feature
     * @param geometryType
     * @return a new geometry containing the added coordinate
     */
    private Geometry addPointToLineFeature( GpsPoint gpsPoint, SimpleFeature feature,
            String geometryType ) {
        Geometry geometry = (Geometry) feature.getDefaultGeometry();
        Coordinate[] coordinates = geometry.getCoordinates();

        Coordinate[] newCoordinates = null;
        if (coordinates[0].equals(coordinates[1])) {
            coordinates[1] = new Coordinate(gpsPoint.longitude, gpsPoint.latitude);
            newCoordinates = coordinates;
        } else {
            newCoordinates = new Coordinate[coordinates.length + 1];
            System.arraycopy(coordinates, 0, newCoordinates, 0, coordinates.length);
            newCoordinates[newCoordinates.length - 1] = new Coordinate(gpsPoint.longitude,
                    gpsPoint.latitude);
        }
        LineString lString = gFac.createLineString(newCoordinates);
        if (geometryType.matches(MULTILINE)) {
            geometry = gFac.createMultiLineString(new LineString[]{lString});
        } else {
            geometry = lString;
        }
        return geometry;
    }

    /**
     * @param gpsPoint
     * @param feature
     * @param geometryType
     * @return
     */
    private Geometry addPointToPolygonFeature( GpsPoint gpsPoint, SimpleFeature feature,
            String geometryType ) {
        Geometry geometry = (Geometry) feature.getDefaultGeometry();
        Coordinate[] coordinates = geometry.getCoordinates();
        Coordinate[] newCoordinates = null;
        if (coordinates[0].equals(coordinates[1])) {
            coordinates[1] = new Coordinate(gpsPoint.longitude, gpsPoint.latitude);
            coordinates[2] = new Coordinate(gpsPoint.longitude, gpsPoint.latitude);
            newCoordinates = coordinates;
        } else if (coordinates[0].equals(coordinates[2])) {
            coordinates[2] = new Coordinate(gpsPoint.longitude, gpsPoint.latitude);
            newCoordinates = coordinates;
        } else {
            newCoordinates = new Coordinate[coordinates.length + 1];
            System.arraycopy(coordinates, 0, newCoordinates, 0, coordinates.length);
            newCoordinates[newCoordinates.length - 1] = newCoordinates[newCoordinates.length - 2];
            newCoordinates[newCoordinates.length - 2] = new Coordinate(gpsPoint.longitude,
                    gpsPoint.latitude);
        }
        LinearRing linearRing = gFac.createLinearRing(newCoordinates);
        Polygon polygon = gFac.createPolygon(linearRing, null);
        if (geometryType.matches(MULTIPOLYGON)) {
            geometry = gFac.createMultiPolygon(new Polygon[]{polygon});
        } else {
            geometry = polygon;
        }
        return geometry;
    }

    public void cleanLayerMap() {
        layerLastFeatureMap.clear();
    }

    public boolean isContinueFromLast() {
        return continueFromLast;
    }

    public void setContinueFromLast( boolean continueFromLast ) {
        this.continueFromLast = continueFromLast;
    }

    /**
     * @param schema
     * @return
     */
    private String getGeometryType( SimpleFeatureType schema ) {
        List<AttributeType> attributeTypes = schema.getTypes();
        String geometryType = null;
        for( AttributeType attributeType : attributeTypes ) {
            if (attributeType instanceof GeometryType) {
                geometryType = ((GeometryType) attributeType).getName().toString();
            }
        }
        return geometryType;
    }

    /**
     * @param feature
     * @throws IOException
     */
    // private void askAttributes(Feature feature) throws IOException {
    // XmlCreator xmlCreator = new XmlCreator("fill feature attributes",
    // "fill feature attributes");
    // xmlCreator.createFeatureGui(feature);
    // NodeList nodeList = xmlCreator.getNodeList();
    // xmlCreator.dumpXmlToFile(null);
    // Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
    // Properties properties = new Properties();
    // GuiBuilderDialog swtDialog = new GuiBuilderDialog(shell, nodeList,
    // properties);
    // swtDialog.setBlockOnOpen(true);
    // swtDialog.open();
    // System.out.println(properties.get(UIBuilder.COMMANDLINEPROPERTY));
    // }
    // @SuppressWarnings({"nls", "deprecation"})
    // public static void main( String[] args ) {
    // try {
    // GeometryFactory gF = new GeometryFactory();
    // GeometryAttributeType geometryAttribute = (GeometryAttributeType) AttributeTypeFactory
    // .newAttributeType("the_geom", MultiPoint.class, false, 1, gF
    // .createMultiPoint(new Coordinate[]{new Coordinate(0.0, 0.0)}));
    // AttributeType catAttribute = AttributeTypeFactory.newAttributeType("cat",
    // Integer.class, true, 1, 1);
    // AttributeType levelAttribute = AttributeTypeFactory.newAttributeType("level",
    // Float.class);
    //
    // SimpleFeatureType featureType = null;
    // featureType = FeatureTypeBuilder.newFeatureType(new AttributeType[]{geometryAttribute,
    // catAttribute, levelAttribute}, "name");
    //
    // AttributeType attributeType = featureType.getAttributeType(0);
    //
    // System.out.println(attributeType);
    // Object attributes[] = DataUtilities.defaultValues(featureType);
    // SimpleFeature featureToUse = featureType.create(attributes);
    //
    // System.out.println(featureToUse.getDefaultGeometry().getGeometryType());
    //
    // FeatureCollection newCollection = FeatureCollections.newCollection();
    // newCollection.add(featureToUse);
    //
    // SimpleFeatureType featureType2 = newCollection.getFeatureType();
    // SimpleFeatureType schema = newCollection.getSchema();
    //
    // System.out.println();
    //
    // } catch (Exception e1) {
    // e1.printStackTrace();
    // }
    //
    // }
}
