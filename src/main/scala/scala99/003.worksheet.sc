// Find the Kth element of a list.
//  By convention, the first element in the list is element 0.
def nth[A](n: Int, xs: List[A]): A = (n, xs) match {
  case (0, h :: _) => h
  case (n, _ :: t) => nth(n-1, t)
  case (_, Nil) => throw new NoSuchElementException
}

def nth2[A](n: Int, xs: List[A]): A = if (n >= 0) xs(n) else throw new NoSuchElementException

val xs =  List(1, 1, 2, 3, 5, 8)
nth(2, xs)
nth2(2, xs)