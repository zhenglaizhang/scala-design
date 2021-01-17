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

import shapeless.{::, Coproduct, HList, HNil, Lazy}

import java.util.Date
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

import shapeless.{:+:, CNil, Inl, Inr}
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
  // summoner method
  def apply[A: CsvEncoder](): CsvEncoder[A] = implicitly[CsvEncoder[A]]

  def the[A: CsvEncoder](): CsvEncoder[A] = this.apply()

  // smart constructor
  def instance[A](f: A => List[String]): CsvEncoder[A] =
    new CsvEncoder[A] {
      def encode(v: A): List[String] = f(v)
    }

  def pure[A](f: A => List[String]): CsvEncoder[A] = this.instance(f)

  def enc[A: CsvEncoder](v: A): List[String] =
    implicitly[CsvEncoder[A]].encode(v)

}

object CsvEncoderInstances {
  implicit val employeeEncoder: CsvEncoder[Employee] =
    (e: Employee) =>
      List(
        e.name,
        e.number.toString,
        if (e.manager) "yes" else "no"
      )

  implicit val booleanEncoder: CsvEncoder[Boolean] =
    CsvEncoder.instance(b => if (b) List("yes") else List("no"))

  implicit val stringEncoder: CsvEncoder[String] =
    CsvEncoder.instance(s => List(s))

  implicit val intEncoder: CsvEncoder[Int] =
    CsvEncoder.instance(i => List(i.toString))

  implicit val doubleEncoder: CsvEncoder[Double] =
    CsvEncoder.instance(d => List(d.toString))

  implicit val hnilEncoder: CsvEncoder[HNil] = CsvEncoder.instance(_ => Nil)
//  implicit def hlistEncoder[H, T <: HList](implicit
//                                           he: CsvEncoder[H],
//                                           te: CsvEncoder[T]): CsvEncoder[H :: T] = new CsvEncoder[HList] {
//    override def encode(v: HList): List[String] = v match {
//      case HNil => ???
//      case (h: H) :: (t: T) => he.encode(h) ++ te.encode(t)
//    }
//  }
  implicit def hlistEncoder[H, T <: HList](implicit
      he: Lazy[CsvEncoder[H]],
      te: CsvEncoder[T]
  ): CsvEncoder[H :: T] =
    CsvEncoder.instance {
      case (h: H) :: (t: T) => he.value.encode(h) ++ te.encode(t)
    }

  implicit val cnilEncoder: CsvEncoder[CNil] =
    CsvEncoder.instance(cnil => throw new Exception("inconceivable"))
  implicit def coproductEncoder[H, T <: Coproduct](implicit
      // todo: ???
      he: Lazy[CsvEncoder[H]],
      te: CsvEncoder[T]
  ): CsvEncoder[H :+: T] =
    CsvEncoder.instance {
      case Inl(l) => he.value.encode(l)
      case Inr(r) => te.encode(r)
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

case class C2(
    v1: Int,
    v2: Int,
    v3: Int,
    v4: Int,
    v5: Int,
    v6: Int,
    v7: Int,
    v8: Int,
    v9: Int,
    v10: Int,
    v11: Int,
    v12: Int,
    v13: Int,
    v14: Int,
    v15: Int,
    v16: Int,
    v17: Int,
    v18: Int,
    v19: Int,
    v20: Int,
    v21: Int,
    v22: Int,
    v23: Int,
    v24: Int
)

val reprEnc = CsvEncoder[Boolean :: String :: Double :: HNil]
val repr2: CsvEncoder[Boolean :: String :: Double :: HNil] = implicitly
reprEnc.encode(true :: "A" :: 1.2 :: HNil)

//implicit val iceEncoder: CsvEncoder[IceCream] = {
//  val gen = Generic[IceCream]
//  val reprEnc = CsvEncoder[gen.Repr]
//  CsvEncoder.instance(ic => reprEnc.encode(gen.to(ic)))
//}

implicit def genericEncoder[A, R](implicit
//    gen: Generic[A] { type Repr = R },
    // todo: ???
    gen: Generic.Aux[A, R],
    enc: Lazy[CsvEncoder[R]]
): CsvEncoder[A] =
  CsvEncoder.instance(ic => enc.value.encode(gen.to(ic)))

case class People(name: String, age: Int, male: Boolean)

IceCream("a", 1, false).toCsv
People("p", 12, false).toCsv

case class Wow(f: Double, b: Boolean)
Wow(12.0, true).toCsv

case class Booking(room: String, date: Date)
// Booking("adfa", new Date()).toCsv
// could not find implicit value for parameter e: CsvEncoder[Booking]

val shapes: List[Shape] = List(Rectangle(1, 1), Circle(1))

def writeCsv[A: CsvEncoder](xs: List[A]): String =
  xs.map(x => implicitly[CsvEncoder[A]].encode(x).mkString(",")).mkString("\n")

writeCsv(shapes)

case class Bar(baz: Int, d: Double)
case class Foo(bar: Bar)
Foo(Bar(1, 1.0)).toCsv

// recursive type
sealed trait Tree[A]
case class Branch[A](left: Tree[A], right: Tree[A]) extends Tree[A]
case class Leaf[A](v: A) extends Tree[A]
CsvEncoder[Tree[Int]]
// diverging implicit expansion for type CsvEncoder[Tree[Int]]

case class Meow(f: Float, d: Double)

//CsvEncoder[Meow]
//could not find implicit value for evidence parameter of type CsvEncoder[Meow]

//CsvEncoder[Float :: Double :: HNil]
//could not find implicit value for evidence parameter of type CsvEncoder[Float :: Double :: shapeless.HNil]

//CsvEncoder[Float]
//could not find implicit value for evidence parameter of type CsvEncoder[Float]

CsvEncoder[Double]

//import scala.reflect
// todo ??? reflect.reify missing

// gen.Repr is a dependent type
def getRepr[A](a: A)(implicit gen: Generic[A]): gen.Repr = gen.to(a)

getRepr(People("w", 12, true))

import shapeless.ops.hlist.Last

val last1 = Last[String :: Double :: HNil]
last1("a" :: 2.0 :: HNil)
//last1("a" :: HNil)
//type mismatch;
//found   : String :: shapeless.HNil
//required: String :: Double :: shapeless.HNil

trait Second[L <: HList] {
  type Out
  def apply(v: L): Out
}

object Second {
  type Aux[L <: HList, O] = Second[L] { type Out = O }
  def apply[L <: HList](implicit inst: Second[L]): Aux[L, inst.Out] = inst
}

import Second._

implicit def hlistSecond[A, B, Rest <: HList]: Aux[A :: B :: Rest, B] =
  new Second[A :: B :: Rest] {
    override type Out = B
    override def apply(v: A :: B :: Rest): Out = v.tail.head
  }

val second1 = Second[Double :: Int :: String :: HNil]
second1(12.0 :: 12 :: "s" :: HNil)

//Second[Boolean :: HNil]
//could not find implicit value for parameter inst: Second[Boolean :: shapeless.HNil]
