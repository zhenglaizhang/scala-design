// A semigroup for some given type A has a single operation (which we will call combine), which takes two values of type A, and returns a value of type A. This operation must be guaranteed to be associative.
//   ((a combine b) combine c) === (a combine (b combine c)) for all possible values of a,b,c.

object w {

  // If a type A can form a Semigroup it has an associative binary operation.
  // Associativity means the following equality must hold for any choice of x, y, and z.
  // Semigroup#combine must be associative
  //  combine(x, combine(y, z)) = combine(combine(x, y), z)
  trait Semigroup[A] {
    def combine(x: A, y: A): A
  }

}

import cats.Semigroup
import cats.instances.int._
import cats.instances.list._

implicit val intAdditionSemigroup: Semigroup[Int] = _ + _

val x = 1
val y = 2
val z = 3
Semigroup[Int].combine(x, y)
Semigroup[Int].combineN(2, 10)
Semigroup[Int].combine(x, Semigroup[Int].combine(y, z))
Semigroup[Int].combine(Semigroup[Int].combine(x, y), z)
Semigroup[List[Int]].combine(List(1, 2, 3), List(4, 5, 6)) // List(1,2,3,4,5,6)

Semigroup[Option[Int]].combine(Some(1), Some(2))
Semigroup[Option[Int]].combine(Some(1), None)
Semigroup[Int => Int].combine(_ + 1, _ * 10).apply(6) // 67 => function compose

// Infix syntax is also available for types that have a Semigroup instance.

import cats.syntax.semigroup._
// or
//import cats.syntax.all._

Map("foo" -> Map("bar" -> 5)).combine(Map("foo" -> Map("bar" -> 6), "baz" -> Map()))
Map("foo" -> List(1, 2)).combine(Map("foo" -> List(3, 4), "bar" -> List(42)))



1 |+| 2

// Semigroup for Maps.

val m1 = Map("hello" -> 1, "world" -> 1)
val m2 = Map("hello" -> 2, "cars" -> 3)
Semigroup[Map[String, Int]].combine(m1, m2)
m1 |+| m2

// Cats provides many Semigroup instances out of the box such as Int (+) and String (++)…
Semigroup[Int]
Semigroup[String]
Semigroup[Map[String, Int]]

// Instances for type constructors regardless of
// their type parameter such as List (++) and Set (union)…
Semigroup[List[Byte]]
Semigroup[Set[Int]]

trait Foo

Semigroup[List[Foo]]

// And instances for type constructors that depend on (one of) their type parameters having instances such as tuples
// (pointwise combine).
Semigroup[(List[Foo], Int, String)]
Semigroup[(List[Foo], Int)]

// laws: associativity
// To sum a List[Int] we can choose to either foldLeft or foldRight since all that changes is associativity.
val leftwards = List(1, 2, 3).foldLeft(0)(_ |+| _)
val rightwards = List(1, 2, 3).foldRight(0)(_ |+| _)
val list = List(1, 2, 3, 4, 5)
val (left, right) = list.splitAt(2)
val sumLeft = left.foldLeft(0)(_ |+| _)
val sumRight = right.foldLeft(0)(_ |+| _)
val result = sumLeft |+| sumRight

//def combineAll[A: Semigroup](as: List[A]): A =
//  as.foldLeft( /* ?? what goes here ?? */ )(_ |+| _)

// Semigroup isn’t powerful enough for us to implement this function - namely, it doesn’t give us an identity or
// fallback value if the list is empty. We need a power expressive abstraction, which we can find in the Monoid type
// class.

// N.B. Cats defines the Semigroup type class in cats-kernel. The cats package object defines type aliases to the
// Semigroup from cats-kernel, so that you can simply import cats.Semigroup.

def optionCombine[A: Semigroup](a: A, opt: Option[A]): A =
  opt.map(a |+| _).getOrElse(a)
def mergeMap[K, V: Semigroup](lhs: Map[K, V], rhs: Map[K, V]): Map[K, V] =
  lhs.foldLeft(rhs) {
    case (acc, (k, v)) => acc.updated(k, optionCombine(v, acc.get(k)))
  }

val xm1 = Map('a' -> 1, 'b' -> 2)
val xm2 = Map('b' -> 3, 'c' -> 4)
val x1 = mergeMap(xm1, xm2)

val ym1 = Map(1 -> List("hello"))
val ym2 = Map(2 -> List("cats"), 1 -> List("world"))
val y1 = mergeMap(ym1, ym2)

//  type of mergeMap satisfies the type of Semigroup specialized to Map[K, *] and is associative
Semigroup[Map[Char, Int]].combine(xm1, xm2) == x
Semigroup[Map[Int, List[String]]].combine(ym1, ym2) == y


val one = Option(1)
val two = Option(2)
val n: Option[Int] = None
one |+| two  // Some(3)
one |+| n  // Some(1)
n |+| n // None
n |+| two // Some(2)