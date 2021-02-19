// Reverse a list.
def reverse[A](xs: List[A]): List[A] = xs.foldRight(List.empty[A])((x, acc) => acc :+ x)

val xs = List(1, 1, 2, 3, 5, 8)
reverse(xs)