// Find the last but one element of a list.
def penultimate[A](xs: List[A]): A = xs match {
  case h :: _ :: Nil => h
  case _ :: t => penultimate(t)
  case _ => throw new NoSuchElementException
}

def penultimate2[A](xs: List[A]): A = xs.reverse.tail.head
def penultimate3[A](xs: List[A]): A = if (xs.length < 2) throw new NoSuchElementException else xs.init.last

val xs = List(1, 1, 2, 3, 5, 8)
penultimate(xs)
penultimate2(xs)
penultimate3(xs)


// a generic lastNth function