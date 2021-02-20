// Decode a run-length encoded list.
// Given a run-length code list generated as specified in problem P10, construct its uncompressed version.
// scala> decode(List((4, 'a), (1, 'b), (2, 'c), (2, 'a), (1, 'd), (4, 'e)))
// res0: List[Symbol] = List('a, 'a, 'a, 'a, 'b, 'c, 'c, 'a, 'a, 'd, 'e, 'e, 'e, 'e)
def decode[A](xs: List[(Int, A)]): List[A] = xs flatMap {
  case (n, x) => List.fill(n)(x)
}

val xs = List((4, 'a), (1, 'b), (2, 'c), (2, 'a), (1, 'd), (4, 'e))
decode(xs)
