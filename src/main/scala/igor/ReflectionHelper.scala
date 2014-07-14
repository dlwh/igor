package igor

import scala.reflect.runtime.{universe => ru, currentMirror => cm}
import igor.logging.Logging

/**
 * @author jda
 */
object ReflectionHelper extends Logging {

  def instantiateWithDefaults[A](params: Map[Symbol, Any] = Map.empty)(implicit tag: ru.TypeTag[A]): A = {

    val mirror = ru.runtimeMirror(getClass.getClassLoader)
    val classSymbol = ru.typeOf[A].typeSymbol.asClass
    val classMirror = mirror.reflectClass(classSymbol)
    val module = classSymbol.companionSymbol.asModule
    val moduleMirror = cm.reflect(cm.reflectModule(module).instance)
    val typeSignature = moduleMirror.symbol.typeSignature
    val constructor = ru.typeOf[A].declaration(ru.nme.CONSTRUCTOR).asMethod

    //val ms = typeSignature.members

    val argSymbols = constructor.paramss.flatten
    val args = argSymbols.zipWithIndex map {
      case (p, i) =>
        val uncoercedParam = params.getOrElse(Symbol(p.name.toString), {
          try {
            val methodDef = typeSignature.member(ru.newTermName(s"$$lessinit$$greater$$default$$${i+1}")).asMethod
            moduleMirror.reflectMethod(methodDef)()
          } catch {
            // TODO we should be able to explicitly recognize this case rather than using an exception handler
            case ScalaReflectionException(_) =>
              throw new IllegalArgumentException("No default or explicit override provided " +
                                                 s"for constructor argument ${p.name.toString}")
          }
        })
        coerce(uncoercedParam, p.typeSignature)
    }
    //logger.debug(args(1).getClass.toString)
    classMirror.reflectConstructor(constructor)(args: _*).asInstanceOf[A]
  }

  private def coerce(uncoercedParam: Any, t: ru.Type): Any = {
    if (uncoercedParam.isInstanceOf[String]) {
      val stringParam = uncoercedParam.asInstanceOf[String]
      t.typeSymbol.name.toString match {
        case "Int" => stringParam.toInt
        case "Long" => stringParam.toLong
        case "Double" => stringParam.toDouble
        case "Boolean" => stringParam.toBoolean
        case "String" => stringParam
        case _ => throw new IllegalArgumentException(s"Don't know how to convert type ${t.typeSymbol.name.toString}")
      }
    } else {
      uncoercedParam
    }
  }

  def getName[A](implicit tag: ru.TypeTag[A]): Symbol = {
    Symbol(ru.typeOf[A].typeSymbol.name.toString)
  }

}
