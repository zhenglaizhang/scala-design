// phantom type
// no runtime effect
trait Cherries

val x = 12.asInstanceOf[Int with Cherries]

import shapeless.labelled.{KeyTag, FieldType, field}
import shapeless.syntax.singleton._

val someNumber = 12
val numCherries = "numCherries" ->> someNumber
//val numCherries: Int with shapeless.labelled.KeyTag[String("numCherries"),Int] = 12

field[Cherries](12)