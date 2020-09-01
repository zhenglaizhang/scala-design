object show {
  trait Semigroup[A] {
    // be associative
    def combine(x: A, y: A): A
  }
  trait Monoid[A] extends Semigroup[A] {
    // identity element
    def empty: A
  }
}

import cats.Monoid
import cats.instances.string._

Monoid[String].combine("Hi ", "there")
Monoid[String].empty
Monoid.apply[String].combine("Hi ", "there")
Monoid.apply[String].empty

import cats.instances.int._
import cats.instances.option._
val a = Option(22)
val b = Option(20)
Monoid[Option[Int]].combine(a, b)

// wow, import everyting...
import cats.syntax.semigroup._

val strResult = "Hi " |+| "there" |+| Monoid[String].empty

val intResult = 1 |+| 2 |+| Monoid[Int].empty

final case class Order(totalCost: Double, quantity: Double)

implicit val orderMonoid = Monoid.instance[Order](
  Order(0, 0),
  (o1, o2) => Order(o1.totalCost + o2.totalCost, o1.quantity + o2.quantity)
)

def add[A](items: List[A])(implicit m: Monoid[A]): A =
  items.foldLeft(m.empty)(m.combine)

add(List(1, 2, 3))
add(List(Order(1, 1), Order(2, 3)))
