// A functor F[_] is a type constructor of kind * -> *.
// In the most general case, an F[A] represents a recipe that may halt, run forever, or produce 0 or more A's.
//
// A functor instance must obey two laws
// Identity: Mapping with the identity function (x => x) is a no-op
//  - map(fa)(identity) == fa
//  - fa.map(x => x) == fa
// Composition: Mapping with f and then again with g is the same as mapping once with the composition of f and g
//  - map(map(fa)(f))(g) == map(fa)(f.andThen(g))
//  - fa.map(f).map(g) == fa.map(f.andThen(g))

object functor {

  // technically, this is a covariant endofunctor,
  trait Functor[F[_]] {
    //  Functor is a type class that abstracts over type constructors that can be map‘ed over.
    def map[A, B](fa: F[A])(f: A => B): F[B]

    // Another way of viewing a Functor[F] is that F allows the lifting of a pure function A => B into the effectful function F[A] => F[B]
    def lift[A, B](f: A => B): F[A] => F[B] =
      fa => map(fa)(f)
  }

}
// Functors for effect management
// The F in Functor is often referred to as an “effect” or “computational context.”
// Different effects will abstract away different behaviors with respect to fundamental functions like map.
// For instance, Option’s effect abstracts away potentially missing values, where map applies the function only in the
// Some case but otherwise threads the None through.
//
// - We can view Functor as the ability to work with a single effect
// - We can apply a pure function to a single effectful value without needing to “leave” the effect.

// Functors compose
//  - List[Either[String, Future[A]]]
//  - _.map(_.map(_.map(f))
//  - Functors compose, which means if F and G have Functor instances, then so does F[G[_]]
//  - Such composition can be achieved via the Functor#compose method.
import cats.Functor
import cats.implicits._
val listOption = List(Some(1), None, Some(2))
Functor[List].compose[Option].map(listOption)(_ + 1)

def needsFunctor[F[_]: Functor, A](fa: F[A]): F[Unit] =
  Functor[F].map(fa)(_ => ())

// This approach will allow us to use composition without wrapping the value in question, but can introduce complications in more complex use cases.
def foo: List[Option[Unit]] = {
  implicit val listOptionFunctor = Functor[List].compose[Option]
  type ListOption[A] = List[Option[A]]
  needsFunctor[ListOption, Int](listOption)
}

// We can make this nicer at the cost of boxing with the Nested data type.
import cats.data.Nested
import cats.implicits._
val nested: Nested[List, Option, Int] = Nested(listOption)
nested.map(_ + 1)
// The Nested approach, being a distinct type from its constituents, will resolve the usual way modulo possible
// SI-2712 issues (which can be addressed through partial unification), but requires syntactic and runtime overhead from wrapping and unwrapping.

// List is a functor, and List[Int] is a trivial description of a computation producing some number of Int's.

// Natural Transformations
// A polymorphic function that maps from one functor F[_] to another functor G[_] is called a natural transformation,
// and is typically denoted using F ~> G. These functions are extremely important in higher-order functional
// programming.
trait NaturalTransformation[-F[_], +G[_]] {
  def apply[A](fa: F[A]): G[A]
}

type ~>[-F[_], +G[_]] = NaturalTransformation[F, G]

// Functor Composition
//  - Two functors can be composed together in a variety of ways to yield another functor.
case class Composite[F[_], G[_], A](run: F[G[A]])

// The product of two functors is a functor.
case class Product[F[_], G[_], A](run: (F[A], G[A]))

// The sum (or coproduct) of two functors is a functor.
case class Coproduct[F[_], G[_], A](run: Either[F[A], G[A]])

//
// Lifting
//
//  - Often, for some value X, F[X] is referred to as the "lifted" version of X, because it is the same value, but
//  placed "inside" of some functor F. For example, you can lift the function x => x * x inside List by writing List
//  (x => x * x).

//
// Apply
//  - Some functors implement Apply, which provides a way of applying a lifted function F[A => B] to some lifted
//  value F[A] to yield F[B].
//  - Associative Composition
//  -  ap(ap(fa)(fab))(fbc) == ap(fa)(ap(fab)(map(fbc)(_.compose(_).curry))
trait Apply[F[_]] extends functor.Functor[F] {
  def apply[A, B](fa: F[A])(fab: F[A => B]): F[B]
}

//
// Applicative
//  - Some functors that implement Apply also implement Applicative,
//    which provides the ability to lift any value into the functor.
trait Applicative[F[_]] extends Apply[F] {
  def point[A](a: A): F[A]

  // Identity
  //  - ap(fa)(point(_)) == fa
  // Homomorphism
  //  - ap(point(a))(point(ab)) == point(ab(a))
  // Interchange
  //  - ap(point(a))(fab) == ap(fab)(point(_.apply(a)))
  // Derived Map
  //  - map(fa)(ab) == ap(fa)(point(ab))
}

//
// Bind
//  - Some functors that implement Apply also implement Bind, which adds the ability to extend a recipe F[A] with a
//  second recipe that depends on the result of A (A => F[B]), and collapse the result into a single recipe F[B]
trait Bind[F[_]] extends Apply[F] {
  def bind[A, B](fa: F[A])(afb: A => F[B]): F[B]

  // Associative Bind
  //  - bind(bind(fa)(afb))(bfc) == bind(fa)((a) => bind(afb(a))(bfc))
  // Derived Ap
  //  - ap(fa)(fab) == bind(fab)(map(fa)(_))
}

//
// Monad
//  - Some functors that implement Applicative and Bind are Monads.
trait Monad[F[_]] extends Applicative[F] with Bind[F]

// Right Identity
//  bind(fa)(point(_)) = fa
// Left Identity
//  bind(point(a))(afb) == afb(a)

import scala.concurrent.ExecutionContext
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

// map leaves the structure of the context unchanged
List(1, 2, 3)
  .map(_ + 1)
  .map(_ * 2)
  .map(n => s"${n}")

val future: Future[String] = Future(123)
  .map(_ + 1)
  .map(_ * 2)
  .map(n => s"${n}")

Await.result(future, 1.second)

import cats.instances.function._ // for Funtor
import cats.syntax.functor._ // for map

val func1: Int => Double = x => x.toDouble
val func2: Double => Double = _ * 2
(func1 map func2)(1)
(func1 andThen func2)(1)
func2(func1(1))

val func = ((x: Int) => x.toDouble)
  .map(_ + 1)
  .map(_ * 2)
  .map(n => s"${n}")
func(123)

import cats.Functor
import cats.instances.list._
import cats.instances.option._

Functor[List].map(List(1, 2, 3))(_ * 2)
Functor[Option].map(Option(123))(_.toString)

val liftedFunc = Functor[Option].lift(func)
liftedFunc(Option(123))
liftedFunc(Option.empty)

Functor[List].as(List(1, 2, 3), "As")

def doMath[F[_]](start: F[Int])(implicit functor: Functor[F]): F[Int] =
  start.map(_ + 1 * 2)
doMath(Option(20))
doMath(List(1, 2, 3))

// implicit def futureFunctor(implicit ec: ExecutionContext): Functor[Future] =
//   new Functor[Future] {
//     def map[A, B](fa: Future[A])(f: A => B): Future[B] = fa.map(f)
//   }

// Functor.apply[Future]

trait Printable[A] { self =>
  def format(value: A): String

  def contramap[B](func: B => A): Printable[B] =
    new Printable[B] {
      def format(value: B): String = self.format(func(value))
    }
}

def format[A](value: A)(implicit p: Printable[A]): String = p.format(value)

implicit val strPrintable: Printable[String] = new Printable[String] {
  def format(value: String): String = s"${value}"
}
implicit val boolPrintable: Printable[Boolean] = new Printable[Boolean] {
  def format(value: Boolean): String = if (value) "yes" else "no"
}

format("hello")
format(true)
format(false)

final case class Box[A](value: A)

// implicit def boxPrintable[A](implicit p: Printable[A]): Printable[Box[A]] =
//   new Printable[Box[A]] {
//     override def format(value: Box[A]): String = p.format(value.value)
//   }

implicit def boxPrintable[A](implicit p: Printable[A]): Printable[Box[A]] =
  p.contramap(box => box.value)

format(Box("hello world"))
format(Box(true))

trait Codec[A] { self =>
  def encode(value: A): String
  def decode(value: String): A
  def imap[B](dec: A => B, enc: B => A): Codec[B] =
    new Codec[B] {
      override def encode(value: B): String = self.encode(enc(value))
      override def decode(value: String): B = dec(self.decode(value))
    }
}
def encode[A](a: A)(implicit codec: Codec[A]): String = codec.encode(a)

def decode[A](a: String)(implicit codec: Codec[A]): A = codec.decode(a)

implicit val stringCodec: Codec[String] = new Codec[String] {
  def encode(value: String): String = value
  def decode(value: String): String = value
}

implicit val intCodec: Codec[Int] = stringCodec.imap(_.toInt, _.toString)
implicit val booleanCodec: Codec[Boolean] =
  stringCodec.imap(_.toBoolean, _.toString)

implicit def boxCodec[A](implicit c: Codec[A]): Codec[Box[A]] =
  c.imap(Box.apply, _.value)
encode(123)
encode(true)
encode(Box(123))
encode(Box(true))
decode[Boolean]("true")
decode[Box[Boolean]]("false")

import cats.Contravariant
import cats.Show
import cats.instances.string._

val showString = Show[String]
val showSymbol =
  Contravariant[Show].contramap(showString)((sym: Symbol) => s"${sym.name}")
showSymbol.show(Symbol("dave"))

import cats.syntax.contravariant._
showString
  .contramap[Symbol](sym => s"${sym.name}")
  .show(Symbol("dave"))
