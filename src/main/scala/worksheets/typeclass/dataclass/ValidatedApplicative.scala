package worksheets.typeclass.dataclass

import cats.{Applicative, Apply, Semigroup}
import cats.data.{Validated, ValidatedNec}
import cats.data.Validated.Valid
import cats.data.Validated.Invalid

import scala.annotation.tailrec

// applicative functor
//  - Allows application of a function in an Applicative context to a value in an Applicative context
object ValidatedApplicative {
  implicit def validatedApplicative[E: Semigroup]
      : Applicative[Validated[E, *]] =
    new Applicative[Validated[E, *]] {
      override def pure[A](x: A): Validated[E, A] = Validated.valid(x)

      override def ap[A, B](ff: Validated[E, A => B])(
          fa: Validated[E, A]
      ): Validated[E, B] =
        (fa, ff) match {
          case (Valid(a), Valid(fab))     => Valid(fab(a))
          case (i @ Invalid(_), Valid(_)) => i
          case (Valid(_), i @ Invalid(_)) => i
          case (Invalid(e1), Invalid(e2)) =>
            Invalid(Semigroup[E].combine(e1, e2))
        }
    }

  sealed abstract class ConfigError

  final case class MissingConfig(field: String) extends ConfigError

  final case class ParseError(field: String) extends ConfigError

  //sealed abstract class Validated[+E, +A]
  //object Validated {
  //  final case class Valid[+A](a: A) extends Validated[Nothing, A]
  //  final case class Invalid[+E](e: E) extends Validated[E, Nothing]
  //}

  // Our goal is to report any and all errors across independent bits of data
  trait Read[A] {
    def read(s: String): Option[A]
  }

  object Read {
    def apply[A](implicit A: Read[A]): Read[A] = A

    implicit val stringRead: Read[String] = (s: String) => Some(s)
    implicit val intRead: Read[Int] = (s: String) =>
      if (s.matches("-?[0-9]+")) Some(s.toInt)
      else None
  }

  import cats.data.Validated
  import cats.data.Validated.{Invalid, Valid}

  case class Config(map: Map[String, String]) {
    def parse[A: Read](key: String): Validated[ConfigError, A] =
      map.get(key) match {
        case None => Invalid(MissingConfig(key))
        case Some(value) =>
          Read[A].read(value) match {
            case None    => Invalid(ParseError(key))
            case Some(a) => Valid(a)
          }
      }
  }

  val personConfig = Config(
    Map(
      ("name", "cat"),
      ("age", "not a number"),
      ("houseNumber", "1234"),
      ("lane", "feline street")
    )
  )

  case class Address(houseNumber: Int, street: String)

  case class Person(name: String, age: Int, address: Address)

  val personFromConfig: ValidatedNec[ConfigError, Person] =
    Apply[ValidatedNec[ConfigError, *]].map4(
      personConfig.parse[String]("name").toValidatedNec,
      personConfig.parse[Int]("age").toValidatedNec,
      personConfig.parse[Int]("house_number").toValidatedNec,
      personConfig.parse[String]("street").toValidatedNec
    ) {
      case (name, age, houseNumber, street) =>
        Person(name, age, Address(houseNumber, street))
    }

  import cats.Monad

  implicit def validatedMonad[E]: Monad[Validated[E, *]] = {
    new Monad[Validated[E, *]] {
      override def pure[A](x: A): Validated[E, A] = Validated.Valid(x)

      override def flatMap[A, B](fa: Validated[E, A])(
          f: A => Validated[E, B]
      ): Validated[E, B] =
        fa match {
          case Valid(a)       => f(a)
          case i @ Invalid(_) => i
        }

      @tailrec
      override def tailRecM[A, B](a: A)(
          f: A => Validated[E, Either[A, B]]
      ): Validated[E, B] =
        f(a) match {
          case Valid(Right(b)) => Valid(b)
          case Valid(Left(a))  => tailRecM(a)(f)
          case i @ Invalid(_)  => i
        }
    }
    /*
// Note that all Monad instances are also Applicative instances, where ap is defined as
trait Monad[F[_]] {
  def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]
  def pure[A](x: A): F[A]

  def map[A, B](fa: F[A])(f: A => B): F[B] =
    flatMap(fa)(f.andThen(pure))

  def ap[A, B](fa: F[A])(f: F[A => B]): F[B] =
    flatMap(fa)(a => map(f)(fab => fab(a)))
}
     */
  }

  // the ap behavior defined in terms of flatMap does not behave the same as that of our ap defined above
  validatedMonad.tuple2(
    Validated.invalidNec[String, Int]("oops"),
    Validated.invalidNec[String, Double]("uh oh")
  )
  // This one short circuits! Therefore, if we were to define a Monad (or FlatMap) instance
  // for Validated we would have to override ap to get the behavior we want.
  // But then the behavior of flatMap would be inconsistent with that of ap, not good.
  // Therefore, Validated has only an Applicative instance.

}
