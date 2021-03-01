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

import java.{util => ju}
// programmer error => comparing Int to Option[Int]
// not a type error technically
List(1, 2, 3).map(Option(_)).filter((it => it == 1))

//import cats.Eq
import cats.instances.int._

val eqInt = Eq[Int]
eqInt.eqv(123, 234)

// eqInt.eqv(123, Some(12)) // type mismatch compilation error

import cats.syntax.eq._
123 === 123
123 =!= 234

import cats.instances.option._
// Some(1) === None
(Some(1): Option[Int]) === (None: Option[Int])
Option(1) === Option.empty[Int]

import cats.syntax.option._
import cats.instances.long._
1.some === none[Int]
1.some =!= none[Int]

implicit val dateEq: Eq[ju.Date] = Eq.instance[ju.Date] { (d1, d2) =>
  d1.getTime === d2.getTime
}

val x = new ju.Date()
val y = new ju.Date()
x === y
x === x

case class Cat(name: String, age: Int, color: String)
import cats.instances.string._
implicit val catEq: Eq[Cat] = Eq.instance { (c1, c2) =>
  c1.name === c2.name && c1.age === c2.age && c1.color === c2.color
}

Option(Cat("name", 1, "red")) === Option.empty[Cat]

// - Number => + String 12.0 => ""
// - Int => + String 12 => ""
