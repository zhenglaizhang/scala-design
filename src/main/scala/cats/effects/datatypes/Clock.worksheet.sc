// Clock provides the current time, as a pure alternative to:
//  - Java’s System.currentTimeMillis for getting the “real-time clock”
//  - System.nanoTime for a monotonic clock useful for time measurements
//  - JavaScript’s Date.now() and performance.now()
//
// The reason for providing this data type is two-fold:
// - the exposed functions are pure, with the results suspended in F[_], no reason to reinvent the wheel and write your own wrappers
// - requiring this data type as a restriction means that code using Clock can have alternative implementations injected; for example time passing can be simulated in tests, such that time-based logic can be tested much more deterministically and with better performance, without actual delays happening

object w {
  import scala.concurrent.duration.TimeUnit
  trait Clock[F[_]] {
    def realTime(unit: TimeUnit): F[Long]
    def monotonic(unit: TimeUnit): F[Long]
  }
}

import cats.effect._
import cats.syntax.all._
import scala.concurrent.duration.MILLISECONDS

def measure[F[_], A](
    fa: F[A]
)(implicit F: Sync[F], clock: Clock[F]): F[(A, Long)] = {

  for {
    start <- clock.monotonic(MILLISECONDS)
    result <- fa
    finish <- clock.monotonic(MILLISECONDS)
  } yield (result, finish - start)
}

import java.lang.System
System.nanoTime
System.currentTimeMillis
