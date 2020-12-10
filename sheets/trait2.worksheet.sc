// https://alvinalexander.com/scala/all-wanted-to-know-about-def-val-var-fields-in-traits/
// https://www.artima.com/scalazine/articles/stackable_trait_pattern.html

trait Pet {
  def speak() = println("Yo") // concrete implementation
  def comeToMaster(): Unit // abstract method
}

trait HasLegs {
  def legsNum: Int
  val a = 1
  def run() = println("Im running")
}

trait HasTail {
  def startTail(): Unit
  def stopTail(): Unit
}

trait HasRubberyNose

trait SentientBeing {
  def imAlive_!(): Unit
}

trait Furry extends SentientBeing

abstract class Dog extends HasLegs with HasTail with HasRubberyNose {
  override val legsNum = 4
}

class Cat extends HasLegs {

  override lazy val legsNum: Int = {
    Thread.sleep(1000)
    4
  }

}

// https://stackoverflow.com/questions/1392862/scala-overriding-a-non-abstract-def-with-a-var/1393389
class Bird extends HasLegs {
  var legsNum = 5
  // override var legsNum = 5
}

class A extends HasLegs {
  override def legsNum: Int = {
    4
  }
}

trait First { println("first") }
trait Second { println("second") }
trait Third { println("third") }
class My extends First with Second with Third {
  println("my")
}

new My


trait Employee
class CorporateEmployee extends Employee 
class StoreEmployee extends Employee
// trait extends class
trait DeliversFood extends StoreEmployee

class DeliveryPerson extends StoreEmployee with DeliversFood
// Compiler error
// class Receptionist extends CorporateEmployee with DeliversFood

// Scala 3
// trait Pet(val name: String)
// trait Feathered extends Pet



// early initializer
trait Pet1 {
  def name: String
  val nameLength = name.length
}

class Dog1 extends {
  // ensure name is initialized early, so nameLength wont throw NPE
  val name = "test"
} with Pet1

class Dog2 extends Pet1 {
  val name: String = "testbd"
}

val d = new Dog1
d.name 
d.nameLength

// NPE
// val d2 = new Dog2
// d2.name
// d2.nameLength

trait Stringify[A] { // type parameter
  def string(a: A): String = "value: " + a.toString
}

trait Stringify2 {
  type A  // type member
  def string(a: A): String
}

object Stringify extends Stringify[Int]
Stringify.string(12)

object Stringify2 extends Stringify2 {
  type A = Int
  def string(a: Int): String = "value: " + a.toString
}

println(Stringify2.string(42))

trait Pair[A, B] {
  def getKey: A
  def getValue: B
}