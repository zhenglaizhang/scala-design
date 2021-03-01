// Choice
// If we have two functions A => C and B => C, how can we compose them into a single function that can take either A
// or B and produce a C?

// we just look for a function that has type (A => C) => (B => C) => (Either[A, B] => C).
// This is exactly typeclass Choice provided, if we make => more generic such as F[_,_], you will get a Choice
object w {
  trait Choice[F[_, _]] {
    def choice[A, B, C, D](fac: F[A, C], fbc: F[B, C]): F[Either[A, B], C]
    // the infix notation of choice is |||
  }
}

// https://typelevel.org/cats/typeclasses/arrowchoice.html
