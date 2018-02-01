package fr.ign.analyse;

import java.util.Hashtable;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.geotools.geometry.DirectPosition2D;


public class RasterMergeResult {
	Hashtable<DirectPosition2D, Integer> cellRepet;
	Hashtable<DirectPosition2D, Float> cellEval;
	DescriptiveStatistics histoDS;
	double[] histo;
	double nbScenar;

	public Hashtable<DirectPosition2D, Integer> getCellRepet() {
		return cellRepet;
	}

	public void setCellRepet(Hashtable<DirectPosition2D, Integer> cellRepet) {
		this.cellRepet = cellRepet;
	}

	public Hashtable<DirectPosition2D, Float> getCellEval() {
		return cellEval;
	}

	public void setCellEval(Hashtable<DirectPosition2D, Float> cellEval) {
		this.cellEval = cellEval;
	}

	public DescriptiveStatistics getHistoDS() {
		return histoDS;
	}

	public void setHistoDS(DescriptiveStatistics histoDS) {
		this.histoDS = histoDS;
	}

	public double[] getHisto() {
		return histo;
	}

	public void setHisto(double[] histo) {
		this.histo = histo;
	}

	public double getNbScenar() {
		return nbScenar;
	}
	public void setNbScenar(double nbScenar) {
		this.nbScenar = nbScenar;
	}
}
