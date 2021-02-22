package cats.effects.concurrent

import cats.effect.concurrent.Semaphore
import cats.effect.{Concurrent, ExitCode, IO, IOApp, Timer}
import cats.syntax.all._

// Semaphore
//  - synchronization and mutual exclusion
//  - non-negative number of permits available
//  - acquire decrements the number
//  - release increases the number
//  - acquire semantically blocks if no permits are available until a permit becomes available
//  - e.g. shared resource producer/consumer channel, philosophers eating with two forks
//
//  - Semaphore does what we call “semantic” blocking, meaning that no actual threads are being blocked while waiting
//  to acquire a permit.
//
//  Behavior on cancellation:
//    - Blocking acquires are cancelable if the semaphore is created with Semaphore.apply (and hence, with a
//      Concurrent[F] instance).
//    - Blocking acquires are non-cancelable if the semaphore is created with Semaphore.uncancelable (and hence, with an
//      Async[F] instance).

object s {
  abstract class Semaphore[F[_]] {
    def available: F[Long]
    def acquire: F[Unit]
    def release: F[Unit]
    // ... and more
  }
}

import scala.concurrent.duration._
class PreciousResource[F[_]](name: String, s: Semaphore[F])(implicit
    F: Concurrent[F],
    timer: Timer[F]
) {
  def use: F[Unit] =
    for {
      x <- s.available
      _ <- F.delay(println(s"$name >> Availablility: $x"))
      _ <- s.acquire
      y <- s.available
      _ <- F.delay(println(s"$name >> Started | Availability: $y"))
      _ <- timer.sleep(3.seconds)
      _ <- s.release
      z <- s.available
      _ <- F.delay(println(s"$name >> Done | Availability: $z"))
    } yield ()
}

object S1 extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    (for {
      s <- Semaphore[IO](1)
      r1 = new PreciousResource[IO]("R1", s)
      r2 = new PreciousResource[IO]("R2", s)
      r3 = new PreciousResource[IO]("R3", s)
      _ <- List(r1.use, r2.use, r3.use).parSequence.void
    } yield ()).as(ExitCode.Success)
}
