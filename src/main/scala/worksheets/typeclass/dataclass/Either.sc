// Exceptions are not tracked in any way, shape, or form by the Scala compiler
//    val magic = throwsSomeStuff.andThen(throwsOtherThings).andThen(moreThrowing)
// How then do we communicate an error? By making it explicit in the data type we return.

// Either vs Validated
//  - Validated is used to accumulate errors,
//  - Either is used to short-circuit a computation upon the first error.

// In Scala 2.10.x and 2.11.x, Either is unbiased.
// Usual combinators like flatMap and map are missing from it.
// Instead, you call .right or .left to get a RightProjection or LeftProjection (respectively) which does have the
// combinators. The direction of the projection indicates the direction of bias. For instance, calling map on a
// RightProjection acts on the Right of an Either.
val e1: Either[String, Int] = Right(4)
e1.right.map(_ + 1)
val e2: String Either Int = Left("error")
e2.right.map(_ + 1)

// However, the convention is almost always to right-bias Either.
// Indeed in Scala 2.12.x Either is right-biased by default.
e1.map(_ + 1)
e2.map(_ + 1)

// Because Either is right-biased, it is possible to define a Monad instance for it.
// Since we only ever want the computation to continue in the case of Right, we fix the left type parameter and leave
// the right one free.

import cats.Monad
import cats.syntax.either._

import java.util.ServiceConfigurationError

object ExceptionStyle {
  def parse(s: String): Int =
    if (s.matches("-?[0-9]+")) s.toInt
    else throw new NumberFormatException(s"$s is not a valid integer")

  def reciprocal(i: Int): Double =
    if (i == 0)
      throw new IllegalArgumentException("Cannot take reciprocal of 0")
    else 1.0 / i

  def stringify(d: Double): String = d.toString
}

object EitherStyle {
  def parse(s: String): Either[Exception, Int] =
    if (s.matches("-?[0-9]+")) Either.right(s.toInt)
    else Either.left(new NumberFormatException(s"$s is not a valid integer"))

  def reciprocal(i: Int): Either[Exception, Double] =
    if (i == 0)
      Either.left(new IllegalArgumentException("Cannot take reciprocal of 0"))
    else Either.right(1.0 / i)

  def stringify(d: Double): String = d.toString
}

import EitherStyle._

def magic(s: String): Either[Exception, String] =
  parse(s).flatMap(reciprocal).map(stringify)

// if we exclude case Left(_) =>
// match may not be exhaustive.
//It would fail on the following input: Left((x: Exception forSome x not in (IllegalArgumentException,
// NumberFormatException)))
magic("123") match {
  case Left(_: NumberFormatException) => println("not a number")
  case Left(_: IllegalArgumentException) =>
    println("cannot take reciprocal of 0")
  case Left(_)  => println("got unknown exception")
  case Right(s) => println(s"Got a reciprocal: $s")
}

// However, we “know” by inspection of the source that those will be the only exceptions thrown, so it seems strange
// to have to account for other exceptions. This implies that there is still room to improve.
// Instead of using exceptions as our error value, let’s instead enumerate explicitly the things that can go wrong in
// our program.

object EitherStyle {

  sealed abstract class Error

  final case class NotANumber(string: String) extends Error

  case object NoZeroReciprocal extends Error

  def parse(s: String): Either[Error, Int] =
    if (s.matches("-?[0-9]+")) Either.right(s.toInt)
    else Either.left(NotANumber(s))

  def reciprocal(i: Int): Either[Error, Double] =
    if (i == 0)
      Either.left(NoZeroReciprocal)
    else Either.right(1.0 / i)

  def stringify(d: Double): String = d.toString

  def magic(s: String): Either[Error, String] =
    parse(s).flatMap(reciprocal).map(stringify)
}

// much nicer pattern matching

import EitherStyle._

magic("123") match {
  case Left(NotANumber(_))    => println("not a number")
  case Left(NoZeroReciprocal) => println("cannot take reciprocal of 0")
  case Right(s)               => println(s"Got reciprocal: $s")
}

//Either in the small, Either in the large
// Once you start using Either for all your error-handling,
// you may quickly run into an issue where you need to call into two separate modules which give back separate kinds of
// errors.
sealed abstract class DatabaseError

trait DatabaseValue

object Database {
  def databaseThings(): Either[DatabaseError, DatabaseValue] = ???
}

sealed abstract class ServiceError

trait ServiceValue

object Service {
  def serviceThings(v: DatabaseValue): Either[ServiceError, ServiceValue] = ???
}

def doApp: Either[AnyRef, ServiceValue] =
  Database.databaseThings().flatMap(Service.serviceThings)

// def flatMap[AA >: A, Y](f: (B) => Either[AA, Y]): Either[AA, Y]
// This flatMap is different from the ones you’ll find on List or Option, for example, in that it has two type
// parameters, with the extra AA parameter allowing us to flatMap into an Either with a different type on the left side.
// This behavior is consistent with the covariance of Either, and in some cases it can be convenient, but it also
// makes it easy to run into nasty variance issues - such as Object being inferred as the type of the left side, as
// it is in this case.

// Solution 1: Application-wide errors
sealed abstract class AppError

case object DatabaseError1 extends AppError

case object DatabaseError2 extends AppError

case object ServiceError1 extends AppError

case object ServiceError2 extends AppError

trait DatabaseValue

object Database {
  def databaseThings(): Either[AppError, DatabaseValue] = ???
}

object Service {
  def serviceThings(v: DatabaseValue): Either[AppError, ServiceValue] = ???
}

def doApp(): Either[AppError, ServiceValue] =
  Database.databaseThings().flatMap(Service.serviceThings)

// But consider the case where another module wants to just use Database,
// and gets an Either[AppError, DatabaseValue] back. Should it want to inspect the errors,
// it must inspect all the AppError cases, even though it was only intended for Database to use DatabaseError1 or
// DatabaseError2.

// Solution 2: ADTs all the way down
// -  Instead of lumping all our errors into one big ADT,
//    we can instead keep them local to each module,
//    and have an application-wide error ADT that wraps each error ADT we need.
sealed abstract class DatabaseError

trait DatabaseValue

object Database {
  def databaseThings(): Either[DatabaseError, DatabaseValue] = ???
}

sealed abstract class ServiceError

trait ServiceValue

object Service {
  def serviceThings(v: DatabaseValue): Either[ServiceError, ServiceValue] = ???
}

sealed abstract class AppError

object AppError {

  final case class Database(error: DatabaseError) extends AppError

  final case class Service(error: ServiceError) extends AppError

}

// Now in our outer application, we can wrap/lift each module-specific error into AppError and then call our
// combinators as usual. Either provides a convenient method to assist with this, called Either.leftMap
def doApp: Either[AppError, ServiceValue] =
  Database
    .databaseThings()
    .leftMap[AppError](AppError.Database)
    .flatMap(dv => Service.serviceThings(dv).leftMap(AppError.Service))

def awesome =
  doApp match {
    case Left(AppError.Database(_)) => "database error"
    case Left(AppError.Service(_))  => "service error"
    case Right(_)                   => "good value"
  }

// Working with exception-y code
val either: Either[NumberFormatException, Int] =
  try {
    Either.right("abc".toInt)
  } catch {
    case nfe: NumberFormatException => Either.left(nfe)
  }
val either: Either[NumberFormatException, Int] =
  Either.catchOnly[NumberFormatException]("abc".toInt)
val either: Either[Throwable, Int] = Either.catchNonFatal("abc".toInt)
