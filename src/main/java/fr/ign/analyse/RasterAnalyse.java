package fr.ign.analyse;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.math3.stat.correlation.Covariance;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.coverage.grid.io.OverviewPolicy;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.grid.Grids;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class RasterAnalyse {

	/**
	 * This class contains several methods used for the analysis of the MUP-City outputs during the sensibility and stability tests raster outputs must contains the selected to
	 * urbanize cells mixed with the evaluation layer (output of the extract-eval-anal method) The raster selected with the selectWith method are compared within the mergeRaster
	 * method There is two ways to compare rasters : if they are composed of the exact same grid, we will use the relative position of the cells within this grid. The "discrete"
	 * variable will be "false" and CreateStats method will be used to calculate statistics if the rasters to compare are different, we use the DirectPosition object to locate the
	 * cells. The "discrete" variable will be "true" and SplitMergedTypo method will be used to calculate statistics
	 * 
	 */

	public static File rootFile;
	public static File statFile;
	public static File discreteFile;
	public static File morphoFile;
	public static boolean discrete = false;
	public static boolean stabilite = false;
	public static boolean sensibility = false;
	public static boolean cutBorder = false;
	public static String echelle;
	public static boolean firstline = true;
	public static boolean compareAHP = false;
	public static boolean compareBaSt = false;
	public static File evalTotale;

	/**
	 * Select a list of file with the argument "with" in its name from the rootFile
	 *
	 * @param with
	 *            : String that is contained into the selection's name
	 * @param in:
	 *            optional , if the search is needed to be in a specific list
	 * @return an arrayList of files
	 * @author Maxime Colomb
	 * @throws Exception
	 * 
	 */
	public static ArrayList<File> selectWith(String with, ArrayList<File> in) throws IOException {
		ArrayList<File> listFile = new ArrayList<File>();
		if (in == null) {
			for (File fil : rootFile.listFiles()) {
				Pattern ech = Pattern.compile("eval_anal-");
				String[] list = ech.split(fil.toString());
				if (fil.toString().contains(with) && list.length > 1 && list[1].equals(echelle + ".0.tif")) {
					listFile.add(fil);
				}

			}
		} else {
			for (File fil : in) {
				Pattern ech = Pattern.compile("eval_anal-");
				String[] list = ech.split(fil.toString());
				if (fil.toString().contains(with) && list[1].equals(echelle + ".0.tif")) {
					listFile.add(fil);
				}
			}
		}
		return listFile;
	}

	// /**
	// * method which analyse the small replication of a lot of parameters described into the experimental tests about the sensibility of MUP-City All the arguments are taken in
	// the
	// * class variable it creates a tab sheet on the /stats folder from the rootFile and merged rasters
	// *
	// * @author Maxime Colomb
	// * @throws Exception
	// *
	// */
	// public static void replication() throws Exception {
	// for (int n = 3; n <= 7; n++) {
	// String N = new String("N" + n);
	// for (int s = 0; s <= 1; s++) {
	// String strict = "St";// part of the folder's name
	// if (s == 1) {
	// strict = "Ba";
	// }
	// for (int ah = 0; ah <= 2; ah++) {
	// String ahp = "ahpS";// part of the folder's name
	// if (ah == 1) {
	// ahp = "ahpE";
	// } else if (ah == 2) {
	// ahp = "ahpT";
	// }
	// for (int rer = 0; rer <= 1; rer++) {
	// String aggreg = "Moy";
	// if (rer == 1) {
	// aggreg = "Yag";
	// }
	// String eachTest = new String(N + "--" + strict + "--" + ahp + "_" + aggreg);
	// ArrayList<File> listRepliFile = selectWith(eachTest, null);
	// System.out.println("pour le scenario " + eachTest);
	// mergeRasters(listRepliFile, eachTest);
	// }
	// }
	// }
	// }
	// }
	//
	// // test de réplications discrètisé
	// public static void replicationCompareScale() throws Exception {
	//
	// discrete = true;
	// rootFile = new File(rootFile, "tests_param/results/G0");
	//
	// ArrayList<String> echelles = new ArrayList<String>();
	// for (Integer i = 20; i <= 180; i = i * 3) {
	// String nombre = i.toString();
	// echelles.add(nombre);
	// }
	//
	// for (int n = 3; n <= 7; n++) {
	// String N = new String("N" + n);
	// for (int s = 0; s <= 1; s++) {
	// String strict = "St";// part of the folder's name
	// if (s == 1) {
	// strict = "Ba";
	// }
	// for (int ah = 0; ah <= 2; ah++) {
	// String ahp = "ahpS";// part of the folder's name
	// if (ah == 1) {
	// ahp = "ahpE";
	// } else if (ah == 2) {
	// ahp = "ahpT";
	// }
	// String aggreg = "Moy";
	//
	// String eachTest = new String(N + "--" + strict + "--" + ahp + "_" + aggreg);
	// System.out.println("pour le scenario " + eachTest);
	// for (String ech : echelles) {
	// echelle = ech;
	// ArrayList<File> listRepliFile = selectWith(eachTest, null);
	// System.out.println("pour une echelle: " + ech);
	// System.out.println(listRepliFile);
	// mergeRasters(listRepliFile, eachTest);
	// }
	// firstline = false;
	// }
	// }
	// }
	// }
	//
	// /**
	// * method which analyse the stability of a big amount of replication of simulation Directly create a statistic file
	// *
	// * @param echelle:
	// * scale of the file
	// * @param isDiscrete
	// * if the process has to discretise the output cells within a shape file
	// * @return a collection of a scenario name with his analysis array (described in the margeRaster method)
	// * @throws Exception
	// */
	// public static void replicationStab() throws Exception {
	//
	// ArrayList<File> listRepliFiles = selectWith("replication", null);
	// stabilite = true;
	// mergeRasters(listRepliFiles, "stability");
	// }
	//
	// /**
	// * Compares replication by the AHP matrix choice
	// *
	// * @param echelle:
	// * scale of the file
	// * @return a collection of a scenario name with his analysis array (described in the margeRaster method)
	// * @throws Exception
	// */
	//
	// public static void compareAHP() throws Exception {
	// compareAHP = true;
	// ArrayList<File> oneSeed = selectWith("replication_7", null);
	// for (int n = 3; n <= 7; n++) {
	// String N = new String("N" + n);
	// for (int s = 0; s <= 1; s++) {
	// String strict = "St";// part of the folder's name
	// if (s == 1) {
	// strict = "Ba";
	// }
	// for (int agg = 0; agg <= 1; agg++) {
	// String aggreg = "Yag";
	// if (agg == 1) {
	// aggreg = "Moy";
	// }
	// String TestNSt = new String(N + "--" + strict);
	// ArrayList<File> tempList = selectWith(TestNSt, oneSeed);
	// ArrayList<File> oneSeedAhp = selectWith(aggreg, tempList);
	// System.out.println("one seed ahp : " + oneSeedAhp);
	// String nameScenar = new String(TestNSt + "--" + aggreg);
	// mergeRasters(oneSeedAhp, nameScenar);
	//
	// }
	// }
	// }
	// }

	// public static void compareBaSt() throws Exception {
	//
	// ArrayList<String> echelles = new ArrayList<String>();
	//
	// for (Integer i = 20; i <= 180; i = i * 3) {
	// String nombre = i.toString();
	// echelles.add(nombre);
	// }
	// for (String scale : echelles) {
	// echelle = scale;
	// compareBaSt = true;
	// System.out.println("echelle " + echelle);
	// ArrayList<File> oneSeed = selectWith("replication_7", null);
	// System.out.println(oneSeed);
	// for (int n = 3; n <= 7; n++) {
	// String N = new String("N" + n);
	// for (int ah = 0; ah <= 2; ah++) {
	// String ahp = "ahpS";// part of the folder's name
	// if (ah == 1) {
	// ahp = "ahpE";
	// } else if (ah == 2) {
	// ahp = "ahpT";
	// }
	// for (int agg = 0; agg <= 1; agg++) {
	// String aggreg = "Yag";
	// if (agg == 1) {
	// aggreg = "Moy";
	// }
	// String aggregahp = new String(ahp + "_" + aggreg);
	// ArrayList<File> tempList = selectWith(N, oneSeed);
	// System.out.println(tempList);
	// ArrayList<File> oneSeedSt = selectWith(aggregahp, tempList);
	// System.out.println("one seed st : " + oneSeedSt);
	// String nameScenar = new String(N + "--" + aggregahp);
	// mergeRasters(oneSeedSt, nameScenar);
	//
	// }
	// }
	// }
	// }
	// }

	/**
	 * Count how many cells of 20m are included in cells of 180m
	 * 
	 * @author Maxime Colomb
	 * @param cellRepetCentroid:
	 * @param echelle:
	 *            scale of the file
	 * @param in:
	 *            array of file to search in (can be null)
	 * @return an ArrayList of File
	 * @throws Exception
	 * @throws IOException
	 */

	public static void compareInclusionSizeCell(Hashtable<DirectPosition2D, Integer> SvgCellRepet20, Hashtable<DirectPosition2D, Float> SvgCellEval20,
			Hashtable<DirectPosition2D, Integer> cellRepetParent, Hashtable<DirectPosition2D, Float> cellEvalParent, String namescenar, int echelle) throws IOException {

		// nb of cells
		int cellIn = 0;
		int cellOut = 0;
		int cellTotal = SvgCellRepet20.size();

		ArrayList<Float> cellInEval = new ArrayList<Float>();
		ArrayList<Float> cellOutEval = new ArrayList<Float>();

		Hashtable<DirectPosition2D, Float> doubleSvgCellEval20 = (Hashtable<DirectPosition2D, Float>) SvgCellEval20.clone();

		for (DirectPosition2D coord : cellRepetParent.keySet()) {
			double empXmin = coord.getX() - echelle / 2;
			double empXmax = coord.getX() + echelle / 2;
			double empYmin = coord.getY() - echelle / 2;
			double empYmax = coord.getY() + echelle / 2;
			for (DirectPosition2D coord20 : SvgCellRepet20.keySet()) {
				if (coord20.getX() > empXmin && coord20.getX() < empXmax && coord20.getY() > empYmin && coord20.getY() < empYmax) {
					cellIn = cellIn + 1;
					cellInEval.add(SvgCellEval20.get(coord20));
					doubleSvgCellEval20.remove(coord20);
				}
			}
		}

		for (DirectPosition2D cell : doubleSvgCellEval20.keySet()) {
			cellOut = cellOut + 1;
			cellOutEval.add(doubleSvgCellEval20.get(cell));
		}

		int cellOutTheorie = cellTotal - cellIn;

		// eval moyenne des cellules contenues
		float sumInVal = 0;
		for (float val : cellInEval) {
			sumInVal = sumInVal + val;
		}
		float averageValIn = sumInVal / cellInEval.size();

		// eval moyenne des cellules non contenues
		float sumOutVal = 0;
		for (float val : cellInEval) {
			sumOutVal = sumOutVal + val;
		}
		float averageValOut = sumOutVal / cellInEval.size();

		// eval moyenne des cellules totales
		float sumCellEval = 0;
		for (float val : SvgCellEval20.values()) {
			sumCellEval = sumCellEval + val;
		}
		float averageValTot = sumCellEval / SvgCellEval20.size();

		double[] resultStats = new double[6];
		String[] firstLine = new String[6];

		firstLine[0] = "Nombre totale de cellules";
		firstLine[1] = "Évaluation moyenne de toutes les cellules";
		firstLine[2] = "Cellules de 20m non inclues dans les cellules de " + echelle + "m";
		firstLine[3] = "Évaluation moyenne des cellules de 20m non inclues dans les cellules de " + echelle + "m";
		firstLine[4] = "Cellules de 20m inclues dans les cellules de " + echelle + "m";
		firstLine[5] = "Évaluation moyenne des cellules de 20m incluses dans les cellules de " + echelle + "m";

		resultStats[0] = cellTotal;
		resultStats[1] = averageValTot;
		resultStats[2] = cellOut;
		resultStats[3] = averageValOut;
		resultStats[4] = cellIn;
		resultStats[5] = averageValIn;

		StatTab result = new StatTab("compare_20to" + echelle, (namescenar + "--compare-20/" + echelle), resultStats, firstLine);

		result.toCsv(statFile, true);

	}

	// /**
	// * explo method to analyse an output when the data have been moved a little in order to impact the sensibility of the simulation to the grid.
	// *
	// * @param echelle
	// * : scale in which the analyse should take place
	// * @param nbTest
	// * : Number of different simulation runned for the sensibility test
	// * @return void, but creates a statistic file
	// * @throws Exception
	// */
	// public static void gridSensibility() throws Exception {
	// ArrayList<File> listRepliFile = new ArrayList<File>();
	//
	// cutBorder = true;
	//
	// for (int i = 0; i <= 8; i++) {
	// File file = new File(rootFile + "/data" + i + "/replication_numero-42-eval_anal-" + echelle + ".0.tif");
	// listRepliFile.add(file);
	// }
	// mergeRasters(listRepliFile, "gridSensibility");
	// discrete = true;
	// for (int i = 0; i <= 8; i++) {
	// ArrayList<File> singleCity = new ArrayList<File>();
	// File file = new File(rootFile + "/data" + i + "/replication_numero-42-eval_anal-" + echelle + ".0.tif");
	// singleCity.add(file);
	// mergeRasters(singleCity, "cityGen" + i);
	// listRepliFile = new ArrayList<File>();
	// }
	// }
	//
	// /**
	// * method to analyse an output when the grid have had a move
	// *
	// * @param echelle
	// * : scale in which the analyse should take place
	// * @param nbTest
	// * : Number of different simulation runned for the sensibility test
	// * @return void, but creates a statistic file
	// * @throws Exception
	// */
	// public static void gridChange() throws Exception {
	//
	// ArrayList<File> listRepliGen = new ArrayList<File>();
	// for (int i = 0; i <= 8; i++) {
	// ArrayList<File> listEachCity = new ArrayList<File>();
	// File file = new File(rootFile + "/G" + i + "/replication_numero-42-eval_anal-" + echelle + ".0.tif");
	// listEachCity.add(file);
	// mergeRasters(listEachCity, "cityGen" + i);
	// }
	// // mergeRasters(listRepliGen, "gridCompare");
	//
	// }

	public static SimpleFeatureCollection createMupOutput(GridCoverage2D coverage, int cellSize)
			throws IOException, NoSuchAuthorityCodeException, FactoryException, ParseException {

		CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:2154");
		ReferencedEnvelope gridBounds = new ReferencedEnvelope(coverage.getEnvelope2D().getMinX(), coverage.getEnvelope2D().getMaxX(), coverage.getEnvelope2D().getMinY(),
				coverage.getEnvelope2D().getMaxY(), sourceCRS);

		WKTReader wktReader = new WKTReader();
		SimpleFeatureTypeBuilder sfTypeBuilder = new SimpleFeatureTypeBuilder();

		sfTypeBuilder.setName("testType");
		sfTypeBuilder.setCRS(sourceCRS);
		sfTypeBuilder.add("the_geom", Polygon.class);
		sfTypeBuilder.setDefaultGeometry("the_geom");
		sfTypeBuilder.add("eval", Float.class);

		SimpleFeatureType featureType = sfTypeBuilder.buildFeatureType();
		SimpleFeatureBuilder sfBuilder = new SimpleFeatureBuilder(featureType);

		DefaultFeatureCollection victory = new DefaultFeatureCollection();

		SimpleFeatureSource grid = Grids.createSquareGrid(gridBounds, cellSize);

		int i = 0;
		for (Object object : grid.getFeatures().toArray()) {
			SimpleFeature feat = (SimpleFeature) object;
			DirectPosition2D coord = new DirectPosition2D((feat.getBounds().getMaxX() - feat.getBounds().getHeight() / 2),
					(feat.getBounds().getMaxY() - feat.getBounds().getHeight() / 2));
			float[] yo = (float[]) coverage.evaluate(coord);
			if (yo[0] > 0) {
				i = i + 1;
				Object[] attr = { yo[0] };
				sfBuilder.add(wktReader.read(feat.getDefaultGeometry().toString()));
				SimpleFeature feature = sfBuilder.buildFeature("id" + i, attr);
				victory.add(feature);
			}
		}
		exportSFC(victory.collection(), new File("home/mcolomb/outMupEx.tif"));
		return victory.collection();
	}

	public static File exportSFC(SimpleFeatureCollection toExport, File fileName) throws IOException {
		return exportSFC(toExport, fileName, toExport.getSchema());
	}

	public static File exportSFC(SimpleFeatureCollection toExport, File fileName, SimpleFeatureType ft) throws IOException {

		ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

		Map<String, Serializable> params = new HashMap<>();
		params.put("url", fileName.toURI().toURL());
		params.put("create spatial index", Boolean.TRUE);

		ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
		newDataStore.createSchema(ft);

		Transaction transaction = new DefaultTransaction("create");

		String typeName = newDataStore.getTypeNames()[0];
		SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);

		SimpleFeatureType SHAPE_TYPE = featureSource.getSchema();
		System.out.println("SHAPE:" + SHAPE_TYPE);

		if (featureSource instanceof SimpleFeatureStore) {
			SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
			featureStore.setTransaction(transaction);
			try {
				featureStore.addFeatures(toExport);
				transaction.commit();
			} catch (Exception problem) {
				problem.printStackTrace();
				transaction.rollback();
			} finally {
				transaction.close();
			}
		} else {
			System.out.println(typeName + " does not support read/write access");
			System.exit(1);
		}
		return fileName;
	}

	public static File compareDiffSizedCell(List<File> listRepliFile, String nameScenar, File discreteFile, File morphoFile, int echelle) throws Exception {

		// with the other parameters

		mergeRasters(listRepliFile);
		File rasterFile = new File(rootFile + "/Raster");

		// with the topological spaces
		// SimpleFeatureCollection city = (new ShapefileDataStore(discreteFile.toURI().toURL())).getFeatureSource().getFeatures();
		// SimpleFeatureCollection morpho = (new ShapefileDataStore(discreteFile.toURI().toURL())).getFeatureSource().getFeatures();
		//
		// for (File f : listRepliFile) {
		// Pattern tiret = Pattern.compile("-");
		// String[] decompNameProj = tiret.split(f.getName());
		// System.out.println(decompNameProj);
		//
		// int sizeCell = 0;
		// Hashtable<String, Hashtable<DirectPosition2D, Float>> couple = new Hashtable<String, Hashtable<DirectPosition2D, Float>>();
		// Hashtable<DirectPosition2D, Float> cellEval = new Hashtable<DirectPosition2D, Float>();
		// Hashtable<DirectPosition2D, Float> cellRepet = new Hashtable<DirectPosition2D, Float>();
		//
		// ParameterValue<OverviewPolicy> policy = AbstractGridFormat.OVERVIEW_POLICY.createValue();
		// policy.setValue(OverviewPolicy.IGNORE);
		// ParameterValue<String> gridsize = AbstractGridFormat.SUGGESTED_TILE_SIZE.createValue();
		// ParameterValue<Boolean> useJaiRead = AbstractGridFormat.USE_JAI_IMAGEREAD.createValue();
		// useJaiRead.setValue(false);
		// GeneralParameterValue[] params = new GeneralParameterValue[] { policy, gridsize, useJaiRead };
		// GridCoverage2DReader reader = new GeoTiffReader(f);
		// GridCoverage2D coverage = reader.read(params);
		//
		// SimpleFeatureSource out = createMupOutput(coverage, new File(f.getParentFile(), "tempFile.shp"), sizeCell);
		//
		// }

		return null;
	}

	// /**
	// * mergeRaster Merge the given Array of Files regarding to a grid. Return a list with different objects
	// *
	// * @param listRepliFile
	// * : ArrayList of File pointing to the raster layer to merge
	// * @param nameScenar
	// * : the given name for scenarios
	// * @return List of object defined as : list[0] = Hashtable<GridCoordinates2D, Integer> Replication of the cells list[1] = Hashtable<GridCoordinates2D, Float> Meaned
	// evaluation
	// * of the cells list[2] = DescriptiveStatistic historical statistic file list[3] = tab of the maximum different cells of the simulation
	// * @throws Exception
	// */
	// public static RasterMergeResult mergeRastersSimple(List<File> listRepliFile) throws Exception {
	//
	// // variables to create statistics
	//
	// DescriptiveStatistics statNb = new DescriptiveStatistics();
	// Hashtable<GridCoordinates2D, Integer> cellRepet = new Hashtable<GridCoordinates2D, Integer>();
	// Hashtable<GridCoordinates2D, ArrayList<Float>> cellEval = new Hashtable<GridCoordinates2D, ArrayList<Float>>();
	//
	// int nbDeScenar = 0; // le nombre total de scénarios analysés dans la fonction
	//
	// double[] histo = new double[listRepliFile.size()];
	// int iter = 0;
	//
	// // variables for merged raster
	// // not cool coz i cannot know the number of column and lines of the enveloppe yet and the type need it
	// // change the type to a collection or an arraylist?
	//
	// Envelope2D env = null;
	//
	// // loop on the different cells
	// for (File f : listRepliFile) {
	// ParameterValue<OverviewPolicy> policy = AbstractGridFormat.OVERVIEW_POLICY.createValue();
	// policy.setValue(OverviewPolicy.IGNORE);
	// ParameterValue<String> gridsize = AbstractGridFormat.SUGGESTED_TILE_SIZE.createValue();
	// ParameterValue<Boolean> useJaiRead = AbstractGridFormat.USE_JAI_IMAGEREAD.createValue();
	// useJaiRead.setValue(false);
	// GeneralParameterValue[] params = new GeneralParameterValue[] { policy, gridsize, useJaiRead };
	//
	// GridCoverage2DReader reader = new GeoTiffReader(f);
	// GridCoverage2D coverage = reader.read(params);
	// GridEnvelope dimensions = reader.getOriginalGridRange();
	// GridCoordinates maxDimensions = dimensions.getHigh();
	//
	// int w = maxDimensions.getCoordinateValue(0) + 1;
	// int h = maxDimensions.getCoordinateValue(1) + 1;
	// int numBands = reader.getGridCoverageCount();
	// double[] vals = new double[numBands];
	// if (env == null) {
	// env = coverage.getEnvelope2D();
	// }
	//
	// int compteurNombre = 0;
	// nbDeScenar = nbDeScenar + 1;
	//
	// // beginning of the all cells loop
	// int debI = 0;
	// int debJ = 0;
	//
	// // analyse normale de la réplication des cellules
	// for (int i = debI; i < w; i++) {
	// for (int j = debJ; j < h; j++) {
	// GridCoordinates2D coord = new GridCoordinates2D(i, j);
	// if (coverage.evaluate(coord, vals)[0] > 0) {
	// compteurNombre = compteurNombre + 1;
	// if (cellRepet.containsKey(coord)) { // si la cellule a déja été sélectionné lors de réplications
	// cellRepet.put(coord, cellRepet.get(coord) + 1);
	// ArrayList<Float> temp = cellEval.get(coord); // on mets les valeurs d'évaluation dans un tableau
	// temp.add((float) coverage.evaluate(coord, vals)[0]);
	// cellEval.put(coord, temp);
	// } else {// si la cellule est sélectionné pour la première fois
	// cellRepet.put(coord, 1);
	// ArrayList<Float> firstList = new ArrayList<Float>();
	// firstList.add((float) coverage.evaluate(coord, vals)[0]);
	// cellEval.put(coord, firstList);
	// }
	// }
	// }
	// }
	//
	// System.out.println("il y a " + cellRepet.size() + " cellules sur " + compteurNombre + " dans la réplication " + nbDeScenar);
	//
	// // Historique de l'évolution du nombre de cellules sélectionnées dans toutes les simulations
	// statNb.addValue(compteurNombre);
	// histo[iter] = (double) cellRepet.size();
	// iter = iter + 1;
	// }
	//
	// Hashtable<GridCoordinates2D, Float> cellEvalFinal = moyenneEvals(cellEval);
	//
	// RasterMergeResult result = new RasterMergeResult();
	// result.setCellRepet(cellRepet);
	// result.setCellEval(cellEvalFinal);
	// result.setHistoDS(statNb);
	// result.setHisto(histo);
	// return result;
	// }

	/**
	 * mergeRaster Merge the given Array of Files regarding to a grid. Return a list with different objects The cells are taken in a general geographic environment
	 * 
	 * @param listRepliFile
	 *            : ArrayList of File pointing to the raster layer to merge
	 * @param nameScenar
	 *            : the given name for scenarios
	 * @return List of object defined as : list[0] = Hashtable<GridCoordinates2D, Integer> Replication of the cells list[1] = Hashtable<GridCoordinates2D, Float> Meaned evaluation
	 *         of the cells list[2] = DescriptiveStatistic historical statistic file list[3] = tab of the maximum different cells of the simulation
	 * @throws Exception
	 */

	public static RasterMergeResult mergeRasters(File f) throws Exception {
		List<File> singleList = new ArrayList<File>();
		singleList.add(f);
		return mergeRasters(singleList);
	}

	public static GridCoverage2D importRaster(File f) throws IOException {
		// setting of useless parameters
		ParameterValue<OverviewPolicy> policy = AbstractGridFormat.OVERVIEW_POLICY.createValue();
		policy.setValue(OverviewPolicy.IGNORE);
		ParameterValue<String> gridsize = AbstractGridFormat.SUGGESTED_TILE_SIZE.createValue();
		ParameterValue<Boolean> useJaiRead = AbstractGridFormat.USE_JAI_IMAGEREAD.createValue();
		useJaiRead.setValue(false);
		GeneralParameterValue[] params = new GeneralParameterValue[] { policy, gridsize, useJaiRead };

		GridCoverage2DReader reader = new GeoTiffReader(f);
		return reader.read(params);
	}

	public static RasterMergeResult mergeRasters(List<File> listRepliFile) throws Exception {

		// variables to create statistics

		DescriptiveStatistics statNb = new DescriptiveStatistics();
		Hashtable<DirectPosition2D, Integer> cellRepetCentroid = new Hashtable<DirectPosition2D, Integer>();
		Hashtable<DirectPosition2D, ArrayList<Float>> cellEvalCentroid = new Hashtable<DirectPosition2D, ArrayList<Float>>();

		int nbDeScenar = 0; // le nombre total de scénarios analysés dans la fonction

		double[] histo = new double[listRepliFile.size()];
		int iter = 0;

		// variables for merged raster
		// not cool coz i cannot know the number of column and lines of the enveloppe yet and the type need it
		// change the type to a collection or an arraylist?

		Envelope2D env = null;

		// loop on the different cells
		for (File f : listRepliFile) {

			GridCoverage2D coverage = importRaster(f);

			if (env == null) {
				env = coverage.getEnvelope2D();
			}
			int compteurNombre = 0;
			nbDeScenar = nbDeScenar + 1;

			// in case of a move of the grid, we have to delete the border cells because they will be moved

			double Xmin = env.getMinX();
			double Xmax = env.getMaxX();
			double Ymin = env.getMinY();
			double Ymax = env.getMaxY();
			if (cutBorder == true) {
				int ecart = Integer.parseInt(echelle);
				Xmin = Xmin + ecart;
				Xmax = Xmax - ecart;
				Ymin = Ymin + ecart;
				Ymax = Ymax - ecart;
			}

			// developpement pour les cas ou l'on veut une analyse discrétisée ou si les bordures doivent être coupées
			for (double r = Xmin + Double.parseDouble(echelle) / 2; r <= Xmax; r = r + Double.parseDouble(echelle)) {
				// those values are the bounds from project (and upped to correspond to a multiple of 180 to analyse all the cells in the project)
				for (double t = Ymin + Double.parseDouble(echelle) / 2; t <= Ymax; t = t + Double.parseDouble(echelle)) {
					DirectPosition2D coordCentre = new DirectPosition2D(r, t);
					float[] yo = (float[]) coverage.evaluate(coordCentre);
					if (yo[0] > 0) {
						compteurNombre = compteurNombre + 1;
						if (cellRepetCentroid.containsKey(coordCentre)) { // si la cellule a déja été sélectionné lors de réplications
							cellRepetCentroid.put(coordCentre, cellRepetCentroid.get(coordCentre) + 1);
							ArrayList<Float> temp = cellEvalCentroid.get(coordCentre); // on mets les valeurs d'évaluation dans un tableau
							temp.add(yo[0]);
							cellEvalCentroid.put(coordCentre, temp);
						} else { // si la cellule est sélectionné pour la première fois
							cellRepetCentroid.put(coordCentre, 1);
							ArrayList<Float> firstList = new ArrayList<Float>();
							firstList.add(yo[0]);
							cellEvalCentroid.put(coordCentre, firstList);
						}
					}
				}
			}
			System.out.println("il y a " + cellRepetCentroid.size() + " cellules sur " + compteurNombre + " dans la réplication " + nbDeScenar);

			// Historique de l'évolution du nombre de cellules sélectionnées dans toutes les simulations
			statNb.addValue(compteurNombre);
			histo[iter] = (double) cellRepetCentroid.size();
			iter = iter + 1;
		}

		Hashtable<DirectPosition2D, Float> cellEvalFinal = moyenneEvals(cellEvalCentroid);

		RasterMergeResult result = new RasterMergeResult();
		result.setCellRepet(cellRepetCentroid);
		result.setCellEval(cellEvalFinal);
		result.setHistoDS(statNb);
		result.setHisto(histo);
		result.setNbScenar(nbDeScenar);
		return result;
	}

	/**
	 * create the statistics for a discretized study
	 * 
	 * @param nameScenar
	 *            : name given to the study
	 * @param cellRepet
	 *            : Collection of the cell's replication
	 * @param cellEval
	 *            : Collection of the cell's evaluation
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static File createStatsDescriptiveDiscrete(String nameScenar, RasterMergeResult result, File discreteFile) throws IOException {

		// different objects to be analyzed
		Hashtable<String, String[]> tabDifferentObjects = new Hashtable<String, String[]>();
		String[] differentObjects = { "UrbanFabric", "typo" };
		tabDifferentObjects.put(differentObjects[0], differentObjects);
		String[] differentObjects2 = { "Morphology", "morpho" };
		tabDifferentObjects.put(differentObjects2[0], differentObjects2);
		String[] differentObjects3 = { "Cities", "NOM_COM" };
		tabDifferentObjects.put(differentObjects3[0], differentObjects3);
		String[] differentObjects4 = { "MorphoCities", "morphocity" };
		tabDifferentObjects.put(differentObjects4[0], differentObjects4);

		// loop on those different objects
		for (String[] differentObject : tabDifferentObjects.values()) {

			System.out.println("pour le sujet " + differentObject[0] + differentObject[1]);
			SimpleFeatureCollection fabricType = (new ShapefileDataStore(discreteFile.toURI().toURL())).getFeatureSource().getFeatures();
			GeometryFactory factory = new GeometryFactory();

			SimpleFeatureIterator iteratorCity = fabricType.features();

			String[] nameLineFabric = new String[5];
			nameLineFabric[0] = differentObject[0] + " name";
			nameLineFabric[1] = "Total Cells";
			nameLineFabric[2] = "Stable cells";
			nameLineFabric[3] = "Unstable cells";
			nameLineFabric[4] = "average evaluation";

			Hashtable<String, Object[]> cellByFabric = new Hashtable<String, Object[]>();

			// pour toutes les villes
			while (iteratorCity.hasNext()) {
				SimpleFeature city = iteratorCity.next();
				Object[] resultFabric = new Object[5];
				String fabricName = (String) city.getAttribute(differentObject[1]);
				boolean notNull = false;
				// pour toutes les cellules
				for (DirectPosition2D coordCell : result.getCellRepet().keySet()) {
					if (((Geometry) city.getDefaultGeometry()).covers(factory.createPoint(new Coordinate(coordCell.getX(), coordCell.getY())))) {
						notNull = true;
						// si le tissus a déja été implémenté
						if (cellByFabric.contains(fabricName)) {
							System.out.println("fils de pute");
							Object[] resultFabricPast = cellByFabric.get(fabricName);
							resultFabric[1] = (int) resultFabricPast[1] + 1;
							// si la cellule est stable
							if (result.getCellRepet().get(coordCell) == result.getNbScenar()) {
								resultFabric[2] = (int) resultFabricPast[2] + 1;
								resultFabric[3] = (int) resultFabricPast[3];
							}
							// ou non
							else {
								resultFabric[3] = (int) resultFabricPast[3] + 1;
								resultFabric[2] = (int) resultFabricPast[2];
							}
							// moyenne des éval
							resultFabric[4] = ((int) resultFabricPast[1] * (float) resultFabricPast[4] + result.getCellEval().get(coordCell)) / (int) resultFabric[1];
						}

						// si le tissus n'as jamais été implémenté
						else {
							System.out.println("devrais passer par ici");
							resultFabric[0] = fabricName;
							// si la cellule est stable
							if (result.getCellRepet().get(coordCell) == result.getNbScenar()) {
								resultFabric[2] = 1;
							}
							// ou non
							else {
								resultFabric[3] = 1;
							}
							resultFabric[4] = result.getCellEval().get(coordCell);
						}
					}
				}
				// si une cellule a été trouvé dans l'entitée
				if (notNull) {
					cellByFabric.put((String) resultFabric[0], resultFabric);
				}
			}
			generateCsvFile(cellByFabric, statFile, ("cellBy" + differentObject[0] + "For-" + nameScenar));
		}
		return statFile;
	}

	/**
	 * historique du nombre de cellules sélectionné par scénarios
	 * 
	 * @return the .csv file
	 * @throws IOException
	 */
	public static void createStatsEvol(double[] histo, String echelle) throws IOException {

		Hashtable<String, double[]> enForme = new Hashtable<String, double[]>();
		enForme.put("histo", histo);
		generateCsvFileCol(enForme, statFile, "selected_cells_all_simu-"+echelle);
	}

	/**
	 * Toutes les évaluations moyennes des scénarios
	 *
	 */
	public static void createStatEvals(Hashtable<DirectPosition2D, Float> cellEvalFinal, File evalTotal) throws Exception {
		Hashtable<String, double[]> deuForme = new Hashtable<String, double[]>();
		double[] distrib = new double[cellEvalFinal.size()];
		int cpt = 0;

		//
		for (DirectPosition2D it : cellEvalFinal.keySet()) {
			distrib[cpt] = cellEvalFinal.get(it);
			cpt++;
		}
		if (evalTotal.exists()) {

			int cptTot = 0;
			Hashtable<DirectPosition2D, Float> evalTot = (Hashtable<DirectPosition2D, Float>) mergeRasters(evalTotale).getCellEval();
			double[] distribEvalTot = new double[evalTot.size()];
			for (DirectPosition2D it : evalTot.keySet()) {
				distribEvalTot[cptTot] = evalTot.get(it);
				cptTot++;
			}
			deuForme.put("Évaluations générales du projet", distribEvalTot);
		}
		deuForme.put("Évaluations du scénario", distrib);
		generateCsvFileCol(deuForme, statFile, "evaluation_comportment-" + echelle);
	}

	private static Hashtable<DirectPosition2D, Float> moyenneEvals(Hashtable<DirectPosition2D, ArrayList<Float>> cellEval) {
		Hashtable<DirectPosition2D, Float> cellEvalFinal = new Hashtable<DirectPosition2D, Float>();
		for (DirectPosition2D temp : cellEval.keySet()) {
			float somme = 0;
			ArrayList<Float> tablTemp = new ArrayList<Float>();
			tablTemp.addAll(cellEval.get(temp));
			for (float nombre : tablTemp) {
				somme = somme + nombre;
			}
			cellEvalFinal.put(temp, somme / tablTemp.size());
		}
		return cellEvalFinal;
	}

	public static File createStatsDescriptive(String nameScenar, Hashtable<DirectPosition2D, Integer> cellRepet, Hashtable<DirectPosition2D, Float> cellEval,
			DescriptiveStatistics statNb) throws IOException {

		statFile.mkdirs();

		double[] tableauFinal = new double[22];
		String[] premiereCol = new String[22];

		DescriptiveStatistics statInstable = new DescriptiveStatistics();
		DescriptiveStatistics statStable = new DescriptiveStatistics();

		// des statistiques du merge des rasters

		// statistiques du nombre de cellules par scénario
		tableauFinal[0] = Double.parseDouble(echelle);
		premiereCol[0] = "echelle";
		tableauFinal[1] = statNb.getMean();
		premiereCol[1] = "nombre moyen de cellules sélectionnées par simulations";
		tableauFinal[2] = statNb.getStandardDeviation();
		premiereCol[2] = "ecart-type du nombre des cellules sélectionnées par simulations";
		tableauFinal[3] = tableauFinal[2] / tableauFinal[1];
		premiereCol[3] = "coeff de variation du nombre de cellules sélectionnées par simulations";

		// tableaux servant à calculer les coefficients de correlations
		double[] tableauMoy = new double[cellEval.size()];
		double[] tableauRepl = new double[cellRepet.size()];

		// pour les réplis
		int j = 0;
		int i = 0;
		for (int repli : cellRepet.values()) {
			tableauRepl[i] = repli;
			i = i + 1;
		}

		// pour les evals
		for (float eval : cellEval.values()) {
			tableauMoy[j] = eval;
			j = j + 1;
		}
		// calcul de la correlation entre les réplis et les évals
		if (tableauMoy.length > 1 && stabilite == false) { // si il n'y a pas de cellules, la covariance fait planter
			double correlationCoefficient = new Covariance().covariance(tableauMoy, tableauRepl);
			tableauFinal[14] = correlationCoefficient;
			premiereCol[14] = ("coefficient de correlation entre le nombre de réplication et les évaluations des cellules");
		} else {
			double correlationCoefficient = new Covariance().covariance(tableauMoy, tableauRepl);
			tableauFinal[21] = correlationCoefficient;
			premiereCol[21] = ("coefficient de correlation entre le nombre de réplication et les évaluations des cellules");
		}

		premiereCol[15] = ("moyenne evaluation des cellules instables");
		premiereCol[16] = ("ecart type des cellules instables");
		premiereCol[17] = ("coefficient de variation des cellules instables");
		premiereCol[18] = ("moyenne evaluation des cellules stables");
		premiereCol[19] = ("ecart type des cellules stables");
		premiereCol[20] = ("coefficient de variation des cellules stables");

		// distribution
		if (stabilite == false) {
			premiereCol[4] = ("repet 1");
			premiereCol[5] = ("repet 2");
			premiereCol[6] = ("repet 3");
			premiereCol[7] = ("repet 4");
			premiereCol[8] = ("repet 5");
			premiereCol[9] = ("repet 6");
			premiereCol[10] = ("repet 7");
			premiereCol[11] = ("repet 8");
			premiereCol[12] = ("repet 9");
			premiereCol[13] = ("repet 10");

			for (DirectPosition2D key : cellRepet.keySet()) {
				switch (cellRepet.get(key)) {
				case 1:
					tableauFinal[4]++;
					break;
				case 2:
					tableauFinal[5]++;
					break;
				case 3:
					tableauFinal[6]++;
					break;
				case 4:
					tableauFinal[7]++;
					break;
				case 5:
					tableauFinal[8]++;
					break;
				case 6:
					tableauFinal[9]++;
					break;
				case 7:
					tableauFinal[10]++;
					break;
				case 8:
					tableauFinal[11]++;
					break;
				case 9:
					tableauFinal[12]++;
					break;
				case 10:
					tableauFinal[13]++;
					break;
				}
				if (cellRepet.get(key) < 10 && compareBaSt == false) {
					statInstable.addValue(cellEval.get(key));
				}

				if (cellRepet.get(key) == 10 && compareBaSt == false) {
					statStable.addValue(cellEval.get(key));
				}
			}
			tableauFinal[15] = statInstable.getMean();
			tableauFinal[16] = statInstable.getStandardDeviation();
			tableauFinal[17] = tableauFinal[16] / tableauFinal[15];
			tableauFinal[18] = statStable.getMean();
			tableauFinal[19] = statStable.getStandardDeviation();
			tableauFinal[20] = tableauFinal[19] / tableauFinal[18];

		} else if (stabilite == true) {
			premiereCol[4] = ("repet de 0 a 100");
			premiereCol[5] = ("repet de 100 a 200");
			premiereCol[6] = ("de 200 a 300");
			premiereCol[7] = ("de 300 a 400");
			premiereCol[8] = ("de 400 a 500");
			premiereCol[9] = ("de 500 a 600");
			premiereCol[10] = ("de 600 a 700");
			premiereCol[11] = ("de 700 a 800");
			premiereCol[12] = ("de 800 a 900");
			premiereCol[13] = ("de 900 a 999");
			premiereCol[14] = ("1000 repet (allstar)");
			for (DirectPosition2D key : cellRepet.keySet()) {
				if (0 < cellRepet.get(key) && cellRepet.get(key) <= 100) {
					tableauFinal[4]++;
				} else if (100 < cellRepet.get(key) && cellRepet.get(key) <= 200) {
					tableauFinal[5]++;
				} else if (200 < cellRepet.get(key) && cellRepet.get(key) <= 300) {
					tableauFinal[6]++;
				} else if (300 < cellRepet.get(key) && cellRepet.get(key) <= 400) {
					tableauFinal[7]++;
				} else if (400 < cellRepet.get(key) && cellRepet.get(key) <= 500) {
					tableauFinal[8]++;
				} else if (500 < cellRepet.get(key) && cellRepet.get(key) <= 600) {
					tableauFinal[9]++;
				} else if (600 < cellRepet.get(key) && cellRepet.get(key) <= 700) {
					tableauFinal[10]++;
				} else if (700 < cellRepet.get(key) && cellRepet.get(key) <= 800) {
					tableauFinal[11]++;
				} else if (800 < cellRepet.get(key) && cellRepet.get(key) <= 900) {
					tableauFinal[12]++;
				} else if (900 < cellRepet.get(key) && cellRepet.get(key) < 1000) {
					tableauFinal[13]++;
				} else if (cellRepet.get(key) == 1000) {
					tableauFinal[14]++;
				}
				if (cellRepet.get(key) < 1000) {
					statInstable.addValue(cellEval.get(key));
				}
				if (cellRepet.get(key) == 1000) {
					statStable.addValue(cellEval.get(key));
				}
			}

			tableauFinal[15] = statInstable.getMean();
			tableauFinal[16] = statInstable.getStandardDeviation();
			tableauFinal[17] = tableauFinal[16] / tableauFinal[15];
			tableauFinal[18] = statStable.getMean();
			tableauFinal[19] = statStable.getStandardDeviation();
			tableauFinal[20] = tableauFinal[19] / tableauFinal[18];

		}

		// moyenne des évaluations pour les cellules instables

		StatTab tableauStat = new StatTab("descriptive_statistics", nameScenar, tableauFinal, premiereCol);
		tableauStat.toCsv(statFile, firstline);
		firstline = false;

		return statFile;
	}

	public static void generateCsvFileMultTab(Hashtable<String, Hashtable<String, Double>> results, File file, String name) throws IOException {

		File fileName = new File(file + "/" + name + ".csv");
		FileWriter writer = new FileWriter(fileName, true);
		for (String tab : results.keySet()) {
			Hashtable<String, Double> intResult = results.get(tab);
			writer.append("scenario " + tab + "\n");
			for (String nomScenar : intResult.keySet()) {
				writer.append(nomScenar + "," + intResult.get(nomScenar));
				writer.append("\n");
			}
			writer.append("\n");
		}
		writer.close();
	}

	public static void generateCsvFile(Hashtable<String, Object[]> cellRepet, File file, String name) throws IOException {
		Hashtable<String, double[]> result = new Hashtable<String, double[]>();
		for (Object[] ligne : cellRepet.values()) {
			double[] aMettre = new double[ligne.length - 1];
			for (int i = 1; i <= ligne.length; i++) {
				aMettre[i] = (double) ligne[i - 1];
			}
			result.put((String) ligne[0], aMettre);
		}
	}

	public static void generateCsvFile(Hashtable<String, double[]> cellRepet, File file, String name, String[] premiereColonne) throws IOException {
		File fileName = new File(file + "/" + name + ".csv");
		boolean addAfter = true;
		FileWriter writer = new FileWriter(fileName, addAfter);
		if (premiereColonne != null) {
			for (String title : premiereColonne) {
				writer.append(title + ",");
			}
			writer.append("\n");
		}

		for (String nomScenar : cellRepet.keySet()) {
			writer.append(nomScenar + ",");
			for (double val : cellRepet.get(nomScenar)) {
				writer.append(val + ",");
			}
			writer.append("\n");
		}
		writer.close();
	}

	public static void generateCsvFileCol(Hashtable<String, double[]> cellRepet, File file, String name) throws IOException {
		File fileName = new File(file + "/" + name + ".csv");
		FileWriter writer = new FileWriter(fileName, false);

		// selec the longest tab
		int longestTab = 0;
		for (double[] tab : cellRepet.values()) {
			if (tab.length > longestTab) {
				longestTab = tab.length;
			}
		}
		// put the main names
		for (String nomm : cellRepet.keySet()) {
			writer.append(nomm + ",");
		}
		writer.append("\n");

		for (int i = 0; i <= longestTab - 1; i++) {
			for (String nomm : cellRepet.keySet()) {
				try {
					writer.append(Double.toString(cellRepet.get(nomm)[i]) + ",");
				} catch (ArrayIndexOutOfBoundsException a) {
				}
			}
			writer.append("\n");
		}

		writer.close();
	}
}
