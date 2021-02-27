// ApplicativeError
//  - ApplicativeError extends Applicative
//  - Provide handling for types that represent the quality of an exception or an error, for example, Either[E, A]
import cats.data.Validated
import cats.{Applicative, ApplicativeError, Monoid}
object w {
  trait ApplicativeError[F[_], E] extends Applicative[F] {
    def raiseError[A](e: E): F[A]
    def handleErrorWith[A](fa: F[A])(f: E => F[A]): F[A]
    def handleError[A](fa: F[A])(f: E => A): F[A]
    def attempt[A](fa: F[A]): F[Either[E, A]]
    //More functions elided
  }
}

def attemptDivide(x: Int, y: Int): Either[String, Int] = {
  if (y == 0) Left("divisor is zero")
  else Right(x / y)
}

// While fine in the above approach, we can abstract the Either away to support any other kind of “error” type
// without having to create multiple functions with different “container” types.
def attemptDivideApplicativeError[F[_]](x: Int, y: Int)(implicit
    ae: ApplicativeError[F, String]
): F[Int] =
  if (y == 0)
    ae.raiseError("divisor is zero")
  else
    ae.pure(x / y) // ApplicativeError is an Applicative

type OnError[A] = Either[String, A]
val e: OnError[Int] = attemptDivideApplicativeError[OnError](30, 10)
val f: Either[String, Int] = attemptDivideApplicativeError(20, 10)

type MyValidated[A] = Validated[String, A]
val g = attemptDivideApplicativeError[MyValidated](30, 10)
val h = attemptDivideApplicativeError[({ type T[A] = Validated[String, A] })#T](
  30,
  10
)
// with KindProjector to make this more readable
// val j = attemptDivideApplicativeError[Validated[String, *]](30, 10)

// It is an Applicative after all
def attemptApplicativeErrorWithMap2[F[_]](x: Int, y: Int)(implicit
    ae: ApplicativeError[F, String]
): F[_] = {
  if (y == 0) ae.raiseError("divisor is zero")
  else {
    val fa = ae.pure(x)
    val fb = ae.pure(y)
    ae.map2(fa, fb)(_ / _)
  }
}

// ApplicativeError has methods to handle what to do when F[_] represents an error.

def attemptDivideApplicativeErrorAbove2[F[_]](x: Int, y: Int)(implicit
    ae: ApplicativeError[F, String]
): F[Int] =
  if (y == 0) ae.raiseError("Bad Math")
  else if (y == 1) ae.raiseError("Waste of Time")
  else ae.pure(x / y)

def handler[F[_]](
    f: F[Int]
)(implicit ae: ApplicativeError[F, String]): F[Int] = {
  ae.handleError(f) {
    case "Bad Math"      => -1
    case "Waste of Time" => -2
    case _               => 3
  }
}

handler(attemptDivideApplicativeErrorAbove2(3, 0))

def handlerErrorWith[F[_], M[_], A](
    f: F[A]
)(implicit F: ApplicativeError[F, String], M: Monoid[A]): F[A] = {
  F.handleErrorWith(f)(_ => F.pure(M.empty))
}

import cats.implicits._
handlerErrorWith(attemptDivideApplicativeErrorAbove2(3, 0))
