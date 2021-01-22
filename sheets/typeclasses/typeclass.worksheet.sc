// def implicitly2[A](implicit value: A): A = value

// implicit convension
// frowned

//Ad-hoc polymorphism
//Break free from your class oppressors!
//Concerns that cross class hierarchy e.g. serialize to JSON
//Common behaviour without (useful) common type
//Abstract behaviour to a type class
//Can implement type class instances in ad-hoc manner
//Can use context-bound type parameters

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

object PrintableSyntax {
  implicit class PrintableOps[A](a: A) {
    def format()(implicit p: Printable[A]): String = p.print(a)

    def print()(implicit p: Printable[A]): Unit = println(p.print(a))
  }
}

import PrintableSyntax._
12.format
12.print
cat.format
cat.print

def p[A: Printable](x: A): Unit = {
  implicitly[Printable[A]].print(x)
}
