// Laws are an important part of cats. Cats uses discipline to define type class laws and the ScalaCheck tests based on them.
import cats._
sealed trait Tree[+A]
case object Leaf extends Tree[Nothing]
case class Node[A](p: A, left: Tree[A], right: Tree[A]) extends Tree[A]

object Tree {
  implicit val treeFunctor: Functor[Tree] = new Functor[Tree] {
    override def map[A, B](fa: Tree[A])(f: A => B): Tree[B] = fa match {
      case Leaf => Leaf
      case Node(p, left, right) => Node(f(p), map(left)(f), map(right)(f))
    }
  }
}
// Cats defines all type class laws tests in cats.laws.discipline.* as discipline’s RuleSets
// todo
// https://typelevel.org/cats/typeclasses/lawtesting.html

implicit def eqTree[A: Eq]: Eq[Tree[A]] = Eq.fromUniversalEquals