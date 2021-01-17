import shapeless._
import shapeless.ops.hlist

("Hello" :: 123 :: true :: HNil).last
("Hello" :: 123 :: true :: HNil).init

//HNil.last
//Implicit not found: shapeless.Ops.Last[shapeless.HNil.type]. shapeless.HNil.type is empty, so there is no last element.

trait Penultimate[L] {
  type Out
  def apply(l: L): Out
}

object Penultimate {
  type Aux[L, O] = Penultimate[L] { type Out = O }
  def apply[L](implicit p: Penultimate[L]): Aux[L, p.Out] = p
}

implicit def hlistPenultimate[L <: HList, M <: HList, O](implicit
    init: hlist.Init.Aux[L, M],
    last: hlist.Last.Aux[M, O]
): Penultimate.Aux[L, O] =
  new Penultimate[L] {
    type Out = O

    def apply(l: L): O = last.apply(init.apply(l))
  }

type BigList = String :: Int :: Boolean :: Double :: HNil
val bigList: BigList = "a" :: 12 :: false :: 12.1 :: HNil
Penultimate[BigList].apply(bigList)

implicit class PenultimateOps[A](a: A) {
  def penultimate(implicit inst: Penultimate[A]): inst.Out = inst.apply(a)
}

bigList.penultimate

case class IceCreamV1(name: String, numCherries: Int, inCone: Boolean)
case class IceCreamV2a(name: String, inCone: Boolean) // remove fields
case class IceCreamV2b(
    name: String,
    inCone: Boolean,
    numCherries: Int
) // reorder fields
case class IceCreamV2c(
    name: String,
    inCone: Boolean,
    numCherries: Int,
    numWaffles: Int
) // insert fields

trait Migration[A, B] {
  def apply(a: A): B
}

//IceCreamV1("Sundae", 1, false).migrateTo[IceCreamV2a]
implicit class MigrationOps[A](a: A) {
  def migrateTo[B](implicit m: Migration[A, B]): B = m.apply(a)
}
