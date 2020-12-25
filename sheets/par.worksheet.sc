def sum1(ints: Seq[Int]): Int = ints.foldLeft(0)(_ + _)
Seq(1, 2) // List(1, 2)
sum1(List(1, 2, 3))

def sumDivideAndConquer(ints: IndexedSeq[Int]): Int = {
  if (ints.length <= 1) {
    ints.headOption.getOrElse(0)
  } else {
    val (l, r) = ints.splitAt(ints.length / 2)
    sumDivideAndConquer(l) + sumDivideAndConquer(r)
  }
}

sumDivideAndConquer(Vector(1, 2, 3, 4))

class Par[A] {}

object Par {
  def unit[A](a: A): Par[A] = ???
  // a given Par should be run in a separate logic thread
  def fork[A](a: => Par[A]): Par[A] = ???
  def map2[A, B](a: Par[A], b: Par[A])(f: (A, A) => B) = ???
}

def sum(ints: IndexedSeq[Int]): Par[Int] = {
  if (ints.length <= 1) {
    Par.unit((ints.headOption.getOrElse(0)))
  } else {
    val (l, r) = ints.splitAt(ints.length / 2)
    Par.map2(sum(l), sum(r))(_ + _)
  }
}
