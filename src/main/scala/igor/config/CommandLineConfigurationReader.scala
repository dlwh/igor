package igor.config

/**
 * @author jda
 */
class CommandLineConfigurationReader(args: Array[String]) extends ConfigurationReader {

  final val FlagStarter = "--"
  final val ImportStarter = "++"
  final val KeySeparatorRegex = "\\.".r
  final val DefaultValue = true

  val kvPairs = getKVPairs(args)

  def iterator: Iterator[(List[Symbol], Any)] = kvPairs.iterator

  def length: Int = kvPairs.length

  def apply(idx: Int): (List[Symbol], Any) = kvPairs(idx)

  private def getKVPairs(args: Array[String]): Seq[(List[Symbol], Any)] = {
    def formatFlagKey(k: String) = KeySeparatorRegex.split(k.drop(FlagStarter.length)).map(s => Symbol(s)).toList
    def formatImportKey(k: String) = k.drop(ImportStarter.length)
    val segmentedArgs = splitOnBoundary[String](args.toList, (a, b) => b.startsWith(FlagStarter) || b.startsWith(ImportStarter))
    assert(segmentedArgs.forall(l => l.length == 1 || l.length == 2))
    for {
      l <- segmentedArgs
      isImport = l(0).startsWith(ImportStarter)
    } yield {
      if (isImport) {
        val path = formatImportKey(l(0))
        new FileConfigurationReader(path.asInstanceOf[String]).iterator
      } else {
        val finalKey = formatFlagKey(l(0))
        val finalValue =
          if (l.length == 2)
            l(1)
          else
            DefaultValue
        Seq(finalKey -> finalValue)
      }
    }
  }.flatten

  private def splitOnBoundary[A](l: List[A], f: (A, A) => Boolean): List[List[A]] = {
    l match {
      case Nil => Nil
      case car :: Nil => List(car :: Nil)
      case car :: cadr :: cddr =>
        val splitRest = splitOnBoundary[A](cadr :: cddr, f)
        if (f(car, cadr))
          List(car) :: splitRest
        else
          (car :: splitRest.head) :: splitRest.tail
    }
  }

}
