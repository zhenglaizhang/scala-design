// Extract a slice from a list.
// Given two indices, I and K, the slice is the list containing the elements from and including the Ith element up to but not including the Kth element of the original list. Start counting the elements with 0.
// Example:

// scala> slice(3, 7, List('a, 'b, 'c, 'd, 'e, 'f, 'g, 'h, 'i, 'j, 'k))
// res0: List[Symbol] = List('d, 'e, 'f, 'g)

def slice[A](start: Int, end: Int, xs: List[A]): List[A] = xs.drop(start).take(end-start)
slice(3, 7, List('a, 'b, 'c, 'd, 'e, 'f, 'g, 'h, 'i, 'j, 'k))

def sliceRec[A](start: Int, end: Int, xs: List[A]): List[A] = (start, end, xs) match {
  case (s, e, Nil) => Nil
  case (s, e, xs) if (s >= e) => xs
}