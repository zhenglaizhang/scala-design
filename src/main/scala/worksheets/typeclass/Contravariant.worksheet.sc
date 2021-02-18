// The Contravariant type class is for functors that define a contramap function with the following type:
//    def contramap[A, B](fa: F[A])(f: B => A): F[B]
// It looks like regular (also called Covariant) Functor’s map, but with the f transformation reversed.
// Generally speaking, if you have some context F[A] for type A, and you can get an A value out of a B value — Contravariant allows you to get the F[B] context for B.
// Examples of Contravariant instances are Show and scala.math.Ordering (along with cats.kernel.Order).

import cats._
import cats.implicits._

case class Money(amount: Int)
case class Salary(size: Money)
implicit val showMoney: Show[Money] = Show.show(m => s"$$${m.amount}")
implicit val showSalary: Show[Salary] = showMoney.contramap(_.size)
Salary(Money(12)).show

// The scala.math.Ordering type class defines comparison operations, e.g. compare:
Ordering.Int.compare(1, 2)
Ordering.Int.compare(2, 1)
Ordering.Int.compare(2, 2)

// def by[T, S](f: T => S)(implicit ord: Ordering[S]): Ordering[T]
// In fact, it is just contramap, defined in a slightly different way! We supply T => S to receive F[S] => F[T] back.
import scala.math.Ordered._
implicit val moneyOrdering: Ordering[Money] = Ordering.by(_.amount)
Money(100) < Money(200)


// Contravariant functors have a natural relationship with subtyping, dual to that of covariant functors:
class A 
class B extends A
val b: B = new B
val a: A = b
val showA: Show[A] = Show.show(a => "a!")
val showB1: Show[B] = showA.contramap(b => b: A)
val showB2: Show[B] = showA.contramap(identity[A])
val showB3: Show[B] = Contravariant[Show].narrow[A, B](showA)
// Subtyping relationships are “lifted backwards” by contravariant functors, such that if F is a lawful contravariant functor and B <: A then F[A] <: F[B], which is expressed by Contravariant.narrow.




