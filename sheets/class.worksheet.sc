class A {
  private var sum = 0
  def value = sum;
}
object A {
  def get(init: Int = 0) = {
    val a = new A
    a.sum = init
    a
  }
}
new A

// side effect => procedure

1
+2

(1
  + 2)

1 +
  2 +
  3

A.get(1).value
A.get().value

// java.lang
// scala
// scala.Predef
Predef.println(12)
Console println 12

sealed trait Animal {
  def speak(): Unit
}

private class Dog extends Animal {
  override def speak(): Unit = {}
}

private class Cat extends Animal {
  override def speak(): Unit = {}
}

object Animal {
  def apply(s: String): Animal = if (s == "dog") new Dog else new Cat
}

Animal("dog")

// compiler error
// new Dog

case class Person(val name: String, val age: Int)

// add more auxiliary factory methods
object Person {
  def apply() = new Person("<no name>", 0)
  def apply(name: String) = new Person(name, 0)
}

Person("test", 1)
Person()
Person("meow")

// a class with private constructor
class PrivatePerson private (name: String)

class Meow(private var _name: String) {
  // private field
  // any Meow instance can access
  private val age: Int = 0;

  // object-private field
  // only current instance can access
  private[this] val wow: Int = 0;

  // name()
  def name = _name

  // name_$eq()
  def name_=(name: String) { _name = name }
}

val x = new Meow("wow")
x.name
x.name_=("mm")
x.name
x.name = "wow"
x.name


abstract class AP(var name: String, var age: Int) 
final class P(var name: String, var age: Int)
case class ImmutablePerson(name: String, age: Int)

def m(seq: Seq[Int]): Unit = println(s"Seq[Int]: $seq")
// type after erasure: def m(seq: Seq)...
// def m(seq: Seq[String]): Unit = println(s"Seq[String]: $seq")

// procedure syntax
// inferred Unit 
def foo() { "a" }

// function syntax
def bar() = { "a" }

foo()
bar()


