package igor

import igor.experiment.Stage

/**
 * @author jda
 */
class PrecomputeFeatures(val corpusPath: Int) extends Stage {
  override def run(): Unit = {
    logger.info("Hello, world!")
  }
}
