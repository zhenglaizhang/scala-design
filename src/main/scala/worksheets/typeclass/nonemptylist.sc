// NonEmptyList is a specialized data type that has at least one element. Otherwise it behaves like a normal List
// For sum types like Validated (and Ior), it does not make sense to have a Invalid with no errors: no errors means
// it is a Valid! By using NonEmptyList, we explicitly say in the type that:
// If it is a Invalid, then there is at least one error.

// Avoiding Option by demanding more specific arguments
def average(xs: List[Int]): Option[Double] =
  if (xs.isEmpty) {
    None
  } else {
    Some(xs.sum / xs.length.toDouble)
  }

import cats.data.NonEmptyList

def average(xs: NonEmptyList[Int]): Double = {
  xs.reduceLeft(_ + _) / xs.length.toDouble
}

// With that, average is free of any “domain invariant validation” and instead can focus on the actual logic of
// computing the average of the list.
// This ties in nicely with the recommendation of shifting your validation to the very borders of your program, where
// the input enters your system.

// Structure of a NonEmptyList
object w {

  final case class NonEmptyList[+A](head: A, tail: List[A])

}

// For List specifically, both head and tail are partial: they are only well-defined if it has at least one element.
// NonEmptyList on the other hand, guarantees you that operations like head and tail are defined, because
// constructing an empty NonEmptyList is simply not possible!

NonEmptyList.one(32)
NonEmptyList.of(1, 2, 3)
NonEmptyList.ofInitLast(List(1, 2, 3), 4)
NonEmptyList.fromList(List())

import cats.syntax.list._

List(1, 2, 3).toNel
List().toNel

// Using fromFoldable and fromReducible
//  - fromReducible can avoid the Option in the return type,
//  - because it is only available for non-empty datastructures
NonEmptyList.fromFoldable(List())
NonEmptyList.fromFoldable(List(1, 2, 3))
NonEmptyList.fromFoldable(Vector(42))
