package igor.experiment

import java.io.FileInputStream
import igor.logging.Logging

/**
 * @author jda
 */
trait Stage extends Logging {
  def run(experiment: Experiment)
}
