// NonEmptyList
// Usage in Validated and Ior
// NonEmptyList is a specialized data type that has at least one element. Otherwise it behaves like a normal List
//  - For sum types like Validated (and Ior), it does not make sense to have a Invalid with no errors: no errors means
//    it is a Valid! By using NonEmptyList, we explicitly say in the type that:
//  - If it is a Invalid, then there is at least one error.

// Avoiding Option by demanding more specific arguments
// That works and is safe, but this only masks the problem of accepting invalid input.
// By using Option, we extend the average function with the logic to handle empty lists.
// Additionally, all callers have to handle the Option cases, maybe over and over again. While better than failing with an exception, this is far from perfect.
def average(xs: List[Int]): Option[Double] =
  if (xs.isEmpty) {
    None
  } else {
    Some(xs.sum / xs.length.toDouble)
  }

// Instead what we would like to express is that average does not make sense at all for an empty list. Luckily, cats
// defines the NonEmptyList. As the name says, this represents a list that cannot, by construction, be empty. So
// given a NonEmptyList[A] you can be sure that there is at least one A in there.
import cats.data.{NonEmptyList, NonEmptyVector}

def average(xs: NonEmptyList[Int]): Double = {
  xs.reduceLeft(_ + _) / xs.length.toDouble
}

// With that, average is free of any “domain invariant validation” and instead can focus on the actual logic of
// computing the average of the list.
// This ties in nicely with the recommendation of shifting your validation to the very borders of your program, where
// the input enters your system.

// Structure of a NonEmptyList
object w {
  // The head of the NonEmptyList will be non-empty. Meanwhile, the tail can have zero or more elements contained in
  // a List.
  final case class NonEmptyList[+A](head: A, tail: List[A])
}

// Defined for all its elements
// An important trait of NonEmptyList is the totality. For List specifically, both head and tail are partial: they
// are only well-defined if it has at least one element.
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
import cats.implicits._
NonEmptyList.fromFoldable(List())
NonEmptyList.fromFoldable(List(1, 2, 3))
NonEmptyList.fromFoldable(Vector(42))
NonEmptyList.fromFoldable(Either.left[String, Int]("Error"))
NonEmptyList.fromFoldable(Either.right[String, Int](42))
NonEmptyList.fromReducible(NonEmptyVector.of(1, 2, 3))
