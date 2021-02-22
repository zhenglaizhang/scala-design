import cats.effect.internals.IOAppPlatform
import cats.effect.{Clock, IO, IOApp, Timer}

import java.util.concurrent.ScheduledExecutorService
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{
  FiniteDuration,
  MILLISECONDS,
  NANOSECONDS,
  TimeUnit
}
// Timer
// It is a scheduler of tasks. You can think of it as the purely functional equivalent of:
//  - Java’s ScheduledExecutorService.
//  - JavaScript’s setTimeout.
//
// It provides:
//  - The ability to get the current time.
//  - Ability to delay the execution of a task with a specified time duration.
// It does all of that in an F[_] monadic context that can suspend side effects and is capable of asynchronous execution (e.g. IO).

// This is NOT a typeclass, as it does not have the coherence requirement.

object w {
  import scala.concurrent.duration.FiniteDuration
  trait Timer[F[_]] {
    def clock: Clock[F]
    def sleep(duration: FiniteDuration): F[Unit]
  }
}

// There’s a default instance of Timer[IO] available. However, you might want to implement your own to have a fine-grained control over your thread pools
//IOApp
//implicit protected def timer: Timer[IO] =
//  IOAppPlatform.defaultTimer

IOApp
final class MyTimer(ec: ExecutionContext, sc: ScheduledExecutorService)
    extends Timer[IO] {
  override def clock: Clock[IO] =
    new Clock[IO] {
      override def realTime(unit: TimeUnit): IO[Long] =
        IO(unit.convert(java.lang.System.currentTimeMillis(), MILLISECONDS))
      override def monotonic(unit: TimeUnit): IO[Long] =
        IO(unit.convert(java.lang.System.nanoTime, NANOSECONDS))
    }

  override def sleep(duration: FiniteDuration): IO[Unit] =
    IO.cancelable { cb: (Either[Throwable, Unit] => Unit) =>
      val tick = new Runnable {
        override def run(): Unit =
          ec.execute(new Runnable {
            override def run(): Unit = cb(Right(()))
          })
      }
      val f = sc.schedule(tick, duration.length, duration.unit)
      IO(f.cancel(false)).void
    }

}
// The one-argument overload of IO.timer lazily instantiates a global ScheduledExecutorService, which is never shut down. This is fine for most applications, but leaks threads when the class is repeatedly loaded in the same JVM, as is common in testing.
// cats.effect.global_scheduler.threads.core_pool_size: sets the core pool size of the global scheduler. Defaults to 2.
//cats.effect.global_scheduler.keep_alive_time_ms: allows the global scheduler’s core threads to timeout and
// terminate when idle. 0 keeps the threads from timing out. Defaults to 0. Value is in milliseconds.
