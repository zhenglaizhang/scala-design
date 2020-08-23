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
