// todo https://gist.github.com/jdegoes/97459c0045f373f4eaf126998d8f65dc

// why FP
//  - pervasive concurrency,
//    - Immutability eliminates the hardest problem in concurrency, coordinating access to shared, mutable state.
//  - write data-centric (e.g., “Big Data”) applications.
//  - write bug-free applications.  from mathematics, that move us further in the direction of provably bug-free
//  programs.
//  - As a mixed-paradigm language, it doesn’t require the rules of functional programming to be followed, but it
//  recommends that you do so whenever possible.
//  - Functional programming is based on the rules of mathematics for the behavior of func‐ tions and values.
//    - In mathematics, functions have no side effects. => pure
//      - Purity simplifies designs by eliminating a lot of the defensive boilerplate required in object-oriented code.
//      - Hiding implementation details is still important for minimizing the API footprint.
//    - This obliviousness to the surrounding context provides referential transparency,
//    - A function that returns Unit can only perform side effects.
//    - A natural uniformity between values and functions, due to the way we can substitute one for the other.
//    - functions are first-class values in functional programming
//      - compose
//      - assign
//      - pass
//      - return
//    - higher-order function
//      -  Higher-order, pure functions are called combinators, because they compose together very well
//  - In functional programming, variables are immutable. values are immutable; using the term “value” as a synonym
//  for immutable instances.
//    - A paradox of immutability is that performance can actually be faster than with mutable data.
//    - Functional data structures minimize the overhead of making copies by sharing the unmodified parts of the data
//    structures between the two copies
//  - You can always represent state changes with new instances or new stack frames, i.e., calling functions and
//  returning values.
//    - calling functions & returning values
//    - We calculate factorials using recursion. Updates to the accumulator are pushed on the stack. We don’t modify
//    a running value in place.
//  -  The art is learning how to use mutation when you must in a deliberate, principled way. The rest of your code
//  should be as pure as possible.
//    - lazy evaluation, such as Scala’s Stream type.
//    - Scala uses eager or strict evaluation by default, but the advantage of lazy evaluation is the ability to
//    avoid work that won’t be necessary.
//    - There are many scenarios where lazy evaluation is less efficient and it is harder to predict the performance
//    of lazy evaluation.
//    - There’s a joke in the Haskell community that they have delayed success until the last possible minute.

//
// function definition
//

// A function is a mapping from one set, called a domain, to another set, called the codomain.
// A function associates every element in the domain with exactly one element in the codomain.
// In Scala, both domain and codomain are types.
val square: Int => Int = x => x * x
square(2)

// functional literals
(1 to 10) filter (_ % 2 == 0) map (_ * 2) reduce (_ * _)

// closures
var factor = 2
val multiplier = (i: Int) => i * factor
// Even though multiplier was an immutable function value, its behavior changed when factor changed.
// i, is a formal parameter to the function. Hence, it is bound to a new value each time multiplier is called.
// factor, is not a formal parameter, but a free variable, a reference to a variable in the enclosing scope. Hence,
// the compiler creates a closure that encompasses (or “closes over”) multiplier and the external context of the
// unbound variables that multipli er references, thereby binding those variables as well.
// the closure references factor and reads its current value each time.
def m2: Int => Int = {
  val factor = 2
  val multiplier = (i: Int) => i * factor
  multiplier
}

// The function returned by m2 is actually a closure that encapsulates a reference to factor.

//
// Lambda
//  - An anonymous (unnamed) function. I
// Closure
//  - A function, anonymous or named, that closes over its environment to bind variables in scope to free variables
//    within the function.

// In Scala, we typically just say anonymous function or function literal for lambdas and we don’t distinguish
// closures from other functions unless it’s important for the discussion.

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
def both[A, B](left: Conf[A], right: Conf[B]): Conf[(A, B)] =
  c => (left(c), right(c))
// both is a combinator that takes two functions and returns a function.

//
// Polymorphic Functions
//
// A polymorphic function is one that is universally quantified over one or more type parameters.
// You can apply that function to many types of values, it is a polymorphic function
// Scala has no support for polymorphic functions, but they can be emulated via polymorphic methods on traits.
// A trait modeling a polymorphic function usually has a single method called apply, so it can be applied with
// ordinary function application syntax.
case object identity {
  // This emulates a polymorphic function called id, which accepts one type parameter A, and a value of type A, and
  // returns that same value.
  def apply[A](a: A): A = a
}

identity(3)
identity("3")

// Polymorphic methods are also referred to as universally-quantified functions, because their domain is universally
// quantified over all types.

//
// method as function
//  - Despite the fact multiplier is now a method, we use it just like a function,
//    because it doesn't reference this. When a method is used where a function is required, we say that Scala lifts
//    the method to be a function
object Multiplier {
  var factor = 2

  def multiplier(i: Int) = i * factor
}

(1 to 10) filter (_ % 2 == 0) map Multiplier.multiplier reduce (_ * _)
Multiplier.factor = 3
(1 to 10) filter (_ % 2 == 0) map Multiplier.multiplier reduce (_ * _)
