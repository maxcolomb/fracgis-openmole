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
import org.geotools.feature.FeatureIterator;
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

import fr.ign.analyse.obj.ScenarAnalyse;

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
	public static boolean stabilite = false;
	public static boolean cutBorder = false;
	public static String echelle;
	public static boolean firstline = true;
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

	/**
	 * Count how many cells of 20m are included in cells of 180m
	 * 
	 * @author Maxime "proud" Colomb
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

	/**
	 * Vectorize a MUP-City output
	 * 
	 * @param coverage
	 *            : raster file of the MUP-City Output
	 * @param cellSize
	 *            : resolution of the cells
	 * @return : A collection of vectorized cells
	 * @throws IOException
	 * @throws NoSuchAuthorityCodeException
	 * @throws FactoryException
	 * @throws ParseException
	 */
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
		// exportSFC(victory.collection(), new File("home/mcolomb/tmp/outMupEx.shp"));
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

	/**
	 * Write how much surface are included into each cities
	 * 
	 * @param listfile
	 * @param nameScenar
	 * @param discreteFile
	 *            : the file to make the discretization. Must contain a "NOM_COM" field
	 * @return
	 * @throws Exception
	 */
	public static void compareDiffSizedCell(List<ScenarAnalyse> listfile, String name, File discreteFile) throws Exception {

		// with the other parameters
		// with the topological spaces
		Hashtable<String, String[]> tabDifferentObjects = loopDiffEntities();

		for (String[] differentObject : tabDifferentObjects.values()) {

			SimpleFeatureCollection discrete = (new ShapefileDataStore(discreteFile.toURI().toURL())).getFeatureSource().getFeatures();
			Hashtable<String, double[]> result = new Hashtable<>();
			String[] firstCol = new String[listfile.size() * 2 + 1];
			firstCol[0] = differentObject[0];
			int scenar = 1;

			// instanciation of the different entities in the result
			for (Object obj : discrete.toArray()) {
				String city = (String) ((SimpleFeature) obj).getAttribute(differentObject[1]);
				result.put(city, new double[0]);
			}
			for (ScenarAnalyse sA : listfile) {

				echelle = sA.getSizeCell();
				firstCol[scenar] = "echelle - " + echelle;
				scenar = scenar + 2;
				int size = Integer.valueOf(echelle);

				SimpleFeatureCollection output = createMupOutput(importRaster(sA.getSimuFile(echelle)), size);
				Hashtable<String, Double> entitySurf = new Hashtable<String, Double>();
				Hashtable<String, Double> entityNumber = new Hashtable<String, Double>();
				for (Object obj : discrete.toArray()) {
					SimpleFeature city = (SimpleFeature) obj;
					String entityName = (String) city.getAttribute(differentObject[1]);
					for (Object objet : output.toArray()) {
						SimpleFeature cell = (SimpleFeature) objet;
						if (((Geometry) city.getDefaultGeometry()).intersects((Geometry) cell.getDefaultGeometry())) {
							double surf = ((Geometry) city.getDefaultGeometry()).intersection((Geometry) cell.getDefaultGeometry()).getArea();
							if (entitySurf.containsKey(entityName)) {
								double temp = entitySurf.get(entityName) + surf;
								entitySurf.remove(entityName);
								entitySurf.put(entityName, temp);

								double tempNb = entityNumber.get(entityName) + 1;
								entityNumber.remove(entityName);
								entityNumber.put(entityName, tempNb);

							} else {
								entitySurf.put(entityName, surf);
								entityNumber.put(entityName, (double) 1);
							}
						}
					}
				}

				// put the values into the right case
				for (String entity : result.keySet()) {
					double[] temp = result.get(entity);
					double[] toPut = new double[temp.length + 2];
					// ça recopie
					if (temp.length > 0) {
						for (int i = 0; i < temp.length; i++) {
							toPut[i] = temp[i];
						}
					}
					if (entitySurf.containsKey(entity)) {
						toPut[temp.length] = entitySurf.get(entity);
						toPut[temp.length + 1] = entityNumber.get(entity);
					} else {
						toPut[temp.length] = 0;
						toPut[temp.length + 1] = 0;
					}
					result.put(entity, toPut);
				}
			}
			generateCsvFile(result, statFile, name + "-surfaceOfCells" + "-" + differentObject[0], firstCol);
		}
	}

	public static RasterMergeResult mergeRasters(List<ScenarAnalyse> listSA, ScenarAnalyse justToOverload) throws Exception {
		List<File> inList = new ArrayList<File>();
		for (ScenarAnalyse sA : listSA) {
			inList.add(sA.getSimuFile());
		}
		return mergeRasters(inList);
	}

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
	 * analyse different geographic objects, all presented in the "discret file"
	 * 
	 * @return String[] with 0 : the type of entity and 1 : the attribute name
	 */
	public static Hashtable<String, String[]> loopDiffEntities() {
		Hashtable<String, String[]> tabDifferentObjects = new Hashtable<String, String[]>();
		String[] differentObjects = { "UrbanFabric", "typo" };
		tabDifferentObjects.put(differentObjects[0], differentObjects);
		String[] differentObjects2 = { "Morphology", "morpholo" };
		tabDifferentObjects.put(differentObjects2[0], differentObjects2);
		String[] differentObjects3 = { "Cities", "NOM_COM" };
		tabDifferentObjects.put(differentObjects3[0], differentObjects3);
		String[] differentObjects4 = { "MorphoCities", "morphocity" };
		tabDifferentObjects.put(differentObjects4[0], differentObjects4);
		return tabDifferentObjects;
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
	public static File createStatsDiscrete(String nameScenar, RasterMergeResult result, File discreteFile) throws IOException {

		Hashtable<String, String[]> tabDifferentObjects = loopDiffEntities();

		// loop on those different objects
		for (String[] differentObject : tabDifferentObjects.values()) {

	
			String[] nameLineFabric = new String[5];
			nameLineFabric[0] = differentObject[0] + " name - echelle " + echelle;
			nameLineFabric[1] = "Total Cells";
			nameLineFabric[2] = "Stable cells";
			nameLineFabric[3] = "Unstable cells";
			nameLineFabric[4] = "average evaluation";

			Hashtable<String, double[]> cellByFabric = new Hashtable<String, double[]>();
			Hashtable<String, List<Double>> evals = new Hashtable<String, List<Double>>();

			System.out.println("pour le sujet " + differentObject[0]);
			SimpleFeatureCollection fabricType = (new ShapefileDataStore(discreteFile.toURI().toURL())).getFeatureSource().getFeatures();
			GeometryFactory factory = new GeometryFactory();

			// pour toutes les villes
			// ce serait plus jolie mais ça me mets une erreure
			// try (SimpleFeatureIterator iteratorCity = fabricType.features()) {
			// while (iteratorCity.hasNext()) {
			// SimpleFeature city = iteratorCity.next();
			for (Object obj : fabricType.toArray()) {
				SimpleFeature city = (SimpleFeature) obj;
				double[] resultFabric = new double[4];

				String fabricName = (String) city.getAttribute(differentObject[1]);
				// pour toutes les cellules
				for (DirectPosition2D coordCell : result.getCellRepet().keySet()) {
					if (((Geometry) city.getDefaultGeometry()).covers(factory.createPoint(new Coordinate(coordCell.getX(), coordCell.getY())))) {
						// si le tissus a déja été implémenté
						if (cellByFabric.containsKey(fabricName)) {
							double[] resultFabricPast = cellByFabric.get(fabricName);
							resultFabric[0] = resultFabricPast[0] + 1;
							// si la cellule est stable
							if (result.getCellRepet().get(coordCell) == result.getNbScenar()) {
								resultFabric[1] = resultFabricPast[1] + 1;
								resultFabric[2] = resultFabricPast[2];
							}
							// ou non
							else {
								resultFabric[2] = resultFabricPast[2] + 1;
								resultFabric[1] = resultFabricPast[1];
							}
							cellByFabric.put(fabricName, resultFabric);
						}
						// si le tissus n'as jamais été implémenté
						else {
							resultFabric[0] = (double) 1;
							// si la cellule est stable
							if (result.getCellRepet().get(coordCell) == result.getNbScenar()) {
								resultFabric[1] = (double) 1;
								resultFabric[2] = (double) 0;
							}
							// ou non
							else {
								resultFabric[2] = (double) 1;
								resultFabric[1] = (double) 0;
							}
							List<Double> salut = new ArrayList<>();
							if (evals.contains(fabricName)) {
								salut = evals.get(fabricName);
							}
							salut.add((double) result.getCellEval().get(coordCell));
							evals.put(fabricName, salut);

							cellByFabric.put(fabricName, resultFabric);
						}
					}
				}
			}
			// put the evals in
			for (String fabricName : evals.keySet()) {
				double sum = 0;
				for (double db : evals.get(fabricName)) {
					sum = sum + db;
				}
				double[] finalle = cellByFabric.get(fabricName);
				finalle[3] = sum / evals.size();
				cellByFabric.put(fabricName, finalle);
			}

			generateCsvFile(cellByFabric, statFile, ("cellBy" + differentObject[0] + "For-" + nameScenar), nameLineFabric);
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
		generateCsvFileCol(enForme, statFile, "selected_cells_all_simu-" + echelle);
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

	public static File createStatsDescriptive(String nameScenar, RasterMergeResult result) throws IOException {

		statFile.mkdirs();

		Hashtable<DirectPosition2D, Integer> cellRepet = result.getCellRepet();
		Hashtable<DirectPosition2D, Float> cellEval = result.getCellEval();
		DescriptiveStatistics statNb = result.getHistoDS();

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
			for (int i = 1; i < ligne.length; i = i + 1) {
				aMettre[i - 1] = (double) ligne[i];
			}
			result.put(String.valueOf(ligne[0]), aMettre);
		}
		generateCsvFileCol(result, file, name);
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
