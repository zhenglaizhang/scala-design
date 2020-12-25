package meow

sealed trait List[+A]
case object Nil extends List[Nothing]
case class Cons[+A](val head: A, val tail: List[A]) extends List[A]
object List {
  def sum(ints: List[Int]): Int =
    ints match {
      case Nil              => 0
      case Cons(head, tail) => head + sum(tail)
    }

  def product(ints: List[Int]): Int =
    ints match {
      case Nil              => 1
      case Cons(0, tail)    => 0
      case Cons(head, tail) => head * product(tail)
    }

  def apply[A](xs: A*): List[A] = {
    if (xs.isEmpty) Nil
    else {
      new Cons(xs.head, apply(xs.tail: _*))
    }
  }
}

object ListApp extends App {
  val x = List.apply(1, 2, 3)
  val x1 = Cons("a", Cons("b", Nil))
  println(x)
  println(x1)
}
