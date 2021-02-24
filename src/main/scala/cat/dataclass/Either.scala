package cat.dataclass

import cats.effect.{ExitCode, IO, IOApp}
import scala.util.Try
import cats.implicits._

object wrap1 {
  // E: failure value
  // A: correct value
  sealed abstract class Either[+E, +A]
  case class Left[+E, +A](error: E) extends Either[E, A]
  case class Right[+E, +A](value: A) extends Either[E, A]
  Either.left("error") // smart ctor for Left(...)
  Either.right("value") // smart ctor for Right(...)
  // usually not frequent to pattern match on either
  // Either forms an Monad, so we can compose multiple Eithers
}

object EitherApp extends IOApp {
  type Parse[A] = Either[Throwable, A]
  def integer1(s: String): Parse[Int] = Try(s.toInt).toEither
  def integer2(s: String): Parse[Int] =
    Either.fromOption(s.toIntOption, new Throwable("error"))
  def integer3(s: String): Parse[Int] = Either.catchNonFatal(s.toInt)

  def add(s1: String, s2: String): Parse[Int] =
    for {
      a <- integer1(s1)
      b <- integer2(s2)
    } yield a + b

  def run(args: List[String]): IO[ExitCode] = {
    IO {
      println(Either.cond(false, "success value", new Throwable("error")))
      println(Either.fromOption(None, new Throwable("error")))
      println(integer1("32"))
      println(add("12", "13"))
      println(
        add("12", "error")
      ) // monadic sequencing, flatMap takes error path into consideration
      val r = add("-8", "4")
        .flatMap(it =>
          Either.cond(it >= 0, it, new Throwable(s"$it is not positive"))
        )
        .leftMap(e => e.getMessage)
        .fold(
          err => "value has errors: " + err,
          v => "values was correct: " + v
        )
      println(r)
    }.as(ExitCode.Success)
  }
}
