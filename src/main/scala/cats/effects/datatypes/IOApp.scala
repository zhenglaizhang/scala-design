package cats.effects.datatypes

import cats.data.EitherT
import cats.effect.ExitCase.Canceled
import cats.effect._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

object IOApp1 {
  // needed for IO.sleep
  implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)

  def program(args: List[String]): IO[Unit] =
    IO.sleep(1.second) *> IO(println(s"Hello world!. Args: $args"))

  def main(args: Array[String]): Unit =
    program(args.toList).unsafeRunSync()
}

object IOApp2 extends IOApp {
  // we use an ExitCode to specify success or an error code, the implementation handling how that is returned and thus you no longer have to deal with a side-effectful Runtime.exit call
  // the Timer[IO] dependency is already provided by IOApp, so on top of the JVM there’s no longer a need for an implicit ExecutionContext to be in scope
  def run(args: List[String]): IO[ExitCode] =
    args.headOption match {
      case Some(name) => IO(println(s"Hello $name")).as(ExitCode.Success)
      case None       => IO(System.err.println("Usage: MyApp name")).as(ExitCode(2))
    }
}

// The cats.effect.IO implementation is cancelable and so is IOApp.
// This means that when IOApp receives a SIGABORT, SIGINT or another interruption signal that can be caught, then the
// IO app will cancel and safely release any resources.

// If you run your IOApp program from sbt, you may observe cancellation and resource releasing is not happening. This is due to sbt, by default, running programs in the same JVM as sbt, so when your program is canceled sbt avoids stopping its own JVM. To properly allow cancellation, ensure your progam is forked into its own JVM via a setting like fork := true in your sbt configuration.
object IOAppCancellable extends IOApp {
  def loop(n: Int): IO[ExitCode] =
    IO.suspend {
      if (n < 10) IO.sleep(1.second) *> IO(println(s"Tick: $n")) *> loop(n + 1)
      else IO.pure(ExitCode.Success)
    }
  def run(args: List[String]): IO[ExitCode] =
    loop(0).guaranteeCase {
      case Canceled => IO(println("Interrupted: releasing and exiting!"))
      case _        => IO(println("Normal exit"))
    }
  // Ctrl-C or do a kill $pid
  // Therefore IOApp automatically installs an interruption handler for you.
}

// Therefore IOApp automatically installs an interruption handler for you.
// Different F[_] data types have different requirements for evaluation at the end of the world.
// For example cats.effect.IO now needs a ContextShift[IO] in scope for working with Concurrent and thus for getting the ConcurrentEffect necessary to evaluate an IO. It also needs a Timer[IO] in scope for utilities such as IO.sleep and timeout.

//ContextShift and Timer are provided by the environment and in this case the environment is the IOApp. Monix’s Task however has global ContextShift[Task] and Timer[Task] always in scope and doesn’t need them, but it does need a Scheduler to be available for the necessary Effect instance. And both Cats-Effect’s IO and Monix’s Task are cancelable, in which case it is desirable for the IOApp / TaskApp to install shutdown handlers to execute in case of interruption, however our type classes can also work with non-cancelable data types, in which case handling interruption is no longer necessary.
//Long story short, it’s better for IOApp to be specialized and each F[_] can come with its own app data type that is better suited for its needs. For example Monix’s Task comes with its own TaskApp.
//That said IOApp can be used for any F[_], because any Effect or ConcurrentEffect can be converted to IO. Example:

object IOApp3 extends IOApp {
  type F[A] = EitherT[IO, Throwable, A]
//  val F = implicitly[Effect[F]]
  val F = implicitly[ConcurrentEffect[F]]
  def run(args: List[String]) =
    F.toIO {
      EitherT
        .right(IO(println("Hello from EitherT")))
        .map(_ => ExitCode.Success)
    }
}

//IOApp is awesome for describing pure FP programs and gives you functionality that does not come for free when using the normal Java main protocol, like the interruption handler.
//And we worked hard to make this behavior available on top of JavaScript, via Scala.js, so you can use this for your Node.js apps as well.
