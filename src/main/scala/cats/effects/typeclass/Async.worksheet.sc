import akka.io.IO
import cats.effect.kernel.Async
import akka.compat.Future
// Async
//  - A Monad that can describe asynchronous or synchronous computations that produce exactly one result.

object w {
  import cats.effect.{LiftIO, Sync}
  trait Async[F[_]] extends Sync[F] with LiftIO[F] {
    def async[A](k: (Either[Throwable, A] => Unit) => Unit): F[A]
  }
}

// This type class allows the modeling of data types that:
//  - Can start asynchronous processes.
//  - Can emit one result on completion.
//  - Can end in error
// Important: on the “one result” signaling, this is not an “exactly once” requirement. At this point streaming types can implement Async and such an “exactly once” requirement is only clear in Effect.

// An asynchronous task represents logic that executes independent of the main program flow, or current callstack. It can be a task whose result gets computed on another thread, or on some other machine on the network.
// In terms of types, normally asynchronous processes are represented as:
//    (A => Unit) => Unit
//  This signature can be recognized in the “Observer pattern” described in the “Gang of Four”, although it should be noted that without an onComplete event (like in the Rx Observable pattern) you can’t detect completion in case this callback can be called zero or multiple times.

// Some abstractions allow for signaling an error condition (e.g. MonadError data types), so this would be a signature that’s closer to Scala’s Future.onComplete:
//  (Either[Throwable, A] => Unit) => Unit

// And many times the abstractions built to deal with asynchronous tasks also provide a way to cancel such processes, to be used in race conditions in order to cleanup resources early:
//  (A => Unit) => Cancelable
// This is approximately the signature of JavaScript’s setTimeout, which will return a “task ID” that can be used to cancel it. Important: this type class in particular is NOT describing cancelable async processes, see the Concurrent type class for that.


// The async method has an interesting signature that is nothing more than the representation of a callback-based API function
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import cats.effect.unsafe.implicits.global
import cats.effect.IO
val apiCall = Future.successful("I come from the future")
val ioa: IO[String] = 
  Async[IO].async { cb => 
    import scala.util.{Failure, Success}
    apiCall.onComplete {
      case Failure(e) => cb(Left(e))
      case Success(v) => cb(Right(v))
    }
  }

ioa.unsafeRunSync()
// ioa will have a successful value A or it will be raise an error in the IO context.
