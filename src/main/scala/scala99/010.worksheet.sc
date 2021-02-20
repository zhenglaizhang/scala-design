// Run-length encoding of a list.
// Use the result of problem P09 to implement the so-called run-length encoding data compression method. 
// Consecutive duplicates of elements are encoded as tuples (N, E) where N is the number of duplicates of the element E.
// scala> encode(List('a, 'a, 'a, 'a, 'b, 'c, 'c, 'a, 'a, 'd, 'e, 'e, 'e, 'e))
// res0: List[(Int, Symbol)] = List((4,'a), (1,'b), (2,'c), (2,'a), (1,'d), (4,'e))
def pack[A](xs: List[A]): List[List[A]] = xs.foldRight(List.empty[List[A]])((h, r) => {
  if (r.nonEmpty && r.head.nonEmpty && h == r.head.head) (h :: r.head) :: r.tail
  else List(h) :: r
})

def encode[A](xs: List[A]): List[(Int,A)] = pack(xs).map(x => x.length -> x.head)

val xs = List('a, 'a, 'a, 'a, 'b, 'c, 'c, 'a, 'a, 'd, 'e, 'e, 'e, 'e)
encode(xs)