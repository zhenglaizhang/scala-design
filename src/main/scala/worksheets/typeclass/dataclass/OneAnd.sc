// The OneAnd[F[_],A] data type represents a single element of type A that is guaranteed to be present (head) and in addition to this a second part that is
// wrapped inside an higher kinded type constructor F[_]
//
// A data type which represents a single element (head) and some other
//  structure (tail). As we have done in package.scala, this can be
// used to represent a Stream which is guaranteed to not be empty:

// todo: revisit

import cats.data.OneAnd

type NonEmptyList[A] = OneAnd[List, A]
// Which used to be the implementation of non-empty lists in Cats
// but has been replaced by the cats.data.NonEmptyList data type.

type NonEmptyStream[A] = OneAnd[Stream, A]
