// Reverse a list.

// pure functional
def reverse[A](xs: List[A]): List[A] = xs.foldRight(List.empty[A])((x, acc) => acc :+ x)
def reverse3[A](xs: List[A]): List[A] = xs.foldLeft(List.empty[A])((acc, x) => x +: acc )

def reverse2[A](xs: List[A]): List[A] = xs match {
  case Nil => Nil
  case h :: t :: others => reverse2(others) ++ (t :: h :: Nil)
  case h :: Nil => h :: Nil
}

def r4[A](xs: List[A]): List[A] = xs.reverse

// O(n^2)
def r[A](xs: List[A]): List[A] = xs match {
  case Nil => Nil 
  case h :: tail => r(tail) ::: List(h)
}

def reverseTailRecursive[A](xs: List[A]): List[A] = {
  @scala.annotation.tailrec
  def reverseR(result: List[A], curList: List[A]): List[A] = curList match {
    case Nil => result
    case h :: t => reverseR(h :: result, t)
  }
  reverseR(Nil, xs)
}

val xs = List(1, 1, 2, 3, 5, 8)
reverse(xs)
reverse2(xs)
reverse3(xs)
r(xs)
reverseTailRecursive(xs)