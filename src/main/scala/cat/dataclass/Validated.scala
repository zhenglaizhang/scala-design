package cat.dataclass

import cats.Applicative
import cats.data.Validated
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import cats.syntax.apply._

object ValidateApp1 extends IOApp {
  case class Person(name: String, age: Int)

  type AppResult[A] = Either[List[String], A] // List of errors, A result type

  def validateUsername(str: String): AppResult[String] = {
    Either.cond(str == "myUsername", str, List(s"username is not correct"))
  }

  def validateAg1(s: String): AppResult[Int] =
    Either.catchNonFatal(s.toInt).leftMap(_ => List("age is not an integer"))

  def run(args: List[String]): IO[ExitCode] = {
    IO {
      val r = for {
        name <- validateUsername("wrongUsername")
        age <- validateAg1("notinteger")
      } yield Person(name, age)
      println(r)

      // todo fix this
//      val r2: Either[List[String], Person] = (
//        validateUsername("wrong").toValidated,
//        validateAg1("arongai").toValidated
//      ).mapN(Person.apply).toEither
//      println(r2)
    }.as(ExitCode.Success)
  }
}

object ValidateApp2 extends IOApp {
  case class Person(name: String, age: Int)

  type AppResult[A] =
    Validated[List[String], A] // List of errors, A result type
  // not a monad, no flatMap,
  // flatMap => sequence computation
  // but applicative functor => parallel computation

//  def validateUsername(str: String): AppResult[String] = {
//    Either
//      .cond(str == "myUsername", str, List(s"username is not correct"))
//      .toValidated
//  }
//
//  def validateAg1(s: String): AppResult[Int] =
//    Either
//      .catchNonFatal(s.toInt)
//      .leftMap(_ => List("age is not an integer"))
//      .toValidated
  def validateUsername(str: String): AppResult[String] = {
    Validated
      .cond(str == "myUsername", str, List(s"username is not correct"))
  }

  def validateAg1(s: String): AppResult[Int] =
    Validated
      .catchNonFatal(s.toInt)
      .leftMap(_ => List("age is not an integer"))

  def run(args: List[String]): IO[ExitCode] = {
//    IO {
//      val r = for {
//        name <- validateUsername("wrongUsername")
//        age <- validateAg1("notinteger")
//      } yield Person(name, age)
//      println(r)
//    }.as(ExitCode.Success)
    IO {
      val r = Applicative[Validated[List[String], *]]
        .product(validateUsername("wrong"), validateAg1("notanint"))
        .map(Person.tupled)
      println(r)

//      val r2: AppResult[Person] =
//        (validateUsername("wrong"), validateAg1("notw")).mapN(Person.apply)
//      println(r2)

      val r3 = Applicative[Validated[List[String], *]]
        .map2(validateUsername("wrong"), validateAg1("wroint"))(Person.apply)
      println(r3)

//      val r4 =
//        (validateUsername("wronga") |@| validateAg1("wrong")).map(Person.apply)
      // |@| is deprecated

//      val r5 =
//        (validateUsername("wron"), validateAg1("wra")).parMapN(Person.apply)
//      println(r5)
    }.as(ExitCode.Success)
  }
}
