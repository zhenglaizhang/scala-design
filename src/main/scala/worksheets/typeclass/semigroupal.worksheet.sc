// Semigroups allows combining two things of the same type into another thing of the same type.
// For example, addition forms a semigroup over integers.
// Monoids add the additional property of having an "zero" element, which you can append to a value without changing
// the value.

import cats.Semigroupal
import cats.instances.option._

// Combine an `F[A]` and an `F[B]` into an `F[(A, B)]` that maintains the effects of both `fa` and `fb`.
Semigroupal[Option].product(Some(123), Some("abc"))
Semigroupal[Option].product(None, Some(123))
Semigroupal.tuple3(Option(1), Option(2), Option(3))
Semigroupal.tuple3(Option(1), Option(2), Option.empty)
Semigroupal.map3(Option(1), Option(2), Option(3))(_ + _ + _)
Semigroupal.map3(Option.empty[Int], Option(1), Option(2))(_ + _ + _)

// apply syntax
import cats.instances.option._
import cats.syntax.apply._
(Option(12), Option("abc"), Option(true)).tupled

final case class Cat(name: String, born: Int, color: String)

(
  Option("name"),
  Option(1921),
  Option("Organcle & black")
).mapN(Cat.apply)

import cats.instances.list._
// cartesian product instead of zip
Semigroupal[List].product(List(1, 2), List(3, 4))

// fail fast instead oof accumulating error handling
import cats.instances.either._
type ErrorOr[A] = Either[Vector[String], A]
val e1: ErrorOr[Int] = Left(Vector("error 1"))
val e2: ErrorOr[Int] = Left(Vector("error 2"))
Semigroupal[ErrorOr].product(e1, e1)

import cats.syntax.apply._
(e1, e2).tupled

// import cats.syntax.parallel._
// (e1, e2).parTupled
