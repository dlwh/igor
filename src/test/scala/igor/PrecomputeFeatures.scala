package igor

import igor.experiment.{Experiment, Stage}

/**
 * @author jda
 */
class PrecomputeFeatures(val corpusPath: Int) extends Stage {
  override def run(experiment: Experiment): Unit = {
    logger.info("Hello, world!")
  }
}
