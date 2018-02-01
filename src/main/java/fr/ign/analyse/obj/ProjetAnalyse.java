package fr.ign.analyse.obj;

import java.io.File;

public class ProjetAnalyse {

	private File projFile;
	private String name;
	private String sizeCell;
	private String grid;
	private String seuil;
	private String data;
	
	public ProjetAnalyse(File projFile2,String name2,String sizeCell2,String grid2,String seuil2,String data2) {
		projFile=projFile2;
		name=name2;
		sizeCell=sizeCell2;
		grid=grid2;
		seuil=seuil2;
		data=data2;
	}
	
	public File getProjFile() {
		return projFile;
	}

	public String getName() {
		return name;
	}

	public String getData() {
		return data;
	}
	
	public String getSizeCell() {
		return sizeCell;
	}

	public String getGrid() {
		return grid;
	}

	public String getSeuil() {
		return seuil;
	}

	
	public ProjetAnalyse(File namefile, String sizeCell2, String grid2, String seuil2,String data2){
		projFile = namefile;
		seuil = seuil2;
		grid = grid2;
		sizeCell = sizeCell2;
		data=data2;
	}
	
	
}
