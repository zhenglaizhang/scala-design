// todo https://gist.github.com/jdegoes/97459c0045f373f4eaf126998d8f65dc

//
// function definition 
//

// A function is a mapping from one set, called a domain, to another set, called the codomain. 
// A function associates every element in the domain with exactly one element in the codomain. 
// In Scala, both domain and codomain are types.
val square: Int => Int = x => x * x
square(2)

//
// Higher-Order Functions
//  - A higher-order function is a function that accepts or returns a function.

trait List[A] {
  // List[A].filter is a higher-order function that accepts the function A => Boolean
  def filter(f: A => Boolean): List[A]
}


// 
// Combinators
// 

class ConfigReader {
  def readString(name: String): String = ???
}

type Conf[A] = ConfigReader => A 
def string(name: String): Conf[String] = _.readString(name)
def both[A, B](left: Conf[A], right: Conf[B]): Conf[(A, B)] = c => (left(c), right(c))
// both is a combinator that takes two functions and returns a function.

// 
// Polymorphic Functions
// 
// A polymorphic function is one that is universally quantified over one or more type parameters.  
// You can apply that function to many types of values, it is a polymorphic function
// Scala has no support for polymorphic functions, but they can be emulated via polymorphic methods on traits. 
// A trait modeling a polymorphic function usually has a single method called apply, so it can be applied with ordinary function application syntax.
case object identity {
  // This emulates a polymorphic function called id, which accepts one type parameter A, and a value of type A, and returns that same value.
  def apply[A](a: A): A = a
}
identity(3)
identity("3")