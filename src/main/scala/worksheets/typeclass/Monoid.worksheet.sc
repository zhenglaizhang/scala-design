// Monoid extends the Semigroup type class, adding an empty method to semigroup's combine. The empty method must return a value that when combined with any other instance of that type returns the other instance, i.e.
//  (combine(x, empty) == combine(empty, x) == x)

def sumInts(xs: List[Int]): Int = xs.foldRight(0)(_ + _)
def concatStrings(xs: List[String]): String = xs.foldRight("")(_ + _)
def unionSets[A](xs: List[Set[A]]): Set[A] =
  xs.foldRight(Set.empty[A])(_ union _)

object show {

  trait Semigroup[A] {
    // be associative
    // combine(a1, combine(a2, a3)) == combine(combine(a1, a2), a3)
    def combine(x: A, y: A): A
  }

  // Monoid extends the power of Semigroup by providing an additional empty value.
  trait Monoid[A] extends Semigroup[A] {
    // identity or zero element
    // combine(a, empty) == a
    def empty: A
  }

  // Many libraries that provide type classes provide a utility method on the companion object of the type class,
  // usually under the name apply, that skirts the need to call implicitly everywhere.
  object Monoid {
    def apply[A: Monoid]: Monoid[A] = implicitly[Monoid[A]]
  }

}

import cats.Monoid
import cats.instances.string._
import show.Semigroup

//def combineAll[A](xs: List[A])(implicit m: Monoid[A]): A =
//  xs.foldRight(m.empty)(m.combine)

//def combineAll[A: Monoid](xs: List[A]) =
//  xs.foldRight(implicitly[Monoid[A]].empty)(implicitly[Monoid[A]].combine)

def combineAll[A: Monoid](xs: List[A]) =
  xs.foldRight(Monoid[A].empty)(Monoid[A].combine)

Monoid[String].combine("Hi ", "there")
Monoid[String].empty
Monoid[String].combineAll(List())
Monoid[String].combineAll(List("a", "b", "c"))
Monoid[Map[String, Int]].combineAll(List(Map("a" -> 1, "b" -> 2), Map("a"->2)))
// res: Map("a" -> 3, "b" -> 2)
Monoid[Map[String, Int]].combineAll(List())  // Map.empty[String, Int]

Monoid.apply[String].combine("Hi ", "there")
Monoid.apply[String].empty

val xs = List(1, 2, 3, 4, 5)
xs.foldMap(identity)
xs.foldMap(_.toString)

import cats.instances.int._
import cats.instances.option._

val a = Option(22)
val b = Option(20)
Monoid[Option[Int]].combine(a, b)

// wow, import everyting...
import cats.syntax.semigroup._

val strResult = "Hi " |+| "there" |+| Monoid[String].empty

val intResult = 1 |+| 2 |+| Monoid[Int].empty

final case class Order(totalCost: Double, quantity: Double)

implicit val orderMonoid = Monoid.instance[Order](
  Order(0, 0),
  (o1, o2) => Order(o1.totalCost + o2.totalCost, o1.quantity + o2.quantity)
)

def add[A](items: List[A])(implicit m: Monoid[A]): A =
  items.foldRight(m.empty)(_ |+| _)

add(List(1, 2, 3))
add(List(Order(1, 1), Order(2, 3)))

import cats.instances.tuple._

("hello", 123) |+| (("world", 234))
Option(1) |+| Option(2)
Option(1) |+| Option.empty

import cats.instances.map._

Map("a" -> 1, "b" -> 2) |+| Map("a" -> 3, "d" -> 4)

add(List(None, Some(1), Some(2)))

// Implicit derivation
final case class Pair[A, B](first: A, second: B)

// Note that a Monoid[Pair[A, B]] is derivable given Monoid[A] and Monoid[B]:
implicit def deriveMonoidPair[A, B](implicit
    A: Monoid[A],
    B: Monoid[B]
): Monoid[Pair[A, B]] =
  new Monoid[Pair[A, B]] {
    def empty: Pair[A, B] = Pair(A.empty, B.empty)

    def combine(x: Pair[A, B], y: Pair[A, B]): Pair[A, B] =
      Pair(A.combine(x.first, y.first), B.combine(x.second, y.second))
  }

// One of the most powerful features of type classes is the ability to do this kind of derivation automatically.
// We can do this through Scala’s implicit mechanism.

combineAll(List(Pair(1, "hello"), Pair(2, "world")))

// There are some types that can form a Semigroup but not a Monoid.
// For example, the NonEmptyList

// How then can we collapse a List[NonEmptyList[A]]
// For such types that only have a Semigroup we can lift into Option to get a Monoid.j
// For any Semigroup[A], there is a Monoid[Option[A]].
import cats.implicits._
import cats.syntax.all._
import cats.instances.option._
implicit def optionMonoid[A: Semigroup]: Monoid[Option[A]] =
  new Monoid[Option[A]] {
    def empty: Option[A] = None
    def combine(x: Option[A], y: Option[A]): Option[A] =
      x match {
        case None => y
        case Some(xv) =>
          y match {
            case None     => x
            case Some(yv) => Some(implicitly[Semigroup[A]].combine(xv, yv))
          }
      }
  }
// This is the Monoid for Option: for any Semigroup[A], there is a Monoid[Option[A]].

import cats.Monoid
import cats.data.NonEmptyList
import cats.implicits._

val xs2 = List(NonEmptyList(1, List(2, 3)), NonEmptyList(4, List(5, 6)))
val lifted = xs2.map(nel => Option(nel))
Monoid.combineAll(lifted)

import cats.Monoid
//Monoid.combineAllOption(xs)

implicit def monoidTuple[A: Monoid, B: Monoid]: Monoid[(A, B)] = new Monoid[(A, B)] {
  def combine(x: (A, B), y: (A, B)): (A, B) = {
    val (xa, xb) = x
    val (ya, yb) = y
    (Monoid[A].combine(xa, ya), Monoid[B].combine(xb, yb))
  }
}

import cats.syntax.all._
List(1,2,3).foldMap(i => (i, i.toString))