import scala.beans.BooleanBeanProperty
import scala.beans.BeanProperty
import scala.util.matching.Regex
object Email extends ((String, String) => String) {
  def apply(user: String, domain: String) = user + "@" + domain

  def unapply(str: String): Option[(String, String)] = {
    var parts = str split "@"
    if (parts.length == 2)
      Some(parts(0), parts(1))
    else
      None
  }
}

Email("zhenglai", "hotmail")
Email.unapply("zhenglaizhang@hotmail.com") equals Some(
  "zhenglaizhang",
  "hotmail.com"
)
Email.unapply("test") == None

"wow@hotmail.com" match {
  case Email(user, domain) => println(user)
}

object UpperCase {
  def unapply(s: String): Boolean = s.toUpperCase == s
}

"ABC" match {
  case UpperCase() => "upper case"
  case _           => "not all upper case"
}

object ExpandedEmail {
  def unapplySeq(email: String): Option[(String, Seq[String])] = {
    var parts = email split "@"
    if (parts.length == 2) {
      Some(parts(0), parts(1).split("\\.").reverse)
    } else {
      None
    }
  }
}

val s = "wow@meow.hotmail.com"
val ExpandedEmail(name, meow, subdoms @ _*) = s

// val decimalPat = new Regex("""(-)?(\d+)(\.\d*)?""")
val DecimalPat = """(-)?(\d+)(\.\d*)?""".r
val input = "for -1 to 99 by 3"
for (s <- DecimalPat findAllIn input)
  println(s)

DecimalPat findFirstIn input

DecimalPat findPrefixOf input

val DecimalPat(sign, integarPart, decimalPart) = "-1.23"

val DecimalPat(sign1, integerPart1, decimalPart1) = "1.0"

for (DecimalPat(s, i, d) <- DecimalPat findAllIn input)
  println(s"$s  $i  $d")

@deprecated("use newShinyMethod() instead", "v18.01")
def bigMistake() = ???

("ab": @unchecked) match {
  case "a"  => "aaa"
  case "ab" => "boom"
}

@volatile var xx = 12

@SerialVersionUID(123L)
class Wow extends Serializable {
  @BeanProperty
  @transient var i = 12

  @BooleanBeanProperty
  var b = false

  @native
  def beginCountDown() = {}
}

def ==(thiz: AnyRef, that: AnyRef): Boolean =
  if (null eq thiz) { null eq that }
  else { thiz equals that }

==("abc", "abc")
==(null, "abc")

class Point(val x: Int, val y: Int) {

  override def hashCode(): Int = (x, y).##

  override def equals(other: Any): Boolean =
    other match {
      case that: Point => this.x == that.x && this.y == that.y
      case _           => false
    }
}
new Point(1, 2).equals(null)
