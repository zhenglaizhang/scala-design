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
  def map[A, B](xs: List[A])(f: A => B): List[B] = ???

  def flatMap[A, B](xs: List[A])(f: A => List[B]): List[B] = ???

  // def scanLeft

  def zipWith[A, B](xs: List[A], ys: List[B])(f: (A, B) => B): List[B] = ???

  def filter[A](xs: List[A])(f: A => Boolean): List[B] = ???

  def foldLeft[A, B](xs: List[A], z: B)(f: (B, A) => B): B = ???

  def hasSubsequence[A](sup: list[A], sub: List[A]): Boolean = ???

  // reverse

  // append

  // concatenate list of lists

  // not stack-safe
  def foldRight[A, B](xs: List[A], z: B)(f: (A, B) => B): B =
    xs match {
      case Nil         => z
      case Cons(x, xs) => f(x, foldRight(xs, z)(f))
    }

  def length[A](xs: List[A]): Int = foldRight(xs, 0)((_, n) => n + 1)

  def sum2(xs: List[Int]): Int = foldRight(xs, 0)(_ + _)

  def sum(ints: List[Int]): Int =
    ints match {
      case Nil              => 0
      case Cons(head, tail) => head + sum(tail)
    }

  def product2(xs: List[Int]): Int = foldRight(xs, 1)(_ * _)
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

  def dropWhile[A](xs: List[A])(p: A => Boolean): List[A] =
    xs match {
      case Cons(head, tail) if p(head)  => dropWhile(tail)(p)
      case Cons(head, tail) if !p(head) => Cons(head, dropWhile(tail)(p))
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
  println(List.dropWhile(List(1, 2, 3, 4, 5, 6))(_ % 2 == 0))
  println(List.dropWhile(List(1, 2, 3, 4, 5, 6))(x => x < 0))

  val xs2 = List.foldRight(List(1, 2, 3), List(4, 5, 6))(Cons(_, _))
  println(xs2)
  println(List.length((xs2)))

  // tuple is also ADT
  val m = ("Bob", 2) match {
    case (a, b) => b
  }
  println(m)
}
