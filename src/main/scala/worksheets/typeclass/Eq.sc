// Eq is an alternative to the standard Java equals method.
//    def eqv(x: A, y: A): Boolean
// In Scala it’s possible to compare any two values using == (which desugars to Java equals).
// This is because equals type signature uses Any (Java’s Object) to compare two values. This means that we can
// compare two completely unrelated types without getting a compiler error. The Scala compiler may warn us in some
// cases, but not all, which can lead to some weird bugs
32 == "hello"
"hello" == 12

// Ideally, Scala shouldn’t let us compare two types that can never be equal.

import cats.implicits._

1 === 1

//"1" === 1
//type mismatch;
//found   : Int(1)
//required: String

// How to easily write Eq instances for every data type
// One option is to use inbuilt helper functions.
// Another option is to use a small library called kittens,
// which can derive a lot of type class instances for our data types including Eq.

import cats.kernel.Eq
import cats.implicits._

case class Foo(a: Int, b: String)

// Eq.fromUniversalEquals only defers to ==
implicit val eqFoo: Eq[Foo] = Eq.fromUniversalEquals
Foo(100, "") === Foo(10, "")
Foo(100, "") =!= Foo(10, "")

//type mismatch;
//found   : String("hello")
//required: Foo
//Foo(1, "") === "hello"
