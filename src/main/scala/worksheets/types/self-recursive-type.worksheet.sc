// todo
// https://www.alessandrolacava.com/blog/scala-self-recursive-types/#:~:text=One%20of%20the%20advantages%20of,
// constraint%20to%20your%20type%20definitions.

//
// self-recursive types, aka F-bounded polymorphic types
//  - you can use the type system to enforce some constraints

// I have a type hierarchy … how do I declare a supertype method that returns the “current” type?
//  - for any expression x with some type A <: Pet, ensure that x.renamed(...) also has type A. To be clear: this is
//  a static guarantee that we want, not a runtime property.
//  - Scala encourages immutability, so methods that return a modified copy of this are quite common
//  - F-bounded type, which mostly works
//  - A better strategy is to use a typeclass

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

trait Pet {
  def name: String

  def renamed(newName: String): Pet
}

case class Fish(name: String, age: Int) extends Pet {
  //  Return types are in covariant position; it’s always ok to return something more specific than what is promised
  def renamed(newName: String): Fish = copy(name = newName)
}

val a = Fish("Jimmy", 2)
val b = a.renamed("Bob")

// - Our trait doesn’t actually constrain the implementation very much;
//    We are simply required to return a Pet, not necessarily the same type of pet
// - We also run into problems trying to abstract over renaming.
//    def esquire[A <: Pet](a: A): A = a.renamed(a.name + ", Esq.")

// An F-bounded type is parameterized over its own subtypes,
// which allows us to “pass” the implementing type as an argument to the superclass.
trait Pet[A <: Pet[A]] {
  def name: String

  def renamed(newName: String): A
}

case class Fish(name: String, age: Int) extends Pet[Fish] {
  def renamed(newName: String) = copy(name = newName)
}

def esquire[A <: Pet[A]](a: A): A = a.renamed(a.name + ", Esq.")

// We still have a problem with lying about what the “current” type is;
// there is nothing forcing us to pass the correct type argument

//  A way to restrict the implementing class claiming to be an A to actually be an A. And it turns out that Scala
//  does give us a way to do that: a self-type annotation
trait Pet[A <: Pet[A]] {
  this: A =>
  def name: String

  def renamed(newName: String): A
}

//case class Kitty(name: String) extends Pet[Fish] {
//  def renamed(newName: String): Fish = new Fish(newName, 42)
//}

// We can still lie about the “current” type by extending another type that correctly meets the constraint. Subtyping
// has provided an unwanted loophole.
class Mammal(val name: String) extends Pet[Mammal] {
  def renamed(newName: String) = new Mammal(newName)
}

class Monkey(name: String) extends Mammal(name)

// Monkey is a Pet[Mammal]

// Also note that the clutter introduced by the type parameter on Pet doesn’t add any information; it’s purely a
// mechanism to restrict implementations

// As is often the case, we can avoid our subtyping-related problems by using a typeclass
trait Pet {
  def name: String
}

trait Rename[A] {
  def rename(a: A, newName: String): A
}

case class Fish(name: String, age: Int) extends Pet

object Fish {
  implicit val FishRename = new Rename[Fish] {
    override def rename(a: Fish, newName: String): Fish = a.copy(name = newName)
  }
}

// any Pet with a Rename intance will automatically gain a renamed method by implicit conversion
implicit class RenameOps[A](a: A)(implicit ev: Rename[A]) {
  def renamed(newName: String) = ev.rename(a, newName)
}

val a = Fish("fish", 12)
a.renamed("cat")
def esquire[A <: Pet: Rename](a: A): A = a.renamed(a.name + ", Esq.")
esquire(a)

// This is a general strategy. By identifying methods that require us to return the “current” type and moving them to
// a typeclass we can guarantee that our desired constraint is met. However it does have a bit of a
// smell: functionality is divided between trait and typeclass, and there is nothing requiring that all Pet
// implementations have a Rename instance (we had to specify both an upper bound and a context bound in esquire above).
// what if we abandon the super-trait altogether

// How about only a Typeclass?
trait Pet[A] {
  def name(a: A): String

  def renamed(a: A, newName: String): A
}

implicit class PetOps[A](a: A)(implicit ev: Pet[A]) {
  def name = ev.name(a)

  def renamed(newName: String): A = ev.renamed(a, newName)
}

case class Fish(name: String, age: Int)

object Fish {
  implicit val FishPet = new Pet[Fish] {
    override def name(a: Fish) = a.name

    override def renamed(a: Fish, newName: String): Fish =
      a.copy(name = newName)
  }
}

Fish("Bob", 12).renamed("wow")
// ad-hoc and parametric polymorphism are really all we need in a programming language;
// we can get along just fine without subtyping

// Ok cool, I have an F-bounded type (or a typeclass) working,
// but I can’t figure out how to put a bunch of instances
// in a list without losing all my type information.

// A heterogeneous collection of pets

// http://tpolecat.github.io/2015/04/29/f-bounds.html
