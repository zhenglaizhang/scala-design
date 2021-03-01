// MonoidK
// MonoidK is a universal monoid which operates on type constructors of one argument.
// This type class is useful when its type parameter F[_] has a structure that can be combined for any particular type, and which also has an “empty” representation. Thus, MonoidK is like a Monoid for type constructors (i.e. parametrized types).
// A MonoidK[F] can produce a Monoid[F[A]] for any type A.

// Here’s how to distinguish Monoid and MonoidK:
//  - Monoid[A] allows A values to be combined, and also means there is an “empty” A value that functions as an
//  identity.
//  - MonoidK[F] allows two F[A] values to be combined, for any A. It also means that for any A, there is an “empty”
//  F[A] value. The combination operation and empty value just depend on the structure of F, but not on the structure of
//  A.
import cats.{Monoid, MonoidK}
import cats.implicits._
Monoid[List[String]].empty
MonoidK[List].empty[String]
MonoidK[List].empty[Int]
// And instead of combine, it has combineK, which also takes one type parameter:
Monoid[List[String]].combine(List("hello", "world"), List("goodbye", "moon"))
MonoidK[List].combineK[String](List("hello", "world"), List("goodbye", "moon"))
MonoidK[List].combineK[Int](List(1, 2), List(3, 4))

// Actually the type parameter can usually be inferred:
MonoidK[List].combineK(List("hello", "world"), List("goodbye", "moon"))
MonoidK[List].combineK(List(1, 2), List(3, 4))

// MonoidK extends SemigroupK, so take a look at the SemigroupK documentation for more examples.
