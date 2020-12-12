"foo".length
List(1, 2, 3).max

// EOP
val a = 0
val b = 2
val greeter = if (a > b) a else b

val i = 1
val evenOrOdd = i match {
  case 1 | 3 | 5 | 7 | 9  => "odd"
  case 2 | 4 | 6 | 8 | 10 => "even"
}

def toInt(s: String): Int = {
  try {
    s.toInt
  } catch {
    case _: Throwable => 0
  }
}

val x = List.range(1, 10)
x.filter(_ % 2 == 0)

x.foreach((i: Int) => println(i))
x.foreach((i) => println(i));
x.foreach(i => println(i))
x.foreach(println(_))
x.foreach(println)

val m = Map(1 -> 10, 2 -> 20, 3 -> 30)
m.transform((k, v) => k + v)
m.foreach(x => println(x._1 + "-->" + x._2))

val double = (i: Int) => i * 2
// val double2 = (i: Int): Int => i * 2
val double3: Int => Double = _ * 2.0
double(12)
x.map(double)

// implicit approach
val add = (x: Int, y: Int) => x + y
val add2: (Int, Int) => Int = (x, y) => x + y

// Eta expansion
def isEven(x: Int) = x % 2 == 0
List.range(1, 10, 2).filter(isEven)

// partially applied function
val isEvenF = isEven _
val c = scala.math.cos _
val c2 = scala.math.cos(_)

def sub(x: Int, y: Int) = x - y
val subF = sub(_, _)
val subF2 = sub _

val square = scala.math.pow(_, 2)
square(2)
square(5)

def exec(f: () => Unit, times: Int = 1) = { for (i <- 1 to times) f() }
val sayHelloF = () => println("hello")
def sayHelloM(): Unit = println("hello")
exec(sayHelloF)
exec(sayHelloM, 2)

def sum(a: Int, b: Int, c: Int) = a + b + c
val addTo3 = sum(1, 2, _)
addTo3(3)

def wrap(prefix: String, html: String, suffix: String) = s"$prefix$html$suffix"
val wrapWithDiv = wrap("<div>", _: String, "</div>")
wrapWithDiv("wow")

def saySth(prefix: String): String => String = (s: String) => s"$prefix $s"
val sayHello = saySth("Hello")
sayHello("wow")

val divide = new PartialFunction[Int, Int] {
  def apply(x: Int) = 10 / x
  def isDefinedAt(x: Int) = x != 0
}

divide.isDefinedAt(0)

val divide2: PartialFunction[Int, Int] = {
  case d if d != 0 => 42 / d
}

divide2.isDefinedAt(0)

trait PartialFunction2[-A, +B] extends (A => B)

val handle1: PartialFunction[Int, Int] = {
  case d if d == 1 => 1
}

val handle2: PartialFunction[Int, Int] = {
  case d if d == 2 => 2
}

val handle12 = handle1 orElse handle2
handle12(1)
handle12(2)

List.range(1, 10).collect {
  case i if i < 5 => i
}

List(Some(1), None, Some(2)) collect {
  case Some(i) => i
}

trait Animal
trait AnimalWithLegs
trait AnimalWithTail

case class Dog(name: String)
    extends Animal
    with AnimalWithLegs
    with AnimalWithTail

trait TailService[AnimalWithTail] {
  def wagTail(a: AnimalWithTail) = println(s"$a is wagging tail")
  def stopTail(a: AnimalWithTail) = println(s"$a is topping wagging")
}

trait AnimalWithLegsServices[AnimalWithLegs] {
  def walk(a: AnimalWithLegs) = println(s"$a is walking")
  def run(a: AnimalWithLegs) = println(s"$a is running")
  def stop(a: AnimalWithLegs) = println(s"$a is stopped")
}

trait DogServices[Dog] { def bark(d: Dog) = println(s"$d says ‘woof’") }

object DogServices
    extends DogServices[Dog]
    with AnimalWithLegsServices[Dog]
    with TailService[Dog]

val dog = new Dog("Dog")
import DogServices._
walk(dog)
wagTail(dog)
stopTail(dog)
