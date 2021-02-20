// Duplicate the elements of a list a given number of times.
// scala> duplicateN(3, List('a, 'b, 'c, 'c, 'd))
// res0: List[Symbol] = List('a, 'a, 'a, 'b, 'b, 'b, 'c, 'c, 'c, 'c, 'c, 'c, 'd, 'd, 'd)
def duplicateN[A](n: Int, xs: List[A]): List[A] = xs flatMap {List.fill(n)(_)}

val xs = List('a, 'b, 'c, 'c, 'd)
duplicateN(3, xs)