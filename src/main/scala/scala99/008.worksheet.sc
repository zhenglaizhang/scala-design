// Eliminate consecutive duplicates of list elements.
def compress[A](xs: List[A]): List[A] = xs match {
  case h1 :: h2 :: others if h1 == h2 => compress(h2 :: others)
  case h1 :: h2 :: others if h1 != h2 => h1 :: compress(h2 :: others)
  case _ => xs
}

def c2[A](xs: List[A]): List[A] = xs match {
  case Nil => Nil
  case h :: tail => h :: c2(tail.dropWhile(_ == h))
}

def compressFunctional[A](xs: List[A]): List[A] = xs.foldRight(List.empty[A]) {(h, r) => {
  if (r.isEmpty || r.head != h) h :: r
  else r
}}

// todo
// def compressTailRecursive[A](xs: List[A]): List[A] = {
//   def compressR(result: List[A], curList: List[A]): List[A] = curList match {
//     case h :: t => compressR(h :: result, curList.dropWhile(_ == h))
//     case Nil => result.reverse
//   }
//   compressR(Nil, xs)
// }

val xs = List('a, 'a, 'a, 'a, 'b, 'c, 'c, 'a, 'a, 'd, 'e, 'e, 'e, 'e)
compress(xs)
compressFunctional(xs)
// compressTailRecursive(List('a, 'a, 'a, 'a, 'b, 'c, 'c, 'a, 'a, 'd, 'e, 'e, 'e, 'e))