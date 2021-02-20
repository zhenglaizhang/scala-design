// Modified run-length encoding.
// Modify the result of problem P10 in such a way that if an element has no duplicates it is simply copied into the result list. Only elements with duplicates are transferred as (N, E) terms.
// scala> encodeModified(List('a, 'a, 'a, 'a, 'b, 'c, 'c, 'a, 'a, 'd, 'e, 'e, 'e, 'e))
// res0: List[Any] = List((4,'a), 'b, (2,'c), (2,'a), 'd, (4,'e))
def pack[A](xs: List[A]): List[List[A]] = xs.foldRight(List.empty[List[A]])((h, r) => {
  if (r.nonEmpty && r.head.nonEmpty && h == r.head.head) (h :: r.head) :: r.tail
  else List(h) :: r
})

def encodeModified[A](xs: List[A]): List[Either[A, (Int, A)]] = pack(xs) map { x => if (x.length == 1) Left(x.head) else Right(x.length -> x.head) }
val xs = List('a, 'a, 'a, 'a, 'b, 'c, 'c, 'a, 'a, 'd, 'e, 'e, 'e, 'e)
encodeModified(xs)