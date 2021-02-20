// Applicative extends Functor with an ap and pure method.
import cats.Functor

object w {
  trait Applicative[F[_]] extends Functor[F] {
    def ap[A, B](ff: F[A => B])(fa: F[A]): F[B]

    // pure wraps the value into the type constructor
    //  Option -> Some(_)
    //  Future -> Future.successful(_)
    //  List   -> List(_)
    def pure[A](a: A): F[A]
    def map[A, B](fa: F[A])(f: A => B): F[B] = ap(pure(f))(fa)
  }

  // An alternative but equivalent formulation via product
  trait Applicative2[F[_]] extends Functor[F] {
    def product[A, B](fa: F[A], fb: F[B]): F[(A, B)]
    def pure[A](a: A): F[A]
  }

// Example implementation for right-biased Either
//  implicit def applicativeForEither[L]: Applicative[Either[L, *]] = new Applicative[Either[L, *]] {
//    def product[A, B](fa: Either[L, A], fb: Either[L, B]): Either[L, (A, B)] = (fa, fb) match {
//      case (Right(a), Right(b)) => Right((a, b))
//      case (Left(l) , _       ) => Left(l)
//      case (_       , Left(l) ) => Left(l)
//    }
//
//    def pure[A](a: A): Either[L, A] = Right(a)
//
//    def map[A, B](fa: Either[L, A])(f: A => B): Either[L, B] = fa match {
//      case Right(a) => Right(f(a))
//      case Left(l)  => Left(l)
//    }
//  }
}

// ap is equivalent to map and product
// Applicative must obey three laws:
//  - Associativity: No matter the order in which you product together three values, the result is isomorphic
//    - fa.product(fb).product(fc) ~ fa.product(fb.product(fc))
//    - With map, this can be made into an equality with fa.product(fb).product(fc) = fa.product(fb.product(fc)).map
//      { case (a, (b, c)) => ((a, b), c) }
//  - Left identity: Zipping a value on the left with unit results in something isomorphic to the original value
//    - pure(()).product(fa) ~ fa
//    - As an equality: pure(()).product(fa).map(_._2) = fa
//  - Right identity: Zipping a value on the right with unit results in something isomorphic to the original value
//    - fa.product(pure(())) ~ fa
//    - As an equality: fa.product(pure(())).map(_._1) = fa

// Applicatives for effect management
// If we view Functor as the ability to work with a single effect,
// Applicative encodes working with multiple independent effects.
// Between product and map, we can take two separate effectful values and compose them.
// From there we can generalize to working with any N number of independent effects.
import cats.Applicative
def product3[F[_]: Applicative, A, B, C](
    fa: F[A],
    fb: F[B],
    fc: F[C]
): F[(A, B, C)] = {
  val F = Applicative[F]
  val fabc = F.product(F.product(fa, fb), fc)
  F.map(fabc) { case ((a, b), c) => (a, b, c) }
}

import cats.implicits._
val f: (Int, Char) => Double = (i, c) => (i + c).toDouble
val int: Option[Int] = Some(5)
val char: Option[Char] = Some('a')
int.map(i => (c: Char) => f(i, c))
// Option[Char => Double] and an Option[Char] to which we want to apply the function to,
// but map doesn’t give us enough power to do that. Hence, ap

// Applicatives compose
//  - Like Functor, Applicatives compose.
//  - If F and G have Applicative instances, then so does F[G[_]].
import cats.data.Nested
import cats.implicits._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

val x: Future[Option[Int]] = Future.successful(Some(5))
val y: Future[Option[Char]] = Future.successful(Some('a'))
val composed = Applicative[Future].compose[Option].map2(x, y)(_ + _)
composed.value

//val nested = Applicative[Nested[Future, Option, *]].map2(Nested(x), Nested(y))(_ + _)

// The straightforward way to use product and map (or just ap) is to compose n independent effects, where n is a
// fixed number. In fact there are convenience methods named apN, mapN, and tupleN (replacing N with a number 2 - 22)
// to make it even easier.
import java.sql.Connection

val username: Option[String] = Some("username")
val password: Option[String] = Some("password")
val url: Option[String] = Some("some.login.url.here")

// Stub for demonstration purposes
def attemptConnect(
    username: String,
    password: String,
    url: String
): Option[Connection] = None

Applicative[Option]
  .map3(username, password, url)(attemptConnect)
// res2: Option[Option[Connection]] = Some(None)
Applicative[Option].tuple3(username, password, url)

// Sometimes we don’t know how many effects will be in play - perhaps we are receiving a list from user input or
// getting rows from a database. This implies the need for a function:
def traverse[F[_]: Applicative, A, B](xs: List[A])(
    f: A => F[B]
): F[List[B]] =
  xs.foldRight(Applicative[F].pure(List.empty[B])) { (a: A, acc: F[List[B]]) =>
    val fb: F[B] = f(a)
    Applicative[F].map2(fb, acc)(_ :: _)
  }
def sequence[F[_]: Applicative, A](fa: List[F[A]]): F[List[A]] =
  traverse(fa)(identity)
// Future.sequence or Future.traverse

12 +: 13 +: Nil
12 :: 13 :: Nil

import cats.implicits._
List(1, 2, 3).traverse(i => Some(i): Option[Int])
// With this addition of traverse, we can now compose any number of independent effects, statically known or otherwise.

// Apply - a weakened Applicative
// A closely related type class is Apply which is identical to Applicative, modulo the pure method. Indeed in Cats Applicative is a subclass of Apply with the addition of this method.
object u {
  trait Apply[F[_]] extends Functor[F] {
    def ap[A, B](ff: F[A => B])(fa: F[A]): F[B]
  }

  trait Applicative[F[_]] extends Apply[F] {
    def pure[A](a: A): F[A]
    def map[A, B](fa: F[A])(f: A => B): F[B] =
      ap(pure(f))(fa)
  }
}
//The laws for Apply are just the laws of Applicative that don’t mention pure. In the laws given above, the only law
// would be associativity.
//
//One of the motivations for Apply’s existence is that some types have Apply instances but not Applicative - one
// example is Map[K, *]. Consider the behavior of pure for Map[K, A]. Given a value of type A, we need to associate
// some arbitrary K to it but we have no way of doing that.
//
// However, given existing Map[K, A] and Map[K, B] (or Map[K, A => B]), it is straightforward to pair up (or apply
// functions to) values with the same key. Hence Map[K, *] has an Apply instance.

// syntax for Applicative/Apply
//  - Achieves a slightly friendlier syntax by enriching Scala’s standard tuple types.
import cats.implicits._
(username, password, url).mapN(attemptConnect)
