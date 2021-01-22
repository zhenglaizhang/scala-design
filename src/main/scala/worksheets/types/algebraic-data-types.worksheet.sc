// todo
// https://gist.github.com/jdegoes/97459c0045f373f4eaf126998d8f65dc

// An algebraic data type is a type formed by composing product and sum types.

//
// Product Type
//  - Product types are defined by a Cartesian cross product on 2 or more types.

type Point2D = (Int, Int)
// A two-dimensional point is a product of a number and a number; each value has both an x-coordinate and a y-coordinate.

// In Scala, case classes are the idiomatic representation of product types.
// The terms of a case class are identified by name.
case class Person(name: String, age: Int)

//
// Sum Type
// - Sum types are defined by a disjoint union on 2 or more types.
//
trait Error

trait HttpResponse
type RequestResult = Either[Error, HttpResponse]

// An request result is a sum of Error and HttpResponse;
// each value is either an error or an HTTP response (but not both).

// Sealed Traits
// In Scala, sealed traits are the idiomatic representation of sum types (pre-Dotty).
// The terms of a sum type are identified by constructor / deconstructor (and, incidentally, by subtype).
sealed trait AddressType
case object Home extends AddressType
case object Business extends AddressType
// An AddressType is either a Home or a Business, but not both.

// Subtyping
// Supertyping

// Universals
// A universally-quantified type defines a category (or kind) of types that are all parameterized by some arbitrary
// type.
// In Scala, type constructors (such as some traits) and methods may be universally quantified, although Scala
// methods do not have a type (they appear in types such as traits).

//
// Type Constructors
// - A type constructor is a universally quantified type, which can be used to construct types.
sealed trait MyList[A]

case object Nil extends MyList[Nothing]

case class Cons[A](head: A, tail: MyList[A]) extends MyList[A]

// List is type constructor, which defines a family of List-like types
// List is said to be universally quantified over its type variable A.

//
// Higher-Kinded Types
//  - Type-Level Functions
//  - Type constructors can be thought of as type-level functions,
//    which accept types and return types. This interpretation is useful when reasoning about higher-kinded types.
//    ist is a type-level function that accepts one type A (the type of its elements), and returns another type
//    List[A]. If you pass Boolean to List, you get back List[Boolean], the type of lists of boolean values.

//
// Kinds
//  - Kinds can be thought of as the type of types.
//  1. * — The kind of types (the set of all types).
//  2. * => * — The kind of type-level functions that accept 1 type (the set of all type-level functions that accept
//  1 type). The type constructor List has kind * => *, represented as _[_] in Scala.
//  3. [*, *] => * — The kind of type-level functions that accept 2 types (the set of all type-level functions that
//  accept 2 types). The type constructor Either has kind [*, *] => *, represented as _[_, _] in Scala.
//  4. Compare with the types of functions: A => B, (A, B) => C, (A, B, C) => D.

//
// Higher-Order Kinds
//
// Just like functions can be "higher-order", type constructors can be higher-order, too. Scala uses underscores to
// encode higher-order type constructors. The declaration trait CollectionModule[Collection[_]] specifies that
// CollectionModule's type constructor requires a type constructor of kind * -> *
// (* => *) => * — The kind of type constructors that accept a type constructor of kind * => *. For example, trait
// Functor[F[_]] { ... } and trait Monad[F[_]] { ... }

//
// Existentials
//  - An existentially quantified type defines a type that depends on some definite but unknowable type. Existential
//    types are useful for hiding type information that is not globally relevant.
trait ListMap[A] {
  type B
  val list: List[B]
  val mapf: B => A

  def run: List[A] = list.map(mapf)
}

// The type ListMap[A]#B is some definite type, but there is no way to know what that type is — it could be anything.

//
// Skolemization
//  - Every existential type can be encoded as a universal type. This process is called skolemization.
case class ListMap1[B, A](list: List[B], mapf: B => A)

trait ListMapInspector[A, Z] {
  def apply[B](v: ListMap1[B, A]): Z
}

case class AnyListMap[A]() {
  def apply[Z](v: ListMapInspector[A, Z]): Z = ???
}

// Example: Instead of using ListMap directly, we use AnyListMap, which allows us to inspect a ListMap but only if we
// can handle any type parameter for B.

//
// Type Lambdas
//  - Functions may be partially applied with the underscore operator; e.g. zip(a, _).
//  - A type lambda is a way to partially apply a higher-kinded type, which yields another type constructor with
//  fewer type parameters.
//  - Type lambdas are to type constructors as lambdas are to functions.
//  - Type constructors and functions are declarations,
//  - While lambdas are expressions (either value expressions, or type expressions).

//({type λ[α] = Either[String, α]})#λ
// This is the Either type, partially applied with a String as the first type parameter.
// In many (but not all) cases, you can use type aliases instead of type lambdas
type StringOr[A] = Either[String, A]

//
// Kind Projector
//  - Kind Projector is a common compiler plugin for Scala that provides easier syntax to create type lambdas. For
//  example, the type lambda ({type λ[α] = Either[String, α]})#λ can be represented with the syntax Either[String, ?]
//  . Other syntax can be used to create more complex type lambdas.
//    https://github.com/non/kind-projector
