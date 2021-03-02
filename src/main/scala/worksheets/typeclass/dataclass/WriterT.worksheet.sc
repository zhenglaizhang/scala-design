import cats.data.WriterT
// WriterT
// WriterT[F[_], L, V] is a type wrapper on an F[(L, V)].
// Speaking technically, it is a monad transformer for Writer

// Composition
// WriterT can be more convenient to work with than using F[Writer[L, V]] directly, because it exposes operations that allow you to work with the values of the inner Writer (L and V) abstracting both the F and Writer.
// For example, map allow you to transform the inner V value, getting back a WriterT that wraps around it.
WriterT[Option, String, Int](Some(("value", 10))).map(x => x * x)
// Plus, when composing multiple WriterT computations, those will be composed following the same behaviour of a Writer and the generic F
// if one of the computations has a None or a Left, the whole computation will return a None or a Left since the way the two types compose typically behaves that way. Moreover, when the computation succeed, the logging side of the Writers will be combined.
val optionWriterT1: WriterT[Option, String, Int] = WriterT(
  Some(("writerT value 1", 123))
)
val optionWriterT2: WriterT[Option, String, Int] = WriterT(
  Some(("writerT value 1", 123))
)
val optionWriterT3: WriterT[Option, String, Int] = WriterT.valueT(None)

val eitherWriterT1: WriterT[Either[String, *], String, Int] = WriterT(
  Right(("writerT value 1", 123))
)
val eitherWriterT2: WriterT[Either[String, *], String, Int] = WriterT(
  Right(("writerT value 1", 123))
)
val eitherWriterT3: WriterT[Either[String, *], String, Int] =
  WriterT.valueT(Left("error!!!"))
for {
  v1 <- optionWriterT1
  v2 <- optionWriterT2
} yield v1 + v2

for {
  v1 <- optionWriterT1
  v2 <- optionWriterT2
  v3 <- optionWriterT3
} yield v1 + v2 + v3

for {
  v1 <- eitherWriterT1
  v2 <- eitherWriterT2
} yield v1 + v2

for {
  v1 <- eitherWriterT1
  v2 <- eitherWriterT2
  v3 <- eitherWriterT3
} yield v1 + v2 + v3

// Just for completeness, we can have a look at the same example, but with Validated since it as a slightly different behaviour than Either. Instead of short-circuiting when the first error is encountered, Validated will accumulate all the errors. In the following example, you can see how this behaviour is respected when Validated is wrapped as the F type of a WriterT. In addition, notice how flatMap and for comprehension can’t be used in this case, since Validated only extends Applicative, but not Monad.
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import cats.implicits._

val validatedWriterT1: WriterT[Validated[String, *], String, Int] = WriterT(
  Valid(("writerT value 1", 123))
)
val validatedWriterT2: WriterT[Validated[String, *], String, Int] = WriterT(
  Valid(("writerT value 1", 123))
)
val validatedWriterT3: WriterT[Validated[String, *], String, Int] =
  WriterT(Invalid("error 1!!!"): Validated[String, (String, Int)])
val validatedWriterT4: WriterT[Validated[String, *], String, Int] = WriterT(
  Invalid("error 2!!!"): Validated[String, (String, Int)]
)
(validatedWriterT1, validatedWriterT2).mapN((v1, v2) => v1 + v2)

(validatedWriterT1, validatedWriterT2, validatedWriterT3, validatedWriterT4)
  .mapN((v1, v2, v3, v4) => v1 + v2 + v3 + v4)


// Construct a WriterT
// WriterT[F[_], L, V](run: F[(L, V)])
val value : Option[(String, Int)] = Some(("value", 123))
WriterT(value)

// liftF[F[_], L, V](fv: F[V])(implicit monoidL: Monoid[L], F: Applicative[F]): WriterT[F, L, V]
// This function allows you to build the datatype starting from the value V wrapped into an F. Notice how it requires:
//  - Monoid[L], since it uses the empty value from the typeclass. to fill the L value not specified in the input.
//  - Applicative[F] to modify the inner value.
import cats.instances.option._
val value1: Option[Int] = Some(123)
WriterT.liftF[Option, String, Int](value1)
// WriterT.put[F[_], L, V](v: V)(l: L)(implicit applicativeF: Applicative[F]): WriterT[F, L, V]
WriterT.put[Option, String, Int](123)("initial value")
WriterT.putT[Option, String, Int](Some(123))("initial value")


// Operations
// In the Writer definition section, we showed how it is actually a WriterT. Therefore, all the operations described into Writer operations are valid for WriterT as well.
// Most of the WriterT functions require a Functor[F] or Monad[F] instance. However, Cats provides all the necessary instances for the Id type, therefore we don’t have to worry about them
import cats.data.WriterT

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

// Mocked HTTP calls
def pingService1() : Future[Int] = Future.successful(100)
def pingService2() : Future[Int] = Future.successful(200)
def pingService3() : Future[Int] = Future.successful(50)
def pingService4() : Future[Int] = Future.successful(75)

// Using WriterT we can log each step of our application, compute, the time and work within the Future effect.
def pingToWriterT(ping: Future[Int], serviceName: String) : WriterT[Future, String, Int] =
    WriterT.valueT[Future, String, Int](ping)
        .tell(s"ping to $serviceName ")
        .flatMap(pingTime => WriterT.put(pingTime)(s"took $pingTime \n"))

val resultWriterT: WriterT[Future, String, Int] = for {
    ping1 <- pingToWriterT(pingService1(), "service #1")
    ping2 <- pingToWriterT(pingService2(), "service #2")
    ping3 <- pingToWriterT(pingService3(), "service #3")
    ping4 <- pingToWriterT(pingService4(), "service #4")
  } yield ping1 + ping2 + ping3 + ping4

val resultFuture: Future[String] = resultWriterT.run.map {
    case (log: String, totalTime: Int) => s"$log> Total time: $totalTime"
}
Await.result(resultFuture, Duration.Inf)
