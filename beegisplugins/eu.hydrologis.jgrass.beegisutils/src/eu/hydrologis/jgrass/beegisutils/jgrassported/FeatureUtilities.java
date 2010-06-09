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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.ICatalog;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.IServiceFactory;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.ui.ApplicationGIS;
import net.refractions.udig.ui.ExceptionDetailsDialog;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.FileDataStoreFactorySpi;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.metadata.Identifier;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

import eu.hydrologis.jgrass.beegisutils.BeegisUtilsPlugin;

public class FeatureUtilities {

    /**
     * @param outputFile
     * @param addToCatalog
     * @param addToActiveMap
     * @param progressMonitor
     */
    public static synchronized void addServiceToCatalogAndMap( String outputFile,
            boolean addToCatalog, boolean addToActiveMap, IProgressMonitor progressMonitor ) {
        try {
            URL fileUrl = new File(outputFile).toURI().toURL();
            if (addToCatalog) {
                IServiceFactory sFactory = CatalogPlugin.getDefault().getServiceFactory();
                ICatalog catalog = CatalogPlugin.getDefault().getLocalCatalog();
                List<IService> services = sFactory.createService(fileUrl);
                for( IService service : services ) {
                    catalog.add(service);
                    if (addToActiveMap) {
                        IMap activeMap = ApplicationGIS.getActiveMap();
                        int layerNum = activeMap.getMapLayers().size();
                        List<IResolve> members = service.members(progressMonitor);
                        for( IResolve iRes : members ) {
                            if (iRes.canResolve(IGeoResource.class)) {
                                IGeoResource geoResource = iRes.resolve(IGeoResource.class,
                                        progressMonitor);
                                ApplicationGIS.addLayersToMap(null, Collections
                                        .singletonList(geoResource), layerNum);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            String message = "An error occurred while adding the service to the catalog.";
            ExceptionDetailsDialog.openError(null, message, IStatus.ERROR,
                    BeegisUtilsPlugin.PLUGIN_ID, e);
            e.printStackTrace();
        }
    }

    /**
     * Fill the prj file with the actual map projection.
     * 
     * @param shapePath the path to the regarding shapefile
     */
    public static void writeProjectionFile( String shapePath ) throws IOException {
        IMap activeMap = ApplicationGIS.getActiveMap();
        CoordinateReferenceSystem mapCrs = activeMap.getViewportModel().getCRS();

        String prjPath = null;
        if (shapePath.toLowerCase().endsWith(".shp")) {
            int dotLoc = shapePath.lastIndexOf(".");
            prjPath = shapePath.substring(0, dotLoc);
            prjPath = prjPath + ".prj";
        } else {
            prjPath = shapePath + ".prj";
        }

        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(prjPath));
            bufferedWriter.write(mapCrs.toWKT());
        } finally {
            if (bufferedWriter != null)
                bufferedWriter.close();
        }
    }

    /**
     * Create a featurecollection from a vector of features
     * 
     * @param features - the vectore of features
     * @return the created featurecollection
     */
    public static FeatureCollection<SimpleFeatureType, SimpleFeature> createFeatureCollection(
            SimpleFeature... features ) {
        FeatureCollection<SimpleFeatureType, SimpleFeature> fcollection = FeatureCollections
                .newCollection();

        for( SimpleFeature feature : features ) {
            fcollection.add(feature);
        }
        return fcollection;
    }

    /**
     * <p>
     * Convert a csv file to a shapefile. <b>This for now supports only point geometries</b>.<br>
     * For different crs it also performs coor transformation.
     * </p>
     * <p>
     * <b>NOTE: this doesn't support date attributes</b>
     * </p>
     * 
     * @param srcCrs the source crs
     * @param destCrs the destination crs to which to reproject
     * @param csvPath the file path
     * @param shapePath
     * @param is3d true if the first three items stang for x, y, z. Else only x, y are supposed to
     *        be there
     * @param fileHasHeader if the file has the header with all the infos
     * @param separator the file separator if different from comma
     * @param _classAndFieldNames the filed names and types (ex.
     *        "geom:MultiLineString,FieldName:java.lang.Integer")
     * @param addToMapAndCatalog
     * @param pm
     * @throws Exception
     */
    @SuppressWarnings("nls")
    public static void csvFileToShapeFile( CoordinateReferenceSystem srcCrs,
            CoordinateReferenceSystem destCrs, String csvPath, String shapePath, boolean is3d,
            boolean fileHasHeader, String separator, String _classAndFieldNames,
            boolean addToMapAndCatalog, IProgressMonitor pm ) throws Exception {

        if (separator == null) {
            separator = ",";
        }

        String classAndFieldNames = null;
        if (_classAndFieldNames != null) {
            classAndFieldNames = _classAndFieldNames;
        }
        // number of fields without geometry
        int fields = _classAndFieldNames.split(separator).length - 1;
        int numberOfFiledstoBeLineWithAttributes = is3d ? 3 + fields : 2 + fields;

        int index = 0;
        BufferedReader bR = new BufferedReader(new FileReader(csvPath));
        while( bR.readLine() != null ) {
            index++;
        }
        bR.close();

        bR = new BufferedReader(new FileReader(csvPath));
        String line = null;
        int rows = 0;
        List<String[]> lines = new ArrayList<String[]>();
        List<CoordinateList> coords = new ArrayList<CoordinateList>();
        CoordinateList cList = new CoordinateList();
        pm.beginTask("Importing raw data", index);
        while( (line = bR.readLine()) != null ) {
            pm.worked(1);
            if (fileHasHeader && classAndFieldNames == null) {
                classAndFieldNames = line;
                continue;
            }

            /*
             * do some check if it is a line with attributes or just a coordinate line for
             * complexer geometries
             */
            String[] lineSplit = line.split(separator);
            cList.add(new Coordinate(Double.parseDouble(lineSplit[0]), Double
                    .parseDouble(lineSplit[1]), (is3d ? Double.parseDouble(lineSplit[2]) : 0.0)));
            if (lineSplit.length == numberOfFiledstoBeLineWithAttributes) {
                lines.add(lineSplit);
                coords.add(cList);
                cList = new CoordinateList();
                rows++;
            }
        }
        bR.close();
        pm.done();

        int cols = classAndFieldNames.split(separator).length;
        // if it is 2d, we will have x,y, but need only onle field for
        // geometry
        // if (is3d) {
        // cols = cols - 2;
        // } else {
        // cols = cols - 1;
        // }

        Object[][] allObjects = new Object[rows][cols];
        GeometryFactory gf = new GeometryFactory();
        MathTransform mT = null;
        if (destCrs != null && srcCrs != null) {
            String destCrsName = null;
            Iterator<ReferenceIdentifier> iterator = destCrs.getIdentifiers().iterator();
            if (iterator.hasNext()) {
                Identifier id = iterator.next();
                destCrsName = id.toString();
            }
            String srcCrsName = null;
            iterator = srcCrs.getIdentifiers().iterator();
            if (iterator.hasNext()) {
                Identifier id = iterator.next();
                srcCrsName = id.toString();
            }
            // transform only if necessary
            if (!srcCrsName.equals(destCrsName)) {
                mT = CRS.findMathTransform(srcCrs, destCrs, true);
            }
        }

        String[] tmp = _classAndFieldNames.split(",");
        pm.beginTask("Creating geometries", rows);
        for( int i = 0; i < rows; i++ ) {
            pm.worked(1);
            String[] l = lines.get(i);
            Coordinate[] c = coords.get(i).toCoordinateArray();

            // the geometry
            Geometry g = null;
            int k = 0;
            if (is3d) {
                k = 3;
            } else {
                k = 2;
            }

            if (tmp[0].matches(".*Point.*")) {
                // points
                g = gf.createMultiPoint(c);
            }

            if (tmp[0].matches(".*Polygon.*")) {
                /*
                 * check polygons
                 */
                List<LinearRing> rings = new ArrayList<LinearRing>();
                List<Coordinate> coordsList = new ArrayList<Coordinate>();
                coordsList.add(c[0]);
                Coordinate checkFirst = c[0];
                for( int j = 1; j < c.length; j++ ) {
                    coordsList.add(c[j]);
                    if (checkFirst.equals(c[j])) {
                        // one polygon was closed
                        if (j + 2 < c.length) {
                            checkFirst = c[j + 1];
                            j = j + 2;
                        }
                        // create the ring
                        LinearRing lRing = gf.createLinearRing((Coordinate[]) coordsList
                                .toArray(new Coordinate[coordsList.size()]));
                        rings.add(lRing);
                        // empty the coords
                        coordsList.removeAll(coordsList);
                        // and again add the first
                        coordsList.add(checkFirst);
                    }
                }

                List<Polygon> polygons = new ArrayList<Polygon>();
                for( LinearRing linearRing : rings ) {
                    Polygon p = gf.createPolygon(linearRing, null);
                    polygons.add(p);
                }
                if (polygons.size() > 0) {
                    g = gf.createMultiPolygon((Polygon[]) polygons.toArray(new Polygon[polygons
                            .size()]));
                }
            }
            if (tmp[0].matches(".*Line.*")) {
                // line
                g = gf.createMultiLineString(new LineString[]{gf.createLineString(c)});
            } else {

            }

            if (mT != null) {
                g = JTS.transform(g, mT);
            }
            allObjects[i][0] = g;
            // all the attributes
            for( int j = 0; j < cols - 1; j++ ) {
                String className = tmp[j + 1].split(":")[1];
                Class< ? > cls = Class.forName(className);
                Class< ? > partypes[] = new Class[1];
                partypes[0] = String.class;
                Constructor< ? > ct = cls.getConstructor(partypes);
                Object arglist[] = new Object[1];
                arglist[0] = l[j + k].trim();
                allObjects[i][j + 1] = ct.newInstance(arglist);
            }
        }
        pm.done();

        pm.beginTask("Writing to shapefile", 4);
        pm.worked(1);
        ShapefileDataStore dStore = createShapeFileDatastore(shapePath, classAndFieldNames, destCrs);

        pm.worked(1);
        FeatureCollection fet = createFeatures(dStore.getSchema(), allObjects);

        pm.worked(1);
        writeToShapefile(dStore, fet);
        pm.worked(1);
        pm.done();
        if (addToMapAndCatalog) {
            // Create the file you want to write to
            if (!shapePath.toLowerCase().endsWith(".shp")) {
                shapePath = shapePath + ".shp";
            }
            addServiceToCatalogAndMap(shapePath, addToMapAndCatalog, addToMapAndCatalog, null);
        }

    }

    /**
     * Create a {@link FeatureCollection} from the feature type and an array of records containing
     * the attributes. Every record holds something like [geom, arg1, arg2, ...]
     * 
     * @param schema
     * @param allObjects
     * @return the new created collection
     */
    private static FeatureCollection<SimpleFeatureType, SimpleFeature> createFeatures(
            SimpleFeatureType schema, Object[][] allObjects ) {

        FeatureCollection<SimpleFeatureType, SimpleFeature> newCollection = FeatureCollections
                .newCollection();
        // create the feature
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(schema);
        // add the values
        for( int i = 0; i < allObjects.length; i++ ) {
            Object[] objects = allObjects[i];
            builder.addAll(objects);
            SimpleFeature feature = builder.buildFeature(schema.getTypeName() + "." + i);
            newCollection.add(feature);
        }
        return newCollection;
    }

    /**
     * Reproject a geometry
     * 
     * @param from the starting crs
     * @param to the destination crs
     * @param geometries the array of geometries, wrapped into an Object array
     * @throws Exception
     */
    public static void reproject( CoordinateReferenceSystem from, CoordinateReferenceSystem to,
            Object[] geometries ) throws Exception {
        // if no from crs, use the map's one
        if (from == null) {
            from = ApplicationGIS.getActiveMap().getViewportModel().getCRS();
        }
        // if no to crs, use lat/long wgs84
        if (to == null) {
            to = CRS.decode("EPSG:4326"); //$NON-NLS-1$
        }
        MathTransform mathTransform = CRS.findMathTransform(from, to);

        for( int i = 0; i < geometries.length; i++ ) {
            geometries[i] = JTS.transform((Geometry) geometries[i], mathTransform);
        }
    }

    /**
     * @param fet the featurecollection for which to create a temporary layer resource
     */
    public static void featureCollectionToTempLayer( FeatureCollection fet ) {
        IGeoResource resource = CatalogPlugin.getDefault().getLocalCatalog()
                .createTemporaryResource(fet.getSchema());
        try {

            FeatureStore fStore = resource.resolve(FeatureStore.class, new NullProgressMonitor());
            fStore.addFeatures(fet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ApplicationGIS.addLayersToMap(ApplicationGIS.getActiveMap(), Collections
                .singletonList(resource), -1);
    }

    /**
     * @param name the shapefile name
     * @param fieldsSpec to create other fields you can use a string like : <br>
     *        "geom:MultiLineString,FieldName:java.lang.Integer" <br>
     *        field name can not be over 10 characters use a ',' between each field <br>
     *        field types can be : java.lang.Integer, java.lang.Long, // java.lang.Double,
     *        java.lang.String or java.util.Date
     * @return
     */
    @SuppressWarnings("nls")
    public static synchronized ShapefileDataStore createShapeFileDatastore( String name,
            String fieldsSpec, CoordinateReferenceSystem crs ) {
        try {
            // Create the DataStoreFactory
            FileDataStoreFactorySpi factory = new ShapefileDataStoreFactory();

            // Create the file you want to write to
            File file = null;
            if (name.toLowerCase().endsWith(".shp")) {
                file = new File(name);
            } else {
                file = new File(name + ".shp");
            }
            // Create a Map object used by our DataStore Factory
            // NOTE: file.toURI().toURL() is used because file.toURL() is
            // deprecated
            Map<String, Serializable> map = Collections.singletonMap("shapefile url",
                    (Serializable) file.toURI().toURL());

            // Create the ShapefileDataStore from our factory based on our Map
            // object
            ShapefileDataStore myData = (ShapefileDataStore) factory.createNewDataStore(map);

            // Tell this shapefile what type of data it will store
            // Shapefile handle only : Point, MultiPoint, MultiLineString,
            // MultiPolygon
            SimpleFeatureType featureType = DataUtilities.createType(name, fieldsSpec);

            // Create the Shapefile (empty at this point)
            myData.createSchema(featureType);

            // Tell the DataStore what type of Coordinate Reference System (CRS)
            // to use
            myData.forceSchemaCRS(crs);

            return myData;

        } catch (IOException e) {
            BeegisUtilsPlugin.log("BeegisUtilsPlugin problem", e); //$NON-NLS-1$
            e.printStackTrace();
        } catch (SchemaException se) {
            BeegisUtilsPlugin.log("BeegisUtilsPlugin problem", se); //$NON-NLS-1$
            se.printStackTrace();
        }
        return null;
    }

    /**
     * Writes a featurecollection to a shapefile
     * 
     * @param data the datastore
     * @param collection the featurecollection
     * @throws IOException 
     */
    public static synchronized void writeToShapefile( ShapefileDataStore data,
            FeatureCollection<SimpleFeatureType, SimpleFeature> collection ) throws IOException {
        String featureName = data.getTypeNames()[0]; // there is only one in
        // a shapefile
        FeatureStore<SimpleFeatureType, SimpleFeature> store = null;

        Transaction transaction = null;
        try {

            // Create the DefaultTransaction Object
            transaction = Transaction.AUTO_COMMIT;

            // Tell it the name of the shapefile it should look for in our
            // DataStore
            FeatureSource<SimpleFeatureType, SimpleFeature> source = data
                    .getFeatureSource(featureName);
            store = (FeatureStore<SimpleFeatureType, SimpleFeature>) source;
            store.addFeatures(collection);
            data.getFeatureWriter(transaction);

            transaction.commit();
        } catch (Exception eek) {
            transaction.rollback();
            throw new IOException("The transaction could now be finished, an error orrcurred", eek);
        } finally {
            transaction.close();
        }
    }

}
