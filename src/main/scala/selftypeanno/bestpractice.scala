package selftypeanno
import selftypeanno.Status.Ok
import selftypeanno.Status.Nok

// scala 3 might eventually deprecate Enumeration in favour of Enum
// object Status extends Enumeration {
//   val Ok, Nok = Value
// }

// sealed trait hierachies
// ADT
// cannot add sub types
sealed trait Status extends Product with Serializable
object Status {
  case object Ok extends Status
  case object Nok extends Status
}

sealed trait Foo
class Bar extends Foo
// bad as no final,
// File other.scala
// class FooBar extends Bar

object BestPractice extends App {
  Array(1) == Array(1) // false
  Array(1).sameElements(Array(1)) // true

  def asOptionBad[A](a: A) = Some(a) // bad

  // prefer type annotation for public memebers
  def asOption[A](a: A): Option[A] = Some(a)
  println(1 -> 4 / 2)

  // compiles without non-exhaustivity warning
  // def foo(w: Status.Value): Unit =
  //   w match {
  //     case Status.Ok => println("ok")
  //   }

  def foo(w: Status): Unit =
    w match {
      case Ok  => println("ok")
      case Nok => println("nok")
    }

  foo(Status.Nok);
  // MatchError

  val x = List(Status.Ok, Status.Nok)
  // List[Product with Serializable with Status]
}
