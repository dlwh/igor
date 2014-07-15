package igor

import igor.experiment.{Experiment, Stage}

/**
 * @author jda
 */
class DummyStage1(
    //val param: Int = 42,
    val flag1: Int = 100,
    val flag2: Boolean = false)
  extends Stage {

  def run(experiment: Experiment): Unit = {
    logger.info("This is stage 1")
    logger.info(s"flag1 = $flag1")
    logger.info(s"flag2 = $flag2")
    logger.info(s"Writing the value of flag2")
    experiment.put('flag2, flag2: java.lang.Boolean)
    logger.info(s"Reading the value of flag2")
    val f2 = experiment.get[Boolean]('flag2)
    logger.info(s"It is $f2")

    task("foo") {
      logger.info("lala")
      val wobble: Int = 5
      task("bar") {
        logger.info("lolo")
      }
    }
  }

}
