import java.util.function.IntUnaryOperator
import java.{util => ju}
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

sealed abstract class Expr
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

def describe(e: Expr): String =
  (e: @unchecked) match {
    case Number(_) => "number"
    case Var(_)    => "var"
  }

def show(x: Option[String]) =
  x match {
    case Some(s) => s
    case None    => "?"
  }

show(Some("a"))
show(None)

// patterns everywhere
val t = 123 -> "abc"
val (number, str) = t

// partial function
val withDefault: Option[Int] => Int = {
  case Some(x) => x
  case None    => 0
}

withDefault(Some(1))
withDefault(None)

val second: PartialFunction[List[Int], Int] = {
  case x :: y :: _ => y
}

second.isDefinedAt(Nil)

for ((a, b) <- Map(1 -> 2))
  println(s"$a -> $b")

for (Some(fruit) <- List(Some("apple"), None)) println(fruit)

// SAM
trait Increaser {
  def increase(i: Int): Int
}

def increaseOne(increaser: Increaser): Int = increaser.increase(1)

increaseOne(
  new Increaser {
    def increase(i: Int): Int = i + 1
  }
)

// use functional literal, no need to convert to Increaser
increaseOne(_ + 1)

val stream = ju.Arrays
  .stream(Array(1, 2, 3))

stream.map(new IntUnaryOperator {
  def applyAsInt(x: Int): Int = x + 1
})

ju.Arrays.stream(Array(1, 2, 3)).map(i => i + 1).toArray

val f = (i: Int) => i + 1
// type mismatch
// only functional literals will be adapted to SAM types, not arbitary expressions that have a function type
// ju.Arrays.stream(Array(1, 2, 3)).map(f)
ju.Arrays.stream(Array(1, 2, 3)).map(i => f(i))
val g: IntUnaryOperator = (i: Int) => i + 1
ju.Arrays.stream(Array(1, 2, 3)).map(g).toArray

abstract class SetAndType {
  type Elem
  val set: mutable.Set[Elem]
}

def javaSet2ScalaSet[T](jset: ju.Collection[T]): SetAndType = {
  val sset = mutable.Set.empty[T] // now T can be named
  val iter = jset.iterator
  while (iter.hasNext()) {
    sset += iter.next()
  }

  return new SetAndType {
    type Elem = T
    val set = sset
  }
}
