package meow

sealed trait List[+A]
case object Nil extends List[Nothing] {
  def tail: List[Nothing] = throw new NoSuchElementException("tail")
  def tailOption: Option[List[Nothing]] = None
}
case class Cons[+A](val head: A, val tail: List[A]) extends List[A] {
  def tailOption: Option[List[A]] = if (tail == Nil) None else Some(tail)
}
object List {
  def sum(ints: List[Int]): Int =
    ints match {
      case Nil              => 0
      case Cons(head, tail) => head + sum(tail)
    }

  def product(ints: List[Int]): Int =
    ints match {
      case Nil              => 1
      case Cons(0, _)       => 0
      case Cons(head, tail) => head * product(tail)
    }

  def apply[A](xs: A*): List[A] = {
    if (xs.isEmpty) Nil
    else {
      new Cons(xs.head, apply(xs.tail: _*))
    }
  }

  def drop[A](xs: List[A], n: Int): List[A] = {
    if (n == 0) {
      xs
    } else {
      xs match {
        case Cons(_, tail) => drop(tail, n - 1)
        case Nil           => Nil
      }
    }
  }

  def dropWhile[A](xs: List[A], p: A => Boolean): List[A] =
    xs match {
      case Cons(head, tail) if p(head)  => dropWhile(tail, p)
      case Cons(head, tail) if !p(head) => Cons(head, dropWhile(tail, p))
      case Nil                          => Nil
    }
}

object ListApp extends App {
  val x = List.apply(1, 2, 3)
  val x1 = Cons("a", Cons("b", Nil))
  x match {
    case Cons(h, Cons(2, Cons(4, _))) => h
    case Nil                          => 42
    case Cons(x, Cons(y, Cons(4, _))) => x + y
    case Cons(h, t)                   => h + List.sum(t)
    case _                            => 101
  }
  println(x)
  println(x1)
  val xs = List.drop(List(1, 2, 3, 4, 5), 3)
  println(xs)
  println(List.drop(Nil, 2))
  println(List.dropWhile(List(1, 2, 3, 4, 5, 6), (_: Int) % 2 == 0))
}
