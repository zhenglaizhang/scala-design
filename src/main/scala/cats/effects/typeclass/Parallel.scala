package cats.effects.typeclass

import cats.data.NonEmptyList
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._

// When browsing the various Monads included in Cats, you may have noticed that some of them have data types that are
// actually of the same structure, but instead have instances of Applicative. E.g. Either and Validated.

// This is because defining a Monad instance for data types like Validated would be inconsistent with its
// error-accumulating behaviour. In short, Monads describe dependent computations and Applicatives describe
// independent computations.

// Sometimes however, we want to use both in conjunction with each other. In the example of Either and Validated it is trivial albeit cumbersome to convert between the two types.

case class Age(v: Int)
case class Name(v: String)
case class Person(name: Name, age: Age)

object ParallelApp1 extends IOApp {

  def parse(s: String): Either[NonEmptyList[String], Int] =
    if (s.matches("-?[0-9]+")) Right(s.toInt)
    else Left(NonEmptyList.one(s"$s is not a valid integer"))

  def validateAge(a: Int): Either[NonEmptyList[String], Age] =
    if (a > 18) Right(Age(a))
    else Left(NonEmptyList.one(s"$a is not old enough"))

  def validateName(n: String): Either[NonEmptyList[String], Name] =
    if (n.length >= 8) Right(Name(n))
    else Left(NonEmptyList.one(s"$n Does not have enough characters"))

  def parsePerson(
      ageStr: String,
      nameStr: String
  ): Either[NonEmptyList[String], Person] =
    for {
      age <- parse(ageStr)
      p <- (validateName(nameStr).toValidated, validateAge(age).toValidated)
        .mapN(Person)
        .toEither
    } yield p

  def run(args: List[String]): IO[ExitCode] = {
    IO {
      val p = parsePerson("1", "name")
      println(p)
    }.as(ExitCode.Success)

  }
}

object w {
  // To mitigate this pain, Cats introduces a type class Parallel that abstracts over Monads which also support
  // parallel composition. It is simply defined in terms of conversion functions between the two data types:
  //
  //trait Parallel[M[_]] {
  //  type F[_]
  //  def sequential: F ~> M
  //  def parallel: M ~> F
  //}
  // Where M[_] has to have an instance of Monad and F[_] an instance of Applicative.
  // The Parallel type class transforms between Monad M[_] and Applicative F[_]

  // Recall that ~> is just an alias for FunctionK. This allows us to get rid of most of our boilerplate from earlier
}
