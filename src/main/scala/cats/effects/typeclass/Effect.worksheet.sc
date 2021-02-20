// Effect
// A Monad that can suspend side effects into the F[_] context and supports lazy and potentially asynchronous evaluation.
object w {
  import cats.effect.{Async, IO, SyncIO}
  trait Effect[F[_]] extends Async[F] {
    def runAsync[A](fa: F[A])(cb: Either[Throwable, A] => IO[Unit]): SyncIO[Unit]
  }
}


// This type class is describing data types that:
//  - Implement the Async algebra.
//  - Implement a lawful Effect.runAsync operation that triggers the evaluation (in the context of SyncIO).
// Note: this is the safe and generic version of IO.unsafeRunAsync (aka Haskell’s unsafePerformIO).


// runAsync
//  - It represents the intention to evaluate a given effect in the context of F[_] asynchronously giving you back a SyncIO[A]
import cats.effect.{SyncIO, IO}
