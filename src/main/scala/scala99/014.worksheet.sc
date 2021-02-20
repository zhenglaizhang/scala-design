// Duplicate the elements of a list.
// scala> duplicate(List('a, 'b, 'c, 'c, 'd))
// res0: List[Symbol] = List('a, 'a, 'b, 'b, 'c, 'c, 'c, 'c, 'd, 'd)

def duplicate[A](xs: List[A]): List[A] = xs.flatMap(x => List.fill(2)(x)) // List(x, x)

val xs = List('a, 'b, 'c, 'c, 'd)
duplicate(xs)
