// - SemigroupK has a very similar structure to Semigroup,
// - SemigroupK operates on type constructors of one argument

// Whereas you can find a Semigroup for types which are fully specified
// like Int or List[Int] or Option[Int], you will find SemigroupK for type constructors like List and Option
// These types are type constructors in that you can think of them as “functions” in the type space. You can think of the List type as a function which takes a concrete type, like Int, and returns a concrete type: List[Int]. This pattern would also be referred to having kind: * -> *, whereas Int would have kind * and Map would have kind *,* -> *, and, in fact, the K in SemigroupK stands for Kind.
import cats._
import cats.implicits._

SemigroupK[List].combineK(List(1, 2, 3), List(4, 5, 6)) ==
  Semigroup[List[Int]].combine(List(1, 2, 3), List(4, 5, 6))

// However for Option, the Semigroup’s combine and the SemigroupK’s combineK operation differ
// Since Semigroup operates on fully specified types, a Semigroup[Option[A]] knows the concrete type of A
// and will use Semigroup[A].combine to combine the inner As.
// Consequently, Semigroup[Option[A]].combine requires an implicit Semigroup[A].
// SemigroupK[Option] operates on Option where the inner type is not fully specified and can be anything (i.e. is “universally quantified”)
// In the case of Option the SemigroupK[Option].combineK method has no choice but to use the orElse method of Option:

Semigroup[Option[Int]].combine(Some(1), Some(2))
Semigroup[Option[Int]].combine(Some(1), None)
Semigroup[Option[Int]].combine(None, Some(2))

SemigroupK[Option].combineK(Some(1), Some(2))
SemigroupK[Option].combineK(Some(1), None)
SemigroupK[Option].combineK(None, Some(2))
// orElse

// |+| is the operator from semigroup
// <+> is the operator from SemigroupK (called Plus in scalaz)
import cats.implicits._
val one = Option(1)
val two = Option(2)
val n: Option[Int] = None
one |+| two
one <+> two

n |+| two // Some(3)
n <+> two // Some(1)
n |+| n
n <+> n
two |+| n
two <+> n
// SemigroupK type class instances is defined for Option, not Some or None
//Some(1) <+> None
None <+> Some(1)
