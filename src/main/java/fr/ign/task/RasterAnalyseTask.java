package fr.ign.task;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fr.ign.analyse.FractalDimention;
import fr.ign.analyse.RasterAnalyse;
import fr.ign.analyse.RasterMerge;
import fr.ign.exp.DataSetSelec;

import com.google.common.io.Files;

public class RasterAnalyseTask {

	public static String echelle;

	public static void main(String[] args) throws Exception {
		File file = new File("/home/mcolomb/workspace/mupcity-openMole/result/emrpise/emprise-Data1.0");
		runStab(file,new File("/home/mcolomb/workspace/mupcity-openMole/data/"), "stats-dicrete");
	}

	public static File runGridSens(File file, int minCell, File discreteFile, File batiFile, String name) throws Exception {
		RasterAnalyse.rootFile = file;
		int[] listEch = { minCell, minCell * 3, minCell * 9 };
		for (int ech : listEch) {
			RasterAnalyse.echelle = String.valueOf(ech);
			RasterAnalyse.cutBorder = true;
			ArrayList<File> listRepliFile = new ArrayList<File>();
			for (File f : file.listFiles()) {
				for (File ff : f.listFiles()) {
					if (ff.toString().endsWith("eval_anal-" + echelle + ".0.tif")) {
						listRepliFile.add(ff);
					}
				}
			}
			RasterAnalyse.mergeRasters(listRepliFile, "gridSensibility");
			RasterAnalyse.discrete = true;
			int count = 0;
			for (File f : listRepliFile) {
				count = count + 1;
				ArrayList<File> singleCity = new ArrayList<File>();
				singleCity.add(f);
				RasterAnalyse.mergeRasters(singleCity, "cityGen" + count);
				listRepliFile = new ArrayList<File>();
			}
			RasterAnalyse.gridChange();
		}
		return batiFile;
	}

	public static File runStab(File file,File fileDonnee, String name) throws Exception {
		File[] fileAnalyse = DataSetSelec.selectFileAnalyse(fileDonnee);
		File discreteFile = fileAnalyse[0];
		File batiFile = fileAnalyse[1];
		System.out.println(batiFile);
		System.out.println(discreteFile);
		return runStab(file, discreteFile, batiFile, name);
	}

	public static File runStab(File file, File discreteFile, File batiFile, String name) throws Exception {
		File resultFile = new File(file, "result");
		for (File f : file.listFiles()) {
			if (f.getName().startsWith("N")) {
				copyDirectory(f, new File(resultFile, "SortieExemple"));
				break;
			}
		}

		File statFile = null;
		File rastFile = new File(file, "raster");
		rastFile.mkdir();

		for (int ech = 20; ech <= 180; ech = ech * 3) {
			echelle = String.valueOf(ech);
			RasterAnalyse.rootFile = file;
			RasterAnalyse.discrete = false;
			RasterAnalyse.echelle = echelle;
			RasterAnalyse.stabilite = true;

			List<File> fileToTest = new ArrayList<File>();

			for (File f : file.listFiles()) {
				if (f.isDirectory()) {
					for (File ff : f.listFiles()) {
						if (ff.getName().endsWith("eval_anal-" + echelle + ".0.tif")) {
							fileToTest.add(ff);
						}
					}
				}
			}

			statFile = RasterAnalyse.mergeRasters(fileToTest, name);

			System.out.println(statFile);
			// discrete analysis
			RasterAnalyse.discrete = true;
			RasterAnalyse.discreteFile = discreteFile;

			RasterAnalyse.mergeRasters(fileToTest, "stat-discrete");

			RasterMerge.merge(fileToTest, new File(rastFile,name+ "-rasterMerged-" + echelle + ".tif"), 20);

			int resolution = 4;
			FractalDimention.getCorrFracDimfromSimu(batiFile, file, statFile, echelle, resolution);

		}
		copyDirectory(statFile, new File(resultFile, "Stat"));
		copyDirectory(rastFile, new File(resultFile, "Raster"));
		return statFile;
	}

	public static void copyDirectory(File copDir, File destinationDir) throws IOException {
		destinationDir.mkdirs();
		for (File f : copDir.listFiles()) {
			Files.copy(f, new File(destinationDir, f.getName()));
		}
	}

	public static File runCompData(File projectFile, File buildFile, String name) throws Exception {
		if (!(name == "autom" || name == "manu")) {
			File fileRef = null;
			RasterAnalyse.rootFile = projectFile;
			echelle = "20";
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
					RasterAnalyse.mergeRasters(fileToTest, "comp_data");
					RasterMerge.merge(fileToTest, new File(projectFile, "raster/rasterMerged-" + n + "_" + strict + "_" + moy + ".tif"), 20);
				}
			}
		}
		return null;
	}

}
