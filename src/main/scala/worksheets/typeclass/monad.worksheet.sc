import scala.util.Try
import scala.util.Try

def parseInt(str: String): Option[Int] = Try(str.toInt).toOption

def divide(a: Int, b: Int): Option[Int] = if (b == 0) None else Some(a / b)

def strDivideBy(a: String, b: String): Option[Int] =
  parseInt(a).flatMap(a =>
    parseInt(b).flatMap { b =>
      divide(a, b)
    }
  )

def strDivideBy2(a: String, b: String): Option[Int] =
  for {
    an <- parseInt(a)
    bn <- parseInt(b)
    ans <- divide(an, bn)
  } yield ans

strDivideBy("4", "2")
strDivideBy("4", "0")

object hidden {
  trait Monad[F[_]] {
    def pure[A](a: A): F[A]

    def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]

    def map[A, B](fa: F[A])(f: A => B): F[B] = flatMap(fa)(f.andThen(pure))
  }
}

import cats.Monad
import cats.instances.option._
import cats.instances.list._

val opt1 = Monad[Option].pure(3)
val opt2 = Monad[Option].flatMap(opt1)(a => Some(a + 2))
val list1 = Monad[List].pure(3)
val list2 = Monad[List].flatMap(list1)(a => List(a, a * 10))

import cats.syntax.applicative._
1.pure[Option]
1.pure[List]

import cats.syntax.flatMap._
import cats.syntax.functor._
def sumSquare[F[_]: Monad](a: F[Int], b: F[Int]): F[Int] =
  a.flatMap(x => b.map(y => x * x + y * y))

sumSquare(Option(1), Option(2))
sumSquare(Option(1), Option.empty[Int])
