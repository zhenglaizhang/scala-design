import scala.concurrent.Future
import cats.data.EitherT
import cats.data.OptionT

// transform List[Option[A]] into single monad
type ListOption[A] = OptionT[List, A]

import cats.instances.list._
import cats.syntax.applicative._

val result1: ListOption[Int] = OptionT(List(Option(10)))
val result2: ListOption[Int] = 32.pure[ListOption]

result1.flatMap { (x: Int) =>
  result2.map { (y: Int) =>
    x + y
  }
}

type ErrorOr[A] = Either[String, A]
type ErrorOrOption[A] = OptionT[ErrorOr, A]

import cats.instances.either._
import cats.instances.future._
import cats.syntax.applicative._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

val a = 10.pure[ErrorOrOption]
val b = 32.pure[ErrorOrOption]
val c = a.flatMap(x => b.map(y => x + y))
c.value

type FutureEither[A] = EitherT[Future, String, A]
type FutureEitherOption[A] = OptionT[FutureEither, A]

val futureEitherOr = for {
  a <- 2.pure[FutureEitherOption]
  b <- 4.pure[FutureEitherOption]
} yield a + b

Await.result(futureEitherOr.value.value, 1.second)