def sum(ints: Seq[Int]): Int = ints.foldLeft(0)(_ + _)
Seq(1, 2) // List(1, 2)
sum(List(1, 2, 3))

def sumDivideAndConquer(ints: IndexedSeq[Int]): Int = {
  if (ints.length <= 1) {
    ints.headOption.getOrElse(0)
  } else {
    val (l, r) = ints.splitAt(ints.length / 2)
    sum(l) + sum(r)
  }
}

sumDivideAndConquer(Vector(1, 2, 3, 4))