// A type class is a bundle of types and operations defined on them.
// Most type classes have laws that implementations are required to satisfy.

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

// defines a way of printing a type A
trait Printable[A] {
  def print(a: A): String
}

object PrintableInstances {
  // A type class instance, or simply instance, is an implementation of a type class for a given set of types.
  // Such instances are usually made implicit so the compiler can thread them through functions that require them.
  implicit val strPrintable = new Printable[String] {
    override def print(a: String): String = a
  }

  implicit val intPrintable = new Printable[Int] {
    override def print(a: Int): String = a.toString
  }
}

object Printable {
  def apply[A: Printable](): Printable[A] = implicitly[Printable[A]]

  def format[A](a: A)(implicit p: Printable[A]): String = p.print(a)

  def print[A](a: A)(implicit p: Printable[A]): Unit = println(p.print((a)))
}

import PrintableInstances._

Printable[Int].print(12)
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

  // Convenient syntax, sometimes called extension methods,
  // can be added to types to make it easier to use type classes.
  // we can put the context bounds at class level or method level
  implicit class PrintableOps[A: Printable](a: A) {
    def format(): String = Printable[A].print(a)

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
