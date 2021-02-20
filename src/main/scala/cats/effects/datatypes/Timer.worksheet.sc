import cats.effect.kernel.Clock
// Timer
// It is a scheduler of tasks. You can think of it as the purely functional equivalent of:

// Java’s ScheduledExecutorService.
// JavaScript’s setTimeout.
// It provides:

// The ability to get the current time.
// Ability to delay the execution of a task with a specified time duration.
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