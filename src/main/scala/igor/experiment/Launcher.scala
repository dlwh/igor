package igor.experiment

import igor.config.{Configuration, CommandLineConfigurationReader}

/**
 * @author jda
 */
trait Launcher {

  def buildExperiment(config: Configuration): Experiment

  def main(args: Array[String]): Unit = {
    val config = Configuration(List(new CommandLineConfigurationReader(args)))
    buildExperiment(config).run()
  }

}
