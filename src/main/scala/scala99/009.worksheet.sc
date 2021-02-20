// Pack consecutive duplicates of list elements into sublists.
// scala> pack(List('a, 'a, 'a, 'a, 'b, 'c, 'c, 'a, 'a, 'd, 'e, 'e, 'e, 'e))
// res0: List[List[Symbol]] = List(List('a, 'a, 'a, 'a), List('b), List('c, 'c), List('a, 'a), List('d), List('e, 'e, 'e, 'e))

// wrong!
// def pack[A](xs: List[A]): List[List[A]] = xs.groupBy(identity).values.toList

def pack[A](xs: List[A]): List[List[A]] = xs.foldRight(List.empty[List[A]])((h, r) => {
  if (r.nonEmpty && r.head.nonEmpty && h == r.head.head) (h :: r.head) :: r.tail
  else List(h) :: r
})

def pack2[A](xs: List[A]): List[List[A]] = 
  if (xs.isEmpty) List(List())
  else {
    val (packed, next) = xs.span( _ == xs.head)
    if (next == Nil) List(packed)
    else packed :: pack2(next)
  }

val xs = List('a, 'a, 'a, 'a, 'b, 'c, 'c, 'a, 'a, 'd, 'e, 'e, 'e, 'e)
pack(xs)
pack2(xs)
