package fracgisplugin

import java.io.File
import fr.ign.task._

trait AnalysisTask {
 /* def apply(inputFolders: Array[File], dataFolder: File, outputFolder: File, name: String): File = {
    fr.ign.task.RasterAnalyseTask.runCompData(inputFolders, dataFolder, outputFolder, name)
  }*/
  def apply(inputFolder: File, dataFolder: File, name: String): File = {
    fr.ign.task.RasterAnalyseTask.runCompData(inputFolder, dataFolder, name)
  }
}

object AnalysisTask extends AnalysisTask
