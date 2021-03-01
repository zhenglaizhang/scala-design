// Invariant
// The Invariant type class is for functors that define an imap function with the following type:
// def imap[A, B](fa: F[A])(f: A => B)(g: B => A): F[B]
// Every covariant (as well as contravariant) functor gives rise to an invariant functor, by ignoring the g (or in
// case of contravariance, f) function.
// Examples for instances of Invariant are Semigroup and Monoid

// Invariant instance for Semigroup
// Pretend that we have a Semigroup[Long] representing a standard UNIX timestamp. Let’s say that we want to create a
// Semigroup[Date], by reusing Semigroup[Long].
// Semigroup does not form a covariant functor
import java.util.Date
def longToDate: Long => Date = new Date(_)
// A Semigroup[Date] should be able to combine two values of type Date, given a Semigroup that only knows how to combine Longs

// Semigroup does not form a contravariant functor
def dateToLong: Date => Long = _.getTime
// Again we are faced with a problem when trying to get a Semigroup[Date] based on a Semigroup[Long]. As before
// consider the case where we have two values of Date at hand. Using dateToLong we can turn them into Longs and use
// Semigroup[Long] to combine the two values. We are left with a value of type Long, but we can’t turn it back into a
// Date using only contramap!

// Semigroup does form an invariant functor
// From the previous discussion we conclude that we need both the map from (covariant) Functor and contramap from
// Contravariant. There already is a type class for this and it is called Invariant. Instances of the Invariant type
// class provide the imap function:
// We can use the g parameter to turn Date into a Long, combine our two values using Semigroup[Long] and then convert the result back into a Date using the f parameter of imap
import cats._
import cats.implicits._
def longToDate: Long => Date = new Date(_)
val dateToLong: Date => Long = _.getTime
implicit val semigroupDate: Semigroup[Date] =
  Semigroup[Long].imap(longToDate)(dateToLong)

val today: Date = longToDate(1449088684104L)
val timeLeft: Date = longToDate(1900918893L)
today |+| timeLeft
