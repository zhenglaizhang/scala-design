package meow

sealed trait List[+A]
case object Nil extends List[Nothing]
case class Cons[+A](val head: A, val tail: List[A]) extends List[A]
object List {
  def apply[A](xs: A*): List[A] = {
    if (xs.isEmpty) Nil
    else {
      new Cons(xs.head, apply(xs.tail: _*))
    }
  }
}

object ListApp extends App {
  val x = List.apply(1, 2, 3)
}
