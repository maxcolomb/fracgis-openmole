package fr.ign.analyse.obj;

import java.io.File;
import java.io.FileNotFoundException;

public class ScenarAnalyse extends ProjetAnalyse {

	public ScenarAnalyse(File projfile, String sizeCell2, String grid2, String seuil2, String data2, String nMax2, String ahp2, String strict2, String yag2, File fileName2,
			String seed2) {
		super(projfile, sizeCell2, grid2, seuil2, data2);

		nMax = nMax2;
		ahp = ahp2;
		strict = strict2;
		yag = yag2;
		seed = seed2;
		folderName = fileName2;

	}

	private String nMax;
	private String ahp;
	private String strict;
	private String yag;
	private File folderName;
	private String seed;

	public String getnMax() {
		return nMax;
	}

	public String getAhp() {
		return ahp;
	}

	public String isStrict() {
		return strict;
	}

	public String isYag() {
		return yag;
	}

	public File getFolderName() {
		return folderName;
	}
	
	public String getScenarName(){
		return folderName.getName();
	}

	public String getSeed() {
		return seed;
	}

	public File getSimuFile() throws FileNotFoundException {
		return getSimuFile("eval-anal", sizeCell);
	}
	public File getSimuFile(String echelle) throws FileNotFoundException {
		return getSimuFile("eval-anal", echelle);
	}

	public File getSimuFile(String type, String echelle) throws FileNotFoundException {
		for (File f : folderName.listFiles()) {
			if (f.getName().endsWith("eval_anal-" + echelle + ".0.tif")) {
				return f;
			}
		}
		throw new FileNotFoundException("Scenar file not found");
	}
}
