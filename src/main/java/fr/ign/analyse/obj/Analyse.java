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
		this(file.listFiles(),name);
	}
	public Analyse(File[] files, String name) {
		// get the different project parameters
		for (File folderProjet : files) {
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
		for (File fileProjet : files) {
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
	public List<ProjetAnalyse> getProjectCollec() {
		return projetCollec;
	}
	public List<ScenarAnalyse> getScenarCollec() {
		return scenarCollec;
	}
	
	public List<List<ScenarAnalyse>> getProjetBySeuil() throws FileNotFoundException {
		List<List<ProjetAnalyse>> listGen = new ArrayList<List<ProjetAnalyse>>();
		for (String cellMin : cellMinCollec)
			for (String grid : gridCollec) {
				for (String data : dataCollec) {
					List<ProjetAnalyse> particularList = new ArrayList<ProjetAnalyse>();
					for (ProjetAnalyse pa : projetCollec) {
						if (pa.getSizeCell().equals(cellMin) && pa.getData().equals(data) && pa.getGrid().equals(grid)) {
							particularList.add(pa);
						}
					}
					listGen.add(particularList);
				}
			}
		return getScenars(listGen);
	}

	public List<List<ScenarAnalyse>> getProjetByGrid() throws FileNotFoundException {
		List<List<ProjetAnalyse>> listGen = new ArrayList<List<ProjetAnalyse>>();
		for (String cellMin : cellMinCollec)
			for (String seuil : seuilCollec) {
				for (String data : dataCollec) {
					List<ProjetAnalyse> particularList = new ArrayList<ProjetAnalyse>();
					for (ProjetAnalyse pa : projetCollec) {
						if (pa.getSizeCell().equals(cellMin) && pa.getData().equals(data) && pa.getSeuil().equals(seuil)) {
							particularList.add(pa);
						}
					}
					listGen.add(particularList);
				}
			}
		return getScenars(listGen);
	}
	
	
	public List<List<ProjetAnalyse>> getProjetByCellmin() {
		List<List<ProjetAnalyse>> listGen = new ArrayList<List<ProjetAnalyse>>();
		for (String seuil : seuilCollec) {
			for (String grid : gridCollec) {
				for (String data : dataCollec) {
					List<ProjetAnalyse> particularList = new ArrayList<ProjetAnalyse>();
					for (ProjetAnalyse pa : projetCollec) {
						if (pa.getSeuil().equals(seuil) && pa.getData().equals(data) && pa.getGrid().equals(grid)) {
							particularList.add(pa);
						}
					}
					listGen.add(particularList);
				}
			}
		}
		return listGen;
	}

	public List<List<ProjetAnalyse>> getProjetByData() {
		List<List<ProjetAnalyse>> listGen = new ArrayList<List<ProjetAnalyse>>();
		for (String seuil : seuilCollec) {
			for (String grid : gridCollec) {
				for (String cellMin : cellMinCollec) {
					List<ProjetAnalyse> particularList = new ArrayList<ProjetAnalyse>();
					for (ProjetAnalyse pa : projetCollec) {
						if (pa.getSeuil().equals(seuil) && pa.getSizeCell().equals(cellMin) && pa.getGrid().equals(grid)) {
							particularList.add(pa);
						}
					}
					listGen.add(particularList);
				}
			}
		}
		return listGen;
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

	/**
	 * Méthode qui retourne les ScenarAnalyses classées selon les scénarios, pour tout types de projets
	 * 
	 * @return
	 * @throws FileNotFoundException
	 */
	public List<List<ScenarAnalyse>> getScenars() throws FileNotFoundException {
		List<List<ProjetAnalyse>> uniList = new ArrayList<List<ProjetAnalyse>>();
		uniList.add(projetCollec);
		return getScenars(uniList);
	}

	/**
	 * Méthode qui retourne les ScenarAnalyses classées selon les scénarios, pour une liste de projets donnée
	 * 
	 * @return
	 * @throws FileNotFoundException
	 */
	public List<List<ScenarAnalyse>> getScenars(List<List<ProjetAnalyse>> listProjets) throws FileNotFoundException {

		List<List<ScenarAnalyse>> listScenars = new ArrayList<List<ScenarAnalyse>>();

		for (List<ProjetAnalyse> listProjet : listProjets) {
			Hashtable<String, List<ScenarAnalyse>> collec = new Hashtable<String, List<ScenarAnalyse>>();
			for (ProjetAnalyse pA : listProjet) {
				for (ScenarAnalyse scenar : scenarCollec) {
					if (scenar.getProjFile().equals(pA.getProjFile())) {
						if (collec.containsKey(scenar.getFolderName().getName())) {
							List<ScenarAnalyse> listTemp = collec.get(scenar.getFolderName().getName());
							listTemp.add(scenar);
							collec.put(scenar.getFolderName().getName(), listTemp);
						} else {
							List<ScenarAnalyse> resultTemp = new ArrayList<ScenarAnalyse>();
							resultTemp.add(scenar);
							collec.put(scenar.getFolderName().getName(), resultTemp);
						}
					}
				}
			}
			for (List<ScenarAnalyse> lists : collec.values()) {
				listScenars.add(lists);
			}
		}
		return listScenars;
	}

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
