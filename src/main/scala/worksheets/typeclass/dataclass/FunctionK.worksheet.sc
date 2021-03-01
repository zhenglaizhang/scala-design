import cats.arrow.FunctionK
// FUnctionK
// A FunctionK transforms values from one first-order-kinded type (a type that takes a single type parameter, such as List or Option) into another first-order-kinded type. This transformation is universal, meaning that a FunctionK[List, Option] will translate all List[A] values into an Option[A] value for all possible types of A

// Ordinal Functions
def first(xs: List[Int]): Option[Int] = xs.headOption // method
val first: List[Int] => Option[Int] = xs =>xs.headOption // function value

// => is really just some syntactic sugar for Function1
val first1: Function1[List[Int], Option[Int]] = xs => xs.headOption

trait MyFunction1[A, B] {
  def apply(a: A): B
}

val first2: Function1[List[Int], Option[Int]] = new Function1[List[Int], Option[Int]] {
  def apply(xs: List[Int]): Option[Int] = xs.headOption
}

// Abstracting via Generics
def first3[A](xs: List[A]): Option[A] = xs.headOption
// We are looking for something like a type of List[A] => Option[A] forAll A, but this isn’t valid scala syntax. Function1 isn’t quite the right fit, because its apply method doesn’t take a generic type parameter.

trait MyFunctionK[F[_], G[_]] {
  def apply[A](fa: F[A]): G[A]
}
// Cats provides this type as FunctionK
val first4: FunctionK[List, Option] = new FunctionK[List, Option] {
  def apply[A](fa: List[A]): Option[A] = fa.headOption
}


// Syntactic Sugar
val first5: FunctionK[List, Option] = λ[FunctionK[List, Option]](_.headOption)

import cats.~>
val first6: List ~> Option = λ[List ~> Option](_.headOption)
// Being able to use ~> as an alias for FunctionK parallels being able to use => as an alias for Function1.

// Use cases
// FunctionK tends to show up when there is abstraction over higher-kinds. For example, interpreters for free monads and free applicatives are represented as FunctionK instances.


// Types with more than one type parameter
// Earlier it was mentioned that FunctionK operates on first-order-kinded types (types that take a single type parameter such as List or Option).
type ErrorOr[A] = Either[String, A]
val errorOrFirst: FunctionK[List, ErrorOr] =  λ[FunctionK[List, ErrorOr]](_.headOption.toRight("ERROR: the list was empty!"))

// Natural Transformation
// In category theory, a natural transformation provides a morphism between Functors while preserving the internal structure. It’s one of the most fundamental notions of category theory.
// If we have two Functors F and G, FunctionK[F, G] is a natural transformation via parametricity. That is, given fk: FunctionK[F, G], for all functions A => B and all fa: F[A] the following are equivalent:
//    fk(F.map(fa)(f)) <-> G.map(fk(fa))(f)
// We don’t need to write a law to test the implementation of the fk for the above to be true. It’s automatically given by parametricity.
// Thus natural transformation can be implemented in terms of FunctionK. This is why a parametric polymorphic function FunctionK[F, G] is sometimes referred as a natural transformation. However, they are two different concepts that are not isomorphic.
// For more details, Bartosz Milewski has written a great blog post titled “Parametricity: Money for Nothing and Theorems for Free”.