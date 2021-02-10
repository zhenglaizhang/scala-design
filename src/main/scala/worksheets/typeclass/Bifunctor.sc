object w {

  trait Bifunctor[F[_, _]] {
    def bimap[A, B, C, D](fab: F[A, B])(f: A => C, g: B => D): F[C, D]

    def leftMap[A, B, C](fab: F[A, B])(f: A => C): F[C, B] =
      bimap(fab)(f, identity)

    // There is no rightMap however - use map instead.
    // The reasoning behind this is that in Cats, the instances of Bifunctor are also mostly instances of Functor,
    // as it is the case with Either.
  }

}

// Either as a Bifunctor

import cats._
import cats.implicits._
import java.time._

case class DomainError(message: String)

def dateTimeFromUser: Either[Throwable, ZonedDateTime] =
  Right(ZonedDateTime.now())

dateTimeFromUser.bimap(
  err => DomainError(err.getMessage),
  dt => dt.toEpochSecond
)
// Tuple2 as a Bifunctor

val records: List[(Int, Int)] =
  List((450000, 3), (770000, 4), (990000, 2), (2100, 4), (43300, 3))
def calculateContributionPerMonth(balance: Int, lifetime: Int) =
  balance / lifetime

val result: List[Int] =
  records
    .map(r => r.bimap(c => c / 100, years => 12 * years))
    .map((calculateContributionPerMonth _).tupled)

// this instance makes it convenient to process two related pieces of data in independent ways,
// especially when there is no state relationship between the two until processing is complete.
