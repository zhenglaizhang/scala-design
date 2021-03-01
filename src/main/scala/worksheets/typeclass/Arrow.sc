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
//  define a combine function that combines two arrows into a single arrow, which takes an input and processes two copies of it with two arrows. combine can be defined in terms of Arrow operations lift, >>> and ***:
def combine[F[_, _]: Arrow, A, B, C](
    fab: F[A, B],
    fac: F[A, C]
): F[A, (B, C)] =
  Arrow[F].lift((a: A) => (a, a)) >>> (fab *** fac)

val mean: List[Int] => Double =
  combine((_: List[Int]).sum, (_: List[Int]).size) >>> {
    case (x, y) => x.toDouble / y
  }

val variance: List[Int] => Double =
// Variance is mean of square minus square of mean
  combine(((_: List[Int]).map(x => x * x)) >>> mean, mean) >>> {
    case (x, y) => x - y * y
  }

val meanAndVar: List[Int] => (Double, Double) = combine(mean, variance)
meanAndVar(List(1, 2, 3, 4))

// Above impl may not be optimized, however Arrow methods are more general and provide a common structure for type
// constructors that have Arrow
// instances. They are also a more abstract way of stitching computations together.

// Kleisli
// A Kleisli[F[_], A, B] represents a function A => F[B]. You cannot directly compose an A => F[B] with a B => F[C] with functional composition, since the codomain of the first function is F[B] while the domain of the second function is B; however, since Kleisli is an arrow (as long as F is a monad), you can easily compose Kleisli[F[_], A, B] with Kleisli[F[_], B, C] using Arrow operations.
import cats.data.Kleisli
val headK = Kleisli((_: List[Int]).headOption)
val lastK = Kleisli((_: List[Int]).lastOption)
//val headPlusLast = combine(headK, lastK) >>> Arrow[Kleisli[Option, *, *]].lift(
//  ((_: Int) + (_: Int)).tupled
//)
//headPlusLast.run(List(2, 3, 5, 8))
//// res1: Option[Int] = Some(10)
//headPlusLast.run(Nil)
//// res2: Option[Int] = None

// FancyFunction
// We shall create a fancy version of Function1 called FancyFunction, that is capable of maintaining states. We then create an Arrow instance for FancyFunction and use it to compute the moving average of a list of numbers.
// That is, given an A, it not only returns a B, but also returns a new FancyFunction[A, B]. This sounds similar to
// the State monad (which returns a result and a new State from an initial State), and indeed, FancyFunction can be
// used to perform stateful transformations.
case class FancyFunction[A, B](run: A => (FancyFunction[A, B], B))

def runList[A, B](ff: FancyFunction[A, B], xs: List[A]): List[B] =
  xs match {
    case h :: t =>
      val (ff2, b) = ff.run(h)
      b :: runList(ff2, t)
    case _ => Nil
  }

implicit val arrowInstance: Arrow[FancyFunction] = new Arrow[FancyFunction] {

  override def lift[A, B](f: A => B): FancyFunction[A, B] =
    FancyFunction(lift(f) -> f(_))

  override def first[A, B, C](
      fa: FancyFunction[A, B]
  ): FancyFunction[(A, C), (B, C)] =
    FancyFunction {
      case (a, c) =>
        val (fa2, b) = fa.run(a)
        (first(fa2), (b, c))
    }

  override def id[A]: FancyFunction[A, A] = FancyFunction(id -> _)

  override def compose[A, B, C](
      f: FancyFunction[B, C],
      g: FancyFunction[A, B]
  ): FancyFunction[A, C] =
    FancyFunction { a =>
      val (gg, b) = g.run(a)
      val (ff, c) = f.run(b)
      (compose(ff, gg), c)
    }
}

def accum[A, B](b: B)(f: (A, B) => B): FancyFunction[A, B] =
  FancyFunction { a =>
    val b2 = f(a, b)
    (accum(b2)(f), b2)
  }

runList(accum[Int, Int](0)(_ + _), List(6, 5, 4, 3, 2, 1))

// We first define arrow sum in terms of accum, and define arrow count by composing _ => 1 with sum:

import cats.kernel.Monoid

def sum[A: Monoid]: FancyFunction[A, A] = accum(Monoid[A].empty)(_ |+| _)
def count[A]: FancyFunction[A, Int] =
  Arrow[FancyFunction].lift((_: A) => 1) >>> sum
def avg: FancyFunction[Int, Double] =
  combine(sum[Int], count[Int]) >>> Arrow[FancyFunction].lift {
    case (x, y) => x.toDouble / y
  }
runList(avg, List(1, 10, 100, 1000))
