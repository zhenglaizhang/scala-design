// Flatten a nested list structure.
def flatten(xs: List[Any]): List[Any] = xs flatMap {
  // case l: List[Any] => flatten(l)
  // case x: Any => List(x)
  case l: List[_] => flatten(l)
  case x => List(x)
}

flatten(List(List(1, 1), 2, List(3, List(5, 8))))