// Scala allows us to have Dynamic Types, right inside of a Statically/Strictly Typed language!

import scala.collection.mutable
import scala.language.dynamics

// Dynamic - marker trait to enable dynamic invocation
class JSON(s: String) extends Dynamic {

  def parse(s: String): Map[String, String] = ???

  // act as a field
  def selectDynamic(name: String): Option[String] = parse(s).get(name)
}

val jsonString =
  """
    |{
    | "name": "wow",
    | "favLangs": ["Scala", "Go", "SML"]
    |}
    |""".stripMargin
val json = new JSON(jsonString)

//val name: Option[String] = json.name

// Compiler rewrites and a special marker trait: scala.Dynamic.
//  - applyDynamic
//  - applyDynamicNamed
//  - selectDynamic
//  - updateDynamic

object OhMy extends Dynamic {
  def applyDynamic(methodName: String)(args: Any*): Unit = {
    println(s"""
         |methodName: $methodName
         |  args: ${args.mkString(",")}
         |""".stripMargin)
  }
}

OhMy.dynamicMethod("with", "some", 124)
OhMy.wow("arg", "afd", 12, false)
OhMy.abd("arg", "afd", 12, false)

object JSON extends Dynamic {
  def applyDynamicNamed(name: String)(args: (String, Any)*): Unit = {
    println(
      s"""Creating a $name, for:\n ${args
        .map {
          case (k, v) => s"\t$k => $v"
        }
        .mkString("\n")} """
    )
  }
}

JSON.node(nickname = "kusto", age = 12, male = true)

object MagicBox extends Dynamic {
  private val box = mutable.Map[String, Any]()

  def updateDynamic(name: String)(value: Any) {
    box(name) = value
  }

  def selectDynamic(name: String) = box(name)
}

MagicBox.banabana = "banabana"
MagicBox.banabana

//MagicBox.unknown
//java.util.NoSuchElementException: key not found: unknown
