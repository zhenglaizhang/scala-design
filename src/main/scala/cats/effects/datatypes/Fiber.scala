package cats.effects.datatypes

import cats.effect.ContextShift
import cats.effect.IO

import scala.concurrent.ExecutionContext

// Fiber
// It represents the (pure) result of an Async data type (e.g. IO) being started concurrently and that can be either
// joined or canceled.
// You can think of fibers as being lightweight threads, a fiber being a concurrency primitive for doing cooperative
// multi-tasking.
object w2 {
  trait Fiber[F[_], A] {
    def cancel: F[Unit]
    def join: F[A]
  }
}
object Fiber1 extends App {
  import cats.effect.{Fiber, IO}
  import scala.concurrent.ExecutionContext.Implicits.global
  implicit val ctx = IO.contextShift(global)
  val io = IO({ println("calculating!"); 12 })
  val out = io.map(n => println(s"output: $n"))
  // For example a Fiber value is the result of evaluating IO.start:
  val fiber: IO[Fiber[IO, Unit]] = out.start
  fiber.unsafeRunSync().join.unsafeRunSync()
}

object Fiber2 extends App {
  implicit val contextShift: ContextShift[IO] = {
    IO.contextShift(ExecutionContext.global)

  }
  val launchMissiles: IO[Unit] = IO.raiseError(new Exception("boom"))
  val runToBunker = IO(println("To the bunker!"))
  val r: IO[Unit] = for {
    fiber <- launchMissiles.start
    _ <- runToBunker.handleErrorWith { err =>
      fiber.cancel *> IO.raiseError(err)
    }
    aftermath <- fiber.join
  } yield aftermath
  r.unsafeRunSync()
}
