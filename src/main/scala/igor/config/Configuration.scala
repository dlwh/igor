package igor.config

import scala.collection.mutable
import igor.logging.Logging

/**
 * @author jda
 */

class Configuration(sourceMap: collection.Map[Symbol,Any], val parent: Option[Configuration])
    extends Map[Symbol,Any] with Logging {

  val backingMap: Map[Symbol,Any] = instantiateBackingMap(sourceMap)

  def get(longKey: List[Symbol]): Option[Any] = longKey match {
    case lastKey :: Nil => get(lastKey) //backingMap.get(lastKey)
    case carKey :: cdrKeys => backingMap(carKey).asInstanceOf[Configuration].get(cdrKeys)
  }

  def get(key: Symbol): Option[Any] = {
    //logger.info("get(%s) called on %s".format(key, this.toString))
    val mine = backingMap.get(key)
    if (mine.nonEmpty)
      mine
    else if (parent.nonEmpty) {
      //logger.info("trying to use parent")
      parent.get.get(key)
    }
      //parent.get.get(key)
    else
      None
  }

  def getSubConfiguration(key: Symbol): Configuration = {
    // TODO not quite right: it shouldn't be possible to access siblings of the requested configuration
    getOrElse(key, this).asInstanceOf[Configuration]
  }

  def iterator: Iterator[(Symbol, Any)] = {
    backingMap.iterator
    /*backingMap.iterator.flatMap {
      case (k, confV: Configuration) =>
        (for {
          (subKey, subVal) <- confV
        } yield (k :: subKey, subVal)).iterator
      case (k, v) => Iterator((k :: Nil, v))
    }*/
  }

  override def toString: String = {
    val inside = for {
      (k, v) <- backingMap
    } yield s"${k.name} -> ${v.toString}"
    s"Configuration{ ${inside.mkString(", ")} }"
  }

  override def -(key: Symbol): Map[Symbol, Any] = ???

  override def +[X >: Any](kv: (Symbol, X)): Map[Symbol, X] = backingMap + kv

  private def instantiateBackingMap(sourceMap: collection.Map[Symbol,Any]): Map[Symbol,Any] = {
    (for {
      (k, v) <- sourceMap
      completeV = v match {
        case mapV: mutable.Map[_,_] => new Configuration(mapV.asInstanceOf[mutable.Map[Symbol,Any]], Some(this))
        case _ => v
      }
    } yield k -> completeV).toMap
  }

}

object Configuration {

  def apply(readers: Iterable[ConfigurationReader]): Configuration = {
    val sourceMap = instantiateSourceMap(readers)
    new Configuration(sourceMap, None)
  }

  private def instantiateSourceMap(readers: Iterable[ConfigurationReader]): mutable.Map[Symbol,Any] = {
    val m = mutable.Map[Symbol,Any]()
    readers.foreach{ reader =>
      reader.foreach{ case (k, v) =>
        putIntoMap(m, k, v)
      }
    }
    m
  }

  private def putIntoMap(m: mutable.Map[Symbol,Any], k: List[Symbol], v: Any): Unit = {
    k match {
      case lastKey :: Nil =>
        m.put(lastKey, v)
      case headKey :: moreKeys =>
        val subMap = m.getOrElseUpdate(headKey, mutable.Map[Symbol,Any]()).asInstanceOf[mutable.Map[Symbol,Any]]
        putIntoMap(subMap, moreKeys, v)
    }
  }

}
