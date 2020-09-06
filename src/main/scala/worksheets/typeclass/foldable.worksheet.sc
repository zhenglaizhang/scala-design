def show[A](xs: List[A]): String =
  xs.foldLeft("nil")((acc, x) => s"$x then $acc")

show(List(1, 2, 3, 4, 5))

List(1, 2, 3).foldLeft(0)(_ - _)
List(1, 2, 3).foldRight(0)(_ - _)

List(1, 2, 3).foldLeft(List.empty[Int])((acc, x) => x :: acc)
List(1, 2, 3).foldRight(List.empty[Int])((x, acc) => x :: acc)

import cats.Foldable
import cats.instances.list._
import cats.instances.option._
import cats.instances.lazyList._
val ints = List(1, 2, 3)
Foldable[List].foldLeft(ints, 0)(_ + _)
Foldable[Option].foldLeft(Option(123), 10)(_ + _)
Foldable[LazyList].foldLeft(LazyList(1, 2), 1)(_ + _)

Foldable[Option].nonEmpty(Option(32))
Foldable[List].find(List(1, 2, 3))(_ % 2 == 0)

import cats.instances.int._
Foldable[List].combineAll(List(1, 2, 3))
import cats.instances.string._
Foldable[List].foldMap(List(1, 2, 3))(_.toString)

import cats.instances.vector._
val intVecs = List(Vector(1, 2), Vector(3, 4))
// compose Foldables to support deep traversal of nested sequences
(Foldable[List] compose Foldable[Vector]).combineAll(intVecs)

import cats.syntax.foldable._
List(1, 2).combineAll
List(1, 2).foldMap(_.toString)

def sum[F[_]: Foldable](xs: F[Int]): Int = xs.foldLeft(0)(_ + _)
