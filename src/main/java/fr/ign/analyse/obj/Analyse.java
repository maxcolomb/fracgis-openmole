package fr.ign.analyse.obj;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Pattern;

public class Analyse {

	// project collections
	List<String> cellMinCollec = new ArrayList<String>();
	List<String> seuilCollec = new ArrayList<String>();
	List<String> gridCollec = new ArrayList<String>();
	List<String> dataCollec = new ArrayList<String>();

	// scenar collections
	List<String> nMaxCollec = new ArrayList<String>();
	List<String> strictCollec = new ArrayList<String>();
	List<String> yagCollec = new ArrayList<String>();
	List<String> ahpCollec = new ArrayList<String>();
	List<String> seedCollec = new ArrayList<String>();

	List<ProjetAnalyse> projetCollec = new ArrayList<ProjetAnalyse>();
	List<ScenarAnalyse> scenarCollec = new ArrayList<ScenarAnalyse>();

	public Analyse(File file, String name) {

		// get the different project parameters
		for (File folderProjet : file.listFiles()) {
			if (folderProjet.isDirectory() && folderProjet.getName().startsWith(name)) {
				String nameProj = folderProjet.getName();
				Pattern tiret = Pattern.compile("-");
				String[] decompNameProj = tiret.split(nameProj);

				// Set les différentes tailles minimales de cellules
				String minCell = decompNameProj[2].replace(".0", "").replace("CM", "");
				if (!cellMinCollec.contains(minCell)) {
					cellMinCollec.add(minCell);
				}
				// Set les différents jeux de données
				String data = decompNameProj[1];
				if (!dataCollec.contains(data)) {
					dataCollec.add(data);
				}
				// Set les differents seuils et grilles
				String seui = null;
				String grille = null;
				if (decompNameProj[3].startsWith("S0")) {
					seui = decompNameProj[3].replace("S", "");
					grille = decompNameProj[4].replace("GP_", "");
				} else {
					seui = decompNameProj[3].replace("S", "") + "-" + decompNameProj[4];
					grille = decompNameProj[5].replace("GP_", "");
				}
				if (!seuilCollec.contains(seui)) {
					seuilCollec.add(seui);
				}
				if (!gridCollec.contains(grille)) {
					gridCollec.add(grille);
				}
				// set scenar
				for (File scenarFile : folderProjet.listFiles()) {
					if (scenarFile.getName().startsWith("N")) {
						Pattern underscore = Pattern.compile("_");
						String[] decompScenar = underscore.split(scenarFile.getName());
						if (!nMaxCollec.contains(decompScenar[0])) {
							nMaxCollec.add(decompScenar[0]);
						}
						if (!strictCollec.contains(decompScenar[1])) {
							strictCollec.add(decompScenar[1]);
						}
						if (!yagCollec.contains(decompScenar[2])) {
							yagCollec.add(decompScenar[2]);
						}
						if (!ahpCollec.contains(decompScenar[3])) {
							ahpCollec.add(decompScenar[3]);
						}
						if (!seedCollec.contains(decompScenar[5])) {
							seedCollec.add(decompScenar[5]);
						}
					}
				}
			}
		}

		// la montagne russe
		for (File fileProjet : file.listFiles()) {
			for (String size : cellMinCollec) {
				for (String grid : gridCollec) {
					for (String seuil : seuilCollec) {
						for (String data : dataCollec) {
							if (fileProjet.getName().contains("CM" + size) && fileProjet.getName().contains("GP_" + grid) && fileProjet.getName().contains("S" + seuil)
									&& fileProjet.getName().contains(data)) {
								ProjetAnalyse proj = new ProjetAnalyse(fileProjet, size, grid, seuil, data);
								projetCollec.add(proj);
								for (File fileScenar : fileProjet.listFiles()) {
									for (String nMax : nMaxCollec) {
										for (String strict : strictCollec) {
											for (String yag : yagCollec) {
												for (String ahp : ahpCollec) {
													for (String seed : seedCollec) {
														if (fileScenar.getName().contains(nMax.toString()) && fileScenar.getName().contains(strict)
																&& fileScenar.getName().contains(yag) && fileScenar.getName().contains(ahp)
																&& fileScenar.getName().contains(seed.toString())) {
															ScenarAnalyse sC = new ScenarAnalyse(fileProjet, size, grid, seuil, data, nMax, ahp, strict, yag, fileScenar, seed);
															scenarCollec.add(sC);
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	public int getNumberProject() {
		return projetCollec.size();
	}

	public Hashtable<String, List<File>> getProjetBySeuil() {
		Hashtable<String, List<File>> listGen = new Hashtable<String, List<File>>();
		for (String seuil : seuilCollec) {
			List<File> particularList = new ArrayList<File>();
			listGen.put(seuil, particularList);
			for (ProjetAnalyse pa : projetCollec) {
				if (pa.getSeuil().equals(seuil)) {
					particularList.add(pa.getProjFile());
				}
			}
		}
		return listGen;
	}

	//faut il renvoyer des objets plutot que des files? 
	public Hashtable<String, List<File>> getProjetByCellmin() {
		Hashtable<String, List<File>> listGen = new Hashtable<String, List<File>>();

		for (String cellMin : cellMinCollec) {
			List<File> particularList = new ArrayList<File>();
			for (ProjetAnalyse pa : projetCollec) {
				if (pa.getSizeCell().equals(cellMin)) {
					particularList.add(pa.getProjFile());
				}
			}
			listGen.put(cellMin, particularList);
		}
		return listGen;
	}
	public Hashtable<String, List<File>> getScenarByCellmin() throws FileNotFoundException {
		Hashtable<String, List<File>> listGen = getProjetByCellmin();
		Hashtable<String, List<File>> listProj = new Hashtable<String, List<File>>();
		for (String list : listGen.keySet()) {
			List<File> toCompare = new ArrayList<File>();
			for (File f : listGen.get(list)) {
				for (ScenarAnalyse sA : scenarCollec) {
					if (sA.getProjFile().equals(f)) {
						toCompare.add(sA.getSimuFile(list));
					}
				}
			}
			listProj.put(list, toCompare);
		}
		return listProj;
	}

	public List<List<ScenarAnalyse>> getScenarPerProject() {
		List<List<ScenarAnalyse>> result = new ArrayList<>();
		for (ProjetAnalyse proj : projetCollec) {
			List<ScenarAnalyse> scenars = new ArrayList<ScenarAnalyse>();
			for (ScenarAnalyse scenar : scenarCollec) {
				if (scenar.getProjFile().equals(proj.getProjFile())) {
					scenars.add(scenar);
				}
			}
			result.add(scenars);
		}
		return result;
	}

	// public List<List<File>> getScenarDiffSeed() {
	// List<List<File>> scenPerProj = getScenarPerProject();
	// List<List<File>> result = new ArrayList<>();
	// for (List<File> scenProj : scenPerProj) {
	// List<File> sortedList = new ArrayList<File>();
	//
	//
	// for (String yag : yagCollec) {
	// for (String n : nMaxCollec){
	// for (String ahp : ahpCollec){
	// for (String str : strictCollec){
	// for (File scenarFile : scenProj) {
	// Pattern tiret = Pattern.compile("_");
	// String[] decomp = tiret.split(scenarFile.getName());
	// if (decomp[0].equals(n) && decomp[1].equals(str) && decomp[2].equals(yag) && decomp[3].equals(ahp)) {
	// for (File ff : scenarFile.listFiles()) {
	// if (ff.getName().endsWith("eval_anal-" + sA.getSizeCell() + ".0.tif")) {
	// sortedList.add(ff);
	// }
	// }
	// }
	// }
	// }
	//
	// }
	//
	// }
	// result.add(sortedList);
	// }
	//
	// return result;
	// }

	public List<List<ScenarAnalyse>> getScenarDiffSeed() throws FileNotFoundException {
		List<List<ScenarAnalyse>> scenPerProj = getScenarPerProject();
		List<List<ScenarAnalyse>> result = new ArrayList<>();
		for (List<ScenarAnalyse> scenProj : scenPerProj) {
		
			for (String yag : yagCollec) {
				for (String n : nMaxCollec) {
					for (String ahp : ahpCollec) {
						for (String str : strictCollec) {
							List<ScenarAnalyse> sortedList = new ArrayList<ScenarAnalyse>();
							for (ScenarAnalyse scen : scenProj) {
								if (scen.getAhp().equals(ahp) && scen.getnMax().equals(n) && scen.isStrict().equals(str) && scen.isYag().equals(yag)) {
									sortedList.add(scen);
								}
							}
							result.add(sortedList);
						}
				
					}

				}

			}
			
		}
		return result;
	}
}
