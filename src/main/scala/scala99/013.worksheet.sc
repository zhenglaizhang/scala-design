// Run-length encoding of a list (direct solution).
// Implement the so-called run-length encoding data compression method directly. I.e. don't use other methods you've written (like P09's pack); do all the work directly.
// scala> encodeDirect(List('a, 'a, 'a, 'a, 'b, 'c, 'c, 'a, 'a, 'd, 'e, 'e, 'e, 'e))
// res0: List[(Int, Symbol)] = List((4,'a), (1,'b), (2,'c), (2,'a), (1,'d), (4,'e))
def encodeDirect[A](xs: List[A]): List[(Int, A)] = {
  if (xs.isEmpty) Nil
  else {
    val (parted, next) = xs.span(_ == xs.head)
    (parted.length, parted.head) :: encodeDirect(next)
  }
}

val xs = List('a, 'a, 'a, 'a, 'b, 'c, 'c, 'a, 'a, 'd, 'e, 'e, 'e, 'e)
encodeDirect(xs)