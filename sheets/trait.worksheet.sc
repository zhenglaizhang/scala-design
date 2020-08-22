import scala.collection.mutable
// trait
// widen thin interface to rich ones
// define stackable modifications

trait Fly {
  def fly() = println("flying...")
}

class Bird extends Fly {
  override def toString(): String = "bird"
}

class Rational(val n: Int, val d: Int) extends Ordered[Rational] {
  override def compare(that: Rational): Int =
    (this.n * that.d) - (that.n * this.d)
}

abstract class IntQueue {
  def get(): Int
  def put(x: Int)
}

class BasicIntQueue extends IntQueue {
  private val buf = new mutable.ArrayBuffer[Int]

  override def get(): Int = buf.remove(0)

  override def put(x: Int): Unit = { buf += x }
}

val q = new BasicIntQueue
q.put(10)
q.put(1)
q.get
q.get

trait Doubling extends IntQueue {
  abstract override def put(x: Int) = { super.put(2 * x) }
}

trait Incrementing extends IntQueue {
  abstract override def put(x: Int) = { super.put(x + 1) }
}

trait Filtering extends IntQueue {
  abstract override def put(x: Int) = { if (x > 0) super.put(x) }
}

class MyQueue extends BasicIntQueue with Doubling
val mq = new MyQueue
mq.put(10)
mq.get

// trait other, right -> left
val queue = new BasicIntQueue with Incrementing with Filtering
queue.put(-1)
queue.put(10)
queue.put(1)
queue.get
queue.get

abstract class Fruit(
    val name: String,
    val color: String
)

object Fruits {
  object Apple extends Fruit("apple", "red")
}

def showFruit(fruit: Fruit) = {
  import fruit._
  println(name + "s are " + color)
}
showFruit(Fruits.Apple)

// implicit imports
// java.lang._
// scala._
// Predef._
