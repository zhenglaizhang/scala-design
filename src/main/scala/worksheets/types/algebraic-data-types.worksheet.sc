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
// A universally-quantified type defines a category (or kind) of types that are all parameterized by some arbitrary type. 
// In Scala, type constructors (such as some traits) and methods may be universally quantified, although Scala methods do not have a type (they appear in types such as traits).

// 
// Type Constructors
// - A type constructor is a universally quantified type, which can be used to construct types.

