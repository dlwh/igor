package igor.experiment

import scala.reflect.runtime.universe.TypeTag
import igor.config._
import igor.ReflectionHelper
import java.io._
import scala.collection.mutable
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization
import breeze.stats.distributions.Rand
import scala.reflect.ClassTag
import igor.logging.Logging

//import scala.Serializable

/**
 * @author jda
 */
abstract class Experiment(val config: Configuration) extends Logging {

  val resultCache = mutable.Map[Symbol,Any]()
  val resume = config.getOrElse('resume, promptForResume())
  val env = ReflectionHelper.instantiateWithDefaults[Environment](config.getSubConfiguration('Environment) + ('resume -> resume))
  //val env = ReflectionHelper.instantiateWithDefaults[Environment](config.getSubConfiguration('Environment))
  var currentStageIndex = 0
  var fromStageIndex = getStageIndex()

  implicit val jsonFormats = DefaultFormats

  def runStages()

  def run(): Unit = {
    runStages()
  }

  def stage[A <: Stage]()(implicit tag: TypeTag[A]): Unit = {
    putStageIndex()
    val stageName = ReflectionHelper.getName[A]
    if (currentStageIndex < fromStageIndex) {
      logger.info(s"skipping ${stageName.name}")
    } else {
      //logger.info(s"Running ${stageName.name}")
      task(s"running ${stageName.name}") {
        val currentStage = ReflectionHelper.instantiateWithDefaults[A](
            config.getSubConfiguration(stageName)
        )
        currentStage.experiment = this
        try {
          currentStage.run()
        } catch {
          case e: Throwable =>
            saveState()
            e.printStackTrace()
            sys.exit(1)
        }
      }
    }
    currentStageIndex += 1
  }

  private def saveState(): Unit = {
    putStageIndex()
  }

  private def putStageIndex(): Unit = {
    val f = new File(env.workDir, "STAGE")
    val writer = new BufferedWriter(new FileWriter(f))
    try {
      writer.write(currentStageIndex.toString)
      writer.newLine()
    } finally {
      writer.close()
    }
  }

  private def getStageIndex(): Int = {
    val f = new File(env.workDir, "STAGE")
    if (f.exists) {
      val reader = new BufferedReader(new FileReader(f))
      try {
        reader.readLine().toInt
      } finally {
        reader.close()
      }
    } else {
      0
    }
  }

  // TODO as lists of symbols?
  def withResource[A](path: String)(op: FileInputStream => A): A = {
    assert(env.runDir != null)
    val f = new File(env.runDir, path)
    val stream = new FileInputStream(f)
    try {
      op(stream)
    } finally {
      stream.close()
    }
  }

  def put(name: Symbol, data: AnyRef): Unit = {
    resultCache.put(name, data)
    putDisk(name, data)
  }

  def putDisk(name: Symbol, data: AnyRef): Unit = {
    assert(env.workDir != null)
    val f = new File(env.workDir, name.name)
    if (f.exists())
      logger.warn(s"overwriting $name")
    val writer = new ObjectOutputStream(new FileOutputStream(f))
    try {
      writer.writeObject(data)
    } finally {
      writer.close()
    }
  }

  def get[A](name: Symbol)(implicit mf: Manifest[A]): A = {
    resultCache.getOrElseUpdate(name, getDisk(name)).asInstanceOf[A]
  }

  def getDisk[A](name: Symbol)(implicit mf: Manifest[A]): A = {
    assert(env.workDir != null)
    val f = new File(env.workDir, name.name)
    assert(f.exists())
    val reader = new ObjectInputStream(new FileInputStream(f))
    try {
      reader.readObject.asInstanceOf[A]
    } finally {
      reader.close()
    }
  }

  //def putDisk(name: Symbol, data: AnyRef): Unit = {
  //  assert(env.workDir != null)
  //  val f = new File(env.workDir, name.name)
  //  //assert(!f.exists())
  //  if (f.exists()) {
  //    logger.warn(s"Overwriting $name")
  //  }
  //  val writer = new FileOutputStream(f)

  //  try {
  //    logger.info("begin serialization")
  //    Serialization.write(data, writer)
  //  } finally {
  //    writer.close()
  //  }
  //}

  //def get[A](name: Symbol)(implicit mf: Manifest[A]): A = {
  //  resultCache.getOrElseUpdate(name, {
  //    assert(env.workDir != null)
  //    val f = new File(env.workDir, name.name)
  //    assert(f.exists())
  //    val reader = new FileReader(f)
  //    try {
  //      Serialization.read[A](reader)
  //    } finally {
  //      reader.close()
  //    }
  //  }).asInstanceOf[A]
  //}

  def promptForResume(): Boolean = {
    val target = Environment.latestLinkTarget(config('experimentPath).asInstanceOf[String])
    if (target.isEmpty)
      false
    else {
      println(target)
      print("Resume previous run? ")
      def query() = readLine("[Yn] ")
      val responseMap = Map("" -> true, "y" -> true, "n" -> false)
      val inputs = Stream.continually(query())
      responseMap(inputs.find(responseMap contains _.toLowerCase).get)
    }
  }

}

object Experiment {
  def makeSplit[T](instances: IndexedSeq[T], testFrac: Double): (IndexedSeq[T], IndexedSeq[T]) = {
    val nTest: Int = (testFrac * instances.length).toInt
    val testInstances = Rand.subsetsOfSize(instances, nTest).draw()
    val trainInstances = instances.filter(!testInstances.contains(_))
    (trainInstances, testInstances)
  }
}
