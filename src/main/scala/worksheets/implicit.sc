//Fortunately, an implicit method can also be used. To function as an implicit value, it
//must not take arguments itself, unless the arguments are also implicit. H

//An implicit method can also be used. To function as an implicit value, it
//must not take arguments itself, unless the arguments are also implicit. H

//
// Rules for Implicit Arguments
//  -1. Only the last argument list,including the only list for a single-list method,can have implicit arguments.
//  -2. The implicit keyword must appear first and only once in the argument list. The list can’t have “nonimplicit”
//  arguments followed by implicit arguments.
//  -3. All the arguments are implicit when the list starts with the implicit keyword.

case class MyList[A](list: List[A]) {
  def sortBy1[B](f: A => B)(implicit ord: Ordering[B]): List[A] =
    list.sortBy(f)(ord)

  // B is bound by a context
  // The type parameter B : Ordering is called a context bound.
  // It implies the second, implicit argument list that takes an Ordering[B] instance
  def sortBy2[B: Ordering](f: A => B): List[A] =
    list.sortBy(f)(implicitly[Ordering[B]])
}

val l = MyList(List(1, 2, 3, 4))
l.sortBy1(i => -i)
l.sortBy2(identity)

//The first category is boilerplate elimination of passing contexts, such as providing
//context information implicitly rather than explicitly. Using a method argument permits composition of behavior
//  - execution context
//  - transactions
//  - database connections
//  - thread pools
//  - user sessions
//
//The second category includes constraints that reduce bugs or limit the allowed types that can be used with certain
//methods with parameterized types. This is to control capabilities
// e, an implicit user session argument might contain authorization tokens that
//control whether or not certain API operations can be invoked on behalf of the user or
//to limit data visibility
// e.g. CanBuildFrom
trait Session {
  def loggedIn: Boolean
}

trait Menu

def createMenu(implicit session: Session): Menu = ???

trait Closeable {
  def close(): Unit
}

object manage {
  def apply[R <: { def close(): Unit }, T](resource: => R)(f: R => T) = ???

  def apply2[R <: Closeable, T](resource: => R)(f: R => T) = ???
}

//This technique doesn’t help when there is no common superclass. For that situation, we
//can use an implicit argument to limit the allowed types.

val x: Int Tuple2 String = 12 -> "12"

//
// implicit conversions
//
implicit class WowString(s: String) {
  def getLength = s.length

  def getSum(implicit toIntWow: String => Int) = toIntWow(s) + 12
}

//
// Implicit Evidence
//
//  Good to convert Collection of Tuple2 pairs to map,
//  This is what toMap does, but we have a dilemma. We can’t allow the user to call toMap if the sequence is not a
//  sequence of pairs.
trait TraversableOnce[A] {
  // the implicit evidence ev will be synthesized by the compiler if A <: (T, U), aka. A is actually a pair
  // “evidence” only has to exist to enforce a type constraint. We don’t have to define an implicit value ourselves to
  // do extra, custom work.
  def toMap[T, U](implicit ev: <:<[A, (T, U)]): Map[T, U]
}

//
// Working Around Erasure
//
//  - With implicit evidence, we didn’t use the implicit object in the computation. Rather, we only used its
//  existence as confirmation that certain type constraints were satisfied.
//  - For historical reasons, the JVM “forgets” the type arguments for parameterized types.
object M {

  implicit object IntMarker

  implicit object StringMarker

  def m(seq: Seq[Int])(implicit i: IntMarker.type): Unit =
    println(s"Seq[Int]: $seq")

  def m(seq: Seq[String])(implicit i: StringMarker.type): Unit =
    println(s"Seq[String: $seq")
}

import M._
// Now the compiler considers the two m methods to be distinct after type erasure.
// => Using implicit values for such common types is not recommended.
// => The safer bet is to limit your use of implicit arguments and values to very specific, purpose-built types.
m(List(1, 3, 4))
m(List("1", "2", "3"))

object Pipeline {

  implicit class toPiped[V](v: V) {
    def |>[R](f: V => R) = f(v)
  }

}

def sToInt: String => Int = _.toInt
def intDouble: Int => Int = _ * 2
def intToS: Int => String = _.toString

import Pipeline._

val r = "12" |> sToInt |> intDouble |> intToS

// implicit conversions
class MyMap {
  def apply[A, B](elems: (A, B)*): Map[A, B] = ???
}

//For something to be considered an implicit conversion, it must be declared with the implicit keyword and it must
// either be a class that takes a single constructor argument or it must be a method that takes a single argument.
implicit final class MyArrowAssoc[A](val self: A) {
  def -->[B](y: B): (A, B) = Tuple2(self, y)
}

//Because String has no --> method, it looks for an implicit conversion in scope to a
//type that has this method.
"2" --> 1
//Implicit methods can still be used, but now they are only necessary when converting to a type that already exists
// for other purposes and it isn’t declared implicit.

// scala.runtime.RichInt
1 to 12 by 3

BigDecimal

//A recommended convention is to put implicit values and conversions into a special package named implicits or an
// object named Implicits, except for those defined in companion objects
//Scala has several implicit wrapper types for Java types like String and Array
//The implicit conversions for the built-in “Wrapped” types are always in scope. They are defined in Predef.

//
// The Expression Problem
//  - This desire to extend modules without modifying their source code is called the Expression Problem,
//  - Object-oriented programming solves this problem with subtyping, more precisely called subtype polymorphism.
//    - Open/Closed Principle
//    - We program to abstractions and use derived classes when we need changed behavior.
//    - What if the behavior is only needed in a few contexts, while for most contexts, it’s just a burden that the
//    client code carries around?
//    - Single Responsibility Principle, a classic design principle that encourages us to define abstractions and
//    implementing types with just a single behavior.
//  - metaprogramming facilities that allow you to modify classes in the runtime environment without modifying source
//  code. This approach can partially solve the problem of types with rarely used behavior.
//    - Unfortunately, for most dynamic languages, any runtime modifications to a type are global, so all users are
//    affected.
//  - The implicit conversion feature of Scala lets us implement a statically typed alternative approach, called type classes,
