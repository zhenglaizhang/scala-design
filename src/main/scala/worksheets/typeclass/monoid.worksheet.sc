object show {
  trait Semigroup[A] {
    // be associative
    def combine(x: A, y: A): A
  }
  trait Monoid[A] extends Semigroup[A] {
    // identity element
    def empty: A
  }
}

import cats.Monoid
import cats.instances.string._

Monoid[String].combine("Hi ", "there")
Monoid[String].empty
Monoid.apply[String].combine("Hi ", "there")
Monoid.apply[String].empty

import cats.instances.int._
import cats.instances.option._
val a = Option(22)
val b = Option(20)
Monoid[Option[Int]].combine(a, b)

// wow, import everyting...
import cats._
import cats.implicits._

import cats.syntax.semigroup._
val strResult = "Hi " |+| "there" |+| Monoid[String].empty

val intResult = 1 |+| 2 |+| Monoid[Int].empty
