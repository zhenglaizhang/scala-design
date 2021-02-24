package cat.dataclass

import cats.effect.IOApp
import cats.effect.IO
import cats.data.Ior
import cats.effect.ExitCode
import cats.Functor
import cats.implicits._
import scala.Either
import cats.data.OptionT
import cats.data.IorT


// Monad Transformers (e.g. OptionT, EitherT, IorT)
//  - Monad doesn't compose due to the one extral level indirection
//  - Functor composes
//  - Transformers complement their corresponding (monadic) data types
//  - You can build entire monad transformer stacks
//  - Contrary to other types monads do not compose 

case class User(name: String, age: Int)

object MTApp extends IOApp {
  def run(args: List[String]): IO[ExitCode] = 
    IO {
     // Option Functor
     val optOpt = Option(Option(User("name", 12))) 
     val r1 = optOpt.map(opt => opt.map(_.name))
     println(r1)
     val r2 = Functor[Option].compose[Option].map(optOpt)(_.name)
     println(r2)

     // Either Functor
     val eo = Either.right[Throwable, Option[User]](User("name", 77).some)
     val r3 = eo.map(o => o.map(_.name))
     println(r3)
     val r4 = Functor[Either[Throwable, *]].compose[Option].map(eo)(_.name)
     println(r4)

     // Monad transformers
     val eo2 = Either.right[Throwable, Option[User]](User("name", 77).some)
     val r5 = for {
       e <- eo
       e2 <- eo2
     } yield for {
       a <- e
       b <- e2
     } yield a.age + b.age
     println(r5)

     val r6: OptionT[Either[Throwable, *], Int] = for {
       a <- OptionT(eo)
       b <- OptionT(eo2)
     } yield a.age + b.age
     println(r6.value)


     val r7: OptionT[Either[Throwable, *], Int] = for {
       a <- OptionT(Either.left[Throwable, Option[User]](new Throwable("boom")))
       b <- OptionT(eo2)
     } yield a.age + b.age
     println(r7.value)

     r7.value.isLeft
     r7.isDefined


     case class TestResult(v: Int)
     case class Permission(s: String)
     val ior = Ior.right[TestResult, Permission](Permission("update-action"))
     val x = OptionT.liftF(Either.right(ior))
     IorT(x).map(p => println(s"permission is $p"))
    }.as(ExitCode.Success)
}