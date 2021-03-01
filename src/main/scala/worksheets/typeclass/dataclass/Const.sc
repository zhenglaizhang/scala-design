// Const
// todo https://typelevel.org/cats/datatypes/const.html
// The Const data type can be thought of similarly to the const function, but as a data type.
object w {
  // The const function takes two arguments and simply returns the first argument, ignoring the second.
  def const[A, B](a: A)(b: => B): A = a

  // The Const data type takes two type parameters, but only ever stores a value of the first type parameter. Because
  // the second type parameter is not used in the data type, the type parameter is referred to as a “phantom type”.
  case class Const[A, B](getConst: A)

}

// the type parameter B is referred to as a “phantom type”.
// case class Const[A, B](getConst: A)

// Types that contain other types are common across many programming paradigms
// A lens can be thought of as a first class getter/setter.
// A Lens[S, A] is a data type that knows how to get an A out of an S, or set an A in an S.
trait Lens[S, A] {
  def get(s: S): A

  def set(s: S, a: A): S

  def modify(s: S)(f: A => A): S = set(s, f(get(s)))

  // Effectful modifications as well -
  // perhaps our modification can fail (Option) or can return several values (List).
  // We extract them as Functor
  def modifyOption(s: S)(f: A => Option[A]): Option[S] =
    f(get(s)).map(a => set(s, a))

  def modifyList(s: S)(f: A => List[A]): List[S] =
    f(get(s)).map(a => set(s, a))
}

import cats.Functor
import cats.Id
import cats.implicits._

trait Lens[S, A] {
  def get(s: S): A

  def set(s: S, a: A): S

  def modify(s: S)(f: A => A): S = modifyF[Id](s)(f)

  def modifyF[F[_]: Functor](s: S)(f: A => F[A]): F[S] =
    f(get(s)).map(a => set(s, a))
}

trait Lens[S, A] {
  // Looking at modifyF, we have an S we can pass in. The tricky part will be the A => F[A],
  // and then somehow getting an A out of F[S]. If we imagine F to be a type-level constant function however,
  // we could imagine it would simply take any type and return some other constant type, an A perhaps.
  // This suggests our F is a Const.
  def modifyF[F[_]: Functor](s: S)(f: A => F[A]): F[S]

  def modify(s: S)(f: A => A): S = modifyF[Id](s)(f)

  def set(s: S, a: A): S = modify(s)(_ => a)

  def get(s: S): A
}
