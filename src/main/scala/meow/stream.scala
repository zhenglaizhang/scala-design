package meow
package stream

sealed trait Stream[+A] {
  def toList: List[A]
  def headOption: Option[A]
  def take(n: Int): Stream[A]
  def takeWhile(p: A => Boolean): Stream[A]
  def exists(p: A => Boolean): Boolean
  def forall(p: A => Boolean): Boolean
  def map[B](f: A => B): Stream[B]
  def flatMap[B](f: A => Stream[B]): Stream[B]
  def foldRight[B](z: => B)(f: (A, => B) => B): B
  def zipAll[B](s2: Stream[B]): Stream[(Option[A], Option[B])]
  def startsWith(s: Stream[A]): Boolean
  // 1,2,3 => 1, 2, 3 + 2, 3
  def tails: Stream[Stream[A]]
}

object Stream {
  def constant[A](a: A): Stream[A] = ???
  def from(n: Int, step: Int = 1): Stream[Int] = ???
  def unfold[A: S](z: S)(f: S => Option[(A, S)]): Stream[A] = ???
}

case class Cons[+A](h: () => A, t: () => Stream[A]) extends Stream[A]
case object Empty extends Stream[Nothing]

object StreamApp extends App {
  def fibs: Stream[Int] = ???
}
