// def implicitly2[A](implicit value: A): A = value

// implicit convension
// frowned
// import scala.language.implicitConversions
// implicit def strToInt(str: String): Int = { str.length() }

trait Printable[A] {
  def print(a: A): String
}

object PrintableInstances {
  implicit val strPrintable = new Printable[String] {
    override def print(a: String): String = a
  }

  implicit val intPrintable = new Printable[Int] {
    override def print(a: Int): String = a.toString
  }
}

object Printable {
  def format[A](a: A)(implicit p: Printable[A]): String = p.print(a)

  def print[A](a: A)(implicit p: Printable[A]): Unit = println(p.print((a)))
}
import PrintableInstances._
Printable.print(12)
Printable.print("abc")

case class Cat(name: String, color: String)
val cat = Cat("abc", "grey")
println(cat)
implicit val catPrinter = new Printable[Cat] {
  override def print(a: Cat): String = s"${a.name} has color ${a.color}"
}
Printable.print(cat)
