package igor.experiment

import java.io.FileInputStream
import igor.logging.Logging

/**
 * @author jda
 */
trait Stage extends Logging {
  var experiment: Experiment = null
  def run()

  // TODO do these work?
  def withResource[A](path: String)(op: FileInputStream => A) = experiment.withResource[A](path)(op)
  def put(name: Symbol, data: AnyRef): Unit = experiment.put(name, data)
  def putDisk(name: Symbol, data: AnyRef): Unit = experiment.putDisk(name, data)
  def get[A](name: Symbol)(implicit mf: Manifest[A]) = experiment.get[A](name)
  def getDisk[A](name: Symbol)(implicit mf: Manifest[A]) = experiment.getDisk[A](name)
}
