// Conceptually, all type classes come with laws
// These laws constrain implementations for a given type and can be exploited and used to reason about generic code.

// The Monoid type class requires that combine be associative and empty be an identity element for combine
//  - combine(x, combine(y, z)) = combine(combine(x, y), z)
//  - combine(x, id) = combine(id, x) = x

// Functions parametrized over a Monoid can leverage them for say, performance reasons.
// A function that collapses a List[A] into a single A can do so with foldLeft or foldRight since combine is assumed to be associative, or it can break apart the list into smaller lists and collapse in parallel,
import cats.Monoid
import cats.instances.int._
import cats.syntax.semigroup._

val xs = List(1, 2, 3, 4, 5)
val (l, r) = xs.splitAt(2)
val sumLeft = Monoid[Int].combineAll(l)
val sumRight = Monoid[Int].combineAll(r)
//val res = Monoid[Int].combine(sumLeft, sumRight)
val res = sumLeft.combine(sumRight)

// Cats provides laws for type classes via the kernel-laws and laws modules which makes law checking type class instances easy.
