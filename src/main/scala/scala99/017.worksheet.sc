// Split a list into two parts.
// The length of the first part is given. Use a Tuple for your result.
def split[A](n: Int, xs: List[A]): (List[A], List[A]) = (xs.take(n), xs.takeRight(xs.length-n))
def split2[A](n: Int, xs: List[A]): (List[A], List[A]) = (xs.take(n), xs.drop(n))
def split3[A](n: Int, xs: List[A]): (List[A], List[A]) = xs.splitAt(n)

val xs = List('a, 'b, 'c, 'd, 'e, 'f, 'g, 'h, 'i, 'j, 'k)
split(3, xs)


  // Simple recursion.
  def splitRecursive[A](n: Int, ls: List[A]): (List[A], List[A]) = (n, ls) match {
    case (_, Nil)       => (Nil, Nil)
    case (0, list)      => (Nil, list)
    case (n, h :: tail) => {
      val (pre, post) = splitRecursive(n - 1, tail)
      (h :: pre, post)
    }
  }

  // Tail recursive.
  def splitTailRecursive[A](n: Int, ls: List[A]): (List[A], List[A]) = {
    def splitR(curN: Int, curL: List[A], pre: List[A]): (List[A], List[A]) =
      (curN, curL) match {
        case (_, Nil)       => (pre.reverse, Nil)
        case (0, list)      => (pre.reverse, list)
        case (n, h :: tail) => splitR(n - 1, tail, h :: pre)
      }
    splitR(n, ls, Nil)
  }