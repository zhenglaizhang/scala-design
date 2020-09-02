import scala.concurrent.ExecutionContext
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

// map leaves the structure of the context unchanged
List(1, 2, 3)
  .map(_ + 1)
  .map(_ * 2)
  .map(n => s"${n}")

val future: Future[String] = Future(123)
  .map(_ + 1)
  .map(_ * 2)
  .map(n => s"${n}")

Await.result(future, 1.second)

import cats.instances.function._ // for Funtor
import cats.syntax.functor._ // for map

val func1: Int => Double = x => x.toDouble
val func2: Double => Double = _ * 2
(func1 map func2)(1)
(func1 andThen func2)(1)
func2(func1(1))

val func = ((x: Int) => x.toDouble)
  .map(_ + 1)
  .map(_ * 2)
  .map(n => s"${n}")
func(123)

object functor {
  trait Functor[F[_]] {
    def map[A, B](fa: F[A])(f: A => B): F[B]
  }
}

import cats.Functor
import cats.instances.list._
import cats.instances.option._

Functor[List].map(List(1, 2, 3))(_ * 2)
Functor[Option].map(Option(123))(_.toString)

val liftedFunc = Functor[Option].lift(func)
liftedFunc(Option(123))
liftedFunc(Option.empty)

Functor[List].as(List(1, 2, 3), "As")

def doMath[F[_]](start: F[Int])(implicit functor: Functor[F]): F[Int] =
  start.map(_ + 1 * 2)
doMath(Option(20))
doMath(List(1, 2, 3))

// implicit def futureFunctor(implicit ec: ExecutionContext): Functor[Future] =
//   new Functor[Future] {
//     def map[A, B](fa: Future[A])(f: A => B): Future[B] = fa.map(f)
//   }

// Functor.apply[Future]

trait Printable[A] { self =>
  def format(value: A): String

  def contramap[B](func: B => A): Printable[B] =
    new Printable[B] {
      def format(value: B): String = self.format(func(value))
    }
}

def format[A](value: A)(implicit p: Printable[A]): String = p.format(value)

implicit val strPrintable: Printable[String] = new Printable[String] {
  def format(value: String): String = s"${value}"
}
implicit val boolPrintable: Printable[Boolean] = new Printable[Boolean] {
  def format(value: Boolean): String = if (value) "yes" else "no"
}

format("hello")
format(true)
format(false)

final case class Box[A](value: A)

// implicit def boxPrintable[A](implicit p: Printable[A]): Printable[Box[A]] =
//   new Printable[Box[A]] {
//     override def format(value: Box[A]): String = p.format(value.value)
//   }

implicit def boxPrintable[A](implicit p: Printable[A]): Printable[Box[A]] =
  p.contramap(box => box.value)

format(Box("hello world"))
format(Box(true))
