package zio1

import zio.console._
import zio._
import zio.ExitCode

// ZIO is a library for asynchronous and concurrent programming that is based on pure functional programming.


/*

The ZIO[R, E, A] data type has three type parameters:
  R - Environment Type. The effect requires an environment of type R. If this type parameter is Any, it means the effect has no requirements, because you can run the effect with any value (for example, the unit value ()).
  E - Failure Type. The effect may fail with a value of type E. Some applications will use Throwable. If this type parameter is Nothing, it means the effect cannot fail, because there are no values of type Nothing.
  A - Success Type. The effect may succeed with a value of type A. If this type parameter is Unit, it means the effect produces no useful information, while if it is Nothing, it means the effect runs forever (or until failure).


For example, an effect of type ZIO[Any, IOException, Byte] has no requirements, may fail with a value of type IOException, or may succeed with a value of type Byte.
A value of type ZIO[R, E, A] is like an effectful version of the following function type:
  R => Either[E, A]
This function, which requires an R, might produce either an E, representing failure, or an A, representing success. ZIO effects are not actually functions, of course, because they model complex effects, like asynchronous and concurrent effects.



The ZIO data type is the only effect type in ZIO. However, there are a family of type aliases and companion objects that simplify common cases:
 - UIO[A] — This is a type alias for ZIO[Any, Nothing, A], which represents an effect that has no requirements, and cannot fail, but can succeed with an A.
 - URIO[R, A] — This is a type alias for ZIO[R, Nothing, A], which represents an effect that requires an R, and cannot fail, but can succeed with an A.
 - Task[A] — This is a type alias for ZIO[Any, Throwable, A], which represents an effect that has no requirements, and may fail with a Throwable value, or succeed with an A.
 - RIO[R, A] — This is a type alias for ZIO[R, Throwable, A], which represents an effect that requires an R, and may fail with a Throwable value, or succeed with an A.
 - IO[E, A] — This is a type alias for ZIO[Any, E, A], which represents an effect that has no requirements, and may fail with an E, or succeed with an A.
These type aliases all have companion objects, and these companion objects have methods that can be used to construct values of the appropriate type.


the Task type, which has a single type parameter, and corresponds most closely to the Future data type 
If you are using Cats Effect libraries, you may find the RIO type useful, since it allows you to thread environments through third-party libraries and your application.
UIO can be useful for describing infallible effects, including those resulting from handling all errors.
*/

object MyApp extends zio.App {
  def run(args: List[String]): zio.URIO[zio.ZEnv,ExitCode] = myAppLogic.exitCode

  val myAppLogic = 
    for {
      _ <- putStrLn("Hello! What's your name?")
      name <- getStrLn
      _ <- putStrLn(s"Hello ${name}, welcome to ZIO!")
    } yield ()
}

object IntegrationExample {
  // https://www.reddit.com/r/scala/comments/cvmo04/difference_between_akka_and_zio/
  // If you are integrating ZIO into an existing application, using dependency injection, or do not control your main function, then you can create a runtime system in order to execute your ZIO programs:
  // If you are integrating ZIO into an existing application, using dependency injection, or do not control your main function, then you can create a runtime system in order to execute your ZIO programs:
  val runtime = Runtime.default
  runtime.unsafeRun(Task(println("Hello world")))
}

object snippets {
  val echo = getStrLn.flatMap(l => putStrLn(l))
}