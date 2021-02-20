// Arrow
// - a type class for modeling composable relationships between two types.
//  - A => B (Function1)
//  - cats.data.Kleisli (A => F[B], aka ReaderT)
//  - cats.data.Cokleisli (F[A] => B)
// - These type constructors all have Arrow instances.
// - An arrow F[A, B] can be thought of as representing a computation from A to B with some context,
//   just like a functor/applicative/monad F[A] represents a value A with some context.
// - Having an Arrow instance for a type constructor F[_, _] means that an F[_, _] can be composed and combined with other F[_, _]s
//   - Lifting a function ab: A => B into arrow F[A, B] with Arrow[F].lift(ab). If F is Function1 then A => B is the
//   same as F[A, B] so lift is just the identity function.
//   - Composing fab: F[A, B] and fbc: F[B, C] into fac: F[A, C] with Arrow[F].compose(fbc, fab), or fab >>> fbc. If F
//   is Function1 then >>> becomes an alias for andThen.
//   - Taking two arrows fab: F[A, B] and fcd: F[C, D] and combining them into F[(A, C) => (B, D)] with fab.split(fcd)
//   or fab *** fcd. The resulting arrow takes two inputs and processes them with two arrows, one for each input.
//   - Taking an arrow fab: F[A, B] and turning it into F[(A, C), (B, C)] with fab.first. The resulting arrow takes two
// inputs, processes the first input and leaves the second input as it is. A similar method, fab.second, turns F[A, B] into F[(C, A), (C, B)].

// scala.Function1 has an Arrow instance
//  - builtin compose/andThen
//  - Arrow instance offers more powerful options

import cats.arrow.Arrow
import cats.implicits._
def combine[F[_, _]: Arrow, A, B, C](fab: F[A, B], fac: F[A, C]): F[A, (B, C)] =
  Arrow[F].lift((a: A) => (a, a)) >>> (fab *** fac)

// Kleisli
