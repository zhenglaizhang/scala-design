// A type class is a bundle of types and operations defined on them.
// Most type classes have laws that implementations are required to satisfy.

// With implicit conversions, it’s feasible to “add”toJSON and toXML methods to any type.
// The Type Class Pattern is ideal for situations where certain clients will benefit from the “illusion” that a set
// of classes provide a particular behavior that isn’t useful for the ma‐ jority of clients. Used wisely, it helps
// balance the needs of various clients while main‐ taining the Single Responsibility Principle.

// Type classes help us avoid the temptation of creating “kitchen-sink” abstractions, like Java’s Object, because we
// can add behavior on an ad hoc basis. Scala’s -> pair- construction idiom is one example. Recall that we aren’t
// modifying these types; we are using the implicit mechanism to wrap objects with types that provide the behaviors
// we need. It only appears that we are modifying the types,

// def implicitly2[A](implicit value: A): A = value

// implicit convension
// frowned

//Ad-hoc polymorphism (extension method)
//  - vs subtype polymorphism
//  - vs parametric pholimorphism
//Break free from your class oppressors!
//Concerns that cross class hierarchy e.g. serialize to JSON
//Common behaviour without (useful) common type
//Abstract behaviour to a type class
//Can implement type class instances in ad-hoc manner
//Can use context-bound type parameters

//
// Implicit Resolution Rules
//
// use the term “value” in the following discussion, although methods, values, or classes can be used, depending on
// the implicit scenario:
//  - Any type-compatible implicit value that doesn’t require a prefix path.
//    In other words, it is defined in the same scope, such as within the same block of code, within the same type,
//    within its companion object (if any), and within a parent type.
//  - An implicit value that was imported into the current scope.(It also doesn’t require a prefix path to use it.)
//  Imported values, the second bullet point, take precedence over the already-in-scope values.
//  In some cases, several possible matches are type compatible. The most specific match wins.
//  If two or more implicit values are ambiguous, such as they have the same specific type, it triggers a compiler
//  error.

//
//  Scala Built-in Implicits
//  - The source code for the Scala 2.11 library has more than 300 implicit methods, values, and types
//  - All of the companion objects for AnyVal types have widening conversions,
//     because of the implicit conversion feature, the Scala grammar doesn’t need to implement the most common type
//     conversions

object SomeBuiltInImplicitsDemo {
  // @inline annotation, which encourages the compiler to try especially hard to inline the method call, elimi‐
  // nating the stack frame overhead.
  // @noinline annotation that prevents the compiler from attempting to inline the method call, even if it can.
  @inline implicit def int2Long(x: Int): Long = x.toLong

  @inline implicit def int2BigDecimal(x: Int): BigDecimal = BigDecimal(x)

  @inline implicit def option2Iterable[A](xo: Option[A]): Iterable[A] =
    xo.toList
}

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

// implicit & case cannot be used together
//  implicit case class IntToStr(v: Int)
// illegal combination of modifiers: implicit and case for: class IntToStr

val zipped = List(1, 2, 3) zip List("1", "2", "3", "4")
val products = zipped map { case (x, y) => x * y.toInt }
val pair = (List(1, 2, 3), List(4, 5, 6))
val unpair = pair.invert

//$conforms

import scala.collection.JavaConverters._
import scala.jdk.CollectionConverters._
//DecorateAsJava
//DecorateAsScala
