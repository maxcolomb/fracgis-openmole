package fr.ign.task;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.geotools.geometry.DirectPosition2D;

import fr.ign.analyse.FractalDimention;
import fr.ign.analyse.RasterAnalyse;
import fr.ign.analyse.RasterMerge;
import fr.ign.analyse.RasterMergeResult;
import fr.ign.analyse.obj.Analyse;
import fr.ign.analyse.obj.ScenarAnalyse;
import fr.ign.exp.DataSetSelec;

public class RasterAnalyseTask {

	// public static String echelle;

	public static void main(String[] args) throws Exception {
		// File file = new File("/home/mcolomb/workspace/mupcity-openMole/result/gridExploProjets2");
		// runGridSens(file, new File("/home/mcolomb/workspace/mupcity-openMole/data/"), "gridExplo");

		// File file = new File("/home/mcolomb/workspace/mupcity-openMole/result/compDonnee");
		// System.out.println(runStab(file, new File("/home/mcolomb/workspace/mupcity-openMole/data/"), "compDonnee"));

		File file = new File("/home/mcolomb/.openmole/RKS1409W205-Ubuntu/webui/projects/results");
		runCompData(file, new File("/home/mcolomb/workspace/mupcity-openMole/data/"), "CompDonnee");
	}

	public static File runGridSens(File file, File fileDonnee, String name) throws Exception {
		File[] fileAnalyse = DataSetSelec.selectFileAnalyse(fileDonnee, file);
		File discreteFile = fileAnalyse[0];
		File batiFile = fileAnalyse[1];
		File morphoFile = fileAnalyse[2];
		return runGridSens(file, discreteFile, batiFile, morphoFile, name);
	}

	/**
	 * Analyse automatique des explorations faites avec le script MupCityGridExplo.oms
	 * 
	 * @param file
	 *            : le dossier ou sont contenus les résultats
	 * @param minCell
	 *            : taille minimale des cellules
	 * @param discreteFile
	 *            : le shapefile contenant les entitées permettant une discrétisation (dans la pluspart des cas, les communes)
	 * @param batiFile
	 *            : le shapefile contenant les batiments
	 * @param name
	 *            : le nom de la simulation (doit être le même que pour le projet)
	 * @return le dossier où sont contenus les fichiers produits
	 * @throws Exception
	 */
	public static File runGridSens(File file, File discreteFile, File batiFile, File morphoFile, String name) throws Exception {

		RasterAnalyse.rootFile = file;
		RasterAnalyse.cutBorder = true;

		File resultFile = new File(file, "result-" + name);
		File rastFile = new File(resultFile, "raster");

		Analyse gridSens = new Analyse(file, name);

		// compare the effect of the minimal sizes of the cells
		List<List<ScenarAnalyse>> listCellMin = gridSens.getScenars(gridSens.getProjetByCellmin());

		for (List<ScenarAnalyse> list : listCellMin) {
			File statFile = new File(resultFile, "stat-" + list.get(0).getProjFile().getName() + "-" + list.get(0).getScenarName());
			statFile.mkdirs();
			RasterAnalyse.statFile = statFile;

			// create analysis of different parameter setting regarding to different size of cells
			RasterAnalyse.compareDiffSizedCell(list, name, discreteFile);
		}

		// compare les réplication entre les seuils

		List<List<ScenarAnalyse>> listsSeuil = gridSens.getProjetBySeuil();

		for (List<ScenarAnalyse> listSeuil : listsSeuil) {
			File statFile = new File(resultFile, "stat-" + listSeuil.get(0).getSeuil() + "-" + listSeuil.get(0).getGrid() + "-" + listSeuil.get(0).getScenarName());
			statFile.mkdirs();
			RasterAnalyse.statFile = statFile;

			RasterMergeResult resultMergedSeuil = RasterAnalyse.mergeRasters(listSeuil, listSeuil.get(0));
			RasterAnalyse.createStatsDescriptive(name + ("SeuilComparison"), resultMergedSeuil);
			// don't know why this one crashes coz of a lock
			RasterAnalyse.createStatsDiscrete(name + ("SeuilComparisonDiscrete"), resultMergedSeuil, discreteFile);
			rastFile.mkdirs();
			RasterMerge.merge(listSeuil,
					new File(rastFile,
							"rasterMerged-SeuilComp" + listSeuil.get(0).getSizeCell() + "-" + listSeuil.get(0).getSeuil() + "-" + listSeuil.get(0).getScenarName() + ".tif"),
					RasterAnalyse.cutBorder);
		}

		// compare les réplication entre les grilles

		System.out.println("------------(((Grid)))------------");
		List<List<ScenarAnalyse>> listsGrid = gridSens.getProjetByGrid();

		for (List<ScenarAnalyse> listGrid : listsGrid) {
			File statFile = new File(resultFile, "stat-" + listGrid.get(0).getSizeCell() + "-" + listGrid.get(0).getSeuil() + "-" + listGrid.get(0).getScenarName());
			statFile.mkdirs();
			RasterAnalyse.statFile = statFile;

			RasterMergeResult resultMergedSeuil = RasterAnalyse.mergeRasters(listGrid, listGrid.get(0));
			RasterAnalyse.createStatsDescriptive(name + ("GridComparison"), resultMergedSeuil);
			RasterAnalyse.createStatsDiscrete(name + ("GridComparisonDiscrete"), resultMergedSeuil, discreteFile);
			rastFile.mkdirs();
			RasterMerge.merge(listGrid,
					new File(rastFile,
							"rasterMerged-SeuilComp" + listGrid.get(0).getSizeCell() + "-" + listGrid.get(0).getSeuil() + "-" + listGrid.get(0).getScenarName() + ".tif"),
					RasterAnalyse.cutBorder);
		}

		// test the effect of grid - compare les réplication entre les seuils et les différentes grilles

		return batiFile;
	}

	public static File runStab(File file, File fileDonnee, String name) throws Exception {

		File discreteFile = getDiscrete(fileDonnee);
		System.out.println("root " + file);
		// folder settings
		File resultFile = new File(file, "result--" + name);
		resultFile.mkdir();
		RasterAnalyse.rootFile = file;
		RasterAnalyse.stabilite = true;

		// Count how much 20m cells are contained into parent cells
		Hashtable<DirectPosition2D, Float> SvgCellEval20 = new Hashtable<DirectPosition2D, Float>();
		Hashtable<DirectPosition2D, Integer> SvgCellRepet20 = new Hashtable<DirectPosition2D, Integer>();

		// toutes les listes des projets à tester
		Analyse anal = new Analyse(file, name);
		System.out.println(anal.getScenarDiffSeed().size() + " scenarios ?");
		for (List<ScenarAnalyse> arL : anal.getScenarDiffSeed()) {
			int minSizeCell = Integer.valueOf(arL.get(0).getSizeCell());
			// convert Scenar to File
			String nameTest = arL.get(0).getProjFile().getName() + "_" + arL.get(0).getnMax() + "_" + arL.get(0).isStrict() + "_" + arL.get(0).isYag() + "_" + arL.get(0).getAhp();

			File eachResultFile = new File(resultFile, nameTest);
			eachResultFile.mkdirs();

			File exampleFolder = copyExample(eachResultFile, arL.get(0).getFolderName());

			File statFile = new File(eachResultFile, "stat");
			RasterAnalyse.statFile = statFile;
			File rastFile = new File(eachResultFile, "raster");
			rastFile.mkdir();

			System.out.println("for the project " + nameTest);
			for (int ech = minSizeCell; ech <= minSizeCell * 9; ech = ech * 3) {

				String echelle = String.valueOf(ech);
				RasterAnalyse.echelle = echelle;
				List<File> fileToTest = new ArrayList<File>();

				// get the set of files to test
				for (ScenarAnalyse sc : arL) {
					fileToTest.add(sc.getSimuFile(echelle));
				}

				// merge the different input rasters
				RasterMergeResult mergedResult = RasterAnalyse.mergeRasters(fileToTest);

				// statistics for the simple task with those objects
				RasterAnalyse.createStatsDescriptive(nameTest, mergedResult);
				RasterAnalyse.createStatsEvol(mergedResult.getHisto(), echelle);

				File evalTotal = new File("");

				// get eval total
				System.out.println(eachResultFile);
				for (File f : eachResultFile.listFiles()) {
					if (f.getName().endsWith("eval-" + echelle + ".0.tif")) {
						evalTotal = f;
					}
				}

				RasterAnalyse.createStatEvals(mergedResult.getCellEval(), evalTotal);

				// discrete statistics
				System.out.println("discrete file found : " + discreteFile);
				RasterAnalyse.createStatsDiscrete(nameTest, mergedResult, discreteFile);

				// create a merged raster
				RasterMerge.merge(fileToTest, new File(rastFile, nameTest + "-rasterMerged-" + echelle + ".tif"), Integer.parseInt(echelle));

				// cells contained
				// reference simulation
				File concernedFile = getOutputExample(exampleFolder, ech);
				if (ech == minSizeCell) {
					SvgCellEval20 = RasterAnalyse.mergeRasters(concernedFile).getCellEval();
					SvgCellRepet20 = RasterAnalyse.mergeRasters(concernedFile).getCellRepet();
				} else if (ech == minSizeCell * 3) {
					Hashtable<DirectPosition2D, Float> cellEval60 = (Hashtable<DirectPosition2D, Float>) RasterAnalyse.mergeRasters(concernedFile).getCellEval();
					Hashtable<DirectPosition2D, Integer> cellRepet60 = (Hashtable<DirectPosition2D, Integer>) RasterAnalyse.mergeRasters(concernedFile).getCellRepet();
					RasterAnalyse.compareInclusionSizeCell(SvgCellRepet20, SvgCellEval20, cellRepet60, cellEval60, nameTest, ech);
				} else if (ech == minSizeCell * 9) {
					Hashtable<DirectPosition2D, Float> cellEval180 = (Hashtable<DirectPosition2D, Float>) RasterAnalyse.mergeRasters(concernedFile).getCellEval();
					Hashtable<DirectPosition2D, Integer> cellRepet180 = (Hashtable<DirectPosition2D, Integer>) RasterAnalyse.mergeRasters(concernedFile).getCellRepet();
					RasterAnalyse.compareInclusionSizeCell(SvgCellRepet20, SvgCellEval20, cellRepet180, cellEval180, nameTest, ech);
				}

				// fractal dimention calculation
				int resolution = 4;
				FractalDimention.getCorrFracDimfromSimu(getBuild(fileDonnee, arL), file, statFile, echelle, resolution);
			}
		}
		return resultFile;
	}

	public static File runStab(File[] files, File fileDonnee, File outDir, String name) throws Exception {

		// folder settings
		File resultFile = new File(outDir, "result--" + name);
		resultFile.mkdir();
		RasterAnalyse.rootFile = outDir;
		RasterAnalyse.stabilite = true;

		File discreteFile = getDiscrete(fileDonnee);

		// Count how much 20m cells are contained into parent cells
		Hashtable<DirectPosition2D, Float> SvgCellEval20 = new Hashtable<DirectPosition2D, Float>();
		Hashtable<DirectPosition2D, Integer> SvgCellRepet20 = new Hashtable<DirectPosition2D, Integer>();

		// toutes les listes des projets à tester
		Analyse anal = new Analyse(files, name);
		for (List<ScenarAnalyse> arL : anal.getScenarDiffSeed()) {
			int minSizeCell = Integer.valueOf(arL.get(0).getSizeCell());
			// convert Scenar to File
			String nameTest = arL.get(0).getProjFile().getName() + "_" + arL.get(0).getnMax() + "_" + arL.get(0).isStrict() + "_" + arL.get(0).isYag() + "_" + arL.get(0).getAhp();

			File eachResultFile = new File(resultFile, nameTest);
			eachResultFile.mkdirs();

			File exampleFolder = copyExample(eachResultFile, arL.get(0).getFolderName());

			File statFile = new File(eachResultFile, "stat");
			RasterAnalyse.statFile = statFile;
			File rastFile = new File(eachResultFile, "raster");
			rastFile.mkdir();

			System.out.println("for the project " + nameTest);
			for (int ech = minSizeCell; ech <= minSizeCell * 9; ech = ech * 3) {

				String echelle = String.valueOf(ech);
				System.out.println("echelle = " + echelle);
				RasterAnalyse.echelle = echelle;
				List<File> fileToTest = new ArrayList<File>();

				// get the set of files to test
				for (ScenarAnalyse sc : arL) {
					fileToTest.add(sc.getSimuFile(echelle));
				}

				// merge the different input rasters
				RasterMergeResult mergedResult = RasterAnalyse.mergeRasters(fileToTest);

				// statistics for the simple task with those objects
				RasterAnalyse.createStatsDescriptive(nameTest, mergedResult);
				RasterAnalyse.createStatsEvol(mergedResult.getHisto(), echelle);

				File evalTotal = new File("");

				// get eval total
				System.out.println(eachResultFile);
				for (File f : eachResultFile.listFiles()) {
					if (f.getName().endsWith("eval-" + echelle + ".0.tif")) {
						evalTotal = f;
					}
				}

				RasterAnalyse.createStatEvals(mergedResult.getCellEval(), evalTotal);

				// discrete statistics
				RasterAnalyse.createStatsDiscrete(nameTest, mergedResult, discreteFile);

				// create a merged raster
				RasterMerge.merge(fileToTest, new File(rastFile, nameTest + "-rasterMerged-" + echelle + ".tif"), Integer.parseInt(echelle));

				// cells contained
				// reference simulation
				File concernedFile = getOutputExample(exampleFolder, ech);
				if (ech == minSizeCell) {
					SvgCellEval20 = RasterAnalyse.mergeRasters(concernedFile).getCellEval();
					SvgCellRepet20 = RasterAnalyse.mergeRasters(concernedFile).getCellRepet();
				} else if (ech == minSizeCell * 3) {
					Hashtable<DirectPosition2D, Float> cellEval60 = (Hashtable<DirectPosition2D, Float>) RasterAnalyse.mergeRasters(concernedFile).getCellEval();
					Hashtable<DirectPosition2D, Integer> cellRepet60 = (Hashtable<DirectPosition2D, Integer>) RasterAnalyse.mergeRasters(concernedFile).getCellRepet();
					RasterAnalyse.compareInclusionSizeCell(SvgCellRepet20, SvgCellEval20, cellRepet60, cellEval60, nameTest, ech);
				} else if (ech == minSizeCell * 9) {
					Hashtable<DirectPosition2D, Float> cellEval180 = (Hashtable<DirectPosition2D, Float>) RasterAnalyse.mergeRasters(concernedFile).getCellEval();
					Hashtable<DirectPosition2D, Integer> cellRepet180 = (Hashtable<DirectPosition2D, Integer>) RasterAnalyse.mergeRasters(concernedFile).getCellRepet();
					RasterAnalyse.compareInclusionSizeCell(SvgCellRepet20, SvgCellEval20, cellRepet180, cellEval180, nameTest, ech);
				}

				// fractal dimention calculation
				int resolution = 4;

				FractalDimention.getCorrFracDimfromSimu(getBuild(fileDonnee, arL), files, statFile, echelle, resolution);
			}
		}
		return resultFile;
	}

	/**
	 * Fonction permettant de comparer les différents sets de données décris dans la partie 2.6.1 de ma thèse
	 * 
	 * @param file
	 * @param buildFile
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public static File runCompData(File file, File fileDonnee, String name) throws Exception {

		// return runCompData(file.listFiles(), fileDonnee, name);
		// }
		//
		// public static File runCompData(File[] file, File fileDonnee, String name) throws Exception {
		//
		// RasterAnalyse.rootFile = file[0].getParentFile();
		//
		// File resultFile = new File(file[0].getParentFile(), "result--" + name);

		RasterAnalyse.rootFile = file;

		File resultFile = new File(file, "result--" + name);

		File discreteFile = getDiscrete(fileDonnee);
		Analyse compDonnee = new Analyse(file, name);

		List<Pair> listPair = new ArrayList<>();
		Pair<String, String> pair = new MutablePair("Manuel", "RouteAutom");
		listPair.add(pair);
		pair = new MutablePair("Manuel", "BatiAutom");
		listPair.add(pair);
		pair = new MutablePair("Manuel", "TransportAutom");
		listPair.add(pair);
		pair = new MutablePair("Manuel", "LoisirAutom");
		listPair.add(pair);
		pair = new MutablePair("Manuel", "ServiceAutom");
		listPair.add(pair);
		pair = new MutablePair("Manuel", "LoisirServiceAutom");
		listPair.add(pair);
		pair = new MutablePair("Autom", "LoisirManu");
		listPair.add(pair);
		pair = new MutablePair("Autom", "ServiceManu");
		listPair.add(pair);
		pair = new MutablePair("Autom", "LoisirServiceManu");
		listPair.add(pair);

		for (Pair<String, String> zePair : listPair) {
			ScenarAnalyse firstSc = null;
			ScenarAnalyse secSc = null;

			for (List<ScenarAnalyse> scenars : compDonnee.getScenars()) {

				for (ScenarAnalyse scenar : scenars) {
					if (scenar.getData().equals((String) zePair.getLeft())) {
						firstSc = scenar;
						System.out.println(scenar.getScenarName() + scenar.getNameProjet());
					} else if (scenar.getData().equals((String) zePair.getRight())) {
						secSc = scenar;
						System.out.println(zePair.getRight());
					}
				}
			}

			int minSizeCell = Integer.valueOf(secSc.getSizeCell());

			// for each scale
			for (int ech = minSizeCell; ech <= minSizeCell * 9; ech = ech * 3) {
				String echelle = String.valueOf(ech);
				RasterAnalyse.echelle = echelle;

				List<File> fileToTest = new ArrayList<>();
				fileToTest.add(firstSc.getSimuFile(echelle));
				fileToTest.add(secSc.getSimuFile(echelle));
				copyExample(fileToTest.get(0), resultFile);
				RasterMergeResult result = RasterAnalyse.mergeRasters(fileToTest);

				String nameComp = firstSc.getData() + "-CompareTo-" + secSc.getData();
				// statistics
				File statFile = new File(resultFile, "stat-" + nameComp);
				RasterAnalyse.statFile = statFile;

				RasterAnalyse.createStatsDescriptive(name + "-" + echelle, result);
				RasterMerge.merge(fileToTest, new File(resultFile, nameComp + "-rasterMerged-" + ech + ".0.tif"), ech);
				RasterAnalyse.createStatsDiscrete(name + "-" + echelle, result, discreteFile);
			}
		}
		return resultFile;
	}

	public static File getEvalTotal(File exampleFile, int ech) throws FileNotFoundException {
		for (File f : exampleFile.listFiles()) {
			if (f.getName().endsWith("eval-" + ech + ".0.tif")) {
				return f;
			}
		}
		throw new FileNotFoundException("Example file not found");
	}

	/**
	 * copy an example of scenar output folder
	 * 
	 * @param resultFile
	 *            : the file where the example will be copied
	 * @param folderToCopy
	 *            : the folder to copy
	 * @return the ccopied file
	 * @throws IOException
	 */
	public static File copyExample(File resultFile, File folderToCopy) throws IOException {
		File exampleFile = new File(resultFile, "SortieExemple");
		if (folderToCopy.getName().startsWith("N")) {
			System.out.println(folderToCopy);
			copyDirectory(folderToCopy, exampleFile);
		}
		return exampleFile;
	}

	/**
	 * select a scaled sample of an eval-anal type output
	 * 
	 * @param exampleFolder
	 *            : where to get the sample
	 * @param ech
	 *            : scale
	 * @return the sample
	 * @throws IOException
	 */
	public static File getOutputExample(File exampleFolder, int ech) throws IOException {
		// copy an example of output
		for (File f : exampleFolder.listFiles()) {
			if (f.getName().endsWith("eval_anal-" + ech + ".0.tif")) {
				return f;
			}
		}
		throw new FileNotFoundException("Example file not found");
	}

	public static File getDiscrete(File fileDonnee) {
		File discreteFile = new File("");
		for (File filesDonnee : fileDonnee.listFiles()) {
			try {
				for (File fileShp : filesDonnee.listFiles()) {
					if (fileShp.getName().equals("discreteFile.shp")) {
						discreteFile = fileShp;
						break;
					}
				}
			} catch (NullPointerException e) { // Si les données sont toutes dans un unique répertoire
				if (filesDonnee.getName().equals("discreteFile.shp")) {
					discreteFile = filesDonnee;
					break;
				}
			}
		}
		return discreteFile;
	}

	public static File getBuild(File fileDonnee, List<ScenarAnalyse> arL) {
		File batiFile = new File("");
		for (File filesDonnee : fileDonnee.listFiles()) {
			if (filesDonnee.toString().equals(arL.get(0).getData())) {
				for (File fileShp : filesDonnee.listFiles()) {
					if (fileShp.toString().startsWith("batiment")) {
						batiFile = fileShp;
					}
				}
			}
		}
		return batiFile;
	}

	public static void copyDirectory(File copDir, File destinationDir) throws IOException {
		destinationDir.mkdirs();
		for (File f : copDir.listFiles()) {
//			Files.copy(f, new File(destinationDir, f.getName()));
		}
	}
}
