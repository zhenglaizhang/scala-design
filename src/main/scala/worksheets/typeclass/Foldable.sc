// Foldable type class instances can be defined for data structures that can be folded to a summary value.
//  - Most collection types have foldLeft methods, which will usually be used by the associated Foldable[_] instance.

// Foldable[F] is implemented in terms of two basic methods:
//  - foldLeft(fa, b)(f) eagerly performs a left-associative fold over fa.
//  - foldRight(fa, b)(f) lazily performs a right-associative fold over fa.

// List sum: List(1, 2, 3) and 0 as starting value
// foldLeft: ((0 + 1) + 2) + 3
// foldRight: 0 + (1 + (2 + 3)).

// since integer addition is associative, both approaches will yield the same result.
// However, for non-associative operations, the two methods can produce different results
import cats._
import cats.implicits._
Foldable[List].fold(List("a", "b", "c"))
Foldable[List].foldMap(List(1, 2, 3))(_.toString)
Foldable[List].foldK(List(List(1, 2, 3), List(4, 5, 6)))
Foldable[List].reduceLeftOption(List.empty[Int])(_ + _)
Foldable[List].reduceLeftToOption(List[Int]())(_.toString)((s, i) => s + i)
Foldable[List].reduceLeftToOption(List[Int](1, 2, 3))(_.toString)((s, i) =>
  s + i
)
//Foldable[List]
//  .reduceRightToOption(List(1, 2, 3, 4))(_.toString)((i, s) =>
//    Later(s.value + i)
//  )
//  .value
//Foldable[List]
//  .reduceRightToOption(List[Int]())(_.toString)((i, s) => Later(s.value + i))
//  .value

//Foldable[List].find(List(1, 2, 3))(_ > 2)
//Foldable[List].exists(List(1, 2, 3))(_ > 2)
//Foldable[List].forall(List(1, 2, 3))(_ > 2)
//Foldable[List].forall(List(1, 2, 3))(_ < 4)
//Foldable[Vector].filter_(Vector(1, 2, 3))(_ < 3)
//Foldable[List].isEmpty(List(1, 2))
//Foldable[Option].isEmpty(None)
//Foldable[List].isEmpty(List(1, 2))
//Foldable[List].nonEmpty(List(1, 2))
//Foldable[Option].toList(Option(1))
//Foldable[Option].toList(None)

def parseInt(s: String): Option[Int] =
  scala.util.Try(Integer.parseInt(s)).toOption

Foldable[List].traverse_(List("1", "2", "3"))(parseInt)
// Some(())
Foldable[List].traverse_(List("1", "A"))(parseInt)
// None
Foldable[List].sequence_(List(Option(1), Option(2)))

Foldable[List].sequence_(List(Option(1), None))

Foldable[List].forallM(List(1, 2, 3))(i =>
  if (i < 2) Some(i % 2 == 0) else None
)
Foldable[List].existsM(List(1, 2, 3))(i =>
  if (i < 2) Some(i % 2 == 0) else None
)

Foldable[List].existsM(List(1, 2, 3))(i =>
  if (i < 3) Some(i % 2 == 0) else None
)

val prints: Eval[Unit] =
  List(Eval.always(println(1)), Eval.always(println(2))).sequence_

Foldable[List].dropWhile_(List[Int](2, 4, 5, 6, 7))(_ % 2 == 0)
Foldable[List].dropWhile_(List[Int](1, 2, 4, 5, 6, 7))(_ % 2 == 0)

import cats.data.Nested
val listOption0 = Nested(List(Option(1), Option(2), Option(3)))
val listOption1 = Nested(List(Option(1), Option(2)))
val listOption1 = Nested(List(Option(1), Option(2), None))
//Foldable[Nested[List, Option, *]].fold(listOption0)
//Foldable[Nested[List, Option, *]].fold(listOption1)

// Hence when defining some new data structure,
// if we can define a foldLeft and foldRight we are able to provide many other useful operations,
// if not always the most efficient implementations, over the structure without further implementation.

// in order to support laziness, the signature of Foldable’s foldRight is
//  def foldRight[A, B](fa: F[A], lb: Eval[B])(f: (A, Eval[B]) => Eval[B]): Eval[B]
// as opposed to
//  def foldRight[A, B](fa: F[A], z: B)(f: (A, B) => B): B
//  // foldRight from the collections in Scala’s standard library might expect. This will prevent operations which are lazy in their right hand argument to traverse the entire structure unnecessarily
val allFalse = Stream.continually(false)
// If you wanted to reduce this to a single false value using the logical and (&&).
// Using foldRight from the standard library will try to consider the entire stream, and thus will eventually cause a stack overflow:
//try {
//  allFalse.foldRight(true)(_ && _)
//} catch {
//  case e: StackOverflowError => println(e)
//}

Foldable[Stream]
  .foldRight(allFalse, Eval.True)((f, e) => if (f) e else Eval.False)
  .value
