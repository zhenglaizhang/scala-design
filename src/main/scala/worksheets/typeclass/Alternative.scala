package worksheets.typeclass

import cats.{Applicative, MonoidK}

import scala.util.Try

// Alternative extends Applicative with a MonoidK. Let’s stub out all the operations just to remind ourselves what
// that gets us.
object a1 {
  trait Alternative[F[_]] extends Applicative[F] with MonoidK[F] {
    // Allows application of a function in an Applicative context to
    // a value in an Applicative context
    def ap[A, B](ff: F[A => B])(fa: F[A]): F[B]

    def pure[A](a: A): F[A] // pure wraps values in the context

    // empty provides the identity element for the combine operation.
    def empty[A]: F[A] //

    // combineK allows us to combine, for any given type A,
    // any two contextual values F[A]
    def combineK[A](x: F[A], y: F[A]): F[A] // <+>
  }
}

import cats.Alternative
import cats.implicits._
object AlternativeApp extends App {
  val empty = Alternative[Vector].empty // Vector()
  val pureOfFive = 5.pure[Vector] // Vector(5)
  val concatenated = 7.pure[Vector] <+> 8.pure[Vector] // Vector(7, 8)

  val double: Int => Int = _ * 2
  val addFive: Int => Int = _ + 5
  val ff: Seq[Int => Int] = double.pure[Vector] <+> addFive.pure[Vector]
  val apForVectors: Seq[Int] =
    (double.pure[Vector] <+> addFive.pure[Vector]) ap concatenated
  // Vector(14, 16, 12, 13)

  println(empty)
  println(pureOfFive)
  println(concatenated)
  println(apForVectors)
}

// Making choices with Alternative
object ParseApp extends App {

  trait Decoder[A] {
    def decode(in: String): Either[Throwable, A]
  }

  object Decoder {
    def from[A](f: String => Either[Throwable, A]): Decoder[A] =
      new Decoder[A] {
        def decode(in: String): Either[Throwable, A] = f(in)
      }
  }

  implicit val decoderAlternative = new Alternative[Decoder] {
    override def pure[A](x: A): Decoder[A] =
      Decoder.from(Function.const(Right(x)))

    // The addition of the Alternative methods allows us to prioritize multiple strategies, compensating for
    // inconsistencies in the source data.
    override def combineK[A](x: Decoder[A], y: Decoder[A]): Decoder[A] =
      new Decoder[A] {
        def decode(in: String) = x.decode(in).orElse(y.decode(in))
      }

    // todo 📌
    override def ap[A, B](ff: Decoder[A => B])(fa: Decoder[A]): Decoder[B] =
      new Decoder[B] {
        def decode(in: String): Either[Throwable, B] =
          fa.decode(in) ap ff.decode(in)
      }

    override def empty[A]: Decoder[A] =
      Decoder.from(Function.const(Left(new Error("No dice."))))
  }

  def parseInt(s: String): Either[Throwable, Int] =
    Try(s.toInt).toEither

  def parseIntFirstChar(s: String): Either[Throwable, Int] =
    Either.catchNonFatal(2 * Character.digit(s.charAt(0), 10))

  // Try first parsing the whole, then just the first character.
  val decoder: Decoder[Int] =
    Decoder.from(parseInt _) <+> Decoder.from(parseIntFirstChar _)

  println(decoder.decode("555")) // Right(55)
  println(decoder.decode("5a")) // Right(10)
}

// Partitioning Results
object PartitionApp extends App {
  // Alternative gives us a notion of partitioning, as long as we also have a Monad available. This is what separate
  // does.
  //
  //The trick here is that the inner type constructor (essentially the replacement for Boolean as the target of our
  // “predicate”) must have a Bifoldable available. A great example of a Bifoldable is Either, and another is Tuple2.

  // Resource holder returns (Request, Status)
  def requestResource(a: Int): Either[(Int, String), (Int, Long)] = {
    if (a % 4 == 0) Left((a, "Bad request"))
    else if (a % 3 == 0) Left(a, "Server error")
    else Right((a, 20L))
  }
  // We can use separate to pull apart the failures and successes zipped with the input, letting us log failures and
  // proceed with successes intelligently. separate will pull the two different outcomes into different sides of a
  // tuple.
  val partitionedResults: (Vector[(Int, String)], Vector[(Int, Long)]) =
    ((requestResource _).pure[Vector] ap Vector(5, 6, 7, 99, 1200, 8,
      22)).separate
  println(partitionedResults)

  val double: Int => Int = _ * 2
  val addFive: Int => Int = _ + 5
  // Surprising regularity in this politico-geographical data model!
  def getRegionAndDistrict(pkey: Int): (Int, Vector[Int]) =
    (
      5 * pkey,
      (double.pure[Vector] <+> addFive.pure[Vector]) ap pkey.pure[Vector]
    )

  val regionsWithDistricts =
    (getRegionAndDistrict _).pure[Vector] ap Vector(5, 6, 7, 97, 1200, 8, 25)

  val regionIds = regionsWithDistricts.separate._1
  val districtIds = regionsWithDistricts.separate._2.flatten
}
