import cats.Functor
// In the Applicative tutorial we saw a more polymorphic version of the standard library Future.traverse and Future.sequence functions, 
// generalizing Future to be any F[_] that’s Applicative.
//  * Traversal over a structure with an effect.
//  *
//  * Traversing with the [[cats.Id]] effect is equivalent to [[cats.Functor]]#map.
//  * Traversing with the [[cats.data.Const]] effect where the first type parameter has
//  * a [[cats.Monoid]] instance is equivalent to [[cats.Foldable]]#fold.

// In general t.map(f).sequence can be replaced with t.traverse(f).

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

def getUptime(hostname: String): Future[Int] = Future(hostname.length * 60)
val hostnames = List("abc.com", "defg.com", "abcdefg.com")
val allUptimes: Future[List[Int]] = Future.sequence(hostnames.map(getUptime))
// parallel map
val allUptimes2: Future[List[Int]] = Future.traverse(hostnames)(getUptime)
val allUptimes3: Future[List[Int]] = hostnames.foldLeft(Future(List.empty[Int])) {
  (acc, host) =>
    val up = getUptime(host)
    for {
      acctime <- acc
      uptime <- up
    } yield acctime :+ uptime
}


import cats.Applicative

object w {
  def traverse[F[_]: Applicative, A, B](xs: List[A])(f: A => F[B]): F[List[B]] = 
    xs.foldRight(Applicative[F].pure(List.empty[B]))((a: A, acc: F[List[B]]) => {
      val fb: F[B] = f(a)
      Applicative[F].map2(fb, acc)(_ :: _)
    })
}

object tree {
  // todo why extends Product with Serializable
  sealed abstract class Tree[A] extends Product with Serializable {
    def traverse[F[_]: Applicative, B](f: A => F[B]): F[Tree[B]] = this match {
      case Tree.Empty() => Applicative[F].pure(Tree.Empty())
      case Tree.Branch(v, l, r) => Applicative[F].map3(f(v), l.traverse(f), r.traverse(f))(Tree.Branch(_, _ ,_))
    }
  }

  object Tree {
    final case class Empty[A]() extends Tree[A]
    final case class Branch[A](value: A, left: Tree[A], right: Tree[A]) extends Tree[A]
  }
}

import tree._
// This suggests an abstraction over “things that can be traversed over,” hence Traverse.
object m {
  trait Traverse[F[_]] {
    def traverse[G[_]: Applicative, A, B](fa: F[A])(f: A => G[B]): G[F[B]]
  }

  implicit val traverseForList: Traverse[List] = new Traverse[List] {
    def traverse[G[_]: Applicative, A, B](fa: List[A])(f: A => G[B]): G[List[B]] = 
      fa.foldRight(Applicative[G].pure(List.empty[B])){ (a: A, acc: G[List[B]]) => 
        Applicative[G].map2(f(a), acc)(_ :: _)
      }
  }

  implicit val traverseForTree: Traverse[Tree] = new Traverse[Tree] {
    def traverse[G[_]: Applicative, A, B](fa: Tree[A])(f: A => G[B]): G[Tree[B]] = fa.traverse(f)
  }
}

// Sometimes you will be given a traversable that has effectful values already, such as a List[Option[A]]. 
// Since the values themselves are effects, traversing with identity will turn the traversable “inside out.”

import cats.implicits._
val xs = List(Some(1), Some(2), None)
val traversed = xs.traverse(identity)
// Cats provides a convenience method for this called sequence.
val traversed2 = xs.sequence


// Traversables are Functors
//  - As it turns out every Traverse is a lawful Functor. By carefully picking the G to use in traverse we can implement map.
object x {
  import cats.{ Applicative, Traverse }
  def traverse[F[_]: Traverse, G[_]: Applicative, A, B](fa: F[A])(f: A => G[B]): G[F[B]] = ???
  def map[F[_]: Traverse, A, B](fa: F[A])(f: A => B): F[B] = ???
  // Both have an F[A] parameter and a similar f parameter. traverse expects the return type of f to be G[B] whereas map just wants B. 
  // Similarly the return type of traverse is G[F[B]] whereas for map it’s just F[B]. 
  // This suggests we need to pick a G such that G[A] communicates exactly as much information as A. 
  // We can conjure one up by simply wrapping an A.

  final case class Id[A](value: A)
  implicit val applicativeForId: Applicative[Id] = new Applicative[Id] {
    override def ap[A, B](ff: Id[A => B])(fa: Id[A]): Id[B] = Id(ff.value(fa.value))
    override def pure[A](x: A): Id[A] = Id(x)
  }

  trait Traverse[F[_]] extends Functor[F] {
    def traverse[G[_]: Applicative, A, B](fa: F[A])(f: A => G[B]): G[F[B]]
    def map[A, B](fa: F[A])(f: A => B): F[B] = traverse(fa)(a => Id(f(a))).value
    // Id is provided in Cats as a type alias type Id[A] = A.
  }
}

// Traversables are Foldable
// The Foldable type class abstracts over “things that can be folded over” similar to how Traverse abstracts over “things that can be traversed.” It turns out Traverse is strictly more powerful than Foldable - that is, foldLeft and foldRight can be implemented in terms of traverse by picking the right Applicative. However, cats.Traverse does not implement foldLeft and foldRight as the actual implementation tends to be ineffecient.
// For brevity and demonstration purposes we’ll implement the equivalent foldMap method in terms of traverse by using cats.data.Const. You can then implement foldRight in terms of foldMap, and foldLeft can then be implemented in terms of foldRight, though the resulting implementations may be slow.
import cats.{Applicative, Monoid, Traverse}
import cats.data.Const

def foldMap[F[_]: Traverse, A, B: Monoid](fa: F[A])(f: A => B): B =
  Traverse[F].traverse[Const[B, *], A, B](fa)(a => Const(f(a))).getConst
// This works because Const[B, *] is an Applicative if B is a Monoid, as explained in the documentation of Const.