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

abstract class Expr
case class Var(name: String) extends Expr
case class Number(num: Double) extends Expr
case class UnOp(operator: String, arg: Expr) extends Expr
case class BinOp(operator: String, left: Expr, right: Expr) extends Expr
val v = Var("x")
val op = BinOp("+", Number(1), v)
op.copy(operator = "-")
UnOp("-", UnOp("-", null)) // double negation
BinOp("+", null, Number(0)) // null

def simplifyTop(expr: Expr): Expr =
  expr match {
    case UnOp("-", UnOp("-", e))         => e
    case UnOp("abs", e @ UnOp("abs", _)) => e
    case BinOp("+", e, Number(0))        => e
    case BinOp("*", e, Number(1))        => e
    case BinOp("+", x, y) if x == y      => BinOp("*", x, Number(2))
    case _                               => expr
  }

simplifyTop(UnOp("-", UnOp("-", Var("x"))))

val pi = math.Pi
math.E match {
  case pi => "strange? Pi=" + pi
  // case _  => "error"
}

math.E match {
  case `pi` => "a real pi"
  case _    => "not a real pi"
}

List(0, 1) match {
  case List(0, _, _) => println("fond 0")
  case List(1)       => "got"
  case List(0, _*)   => "start with 0, length is not 3"
  case Nil           => "nil"
  case _             => "others"
}

(1, 2) match {
  case (_, 2) => println("tuple end with 2")
  case _      => "boom"
}

def generalSize(x: Any) =
  x match {
    case s: String    => s.length
    case m: Map[_, _] => m.size
    case _            => -1
  }

generalSize("abc")
generalSize(1)
generalSize(Map(1 -> 1))

// type erasure
def isIntIntMap(x: Any) =
  x match {
    case m: Map[Int, Int] => "mapint"
    case a: Array[String] => "str array"
    case _                => "boom"
  }
isIntIntMap(Map("abc" -> "abc"))
isIntIntMap(Array(1))
isIntIntMap(Array("1"))
