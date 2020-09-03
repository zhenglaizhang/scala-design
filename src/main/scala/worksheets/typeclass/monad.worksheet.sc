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

trait Monad[F[_]] {
  def pure[A](a: A): F[A]

  def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]

  def map[A, B](fa: F[A])(f: A => B): F[B] = flatMap(fa)(f.andThen(pure))
}
