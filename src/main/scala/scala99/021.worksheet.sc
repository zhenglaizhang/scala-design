//Insert an element at a given position into a list.
//Example:
//scala> insertAt('new, 1, List('a, 'b, 'c, 'd))
//res0: List[Symbol] = List('a, 'new, 'b, 'c, 'd)

def insertAt[A](x: A, pos: Int, xs: List[A]): List[A] =
  (pos, xs) match {
    case (0, xs)              => x :: xs
    case (i, h :: t) if i > 0 => h :: insertAt(x, i - 1, t)
    case _                    => xs
  }

def insertAt2[A](x: A, pos: Int, xs: List[A]): List[A] =
  xs.splitAt(pos) match {
    //🧡
    case (pre, post) => pre ::: x :: post
  }

val xs = List('a, 'b, 'c, 'd)
insertAt('new, 1, xs)
insertAt2('new, 1, xs)
