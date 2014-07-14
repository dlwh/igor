package igor

import igor.experiment.{Launcher, Experiment}
import igor.config.Configuration

/**
 * @author jda
 */
class DummyExperiment(config: Configuration) extends Experiment(config) {

  def runStages(): Unit = {
    stage[DummyStage1]
    //stage[DummyStage1]
    //stage[PrecomputeFeatures]
    //stage[DummyStage2]
  }

}

object DummyExperiment extends Launcher {
  override def buildExperiment(config: Configuration) = new DummyExperiment(config)
}
