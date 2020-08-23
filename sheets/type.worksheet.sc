import java.awt.event.ActionEvent
import java.awt.event.ActionListener
trait Abstract {
  type T
  def tansform(x: T): T
  val initial: T
  var current: T
}

trait RationalTrait {
  val number: Int
  val denom: Int
}

new RationalTrait {
  val number: Int = 1
  val denom: Int = 2
}

// pre-initialized fields
new {
  val number = 1
  val denom = 2
} with RationalTrait

object twoThirds extends {
  val number = 2
  val denom = 3
} with RationalTrait

object Demo {
  val x = {
    println("init x")
    "done"
  }
}
Demo
Demo.x

object LazyDemo {
  lazy val x = {
    println("lazy init")
    "done"
  }
}
LazyDemo
LazyDemo.x

class Food
abstract class Animal {
  type SuitableFood <: Food
  def eat(food: SuitableFood)
}

class Grass extends Food
class Cow extends Animal {
  type SuitableFood = Grass
  override def eat(food: SuitableFood): Unit = {}
}

val bessy = new Cow
bessy eat (new bessy.SuitableFood)
bessy eat (new Grass)
val meow = new Cow
meow eat (new bessy.SuitableFood)

type AnimalThatEatGrass = Animal { type SuitableFood = Grass }

class Pasture {
  var animals: List[Animal { type SuitableFood = Grass }] = Nil
}

object Color extends Enumeration {
  val Red, Green, Blue = Value
}
// path dependent type
import Color._

Red
Green
Blue

object Direction extends Enumeration {
  val North = Value("North")
  val East = Value("East")
}
for (d <- Direction.values)
  println(d + " ")

Direction.North.id
Direction.East.id
Direction(1)
// Direction(100)

val button = new java.awt.Button
button.addActionListener(new ActionListener {
  def actionPerformed(x: ActionEvent): Unit = ???
})
implicit def function2ActionListener(f: ActionEvent => Unit) =
  new ActionListener {
    def actionPerformed(x: ActionEvent): Unit = f(x)
  }
button.addActionListener(x => {})

case class Rectangle(width: Int, height: Int)
implicit class RectangleMaker(width: Int) {
  def x(height: Int) = Rectangle(width, height)
}

val rec = 12 x 3

// context bound
def max[T: Ordering](xs: List[T]): T = {
  // implicitly[Ordering[T]].compare()
  ???
}
