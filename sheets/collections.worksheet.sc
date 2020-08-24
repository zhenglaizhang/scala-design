import scala.collection.LinearSeq
import scala.collection.mutable
import scala.collection.SortedSet
val empty: List[Nothing] = List()
val x: List[String] = empty

val nums = 1 :: 2 :: 3 :: Nil
1 :: (2 :: (3 :: Nil))
nums.head
nums.headOption
nums.tail
nums.isEmpty

val List(n1, n2, n3) = nums
val a :: rest = nums
println(rest)

def append[T](xs: List[T], ys: List[T]): List[T] =
  xs match {
    case Nil      => ys
    case x :: xs1 => x :: append(xs1, ys)
  }

append(List(1, 2, 3), List(4, 5))

val xs = List("a", "b", "c")
xs.indices zip xs
xs.zipWithIndex

for (
  i <- List.range(1, 5);
  j <- List.range(1, i)
) yield (i, j)

xs find (_ == "b")

def hasZeroRow(m: List[List[Int]]) =
  m exists (row => row forall (_ == 0))

def sum(xs: List[Int]): Int = (0 /: xs)(_ + _)

def product(xs: List[Int]): Int = xs.foldLeft(1)(_ * _)

List.fill(2)('a')

List.tabulate(4)(n => n * n)

List.tabulate(5, 5)(_ * _)

List.concat()

List.concat(List(1), List(2), List(3))

(List("abc", "de"), List(3, 2)).zipped.forall(_.length == _)

def notImplemented = ???

import scala.collection.mutable.ListBuffer
val buf = new ListBuffer[Int]
buf += 1
buf += 2
3 +=: buf
buf.toList

class Time {
  var hour = 12
  var minute = 0
}

class GenerateddTime {
  private[this] var h = 12
  private[this] var m = 0

  def hour: Int = h
  def hour_=(x: Int) = { h = x }

  def minute: Int = m
  def minute_=(x: Int) = { m = x }

}

class SlowAppendQueue[T](elems: List[T]) {
  def head = elems.head
  def tail = new SlowAppendQueue(elems.tail)
  def enqueue(x: T) = new SlowAppendQueue(elems ::: List(x))
}

// type constructor
// generic trait
trait Queue[+T] {
  def head: T
  def tail: Queue[T]
  def enqueue[U >: T](x: U): Queue[U]
}

object Queue {
  def apply[T](xs: T*): Queue[T] = new QueueImpl(xs.toList, Nil)

  private class QueueImpl[T](
      private val leading: List[T],
      private val trailing: List[T]
  ) extends Queue[T] {
    override def head: T = ???
    override def tail: Queue[T] = ???
    override def enqueue[U >: T](x: U): Queue[U] = ???
  }
}

// Scala array in invariant (rigid)
val a1: Array[String] = Array("abc")
// val a2: Array[AnyRef] = a1
val a3 = a1.asInstanceOf[Array[AnyRef]]

// contravariant output channel
trait OutputChannel[-T] {
  def write(t: T)
}

var strOutput = new OutputChannel[String] {
  def write(t: String) = {}
}

var anyOutput = new OutputChannel[AnyRef] {
  def write(t: AnyRef): Unit = {}
}

strOutput.write("abc")

anyOutput.write("abc")
anyOutput.write(List(1, 2))
// anyOutput = strOutput
strOutput = anyOutput

def upperBounds[T <: Ordered[T]](xs: List[T]): List[T] =
  xs.sortWith((a, b) => a < b)

Traversable(1, 2, 3)
Iterable(1, 2, 3)
SortedSet("hello", "a")
mutable.Buffer(1, 2, 3)
LinearSeq(1, 2, 3)

List(1, 2) ++ List(3, 4)
List(1, 2) ++: List(3, 4)

val xs1 = List(1, 2, 3, 4, 5)
val git = xs1 grouped 3
git.next()
git.next()
val sit = xs1 sliding 3
sit.next()
sit.next()
sit.next()
// sit.next() // NoSuchElementError

1 to 14 by 4
1 until 3
