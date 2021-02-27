package cats.effects.datatypes

import cats.effect.{ExitCode, IO, IOApp, SyncIO}
import cats.Eval

// A pure abstraction representing the intention to perform a side effect,
// where the result of that side effect is obtained synchronously.

// SyncIO is similar to IO, but does not support asynchronous computations.
// Consequently, a SyncIO can be run synchronously to obtain a result via unsafeRunSync.
// This is unlike IO#unsafeRunSync, which cannot be safely called in general.
// Doing so on the JVM blocks the calling thread while the async part of the computation is run
// and doing so on Scala.js throws an exception upon encountering an async boundary.

// SyncIO defines an eval method in its companion object to lift any cats.Eval value.
// SyncIO also defines a to[F] method at the class level to lift your value into any F with a LiftIO instance available.
object SyncIOApp extends IOApp {
  def putStrLn(str: String): SyncIO[Unit] = SyncIO(println(str))
  SyncIO.pure("cats!").flatMap(putStrLn).unsafeRunSync()
  val eval = Eval.now("hey!")
  SyncIO.eval(eval).unsafeRunSync()
  def run(args: List[String]): IO[ExitCode] =
    putStrLn("wow").to[IO].as(ExitCode.Success)

  import cats.effect.IO

  val ioa: SyncIO[Unit] = SyncIO(println("Hello world!"))
  // ioa: SyncIO[Unit] = SyncIO$1965096469
  val iob: IO[Unit] = ioa.to[IO]
  // iob: IO[Unit] = Delay(thunk = <function0>)
  iob.unsafeRunAsync(_ => ())
}
