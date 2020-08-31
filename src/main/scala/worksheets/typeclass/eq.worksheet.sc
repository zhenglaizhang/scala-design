import java.{util => ju}
// programmer error => comparing Int to Option[Int]
// not a type error technically
List(1, 2, 3).map(Option(_)).filter((it => it == 1))

import cats.Eq
import cats.instances.int._

val eqInt = Eq[Int]
eqInt.eqv(123, 234)

// eqInt.eqv(123, Some(12)) // type mismatch compilation error

import cats.syntax.eq._
123 === 123
123 =!= 234

import cats.instances.option._
// Some(1) === None
(Some(1): Option[Int]) === (None: Option[Int])
Option(1) === Option.empty[Int]

import cats.syntax.option._
import cats.instances.long._
1.some === none[Int]
1.some =!= none[Int]

implicit val dateEq: Eq[ju.Date] = Eq.instance[ju.Date] { (d1, d2) =>
  d1.getTime === d2.getTime
}

val x = new ju.Date()
val y = new ju.Date()
x === y
x === x