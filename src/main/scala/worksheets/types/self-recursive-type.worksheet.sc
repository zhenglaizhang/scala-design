// todo
// https://www.alessandrolacava.com/blog/scala-self-recursive-types/#:~:text=One%20of%20the%20advantages%20of,
// constraint%20to%20your%20type%20definitions.

//
// self-recursive types, aka F-bounded polymorphic types
//  - you can use the type system to enforce some constraints

object OldBad {

  trait Doubler[T] {
    def double: T
  }

  case class Square(base: Double) extends Doubler[Square] {
    override def double: Square = Square(base * 2)
  }

  case class Person(firstname: String, lastname: String, age: Int)

  // compiler wont complain
  case class SquareBad(firstname: String, lastname: String, age: Int)
      extends Doubler[Person] {
    override def double: Person = Person(firstname, lastname, age * 2)
  }

}

trait Doubler[T <: Doubler[T]] {
  def double: T
}

case class Person(age: Int)

// type arguments [Person] do not conform to trait Doubler's type parameter bounds [T <: Doubler[T]]
//case class Square(base: Double) extends Doubler[Person] {
case class Square(base: Double) extends Doubler[Square] {
  override def double: Square = ???
}

//  If someone tries to extends Doubler with a type which doesn’t extend Doubler
//  in turn (hence self-recursive), do not compile it”

// Java enum
// abstract class Enum[E <: Enum[E]] extends Comparable[E] with Serializable {}

trait Doubler[T <: Doubler[T]] {
  def double: T
}

case class Square(base: Double) extends Doubler[Square] {
  override def double: Square = Square(base * 2)
}

// Look at the Apple definition, it extends Doubler[Square] instead of Doubler[Apple].
case class Apple(kind: String) extends Doubler[Square] {
  override def double: Square = Square(5)
}

// Sometimes this is what you want in which case the self-recursive type will do.
// In cases when you don’t want this to happen a self type can work this out
trait Doubler[T <: Doubler[T]] {
  self: T =>
  def double: T
}

// self-type Apple does not conform to Doubler[Square]'s selftype Square
//case class Apple(kind: String) extends Doubler[Square] {
case class Apple(kind: String) extends Doubler[Apple] {
  override def double: Apple = ???
}

// Generally you are not the only one working on a project and, anyway, a good rule of thumb is to design your
// software as if you’re designing a public API. In this case you want to be sure no one will use your API in the
// wrong way.
//
// Compilers are implemented by smart guys, generally. Having the compiler help by your side is always a good thing
// in my humble opinion.

// Are there alternatives to this type of problems? Yes indeed, Type Classes, which is by the way the option I prefer
// . But this is another story for a future post.
