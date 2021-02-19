// Find the last element of a list.
def last[A](xs: List[A]): A = xs match {
  case h :: Nil => h
  case _ :: t => last(t)
  case _ => throw new NoSuchElementException
}
def last2[A](xs: List[A]): A = xs.reverse.head
def last3[A](xs: List[A]): A = xs.last

val xs = List(1, 2, 3, 5, 8)
last(xs)
last2(xs)
last3(xs)