package fr.ign.task;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.geotools.geometry.DirectPosition2D;

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
		runStab(file, new File("/home/mcolomb/workspace/mupcity-openMole/data/"), "emprise");
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

		File rastFile = new File(file, "raster");

		Analyse gridSens = new Analyse(file, name);
		System.out.println(gridSens.getNumberProject());
		Hashtable<String, List<File>> listCellMin = gridSens.getScenarByCellmin();
		// Hashtable<String,ArrayList<File>> listCellMin = gridSens.getProjetByCellmin();

		for (String cellMin : listCellMin.keySet()) {
			RasterAnalyse.echelle = cellMin;
			System.out.println(listCellMin.get(cellMin).size());

			// create analysis of different parameter setting regarding to different size of cells
			RasterAnalyse.compareDiffSizedCell(listCellMin.get(cellMin), name, discreteFile, morphoFile, Integer.parseInt(cellMin));

			// create associated merged rasters
			RasterMerge.merge(listCellMin.get(cellMin), new File(rastFile, name + "-rasterMerged-" + cellMin + ".tif"), Integer.valueOf(cellMin), true);
		}

		// // test the effect of grid - compare les réplication entre les seuils et les différentes grilles
		// for (String scenarName : scenarCollec) {
		// for (String echel : echCollec) {
		// int minCell = Integer.parseInt(echel);
		// RasterAnalyse.echelle = echel;
		// echelle = echel;
		// int[] listEch = { minCell, minCell * 3, minCell * 9 };
		// for (int ech : listEch) {
		// for (String seui : seuilCollec) {
		// ArrayList<File> listRepliFile = new ArrayList<File>();
		// // on va chercher les fichiers correspondants
		// // pour les différents projets
		// for (File folderProjet : file.listFiles()) {
		// if (folderProjet.isDirectory() && folderProjet.getName().startsWith(name)) {
		// String nameProj = folderProjet.getName();
		// Pattern tiret = Pattern.compile("-");
		// String[] decompNameProj = tiret.split(nameProj);
		// // Set les differents seuils
		// String seuiScenar = null;
		// if (decompNameProj[3].startsWith("0")) {
		// seuiScenar = decompNameProj[3];
		// } else {
		// seuiScenar = decompNameProj[3] + decompNameProj[4];
		// }
		// if (seuiScenar.equals(seui)) {
		// // pour les différents scénarios
		// for (File folderScenar : folderProjet.listFiles()) {
		// if (folderScenar.isDirectory()) {
		// // pour les différentes échelles
		// for (File fileScenar : folderScenar.listFiles()) {
		// if (fileScenar.getName().contains(scenarName) && fileScenar.getName().endsWith("eval_anal-" + ech + ".0.tif")) {
		// listRepliFile.add(fileScenar);
		// }
		// }
		// }
		// }
		// }
		// }
		// }
		// System.out.println("---------- NOUVO ------------");
		// System.out.println(listRepliFile);
		// RasterAnalyse.mergeRasters(listRepliFile, "gridSensibility-Seuil_" + seui + "-Echelle_" + echelle + "Scenar_" + scenarName);
		// }
		// }
		// }
		// }
		//
		// // // test the effect of thresholds x grid - compare les réplication entre les seuils et les différentes grilles
		// // for (String scenarName : scenarCollec) {
		// // for (String echel : echCollec) {
		// // int minCell = Integer.parseInt(echel);
		// // RasterAnalyse.echelle = echel;
		// // echelle = echel;
		// // int[] listEch = { minCell, minCell * 3, minCell * 9 };
		// // for (int ech : listEch) {
		// // ArrayList<File> listRepliFile = new ArrayList<File>();
		// // // pour les différents projets
		// // for (File folderProjet : file.listFiles()) {
		// // if (folderProjet.isDirectory()) {
		// // // pour les différents scénarios
		// // for (File folderScenar : folderProjet.listFiles()) {
		// // if (folderScenar.isDirectory()) {
		// // // pour les différentes échelles
		// // for (File fileScenar : folderScenar.listFiles()) {
		// // if (fileScenar.getName().contains(scenarName) && fileScenar.getName().endsWith("eval_anal-" + ech + ".0.tif")) {
		// // listRepliFile.add(fileScenar);
		// // }
		// // }
		// // }
		// // }
		// // }
		// // }
		// //
		// // System.out.println("---------- NOUVO ------------");
		// // System.out.println(listRepliFile);
		// // RasterAnalyse.mergeRasters(listRepliFile, "gridSensibility-Seuil-Echelle_" + echelle+"Scenar_"+scenarName);
		// // }
		// // }
		// // }

		// // test the effect of the minimal sized cell on the grid
		// for (String scenarName : scenarCollec) {
		// for (String seu : seuilCollec) {
		// for (File folderProjet : file.listFiles()) {
		// ArrayList<File> listRepliFile = new ArrayList<File>();
		// if (folderProjet.isDirectory() && folderProjet.getName().startsWith(name)) {
		// Pattern tiret = Pattern.compile("-");
		// String[] decompNameProj = tiret.split(folderProjet.getName());
		// String echel = decompNameProj[2].replace(".0", "");
		// RasterAnalyse.echelle = echel;
		// int minCell = Integer.parseInt(echel);
		// int[] listEch = { minCell, minCell * 3, minCell * 9 };
		// for (int ech : listEch) {
		// if (folderProjet.isDirectory()) {
		// for (File folderScenar : folderProjet.listFiles()) {
		// if (folderScenar.isDirectory()) {
		// for (File fileScenar : folderScenar.listFiles()) {
		// if (fileScenar.getName().contains(scenarName) && fileScenar.getName().endsWith("eval_anal-" + ech + ".0.tif")) {
		// listRepliFile.add(fileScenar);
		// }
		// }
		// }
		// }
		// }
		// }
		//
		// System.out.println("---------- NOUVO ------------");
		// System.out.println(listRepliFile);
		// RasterAnalyse.compareDiffSizedCell(listRepliFile, "gridSensibility-minCell_" + echel);
		// }
		// }
		// }
		// }

		// RasterAnalyse.mergeRasters(listRepliFile, "gridSensibility");
		// RasterAnalyse.discrete = true;
		// int count = 0;
		// for (File f : listRepliFile) {
		// count = count + 1;
		// ArrayList<File> singleCity = new ArrayList<File>();
		// singleCity.add(f);
		// RasterAnalyse.mergeRasters(singleCity, "cityGen" + count);
		// listRepliFile = new ArrayList<File>();
		// }
		// RasterAnalyse.gridChange();
		// }
		return batiFile;
	}

	public static void runStab(File file, File fileDonnee, String name) throws Exception {
		File[] fileAnalyse = DataSetSelec.selectFileAnalyse(fileDonnee);
		File discreteFile = fileAnalyse[0];
		File batiFile = fileAnalyse[1];
		runStab(file, discreteFile, batiFile, name);
	}

	public static void runStab(File file, File discreteFile, File batiFile, String name) throws Exception {

		// folder settings
		File resultFile = new File(file, "result--" + name);

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
				RasterAnalyse.createStatsDescriptive(nameTest, mergedResult.getCellRepet(), mergedResult.getCellEval(), mergedResult.getHistoDS());
				RasterAnalyse.createStatsEvol(mergedResult.getHisto(),echelle);

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
				// RasterAnalyse.createStatsDescriptiveDiscrete(nameTest, mergedResult, discreteFile);

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
	}

	public static File runCompData(File projectFile, File buildFile, String name) throws Exception {
		if (!(name == "autom" || name == "manu")) {
			File fileRef = null;
			RasterAnalyse.rootFile = projectFile;
			String echelle = "20";
			RasterAnalyse.echelle = echelle;
			if (projectFile.getName().startsWith("autom")) {
				fileRef = new File(projectFile.getParentFile().getParentFile(), "autom/autom");
			} else if (projectFile.getName().startsWith("manu")) {
				fileRef = new File(projectFile.getParentFile().getParentFile(), "manu/manu");
			}
			for (File f : projectFile.listFiles()) {
				List<File> fileToTest = new ArrayList<File>();
				if (f.isDirectory() && f.getName().startsWith("N")) {
					String strict;
					String n;
					String moy;
					if (f.getName().contains("St")) {
						strict = "St";
					} else {
						strict = "Ba";
					}
					if (f.getName().contains("N5")) {
						n = "N5";
					} else {
						n = "N6";
					}
					if (f.getName().contains("Moy")) {
						moy = "Moy";
					} else {
						moy = "Yag";
					}
					for (File ff : f.listFiles()) {
						if (ff.getName().endsWith("eval_anal-" + echelle + ".0.tif")) {
							fileToTest.add(ff);
						}
					}
					for (File ff : fileRef.listFiles()) {
						if (ff.getName().contains(n + "_" + strict + "_" + moy)) {
							for (File fff : ff.listFiles()) {
								if (fff.getName().endsWith("eval_anal-" + echelle + ".0.tif")) {
									fileToTest.add(fff);
								}
							}
						}
					}
					RasterAnalyse.mergeRasters(fileToTest);
					RasterMerge.merge(fileToTest, new File(projectFile, "raster/rasterMerged-" + n + "_" + strict + "_" + moy + ".tif"), 20);
				}
			}
		}

		return null;
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
