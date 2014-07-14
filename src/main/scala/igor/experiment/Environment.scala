package igor.experiment

import breeze.stats.distributions.Rand
import igor.config._
import java.io._
import java.util.{Calendar, Formatter}
import igor.logging.{Logging, LoggingHelper}

/**
 * @author jda
 */
class Environment(
    val experimentPath: String,
    //val logLevel: String = "debug",
    //val logLayout: String = "XXX",
    val randomSeed: Long = 0,
    val resume: Boolean) extends Logging {

  // LoggingHelper.setLog4JLevel(logLevel)
  // LoggingHelper.setLog4JLayout(logLayout)

  val experimentDir = ensureExperimentDir()
  val workDir = ensureWorkDir()
  val runDir = new File(System.getProperty("user.dir"))
  Rand.generator.setSeed(randomSeed)

  def ensureExperimentDir(): File = {
    val experimentDir = new java.io.File(experimentPath)
    if (!experimentDir.exists)
      experimentDir.mkdir()
    experimentDir
  }

  def ensureWorkDir(): File = {
    if (resume)
      Environment.latestLinkTarget(experimentPath).get
    else {
      val timestamp = String.format("%1$tY-%1$tm-%1$td_%1$tH-%1$tM-%1$tS", Calendar.getInstance())
      //logger.debug(timestamp)
      val workDir = new java.io.File(experimentDir, timestamp)
      assert(!workDir.exists())
      workDir.mkdir()
      val latestLinkPath = new File(experimentPath, Environment.LatestLinkName).getAbsolutePath
      new File(latestLinkPath).delete()
      java.lang.Runtime.getRuntime.exec(Array("ln", "-s", workDir.getCanonicalPath, latestLinkPath))
      workDir
    }
  }
}

object Environment {

  final val LatestLinkName = "latest"

  def latestLinkTarget(experimentPath: String): Option[File] = {
    val latestLink = new File(experimentPath, LatestLinkName)
    if (!latestLink.exists)
      None
    else
      Some(new File(latestLink.getCanonicalPath))
  }
}
