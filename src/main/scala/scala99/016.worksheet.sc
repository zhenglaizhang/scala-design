// Drop every Nth element from a list.
// scala> drop(3, List('a, 'b, 'c, 'd, 'e, 'f, 'g, 'h, 'i, 'j, 'k))
// res0: List[Symbol] = List('a, 'b, 'd, 'e, 'g, 'h, 'j, 'k)
def drop[A](n: Int, xs: List[A]): List[A] = xs.foldLeft((n, List.empty[A]))((acc, x) => {
  if (acc._1 == 1) (n, acc._2)
  else (acc._1-1, x :: acc._2)
})._2.reverse


def dropFunctional[A](n: Int, xs: List[A]): List[A] = xs.zipWithIndex filter { case (x, ix) => (ix + 1) % n != 0 } map {_._1}

val xs = List('a, 'b, 'c, 'd, 'e, 'f, 'g, 'h, 'i, 'j, 'k)
drop(3, xs)
dropFunctional(3, xs)
drop(3, xs) == dropFunctional(3, xs)