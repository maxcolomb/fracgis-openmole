package fr.ign.task;

import java.awt.image.Raster;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.geotools.geometry.DirectPosition2D;
import org.hsqldb.persist.RAShadowFile;

import com.google.common.io.Files;

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

		File file = new File("/home/mcolomb/workspace/mupcity-openMole/result/emprise");
		System.out.println(runStab(file, new File("/home/mcolomb/workspace/mupcity-openMole/data/"),"emprise"));

		// File file = new File("/home/mcolomb/workspace/mupcity-openMole/result/compDonnee");
		// runCompData(file, new File("/home/mcolomb/workspace/mupcity-openMole/data/"));
	}

	public static File runGridSens(File file, File fileDonnee, String name) throws Exception {
		File[] fileAnalyse = DataSetSelec.selectFileAnalyse(fileDonnee);
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
			// RasterAnalyse.createStatsDiscrete(name + ("SeuilComparisonDiscrete"), resultMergedSeuil, discreteFile);
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
			// RasterAnalyse.createStatsDiscrete(name + ("GridComparisonDiscrete"), resultMergedSeuil, discreteFile);
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
		File[] fileAnalyse = DataSetSelec.selectFileAnalyse(fileDonnee);
		File discreteFile = fileAnalyse[0];
		File batiFile = fileAnalyse[1];
		return runStab(file, discreteFile, batiFile, name);
	}

	public static File runStab(File file, File discreteFile, File batiFile, String name) throws Exception {

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

			System.out.println("for the project" + nameTest);
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
				// je ne sais pourquoi celle ci ne marche pas .. .. ..
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
				FractalDimention.getCorrFracDimfromSimu(batiFile, file, statFile, echelle, resolution);
			}
		}
		return resultFile;
	}

	public static File runCompData(File file, File fileDonnee) throws Exception {
		File[] fileAnalyse = DataSetSelec.selectFileAnalyse(fileDonnee);
		File discreteFile = fileAnalyse[0];
		File batiFile = fileAnalyse[1];
		String name = file.getName();
		return runCompData(file, discreteFile, batiFile, name);
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
	public static File runCompData(File file, File discreteFile, File batiFile, String name) throws Exception {

		// faire pour les trois échelles

		RasterAnalyse.rootFile = file;

		File resultFile = new File(file, "result--" + name);

		Analyse compDonnee = new Analyse(file, name);
		List<List<ScenarAnalyse>> scenars = compDonnee.getScenars();

		for (List<ScenarAnalyse> scenarToTest : scenars) {

			int minSizeCell = Integer.valueOf(scenarToTest.get(0).getSizeCell());

			// for each scale
			for (int ech = minSizeCell; ech <= minSizeCell * 9; ech = ech * 3) {
				String echelle = String.valueOf(ech);
				RasterAnalyse.echelle = echelle;

				List<File> fileToTest = new ArrayList<>();
				for (ScenarAnalyse sc : scenarToTest) {
					fileToTest.add(sc.getSimuFile(echelle));
				}

				copyExample(fileToTest.get(0), resultFile);
				RasterMergeResult result = RasterAnalyse.mergeRasters(fileToTest);

				// statistics
				File statFile = new File(resultFile, "stat-" + scenarToTest.get(5).getScenarName());
				RasterAnalyse.statFile = statFile;

				RasterAnalyse.createStatsDescriptive(name + "-" + echelle, result);
				RasterMerge.merge(fileToTest, new File(resultFile, scenarToTest.get(5).getScenarName() + "-rasterMerged-" + ech + ".0.tif"), ech);
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

	public static void copyDirectory(File copDir, File destinationDir) throws IOException {
		destinationDir.mkdirs();
		for (File f : copDir.listFiles()) {
			Files.copy(f, new File(destinationDir, f.getName()));
		}
	}
}
