package cats.effects.concurrent

// Ref
//  - A pure atomic reference
//  - mutual exclusion (communication channel)
//  - cannot be empty
//  - always initialized to a value
//  - modify is atomic
//  - allows concurrent update
//  - e.g. concurrent counter, cache
//  - Ref + Deferred can be used to build more complex structures

object ref1 {
  abstract class Ref[F[_], A] {
    def get: F[A]
    def set(a: A): F[Unit]
    def modify[B](f: A => (A, B)): F[B]
    // ...
  }
}
// Provides safe concurrent access and modification of its content, but no functionality for synchronisation, which
// is instead handled by Deferred.
// For this reason, a Ref is always initialised to a value.
//
//The default implementation is nonblocking and lightweight, consisting essentially of a purely functional wrapper
// over an AtomicReference.

// Concurrent Counter

import cats.effect.{ExitCode, IO, IOApp, Sync}
import cats.effect.concurrent.Ref
import cats.syntax.all._

import scala.concurrent.ExecutionContext

class Worker[F[_]](number: Int, ref: Ref[F, Int])(implicit F: Sync[F]) {
  private def putStrLn(v: String): F[Unit] = F.delay(println(v))
  def start: F[Unit] =
    for {
      c1 <- ref.get
      _ <- putStrLn(show"#$number >> $c1")
      c2 <- ref.modify(x => (x + 1, x))
      _ <- putStrLn(show"#$number >> $c2")
    } yield ()
}

// Needed for triggering evaluation in parallel
object RefApp extends IOApp {
  // Needed for triggering evaluation in parallel
  implicit val ctx = IO.contextShift(ExecutionContext.global)

  val program: IO[Unit] =
    for {
      ref <- Ref.of[IO, Int](0)
      w1 = new Worker[IO](1, ref)
      w2 = new Worker[IO](2, ref)
      w3 = new Worker[IO](3, ref)
      _ <- List(w1.start, w2.start, w3.start).parSequence.void
    } yield ()

  def run(args: List[String]): IO[ExitCode] =
    program.as(ExitCode.Success)
//    program *> IO(ExitCode.Success)
}
