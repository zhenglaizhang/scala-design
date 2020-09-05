def show[A](xs: List[A]): String =
  xs.foldLeft("nil")((acc, x) => s"$x then $acc")

show(List(1, 2, 3, 4, 5))

List(1, 2, 3).foldLeft(0)(_ - _)
List(1, 2, 3).foldRight(0)(_ - _)

List(1, 2, 3).foldLeft(List.empty[Int])((acc, x) => x :: acc)
List(1, 2, 3).foldRight(List.empty[Int])((x, acc) => x :: acc)
