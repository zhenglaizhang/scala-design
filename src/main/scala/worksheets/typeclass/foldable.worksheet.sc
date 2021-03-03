// Foldable type class instances can be defined for data structures that can be folded to a summary value.

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

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
def getUptime(hostname: String): Future[Int] = Future(hostname.length * 60)

def getUptimes(xs: List[String]): Future[List[Int]] =
  Future.traverse(xs)(getUptime)

Await.result(getUptimes(List("github.com", "baidu.com")), 1.second)

val f = Future.sequence(List(Future(1), Future(2)))
Await.result(f, 1.second)

import cats.Monoid
import cats.syntax.semigroup._
// def foldMap[A, B: Monoid](xs: Vector[A])(func: A => B): B =
//   xs.map(func).foldLeft(Monoid[B].empty)(_ |+| _)
def foldMap[A, B: Monoid](xs: Vector[A])(func: A => B): B =
  xs.foldLeft(Monoid[B].empty)(_ |+| func(_))

foldMap(Vector(1, 2, 3))(_.toString + "!")

def parallelFoldMap[A, B: Monoid](xs: Vector[A])(func: A => B): Future[B] = {
  val numCores = Runtime.getRuntime().availableProcessors()
  val groupSize = (1.0 * xs.size / numCores).ceil.toInt
  val batches = xs.grouped(groupSize)
  val futures = batches.map { batch => Future(foldMap(batch)(func)) }
  Future.sequence(futures).map { ys =>
    ys.foldLeft(Monoid[B].empty)(Monoid.combine)
  }
}

val r = parallelFoldMap(Vector(1, 2, 3))(x => x * x)
Await.result(r, 1.second)
