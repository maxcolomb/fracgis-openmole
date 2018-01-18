package fr.ign.exp;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Hashtable;
import java.util.Map;

public class DataSetSelec {

/**
 * This class deals with data issues concerning the run of MUP-City using predefined tasks. 
 * Is is either possible to pre
 * 
 */
	public static Map<String, Map<String, String>> dataHT = new Hashtable<String, Map<String, String>>();
	// public static Map<String, String> dataHTtemp = new Hashtable<String, String>();

	// Data1.0
	public static void main(String[] args) {

	}

	public static void predefSet() {
		Map<String, String> dataHTtemp = new Hashtable<String, String>();
		dataHTtemp.put("build", "batimentPro.shp");
		dataHTtemp.put("road", "routePro.shp");
		dataHTtemp.put("fac", "servicePro.shp");
		dataHTtemp.put("lei", "loisirPro.shp");
		dataHTtemp.put("ptTram", "tramPro.shp");
		dataHTtemp.put("ptTrain", "trainPro.shp");
		dataHTtemp.put("nU", "nonUrbaPro.shp");
		dataHTtemp.put("name", "Data1.0");
		dataHT.put("Data1.0", dataHTtemp);

		// Data1.1
		dataHTtemp = new Hashtable<String, String>();
		dataHTtemp.put("build", "batimentPro.shp");
		dataHTtemp.put("road", "routePro.shp");
		dataHTtemp.put("fac", "servicePro.shp");
		dataHTtemp.put("lei", "loisirPro.shp");
		dataHTtemp.put("ptTram", "tramPro.shp");
		dataHTtemp.put("ptTrain", "trainPro.shp");
		dataHTtemp.put("nU", "nonUrbaPhyPro.shp");
		dataHTtemp.put("name", "Data1.1");
		dataHT.put("Data1.1", dataHTtemp);

		// Data1.2
		dataHTtemp = new Hashtable<String, String>();
		dataHTtemp.put("build", "batimentPro.shp");
		dataHTtemp.put("road", "routePro.shp");
		dataHTtemp.put("fac", "servicePro.shp");
		dataHTtemp.put("lei", "loisirPro.shp");
		dataHTtemp.put("ptTram", "tramPro.shp");
		dataHTtemp.put("ptTrain", "trainPro.shp");
		dataHTtemp.put("name", "Data1.2");
		dataHT.put("Data1.2", dataHTtemp);

		// Data2
		dataHTtemp = new Hashtable<String, String>();
		dataHTtemp.put("build", "batimentSys.shp");
		dataHTtemp.put("road", "routeSys.shp");
		dataHTtemp.put("fac", "serviceSys.shp");
		dataHTtemp.put("lei", "loisirSys.shp");
		dataHTtemp.put("ptTram", "tramSys.shp");
		dataHTtemp.put("ptTrain", "trainSys.shp");
		dataHTtemp.put("nU", "nonUrbaSys.shp");
		dataHTtemp.put("name", "Data2");
		dataHT.put("Data2", dataHTtemp);

		dataHTtemp = new Hashtable<String, String>();
		dataHTtemp.put("build", "batimentSys.shp");
		dataHTtemp.put("road", "routeSys.shp");
		dataHTtemp.put("fac", "serviceSansResto.shp");
		dataHTtemp.put("lei", "loisirSys.shp");
		dataHTtemp.put("ptTram", "tramSys.shp");
		dataHTtemp.put("ptTrain", "trainSys.shp");
		dataHTtemp.put("nU", "nonUrbaSys.shp");
		dataHTtemp.put("name", "serviceSansResto");
		dataHT.put("serviceSansResto", dataHTtemp);

		dataHTtemp = new Hashtable<String, String>();
		dataHTtemp.put("build", "batimentSys.shp");
		dataHTtemp.put("road", "routeSys.shp");
		dataHTtemp.put("fac", "serviceSans.shp");
		dataHTtemp.put("lei", "loisirSys.shp");
		dataHTtemp.put("ptTram", "tramSys.shp");
		dataHTtemp.put("ptTrain", "trainSys.shp");
		dataHTtemp.put("nU", "nonUrbaSys.shp");
		dataHTtemp.put("name", "serviceSans");
		dataHT.put("serviceSans", dataHTtemp);

		// Data2.1
		dataHTtemp = new Hashtable<String, String>();
		dataHTtemp.put("build", "batimentSys.shp");
		dataHTtemp.put("road", "routeSys.shp");
		dataHTtemp.put("fac", "serviceSys.shp");
		dataHTtemp.put("lei", "loisirSys.shp");
		dataHTtemp.put("ptTram", "tramSys.shp");
		dataHTtemp.put("ptTrain", "trainSys.shp");
		dataHTtemp.put("nU", "nonUrbaPhySys.shp");
		dataHTtemp.put("name", "Data2.1");
		dataHT.put("Data2.1", dataHTtemp);

		// Data2.2
		dataHTtemp = new Hashtable<String, String>();
		dataHTtemp.put("build", "batimentSys.shp");
		dataHTtemp.put("road", "routeSys.shp");
		dataHTtemp.put("fac", "serviceSys.shp");
		dataHTtemp.put("lei", "loisirSys.shp");
		dataHTtemp.put("ptTram", "tramSys.shp");
		dataHTtemp.put("ptTrain", "trainSys.shp");
		dataHTtemp.put("name", "Data2.2");
		dataHT.put("Data2.2", dataHTtemp);

		// Data3
		dataHTtemp = new Hashtable<String, String>();
		dataHTtemp.put("build", "batimentPro.shp");
		dataHTtemp.put("road", "routeSys.shp");
		dataHTtemp.put("fac", "servicePro.shp");
		dataHTtemp.put("lei", "loisirPro.shp");
		dataHTtemp.put("ptTram", "tramPro.shp");
		dataHTtemp.put("ptTrain", "trainPro.shp");
		dataHTtemp.put("nU", "nonUrbaPro.shp");
		dataHTtemp.put("name", "manuRoute");
		dataHT.put("Data3", dataHTtemp);

		// Data4
		dataHTtemp = new Hashtable<String, String>();
		dataHTtemp.put("build", "batimentPro.shp");
		dataHTtemp.put("road", "routePro.shp");
		dataHTtemp.put("fac", "servicePro.shp");
		dataHTtemp.put("lei", "loisirSys.shp");
		dataHTtemp.put("ptTram", "tramPro.shp");
		dataHTtemp.put("ptTrain", "trainPro.shp");
		dataHTtemp.put("nU", "nonUrbaPro.shp");
		dataHTtemp.put("name", "manuLoisir");
		dataHT.put("Data3.1", dataHTtemp);

		// Data5
		dataHTtemp = new Hashtable<String, String>();
		dataHTtemp.put("build", "batimentPro.shp");
		dataHTtemp.put("road", "routePro.shp");
		dataHTtemp.put("fac", "serviceSys.shp");
		dataHTtemp.put("lei", "loisirPro.shp");
		dataHTtemp.put("ptTram", "tramPro.shp");
		dataHTtemp.put("ptTrain", "trainPro.shp");
		dataHTtemp.put("nU", "nonUrbaPro.shp");
		dataHTtemp.put("name", "manuService");
		dataHT.put("Data3.2", dataHTtemp);

		dataHTtemp = new Hashtable<String, String>();
		dataHTtemp.put("build", "batimentPro.shp");
		dataHTtemp.put("road", "routePro.shp");
		dataHTtemp.put("fac", "serviceSys.shp");
		dataHTtemp.put("lei", "loisirPro.shp");
		dataHTtemp.put("ptTram", "tramPro.shp");
		dataHTtemp.put("ptTrain", "trainPro.shp");
		dataHTtemp.put("nU", "nonUrbaPro.shp");
		dataHTtemp.put("name", "manuServiceLoisir");
		dataHT.put("Data3.2", dataHTtemp);

		dataHTtemp = new Hashtable<String, String>();
		dataHTtemp.put("build", "batimentPro.shp");
		dataHTtemp.put("road", "routePro.shp");
		dataHTtemp.put("fac", "serviceSys.shp");
		dataHTtemp.put("lei", "loisirPro.shp");
		dataHTtemp.put("ptTram", "tramPro.shp");
		dataHTtemp.put("ptTrain", "trainPro.shp");
		dataHTtemp.put("nU", "nonUrbaPro.shp");
		dataHTtemp.put("name", "manuTrans");
		dataHT.put("Data3.2", dataHTtemp);

		dataHTtemp = new Hashtable<String, String>();
		dataHTtemp.put("build", "batimentPro.shp");
		dataHTtemp.put("road", "routePro.shp");
		dataHTtemp.put("fac", "serviceSys.shp");
		dataHTtemp.put("lei", "loisirPro.shp");
		dataHTtemp.put("ptTram", "tramPro.shp");
		dataHTtemp.put("ptTrain", "trainPro.shp");
		dataHTtemp.put("nU", "nonUrbaPro.shp");
		dataHTtemp.put("name", "manuServiceLoisir");
		dataHT.put("Data3.2", dataHTtemp);

		dataHTtemp = new Hashtable<String, String>();
		dataHTtemp.put("build", "batimentSys.shp");
		dataHTtemp.put("road", "routePro.shp");
		dataHTtemp.put("fac", "servicePro.shp");
		dataHTtemp.put("lei", "loisirPro.shp");
		dataHTtemp.put("ptTram", "tramPro.shp");
		dataHTtemp.put("ptTrain", "trainPro.shp");
		dataHTtemp.put("nU", "nonUrbaPro.shp");
		dataHTtemp.put("name", "manuBati");
		dataHT.put("Data3.2", dataHTtemp);

	}

	public static Map<String, String> dig(File file) {
		Map<String, String> dataHT = new Hashtable<String, String>();

		for (File f : file.listFiles()) {
			String nameFile = f.getName();
			if (nameFile.startsWith("batiment") && nameFile.endsWith(".shp")) {
				dataHT.put("build", nameFile);
			}
			if (nameFile.startsWith("route") && nameFile.endsWith(".shp")) {
				dataHT.put("road", nameFile);
			}
			if (nameFile.startsWith("service") && nameFile.endsWith(".shp")) {
				dataHT.put("fac", nameFile);
			}
			if (nameFile.startsWith("loisir") && nameFile.endsWith(".shp")) {
				dataHT.put("lei", nameFile);
			}
			if (nameFile.startsWith("tram") && nameFile.endsWith(".shp")) {
				dataHT.put("ptTram", nameFile);
			}
			if (nameFile.startsWith("train") && nameFile.endsWith(".shp")) {
				dataHT.put("ptTrain", nameFile);
			}
			if (nameFile.startsWith("nonUrba") && nameFile.endsWith(".shp")) {
				dataHT.put("nU", nameFile);
			}
		}
		dataHT.put("name", file.getName());
		return dataHT;
	}

	public static File[] selectFileAnalyse(File file) {
		File[] fileSelec = new File[2];
		for (File f : file.listFiles()) {
			if (f.getName().startsWith("batiment")) {
				fileSelec[1] = f;
			} else if (f.getName().startsWith("admin")) {
				fileSelec[0] = f;
			}
		}
		return fileSelec;
	}

	public static Map<String, Map<String, String>> getAll() throws Exception {
		return dataHT;
	}

	public static Map<String, String> get(String name) throws Exception {
		if (dataHT.containsKey(name)) {
			return dataHT.get(name);
		}
		throw new FileNotFoundException("no match found");
	}
}
