// Invariant Monoidal
// InvariantMonoidal combines Invariant and Semigroupal with the addition of a unit methods,
object w {
  trait InvariantMonoidal[F[_]] {
    def unit: F[Unit]
    def imap[A, B](fa: F[A])(f: A => B)(g: B => A): F[B]
    def product[A, B](fa: F[A], fb: F[B]): F[(A, B)]
  }
}

// Practical uses of InvariantMonoidal appear in the context of codecs, that is interfaces to capture both
// serialization and deserialization for a given format. Another notable examples is Semigroup.

// Semigroup is InvariantMonoidal
// Semigroup forms an invariant functor. Indeed, given a Semigroup[A] and two functions A => B and B => A, one can construct a Semigroup[B] by transforming two values from type B to type A, combining these using the Semigroup[A], and transforming the result back to type B
//
import cats.{InvariantMonoidal, Semigroup}
def unit: Semigroup[Unit] =
  (x: Unit, y: Unit) => ()

def product[A, B](fa: Semigroup[A], fb: Semigroup[B]): Semigroup[(A, B)] =
  new Semigroup[(A, B)] {
    def combine(x: (A, B), y: (A, B)): (A, B) =
      (x, y) match {
        case ((xa, xb), (ya, yb)) => fa.combine(xa, ya) -> fb.combine(xb, yb)
      }
  }

// Given an instance of InvariantMonoidal for Semigroup, we are able to combine existing Semigroup instances to form a new Semigroup by using the Semigroupal syntax:
import cats.implicits._
case class Foo(a: String, c: List[Double])
implicit val fooSemigroup: Semigroup[Foo] =
  (implicitly[Semigroup[String]], implicitly[Semigroup[List[Double]]])
    .imapN(Foo.apply)(Function.unlift(Foo.unapply))

Foo("Hello", List(0.0)) |+| Foo("World", Nil) |+| Foo("!", List(1.1, 2.2))

// CsvCodec is InvariantMonoidal
type CSV = List[String]
trait CsvCodec[A] {
  def read(s: CSV): (Option[A], CSV)
  def write(a: A): CSV
}
// The read method consumes columns from a CSV row and returns an optional value and the remaining CSV. The write
// method produces the CSV representation of a given value.
// forAll { (c: CsvCodec[A], a: A) => c.read(c.write(a)) == ((Some(a), List()))
trait CCUnit {
  def unit: CsvCodec[Unit] =
    new CsvCodec[Unit] {
      def read(s: CSV): (Option[Unit], CSV) = (Some(()), s)
      def write(u: Unit): CSV = List.empty
    }
}

trait CCProduct {
  def product[A, B](fa: CsvCodec[A], fb: CsvCodec[B]): CsvCodec[(A, B)] =
    new CsvCodec[(A, B)] {
      def read(s: CSV): (Option[(A, B)], CSV) = {
        val (a1, s1) = fa.read(s)
        val (a2, s2) = fb.read(s1)
        ((a1, a2).mapN(_ -> _), s2)
      }

      def write(a: (A, B)): CSV =
        fa.write(a._1) ++ fb.write(a._2)
    }
}

// Changing a CsvCodec[A] to CsvCodec[B] requires two functions of type A => B and B => A to transform a value from A to B after deserialized, and from B to A before serialization:
trait CCImap {
  def imap[A, B](fa: CsvCodec[A])(f: A => B)(g: B => A): CsvCodec[B] =
    new CsvCodec[B] {
      def read(s: CSV): (Option[B], CSV) = {
        val (a1, s1) = fa.read(s)
        (a1.map(f), s1)
      }

      def write(a: B): CSV =
        fa.write(g(a))
    }
}

implicit val csvCodecIsInvariantMonoidal: InvariantMonoidal[CsvCodec] =
  new InvariantMonoidal[CsvCodec] with CCUnit with CCProduct with CCImap

val stringCodec: CsvCodec[String] =
  new CsvCodec[String] {
    def read(s: CSV): (Option[String], CSV) = (s.headOption, s.drop(1))
    def write(a: String): CSV = List(a)
  }

def numericSystemCodec(base: Int): CsvCodec[Int] =
  new CsvCodec[Int] {
    def read(s: CSV): (Option[Int], CSV) =
      (
        s.headOption.flatMap(head =>
          scala.util.Try(Integer.parseInt(head, base)).toOption
        ),
        s.drop(1)
      )

    def write(a: Int): CSV =
      List(Integer.toString(a, base))
  }
case class BinDec(binary: Int, decimal: Int)

val binDecCodec: CsvCodec[BinDec] = (
  (numericSystemCodec(2), numericSystemCodec(10))
    .imapN(BinDec.apply)(Function.unlift(BinDec.unapply))
  )

case class Foo(name: String, bd1: BinDec, bd2: BinDec)

val fooCodec: CsvCodec[Foo] = (
  (stringCodec, binDecCodec, binDecCodec)
    .imapN(Foo.apply)(Function.unlift(Foo.unapply))
  )

val foo = Foo("foo", BinDec(10, 10), BinDec(20, 20))
// foo: Foo = Foo("foo", BinDec(10, 10), BinDec(20, 20))

val fooCsv = fooCodec.write(foo)
// fooCsv: CSV = List("foo", "1010", "10", "10100", "20")

fooCodec.read(fooCsv)
// res2: (Option[Foo], CSV) = (
//   Some(Foo("foo", BinDec(10, 10), BinDec(20, 20))),
//   List()
// )

fooCodec.read(fooCodec.write(foo)) == ((Some(foo), List()))
// res3: Boolean = true
