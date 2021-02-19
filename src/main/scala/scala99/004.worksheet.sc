// Find the number of elements of a list.
def length[A](xs: List[A]): Int = xs match {
  case _ :: t => 1 + length(t)
  case Nil => 0
}

// Tail recursive solution.  Theoretically more efficient; with tail-call
// elimination in the compiler, this would run in constant space.
// Unfortunately, the JVM doesn't do tail-call elimination in the general
// case.  Scala *will* do it if the method is either final or is a local
// function.  In this case, `lengthR` is a local function, so it should
// be properly optimized.
// For more information, see
// http://blog.richdougherty.com/2009/04/tail-calls-tailrec-and-trampolines.html
def lengthTailRecursive[A](xs: List[A]): Int = {
  @scala.annotation.tailrec
  def lengthR(result: Int, curList: List[A]): Int = curList match {
    case Nil => result
    case head :: next => lengthR(result+1, next)
  }
  lengthR(0, xs)
}

def lengthFold[A](xs: List[A]): Int = xs.foldLeft(0)((c, _) => c + 1)

def length2[A](xs: List[A]): Int = xs.length
def length3[A](xs: List[A]): Int = xs.size

val xs = List(1, 1, 2, 3, 5, 8)
length(xs)
lengthTailRecursive(xs)
lengthFold(xs)

12 :: Nil
12 +: Nil