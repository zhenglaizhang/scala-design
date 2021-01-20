import akka.actor.TypedActor.PostStop
import scala.util.Random
//
// Projecting Nested Classes
//

class Foo {
  class Bar
}

val foo1 = new Foo
val bar1: foo1.Bar = new foo1.Bar

val foo2 = new Foo
val bar2: foo2.Bar = new foo2.Bar

val b = true

// This broadly corresponds to the syntax used for referring to nested classes in Java, where the syntax is just Foo.Bar. Scala reserves the dot operator for accessing the members of values, as in foo1.Bar
val bar: Foo#Bar = if (b) bar1 else bar2

// thanks to Scala’s path-dependent types, foo1.Bar is actually a different type than foo2.Bar, although they are both subtypes of Foo#Bar

//
// Projecting Other Nested Types
//

// in Scala, one can define type members (i.e., type aliases) inside classes, and these type members can be left abstract and only be given a concrete definition in subclasses.
abstract class Meow {
  type Bar // an abstract type
  def process(x: Bar): Bar
}

class FootInt extends Meow {
  type Bar = Int
  def process(x: Int): Int = x + 1
}

class FooString extends Meow {
  type Bar = String
  def process(x: String): String = s"Hello, $x!"
}

// We can still refer to these abstract type members from the outside, either through a proper value (using the dot operator) or through a type projection
val meow1 = new FootInt
val meow2 = new FooString

// Referring to the abstract type through value 'meow':
def test(meow: Meow)(bar: meow.Bar) = {
  val res = meow.process(bar)
  println(s"result of process is $res")
  res
}
val f1 = test(meow1)(31)
val f2 = test(meow2)("test")

// Referring to the abstract type through a projection:
val tru = Random.nextInt() > 0
val barz: Meow#Bar = if (tru) f1 else f2

// In the above, f1 has type meow1.Bar, but the compiler knows that meow1.Bar == FooInt#Bar == Int,
// so we can use it in expressions like f1 + 1, which has type Int.
// So far so good. But notice that we cannot do anything with a value of type Foo#Bar, since it could really be anything. That does not make projecting such types useless in general

barz.getClass

//
// Type Projection to Encode Open Type Families
//

// The reason type projection is already useful in this limited context is that we can use it on abstract prefix types. For example, we can write F#Bar where F is some abstract type that is known to be a subtype of Foo. When F is later resolved to be, for example, FooInt, then F#Bar will be resolved to FooInt#Bar, which is an alias of Int.
// This is akin to what is called “open type families” in languages like Haskell
abstract class Collection[E] {
  type Index
  def get(ix: Index): Option[E]
  def indices: Iterator[Index]
}

abstract class Sequence[E] extends Collection[E] {
  type Index = Int
}

abstract class Mapping[K, V] extends Collection[V] {
  type Index = K
}

// It makes sense to use a type member for Index, as opposed to a type parameter, because the Index is completely determined by the collection type. Having it as a type parameter to Collection would needlessly complicate its interface.
case class ArraySequence[E](underlying: Array[E]) extends Sequence[E] {
  def get(ix: Index): Option[E] =
    if (0 <= ix && ix < underlying.length) Some(underlying(ix))
    else None

  def indices: Iterator[Int] = Iterator.range(0, underlying.length - 1)
}

def head[A](col: Collection[A]): Option[A] = {
  // path dependant type
  val ite: Iterator[col.Index] = col.indices
  if (ite.hasNext) col.get(ite.next())
  else None
}
// But path-dependent types are sometimes too restrictive: they only work when the abstract type we manipulate live in a single, specific instance.
abstract class Matrix[Cols <: Collection[Double]] {
  // using the Cols{type Index = Cols#Index} type refinement
  type Row = Cols { type Index = Cols#Index }
  val rows: Collection[Row]
  type Position = (rows.Index, Cols#Index)
  def get(p: Position): Option[Double] =
    rows.get(p._1).flatMap(r => r.get(p._2))
}

case class DenseMatrix(rows: Sequence[Sequence[Double]])
    extends Matrix[Sequence[Double]]

type SparseArr[E] = Mapping[BigInt, E]
case class SparseMatrix(rows: SparseArr[SparseArr[Double]])
    extends Matrix[SparseArr[Double]]

// we can also use type class, which is preferred way to do so
// Type classes have the advantage that they decouple the types they talk about from the abstractions used to talk about them. For example, with a Collection type class, we could add static Index information “after the fact”, to types that were already defined without extending some Collection base class.
// However, the type-class-based approach comes with its own tradeoffs: it require passing along implicit parameters everywhere, which can be cumbersome. In contrast, type projection can remain entirely at the type level, without necessitating the presence of values in scope.
// Moreover, in Scala, there is no way to make sure that type class instances are consistent: the same type can be given incompatible instances in different parts of an application, which can sometimes break some invariants. This problem does not exist with type projections.
// Implicit resolution, on which type classes rely, is also a tricky matter and can become problematic when types become more involved, due to compiler limitations. The process of implicit search has restrictions to prevent divergence, which can limit expressiveness. On the other hand, type projections are actually quite reliable and also uniquely expressive, as we shall see now.

//
// Type projection is very expressive
//
// it turns out that type projection makes Scala’s type system Turing Complete.
// In fact, most practical languages turn out to have Turing Complete (and thus undecidable) type systems, such as C++, Rust, Haskell, and yes, even Java!
// This means that Scala’s type system is undecidable: however complicated we make our type checker implementation trying to follow the Scala specification, it will never be complete (there will always be programs on which it crashes or never terminates).

//
// Soundness
//

// Projecting Bounded Abstract Types

// upper bounded abstract type
abstract class Wow {
  type Self <: Wow // upper bound - an abstract type that's a subtype of Foo
}
final class WowA extends Wow {
  type Self = WowA
}

final class WowB extends Wow {
  type Self = WowB
}

// When we hold onto a value of type Foo#Self, we can use it anywhere a value of type Foo is expected, since we know Foo#Self is a subtype of Foo. Indeed, no matter which instance of Foo was used to instantiate the value, we know that its Self type member has to be a subtype of Foo.

// Lower-Bounded Abstract Types
class FooA extends Wow {
  type Self = FooB // BOOM
}

class FooB extends Wow {
  type Self = FooA // BOOM
}

// the singleton type this.type, which in Scala represents the type of the current class instance. Note that .type denotes the type of a specific value, and so for example a.type and b.type will never be the same unless a and b are known to be the same value.
abstract class WowFixed {
  type Self >: this.type <: WowFixed
}

class FooC extends WowFixed {
  type Self = FooC
}

class FooD extends WowFixed {
  type Self = FooD
}

val wow1 = new FooC
val wow2: wow1.Self =
  wow1 //  works because foo1.type is a subtype of foo1.Self == FooA
val wow3: WowFixed#Self = wow1 // works with type projection

// This way, it becomes possible to type check the following program, for example:
def baz(foo: WowFixed): foo.Self = foo // works as foo.type <: foo.Self

//
// Type Projection Is Unsound
//
// The trouble is that Scala’s type language is expressive enough to talk about abstract types with bounds that may not make sense (also called bad bounds).
class Bad {
  // type A >: String <: Int // bounds do not confirm
}

// However, such bad bounds can arise indirectly because of a feature called type intersection, which combines two types using the with operator
// A with B represents the type of values which inherits both from A and from B. Such type is both a subtype of A and a subtype of B. If we view types as sets of values, A with B (or A & B) can be understood as the set intersection of A and B
trait BadLower { type A >: Any }
trait BadUpper { type A <: Nothing }
// val x = new BadLower with BadUpper

// Bad Bounds are Fine… Usually
// Such object types are impossible to instantiate (they are not “realizable”). Indeed, Scala’s type checker can make sure, every time we create an object, that it does not contain bad bounds, so it will always prevent us from creating objects of types similar to BadLower with BadUpper.
// This means that every time we have a value of some type in scope, we can trust its bounds. Essentially, Scala’s core type system treats values in scopes as proofs that some bounds are correct (proofs that the lower bounds are indeed subtypes of the corresponding upper bounds). So we can use path-dependent types like foo.A without worrying, because they are rooted in values.
// However, type projection does not follow this safety precaution!

// Since type projection applies on types and not values, and since we can make up types with bad bounds, it follows that we should not be able to fully trust the bounds that we obtain from type projection.
// But that is exactly what the Scala 2 compiler does…
def oops0[T <: BadLower]: Any => T#A = a => a
def oops1[T <: BadUpper]: T#A => Nothing = a => a
def oops[A <: BadLower with BadUpper]: Any => Nothing =
  oops0[A].andThen(oops1[A])
// We define two functions oops0 and oops1 which independently leverage the information present in the bounds of BadLower and BadUpper, and we compose these two functions together into a function oops which… can convert anything into nothing, an obviously nonsensical function. Yet, Scalac type checks this code without complaining.
// We can then make our program crash at runtime by calling (oops("hi"): Int) + 1, which results in a ClassCastException from the Java Virtual Machine.
// oops("hi")

// It allows us to refer to type members belonging to some owner type, without necessarily having a value of the owner type in scope.
// Due to the risk of bad bounds, it is not safe to trust the bounds of a type projection, but that is exactly what the current Scala compiler does. This can result in runtime crash, meaning that the Scala 2 type system is unsound.
// For this reason, Dotty (the future Scala 3 compiler) currently rejects any type projection based on a type that is not a class type. For example, it rejects T#A if T is a type parameter.
// However, I am convinced that type projection is completely harmless as long as we disregard the lower bound of the projected type. This should allow us to retain virtually all of today’s uses of type projection (I have never seen type projection lower bounds used in the wild — apart from examples designed to show their unsoundness). But this will be the subject of a future article, where I will properly explain why I think it works.
