sealed trait Shape // product
final case class Rectangle(width: Double, height: Double) extends Shape
final case class Circle(radius: Double) extends Shape

def area(s: Shape): Double =
  s match {
    case Circle(radius)           => math.Pi * radius * radius
    case Rectangle(width, height) => width * height
  }

// algebra = symbols + rules (methods)

//
// alternative encodings
//
type Rect = (Double, Double)
type Circ = Double
type Shap = Either[Rect, Circ]
def area2(s: Shap): Double =
  s match {
    case Left((w, h)) => w * h
    case Right(r)     => math.Pi * r * r
  }

// Shap is a more generic encoding than Shape

// tuple to encode product type?
//  tupleN is of different type
//  no tuple0, Unit?

import shapeless.{HList, ::, HNil}
val product: String :: Int :: Boolean :: HNil = "Sunday" :: 1 :: false :: HNil
product.head
product.tail
product.tail.head
product.tail.tail
// compiler knows the exact length of HList
// product.tail.tail.tail.tail

// prepend an element
32L :: product

import shapeless.Generic
case class IceCream(name: String, numCherries: Int, inCone: Boolean)
val iceCreamGen = Generic[IceCream]
val ic = IceCream("Sundate", 1, false)
val repr = iceCreamGen.to(ic)
iceCreamGen.from(repr)

case class Employee(name: String, number: Int, manager: Boolean)
val e = Generic[Employee].from(repr)

import shapeless.{Coproduct, :+:, CNil, Inl, Inr}
case class Red()
case class Amber()
case class Green()
type Light = Red :+: Amber :+: Green :+: CNil
val red: Light = Inl(Red())
val green: Light = Inr(Inr(Inl(Green())))
val gen = Generic[Shape]
gen.to(Rectangle(3, 4))
gen.to(Circle(1.0))

// HList for product types
// Coproducts for coproduct types

trait CsvEncoder[A] {
  def encode(v: A): List[String]
}

object CsvEncoder {
  def enc[A: CsvEncoder](v: A): List[String] =
    implicitly[CsvEncoder[A]].encode(v)

}

object CsvEncoderInstances {
  implicit val employeeEncoder: CsvEncoder[Employee] =
    new CsvEncoder[Employee] {
      def encode(v: Employee): List[String] =
        List(
          e.name,
          e.number.toString,
          if (e.manager) "yes" else "no"
        )
    }

}

import CsvEncoderInstances._
CsvEncoder.enc(e)

object CsvEncoderSyntax {
  implicit class CsvEncoderOps[A](v: A) {
    def toCsv(implicit e: CsvEncoder[A]): List[String] = e.encode(v)
  }
}
import CsvEncoderSyntax._
e.toCsv
