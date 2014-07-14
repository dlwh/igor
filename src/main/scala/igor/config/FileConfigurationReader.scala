package igor.config
import org.yaml.snakeyaml.Yaml
import java.io.FileReader
import scala.collection.mutable
import collection.JavaConverters._

/**
 * @author jda
 */
class FileConfigurationReader(path: String) extends ConfigurationReader {

  override def iterator: Iterator[(List[Symbol], Any)] = {
    val yaml = new Yaml()
    val loaded = yaml.load(new FileReader(path))
    val pairs = extractFlatKVPairs(loaded)
    //println(pairs)
    pairs
  }.toIterator

  def extractFlatKVPairs(o: Any): Iterable[(List[Symbol],Any)] = {
    assert (o.isInstanceOf[java.util.Map[String,Object]])
    val map = o.asInstanceOf[java.util.Map[String,Object]].asScala
    map.flatMap { case (key, value) =>
      val symKey = Symbol(key)
      if (value.isInstanceOf[java.util.Map[String,Object]]) {
        extractFlatKVPairs(value).map { case (innerFullKey, innerValue) =>
          (symKey :: innerFullKey, innerValue)
        }
      } else {
        Iterable((symKey :: Nil, value))
      }
    }
  }
}
